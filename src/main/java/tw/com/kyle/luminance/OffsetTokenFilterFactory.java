/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

/**
 *
 * @author Sean_S325
 */
public class OffsetTokenFilterFactory extends TokenFilterFactory {
    protected OffsetTokenFilterFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public TokenStream create(TokenStream stream) {
        return new OffsetTokenFilter(stream);
    }
}
