/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
;
import java.util.List;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;

/**
 *
 * @author Simport org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
ean
 */
public class Concordance {    
    IndexReader idx_reader = null;
    public Concordance(IndexReader _reader){
        idx_reader = _reader;
    }
    
    public List<ConcordanceResult> query(String term, String field, boolean useNearQuery) 
        throws IOException{
        
        List<ConcordanceResult> con_list = new ArrayList<>();
        
        if (term.length() == 0) return null;
        
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
            Spans spans = weights.getSpans(ctx, SpanWeight.Postings.POSITIONS);
            if (spans == null){
                System.out.printf("Nothing found for %s%n", term);
                continue;
            }
            int nxtDoc = 0;
            
            LeafReader leaf_reader = ctx.reader();
            while ((nxtDoc = spans.nextDoc()) != Spans.NO_MORE_DOCS) {     
                
                String doc_content = leaf_reader.document(nxtDoc).get(field);                
                Document targ_doc = leaf_reader.document(nxtDoc);
                String base_ref = leaf_reader.document(nxtDoc).get("baseref");                
                
                long base_uuid = 0;
                LumWindow lumWin = new LumWindow();
                lumWin.initialize(targ_doc, idx_reader);

                while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {                                        
                    int[] span_arr = {spans.startPosition(), spans.endPosition()};

                    System.out.printf("%d, %d, %d%n", nxtDoc, span_arr[0], span_arr[1]); 
                    System.out.printf("%s%n", lumWin.GetWindow(5, span_arr[0], span_arr[1]));
                    ConcordanceResult concord = new ConcordanceResult();
                    String[] win_arr = lumWin.GetWindowAsArray(5, span_arr[0], span_arr[1]);
                    // concord.prec_context = win_arr[0];
                    // concord.target = win_arr[1];
                    // concord.succ_context = win_arr[2];
                    con_list.add(concord);
                }                                              
                
            }
            
            
        }

        // OffsetTermVectorMapper tvm = new OffsetTermVectorMapper();
        return con_list;
    }
}
