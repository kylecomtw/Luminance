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
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

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

    public static LumPositionMap Get(String raw_text) throws IOException {
        StandardAnalyzer analyzer = new StandardAnalyzer();
        TokenStream tstream = analyzer.tokenStream("", raw_text);        
                
        CharTermAttribute termAttr = tstream.getAttribute(CharTermAttribute.class);
        OffsetAttribute offAttr = tstream.getAttribute(OffsetAttribute.class);        
        // PositionIncrementAttribute posIncAttr = tstream.getAttribute(PositionIncrementAttribute.class);        
        // PositionLengthAttribute posLenAttr = tstream.getAttribute(PositionLengthAttribute.class);

        List<String> tokens = new ArrayList<>();
        List<Integer> pos_list = new ArrayList<>();

        int pos_counter = 0;
        tstream.reset();
        while (tstream.incrementToken()) {
            tokens.add(termAttr.toString());
            pos_list.add(offAttr.startOffset());            
        }

        return new LumPositionMap(tokens, pos_list);
    }

    public int FindPosition(String term, int start) {
        for(int i = 0; i < pos_info.tokens.size(); ++i){
            int pos_x = pos_info.pos_list.get(i);
            if (pos_x >= start && pos_info.tokens.get(i).equals(term)) {
                return pos_x;
            }             
        }
        
        return -1;
    }

}
