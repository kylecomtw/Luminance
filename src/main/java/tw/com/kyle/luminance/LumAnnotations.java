/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.apache.lucene.document.Document;

/**
 *
 * @author Sean_S325
 */
public class LumAnnotations {
    private class AnnotRecord {  
        public long annot_uuid = -1;
        public String annot_type = "";
        public String annot_range = "";
        public String annot_mode = "";        
        public Document annot_doc;
    }
    private List<AnnotRecord> annot_list = new ArrayList<>();
    private long base_ref = 0;
    private HashMap<Long, List<LumRange>> range_cache = new HashMap<>();
    public boolean hasSegmentation() {
        return annot_list.stream().anyMatch((x)->x.annot_type.equals("seg"));
    }
    
    public boolean hasPOSTagged() { 
        return annot_list.stream().anyMatch((x)->x.annot_type.equals("pos"));
    }
    
    public boolean hasAnnotType(String atype) { 
        return annot_list.stream().anyMatch((x)->x.annot_type.equals(atype));
    }
        
    public long getLatestUuid(String atype){
        Optional<Long> opt_uuid = annot_list.stream()
                .filter((x)->x.annot_type.equals(atype))
                .map((x)->x.annot_uuid)
                .reduce((a,b)->b);
        if(opt_uuid.isPresent()){
            return opt_uuid.get();
        } else {
            return Long.MIN_VALUE;
        }
    }
    
    public int size() { return annot_list.size(); }
    
    public LumAnnotations(long ref_uuid) { base_ref = ref_uuid; }
    public void AddAnnotation(long annot_uuid, Document annot_doc){
        if (!annot_doc.get("class").equals(LumDocument.ANNO)) {
            return;
        }
        AnnotRecord a_rec = new AnnotRecord();
        String anno_name = annot_doc.get("anno_name");
        String anno_type = annot_doc.get("anno_type");
        if (anno_type != null) {
            a_rec.annot_type = anno_type;
        }
        a_rec.annot_uuid = annot_uuid;
        
        
        // a_rec.annot_range = annot_doc.get("anno_range");
        // a_rec.annot_mode = annot_doc.get("anno_mode");
        a_rec.annot_doc = annot_doc;
        annot_list.add(a_rec);
    }
    
    public Document GetAnnotDocument(long annot_uuid){
        Optional<Document> opt_doc = annot_list.stream()                
                .filter((x)->x.annot_uuid == annot_uuid)
                .map((x)->x.annot_doc)
                .findFirst();
        if (opt_doc.isPresent()){
            return opt_doc.get();            
        } else {
            return null;
        }
    }
    
    public List<LumRange> GetLumRange(long annot_uuid, LumWindow lumWin) throws IOException{
        if(range_cache.containsKey(annot_uuid)) return range_cache.get(annot_uuid);
                
        range_cache.put(annot_uuid, lumWin.BuildLumRange(annot_uuid));
        return range_cache.get(annot_uuid);
    }
       
}
