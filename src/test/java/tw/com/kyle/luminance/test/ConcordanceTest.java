/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.test;

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

/**
 *
 * @author Sean_S325
 */
public class ConcordanceTest {  
    private String INDEX_DIR = "h:/index_dir";
          
    private void setup() {
        try {
            Luminance.clean_index(INDEX_DIR);
            Luminance lum = new Luminance(INDEX_DIR);
            String txt = String.join("\n",
                    Files.readAllLines(Paths.get("etc/test/simple_text.txt"), StandardCharsets.UTF_8));
            JsonObject elem = (JsonObject) lum.add_document(txt);
            lum.close();
        } catch (IOException ex) {
            System.out.println(ex);
            fail(ex.toString());
        }
    }
        
    @Test
    public void testConcordance_Word() throws IOException {
        setup();
        try(LumReader reader = new LumReader(INDEX_DIR);){
            Concordance concord = new Concordance(reader, 10);
            List<KwicResult> kwic_list = concord.findWord("詞");
            kwic_list.stream().forEach((x)->System.out.println(x.toString()));
            assertTrue(kwic_list.size() > 0); 
        }
    }
    
    @Test
    public void testConcordance_Grams() throws IOException {
        setup();
        try(LumReader reader = new LumReader(INDEX_DIR);){
            Concordance concord = new Concordance(reader, 10);
            List<KwicResult> kwic_list = concord.findGrams("自動分詞");            
            kwic_list.stream().forEach((x)->System.out.println(x.toString()));
            assertTrue(kwic_list.size() > 0); 
        }
    }

    @Test
    public void testConcordance_Tag() throws IOException {
        setup();
        try(LumReader reader = new LumReader(INDEX_DIR);){
            Concordance concord = new Concordance(reader, 10);
            List<KwicResult> kwic_list = concord.findPos("Neqa");
            kwic_list.stream().forEach((x)->System.out.println(x.toString()));
            assertTrue(kwic_list.size() > 0); 
        }
    }
}
