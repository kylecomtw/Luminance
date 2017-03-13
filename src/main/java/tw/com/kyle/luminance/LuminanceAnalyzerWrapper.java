/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import tw.com.kyle.luminance.AnnotAnalyzerFactory.AnnotAnalyzerEnum;
/**
 *
 * @author Sean_S325
 */
public class LuminanceAnalyzerWrapper{
    public static PerFieldAnalyzerWrapper Get() {                
        Map<String, Analyzer> amap = new HashMap<>();
        amap.put("content", new StandardAnalyzer());
        amap.put("anno", AnnotAnalyzerFactory.Get(AnnotAnalyzerEnum.RangeAnnotAnalyzer));
        amap.put("base_ref", new StandardAnalyzer());
        PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer());

        return wrapper;
    }
}
