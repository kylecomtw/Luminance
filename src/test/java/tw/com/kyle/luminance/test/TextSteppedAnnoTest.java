package tw.com.kyle.luminance.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import tw.com.kyle.luminance.Concordance;
import tw.com.kyle.luminance.KwicResult;
import tw.com.kyle.luminance.LumReader;
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
        
        long ref_uuid = elem.get("uuid").getAsLong();
        JsonObject anno_templ = lum.get_annotation_template(ref_uuid);
        Files.write(Paths.get("etc/test/plain_text.anno.templ.txt"), 
                anno_templ.get("anno.templ").getAsString().getBytes(StandardCharsets.UTF_8));
        long sz_templ = Files.size(Paths.get("etc/test/plain_text.anno.templ.txt"));
        assertTrue(Files.size(Paths.get("etc/test/plain_text.anno.templ.txt")) > 20);
                
        String anno_seg1 = readAll("etc/test/plain_text.anno.seg1.txt").replace("{REF}", String.valueOf(ref_uuid));
        lum.add_document(anno_seg1);
        
        String anno_seg2 = readAll("etc/test/plain_text.anno.seg2.txt").replace("{REF}", String.valueOf(ref_uuid));
        lum.add_document(anno_seg2);
        
        String anno_pos = readAll("etc/test/plain_text.anno.pos.txt").replace("{REF}", String.valueOf(ref_uuid));
        lum.add_document(anno_pos);
        
        String anno_other = readAll("etc/test/plain_text.anno.other.txt").replace("{REF}", String.valueOf(ref_uuid));
        lum.add_document(anno_other);
        lum.close();
                
        JsonArray annot_obj = lum.get_annotations(elem.get("uuid").getAsLong());
        assertTrue("Index reference text and annotations: PASS", annot_obj.size() == 4);
        
        LumReader reader = new LumReader(INDEX_DIR);
        Concordance concord = new Concordance(reader, 10);
        List<KwicResult> kwics = concord.findWord("人工智慧");
        assertTrue(kwics.size() == 1);
        kwics.forEach((x)->System.out.println(x));
    }
    
    private String readAll(String fpath) throws IOException{
        String txt = String.join("\n",
                Files.readAllLines(Paths.get(fpath), StandardCharsets.UTF_8));
        return txt;
    }
    
}
