/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

/**
 * Represent an indexed unit in Luminance
 * @author Sean_S325
 */
public class LumDocument {
    public static final String DISCOURSE = "discourse";
    public static final String ANNO = "anno";
    public static final String FRAG = "frag";    
    public static final String GENERIC = "generic";
            
    private String doc_type = DISCOURSE;
    private String doc_class = GENERIC;
    private String timestamp = DateUtils.now();
    private String base_ref = "";
    private String content = "";
    
    //! Getters and Setters
    public void SetDocType(String val) {doc_type = val;}
    public void SetDocClass(String val) {doc_class = val;}
    public void SetBaseRef(String val){base_ref = val;}
    public void SetContent(String val){content = val;}
    public void SetTimestamp(String val) {timestamp = val;}
    public String GetDocType() {return doc_type;}
    public String GetDocClass() {return doc_class;}
    public String GetBaseRef() {return base_ref;}
    public String GetContent(){return content;}
    public String GetTimestamp() {return timestamp;}
    
    public LumDocument(){
        
    }
    
    
}
