/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
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
import org.apache.lucene.search.highlight.TokenSources;

/**
 *
 * @author Sean
 */
public class LumWindow {
    private Logger logger = Logger.getLogger(LumWindow.class.getName());
    private IndexReader reader = null;
    private String ref_doc_content = null;
    private Mappings targ_mappings = null;
    private Mappings ref_mappings = null;
    private IntFunction<Integer> min_guard = null;
    private IntFunction<Integer> max_guard = null;

    private class Mappings {
        public List<Integer> pos_list;
        public List<Integer> off_list;
    }

    private void common_init() {
        
    }

    public LumWindow() {

    }

    public void initialize(Document doc, IndexReader r) {
        reader = r;        
        try {
            if (doc == null) throw new NullPointerException();
            LumReader lum_reader = new LumReader(reader);
            String doc_class = doc.get("class");
            if (doc_class.equals(LumIndexer.DOC_DISCOURSE)){
                initialize_mappings(null, doc, lum_reader);
            } else {
                Document ref_doc = lum_reader.getDocument(LumUtils.BytesRefToLong(doc.getBinaryValue("base_ref")));
                initialize_mappings(doc, ref_doc, lum_reader);
            }      
            
            min_guard = (int x) -> Math.max(0, x);
            max_guard = (int x) -> Math.min(ref_doc_content.length() - 1, x);
        } catch (IOException ex) {
            Logger.getLogger(LumWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex){
            logger.severe(ex.getLocalizedMessage());      
            
        }
    }
    
    private void initialize_mappings(Document targ_doc, Document ref_doc, LumReader lum_reader) 
            throws IOException {
        if (ref_doc != null && targ_doc != null) {
            ref_doc_content = ref_doc.get("content");
            int ref_doc_id = lum_reader.getDocId(ref_doc);
            ref_mappings = prepare_mappings(ref_doc_id, "content");

            int targ_doc_id = LumUtils.GetDocId(targ_doc, reader);
            targ_mappings = prepare_mappings(targ_doc_id, "anno");
        } else if (ref_doc != null && targ_doc == null) {
            ref_doc_content = ref_doc.get("content");
            int ref_doc_id = LumUtils.GetDocId(ref_doc, reader);            
            ref_mappings = prepare_mappings(ref_doc_id, "content");
        } else {
            logger.severe("Mapping initialization error");
        }
    }
    
    public String Reconstruct(int window_size, int targ_spos, int targ_epos) {
        Integer[] ref_range = map_to_reference_offset(targ_spos, targ_epos);
        return null;
    }
    
    public String GetWindow(int window_size, int targ_spos, int targ_epos) {
        return String.join(" ", GetWindowAsArray(window_size, targ_spos, targ_epos));
    }

    public String[] GetWindowAsArray(int window_size, int targ_spos, int targ_epos) {        
        Integer[] ref_range = map_to_reference_offset(targ_spos, targ_epos);
        int w = window_size;
        int ref_so = ref_range[0];
        int ref_eo = ref_range[1];
        String[] kwic = new String[]{
            ref_doc_content.substring(min_guard.apply(ref_so - w), max_guard.apply(ref_so)),
            ref_doc_content.substring(min_guard.apply(ref_so), max_guard.apply(ref_eo)),
            ref_doc_content.substring(min_guard.apply(ref_eo), max_guard.apply(ref_eo + w))};
        return kwic;
    }
    
    private Integer[] map_to_target_position(int ref_soff, int ref_eoff){
        int ref_spos = 0;
        int ref_epos = 0;
        
        //! reference offset -> reference position
        if (targ_mappings != null) {
            ref_spos = targ_mappings.off_list.get(targ_mappings.pos_list.indexOf(ref_soff));
            ref_epos = targ_mappings.off_list.get(targ_mappings.pos_list.indexOf(ref_eoff));
        } else {
            ref_spos = ref_soff;
            ref_epos = ref_eoff;
        }
        
        return new Integer[] {0, 0};
    }
    
    private Integer[] map_to_reference_offset(int targ_spos, int targ_epos){
        int targ_so = 0;
        int targ_eo = 0;
        
        //! target position -> target offset
        if (targ_mappings != null) {
            targ_so = targ_mappings.off_list.get(targ_mappings.pos_list.indexOf(targ_spos));
            targ_eo = targ_mappings.off_list.get(targ_mappings.pos_list.indexOf(targ_epos));
        } else {
            //! when there is no 
            targ_so = targ_spos;
            targ_eo = targ_epos;
        }
                
        if (ref_mappings == null) return new Integer[]{};
        
        //! target offset is in the same axis of ref position
        //! target offset ->  reference offset
        int ref_so = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_so));
        int ref_eo = 0;
        try {
            ref_eo = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_eo) - 1) + 1;
        } catch (IndexOutOfBoundsException ex) {
            ref_eo = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_eo));
        }
        
        return new Integer[] {ref_so, ref_eo};
    }
    
    private Mappings prepare_mappings(int doc_id, String field) throws IOException {
        List<Integer> pos_list = new ArrayList<>();
        List<Integer> off_list = new ArrayList<>();            
        TokenStream tokenStream = TokenSources.getTermVectorTokenStreamOrNull(
                field,
                reader.getTermVectors(doc_id), -1);
        if (tokenStream == null) return null;
        
        OffsetAttribute offsetAttr = tokenStream.getAttribute(OffsetAttribute.class);
        PositionIncrementAttribute posincAttr = tokenStream.getAttribute(PositionIncrementAttribute.class);
        int pos_counter = 0;
        while (tokenStream.incrementToken()) {
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
