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
import java.util.stream.Collectors;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.document.Document;
import tw.com.kyle.luminance.LumToken.LTField;

/**
 *
 * @author Sean
 */
public class LumWindow {

    private Logger logger = Logger.getLogger(LumWindow.class.getName());
    private LumReader lum_reader = null;
    private String ref_doc_content = null;
    private Mappings ref_mappings = null;
    private LumAnnotations lum_annot = null;
    private int ref_doc_id = -1;
    private long ref_uuid = 0;
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
            if (doc == null) {
                throw new NullPointerException();
            }
            String doc_class = doc.get("class");
            if (doc_class.equals(LumIndexer.DOC_DISCOURSE)) {
                initialize_mappings(doc, lum_reader);
            } else {
                Document ref_doc = lum_reader.GetDocument(LumUtils.BytesRefToLong(doc.getBinaryValue("base_ref")));
                initialize_mappings(ref_doc, lum_reader);
            }
            
            int REF_DOC_MAX = Math.max(ref_doc_content.length() - 1, 0);
            min_guard = (int x) -> Math.max(0, Math.min(REF_DOC_MAX, x));
            max_guard = (int x) -> Math.min(REF_DOC_MAX, x);
        } catch (IOException ex) {
            Logger.getLogger(LumWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            logger.severe(ex.getLocalizedMessage());

        }
    }

    private void initialize_mappings(Document ref_doc, LumReader lum_reader)
            throws IOException {
        if (ref_doc != null) {
            ref_doc_content = ref_doc.get("content");
            ref_doc_id = lum_reader.getDocId(ref_doc);
            ref_uuid = lum_reader.GetDocUuid(ref_doc);
            // ref_mappings = prepare_mappings(ref_doc_id, "content");
            buildAnnotationData(ref_uuid, ref_doc_id);
        } else {
            logger.severe("Mapping initialization error");
        }
    }

    public KwicResult Reconstruct(int window_size, int ref_soff, int ref_eoff) {
        KwicResult kwic = new KwicResult();
        try {
            kwic.keyword = reconstruct_token_list(ref_soff, ref_eoff);
            kwic.prec_context = reconstruct_token_list(
                    min_guard.apply(ref_soff - window_size),
                    max_guard.apply(ref_soff));
            kwic.succ_context = reconstruct_token_list(
                    min_guard.apply(ref_eoff),
                    max_guard.apply(ref_eoff + window_size));
        } catch (IOException ex) {
            logger.severe(ex.toString());
        } 
        return kwic;
    }

    private List<LumToken> reconstruct_token_list(int soff, int eoff) throws IOException {
        if (eoff - soff == 0 || ref_doc_content == null) return new ArrayList<>();        
        
        long seg_uuid = lum_annot.getLatestUuid("seg");
        long pos_uuid = lum_annot.getLatestUuid("pos");
        long ner_uuid = lum_annot.getLatestUuid("ner");        
        

        LumTokensBuilder builder = new LumTokensBuilder();
        
        builder.init(ref_doc_content.substring(soff, eoff), soff);
        
        if (seg_uuid > 0) {
            List<LumRange> seg_range = ExtractLumRanges(seg_uuid, soff, eoff);
            builder.combines(seg_range);
        }

        if (pos_uuid > 0) {
            List<LumRange> pos_range = ExtractLumRanges(pos_uuid, soff, eoff);
            builder.combines(pos_range, LTField.POS);
        }

        if (ner_uuid > 0) {
            List<LumRange> ner_range = ExtractLumRanges(ner_uuid, soff, eoff);
            builder.combines(ner_range, LTField.NER);
        }

        if (builder.nSeq() > 1) {
            logger.info("More than one sequence when reconstructing");
        } else if (builder.nSeq() == 0){
            builder.combines(new ArrayList<>());
        }
        
        return builder.get(0);
        
    }

    public List<LumRange> ExtractLumRanges(long annot_uuid, int ref_soff, int ref_eoff) throws IOException {
        List<LumRange> range_data = lum_annot.GetLumRange(annot_uuid, this);
        List<LumRange> ext_range = range_data.stream()
                .filter((x) -> x.start_off >= ref_soff && x.end_off <= ref_eoff)
                .collect(Collectors.toList());
        return ext_range;
    }

    public LumAnnotations GetAnnotationData() throws IOException {
        return lum_annot;
    }

    public String GetWindow(int window_size, int ref_soff, int ref_eoff) {
        return String.join(" ", GetWindowAsArray(window_size, ref_soff, ref_eoff));
    }

    public String[] GetWindowAsArray(int window_size, int ref_soff, int ref_eoff) {
        Integer[] ref_range = new Integer[]{ref_soff, ref_eoff};
        int w = window_size;
        int ref_so = ref_range[0];
        int ref_eo = ref_range[1];
        String[] kwic = new String[]{
            ref_doc_content.substring(min_guard.apply(ref_so - w), max_guard.apply(ref_so)),
            ref_doc_content.substring(min_guard.apply(ref_so), max_guard.apply(ref_eo)),
            ref_doc_content.substring(min_guard.apply(ref_eo), max_guard.apply(ref_eo + w))};
        return kwic;
    }

    public List<LumRange> BuildLumRange(long annot_uuid) throws IOException {
        Document adoc = lum_annot.GetAnnotDocument(annot_uuid);
        if (adoc == null) {
            return new ArrayList<>();
        }

        int doc_id = lum_reader.getDocId(adoc);
        TokenStream tokenStream = lum_reader.GetTokenStream(doc_id, "anno");
        if (tokenStream == null) {
            return null;
        }

        OffsetAttribute offAttr = tokenStream.getAttribute(OffsetAttribute.class);
        CharTermAttribute chAttr = tokenStream.getAttribute(CharTermAttribute.class);

        tokenStream.reset();
        List<LumRange> lr_list = new ArrayList<>();
        while (tokenStream.incrementToken()) {
            LumRange lr = new LumRange();
            lr.data = chAttr.toString();
            lr.start_off = offAttr.startOffset();
            lr.end_off = offAttr.endOffset();
            lr_list.add(lr);
        }

        return lr_list;
    }

    private Integer[] map_to_target_position(int ref_soff, int ref_eoff) {
        //! map reference offsets to reference positions
        //! reference positions are target positions
        int ref_spos = ref_mappings.pos_list.get(ref_mappings.off_list.indexOf(ref_soff));
        int ref_epos = ref_mappings.pos_list.get(ref_mappings.off_list.indexOf(ref_eoff));
        return new Integer[]{ref_spos, ref_epos};

    }

    private Integer[] map_to_reference_offset(int targ_spos, int targ_epos) {
        //! target positions are reference positions        

        //! target position -> target offset        
        if (ref_mappings == null) {
            return new Integer[]{};
        }

        //! target offset is in the same axis of ref position
        //! target offset ->  reference offset
        int ref_so = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_spos));
        int ref_eo = 0;
        try {
            ref_eo = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_epos) - 1) + 1;
        } catch (IndexOutOfBoundsException ex) {
            ref_eo = ref_mappings.off_list.get(ref_mappings.pos_list.indexOf(targ_epos));
        }

        return new Integer[]{ref_so, ref_eo};
    }

    private Mappings prepare_mappings(int doc_id, String field) throws IOException {
        List<Integer> pos_list = new ArrayList<>();
        List<Integer> off_list = new ArrayList<>();

        TokenStream tokenStream = lum_reader.GetTokenStream(doc_id, field);
        if (tokenStream == null) {
            return null;
        }

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

    private void buildAnnotationData(long ruuid, int rdocid) throws IOException {
        lum_annot = new LumAnnotations(ruuid);
        List<Long> annot_uuids = lum_reader.getAnnotations(ruuid);
        if (annot_uuids == null) {
            logger.severe("Cannot find annotation data");
        }
        for (long a_uuid : annot_uuids) {
            Document adoc = lum_reader.GetDocument(a_uuid);
            if (!adoc.get("class").equals(LumDocument.ANNO)) 
                continue;
            lum_annot.AddAnnotation(a_uuid, adoc);
        }
    }
}
