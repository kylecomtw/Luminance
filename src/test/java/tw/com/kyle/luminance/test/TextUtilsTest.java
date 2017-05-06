package tw.com.kyle.luminance.test;


import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import tw.com.kyle.luminance.TextUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sean_S325
 */
public class TextUtilsTest {
    @Test
    public void testAnnotation() {
        String annot_text = "自備(VC)　２５０萬(Neu)　，(COMMACATEGORY)\n" + 
                "並(Cbb)　可(D)　省(VJ)　仲介(Nv)　費用(Na)　。(PERIODCATEGORY)";
        String raw = TextUtils.extract_raw_text(annot_text);
        List<String[]> seg = TextUtils.extract_seg_annot(annot_text);
        List<String[]> pos = TextUtils.extract_pos_annot(annot_text);
        
    }
    
    @Test
    public void testNer() {
        String annot_text = "自備(VC)　２５０萬(Neu/NEU)　，(COMMACATEGORY)\n" + 
                "並(Cbb)　可(D)　省(VJ)　仲介(Nv)　費用(Na)　。(PERIODCATEGORY)";
        List<String[]> ner = TextUtils.extract_ner_annot(annot_text);
        assertEquals(ner.get(1)[1], "NEU");
    }
    
    @Test
    public void testHint() {
        String annot_text = "自備@0(VC)　２５０萬(Neu/NEU)　，(COMMACATEGORY)\n" + 
                "並(Cbb)　可(D)　省@5(VJ)　仲介(Nv)　費用(Na)　。(PERIODCATEGORY)";
        List<String[]> seg = TextUtils.extract_seg_annot(annot_text);
        assertTrue(seg.get(5)[0].equals("省@5"));
    }
}
