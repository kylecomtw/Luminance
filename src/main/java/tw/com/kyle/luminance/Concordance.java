/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.document.Document;
;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Simport org.apache.lucene.document.Document; import
 * org.apache.lucene.index.LeafReader; ean
 */


public class Concordance {

    private LumReader reader = null;
    private int win_size = 10;
    
    public Concordance(LumReader r, int w) {
        reader = r;
        win_size = w;
    }

    public List<KwicResult> findWord(String probe) throws IOException {       
        LumQuery query = new LumQuery(reader);
        List<Integer[]> off_list = query.queryWord(probe);
        List<KwicResult> kwic_list = build_kwic_list(off_list);
        
        return kwic_list;
    }

    public List<KwicResult> findGrams(String probe) throws IOException {
        LumQuery query = new LumQuery(reader);
        List<Integer[]> off_list = query.queryGrams(probe);
        List<KwicResult> kwic_list = build_kwic_list(off_list);
        
        return kwic_list;
    }

    public List<KwicResult> findPos(String probe) throws IOException {
        LumQuery query = new LumQuery(reader);
        List<Integer[]> off_list = query.queryPos(probe);
        List<KwicResult> kwic_list = build_kwic_list(off_list);
        
        return kwic_list;
    }
    
    private List<KwicResult> build_kwic_list(List<Integer[]> off_list) throws IOException {
        List<KwicResult> kwic_list = new ArrayList<>();
        Map<Integer, List<Integer[]>> doc_map = off_list.stream()
                .collect(Collectors.groupingBy((Integer[] x) -> x[0], Collectors.toList()));
        List<Integer> doc_list = doc_map.keySet().stream().sorted().collect(Collectors.toList());
        for (int docid_x : doc_list) {
            Document doc = reader.GetDocumentByDocId(docid_x);
            LumWindow lumWin = new LumWindow(doc, reader);
            List<KwicResult> kwics = doc_map.get(docid_x).stream()
                    .map((x) -> lumWin.Reconstruct(win_size, x[1], x[2]))
                    .collect(Collectors.toList());
            kwic_list.addAll(kwics);
        }
        
        return kwic_list;
    }
}
