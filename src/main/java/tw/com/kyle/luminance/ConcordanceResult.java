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
public class ConcordanceResult {
    public Token target;
    public List<Token> prec_context;
    public List<Token> succ_context;
}
