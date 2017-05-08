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
public class LumToken {    
    public String word;
    public String pos;
    public String ner;
    public String depLabel;
    public int    depGov = -1;
    public List<String> data;
}
