/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import tw.com.kyle.luminance.LumToken.LTField;

/**
 *
 * @author Sean_S325
 */
public class LumTokensBuilder {
    List<List<LumToken>> seq_list = new ArrayList<>();
        
    public int nSeq() { return seq_list.size(); }
    public List<LumToken> get(int i) {return seq_list.get(i);}
    
    public LumTokensBuilder combines(List<LumRange> range) {
        return combines(range, LTField.DATA);
    }
    
    public LumTokensBuilder combines(List<LumRange> range, LTField field) {
        boolean isSuccess = false;
        for(int i = 0; i < seq_list.size(); ++i){
            isSuccess = combine_with_sequence(seq_list.get(i), range, field);
        }
        
        if (!isSuccess) {
            List<LumToken> new_seq = new ArrayList<>();
            combine_with_sequence(new_seq, range, LTField.WORD);
            seq_list.add(new_seq);
        }
        
        return this;
    }
    
    private boolean combine_with_sequence(List<LumToken> tok_list, List<LumRange> range, LTField field) {        
        if(tok_list.isEmpty()){
            Stream<LumToken> stok = range.stream()
                            .map((LumRange x)->make_LumToken(field, x.data, x.start_off));
            tok_list.addAll(stok.collect(Collectors.toList()));
            return true;
        }
        
        //! when tok_list is not empty
        int rng_start = range.stream().mapToInt((LumRange x)->x.start_off).min().getAsInt();
        OptionalInt opt_delta = IntStream.range(0, tok_list.size())
                                  .filter((i)->tok_list.get(i).start_offset == rng_start).findFirst();
        int delta = -1;
        if (opt_delta.isPresent()) {
            delta = opt_delta.getAsInt();
            
            for(int rng_i = 0; rng_i < range.size(); ++rng_i){                
                int tok_i = rng_i + delta;
                if(tok_list.get(tok_i).start_offset != range.get(rng_i).start_off){
                    //! these two sequences do not match after all
                    return false;
                }
                
                tok_list.get(tok_i).set(field, range.get(rng_i).data);
            }
            return true;
        } else {
            //! cannot find a start alignment
            return false;
        }
        
    }
    
    @Override
    public String toString() {
        if (nSeq() == 0) {
            return "";
        } else {
            List<LumToken> token_list = seq_list.get(0);            
            return String.join("\u3000", token_list.stream()
                    .map((LumToken x)->{
                        if (x.word != null && x.pos != null && x.ner != null)
                            return String.format("%s(%s/%s)", x.word, x.pos, x.ner);
                        else if (x.word != null && x.pos != null)
                            return String.format("%s(%s)", x.word, x.pos);
                        else if (x.word != null)
                            return String.format("%s", x.word);
                        else
                            return "";
                }).collect(Collectors.toList()));
        }
    }
    
    private LumToken make_LumToken(LTField field, String data, int start_off){
        LumToken lt = new LumToken();
        lt.start_offset = start_off;
        lt.set(field, data);
        return lt;
    }
}
