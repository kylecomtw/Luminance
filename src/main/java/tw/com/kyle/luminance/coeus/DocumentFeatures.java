/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.coeus;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sean_S325
 */
public class DocumentFeatures {
    protected String get_category() {
        return "Generic";
    }
    
    public List<String[]> get_meta_info(){
        List<String[]> meta_info = new ArrayList<>();
        
        Method[] methods = this.getClass().getMethods();
        for(Method method: methods){
            FeatureAnnot annot = method.getAnnotation(FeatureAnnot.class);
            if (annot == null) continue;
            
            String name = annot.value();
            String ret_type = method.getReturnType().getName();
            meta_info.add(new String[]{name, ret_type});
        }
        return meta_info;
    }
}
