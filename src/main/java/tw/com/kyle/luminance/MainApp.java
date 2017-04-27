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
import tw.com.kyle.luminance.corpus.compute.CorpusStat;
import tw.com.kyle.luminance.corpus.compute.ExpNetwork;

/**
 *
 * @author Sean_S325
 */
public class MainApp {
    final static String CORPUS_PATH = "E:\\Kyle\\Corpus\\PTT\\data\\FuMouDiscuss";  
    final static String INDEX_DIR = "h:/lum_index_fumou";
    // final String CORPUS_PATH = "E:\\Kyle\\Corpus\\ASBC\\ASBC_A";        
    // final String CORPUS_PATH = "E:\\Study\\16_Idioms\\20_Materials\\Apple_text";        
    public static void main(String[] args) {
        try{            
            Map<String, String> props = PropLoader.Load();                        
            if (args.length == 0) {                
                // import_corpus(props);
                // summarize_stat(props);
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
                    break;
                case "query_test":                    
                    break;
                default:
                    break;
            }
        } catch (IOException ex){
            System.out.println(ex);
        }        
    }
    
    private static void clear(Map<String, String> props) throws IOException {
        clear(props.get("index_dir"));
    }  
    
    private static void clear(String index_dir) throws IOException {
        if (!Files.exists(Paths.get(index_dir))) return;
        DirectoryStream<Path> stream = Files.newDirectoryStream(
                                        Paths.get(index_dir));
        for(Path path: stream){
            Files.delete(path);
        }
        
        // Files.delete(Paths.get(props.get("index_dir")));
    }  
    
    private static void import_corpus(Map<String, String> props) 
            throws IOException {        
        LumIndexInterface adaptor = new PttJsonAdaptor();
        // final String CORPUS_PATH = "E:\\Kyle\\Corpus\\ASBC\\ASBC_A";
        // AsbcXmlAdaptor adaptor = new AsbcXmlAdaptor();
        // final String CORPUS_PATH = "E:\\Study\\16_Idioms\\20_Materials\\Apple_text";
        // AppleLineDelimAdaptor adaptor = new AppleLineDelimAdaptor();
        if (!Files.exists(Paths.get(CORPUS_PATH))) return;
        DirectoryStream<Path> stream = Files.newDirectoryStream(
                                        Paths.get(CORPUS_PATH));
        
        LumIndexer indexer = new LumIndexer(INDEX_DIR);
        for(Path path: stream){                      
            adaptor.Index(indexer, path.toString());
            // break;
        }
        indexer.close();
    }
    
    private static void analyze(Map<String, String> props) throws IOException {        
        Directory index = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader idx_reader = DirectoryReader.open(index);        
        CollocateFromIndex col = new CollocateFromIndex(idx_reader);        
        col.GetAllMwe("content");        
        col.WriteJson("h:/mwe_fumou.json");
    }
    
    private static void build_networks(Map<String, String> props) throws IOException {
        final String MWE_JSON = "E:\\Kyle\\LocalData\\mwe_fumou_4-10_edit.json";
        Directory index = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader idx_reader = DirectoryReader.open(index);        
        ExpNetwork exp_net = new ExpNetwork(idx_reader);
        exp_net.SetTimeRange("20140320", "20140502", 5);        
        exp_net.AddNodes(ExpNetwork.LoadMweFromJson(MWE_JSON));
        exp_net.KeepNodes(200);
        exp_net.ComputeEdges(50);        
        exp_net.WriteJson("h:/net_fumou.json");
    }
    
    private static void summarize_stat(Map<String, String> props) throws IOException  {
        final String MWE_JSON = "E:\\Kyle\\LocalData\\mwe_fumou_4-10.json";
        Directory index = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader idx_reader = DirectoryReader.open(index);        
        CorpusStat stat = new CorpusStat(idx_reader);
        stat.SetTimeRange("20140320", "20140502", 5);
        stat.Summarize();
        stat.WriteJson("h:/stat_fumou.json");
    }

}
