/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.List;

/**
 *
 * @author Sean_S325
 */
public class KwicResult {
    public List<LumToken> prec_context;
    public List<LumToken> succ_context;
    public LumToken keyword;
    
    @Override
    public String toString() {
        String prec = new LumToken.LumTokenListStringBuilder(prec_context).toString();
        String succ = new LumToken.LumTokenListStringBuilder(succ_context).toString();
        String key = keyword.toString();
        return String.format("%s\u3000<%s>\u3000%s", prec, key, succ);
    }
}
