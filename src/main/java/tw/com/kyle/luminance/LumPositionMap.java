/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
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
    
    private LumPositionMap(List<String> t, List<Integer> p) {
        pos_info = new PosInfo();
        pos_info.tokens = t;
        pos_info.pos_list = p;
    }
    
    private PosInfo pos_info = null;
    
    public static LumPositionMap Get(IndexReader reader, int doc_id, String field) throws IOException {
        Terms terms = reader.getTermVector(doc_id, field);
        TokenStream tstream = TokenSources.getTermVectorTokenStreamOrNull(
                field, reader.getTermVectors(doc_id), -1);        
                        
        CharTermAttribute termAttr = tstream.getAttribute(CharTermAttribute.class);
        PositionIncrementAttribute posIncAttr = tstream.getAttribute(PositionIncrementAttribute.class);
        // PositionLengthAttribute posLenAttr = tstream.getAttribute(PositionLengthAttribute.class);
        
        List<String> tokens = new ArrayList<>();
        List<Integer> pos_list = new ArrayList<>();
        
        int pos_counter = 0;
        while(tstream.incrementToken()){
            tokens.add(termAttr.toString());
            pos_list.add(pos_counter);
            pos_counter += posIncAttr.getPositionIncrement();
        }
        
        return new LumPositionMap(tokens, pos_list);
    }
        
    public int FindPosition(String term, int start){
        List<String> tokens = pos_info.tokens;                
        int idx = tokens.subList(start, tokens.size()).indexOf(term);
        if (idx >= 0) {
            return pos_info.pos_list.get(start + idx);
        } else {
            return idx;
        }        
    }
        
}
