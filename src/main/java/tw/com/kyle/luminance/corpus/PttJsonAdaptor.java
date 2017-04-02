/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.corpus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 *
 * @author Sean
 */
public class PttJsonAdaptor {
    public class PttComment {
        public String author;
        public int valence;
        public String comment;
    }
    public PttJsonAdaptor(){}
    
    public void Parse(String injsonpath) throws IOException{        
        String json_content = String.join("\n", Files.readAllLines(
                                Paths.get(injsonpath), StandardCharsets.UTF_8));
        JsonElement root = new JsonParser().parse(json_content);
        JsonArray rarray = root.getAsJsonArray();
        for (JsonElement elem: rarray){
            JsonObject art_x = elem.getAsJsonObject();
            String art_content = art_x.get("content").getAsString();
            String title = art_x.get("title").getAsString();
            String author = art_x.get("author").getAsString();
            String url = art_x.get("URL").getAsString();
            List<String> comments = get_comments_from_json(art_x.getAsJsonObject("comments"));            
        }
        return;
    }
    
    private List<String> get_comments_from_json(JsonObject com_json){
        JsonArray push_arr = com_json.getAsJsonArray("push");
        JsonArray boo_arr = com_json.getAsJsonArray("boo");
        JsonArray arrow_arr = com_json.getAsJsonArray("arrow");
        
        class Extract_comment implements Function<JsonArray, List<PttComment>>{
            private int valence = 0;
            public Extract_comment(int val) {
                valence = val;
            }
            @Override
            public List<PttComment> apply(JsonArray jarr) {
                List<PttComment> com_list = new ArrayList<>();
                for(JsonElement com: push_arr){
                    JsonArray com_arr_x = com.getAsJsonArray();
                    PttComment com_obj = new PttComment();
                    com_obj.valence = valence;
                    com_obj.comment = com_arr_x.get(1).getAsString();
                    com_obj.author = com_arr_x.get(0).getAsString();                    
                    com_list.add(com_obj);     
                }
                return com_list;
            }
        }
                
        List<PttComment> comments = new ArrayList<>();
        comments.addAll(new Extract_comment(1).apply(push_arr));
        comments.addAll(new Extract_comment(-1).apply(boo_arr));
        comments.addAll(new Extract_comment(0).apply(arrow_arr));
        return new ArrayList<String>();
    }
}
