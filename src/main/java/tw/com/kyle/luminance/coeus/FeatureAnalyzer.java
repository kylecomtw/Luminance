package tw.com.kyle.luminance.coeus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sean_S325
 */
public class FeatureAnalyzer {
    private DocData doc_data = null;
    private List<DocumentFeatures> doc_feat_list = new ArrayList<>();
    SurfaceFeatures surf = null;
    StructureFeatures struct = null; 
    CohesiveFeatures coh = null;
    TopicFeatures topics = null;
    public FeatureAnalyzer(DocData ddata){
        surf = new SurfaceFeatures(doc_data);
        struct = new StructureFeatures(doc_data);
        coh = new CohesiveFeatures(doc_data);
        topics = new TopicFeatures(doc_data);
        doc_feat_list = Arrays.asList(surf, struct, coh, topics);
        doc_data = ddata;
    }
    
    public JsonObject GetFeatureTemplate(){
        JsonObject jobj = new JsonObject();
        for(DocumentFeatures doc_feat: doc_feat_list){
            JsonArray jarr = new JsonArray();
            for(String[] feat_info: doc_feat.get_meta_info()){
                JsonObject feat_obj = new JsonObject();
                feat_obj.addProperty("name", feat_info[0]);
                feat_obj.addProperty("type", feat_info[1].substring(feat_info[1].lastIndexOf(".")+1));
                jarr.add(feat_obj);
            }
            
            jobj.add(doc_feat.get_category(), jarr);
        }
        
        return jobj;
    }
    
    public JsonObject GetSurfaceFeatures() {
        JsonObject jobj = new JsonObject();
        
        Quantiles cf_qarr = surf.get_charac_freq();
        Quantiles wf_qarr = surf.get_word_freq();
        Quantiles cl_qarr = surf.get_clause_length();
        Quantiles sl_qarr = surf.get_sentence_length();
        jobj.addProperty("CFreq_Q25", cf_qarr.Q25());
        jobj.addProperty("CFreq_Q50", cf_qarr.Q50());
        jobj.addProperty("CFreq_Q75", cf_qarr.Q75());
        jobj.addProperty("WFreq_Q25", wf_qarr.Q25());
        jobj.addProperty("WFreq_Q50", wf_qarr.Q50());
        jobj.addProperty("WFreq_Q75", wf_qarr.Q75());
        jobj.addProperty("CLen_Q25", cl_qarr.Q25());
        jobj.addProperty("CLen_Q50", cl_qarr.Q50());
        jobj.addProperty("CLen_Q75", cl_qarr.Q75());
        jobj.addProperty("SLen_Q25", sl_qarr.Q25());
        jobj.addProperty("SLen_Q50", sl_qarr.Q50());
        jobj.addProperty("SLen_Q75", sl_qarr.Q75());
        jobj.addProperty("Char_N", surf.get_character_count());
        jobj.addProperty("Word_N", surf.get_word_count());
        return jobj;
    }
    
    public JsonObject GetStructuralFeatures() {
        JsonObject jobj = new JsonObject();
               
        
        return jobj;
    }
    
    public JsonObject GetCohesiveFeatures() {
        JsonObject jobj = new JsonObject();
        
        return jobj;
    }
    
    public JsonObject GetThematicFeatures() {
        JsonObject jobj = new JsonObject();        
        
        return jobj;
    }
    
}
