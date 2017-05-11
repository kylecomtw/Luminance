/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        if (is_segmented(annots) || is_pos_tagged(annots)){
            String[] tokens = annots.split("[\u3000\n]");
            Pattern pat = Pattern.compile("(.*?)\\((.*?)\\)");
            String parsed = Arrays.asList(tokens).stream()
                    .map((String x)->{
                        Matcher m = pat.matcher(x);
                        if(!m.find()) return x;
                        else return m.group(1);
                    }).collect(Collectors.joining());
            return parsed;
        } else {
            return annots;
        }
    }
    
    private static List<String[]> parse_annot_text(String intxt) {
        String[] tokens = intxt.split("[\u3000\n]");
        Pattern pat = Pattern.compile("(.*?)\\((.*?)\\)");
        List<String[]> parsed = Arrays.asList(tokens).stream()
                .map((String x)->{
                    Matcher m = pat.matcher(x);
                    if(!m.find()) return new String[]{x};
                    else return new String[]{m.group(1), m.group(2)};
                }).collect(Collectors.toList());
        return parsed;
    }
    
    public static List<String[]> extract_seg_annot(String intxt) {
        List<String[]> annot_list = parse_annot_text(intxt).stream()                
                .map((x)->new String[]{x[0], x[0]})
                .collect(Collectors.toList());                
        
        return annot_list;
    }
    
    public static List<String[]> extract_pos_annot(String intxt) {
        List<String[]> annot_list = parse_annot_text(intxt).stream()
                .filter((x)->x.length == 2)
                .map((x)->{
                    int sidx = x[1].indexOf('/');
                    if (sidx < 0) {
                        return new String[]{x[0], x[1]};
                    } else {
                        return new String[]{x[0], x[1].substring(0, sidx)};
                    }
                })
                .collect(Collectors.toList());                
        
        return annot_list;
    }
    
    public static List<String[]> extract_ner_annot(String intxt) {
        List<String[]> annot_list = parse_annot_text(intxt).stream()
                .filter((x)->x.length == 2)
                .map((x)->new String[]{x[0], x[1].substring(x[1].indexOf('/')+1)})
                .collect(Collectors.toList());                
        
        return annot_list;
    }
    
    public static List<String> make_annotation_format(LumPositionMap pos_map, List<String[]> annot_list) {
        List<String> atxt = new ArrayList<>();
        
        int last_pos = 0;
        List<Integer> pos_list = new ArrayList<>();
        
        for(int i = 0; i < annot_list.size(); i++){
            String term = annot_list.get(i)[0];
            String tag = annot_list.get(i)[1];
            if (term.length() == 0) continue;
            
            if (term.contains("@")){
                last_pos = Integer.parseInt(term.substring(term.indexOf('@')));
            }
            
            int slop = 20;
            for(int t = 0; t < term.length(); ++t){
                int pos = pos_map.FindPosition(term.substring(t, t+1), last_pos);                
                if(pos >= 0 && pos - last_pos <= slop) {                                        
                    pos_list.add(pos);
                    last_pos = pos;
                } else {
                    pos_list.clear();
                    break;                    
                }                                
            }
                        
            if (pos_list.size() > 0 && pos_list.size() == term.length()){
                int spos = pos_list.stream().min((Integer x, Integer y) -> x-y).get();
                int epos = pos_list.stream().max((Integer x, Integer y) -> x-y).get();
                atxt.add(String.format("(%d,%d,%s)", spos, epos + 1, tag));
                pos_list.clear();
            }
        }
        
        return atxt;
    }
    
    public static Boolean checkCJK(char ch){
        int cp = (int)ch;
        //! ignore all ideographs not in BMP
        if (cp >= 0x4e00 && cp <= 0x9fff || 
            cp >= 0xf900 && cp <= 0xfaff ||
            cp >= 0x3400 && cp <= 0x4dbf) {
            return true;
        } else {
            return false;
        }
    }
    
    public static Boolean checkCJK(String str) {
        for(char ch: str.toCharArray()){
            if(!checkCJK(ch)) return false;
        }
        
        return true;
    }
}
