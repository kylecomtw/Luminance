/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.corpus.compute;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;
import tw.com.kyle.luminance.TextUtils;

/**
 *
 * @author Sean
 */
public class CollocateFromIndex {
    private class FreqInfo {
        int nFragments = 0;
        int nDocuments = 0;
        int nTokens = 0;
    }
    private IndexReader reader = null;
    private IndexSearcher searcher = null;
    private Logger logger = Logger.getLogger(CollocateFromIndex.class.getName());
    private Map<String, FreqInfo> mwe_map = new HashMap<>();
    private Set<Integer> doc_set = null;
    private long total_token_freq = 0;
    
    public CollocateFromIndex(IndexReader r) {
        reader = r;
        searcher = new IndexSearcher(reader);
    }
        
    public void GetAllMwe(String field_name) {
        mwe_map.clear();     
        List<Integer> mwe_history = new ArrayList<>();
        try {
            doc_set = get_discourse_doc_id().stream().collect(Collectors.toSet());
            Set<Integer> all_terms = get_all_terms(field_name, 1e-5).stream()
                                     .map((x)->(int)x.charAt(0))
                                     .collect(Collectors.toSet());
            System.out.printf("Valid term set size: %d%n", all_terms.size());
            // Set<Integer> all_terms = null;
                        
            DocumentWalker doc_walker = new DocumentWalker(reader);           
            int max_doc = doc_walker.length();
            int doc_i = 0;
            while(doc_walker.Walk()){
                String doc_content = doc_walker.GetContent();
                if (doc_i % 100 == 0) {
                    System.out.printf("Document %d, %d: %d/%d: MWE found %d%n", 
                            doc_walker.DocId(), doc_walker.DocStep(), 
                            doc_i, max_doc, mwe_map.size());                    
                }
                
                if (doc_i % 1000 == 0) {
                    WriteJson(String.format("h:/check_point_%05d.json", doc_i));
                }
                
                final int MAX_WIN = 10;
                final int MIN_WIN = 4;
                final int MIN_FREQ = (int)Math.ceil(total_token_freq * 5e-6);
                find_mwe(doc_content, MAX_WIN, MIN_WIN, MIN_FREQ, all_terms);
                
                doc_i += 1;
                mwe_history.add(mwe_map.size());
                
                if(mwe_history.size() > 1000 && doc_i % 100 == 0){
                    int n_hist = mwe_history.size();
                    double prev_100 = mwe_history.stream().skip(n_hist - 200).limit(100)
                                      .mapToInt((x)->x)
                                      .average().getAsDouble();
                    double cur_100 = mwe_history.stream().skip(n_hist - 100).limit(100)
                                      .mapToInt((x)->x)
                                      .average().getAsDouble();
                    System.out.printf("Terminate condition (cur100 - prev100): %.2f%n", 
                            cur_100 - prev_100);
                    if(cur_100 - prev_100 < 10) {
                        System.out.println("Termination condition satisfied");
                        break;
                    }                    
                }
                // if(doc_i > 30) break;
            }                        
        } catch (IOException ex) {
            Logger.getLogger(CollocateFromIndex.class.getName()).log(Level.SEVERE, null, ex);
        }

        return;
    }
    
    public void ExcludeEnclosed() {
        Set<String> excl_set = new HashSet<>();
        List<String> mwe_list = new ArrayList(mwe_map.keySet());
        for(int i = 0; i < mwe_list.size(); ++i){
            String mwe_x = mwe_list.get(i);
            for(int j = 0; j < i; ++j){
                String mwe_y = mwe_list.get(j);
                if(mwe_x.contains(mwe_y)){
                    excl_set.add(mwe_y);
                }
                
                if(mwe_y.contains(mwe_x)){
                    excl_set.add(mwe_x);
                }
            }
        }
        
        mwe_map = mwe_map.entrySet().stream()
                    .filter((x)->!excl_set.contains(x.getKey()))
                    .collect(Collectors.toMap((x)->x.getKey(), (x)->x.getValue()));
    }
    
