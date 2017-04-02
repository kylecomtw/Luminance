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
import org.apache.lucene.queryparser.classic.ParseException;
import tw.com.kyle.luminance.corpus.PttJsonAdaptor;

/**
 *
 * @author Sean_S325
 */
public class MainApp {
    public static void main(String[] args) {
        try{            
            Map<String, String> props = PropLoader.Load();            
            clear(props);
            if (args.length == 0) {                
                import_corpus(props);
                // index(props);
                // query(props);                
                return;                
            }
            
            
            switch (args[0]) {
                case "import":
                    import_corpus(props);
                    break;
                case "index":
                    index(props);
                    break;
                case "query":
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
    
    private static void import_corpus(Map<String, String> props) throws IOException {
        final String CORPUS_PATH = "E:\\Kyle\\Corpus\\PTT\\data\\ask";
        if (!Files.exists(Paths.get(CORPUS_PATH))) return;
        DirectoryStream<Path> stream = Files.newDirectoryStream(
                                        Paths.get(CORPUS_PATH));
        PttJsonAdaptor ptt_adaptor = new PttJsonAdaptor();
        for(Path path: stream){
            ptt_adaptor.Parse(path.toString());
        }
        
    }

}
