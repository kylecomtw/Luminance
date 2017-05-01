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
        min_guard = (int x) -> Math.max(0, x);
        max_guard = (int x) -> Math.min(ref_doc_content.length() - 1, x);
    }

    public LumWindow(Document doc, IndexReader r) {
        reader = r;
        try {
            if (doc == null) throw new NullPointerException();
            LumReader lum_reader = new LumReader(reader);
            if (doc.get("type").equals(LumIndexer.DOC_DISCOURSE)){
                initialize_mappings(null, doc, lum_reader);
            } else {
                Document ref_doc = lum_reader.getDocument(LumUtils.BytesRefToLong(doc.getBinaryValue("base_ref")));
                initialize_mappings(doc, ref_doc, lum_reader);
            }            
            
        } catch (IOException ex) {
            Logger.getLogger(LumWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex){
            logger.severe("Null pointer in constructor");
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
        } else if (ref_doc != null && targ_doc != null) {
            ref_doc_content = ref_doc.get("content");
            int targ_doc_id = LumUtils.GetDocId(targ_doc, reader);            
            ref_mappings = prepare_mappings(targ_doc_id, "content");
        } else {
            logger.severe("Mapping initialization error");
        }
    }

    public String GetWindow(int window_size, int targ_spos, int targ_epos) {
        return String.join(" ", GetWindowAsArray(window_size, targ_spos, targ_epos));
    }

    public String[] GetWindowAsArray(int window_size, int targ_spos, int targ_epos) {
        int w = window_size;
        int targ_so = 0;
        int targ_eo = 0;
        if (targ_mappings != null) {
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
        } catch (IndexOutOfBoundsException ex) {
            ref_eo = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_eo));
        }

        String[] kwic = new String[]{
            ref_doc_content.substring(min_guard.apply(ref_so - w), max_guard.apply(ref_so)),
            ref_doc_content.substring(min_guard.apply(ref_so), max_guard.apply(ref_eo)),
            ref_doc_content.substring(min_guard.apply(ref_eo), max_guard.apply(ref_eo + w))};
        return kwic;
    }


    private Mappings prepare_mappings(int doc_id, String field) throws IOException {
        List<Integer> pos_list = new ArrayList<>();
        List<Integer> off_list = new ArrayList<>();            
        TokenStream tokenStream = TokenSources.getTermVectorTokenStreamOrNull(
                field,
                reader.getTermVectors(doc_id), -1);
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