    public void WriteJson(String outpath) throws IOException {
        JsonArray root = new JsonArray();
        Gson gson = new Gson();
        List<String> mwe_list = mwe_map.entrySet().stream()
                                .sorted(Comparator.comparing(
                                        (x)->-x.getValue().nTokens))
                                .map((x)->x.getKey())
                                .collect(Collectors.toList());
        for(String mwe:mwe_list){
            FreqInfo finfo = mwe_map.get(mwe);
            JsonObject obj_x = new JsonObject();
            JsonObject freq_x = new JsonObject();
            freq_x.addProperty("nT", finfo.nTokens);
            freq_x.addProperty("nD", finfo.nDocuments);
            freq_x.addProperty("nF", finfo.nFragments);            
            obj_x.addProperty("L", mwe);
            obj_x.add("f", freq_x);
            root.add(obj_x);
        }
        
        try {
            JsonWriter writer = new JsonWriter(new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outpath), StandardCharsets.UTF_8)));
            writer.setIndent("  ");
            gson.toJson(root, writer);
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CollocateFromIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private List<String> get_all_terms(String field_name, double min_prob) {
        Map<String, Integer> term_map = new HashMap<String, Integer>();
        long total_freq = 0;
        try {
            for (LeafReaderContext leaf : reader.leaves()) {
                Terms terms = leaf.reader().terms("content");
                TermsEnum term_enum = terms.iterator();
                while (term_enum.next() != null) {                    
                    int freq = (int)term_enum.totalTermFreq();
                    String term_str = term_enum.term().utf8ToString();
                    if(term_str.length() == 1 && TextUtils.checkCJK(term_str.charAt(0))){
                        System.out.printf("%s: %d%n", term_str, freq);
                        term_map.put(term_str, term_map.getOrDefault(term_str, 0) + freq);
                        total_freq += freq;
                    }
                    
                }
                System.out.printf("%n");
            }
        } catch (IOException ex) {
            logger.severe(ex.toString());
        }
        
        int min_freq = (int)Math.ceil(total_freq * min_prob);
        logger.info(String.format("total token frequency: %d", total_freq));
        logger.info(String.format("threshold terms at %g, %d time(s)", min_prob, min_freq));
        List<String> term_list = term_map.entrySet().stream()
                                .filter((x)->x.getValue() > min_freq)
                                .sorted(Comparator.comparing((x)->-x.getValue()))
                                .map((x)->x.getKey())
                                .collect(Collectors.toList());
        
        total_token_freq = total_freq;
        return term_list;
    }

    private FreqInfo query_ngram(String ngram) throws IOException {
        SpanQuery sq = null;
        final String FIELD = "content";
        SpanNearQuery.Builder builder = new SpanNearQuery.Builder(FIELD, true);
        if (ngram.length() > 1) {
            for (int i = 0; i < ngram.length(); ++i) {
                builder.addClause(new SpanTermQuery(new Term(FIELD, ngram.substring(i, i + 1))));
            }
            sq = builder.build();
        } else {
            sq = new SpanTermQuery(new Term(FIELD, ngram));  
        }

        int all_frag_freq = 0;
        int all_tok_freq = 0;        
        int doc_freq = 0;
        for (LeafReaderContext ctx : reader.leaves()) {

            SpanWeight weights = sq.createWeight(searcher, false);
            if (weights == null) {
                continue;
            }
            Spans spans = weights.getSpans(ctx, SpanWeight.Postings.POSITIONS);
            if (spans == null) {
                // System.out.printf("Nothing found for %s%n", ngram);
                continue;
            }
            int nxtDoc = 0;

            while ((nxtDoc = spans.nextDoc()) != Spans.NO_MORE_DOCS) {
                all_frag_freq += 1;
                if(doc_set.contains(nxtDoc)){
                    doc_freq += 1;
                }
                while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
                    all_tok_freq += 1;
                }
            }
        }

        // System.out.printf("Occurrence frag: %d, doc: %d(tok: %d)%n", all_frag_freq, discourse_freq, tok_freq);        
        FreqInfo freq_info = new FreqInfo();
        freq_info.nDocuments = doc_freq;
        freq_info.nFragments = all_frag_freq;
        freq_info.nTokens = all_tok_freq;
        return freq_info;
    }

    private List<Integer> get_discourse_doc_id() throws IOException {
        Query cq = new TermQuery(new Term("class", "discourse"));
        IndexSearcher searcher = new IndexSearcher(reader);
        Weight w = cq.createWeight(searcher, false);
        List<Integer> docid_list = new ArrayList<>();
        for (LeafReaderContext ctx : reader.leaves()) {
            Scorer scorer = w.scorer(ctx);
            DocIdSetIterator doc_it = scorer.iterator();
            int nxtDoc = 0;
            while ((nxtDoc = doc_it.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                int doc_freq = scorer.freq();
                docid_list.add(nxtDoc);
            }
        }
        return docid_list;
    }
    
    private void find_mwe(String content, int max_win, int min_win, int min_freq, Set<Integer> valid_set) 
            throws IOException{
        int txt_cursor = 0;
        int txt_length = content.length();        
        
        while(txt_cursor < txt_length){
            for(int win = max_win; win >= min_win; --win){                
                String probe = content.substring(txt_cursor, Math.min(txt_cursor + win, txt_length));                
                if (probe.length() < min_win || 
                    mwe_map.containsKey(probe)) {
                    //! remeber to substract one to make room for the 
                    //! increment in the end of while-loop
                    txt_cursor += probe.length() - 1;
                    break;
                }
                
                //! check all characters are valid (occur more than threshold)
                Boolean all_valid = true;
                if (valid_set != null){
                    all_valid = probe.chars().allMatch((x)->valid_set.contains(x));
                }
                
                if (!all_valid || !TextUtils.checkCJK(probe)) continue;
                
                //! query the index
                FreqInfo freq = query_ngram(probe);
                if (freq.nTokens > min_freq) {
                    // System.out.printf("Occurence %s: %d%n", probe, freq);
                    mwe_map.put(probe, freq);
                    //! remeber to substract one to make room for the 
                    //! increment in the end of while-loop
                    txt_cursor += probe.length() - 1;
                    break;
                }
            }
            txt_cursor += 1;            
        }
                
    }
}
