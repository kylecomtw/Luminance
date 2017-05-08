/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.util.List;
import org.apache.lucene.analysis.Token;

/**
 *
 * @author Sean
 */
public class ConcordanceResult {
    public LumToken target;
    public List<LumToken> prec_context;
    public List<LumToken> succ_context;
}
