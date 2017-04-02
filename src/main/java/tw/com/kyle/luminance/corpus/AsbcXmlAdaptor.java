/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.corpus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Sean
 */
public class AsbcXmlAdaptor {
    public class AsbcDocument {
        public int serial_no = 0;
        public String title = "";
        public String asbc_class = "";
        public String asbc_pdate = "";        
        public String text = "";
    }
    
    private Logger logger = Logger.getLogger(AsbcXmlAdaptor.class.getName());
    
    public AsbcXmlAdaptor() {}
    
    public List<AsbcDocument> Parse(String inxmlpath) throws IOException {
        if (!Files.exists(Paths.get(inxmlpath))){
            return new ArrayList<>();
        }
        if (!inxmlpath.endsWith(".xml")) {
            return new ArrayList<>();
        }
                
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        Document xdoc = null;
        try {
            builder = factory.newDocumentBuilder();
            xdoc = builder.parse(new File(inxmlpath));
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(AsbcXmlAdaptor.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        
        Element root = xdoc.getDocumentElement();
        NodeList art_list = root.getElementsByTagName("article");        
        List<AsbcDocument> adoc_list = new ArrayList<>();
        for (int i = 0; i < art_list.getLength(); i++){
            AsbcDocument adoc = new AsbcDocument();
            Node art_node = art_list.item(i);
            Element art_elem = (Element) art_node;
            adoc.serial_no = Integer.parseInt(art_elem.getAttribute("no"));
            adoc.asbc_class = get_element_text(art_elem, "class");
            adoc.asbc_pdate = get_element_text(art_elem, "publishdate");
            adoc.title = get_element_text(art_elem, "title");
            adoc.text = get_text_content(art_elem, "text");
            adoc_list.add(adoc);
        }
        
        logger.info(String.format("Processing %d Documents in %s", adoc_list.size(), inxmlpath));
        return adoc_list;
    }
    
    private String get_element_text(Element parent, String tag) {
        Element ch_elem = (Element)parent.getElementsByTagName(tag).item(0);
        return ch_elem.getTextContent();
    }
    
    private String get_text_content(Element art_elem, String tag) {
        NodeList sentence_list = art_elem.getElementsByTagName("sentence");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < sentence_list.getLength(); ++i){
            Node sent_elem = (Element)sentence_list.item(i);
            sb.append(sent_elem.getTextContent());            
        }
        return sb.toString();
    }                    
}
