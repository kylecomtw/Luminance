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
    private Document targ_doc = null;
    private String base_doc = null;
    private List<Integer> pos_list = null;
    private List<Integer> off_list = null;
    private IntFunction<Integer> min_guard = null;
    private IntFunction<Integer> max_guard = null;
    public LumWindow(long uuid, IndexReader r) {
        reader = r;
        try {
            int doc_id = get_base_doc(uuid);
            if (doc_id >= 0) {
                targ_doc = reader.document(doc_id);
                base_doc = targ_doc.get("content"); 
                prepare_mappings(doc_id);
                min_guard = (int x)->Math.max(0, x-3);
                max_guard = (int x)->Math.min(base_doc.length()-1, x);
            }
        } catch (IOException ex) {
            Logger.getLogger(LumWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public String GetWindow(int window_size, int start_pos, int end_pos) {
        int w = window_size;
        int so = off_list.get(pos_list.indexOf(start_pos));
        int eo = off_list.get(pos_list.indexOf(end_pos));
        
        return String.format("%s - %s - %s", 
                            base_doc.substring(min_guard.apply(so - w), max_guard.apply(so)),
                            base_doc.substring(min_guard.apply(so), max_guard.apply(eo)),
                            base_doc.substring(min_guard.apply(eo), max_guard.apply(eo + w)));
    }
    
    private int get_base_doc(long uuid) throws IOException {        
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
    
    private void prepare_mappings(int doc_id) throws IOException{
        if(targ_doc == null) return;
        
        pos_list = new ArrayList<Integer>(base_doc.length());
        off_list = new ArrayList<Integer>(base_doc.length());
        TokenStream tokenStream = TokenSources.getTermVectorTokenStreamOrNull(
                        "content", 
                        reader.getTermVectors(doc_id), -1);
        OffsetAttribute offsetAttr = tokenStream.getAttribute(OffsetAttribute.class);
        PositionIncrementAttribute posincAttr = tokenStream.getAttribute(PositionIncrementAttribute.class);
        int pos_counter = 0;
        while(tokenStream.incrementToken()){
            pos_list.add(pos_counter);
            off_list.add(offsetAttr.startOffset());
            pos_counter += posincAttr.getPositionIncrement();
        }
    }
}
