/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Sean_S325
 */
public class LumTokensBuilder {
    List<List<LumToken>> seq_list = new ArrayList<>();
        
    public int nSeq() { return seq_list.size(); }
    public List<LumToken> get(int i) {return seq_list.get(i);}
    public LumTokensBuilder combines(List<LumRange> range) {
        return this;
    }
    
    public String toString() {
        if (nSeq() == 0) {
            return "";
        } else {
            List<LumToken> token_list = seq_list.get(0);
            return String.join("\u3000", token_list.stream()
                    .map((LumToken x)->{
                        if (x.pos.length() > 0 && x.ner.length() > 0)
                            return String.format("%s(%s/%s)", x.word, x.pos, x.ner);
                        else if (x.pos.length() > 0)
                            return String.format("%s(%s)", x.word, x.pos);
                        else
                            return x.word;
                }).collect(Collectors.toList()));
        }
    }
}
