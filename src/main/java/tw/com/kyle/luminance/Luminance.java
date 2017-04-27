/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import org.apache.lucene.util.BytesRef;

/**
 * Main interface of project Luminance
 *
 * @author Sean
 */
public class Luminance {

    private String index_dir;
    public Luminance(String idir) {
        index_dir = idir;
    }

    public JsonElement add_document(String text) {
        return null;
    }

    public JsonObject add_plain_text(String text) throws IOException {
        String INDEX_DIR = "h:/index_test";        
        LumIndexer indexer = new LumIndexer(index_dir);
        org.apache.lucene.document.Document base_doc = indexer.CreateIndexDocument(LumIndexer.DOC_DISCOURSE);                
        indexer.AddField(base_doc, "title", "", FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(base_doc, "timestamp", LumUtils.get_lucene_timestamp(), FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(base_doc, "timestamp", new BytesRef(LumUtils.get_lucene_timestamp()), FieldTypeFactory.Get(FieldTypeFactory.FTEnum.TimestampIndex));
        indexer.AddField(base_doc, "content", text, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.FullIndex));

        BytesRef base_doc_ref = indexer.GetUUIDAsBytesRef(base_doc);
        indexer.AddToIndex(base_doc);
        indexer.flush();
        indexer.close();
        
        JsonObject jObj = new JsonObject();
        jObj.addProperty("uuid", Long.toHexString(LumUtils.BytesRefToLong(base_doc_ref)));
        return jObj;
    }

    public JsonObject add_annotation(String text, String base_uuid) throws IOException {
        AnnotationProvider annot = new AnnotationProvider(text);
        LumIndexer indexer = new LumIndexer(index_dir);        
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

    public JsonElement find_text() {
        return null;
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
        indexer.AddField(idx_doc, "annot", annot_content, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.NonStoredFullIndex));
        indexer.AddField(idx_doc, "base_ref", base_doc_ref, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(idx_doc, "annot_type", annot_type, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(idx_doc, "annot_mode", "manual", FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(idx_doc, "annot_range", "spanned", FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
    }
}
