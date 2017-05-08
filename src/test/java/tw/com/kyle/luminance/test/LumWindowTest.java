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
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.junit.Test;
import tw.com.kyle.luminance.LumAnnotations;

import tw.com.kyle.luminance.LumReader;
import tw.com.kyle.luminance.LumWindow;
import tw.com.kyle.luminance.Luminance;

/**
 *
 * @author Sean_S325
 */
public class LumWindowTest {

    private String INDEX_DIR = "h:/index_dir";

    private void setup() {
        try {
            Luminance.clean_index(INDEX_DIR);
            Luminance lum = new Luminance(INDEX_DIR);
            String txt = String.join("",
                    Files.readAllLines(Paths.get("etc/test/simple_text.txt"), StandardCharsets.UTF_8));
            JsonObject elem = (JsonObject) lum.add_document(txt);            
            lum.close();
        } catch (IOException ex) {
            System.out.println(ex);
            fail(ex.toString());
        }
    }

    @Test
    public void testInstantiation() {
        setup();
        try (LumReader lum_reader = new LumReader(INDEX_DIR);) {
            IndexReader reader = lum_reader.GetReader();
            Document targ_doc = lum_reader.GetDocumentByDocId(0);
            LumWindow lumWin = new LumWindow(targ_doc, lum_reader);
        } catch (IOException ex) {
            Logger.getLogger(LumWindowTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("IOException thrown");
        }
    }

    @Test
    public void testDiscourseWindow() {
        setup();
        try (LumReader lum_reader = new LumReader(INDEX_DIR);) {            
            IndexReader reader = lum_reader.GetReader();
            Document targ_doc = lum_reader.GetDocumentByDocId(0);
            LumWindow lumWin = new LumWindow(targ_doc, lum_reader);

            String ret = lumWin.GetWindow(5, 10, 11);
            assertTrue(ret.equals("意義且可以 自 由使用的語"));
            lum_reader.close();
        } catch (IOException ex) {
            Logger.getLogger(LumWindowTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("IOException thrown");
        }
    }

    @Test
    public void testAnnotationWindow() {
        setup();
        try (LumReader lum_reader = new LumReader(INDEX_DIR);) {            
            Document targ_doc = lum_reader.GetDocumentByDocId(1);
            LumWindow lumWin = new LumWindow(targ_doc, lum_reader);

            String ret = lumWin.GetWindow(5, 5, 7);
            assertTrue(ret.equals("詞是最小有 意義 且可以自由"));
            lum_reader.close();
        } catch (IOException ex) {
            Logger.getLogger(LumWindowTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("IOException thrown");
        }
    }

    @Test
    public void testReconstruct() {
        setup();
        try (LumReader lum_reader = new LumReader(INDEX_DIR);) {            
            Document targ_doc = lum_reader.GetDocumentByDocId(1);
            LumWindow lumWin = new LumWindow(targ_doc, lum_reader);

            fail("Not implemented");
        } catch (IOException ex) {
            Logger.getLogger(LumWindowTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("IOException thrown");
        }
    }

    @Test
    public void testGetAnnotationList() {
        setup();
        try (LumReader lum_reader = new LumReader(INDEX_DIR);) {                        
            Document ref_doc = lum_reader.GetDocumentByDocId(0);
            LumWindow lumWin = new LumWindow(ref_doc, lum_reader);
            LumAnnotations annot_data = lumWin.GetAnnotationData();
            assertTrue(annot_data.hasSegmentation());
            assertTrue(annot_data.hasPOSTagged());            
        } catch (IOException ex) {
            Logger.getLogger(LumWindowTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("IOException thrown");
        }
    }
}
