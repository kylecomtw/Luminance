/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.List;

/**
 *
 * @author Sean
 */
public class LumToken {
    public enum LTField {WORD, POS, NER, DEPLABEL, DEPGOV, DATA}
    
    public int    start_offset = -1;
    public String word;
    public String pos;
    public String ner;
    public String depLabel;
    public int    depGov = -1;
    public List<String> data;
    
    public void set(LTField field, String value) {
        switch(field){
            case WORD: 
                word = value;
                break;
            case POS:
                pos = value;
                break;
            case NER:
                ner = value;
                break;
            case DEPLABEL:
                depLabel = value;
                break;
            case DEPGOV:
                try {
                    depGov = Integer.valueOf(value);                    
                } catch (NumberFormatException ex){
                    depGov = -1;
                }
                break;
            case DATA:
            default:
                data.add(value);
                break;                            
        }
    }
}
