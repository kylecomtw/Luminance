/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.junit.Test;
import tw.com.kyle.luminance.Concordance;
import tw.com.kyle.luminance.KwicResult;
import tw.com.kyle.luminance.LumReader;
import tw.com.kyle.luminance.Luminance;
import tw.com.kyle.sketcher.Sketcher;

/**
 *
 * @author Sean_S325
 */
public class SketcherTest {

    private String INDEX_DIR = "h:/index_dir";
    private String rules = "";
    private void setup() {
        try {
            Luminance.clean_index(INDEX_DIR);
            Luminance lum = new Luminance(INDEX_DIR);
            String txt = String.join("\n",
                    Files.readAllLines(Paths.get("etc/test/simple_text.txt"), StandardCharsets.UTF_8));
            rules = String.join("\n",
                    Files.readAllLines(Paths.get("etc/test/sketcher_rules.txt"), StandardCharsets.UTF_8));
            JsonObject elem = (JsonObject) lum.add_single_document(txt);
            lum.close();
        } catch (IOException ex) {
            System.out.println(ex);
            fail(ex.toString());
        }
    }

    @Test
    public void testConcordance_Word() throws IOException {
        setup();
        try (LumReader reader = new LumReader(INDEX_DIR);) {
            Concordance concord = new Concordance(reader, 10);
            List<KwicResult> kwic_list = concord.findWord("詞");            
            for(KwicResult kr: kwic_list){
                String input = kr.toStringRepr(false);
                Sketcher sketcher = new Sketcher();
                String results = sketcher.match(input, rules);
                System.out.println(results);
            }            
        }
    }
        
    @Test
    public void testSketch_Json() throws IOException {
        setup();
        Luminance lum = new Luminance(INDEX_DIR);
        JsonArray ret = lum.sketch_text("詞", true);
        assertTrue(ret.size() > 0);
    }
    
    @Test
    public void testResource() {
        
    }
}
