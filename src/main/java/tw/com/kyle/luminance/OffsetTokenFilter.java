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
        } else {                
            return true;
        }
    }    
}
