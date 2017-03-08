/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.pattern.PatternTokenizerFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilterFactory;

/**
 *
 * @author Sean
 */

public class AnnotAnalyzerFactory {
    public enum AnnotAnalyzerEnum {
        SegAnalyzer, SegPosAnalyzer, SegPosNerAnalyzer, 
        RangeAnnotAnalyzer, TokenAnnotAnalyzer
    }
    
    public static Analyzer Get(AnnotAnalyzerEnum an_enum) throws IOException{
        Analyzer ana = null;
        CustomAnalyzer.Builder builder = CustomAnalyzer.builder();
        switch(an_enum){
            case SegAnalyzer:
                Map<String, String> params = new HashMap<>();
                Map<String, String> fparams = new HashMap<>();
                ana = builder.withTokenizer(PatternTokenizerFactory.class, params)
                        .addTokenFilter(StandardFilterFactory.class, fparams)
                        .build();                                                
                break;
            case SegPosAnalyzer:
                break;
            case SegPosNerAnalyzer:
                break;
            case RangeAnnotAnalyzer:
                break;
            case TokenAnnotAnalyzer:
                break;
            default:
                ana = new StandardAnalyzer();
        }
        
        return ana;
    }
}
