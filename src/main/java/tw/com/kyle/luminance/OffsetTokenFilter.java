/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

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
        
        CharTermAttribute charAttr = getAttribute(CharTermAttribute.class);        
        int length = charAttr.length();
        String str = new String(charAttr.buffer());
        String[] parts = str.split(",");
        if (parts.length == 3){
            int s_offset = Integer.parseInt(parts[0]);
            int e_offset = Integer.parseInt(parts[1]);
            String tag = parts[2];
            
            charAttr.setEmpty();
            charAttr.copyBuffer(tag.toCharArray(), 0, tag.length());
            OffsetAttribute offAttr = addAttribute(OffsetAttribute.class);
            offAttr.setOffset(s_offset, e_offset);            
        }
                
        return true;
    }
    
}
