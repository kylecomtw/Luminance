/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sean_S325
 */
public class LumDocumentChain {
    private String base_ref = "";
    public LumDocumentChain(){}
    public LumDocumentChain(String base_id) {
        base_ref = base_id;
    }
    
    private List<LumDocument> doc_list = new ArrayList<>();
    public static LumDocumentChain FromAnnotated(String annots){
        LumDocumentChain chain = new LumDocumentChain();        
        chain.add_text(annots);
        if (TextUtils.is_segmented(annots)) chain.add_segmentation(annots);
        if (TextUtils.is_pos_tagged(annots)) chain.add_pos_tag(annots);
        return chain;
    }
    
    public void add_text(String annots){
        LumDocument lum_doc = new LumDocument();
        lum_doc.SetDocType(LumDocument.DISCOURSE);
        lum_doc.SetContent(TextUtils.extract_raw_text(annots));
        doc_list.add(lum_doc);
    }
    
    public void add_segmentation(String atxt){
        LumDocument lum_doc = new LumDocument();
        lum_doc.SetDocType(LumDocument.ANNO);
        lum_doc.SetContent(String.join("\n", TextUtils.extract_seg_annot(atxt)));
        doc_list.add(lum_doc);
    }
            
    public void add_pos_tag(String atxt){
        LumDocument lum_doc = new LumDocument();
        lum_doc.SetDocType(LumDocument.ANNO);
        lum_doc.SetContent(String.join("\n", TextUtils.extract_pos_annot(atxt)));
        doc_list.add(lum_doc);
    }
    
    public void commit_chain(LumIndexer indexer) {
        String prev_doc_id = "";
        for(LumDocument doc: doc_list){
            doc.SetBaseRef(prev_doc_id);
            try {
                prev_doc_id = Long.toString(indexer.index_doc(doc));
            } catch (IOException ex) {
                Logger.getLogger(LumDocumentChain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }               
    }
}
