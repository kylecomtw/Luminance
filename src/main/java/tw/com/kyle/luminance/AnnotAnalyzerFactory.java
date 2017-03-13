/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.UnsupportedOperationException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.pattern.PatternTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;


/**
 *
 * @author Sean
 */

public class AnnotAnalyzerFactory {
    public enum AnnotAnalyzerEnum {        
        RangeAnnotAnalyzer, TokenAnnotAnalyzer
    }
    
    public static Analyzer Get(AnnotAnalyzerEnum an_enum){
        Analyzer ana = null;
        try {
            
            CustomAnalyzer.Builder builder = CustomAnalyzer.builder();
            switch(an_enum){
                case RangeAnnotAnalyzer:
                    Map<String, String> params = new HashMap<>();
                    Map<String, String> fparams = new HashMap<>();
                    params.put("pattern", "(\\d+,\\d+,[^)]*)");                    
                    
                    ana = builder.withTokenizer(PatternTokenizerFactory.class, params)
                            .addTokenFilter(OffsetTokenFilterFactory.class, fparams)
                            .build();
                    break;                
                case TokenAnnotAnalyzer:
                    //! TokenAnnotAnalyzer at best is a convenient interface of
                    //! RangeAnnotAnalyzer. That is, Token is just a lenth-1-range
                    //! There is no need to specifically write another anlayzer for that.
                    throw new UnsupportedOperationException("Not implemented");                    
                default:
                    ana = new StandardAnalyzer();
            }
            
            return ana;
        } catch (IOException ex) {
            Logger.getLogger(AnnotAnalyzerFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (ana == null){
            ana = new StandardAnalyzer();
        }
        
        return ana;
    }
}
