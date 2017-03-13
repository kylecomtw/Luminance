/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;

/**
 *
 * @author Sean_S325
 */
public class OffsetTokenFilter extends TokenFilter{
    
    protected OffsetTokenFilter(TokenStream ts){
        super(ts);        
    }
    
    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()){
            return false;
        }
        
        CharTermAttribute charAttr = addAttribute(CharTermAttribute.class);        
        String str = new String(charAttr.buffer());
        String[] parts = str.trim().split(",");
        if (parts.length == 3){
            int s_offset = Integer.parseInt(parts[0]);
            int e_offset = Integer.parseInt(parts[1]);
            String tag = parts[2];
            
            // charAttr.setEmpty();
            charAttr.append(tag);
            // charAttr.resizeBuffer(tag.length());
            
            OffsetAttribute offAttr = addAttribute(OffsetAttribute.class);
            PositionIncrementAttribute posAttr = addAttribute(PositionIncrementAttribute.class);
            PositionLengthAttribute posLenAttr = addAttribute(PositionLengthAttribute.class);
            posAttr.setPositionIncrement(1);
            posLenAttr.setPositionLength(1);
            
            offAttr.setOffset(s_offset, e_offset);            
        }
                
        return true;
    }
    
}
