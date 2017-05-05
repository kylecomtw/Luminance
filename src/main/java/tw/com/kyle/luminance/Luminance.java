/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.lucene.util.BytesRef;

/**
 * Main interface of project Luminance
 *
 * @author Sean
 */
public class Luminance {

    private final String index_dir;
    private LumIndexer indexer = null;
    public Luminance(String idir) throws IOException {
        index_dir = idir;
        indexer = new LumIndexer(index_dir);
    }
    
    public void close() throws IOException {
        indexer.flush();
        indexer.close();
    }
    
    public static void clean_index(String index_dir) throws IOException {
        LumIndexer.CleanIndex(index_dir);        
    }
    
    public JsonElement add_document(String text) throws IOException {
        
        JsonObject ret = null;
        if(TextUtils.is_segmented(text)){
            ret = add_plain_text(TextUtils.extract_raw_text(text));
            String base_uuid = ret.get("uuid").getAsString();
            JsonObject annot_ret = add_annotation(text, base_uuid);
            for(Entry<String, JsonElement> ent: annot_ret.entrySet()){
                ret.add(ent.getKey(), ent.getValue());
            }
        } else {
            ret = add_plain_text(text);
        }
        
        return ret;
    }

    public JsonObject add_plain_text(String text) throws IOException { 
        indexer.open();
        org.apache.lucene.document.Document base_doc = indexer.CreateIndexDocument(LumIndexer.DOC_DISCOURSE);                
        indexer.AddField(base_doc, "title", "", FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(base_doc, "timestamp", LumUtils.get_lucene_timestamp(), FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(base_doc, "timestamp", new BytesRef(LumUtils.get_lucene_timestamp()), FieldTypeFactory.Get(FieldTypeFactory.FTEnum.TimestampIndex));
        indexer.AddField(base_doc, "content", text, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.FullIndex));

        BytesRef base_doc_ref = indexer.GetUUIDAsBytesRef(base_doc);
        indexer.AddToIndex(base_doc);
        
        JsonObject jObj = new JsonObject();
        jObj.addProperty("uuid", Long.toHexString(LumUtils.BytesRefToLong(base_doc_ref)));
        return jObj;
    }

    public JsonObject add_annotation(String text, String base_uuid) throws IOException {
        indexer.open();
        AnnotationProvider annot = new AnnotationProvider(text);               
        BytesRef base_doc_ref = LumUtils.LongToBytesRef(Long.parseLong(base_uuid, 16));
        JsonObject jobj = new JsonObject();
        if (annot.has_segmented()) {
            LumDocument seg_doc = annot.create_annot_doc_seg(base_doc_ref);
            org.apache.lucene.document.Document seg_adoc = indexer.CreateIndexDocument(LumIndexer.DOC_ANNOTATION);
            setup_index_annot_document(indexer, seg_adoc, "seg", seg_doc.GetContent(), base_doc_ref);
            indexer.AddToIndex(seg_adoc);
            
            BytesRef seg_doc_ref = indexer.GetUUIDAsBytesRef(seg_adoc);
            jobj.addProperty("seg", Long.toHexString(LumUtils.BytesRefToLong(seg_doc_ref)));
        }

        if (annot.has_pos_tagged()) {
            LumDocument pos_doc = annot.create_annot_doc_pos(base_doc_ref);
            org.apache.lucene.document.Document pos_adoc = indexer.CreateIndexDocument(LumIndexer.DOC_ANNOTATION);
            setup_index_annot_document(indexer, pos_adoc, "pos", pos_doc.GetContent(), base_doc_ref);
            indexer.AddToIndex(pos_adoc);
            
            BytesRef seg_doc_ref = indexer.GetUUIDAsBytesRef(pos_adoc);
            jobj.addProperty("pos", Long.toHexString(LumUtils.BytesRefToLong(seg_doc_ref)));
        }
        
        return jobj;
    }

    public JsonElement get_annotation_template() {
        return null;
    }

    public JsonElement list_documents() {
        return null;
    }

    public JsonElement get_document() {
        return null;
    }

    public JsonElement get_annotation() {
        return null;
    }

    public JsonArray find_text(String text) throws IOException {                
        Concordance concord = new Concordance(indexer.GetReaderOnly());
        JsonObject jobj = new JsonObject();
        List<ConcordanceResult> cr_list = concord.query(text, "annot", false);
        Function<Token, JsonArray> map_token = (Token x)->{
            JsonObject x_obj = new JsonObject();
            x_obj.addProperty("text", x.word);
            x_obj.addProperty("pos", x.pos);
            if (x.data != null) x_obj.addProperty("data", String.join(", ", x.data));
            if (x.ner != null) x_obj.addProperty("ner", x.ner);
            if (x.dep != null) x_obj.addProperty("dep", x.dep);
            
            JsonArray x_arr = new JsonArray();
            x_arr.add(x_obj);
            return x_arr;
        };                
        
        BinaryOperator<JsonArray> combiner = (a, b) -> {
            a.addAll(b); return a;
        };
        
        Function<List<Token>, JsonArray> jarr_pipeline = (List<Token> cr_x) -> 
                        cr_x.stream()
                            .map((x)->map_token.apply(x))
                            .collect(Collectors.reducing(
                                     new JsonArray(), combiner));
        
        JsonArray cr_jarr = new JsonArray();
        for(ConcordanceResult cr: cr_list){
            JsonArray prec_arr = jarr_pipeline.apply(cr.prec_context);
            JsonArray succ_arr = jarr_pipeline.apply(cr.succ_context);
            JsonObject cr_obj = (JsonObject)map_token.apply(cr.target).get(0);
            cr_obj.add("prec", prec_arr);
            cr_obj.add("succ", succ_arr);
            cr_jarr.add(cr_obj);
        }
        
        return cr_jarr;
    }

    public JsonElement find_tag() {
        return null;
    }

    public JsonElement sketch_text() {
        return null;
    }

    public JsonElement find_MWE() {
        return null;
    }

    public JsonElement build_graph() {
        return null;
    }
        
    private void setup_index_annot_document(LumIndexer indexer,
            org.apache.lucene.document.Document idx_doc,
            String annot_type, String annot_content, BytesRef base_doc_ref) {
        indexer.AddField(idx_doc, "anno", annot_content, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.NonStoredFullIndex));
        indexer.AddField(idx_doc, "base_ref", base_doc_ref, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(idx_doc, "anno_type", annot_type, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(idx_doc, "anno_mode", "manual", FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(idx_doc, "anno_range", "spanned", FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
    }
}
