/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.test;

import java.util.ArrayList;
import java.util.List;
import static junit.framework.Assert.assertTrue;
import org.junit.Test;
import tw.com.kyle.luminance.LumRange;
import tw.com.kyle.luminance.LumToken.LTField;
import tw.com.kyle.luminance.LumTokensBuilder;

/**
 *
 * @author Sean
 */
public class LumTokensBuilderTest {
    @Test
    public void testCombines() {
        List<LumRange> rdata = new ArrayList<>();
        LumTokensBuilder builder = new LumTokensBuilder();        
        rdata.add(make_range(0, 1, "A"));
        rdata.add(make_range(1, 3, "B"));
        rdata.add(make_range(3, 4, "C"));
        rdata.add(make_range(4, 6, "D"));
        builder.combines(rdata);
        assertTrue(builder.toString().equals("A\u3000B\u3000C\u3000D"));
    }
    
    @Test
    public void testCombines2() {
        List<LumRange> rdata1 = new ArrayList<>();        
        rdata1.add(make_range(0, 1, "A"));
        rdata1.add(make_range(1, 3, "B"));
        rdata1.add(make_range(3, 4, "C"));
        rdata1.add(make_range(4, 6, "D"));
        
        List<LumRange> rdata2 = new ArrayList<>();        
        rdata2.add(make_range(0, 1, "w"));
        rdata2.add(make_range(1, 3, "x"));
        rdata2.add(make_range(3, 4, "y"));
        rdata2.add(make_range(4, 6, "z"));
        
        LumTokensBuilder builder = new LumTokensBuilder();                
        builder.combines(rdata1).combines(rdata2, LTField.POS);
        assertTrue(builder.toString().equals("A(w)\u3000B(x)\u3000C(y)\u3000D(z)"));
    }
    
    @Test
    public void testCombines3() {
        List<LumRange> rdata1 = new ArrayList<>();        
        rdata1.add(make_range(0, 1, "A"));
        rdata1.add(make_range(1, 3, "B"));
        rdata1.add(make_range(3, 4, "C"));
        rdata1.add(make_range(4, 6, "D"));
        
        List<LumRange> rdata2 = new ArrayList<>();                
        rdata2.add(make_range(1, 3, "x"));
        rdata2.add(make_range(3, 4, "y"));        
        
        LumTokensBuilder builder = new LumTokensBuilder();                
        builder.combines(rdata1).combines(rdata2, LTField.POS);
        String str = builder.toString();
        assertTrue(str.equals("A\u3000B(x)\u3000C(y)\u3000D"));
    }
    
    private LumRange make_range(int so, int eo, String d) {
        LumRange lr = new LumRange();
        lr.data = d;
        lr.end_off = eo;
        lr.start_off = so;
        return lr;
    }
}
