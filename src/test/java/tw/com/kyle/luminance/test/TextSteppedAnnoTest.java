package tw.com.kyle.luminance.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import tw.com.kyle.luminance.Luminance;

/**
 *
 * @author Sean_S325
 */
public class TextSteppedAnnoTest {

    String INDEX_DIR = "h:/index_dir";

    @Test
    public void test_plaintext() throws IOException {
        Luminance.clean_index(INDEX_DIR);
        Luminance lum = new Luminance(INDEX_DIR);
        String txt = readAll("etc/test/plain_text.txt");
        JsonObject elem = (JsonObject) lum.add_document(txt);
        lum.close();
        
        JsonObject anno_templ = lum.get_annotation_template(elem.get("uuid").getAsLong());
        Files.write(Paths.get("etc/test/plain_text.anno.templ.txt"), 
                anno_templ.get("anno.templ").getAsString().getBytes(StandardCharsets.UTF_8));
                
        String anno_seg1 = readAll("etc/test/plain_text.anno.seg1.txt");
        lum.add_document(anno_seg1);
        
        String anno_seg2 = readAll("etc/test/plain_text.anno.seg2.txt");
        lum.add_document(anno_seg2);
        
        String anno_pos = readAll("etc/test/plain_text.anno.pos.txt");
        lum.add_document(anno_pos);
        
        String anno_other = readAll("etc/test/plain_text.anno.other.txt");
        lum.add_document(anno_other);
        
        JsonArray annot_obj = lum.get_annotations(elem.get("uuid").getAsLong());
        assertTrue(annot_obj.size() > 0);
    }
    
    private String readAll(String fpath) throws IOException{
        String txt = String.join("",
                Files.readAllLines(Paths.get(fpath), StandardCharsets.UTF_8));
        return txt;
    }
    
}
