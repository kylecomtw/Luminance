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
        if (TextUtils.is_segmented(text)) {
            ret = add_plain_text(TextUtils.extract_raw_text(text));
            String base_uuid = ret.get("uuid").getAsString();
            JsonObject annot_ret = add_annotation(text, base_uuid);
            for (Entry<String, JsonElement> ent : annot_ret.entrySet()) {
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

    public JsonObject add_tag_annotation(String text, String anno_type, String base_uuid) throws IOException {
        indexer.open();
        AnnotationProvider annot = new AnnotationProvider(text);
        BytesRef base_doc_ref = LumUtils.LongToBytesRef(Long.parseLong(base_uuid, 16));
        JsonObject jobj = new JsonObject();
        if (annot.has_pos_tagged()) {
            LumDocument pos_doc = annot.create_annot_doc_pos(base_doc_ref);
            org.apache.lucene.document.Document pos_adoc = indexer.CreateIndexDocument(LumIndexer.DOC_ANNOTATION);
            setup_index_annot_document(indexer, pos_adoc, anno_type, pos_doc.GetContent(), base_doc_ref);
            indexer.AddToIndex(pos_adoc);

            BytesRef seg_doc_ref = indexer.GetUUIDAsBytesRef(pos_adoc);
            jobj.addProperty(anno_type, Long.toHexString(LumUtils.BytesRefToLong(seg_doc_ref)));
        }
        
        return jobj;
    }

    public JsonElement get_annotation_template() {
        return null;
    }

    public JsonArray list_documents() throws IOException {
        LumQuery query = new LumQuery(index_dir);
        List<Long> uuids = query.ListDocuments(10);
        JsonArray jarr = new JsonArray();
        uuids.stream().forEach((x) -> jarr.add(x));
        return jarr;
    }

    public JsonElement get_document() {
        return null;
    }

    public JsonElement get_annotation() {
        return null;
    }

    public JsonArray findWord(String text) throws IOException {
        try (LumReader reader = new LumReader(index_dir);) {
            Concordance concord = new Concordance(reader);
            return new KwicResult.KwicJsonList(concord.findWord(text)).toJson();
        }
    }

    public JsonArray findGrams(String text) throws IOException {
        try (LumReader reader = new LumReader(index_dir);) {
            Concordance concord = new Concordance(reader);
            return new KwicResult.KwicJsonList(concord.findGrams(text)).toJson();
        }
    }

    public JsonArray findPos(String tag) throws IOException {
        try (LumReader reader = new LumReader(index_dir);) {
            Concordance concord = new Concordance(reader);
            return new KwicResult.KwicJsonList(concord.findPos(tag)).toJson();
        }
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
