/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Sean_S325
 */
public class AnnotationProvider {
    
    private long anno_ref_uuid = -1;
    private LumPositionMap pos_map = null;
    private List<LumDocument> doc_list = new ArrayList<>();
    
    private Logger logger = Logger.getLogger(AnnotationProvider.class.getName());
    
    public AnnotationProvider(String inputs) {
        try {
            
            if (inputs.substring(0, Math.min(100, inputs.length()))
                      .replace(" ", "").contains("#base_ref:")) {
                //! this is a anno doc format
                LumDocument lum_doc = LumDocumentAdapter.FromText(inputs);
                pos_map = LumPositionMap.Get(TextUtils.extract_raw_text(lum_doc.GetContent()));
                List<String[]> anno_data = null;
                if (lum_doc.GetAnnoType().equals(LumDocument.ANNO_SEG)){
                    anno_data = TextUtils.extract_seg_annot(lum_doc.GetContent()); 
                } else {
                    anno_data = TextUtils.extract_pos_annot(lum_doc.GetContent());                    
                }
                String anno_content = transform_to_annot_format(anno_data);
                lum_doc.SetContent(anno_content);
                doc_list.add(lum_doc);
                
            } else {
                //! it is a plain text, extract all informations we can
                pos_map = LumPositionMap.Get(TextUtils.extract_raw_text(inputs));
                build_anno_doc_list(inputs);
            }
        } catch (IOException ex) {
            logger.severe(ex.toString());
            doc_list.add(new LumDocument());
        }
        
    }
        
    public long GetRefUuid() { 
        if (doc_list.get(0).GetDocClass().equals(LumDocument.DISCOURSE)) {
            return doc_list.get(0).GetUuid();
        } else {
            return doc_list.get(0).GetBaseRef();
        }
    }
    
    public void SetBaseTimestamp(String timestamp) {
        doc_list.get(0).SetTimestamp(timestamp);
    }
    
    public void AddLumDocument(LumDocument doc){
        doc_list.add(doc);
    }
    
    private void build_anno_doc_list(String inputs) {
        LumDocument base_doc = create_discourse_doc(inputs);
        doc_list.add(base_doc);
        long ref_uuid = base_doc.GetUuid();
        if (TextUtils.is_segmented(inputs)) {
            doc_list.add(create_annot_doc_seg(ref_uuid, inputs));
        }
        
        if (TextUtils.is_pos_tagged(inputs)) {
            doc_list.add(create_annot_doc_pos(ref_uuid, inputs));
        }
    }
    
    public LumDocument create_discourse_doc(String inputs) {
        LumDocument lum_doc = new LumDocument();
        lum_doc.SetDocClass(LumDocument.DISCOURSE);
        lum_doc.SetContent(TextUtils.extract_raw_text(inputs));
        return lum_doc;
    }
    
    public List<LumDocument> IndexableDocuments() {
        return doc_list;
    }
    
    public void AddSupplementData(String key, String value) {
        LumDocument doc = doc_list.get(0);
        doc.AddSuppData(key, value);
    }
    
    public void Index(LumIndexer indexer) throws IOException {
        for(LumDocument doc: doc_list){
            indexer.index_doc(doc);
        }
    }
    
    private LumDocument create_annot_doc_seg(long ref_uuid, String inputs) {        
        List<String[]> annot_data = TextUtils.extract_seg_annot(inputs);
        String anno_content = transform_to_annot_format(annot_data);
        
        LumDocument lum_doc = build_lumDocument(ref_uuid, anno_content);
        lum_doc.SetAnnoName("anno-seg-extracted");
        lum_doc.SetAnnoType("seg");
        return lum_doc;
    }
    
    private LumDocument create_annot_doc_pos(long ref_uuid, String inputs) {
        List<String[]> annot_data = TextUtils.extract_pos_annot(inputs);
        String anno_content = transform_to_annot_format(annot_data);
        
        LumDocument lum_doc = build_lumDocument(ref_uuid, anno_content);
        lum_doc.SetAnnoName("anno-pos-extracted");
        lum_doc.SetAnnoType("pos");
        return lum_doc;
    }
    
    private LumDocument create_annot_doc_tag(long ref_uuid, String inputs) {
        List<String[]> annot_data = TextUtils.extract_pos_annot(inputs);
        String anno_content = transform_to_annot_format(annot_data);
        
        LumDocument lum_doc = build_lumDocument(ref_uuid, anno_content);        
        lum_doc.SetAnnoType("tag");
        return lum_doc;
    }
    
    private String transform_to_annot_format(List<String[]> annot_data) {
        List<String> annot_in = TextUtils.make_annotation_format(
                pos_map, annot_data);
        return String.join("\n", annot_in);
    }
    
    private LumDocument build_lumDocument(long doc_uuid, String anno_content) {
        LumDocument lum_doc = new LumDocument();
        lum_doc.SetDocClass(LumDocument.ANNO);        
        lum_doc.SetBaseRef(doc_uuid);
        lum_doc.SetContent(anno_content);
        
        return lum_doc;
    }
}
