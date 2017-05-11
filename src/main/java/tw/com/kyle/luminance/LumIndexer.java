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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.DateTools;
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
    
    public IndexReader GetReaderOnly() throws IOException {
        Directory index = FSDirectory.open(Paths.get(index_dir));
        return DirectoryReader.open(index);
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
    
    public void open() throws IOException {
        if (!idx_writer.isOpen()) {                    
            reset();
        }
    }
    
    public void index_text(String text_content) throws IOException {
        Document doc = new Document();
        FieldType ftype = FieldTypeFactory.Get(FTEnum.FullIndex);
        doc.add(new Field("content", text_content, ftype)); 
        idx_writer.addDocument(doc);
    }
    
    public long index_doc(LumDocument lum_doc) throws IOException {
        Document idx_doc = new Document();     
        BytesRef uuid_bytes = LumUtils.LongToBytesRef(lum_doc.GetUuid());
        idx_doc.add(new Field("uuid", uuid_bytes, FieldTypeFactory.Get(FTEnum.RawStoredIndex)));
        idx_doc.add(new Field("class", lum_doc.GetDocClass(), FieldTypeFactory.Get(FTEnum.RawStoredIndex)));                
        idx_doc.add(new Field("mode", lum_doc.GetDocMode(), FieldTypeFactory.Get(FTEnum.RawStoredIndex)));                
        idx_doc.add(new Field("timestamp", new BytesRef(lucene_date_format(lum_doc.GetTimestamp())), FieldTypeFactory.Get(FTEnum.TimestampIndex)));
        idx_doc.add(new Field("timestamp", lum_doc.GetTimestamp(), FieldTypeFactory.Get(FTEnum.RawIndex)));
        if (lum_doc.GetBaseRef() > 0){
            BytesRef base_ref_bytes = LumUtils.LongToBytesRef(lum_doc.GetBaseRef());
            idx_doc.add(new Field("base_ref", base_ref_bytes, FieldTypeFactory.Get(FTEnum.RawStoredIndex)));
        }
        
        if(lum_doc.GetDocClass().equals(LumDocument.ANNO)){
            idx_doc.add(new Field("anno", lum_doc.GetContent(), FieldTypeFactory.Get(FTEnum.FullIndex)));
            idx_doc.add(new Field("anno_name", lum_doc.GetAnnoName(), FieldTypeFactory.Get(FTEnum.RawStoredIndex)));
            idx_doc.add(new Field("anno_type", lum_doc.GetAnnoType(), FieldTypeFactory.Get(FTEnum.RawStoredIndex)));
        } else {
            idx_doc.add(new Field("content", lum_doc.GetContent(), FieldTypeFactory.Get(FTEnum.FullIndex)));
        }
        
        for(String supp_key: lum_doc.GetSuppDataKey()){
            idx_doc.add(new Field(supp_key, 
                    lum_doc.GetSuppData(supp_key), 
                    FieldTypeFactory.Get(FTEnum.RawStoredIndex)));
        }
        
        idx_writer.addDocument(idx_doc);
        return lum_doc.GetUuid();
    }
    
    public Document CreateIndexDocument_deprecate(String doc_type) {
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
    
    private String lucene_date_format(String timestamp) {                    
        String lum_date_str = null;
        try {
            DateFormat format = new SimpleDateFormat("yyyyMMdd");
            lum_date_str = DateTools.dateToString(
                                    format.parse(timestamp),
                                    DateTools.Resolution.DAY);            
        } catch (ParseException ex) { 
            Calendar cal = Calendar.getInstance();
            cal.set(2010, 1, 1, 0, 0, 0);
            lum_date_str = DateTools.dateToString(
                                    cal.getTime(), DateTools.Resolution.DAY);            
        }
        
        return lum_date_str;                
    }
            
            
}
