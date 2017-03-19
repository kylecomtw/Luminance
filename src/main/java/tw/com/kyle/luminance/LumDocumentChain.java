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
public class LumDocumentChain {
    private class AnnotRecord {
        ANNOT annot_type; String annot_text;
        public AnnotRecord(ANNOT a, String s){
            annot_type = a; annot_text = s;
        }
    }
    
    private List<AnnotRecord> doc_queue = null;
    private String base_ref = "";
    private enum ANNOT {RAW, SEG, POS}
    public LumDocumentChain(){}
    public LumDocumentChain(String base_id) {
        base_ref = base_id;
    }
        
    public static LumDocumentChain FromAnnotated(String annots){
        LumDocumentChain chain = new LumDocumentChain();        
        chain.add_annotation(ANNOT.RAW, annots);
        if (TextUtils.is_segmented(annots)) chain.add_annotation(ANNOT.SEG, annots);
        if (TextUtils.is_pos_tagged(annots)) chain.add_annotation(ANNOT.POS, annots);
        return chain;
    }
    
    private void add_annotation(ANNOT annot_type, String annots) {
        if (doc_queue == null) doc_queue = new ArrayList<>();
        doc_queue.add(new AnnotRecord(annot_type, annots));
    }
    
    public LumDocument add_text(String annots){
        LumDocument lum_doc = new LumDocument();
        lum_doc.SetDocType(LumDocument.DISCOURSE);
        lum_doc.SetContent(TextUtils.extract_raw_text(annots));
        return lum_doc;
    }       
    
    public LumDocument add_segmentation(String atxt, IndexReader reader, long doc_uuid, String field){
        
        LumPositionMap pos_map = null;
        try{
            int doc_id = query_doc_id(reader, doc_uuid);
            pos_map = LumPositionMap.Get(reader, doc_id, field);
        } catch (IOException ex){
            Logger.getLogger(LumDocumentChain.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        List<String> annot_in = TextUtils.make_annotation_format(
                pos_map,
                TextUtils.extract_seg_annot(atxt));
        
        LumDocument lum_doc = new LumDocument();        
        lum_doc.SetDocType(LumDocument.ANNO);        
        lum_doc.SetContent(String.join("\n", annot_in));
        
        return lum_doc;
    }
            
    public LumDocument add_pos_tag(String atxt, IndexReader reader, long doc_uuid, String field){
        
        LumPositionMap pos_map = null;
        try{            
            int doc_id = query_doc_id(reader, doc_uuid);
            pos_map = LumPositionMap.Get(reader, doc_id, field);
        } catch (IOException ex){
            Logger.getLogger(LumDocumentChain.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        List<String> annot_in = TextUtils.make_annotation_format(
                    pos_map,
                    TextUtils.extract_pos_annot(atxt));
                        
        LumDocument lum_doc = new LumDocument();
        lum_doc.SetDocType(LumDocument.ANNO);
        lum_doc.SetContent(String.join("\n", annot_in));
        
        return lum_doc;
    }
    
    public void commit_chain(LumIndexer indexer) throws IOException {
        long base_doc_id = -1;
        LumDocument doc = null;
        IndexReader ir = null;
        for(AnnotRecord ar: doc_queue){
            switch(ar.annot_type){
                case RAW:
                    doc = add_text(ar.annot_text);                    
                    base_doc_id = indexer.index_doc(doc);
                    indexer.reset();
                    break;
                case SEG:
                    ir = LumIndexer.GetReader(indexer);
                    doc = add_segmentation(ar.annot_text, ir, base_doc_id, "content");
                    doc.SetBaseRef(Long.toString(base_doc_id));
                    indexer.index_doc(doc);
                    ir.close();
                    break;
                case POS:
                    ir = LumIndexer.GetReader(indexer);
                    doc = add_pos_tag(ar.annot_text, ir, base_doc_id, "content");
                    doc.SetBaseRef(Long.toString(base_doc_id));                    
                    indexer.index_doc(doc);
                    ir.close();
                    break;
                default:
                    break;
            }            
        }      
        indexer.flush();
    }
    
    private int query_doc_id(IndexReader reader, long uuid) throws IOException{
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
}
