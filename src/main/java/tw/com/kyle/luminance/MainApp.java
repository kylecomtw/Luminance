/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import tw.com.kyle.luminance.corpus.LumIndexInterface;
import tw.com.kyle.luminance.corpus.PttJsonAdaptor;
import tw.com.kyle.luminance.corpus.compute.CollocateFromIndex;
import tw.com.kyle.luminance.corpus.compute.ExpNetwork;

/**
 *
 * @author Sean_S325
 */
public class MainApp {
    public static void main(String[] args) {
        try{            
            Map<String, String> props = PropLoader.Load();                        
            if (args.length == 0) {                
                // import_corpus(props);
                // analyze(props);   
                build_networks(props);
                // index(props);
                // query(props);                
                // clear(props);
                return;                
            }
            
            
            switch (args[0]) {
                case "analyze":
                    analyze(props);
                    break;
                case "import":
                    import_corpus(props);
                    break;
                case "index_test":
                    index(props);
                    break;
                case "query_test":
                    query(props);
                    break;
                default:
                    break;
            }
        } catch (IOException | ParseException ex){
            System.out.println(ex);
        }        
    }
    
    private static void index(Map<String, String> props) throws IOException{
        Luminance lum = new Luminance();    
        DirectoryStream<Path> stream = Files.newDirectoryStream(
                                        Paths.get(props.get("text_dir")), "*.txt");
        for(Path path: stream) {
            System.out.println(path.getFileName());            
            lum.index(path, props);
        }                           
    }
    
    private static void query(Map<String, String> props) throws IOException, ParseException {
        LumQuery lum_query = new LumQuery(props.get("index_dir"));
        // lum_query.query("三國");
        lum_query.span_query(props.get("query_str"), "anno", false);
        // System.out.println(lum_query.getTermFreq("學"));
    }
    
    private static void clear(Map<String, String> props) throws IOException {
        if (!Files.exists(Paths.get(props.get("index_dir")))) return;
        DirectoryStream<Path> stream = Files.newDirectoryStream(
                                        Paths.get(props.get("index_dir")));
        for(Path path: stream){
            Files.delete(path);
        }
        
        // Files.delete(Paths.get(props.get("index_dir")));
    }  
    
    private static void import_corpus(Map<String, String> props) 
            throws IOException {
        final String CORPUS_PATH = "E:\\Kyle\\Corpus\\PTT\\data\\FuMouDiscuss";
        LumIndexInterface adaptor = new PttJsonAdaptor();
        // final String CORPUS_PATH = "E:\\Kyle\\Corpus\\ASBC\\ASBC_A";
        // AsbcXmlAdaptor adaptor = new AsbcXmlAdaptor();
        // final String CORPUS_PATH = "E:\\Study\\16_Idioms\\20_Materials\\Apple_text";
        // AppleLineDelimAdaptor adaptor = new AppleLineDelimAdaptor();
        if (!Files.exists(Paths.get(CORPUS_PATH))) return;
        DirectoryStream<Path> stream = Files.newDirectoryStream(
                                        Paths.get(CORPUS_PATH));
        
        LumIndexer indexer = new LumIndexer(props.get("index_dir"));
        for(Path path: stream){                      
            adaptor.Index(indexer, path.toString());
            // break;
        }
        indexer.close();
    }
    
    private static void analyze(Map<String, String> props) throws IOException {
        final String INDEX_DIR = "h:/lum_index_fumou";
        Directory index = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader idx_reader = DirectoryReader.open(index);        
        CollocateFromIndex col = new CollocateFromIndex(idx_reader);        
        col.GetAllMwe("content");
        col.WriteJson("h:/mwe_fumou.json");
    }
    
    private static void build_networks(Map<String, String> props) throws IOException {
        final String INDEX_DIR = "h:/lum_index_fumou";
        final String MWE_JSON = "E:\\Kyle\\LocalData\\mwe_fumou_4-10.json";
        Directory index = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader idx_reader = DirectoryReader.open(index);        
        ExpNetwork exp_net = new ExpNetwork(idx_reader);
        exp_net.AddNodes(ExpNetwork.LoadMweFromJson(MWE_JSON));
        exp_net.ComputeEdges();        
        exp_net.WriteJson("h:/net_fumou.json");
    }

}
