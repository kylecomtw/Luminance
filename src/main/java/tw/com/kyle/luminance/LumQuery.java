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
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
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
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.SpanWeight.Postings;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

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
                                                                
                IntFunction<Integer> min_guard = (int x)->Math.max(0, x-3);
                IntFunction<Integer> max_guard = (int x)->Math.min(doc_content.length()-1, x);
                
                List<int[]> tup_list = new ArrayList<>();
                while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {                                        
                    int[] span_arr = {spans.startPosition(), spans.endPosition()};
                    tup_list.add(span_arr);
                    System.out.printf("%d, %d, %d%n", nxtDoc, span_arr[0], span_arr[1]); 
                }        
                
                TokenStream tokenStream = TokenSources.getTermVectorTokenStreamOrNull(
                        field, 
                        idx_reader.getTermVectors(nxtDoc), -1);
                OffsetAttribute offsetAttr = tokenStream.getAttribute(OffsetAttribute.class);
                PositionIncrementAttribute posincAttr = tokenStream.getAttribute(PositionIncrementAttribute.class);
                PositionLengthAttribute poslenAttr = tokenStream.getAttribute(PositionLengthAttribute.class);
                PayloadAttribute payAttr = tokenStream.getAttribute(PayloadAttribute.class);
                
                int pos_counter = 0;
                int last_token_end = -1; // a work-around for filtered token
                int[] offset_pair = {-1, -1};
                while(tokenStream.incrementToken()){                    
                    final int cur_pos = pos_counter;                                        
                    boolean spos_matched = tup_list.stream().map((x) -> x[0] == cur_pos).anyMatch((y)->y);
                    boolean epos_matched = tup_list.stream().map((x) -> x[1] == cur_pos).anyMatch((y)->y);
                    if (spos_matched) offset_pair[0] = offsetAttr.startOffset();
                    if (epos_matched) offset_pair[1] = last_token_end;
                    
                    last_token_end = offsetAttr.endOffset();
                    pos_counter += posincAttr.getPositionIncrement();

                    if (offset_pair[0] >= 0 && offset_pair[1] >= 0){
                        ByteBuffer bbuf = ByteBuffer.wrap(payAttr.getPayload().bytes);                        
                        int so = bbuf.getInt();
                        int eo = bbuf.getInt();
                                                                    
                        System.out.printf("%s - %s - %s %n", 
                            doc_content.substring(min_guard.apply(so - 3), max_guard.apply(so)),
                            doc_content.substring(min_guard.apply(so), max_guard.apply(eo)),
                            doc_content.substring(min_guard.apply(eo), max_guard.apply(eo + 3)));
                        
                        offset_pair[0] = -1; offset_pair[1] = -1;
                    }                    
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
