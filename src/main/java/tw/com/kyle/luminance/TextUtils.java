/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sean_S325
 */
public class TextUtils {
    private static Pattern pos_pat = Pattern.compile("\\(\\w*\\)");
    public static boolean is_segmented(String intxt) {
        if(intxt.split("\u3000").length > 2){
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean is_pos_tagged(String intxt){
        Matcher m = pos_pat.matcher(intxt);
        return m.find();            
    }
    
    public static String normalize(String intxt) {        
        String new_txt = intxt.replace(")", "）");
        new_txt = new_txt.replace("(", "（");
        
        return new_txt;
    }
    
    public static String extract_raw_text(String annots){
        StringBuilder buf = new StringBuilder();
        int offset_counter = 0;
        boolean ignore_ch = false;
        for(int i = 0; i < annots.length(); i++){
            String ch = annots.substring(i, i+1);            
            if (ch.equals("(")){
                ignore_ch = true;
            } else if (ch.equals(")")){
                ignore_ch = false;
            } else if (ch.equals("\u3000")) {
                //! pass                
            } else if (ch.equals("\r") || ch.equals("\n") || ch.equals("\t") || ch.equals(" ")) {
                //! pass
            } else {
                if(ignore_ch) continue;
                buf.append(ch);                
            }
        }
                        
        return buf.toString();
    }
    
    public static List<String[]> extract_seg_annot(String intxt) {
        List<String[]> annot_list = new ArrayList<>();        
        String buf = "";
        int offset_counter = 0;
        boolean ignore_ch = false;
        for(int i = 0; i < intxt.length(); i++){
            String ch = intxt.substring(i, i+1);            
            if (ch.equals("(")){
                ignore_ch = true;
            } else if (ch.equals(")")){
                ignore_ch = false;
            } else if (ch.equals("\u3000")) {
                annot_list.add(new String[]{buf, buf});                
                buf = "";
            } else if (ch.equals("\r") || ch.equals("\n") || ch.equals("\t") || ch.equals(" ")) {
                //! pass
            } else {
                if(ignore_ch) continue;
                buf += ch;
                offset_counter += 1;
            }
        }
        
        if(buf.length() > 0){
            annot_list.add(new String[]{buf, ""}); 
            buf = "";
        }
        
        return annot_list;
    }
    
    public static List<String[]> extract_pos_annot(String intxt) {
        List<String[]> annot_list = new ArrayList<>();        
        String tag_buf = "";
        String ch_buf = "";
        boolean in_tag = false;
        for(int i = 0; i < intxt.length(); i++){
            String ch = intxt.substring(i, i+1);            
            if (ch.equals("(")){                            
                in_tag = true;
            } else if (ch.equals(")")){
                annot_list.add(new String[]{ch_buf, tag_buf});                 
                ch_buf = ""; tag_buf = "";
                in_tag = false;
            } else if (ch.equals("\u3000")) {                
                // pass
            } else if (ch.equals("\r") || ch.equals("\n") || ch.equals("\t") || ch.equals(" ")) {
                //! pass
            } else {
                if(in_tag){
                    tag_buf += ch;
                } else {
                    ch_buf += ch;
                }                
            }
        }
        
        if(tag_buf.length() > 0){
            annot_list.add(new String[]{ch_buf, tag_buf});  
            tag_buf = "";
            ch_buf = "";
        }
        
        return annot_list;
    }
    
    public static List<String> make_annotation_format(LumPositionMap pos_map, List<String[]> annot_list) {
        List<String> atxt = new ArrayList<>();
        
        int last_pos = 0;
        for(int i = 0; i < annot_list.size(); i++){
            String term = annot_list.get(i)[0];
            String tag = annot_list.get(i)[1];
            int pos = pos_map.FindPosition(term.substring(0, 1), last_pos);
            if(pos < 0) continue;
            
            atxt.add(String.format("(%s, %d, %d)", tag, pos, pos+term.length()));
        }
        return atxt;
    }
}
