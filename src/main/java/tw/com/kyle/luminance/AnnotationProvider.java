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
    
    public boolean has_segmented() {
        return is_segmented;
    }
    
    public boolean has_pos_tagged() {
        return is_pos_tagged;
    }
    
    public boolean has_ner_tagged() {
        return is_ner_tagged;
    }
    
    public AnnotationProvider(String annots){
        is_segmented = TextUtils.is_segmented(annots);
        is_pos_tagged = TextUtils.is_pos_tagged(annots);
        annot_text = annots;
    }
    
    public LumDocument create_discourse_doc(){
        LumDocument lum_doc = new LumDocument();
        lum_doc.SetDocType(LumDocument.DISCOURSE);
        lum_doc.SetContent(TextUtils.extract_raw_text(annot_text));
        return lum_doc;
    }               
    
    public LumDocument create_annot_doc_seg(BytesRef b_doc_uuid){
        if (!is_segmented) return null;
        
        LumPositionMap pos_map = null;        
        long doc_uuid = LumUtils.BytesRefToLong(b_doc_uuid);
        try{                        
            pos_map = LumPositionMap.Get(TextUtils.extract_raw_text(annot_text));
        } catch (IOException ex){
            Logger.getLogger(AnnotationProvider.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        List<String> annot_in = TextUtils.make_annotation_format(
                pos_map,
                TextUtils.extract_seg_annot(annot_text));
        
        LumDocument lum_doc = new LumDocument();        
        lum_doc.SetDocType(LumDocument.ANNO);  
        lum_doc.SetBaseRef(Long.toHexString(doc_uuid));
        lum_doc.SetContent(String.join("\n", annot_in));
        
        return lum_doc;
    }
            
    public LumDocument create_annot_doc_pos(BytesRef b_doc_uuid){
        if (!is_pos_tagged) return null;
        
        LumPositionMap pos_map = null;
        long doc_uuid = LumUtils.BytesRefToLong(b_doc_uuid);
        try{            
            pos_map = LumPositionMap.Get(TextUtils.extract_raw_text(annot_text));
        } catch (IOException ex){
            Logger.getLogger(AnnotationProvider.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        List<String> annot_in = TextUtils.make_annotation_format(
                    pos_map,
                    TextUtils.extract_pos_annot(annot_text));
                        
        LumDocument lum_doc = new LumDocument();
        lum_doc.SetDocType(LumDocument.ANNO);
        lum_doc.SetBaseRef(Long.toHexString(doc_uuid));
        lum_doc.SetContent(String.join("\n", annot_in));
        
        return lum_doc;
    }    
}
