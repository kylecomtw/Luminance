/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Ignore;
import org.junit.Test;
import tw.com.kyle.luminance.Luminance;
import tw.com.kyle.luminance.coeus.DocData;
import tw.com.kyle.luminance.coeus.FeatureAnalyzer;

/**
 *
 * @author Sean_S325
 */
public class DocumentFeatureTest {

    @Test
    public void testFeatureTemplate() throws IOException {
        DocData doc_data = new DocData();
        FeatureAnalyzer feat_analy = new FeatureAnalyzer(doc_data);
        JsonObject jobj = feat_analy.GetFeatureTemplate();

        String FEAT_PATH = "h:/feature_template.json";
        JsonWriter writer = new JsonWriter(new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(FEAT_PATH), StandardCharsets.UTF_8)));
        writer.setIndent("  ");
        Gson gson = new Gson();
        gson.toJson(jobj, writer);
        writer.close();
    }

    @Ignore
    @Test
    public void testFeatures() throws IOException {
        String INDEX_DIR = "h:/index_dir";
        Luminance.clean_index(INDEX_DIR);
        Luminance lum = new Luminance(INDEX_DIR);
        String txt = String.join("",
                Files.readAllLines(Paths.get("etc/test/simple_text.txt"), StandardCharsets.UTF_8));

    }
}
