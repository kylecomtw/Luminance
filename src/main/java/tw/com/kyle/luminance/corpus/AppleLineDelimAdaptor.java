/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.corpus;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sean
 */
public class AppleLineDelimAdaptor {
    private Logger logger = Logger.getLogger(AppleLineDelimAdaptor.class.getName());
    private Pattern datePat = Pattern.compile("\\d{8}");
    public class AppleDocument {
        public String title = "";
        public String pdate = "";
        public String content = "";        
    }
    
    public AppleLineDelimAdaptor(){
        
    }
    
    public List<AppleDocument> Parse(String inpath) {
        if (!Files.exists(Paths.get(inpath))){
            return new ArrayList<>();
        }
        if (!inpath.endsWith(".txt")) {
            return new ArrayList<>();
        }
        
        List<String> lines = null;
        try{
            lines = Files.readAllLines(Paths.get(inpath));        
        } catch (IOException ex){
            logger.severe("invalid input in " + inpath);
        }
        int state = 0;
        AppleDocument adoc = new AppleDocument();
        StringBuilder content_builder = new StringBuilder();
        List<AppleDocument> adoc_list = new ArrayList<>();
        
        for(String ln: lines){
            Boolean empty_line = ln.trim().length() == 0;
            if (state == 0 && !empty_line){
                //! title state
                Matcher m = datePat.matcher(ln);
                if (m.find()){
                    adoc.pdate = m.group();
                    adoc.title = ln.substring(0, m.start()).trim();
                } else {
                    adoc.pdate = "20000101";
                    adoc.title = ln.trim();
                    logger.warning("Empty date string");
                }
                state = 1;                
            } else if (state == 1 && !empty_line) {            
                //! content state                
                content_builder.append(ln.trim());
                content_builder.append("\n");
            } if (empty_line) {                
                state = 0;
            }
            
            if (state == 0 && content_builder.length() > 0){
                adoc.content = content_builder.toString();
                content_builder.setLength(0);
                adoc_list.add(adoc);
                adoc = new AppleDocument();                
            }            
        }
        
        if (content_builder.length() > 0){
            adoc.content = content_builder.toString();
            adoc_list.add(adoc);            
        }  
        
        logger.info(String.format("Processing %d Documents in %s", adoc_list.size(), inpath));
        return adoc_list;
    }        
}
    

