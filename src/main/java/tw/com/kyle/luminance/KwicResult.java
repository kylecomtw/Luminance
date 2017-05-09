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

    @Override
    public String toString() {
        String prec = new LumToken.LumTokenListStringBuilder(prec_context).toString();
        String succ = new LumToken.LumTokenListStringBuilder(succ_context).toString();
        String key = new LumToken.LumTokenListStringBuilder(keyword).toString();
        return String.format("%s\u3000<%s>\u3000%s", prec, key, succ);
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
