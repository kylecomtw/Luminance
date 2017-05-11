/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sean_S325
 */
public class LumDocumentAdapter {

    public static LumDocument FromText(String text) {
        LumDocument doc = new LumDocument();
        String[] lines = text.split("[\r\n]");

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("#")) {
                String[] toks = line.split(":");
                toks[0] = toks[0].replace("#", "").trim();
                if (toks.length < 2) {
                    continue;
                }

                switch (toks[0].toLowerCase()) {
                    case "anno_name":
                        doc.SetAnnoName(toks[1].trim());
                        break;
                    case "anno_type":
                        doc.SetAnnoType(toks[1].trim());
                        break;
                    case "base_ref":
                        doc.SetBaseRef(Long.valueOf(toks[1].trim()));
                        doc.SetDocClass(LumDocument.ANNO);
                        break;
                    default:
                        break;
                }

            } else {
                sb.append(line.trim());
            }
        }

        doc.SetContent(sb.toString());
        return doc;

    }
}
