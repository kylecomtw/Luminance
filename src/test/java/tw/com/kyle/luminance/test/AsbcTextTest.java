package tw.com.kyle.luminance.test;


import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import tw.com.kyle.luminance.LumIndexer;
import tw.com.kyle.luminance.corpus.AsbcXmlAdaptor;
import tw.com.kyle.luminance.corpus.AsbcXmlAdaptor.AsbcDocument;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sean_S325
 */
public class AsbcTextTest {
    @Test
    public void testAsbcParse() throws IOException{
        String asbc_path = "etc/test/asbc.xml";
        AsbcXmlAdaptor adaptor = new AsbcXmlAdaptor();
        List<AsbcDocument> doc_list = adaptor.Parse(asbc_path);        
        assertTrue(doc_list.size() > 0);
    }
    
    @Test
    public void testAsbcIndex() throws IOException {
        String asbc_path = "etc/test/asbc.xml";
        String INDEX_DIR = "h:/index_test";
        LumIndexer.CleanIndex(INDEX_DIR);
        LumIndexer indexer = new LumIndexer(INDEX_DIR);
        AsbcXmlAdaptor adaptor = new AsbcXmlAdaptor();                
        adaptor.Index(indexer, asbc_path);                  
        indexer.close();
    }
}
