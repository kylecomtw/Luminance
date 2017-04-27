/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import tw.com.kyle.luminance.FieldTypeFactory.FTEnum;

/**
 *
 * @author Sean_S325
 */
public class LumIndexer {
    private IndexWriter idx_writer = null;
    private IndexReader idx_reader = null;
    private String index_dir = "";
    public static final String DOC_DISCOURSE = "discourse";
    public static final String DOC_FRAGMENT = "fragment";
    public static final String DOC_ANNOTATION = "annotation";
    
    public static void CleanIndex(String index_dir) throws IOException {        
        if (!Files.exists(Paths.get(index_dir))) return;
        DirectoryStream<Path> stream = Files.newDirectoryStream(
                                        Paths.get(index_dir));
        for(Path path: stream){
            Files.delete(path);
        }
    }  
    
    public String GetIndexDir() {return index_dir;}
    
    public IndexReader GetReader() throws IOException {
        if (idx_reader == null){            
            idx_reader = DirectoryReader.open(idx_writer);
        }
        return idx_reader;
    }
    
    public LumIndexer(String indir) throws IOException {
        index_dir = indir;
        Directory index = FSDirectory.open(Paths.get(index_dir));
        PerFieldAnalyzerWrapper wrapper = LuminanceAnalyzerWrapper.Get();
        IndexWriterConfig config = new IndexWriterConfig(wrapper);  
        idx_writer = new IndexWriter(index, config);
    }
            
    public void reset() throws IOException {
        if (idx_writer.isOpen()) {                    
            close();
        }
        idx_writer = null;
        Directory index = FSDirectory.open(Paths.get(index_dir));
        PerFieldAnalyzerWrapper wrapper = LuminanceAnalyzerWrapper.Get();
        IndexWriterConfig config = new IndexWriterConfig(wrapper);  
        idx_writer = new IndexWriter(index, config);
    }
    
    public void flush() throws IOException {
        idx_writer.flush();
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
        long timestamp = System.nanoTime();
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(timestamp);
        idx_doc.add(new Field("uuid", buf.array(), FieldTypeFactory.Get(FTEnum.RawStoredIndex)));
        idx_doc.add(new Field("class", lum_doc.GetDocClass(), FieldTypeFactory.Get(FTEnum.SimpleIndex)));
        idx_doc.add(new Field("type", lum_doc.GetDocType(), FieldTypeFactory.Get(FTEnum.RawIndex)));
        idx_doc.add(new Field("timestamp", lum_doc.GetTimestamp(), FieldTypeFactory.Get(FTEnum.RawIndex)));
        if (lum_doc.GetBaseRef().length() > 0){
            idx_doc.add(new Field("baseref", lum_doc.GetBaseRef(), FieldTypeFactory.Get(FTEnum.RawStoredIndex)));
        }
        
        if(lum_doc.GetDocType().equals(LumDocument.ANNO)){
            idx_doc.add(new Field("anno", lum_doc.GetContent(), FieldTypeFactory.Get(FTEnum.FullIndex)));
        } else {
            idx_doc.add(new Field("content", lum_doc.GetContent(), FieldTypeFactory.Get(FTEnum.FullIndex)));
        }
        
        idx_writer.addDocument(idx_doc);
        return timestamp;
    }
    
    public Document CreateIndexDocument(String doc_type) {
        Document idx_doc = new Document();
        long timestamp = System.nanoTime();
        ByteBuffer buf = ByteBuffer.allocate(8);
        buf.putLong(timestamp);
        idx_doc.add(new Field("uuid", buf.array(), FieldTypeFactory.Get(FTEnum.RawStoredIndex)));        
        idx_doc.add(new Field("class", doc_type, FieldTypeFactory.Get(FTEnum.RawStoredIndex)));        
        return idx_doc;
    }
    
    public long GetUUID(Document doc) {
        BytesRef uuid = doc.getBinaryValue("uuid");
        return LumUtils.BytesRefToLong(uuid);
    }
    
    public BytesRef GetUUIDAsBytesRef(Document doc) {
        BytesRef uuid = doc.getBinaryValue("uuid");
        return uuid;
    }
    
    public void AddField(Document idx_doc, String field_name, String content, FieldType ftype){
        idx_doc.add(new Field(field_name, content, ftype));        
    }
    
    public void AddField(Document idx_doc, String field_name, BytesRef bref, FieldType ftype){
        idx_doc.add(new Field(field_name, bref, ftype));        
    }
    
    public void AddToIndex(Document idx_doc) throws IOException {
        idx_writer.addDocument(idx_doc);
    }
    
    public void close() throws IOException {
        idx_writer.close();
    }

            
            
}
