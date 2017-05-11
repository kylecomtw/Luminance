/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represent an indexed unit in Luminance
 * @author Sean_S325
 */
public class LumDocument {
    public static final String DISCOURSE = "discourse";
    public static final String ANNO = "anno";
    public static final String FRAG = "frag";    
    public static final String GENERIC = "generic";
    
    private final List<String> ANNO_TYPES = Arrays.asList(new String[] {"seg", "pos", "tag"});        
    private String doc_class = GENERIC;
    private String doc_mode = GENERIC;
    private String timestamp = DateUtils.now();    
    private String content = "";
    private String anno_type = "";
    private String anno_name = "";
    private long   base_ref = -1;
    private long   uuid = -1;
    private Map<String, String> supp_data = new HashMap<>();
    
    public LumDocument() {
        uuid = System.nanoTime();
    }
    
    //! Getters and Setters
    public void SetDocClass(String val) {doc_class = val;}
    public void SetDocMode(String val) { doc_mode = val; }
    public void SetBaseRef(long val){base_ref = val;}
    public void SetContent(String val){content = val;}
    public void SetTimestamp(String val) {timestamp = val;}
    public void SetAnnoName(String val) {anno_name = val;}
    public void SetAnnoType(String val) { 
        int ret = ANNO_TYPES.indexOf(val.toLowerCase());
        if (ret >= 0) anno_type = val.toLowerCase();
        else anno_type = "tag'";
    }    
    public void AddSuppData(String key, String val){
        supp_data.put(key, val);
    }
            
    public String GetDocClass() {return doc_class;}
    public String GetDocMode() {return doc_mode; }
    public long GetBaseRef() {return base_ref;}
    public String GetContent(){return content;}
    public String GetTimestamp() {return timestamp;}
    public String GetAnnoType() {return anno_type;}
    public String GetAnnoName() {return anno_name;}
    public long GetUuid() {return uuid;}
    public Set<String> GetSuppDataKey() {return supp_data.keySet();}
    public String GetSuppData(String key) {return supp_data.getOrDefault(key, "");}
    
    
}
