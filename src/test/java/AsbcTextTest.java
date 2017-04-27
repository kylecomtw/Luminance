
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;
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
        assertThat(doc_list.size(), greaterThan(0));
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
