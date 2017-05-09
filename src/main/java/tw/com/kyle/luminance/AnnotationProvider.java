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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Sean_S325
 */
public class AnnotationProvider {

    private boolean is_segmented = false;
    private boolean is_pos_tagged = false;
    private boolean is_ner_tagged = false;
    private String annot_text = "";
    private LumPositionMap pos_map = null;
    private Logger logger = Logger.getLogger(AnnotationProvider.class.getName());

    public boolean has_segmented() {
        return is_segmented;
    }

    public boolean has_pos_tagged() {
        return is_pos_tagged;
    }

    public boolean has_ner_tagged() {
        return is_ner_tagged;
    }

    public AnnotationProvider(String annots) {
        is_segmented = TextUtils.is_segmented(annots);
        is_pos_tagged = TextUtils.is_pos_tagged(annots);
        annot_text = annots;
        try {
            pos_map = LumPositionMap.Get(TextUtils.extract_raw_text(annot_text));
        } catch (IOException ex) {
            logger.severe(ex.toString());
        }
    }

    public LumDocument create_discourse_doc() {
        LumDocument lum_doc = new LumDocument();
        lum_doc.SetDocType(LumDocument.DISCOURSE);
        lum_doc.SetContent(TextUtils.extract_raw_text(annot_text));
        return lum_doc;
    }

    public LumDocument create_annot_doc_seg(BytesRef b_doc_uuid) {
        if (!is_segmented) {
            return null;
        }
        
        long doc_uuid = LumUtils.BytesRefToLong(b_doc_uuid);        
        List<String[]> annot_data = TextUtils.extract_seg_annot(annot_text);
        String anno_content = transform_to_annot_format(annot_data);
        
        LumDocument lum_doc = build_lumDocument(doc_uuid, anno_content);

        return lum_doc;
    }

    public LumDocument create_annot_doc_pos(BytesRef b_doc_uuid) {
        if (!is_pos_tagged) {
            return null;
        }

        long doc_uuid = LumUtils.BytesRefToLong(b_doc_uuid);
        List<String[]> annot_data = TextUtils.extract_pos_annot(annot_text);
        String anno_content = transform_to_annot_format(annot_data);

        LumDocument lum_doc = build_lumDocument(doc_uuid, anno_content);

        return lum_doc;
    }
    
    public LumDocument create_annot_doc_tag(BytesRef b_doc_uuid){
        if (!is_pos_tagged) {
            return null;            
        }
        
        long doc_uuid = LumUtils.BytesRefToLong(b_doc_uuid);
        List<String[]> annot_data = TextUtils.extract_pos_annot(annot_text);
        String anno_content = transform_to_annot_format(annot_data);

        LumDocument lum_doc = build_lumDocument(doc_uuid, anno_content);

        return lum_doc;
    }
    
    private String transform_to_annot_format(List<String[]> annot_data) {
        List<String> annot_in = TextUtils.make_annotation_format(
                pos_map, annot_data);
        return String.join("\n", annot_in);
    }
    
    private LumDocument build_lumDocument(long doc_uuid, String anno_content) {
        LumDocument lum_doc = new LumDocument();
        lum_doc.SetDocType(LumDocument.ANNO);
        lum_doc.SetBaseRef(Long.toHexString(doc_uuid));
        lum_doc.SetContent(anno_content);
        
        return lum_doc;
    }
}
