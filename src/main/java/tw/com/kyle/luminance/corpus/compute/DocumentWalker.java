/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.corpus.compute;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.util.Bits;

/**
 *
 * @author Sean
 */
public class DocumentWalker {
    private IndexReader reader = null;
    private Logger logger = Logger.getLogger(DocumentWalker.class.getName());
    private Bits liveDocs = null;
    private int doc_cursor = 0;
    private int doc_idx = 0;
    private int maxDoc = 0;
    private List<Integer> doc_id_list = null;
    
    public int length() {return maxDoc;}
    public int DocId() {return doc_idx;}
    public int DocStep() {return doc_cursor;}
    public DocumentWalker(IndexReader r) {
        reader = r;
        maxDoc = reader.maxDoc();
        liveDocs = MultiFields.getLiveDocs(reader);
        doc_cursor = -1;
        doc_id_list = generate_random_permute();
    }        
    
    public Boolean Walk() throws IOException {            
        doc_cursor += 1;
        while(doc_cursor < maxDoc){            
            doc_idx = doc_id_list.get(doc_cursor);
            if (liveDocs != null && !liveDocs.get(doc_idx)){                
                // document is deleted
                doc_cursor += 1;                
            } else {                
                return true;                
            }            
        }
        
        return false;
    }
    
    public String GetContent() throws IOException {                                        
        Document doc_x = reader.document(doc_idx);
        String content_x = doc_x.get("content");

        return content_x;
    }
    
    private List<Integer> generate_random_permute(){
        List<Integer> id_list = IntStream.range(0, maxDoc).boxed().collect(Collectors.toList());
        Collections.shuffle(id_list);
        return id_list;
        
    }
}
