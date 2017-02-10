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
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author Sean_S325
 */
public class MainApp {
    public static void main(String[] args) throws IOException, ParseException {
        
        LumIndexer lum_indexer = new LumIndexer("h:/luc_index");
        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("h:/JinYong"), "*.txt");
        for(Path path: stream) {
            System.out.println(path.getFileName());
            List<String> lines = Files.readAllLines(path, Charset.forName("cp950"));
            lines = lines.subList(0, Math.min(10, lines.size()-1));
            // lum_indexer.index_text(String.join("", lines));         
        }   
        
        lum_indexer.close();
        
        LumQuery lum_query = new LumQuery("h:/luc_index");
        lum_query.query("金庸");
        lum_query.span_query("金");
        System.out.println(lum_query.getTermFreq("金"));
    }

}
