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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
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

        JsonObject ret = new JsonObject();
        AnnotationProvider annot_prov = new AnnotationProvider(text);
        indexer.open();
        for(LumDocument lum_doc: annot_prov.IndexableDocuments()){            
            indexer.index_doc(lum_doc);
        }        
        
        ret.addProperty("uuid", annot_prov.GetRefUuid());
        return ret;
    }
    
    public JsonObject get_annotation_template(long uuid) throws IOException {
        LumReader reader = new LumReader(index_dir);        
        StringBuilder sb = new StringBuilder();
        sb.append("# uuid: ");
        sb.append(uuid); sb.append("\n");        
        sb.append(reader.GetPlainText(uuid));
        
        JsonObject jobj = new JsonObject();
        jobj.addProperty("anno.templ", sb.toString());
        
        return jobj;
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

    public JsonArray get_annotations(long uuid) throws IOException {
        JsonArray jarr = new JsonArray();
        try (LumReader reader = new LumReader(index_dir);) {
            List<Long> uuids = reader.getAnnotations(uuid);
            uuids.stream().forEach((x)->jarr.add(x));
        }
        
        return jarr;
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
