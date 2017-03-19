/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.SpanWeight.Postings;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Sean_S325
 */
public class LumQuery {

    private IndexReader idx_reader = null;

    public LumQuery(String index_dir) throws IOException, ParseException {
        Directory index = FSDirectory.open(Paths.get(index_dir));
        idx_reader = DirectoryReader.open(index);

    }

    public void query(String query_str) throws ParseException, IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Query q = new QueryParser("content", analyzer).parse(query_str);
        int hitsPerPage = 10;
        IndexSearcher searcher = new IndexSearcher(idx_reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        System.out.println("Found " + hits.length + "hits. ");
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println(d.get("content"));
            // System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
        }

    }

    public int getTermFreq(String term, String field) throws IOException {
        Term term_inst = new Term(field, term);
        return (int) (idx_reader.totalTermFreq(term_inst));
    }

    public int span_query(String term, String field, boolean useNearQuery) throws IOException {
        if (term.length() == 0) return -1;
        
        SpanQuery sq = null;
        if(!useNearQuery){
            sq = new SpanTermQuery(new Term(field, term));        
        } else {
        
            SpanNearQuery.Builder builder = new SpanNearQuery.Builder(field, true);
            for(int i = 0; i < term.length(); ++i){
                builder.addClause(new SpanTermQuery(new Term(field, term.substring(i,i+1))));
            }        
            sq = builder.build();
        }
        
        IndexSearcher searcher = new IndexSearcher(idx_reader);
        for (LeafReaderContext ctx : idx_reader.leaves()) {
            SpanWeight weights = sq.createWeight(searcher, false);
            if(weights == null) continue;
            Spans spans = weights.getSpans(ctx, Postings.POSITIONS);
            if (spans == null){
                System.out.printf("Nothing found for %s%n", term);
                continue;
            }
            int nxtDoc = 0;
            
            while ((nxtDoc = spans.nextDoc()) != Spans.NO_MORE_DOCS) {                
                String doc_content = searcher.doc(nxtDoc).get(field);
                String base_ref = searcher.doc(nxtDoc).get("baseref");
                long base_uuid = Long.parseLong(base_ref, 16);
                IntFunction<Integer> min_guard = (int x)->Math.max(0, x-3);
                IntFunction<Integer> max_guard = (int x)->Math.min(doc_content.length()-1, x);
                
                List<int[]> tup_list = new ArrayList<>();
                LumWindow lumWin = new LumWindow(base_uuid, idx_reader);
                while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {                                        
                    int[] span_arr = {spans.startPosition(), spans.endPosition()};
                    tup_list.add(span_arr);
                    System.out.printf("%d, %d, %d%n", nxtDoc, span_arr[0], span_arr[1]); 
                    System.out.printf("%s%n", lumWin.GetWindow(5, span_arr[0], span_arr[1]));
                }                                              
                
            }
        }

        // OffsetTermVectorMapper tvm = new OffsetTermVectorMapper();
        return 0;
    }

    public void ListTerm(int docId) throws IOException{
        
        Terms terms = idx_reader.getTermVector(docId, "content");
        TermsEnum term_enum = terms.iterator();
        while (term_enum.next() != null) {
            System.out.printf("%s", term_enum.term().utf8ToString());
            PostingsEnum post_enum = term_enum.postings(null, PostingsEnum.POSITIONS);
            post_enum.nextDoc();
            int freq = post_enum.freq();
            System.out.printf("%d: ", freq);
            for (int i = 0; i < freq; ++i) {
                System.out.printf("%d, ", post_enum.nextPosition());
            }
            System.out.printf("%n");
        }
    }

    public void close() throws IOException {
        idx_reader.close();
    }
}
