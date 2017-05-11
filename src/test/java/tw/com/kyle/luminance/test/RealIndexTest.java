/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.test;

import com.google.gson.JsonArray;
import java.io.IOException;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import tw.com.kyle.luminance.Luminance;

/**
 *
 * @author Sean
 */
public class RealIndexTest {
    @Test
    public void testConcordance() throws IOException {
        Luminance lum = new Luminance("data/index_ASBC");
        JsonArray kwic_list = lum.findWord("政府");
        assertTrue(kwic_list.size() > 0);
    }
    
    @Test
    public void testConcordance2() throws IOException {
        Luminance lum = new Luminance("data/index_WomenTalk");
        JsonArray kwic_list = lum.findGrams("成立");
        assertTrue(kwic_list.size() > 0);
    }
}
