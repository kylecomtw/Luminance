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
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tw.com.kyle.luminance.LumReader;
import tw.com.kyle.luminance.LumWindow;
import tw.com.kyle.luminance.Luminance;

/**
 *
 * @author Sean_S325
 */
public class LumWindowTest {

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

    @Test
    public void testInstantiation() throws IOException {
        LumReader lum_reader = new LumReader(INDEX_DIR);
        IndexReader reader = lum_reader.GetReader();
        Document targ_doc = reader.document(0);
        LumWindow lumWin = new LumWindow();
        lumWin.initialize(targ_doc, reader);                
    }
    
    @Test
    public void testDiscourseWindow() throws IOException {
        LumReader lum_reader = new LumReader(INDEX_DIR);
        IndexReader reader = lum_reader.GetReader();
        Document targ_doc = reader.document(0);
        LumWindow lumWin = new LumWindow();
        lumWin.initialize(targ_doc, reader);  
        
        String ret = lumWin.GetWindow(5, 10, 11);
        assertEquals("window text assertion", ret, "意義且可以 自 由使用的語");
    }
    
    @Test
    public void testAnnotationWindow() throws IOException {
        LumReader lum_reader = new LumReader(INDEX_DIR);
        IndexReader reader = lum_reader.GetReader();
        Document targ_doc = reader.document(1);
        LumWindow lumWin = new LumWindow();
        lumWin.initialize(targ_doc, reader);  
        
        String ret = lumWin.GetWindow(5, 5, 6);
        assertEquals("window text assertion", ret, "詞是最小有 意義 且可以自由");
    }
    
    @Test
    public void testReconstruct() throws IOException {
        LumReader lum_reader = new LumReader(INDEX_DIR);
        IndexReader reader = lum_reader.GetReader();
        Document targ_doc = reader.document(1);
        LumWindow lumWin = new LumWindow();
        lumWin.initialize(targ_doc, reader);  
        
        String ret = lumWin.Reconstruct(5, 5, 6);
        
        
    }
}
