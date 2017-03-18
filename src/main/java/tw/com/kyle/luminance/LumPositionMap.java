/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.util.List;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.search.highlight.TokenSources;

/**
 *
 * @author Sean
 */
public class LumPositionMap {
    public class PosInfo {
        List<String> tokens = null;
        List<Integer> pos_list = null;
    }
    
    private IndexReader reader = null;
    public LumPositionMap(IndexReader r) {
        reader = r;
    }
    
    public PosInfo GetPositions(int doc_id, String field) throws IOException {
        Terms terms = reader.getTermVector(doc_id, field);
        TokenStream tstream = TokenSources.getTermVectorTokenStreamOrNull(
                field, reader.getTermVectors(doc_id), -1);
        PosInfo pos_info = new PosInfo();
        
        CharTermAttribute termAttr = tstream.getAttribute(CharTermAttribute.class);
        PositionIncrementAttribute posIncAttr = tstream.getAttribute(PositionIncrementAttribute.class);
        // PositionLengthAttribute posLenAttr = tstream.getAttribute(PositionLengthAttribute.class);
        
        int pos_counter = 0;
        while(tstream.incrementToken()){
            pos_info.tokens.add(termAttr.toString());
            pos_info.pos_list.add(pos_counter);
            pos_counter += posIncAttr.getPositionIncrement();
        }
        
        return pos_info;        
    }
        
    public int FindPosition(PosInfo pos_info, String term, int start){
        List<String> tokens = pos_info.tokens;                
        int idx = tokens.subList(start, tokens.size()).indexOf(term);
        if (idx >= 0) {
            return idx + start;
        } else {
            return idx;            
        }        
    }
    
    
}
