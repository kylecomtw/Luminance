package tw.com.kyle.luminance.test;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import static junit.framework.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;

import tw.com.kyle.luminance.Luminance;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sean_S325
 */
public class LuminanceTest {
    @Ignore @Test
    public void testAddSimpleText() throws IOException{
        String INDEX_DIR = "h:/index_dir";
        Luminance.clean_index(INDEX_DIR);
        Luminance lum = new Luminance(INDEX_DIR);        
        String txt = String.join("", 
                Files.readAllLines(Paths.get("etc/test/simple_text.txt"), StandardCharsets.UTF_8));
        JsonObject elem = (JsonObject) lum.add_document(txt);
        lum.close();
                   
        assertTrue(elem.has("uuid"));       
        assertTrue(elem.has("seg"));       
        assertTrue(elem.has("pos"));       
        
    }

}
