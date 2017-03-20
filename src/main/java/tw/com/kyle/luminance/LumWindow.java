/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Sean
 */
public class LumWindow {
    private IndexReader reader = null;
    private Document ref_doc = null;
    private String ref_doc_content = null;
    private Mappings targ_mappings = null;
    private Mappings ref_mappings = null;
    private IntFunction<Integer> min_guard = null;
    private IntFunction<Integer> max_guard = null;
    private class Mappings {
        public List<Integer> pos_list; public List<Integer> off_list;
    }
    
    private void common_init() {
        min_guard = (int x)->Math.max(0, x);
        max_guard = (int x)->Math.min(ref_doc_content.length()-1, x);
    }
    
    public LumWindow(Document targ_doc, IndexReader r) {
        reader = r;        
        try {                                   
            if (targ_doc != null) {
                ref_doc = targ_doc;
                ref_doc_content = ref_doc.get("content");                 
                int targ_doc_id = LumUtils.GetDocId(targ_doc, reader);
                ref_mappings = prepare_mappings(targ_doc_id, "content");                
                common_init();
            }
        } catch (IOException ex) {
            Logger.getLogger(LumWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public LumWindow(Document targ_doc, long ref_uuid, IndexReader r) {
        reader = r;        
        try {            
            int ref_doc_id = get_doc_id(ref_uuid);            
            if (ref_doc_id >= 0 && targ_doc != null) {
                ref_doc = reader.document(ref_doc_id);
                ref_doc_content = ref_doc.get("content"); 
                ref_mappings = prepare_mappings(ref_doc_id, "content");
                
                int targ_doc_id = LumUtils.GetDocId(targ_doc, reader);
                targ_mappings = prepare_mappings(targ_doc_id, "anno");
                common_init();
            }
        } catch (IOException ex) {
            Logger.getLogger(LumWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public String GetWindow(int window_size, int targ_spos, int targ_epos) {
        int w = window_size;
        int targ_so = 0; int targ_eo = 0;
        if (targ_mappings != null){
            targ_so = targ_mappings.off_list.get(targ_mappings.pos_list.indexOf(targ_spos));
            targ_eo = targ_mappings.off_list.get(targ_mappings.pos_list.indexOf(targ_epos));
        } else {
            targ_so = targ_spos;
            targ_eo = targ_epos;
        }
        // target offset is ref position
        int ref_so = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_so));
        int ref_eo = 0;
        try {
            ref_eo = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_eo) - 1) + 1;        
        } catch (IndexOutOfBoundsException ex){
            ref_eo = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_eo));   
        }
                
        String kwic = String.format("%s - %s - %s", 
                            ref_doc_content.substring(min_guard.apply(ref_so - w), max_guard.apply(ref_so)),
                            ref_doc_content.substring(min_guard.apply(ref_so), max_guard.apply(ref_eo)),
                            ref_doc_content.substring(min_guard.apply(ref_eo), max_guard.apply(ref_eo + w)));
        return kwic;
    }
    
    private int get_doc_id(long uuid) throws IOException {        
        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
        buf.putLong(uuid);
        TermQuery q = new TermQuery(new Term("uuid", new BytesRef(buf.array())));
        IndexSearcher is = new IndexSearcher(reader);
        TopDocs docs = is.search(q, 3);
        if (docs.totalHits > 0){
            return docs.scoreDocs[0].doc;
        } else {
            return -1;
        }    
    }
    
    private Mappings prepare_mappings(int doc_id, String field) throws IOException{
        if(ref_doc == null) return null;
        
        List<Integer> pos_list = new ArrayList<Integer>();
        List<Integer> off_list = new ArrayList<Integer>();
        TokenStream tokenStream = TokenSources.getTermVectorTokenStreamOrNull(
                        field, 
                        reader.getTermVectors(doc_id), -1);
        OffsetAttribute offsetAttr = tokenStream.getAttribute(OffsetAttribute.class);
        PositionIncrementAttribute posincAttr = tokenStream.getAttribute(PositionIncrementAttribute.class);
        int pos_counter = 0;
        while(tokenStream.incrementToken()){
            pos_list.add(pos_counter);
            off_list.add(offsetAttr.startOffset());
            pos_counter += posincAttr.getPositionIncrement();
        }
        
        Mappings mappings = new Mappings();
        mappings.off_list = off_list;
        mappings.pos_list = pos_list;
        return mappings;
    }
        
}
