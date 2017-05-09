/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;

/**
 *
 * @author Sean_S325
 */
public class KwicResult {

    public List<LumToken> prec_context;
    public List<LumToken> succ_context;
    public List<LumToken> keyword;

    public String toStringRepr(boolean mark_key) {
        String prec = new LumToken.LumTokenListStringBuilder(prec_context).toString();
        String succ = new LumToken.LumTokenListStringBuilder(succ_context).toString();
        String key = new LumToken.LumTokenListStringBuilder(keyword).toString();
        StringBuilder sb = new StringBuilder();
        if (!prec.isEmpty()) {
            sb.append(prec);sb.append("\u3000");
        }
        
        if (mark_key) {
            sb.append(String.format("<%s>", key));
        } else {
            sb.append(String.format("%s", key));
        }
        
        if (!succ.isEmpty()){
            sb.append("\u3000"); sb.append(succ);
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return toStringRepr(true);
    }

    public static class KwicJsonList {

        private List<KwicResult> kwic_list = null;

        public KwicJsonList(List<KwicResult> ks) {
            kwic_list = ks;
        }

        public JsonArray toJson() {
            JsonArray cr_jarr = new JsonArray();
            for (KwicResult kwic : kwic_list) {
                JsonArray prec_arr = new LumToken.LumTokenListStringBuilder(kwic.prec_context).toJsonArray();
                JsonArray succ_arr = new LumToken.LumTokenListStringBuilder(kwic.succ_context).toJsonArray();
                JsonArray cr_arr = new LumToken.LumTokenListStringBuilder(kwic.keyword).toJsonArray();
                JsonObject cr_obj = new JsonObject();
                cr_obj.add("prec", prec_arr);
                cr_obj.add("succ", succ_arr);
                cr_obj.add("target", cr_arr);

                cr_jarr.add(cr_obj);
            }

            return cr_jarr;
        }

    }

}
