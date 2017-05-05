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
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import tw.com.kyle.luminance.Concordance;
import tw.com.kyle.luminance.LumReader;
import tw.com.kyle.luminance.Luminance;

/**
 *
 * @author Sean_S325
 */
public class ConcordanceTest {  
    private String INDEX_DIR = "h:/index_dir";
    
    @BeforeEach
    public void setup() throws IOException {        
        Luminance.clean_index(INDEX_DIR);
        Luminance lum = new Luminance(INDEX_DIR);
        String txt = String.join("", 
                Files.readAllLines(Paths.get("etc/test/simple_text.txt"), StandardCharsets.UTF_8));        
        JsonObject elem = (JsonObject) lum.add_document(txt);
        lum.close();
    }
    
    @Disabled
    public void testConcordance() throws IOException {
        Luminance lum = new Luminance(INDEX_DIR);
                
        JsonArray con_list = lum.find_text("詞");
        assertThat(con_list.size(), greaterThan(0));
        System.out.println(con_list.toString());
        
        lum.close();
    }
    
    @Test 
    public void testSearch() throws IOException {
        LumReader reader = new LumReader(INDEX_DIR);
        Concordance concord = new Concordance(reader.GetReader());
        concord.query("詞", "content", false);
    }
}
