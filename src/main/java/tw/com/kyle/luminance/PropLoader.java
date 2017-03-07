/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sean
 */
public class PropLoader {
    public static Map<String, String> Load(){
        Properties props = new Properties();
        try {
            FileInputStream in = new FileInputStream("luminance.properties");
            props.load(in);
            in.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PropLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PropLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        Map<String, String> map = default_props();
        for(Object ent: props.keySet()){
            map.put((String)ent, props.getProperty((String)ent));
        }
        
        return map;
    }
    
    private static Map<String, String> default_props(){
        Map<String, String> map = new HashMap<>();
        map.put("indir", "text_data/");
        map.put("in_filter", "*.txt");
        
        return map;
    }
    
}
