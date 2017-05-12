/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.util.BytesRef;
import tw.com.kyle.sketcher.Sketcher;

/**
 * Main interface of project Luminance
 *
 * @author Sean
 */
public class Luminance {

    private final String index_dir;
    private LumIndexer indexer = null;
    private final int CONCORD_WIN_SIZE = 20;

    public Luminance(String idir) throws IOException {
        index_dir = idir;
    }

    public void close() throws IOException {
        indexer.flush();       
        indexer.close();
    }

    public static void clean_index(String index_dir) throws IOException {
        LumIndexer.CleanIndex(index_dir);
    }

    public static String J2S(JsonElement jelem) {
        Gson gson = new Gson();
        String ret = gson.toJson(jelem);
        return ret;
    }

    public void begin_write() throws IOException {
        if (indexer == null) {
            indexer = new LumIndexer(index_dir);
        }

        indexer.open();
    }
    
    public void end_write() throws IOException {
        close();
    }

    public JsonElement add_document(String text) throws IOException {

        JsonObject ret = new JsonObject();
        AnnotationProvider annot_prov = new AnnotationProvider(text);
        for (LumDocument lum_doc : annot_prov.IndexableDocuments()) {
            indexer.index_doc(lum_doc);
        }

        ret.addProperty("uuid", annot_prov.GetRefUuid());
        return ret;
    }

    public JsonElement add_single_document(String text) throws IOException {
        begin_write();
        JsonElement ret = add_document(text);        
        end_write();
        return ret;
    }

    public JsonObject get_annotation_template(long uuid) throws IOException {
        LumReader reader = new LumReader(index_dir);
        StringBuilder sb = new StringBuilder();
        sb.append("# base_ref: ");
        sb.append(uuid);
        sb.append("\n");
        sb.append("# anno_type: seg\n");
        sb.append("# anno_name: user_defined\n");
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
            for(long uuid_x: uuids){
                JsonObject jobj = new JsonObject();
                Document adoc = reader.GetDocument(uuid_x);
                jobj.addProperty("uuid", uuid_x);
                jobj.addProperty("anno_type", adoc.get("anno_type"));
                jobj.addProperty("anno_name", adoc.get("anno_name"));               
                jarr.add(jobj);
            }                    
        }

        return jarr;
    }

    public JsonArray findWord(String text) throws IOException {
        try (LumReader reader = new LumReader(index_dir);) {
            Concordance concord = new Concordance(reader, CONCORD_WIN_SIZE);
            return new KwicResult.KwicJsonList(concord.findWord(text)).toJson();
        }
    }

    public JsonArray findGrams(String text) throws IOException {
        try (LumReader reader = new LumReader(index_dir);) {
            Concordance concord = new Concordance(reader, CONCORD_WIN_SIZE);
            return new KwicResult.KwicJsonList(concord.findGrams(text)).toJson();
        }
    }

    public JsonArray findPos(String tag) throws IOException {
        try (LumReader reader = new LumReader(index_dir);) {
            Concordance concord = new Concordance(reader, CONCORD_WIN_SIZE);
            return new KwicResult.KwicJsonList(concord.findPos(tag)).toJson();
        }
    }

    public JsonArray match_text(String inputs, String rules) {
        JsonArray jarr = new JsonArray();
        Sketcher sketcher = new Sketcher();
        JsonElement jelem = sketcher.match_json(inputs, rules);
        jarr.add(jelem);

        return jarr;
    }

    public JsonArray sketch_text(String key, boolean as_word) throws IOException {
        JsonArray jarr = new JsonArray();
        try (LumReader reader = new LumReader(index_dir);) {
            Concordance concord = new Concordance(reader, CONCORD_WIN_SIZE);
            List<KwicResult> kwic_list = null;
            String rules = "";
            URL resUrl = null;
            try {
                if (as_word) {
                    resUrl = getClass().getResource("/sketch_word_rules.txt");
                    kwic_list = concord.findWord(key);
                } else {
                    resUrl = getClass().getResource("/sketch_gram_rules.txt");
                    kwic_list = concord.findGrams(key);
                }
                rules = String.join("\n", Files.readAllLines(Paths.get(resUrl.toURI())));
            } catch (URISyntaxException ex) {
                Logger.getLogger(Luminance.class.getName()).severe(ex.toString());
            }

            for (KwicResult kr : kwic_list) {
                String input = kr.toStringRepr(false);
                Sketcher sketcher = new Sketcher();
                JsonElement jelem = sketcher.match_json(input, rules);
                jarr.add(jelem);
            }
        }

        return jarr;
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
