/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Map;

/**
 * Main interface of project Luminance
 * @author Sean
 */
public class Luminance {
    public void index(Path path, Map<String, String> props) throws IOException {
        
        FileInputStream reader = new FileInputStream(path.toString());
        byte[] buf = new byte[1024 * 10];
        reader.read(buf);
        reader.close();
        
        String strbuf = new String(buf, "utf-8");
        if (TextUtils.is_segmented(strbuf) || TextUtils.is_pos_tagged(strbuf)) {
            index_annotated(path, props);
        } else {
            index_content(path, props);
        }
    }
    
    public void index_annotated(Path path, Map<String, String> props) throws IOException{
        LumIndexer lum_indexer = new LumIndexer(props.get("index_dir"));
        FileInputStream in = new FileInputStream(path.toString());
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));        
        LumDocument lum_doc = LumDocumentAdapter.FromReader(br);
        LumDocumentChain chain = LumDocumentChain.FromAnnotated(lum_doc.GetContent());
        chain.commit_chain(lum_indexer);
        lum_indexer.close();
    }
    
    public void index_content(Path path, Map<String, String> props) throws IOException {
        LumIndexer lum_indexer = new LumIndexer(props.get("index_dir"));
        FileInputStream in = new FileInputStream(path.toString());
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "utf-8"));
        lum_indexer.index_doc(LumDocumentAdapter.FromReader(br));
        lum_indexer.close();
        in.close();
    }
            
}
