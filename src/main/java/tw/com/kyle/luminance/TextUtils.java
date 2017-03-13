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
        return m.matches();            
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
    
    public static List<String> extract_seg_annot(String intxt) {
        List<String> annot_list = new ArrayList<>();        
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
                annot_list.add(String.format("(%d, %d, %s)", offset_counter - buf.length(), offset_counter, buf));
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
            annot_list.add(String.format("(%d, %d, %s)", offset_counter - buf.length(), offset_counter, buf));
            buf = "";
        }
        
        return annot_list;
    }
    
    public static List<String> extract_pos_annot(String intxt) {
        List<String> annot_list = new ArrayList<>();        
        String buf = "";
        int offset_counter = 0;
        boolean in_tag = false;
        for(int i = 0; i < intxt.length(); i++){
            String ch = intxt.substring(i, i+1);            
            if (ch.equals("(")){
                in_tag = true;
            } else if (ch.equals(")")){
                annot_list.add(String.format("(%d, %d, %s)", offset_counter - 1, offset_counter, buf));
                in_tag = false;
            } else if (ch.equals("\u3000")) {
                offset_counter += 1;
            } else if (ch.equals("\r") || ch.equals("\n") || ch.equals("\t") || ch.equals(" ")) {
                //! pass
            } else {
                if(in_tag){
                    buf += ch;
                } else {
                    // pass
                }
            }
        }
        
        if(buf.length() > 0){
            annot_list.add(String.format("(%d, %d, %s)", offset_counter - 1, offset_counter, buf));
            buf = "";
        }
        
        return annot_list;
    }
}
