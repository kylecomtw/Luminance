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

/**
 *
 * @author Sean
 */
public class LumWindow {
    private Logger logger = Logger.getLogger(LumWindow.class.getName());
    private LumReader lum_reader = null;
    private String ref_doc_content = null;
    // private Mappings targ_mappings = null;
    private Mappings ref_mappings = null;
    private IntFunction<Integer> min_guard = null;
    private IntFunction<Integer> max_guard = null;

    private class Mappings {
        public List<Integer> pos_list;
        public List<Integer> off_list;
    }

    public LumWindow(Document doc, LumReader r) {
        initialize(doc, r);
    }

    public final void initialize(Document doc, LumReader r) {
        lum_reader = r;        
        try {
            if (doc == null) throw new NullPointerException();            
            String doc_class = doc.get("class");
            if (doc_class.equals(LumIndexer.DOC_DISCOURSE)){
                initialize_mappings(doc, lum_reader);
            } else {
                Document ref_doc = lum_reader.GetDocument(LumUtils.BytesRefToLong(doc.getBinaryValue("base_ref")));
                initialize_mappings(ref_doc, lum_reader);
            }      
            
            min_guard = (int x) -> Math.max(0, x);
            max_guard = (int x) -> Math.min(ref_doc_content.length() - 1, x);
        } catch (IOException ex) {
            Logger.getLogger(LumWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex){
            logger.severe(ex.getLocalizedMessage());      
            
        }
    }
    
    private void initialize_mappings(Document ref_doc, LumReader lum_reader) 
            throws IOException {
        if (ref_doc != null) {
            ref_doc_content = ref_doc.get("content");
            int ref_doc_id = lum_reader.getDocId(ref_doc);
            ref_mappings = prepare_mappings(ref_doc_id, "content");
        } else {
            logger.severe("Mapping initialization error");
        }
    }
    
    public String Reconstruct(int window_size, int ref_spos, int ref_epos) {
        Integer[] targ_pos = map_to_target_position(ref_spos - window_size, ref_epos + window_size);
        
        return null;
    }
    
    public String ExtractTargetRange(String field, int targ_spos, int targ_epos) {
        throw new UnsupportedOperationException();
    }
    
    public LumAnnotations GetAnnotationData() {
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
        //! map reference offsets to reference positions
        //! reference positions are target positions
        int ref_spos = ref_mappings.pos_list.get(ref_mappings.off_list.indexOf(ref_soff));
        int ref_epos = ref_mappings.pos_list.get(ref_mappings.off_list.indexOf(ref_eoff));
        return new Integer[] {ref_spos, ref_epos};
        
    }
    
    private Integer[] map_to_reference_offset(int targ_spos, int targ_epos){
        //! target positions are reference positions        
        
        //! target position -> target offset        
                
        if (ref_mappings == null) return new Integer[]{};
        
        //! target offset is in the same axis of ref position
        //! target offset ->  reference offset
        int ref_so = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_spos));
        int ref_eo = 0;
        try {
            ref_eo = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_epos) - 1) + 1;
        } catch (IndexOutOfBoundsException ex) {
            ref_eo = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_epos));
        }
        
        return new Integer[] {ref_so, ref_eo};
    }
    
    private Mappings prepare_mappings(int doc_id, String field) throws IOException {
        List<Integer> pos_list = new ArrayList<>();
        List<Integer> off_list = new ArrayList<>();   
        
        TokenStream tokenStream = lum_reader.GetTokenStream(doc_id, field);
        if (tokenStream == null) return null;
        
        OffsetAttribute offsetAttr = tokenStream.getAttribute(OffsetAttribute.class);
        PositionIncrementAttribute posincAttr = tokenStream.getAttribute(PositionIncrementAttribute.class);
        tokenStream.reset();
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
