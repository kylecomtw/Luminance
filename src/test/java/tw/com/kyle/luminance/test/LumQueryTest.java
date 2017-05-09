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
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import tw.com.kyle.luminance.LumQuery;
import tw.com.kyle.luminance.LumReader;
import tw.com.kyle.luminance.LumWindow;
import tw.com.kyle.luminance.Luminance;

/**
 *
 * @author Sean_S325
 */
public class LumQueryTest {

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
    public void testDiscourseQuery() {
        setup();
        try (LumQuery query = new LumQuery(INDEX_DIR);) {                        
            List<Integer[]> offs = query.query_for_offsets("意", "content", false);
            assertTrue(offs.get(0)[0] == 0);  // docid
            assertTrue(offs.get(0)[1] == 5);  // start offset
            assertTrue(offs.get(0)[2] == 6);  // end offset
            
        } catch (IOException ex) {
            Logger.getLogger(LumQueryTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testDiscourseNearQuery() {
        setup();
        try (LumQuery query = new LumQuery(INDEX_DIR);){                                    
            List<Integer[]> offs = query.query_for_offsets("自動分詞", "content", true);
            assertTrue(offs.get(0)[0] == 0);  // docid
            assertTrue(offs.get(0)[1] == 78);  // start offset
            assertTrue(offs.get(0)[2] == 82);  // end offset            

        } catch (IOException ex) {
            Logger.getLogger(LumQueryTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    @Test
    public void testAnnoQuery() {
        setup();     
        try (LumQuery query = new LumQuery(INDEX_DIR);){                   
            List<Integer[]> offs = query.query_for_offsets("意義", "anno", false);
            assertTrue(offs.get(0)[0] == 1);  // docid
            assertTrue(offs.get(0)[1] == 5);  // start offset
            assertTrue(offs.get(0)[2] == 7);  // end offset

        } catch (IOException ex) {
            Logger.getLogger(LumQueryTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    @Test
    public void testAnnoPosQuery() {
        setup();        
        try (LumQuery query = new LumQuery(INDEX_DIR);){                
            List<Integer[]> offs = query.query_for_offsets("Neqa", "anno", false);
            assertTrue(offs.get(0)[0] == 2);  // docid
            assertTrue(offs.get(0)[1] == 20);  // start offset
            assertTrue(offs.get(0)[2] == 22);  // end offset

        } catch (IOException ex) {
            Logger.getLogger(LumQueryTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
