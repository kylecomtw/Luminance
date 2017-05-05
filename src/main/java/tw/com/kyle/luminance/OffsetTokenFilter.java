/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 *
 * @author Sean_S325
 */
public class OffsetTokenFilter extends TokenFilter{
    
    protected OffsetTokenFilter(TokenStream ts){
        super(ts);        
    }
    
    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()){
            return false;
        } else {                
            return true;
        }
    }    
}
