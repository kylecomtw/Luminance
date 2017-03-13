/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sean_S325
 */
public class LumDocumentAdapter {
    public static LumDocument FromString(BufferedReader reader){        
        LumDocument doc = new LumDocument();
        try {
            String line = reader.readLine();
            StringBuilder sb = new StringBuilder();
            while(line != null){
                if (line.startsWith("#")){
                    String[] toks = line.split("=");
                    if (toks.length <= 2) continue;
                    switch(toks[0].trim().toLowerCase()){
                        case "doctype":
                            doc.SetDocType(toks[1].trim());
                            break;
                        case "docclass":
                            doc.SetDocClass(toks[1].trim());
                            break;
                        case "baseref":
                            doc.SetBaseRef(toks[1].trim());
                            break;
                        default:
                            break;
                    }
                    
                } else {
                    sb.append(line.trim());
                }
                line = reader.readLine();                
            }
            doc.SetContent(sb.toString());
            return doc;
        } catch (IOException ex) {
            Logger.getLogger(LumDocumentAdapter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static LumDocument FromJson(String json_str){
        return new LumDocument();
    }
}
