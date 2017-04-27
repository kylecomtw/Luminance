package tw.com.kyle.luminance.test;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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
    @Test
    public void testAddSimpleText() throws IOException{
        Luminance lum = new Luminance();
        String txt = String.join("", 
                Files.readAllLines(Paths.get("etc/test/simple_text.txt"), StandardCharsets.UTF_8));
        JsonObject elem = (JsonObject) lum.add_document(txt);
        String uuid = elem.get("uuid").getAsString();
        assertThat(uuid.length(), is(16));
        lum.get_annotation_template();
        lum.find_text();
    }
}
