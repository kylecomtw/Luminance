/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.corpus;

import java.io.IOException;
import tw.com.kyle.luminance.LumIndexer;

/**
 *
 * @author Sean
 */
public interface LumIndexInterface {
    public void Index(LumIndexer indexer, String inpath) throws IOException;
}
