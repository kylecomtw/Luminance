/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author Sean_S325
 */
public class MainApp {
    public static void main(String[] args) {
        try{
            Map<String, String> props = PropLoader.Load();            
            if (args.length == 0) {                
                index(props);
                query(props);
                return;                
            }
            
            
            switch (args[0]) {
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
        
        LumIndexer lum_indexer = new LumIndexer(props.get("index_dir"));
        DirectoryStream<Path> stream = Files.newDirectoryStream(
                                        Paths.get(props.get("text_dir")), "*.txt");
        for(Path path: stream) {
            System.out.println(path.getFileName());
            List<String> lines = Files.readAllLines(path, Charset.forName("utf-8"));            
            lum_indexer.index_text(String.join("", lines));         
        }   
        
        lum_indexer.close();
    }
    
    private static void query(Map<String, String> props) throws IOException, ParseException {
        LumQuery lum_query = new LumQuery(props.get("index_dir"));
        // lum_query.query("三國");
        lum_query.span_query("水鏡");
        // System.out.println(lum_query.getTermFreq("學"));
    }

}
