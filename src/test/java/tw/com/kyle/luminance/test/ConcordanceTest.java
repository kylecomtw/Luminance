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
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.junit.Test;
import tw.com.kyle.luminance.Concordance;
import tw.com.kyle.luminance.KwicResult;
import tw.com.kyle.luminance.LumReader;
import tw.com.kyle.luminance.Luminance;

/**
 *
 * @author Sean_S325
 */
public class ConcordanceTest {  
    private String INDEX_DIR = "h:/index_dir";
          
    private void setup() {
        try {
            Luminance.clean_index(INDEX_DIR);
            Luminance lum = new Luminance(INDEX_DIR);
            String txt = String.join("\n",
                    Files.readAllLines(Paths.get("etc/test/simple_text.txt"), StandardCharsets.UTF_8));
            JsonObject elem = (JsonObject) lum.add_single_document(txt);
            lum.close();
        } catch (IOException ex) {
            System.out.println(ex);
            fail(ex.toString());
        }
    }
        
    @Test
    public void testConcordance_Word() throws IOException {
        setup();
        try(LumReader reader = new LumReader(INDEX_DIR);){
            Concordance concord = new Concordance(reader, 10);
            List<KwicResult> kwic_list = concord.findWord("詞");            
            kwic_list.stream().forEach((x)->System.out.println(x.toString()));
            assertTrue(kwic_list.size() == 6); 
            assertTrue(kwic_list.get(0).toString().equals(
                    "<詞(Na)>　是(SHI)　最(Dfa)　小(VH)　有(V_2)　意義(Na)　且(Cbb)　可以(D)　自"));
            assertTrue(kwic_list.get(5).toString().equals(
                    "錄　的(DE)　詞(Na)　出現(VH)　的(DE)　問題(Na)　（　新(VH)　<詞(Na)>　如何(D)　辨認(VC)　）"));
            
        }
    }
    
    @Test
    public void testConcordance_Grams() throws IOException {
        setup();
        try(LumReader reader = new LumReader(INDEX_DIR);){
            Concordance concord = new Concordance(reader, 10);
            List<KwicResult> kwic_list = concord.findGrams("自動分詞");            
            kwic_list.stream().forEach((x)->System.out.println(x.toString()));
            assertTrue(kwic_list.size() == 2); 
            assertTrue(kwic_list.get(0).toString().equals(
                    "、　資訊(Na)　抽取(VC)　。　因此(Cbb)　中文(Na)　<自動(VH)　分詞(Na)>　的(DE)　" + 
                    "工作(Na)　成(VG)　了(Di)　語言(Na)　處理(VC)　不"));
            assertTrue(kwic_list.get(1).toString().equals(
                    "可　或　缺　的(DE)　技術(Na)　。　基本(Na)　上(Ncd)　<自動(VH)　分詞(Na)>　多(D)　" + 
                    "利用(VC)　詞典(Na)　中(Ng)　收錄(VC)　的(DE)　詞(Na)"));
        }
    }

    @Test
    public void testConcordance_Tag() throws IOException {
        setup();
        try(LumReader reader = new LumReader(INDEX_DIR);){
            Concordance concord = new Concordance(reader, 10);
            List<KwicResult> kwic_list = concord.findPos("Neqa");
            kwic_list.stream().forEach((x)->System.out.println(x.toString()));
            assertTrue(kwic_list.size() == 2); 
            assertTrue(kwic_list.get(0).toString().equals(
                    "自由(VH)　使用(VC)　的(DE)　語言(Na)　單位(Na)　。　<任何(Neqa)>　語言(Na)　處理(VC)　的(DE)　系統(Na)　都(D)　必須(D)"));
            assertTrue(kwic_list.get(1).toString().equals(
                    "歧義(Na)　的(DE)　切分(VC)　結果(Na)　，　因此(Cbb)　<多數(Neqa)>　的(DE)　中文(Na)　分詞(Na)　程式(Na)　多(D)　討論(VE)"));
        }
    }
}
