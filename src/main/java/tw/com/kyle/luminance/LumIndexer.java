/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import tw.com.kyle.luminance.FieldTypeFactory.FTEnum;

/**
 *
 * @author Sean_S325
 */
public class LumIndexer {
    private IndexWriter idx_writer = null;
    
    public LumIndexer(String index_dir) throws IOException {
        Directory index = FSDirectory.open(Paths.get(index_dir));
        PerFieldAnalyzerWrapper wrapper = LuminanceAnalyzerWrapper.Get();
        IndexWriterConfig config = new IndexWriterConfig(wrapper);  
        idx_writer = new IndexWriter(index, config);
    }
    
    public void index_text(String text_content) throws IOException {
        Document doc = new Document();
        FieldType ftype = FieldTypeFactory.Get(FTEnum.FullIndex);
        doc.add(new Field("content", text_content, ftype)); 
        idx_writer.addDocument(doc);
    }
    
    public long index_doc(LumDocument lum_doc) throws IOException {
        Document idx_doc = new Document();        
        FieldType stored_ft = new FieldType();
        idx_doc.add(new Field("class", lum_doc.GetDocClass(), FieldTypeFactory.Get(FTEnum.SimpleIndex)));
        idx_doc.add(new Field("type", lum_doc.GetDocType(), FieldTypeFactory.Get(FTEnum.RawIndex)));
        idx_doc.add(new Field("timestamp", lum_doc.GetTimestamp(), FieldTypeFactory.Get(FTEnum.RawIndex)));
        if (lum_doc.GetBaseRef().length() > 0){
            idx_doc.add(new Field("baseref", lum_doc.GetBaseRef(), FieldTypeFactory.Get(FTEnum.RawIndex)));
        }
        
        if(lum_doc.GetDocType().equals(LumDocument.ANNO)){
            idx_doc.add(new Field("anno", lum_doc.GetContent(), FieldTypeFactory.Get(FTEnum.FullIndex)));
        } else {
            idx_doc.add(new Field("content", lum_doc.GetContent(), FieldTypeFactory.Get(FTEnum.FullIndex)));
        }
        
        return idx_writer.addDocument(idx_doc);
    }
    
    public void close() throws IOException {
        idx_writer.close();
    }
            
            
}
