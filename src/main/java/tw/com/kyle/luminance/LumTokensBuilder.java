/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import tw.com.kyle.luminance.LumToken.LTField;

/**
 *
 * @author Sean_S325
 */
public class LumTokensBuilder {

    private List<List<LumToken>> seq_list = new ArrayList<>();

    private Logger logger = Logger.getLogger(LumTokensBuilder.class.getName());

    public int nSeq() {
        return seq_list.size();
    }

    public List<LumToken> get(int i) {
        return seq_list.get(i);
    }
    private int ref_offset_start = 0;
    private int ref_offset_end = 0;
    private String ref_text = "";

    public LumTokensBuilder init(String base_text, int offset) {
        ref_offset_start = offset;
        ref_offset_end = offset + base_text.length();
        ref_text = base_text;
        return this;
    }

    public LumTokensBuilder combines(List<LumRange> range) {
        return combines(range, LTField.DATA);
    }

    public LumTokensBuilder combines(List<LumRange> range, LTField field) {
        if (ref_text.isEmpty()) {
            logger.warning("LumTokensBuilder is not initialized yet, call init() first.");
            return this;
        }

        boolean isSuccess = false;
        for (int i = 0; i < seq_list.size(); ++i) {
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

        //! if tok_list is empty, transform range data to create a new token list
        boolean ret = false;
        if (tok_list.isEmpty()) {
            if (range.isEmpty()) 
                ret = expand_reference_to_token_list(tok_list);
            else 
                ret = build_new_token_list(tok_list, range, field);
        } else {
            ret = align_with_token_list(tok_list, range, field);
        }

        return ret;
    }

    private boolean expand_reference_to_token_list(List<LumToken> tok_list) {
        int ref_off_x = ref_offset_start;
        while (ref_off_x < ref_offset_end) {            
            LumToken lt = make_LumToken(LTField.WORD,
                    Character.toString(ref_text.charAt(ref_off_x - ref_offset_start)),
                    ref_off_x);
            tok_list.add(lt);
            ref_off_x += 1;            
        }
        return true;
    }

    private boolean build_new_token_list(List<LumToken> tok_list, List<LumRange> range, LTField field) {
        if (!tok_list.isEmpty()) {
            tok_list.clear();
        }

        boolean is_match_success = true;
        int rng_i = 0;
        int rng_off_x = range.get(rng_i).start_off;
        int ref_off_x = ref_offset_start;
        while (ref_off_x < ref_offset_end) {
            if (rng_off_x != ref_off_x) {
                LumToken lt = make_LumToken(LTField.WORD,
                        Character.toString(ref_text.charAt(ref_off_x - ref_offset_start)),
                        ref_off_x);
                tok_list.add(lt);
                ref_off_x += 1;
            } else {
                LumRange lr = range.get(rng_i);
                tok_list.add(make_LumToken(field, lr.data, lr.start_off));
                ref_off_x = lr.end_off;
                rng_i = Math.min(rng_i + 1, range.size() - 1);
                rng_off_x = range.get(rng_i).start_off;
            }
        }

        return is_match_success;
    }

    private boolean align_with_token_list(List<LumToken> tok_list, List<LumRange> range, LTField field) {
        boolean is_match_success = true;        
        int tok_i = 0;
        for (int rng_i = 0; rng_i < range.size(); ++rng_i) {
            int rng_off_x = range.get(rng_i).start_off;
            int tok_off_x = tok_list.get(tok_i).start_offset;
            while (rng_off_x > tok_off_x) {
                tok_i += 1;
                tok_off_x = tok_list.get(tok_i).start_offset;
            }

            if (tok_off_x != rng_off_x) {
                is_match_success = false;
                break;
            } else {
                tok_list.get(tok_i).set(field, range.get(rng_i).data);
                tok_i += 1;
            }
        }

        return is_match_success;
    }

    @Override
    public String toString() {
        if (nSeq() == 0) {
            return "";
        } else {
            return new LumToken.LumTokenListStringBuilder(seq_list.get(0)).toString();
        }
    }

    private LumToken make_LumToken(LTField field, String data, int start_off) {
        LumToken lt = new LumToken();
        lt.start_offset = start_off;
        lt.set(field, data);
        return lt;
    }
}
