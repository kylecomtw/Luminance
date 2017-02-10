/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Sean_S325
 */
public class LumIndexer {
    private IndexWriter idx_writer = null;
    
    public LumIndexer(String index_dir) throws IOException {
        Directory index = FSDirectory.open(Paths.get(index_dir));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);        
        idx_writer = new IndexWriter(index, config);
    }
    
    public void index_text(String text_content) throws IOException {
        Document doc = new Document();
        FieldType ftype = new FieldType();        
        ftype.setStored(true);
        ftype.setTokenized(true);
        ftype.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        ftype.setStoreTermVectors(true);
        ftype.setStoreTermVectorOffsets(true);
        ftype.setStoreTermVectorPositions(true);
        doc.add(new Field("content", text_content, ftype));        
        idx_writer.addDocument(doc);
    }
    
    public void close() throws IOException {
        idx_writer.close();
    }
            
            
}
