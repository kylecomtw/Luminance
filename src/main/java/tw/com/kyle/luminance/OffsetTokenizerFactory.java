/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.Map;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

/**
 *
 * @author Sean
 */
public class OffsetTokenizerFactory extends TokenizerFactory {
    public OffsetTokenizerFactory(Map<String, String> args){
        super(args);
    }

    @Override
    public Tokenizer create(AttributeFactory af) {        
        return new OffsetTokenizer();
    }
}
