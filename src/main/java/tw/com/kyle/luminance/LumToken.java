/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

    public JsonObject toJson() {
        JsonObject x_obj = new JsonObject();
        x_obj.addProperty("text", word);
        x_obj.addProperty("pos", pos);
        if (data != null) {
            x_obj.addProperty("data", String.join(", ", data));
        }
        if (ner != null) {
            x_obj.addProperty("ner", ner);
        }
        if (depLabel != null) {
            x_obj.addProperty("depLabel", depLabel);
        }
        if (depGov >= 0) {
            x_obj.addProperty("depGov", depGov);
        }

        return x_obj;
    }

    public static class LumTokenListStringBuilder {

        private List<LumToken> tok_list = null;

        public LumTokenListStringBuilder(List<LumToken> toks) {
            tok_list = toks;
        }

        @Override
        public String toString() {
            if (tok_list == null) {
                return "";
            }
            return String.join("\u3000", tok_list.stream()
                    .map((LumToken x) -> x.toString())
                    .collect(Collectors.toList()));
        }

        public JsonArray toJsonArray() {
            JsonArray jarr = new JsonArray();
            tok_list.stream().map((x) -> x.toJson())
                    .forEach((x) -> jarr.add(x));
            return jarr;
        }
    }
}
