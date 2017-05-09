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
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.junit.Test;
import tw.com.kyle.luminance.KwicResult;
import tw.com.kyle.luminance.LumAnnotations;
import tw.com.kyle.luminance.LumRange;

import tw.com.kyle.luminance.LumReader;
import tw.com.kyle.luminance.LumToken;
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
    public void testExtractLumRanges() {
        setup();
        try (LumReader lum_reader = new LumReader(INDEX_DIR);) {
            Document targ_doc = lum_reader.GetDocumentByDocId(1);
            LumWindow lumWin = new LumWindow(targ_doc, lum_reader);
            LumAnnotations annot_data = lumWin.GetAnnotationData();
            List<LumRange> range_data = lumWin.ExtractLumRanges(annot_data.getLatestUuid("seg"), 5, 7);
            assertTrue(range_data.get(0).data.equals("意義"));
            assertTrue(range_data.get(0).start_off == 5);
            assertTrue(range_data.get(0).end_off == 7);

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

            KwicResult kwic = lumWin.Reconstruct(5, 5, 7);
            String str = kwic.toString();
            assertTrue(str.equals("詞(Na)　是(SHI)　最(Dfa)　小(VH)　有(V_2)　<意義(Na)>　且(Cbb)　可以(D)　自由(VH)"));           
        } catch (IOException ex) {
            Logger.getLogger(LumWindowTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("IOException thrown");
        }
    }
    
    @Test
    public void testReconstruct_partial() {
        setup();
        try (LumReader lum_reader = new LumReader(INDEX_DIR);) {
            Document targ_doc = lum_reader.GetDocumentByDocId(1);
            LumWindow lumWin = new LumWindow(targ_doc, lum_reader);
            
            KwicResult kwic = lumWin.Reconstruct(7, 25, 27);
            String str = kwic.toString();            
            assertTrue(str.equals("位　。　任何(Neqa)　語言(Na)　處　<理　的(DE)>　系統(Na)　都(D)　必須(D)　先能(Nb)"));           
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
            assertTrue(annot_data.size() == 2);
        } catch (IOException ex) {
            Logger.getLogger(LumWindowTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("IOException thrown");
        }
    }
}
