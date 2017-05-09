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
 * @author Sean
 */
public class LumToken {

    public enum LTField {
        WORD, POS, NER, DEPLABEL, DEPGOV, DATA
    }

    public int start_offset = -1;
    public String word;
    public String pos;
    public String ner;
    public String depLabel;
    public int depGov = -1;
    public List<String> data;

    public void set(LTField field, String value) {
        switch (field) {
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
                } catch (NumberFormatException ex) {
                    depGov = -1;
                }
                break;
            case DATA:
            default:
                if (data == null) {
                    data = new ArrayList<>();
                }
                data.add(value);
                break;
        }
    }

    @Override
    public String toString() {
        if (word != null && pos != null && ner != null) {
            return String.format("%s(%s/%s)", word, pos, ner);
        } else if (word != null && pos != null) {
            return String.format("%s(%s)", word, pos);
        } else if (word != null) {
            return String.format("%s", word);
        } else {
            return "";
        }
    }

    public static class LumTokenListStringBuilder {

        private List<LumToken> tok_list = null;

        public LumTokenListStringBuilder(List<LumToken> toks) {
            tok_list = toks;
        }

        @Override
        public String toString() {
            if (tok_list == null) {return "";}
            return String.join("\u3000", tok_list.stream()
                    .map((LumToken x) -> x.toString())
                    .collect(Collectors.toList()));
        }
    }
}
