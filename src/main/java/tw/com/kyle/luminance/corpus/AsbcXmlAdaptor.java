/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.corpus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.BytesRef;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tw.com.kyle.luminance.AnnotationProvider;
import tw.com.kyle.luminance.FieldTypeFactory;
import tw.com.kyle.luminance.LumDocument;
import tw.com.kyle.luminance.LumIndexer;

/**
 *
 * @author Sean
 */
public class AsbcXmlAdaptor implements LumIndexInterface {

    public class AsbcDocument {

        public int serial_no = 0;
        public String title = "";
        public String asbc_class = "";
        public String asbc_pdate = "";
        public String text = "";
    }

    private Logger logger = Logger.getLogger(AsbcXmlAdaptor.class.getName());

    public AsbcXmlAdaptor() {
    }

    public List<AsbcDocument> Parse(String inxmlpath) throws IOException {
        if (!Files.exists(Paths.get(inxmlpath))) {
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
        for (int i = 0; i < art_list.getLength(); i++) {
            AsbcDocument adoc = new AsbcDocument();
            Node art_node = art_list.item(i);
            Element art_elem = (Element) art_node;
            // adoc.serial_no = Integer.parseInt(art_elem.getAttribute("no"));
            // adoc.asbc_class = get_element_text(art_elem, "class");
            adoc.asbc_pdate = get_element_text(art_elem, "publishdate");
            adoc.title = get_element_text(art_elem, "title");
            adoc.text = get_text_content(art_elem, "text");
            adoc_list.add(adoc);
        }

        logger.info(String.format("Processing %d Documents in %s", adoc_list.size(), inxmlpath));
        return adoc_list;
    }

    @Override
    public void Index(LumIndexer indexer, String inpath) throws IOException {
        List<AsbcDocument> doc_list = Parse(inpath);
        for (AsbcDocument doc : doc_list) {
            org.apache.lucene.document.Document base_doc = indexer.CreateIndexDocument(LumIndexer.DOC_DISCOURSE);
            AnnotationProvider annot = new AnnotationProvider(doc.text);
            LumDocument lum_doc = annot.create_discourse_doc();
            indexer.AddField(base_doc, "title", doc.title, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
            indexer.AddField(base_doc, "timestamp", lucene_date_format(doc.asbc_pdate), FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
            indexer.AddField(base_doc, "timestamp", new BytesRef(lucene_date_format(doc.asbc_pdate)), FieldTypeFactory.Get(FieldTypeFactory.FTEnum.TimestampIndex));
            indexer.AddField(base_doc, "content", lum_doc.GetContent(), FieldTypeFactory.Get(FieldTypeFactory.FTEnum.FullIndex));

            BytesRef base_doc_ref = indexer.GetUUIDAsBytesRef(base_doc);
            indexer.AddToIndex(base_doc);
            indexer.flush();
            
            IndexReader idx_reader = indexer.GetReader();
            if (annot.has_segmented()) {
                LumDocument seg_doc = annot.create_annot_doc_seg(base_doc_ref);
                org.apache.lucene.document.Document seg_adoc = indexer.CreateIndexDocument(LumIndexer.DOC_ANNOTATION);
                setup_index_annot_document(indexer, seg_adoc, "seg", seg_doc.GetContent(), base_doc_ref);
                indexer.AddToIndex(seg_adoc);
            }

            if (annot.has_pos_tagged()) {
                LumDocument pos_doc = annot.create_annot_doc_pos(base_doc_ref);
                org.apache.lucene.document.Document pos_adoc = indexer.CreateIndexDocument(LumIndexer.DOC_ANNOTATION);
                setup_index_annot_document(indexer, pos_adoc, "pos", pos_doc.GetContent(), base_doc_ref);
                indexer.AddToIndex(pos_adoc);
            }
        }
    }

    private void setup_index_annot_document(LumIndexer indexer,
            org.apache.lucene.document.Document idx_doc,
            String annot_type, String annot_content, BytesRef base_doc_ref) {
        indexer.AddField(idx_doc, "annot", annot_content, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.NonStoredFullIndex));
        indexer.AddField(idx_doc, "base_ref", base_doc_ref, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(idx_doc, "annot_type", annot_type, FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(idx_doc, "annot_mode", "manual", FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
        indexer.AddField(idx_doc, "annot_range", "spanned", FieldTypeFactory.Get(FieldTypeFactory.FTEnum.RawStoredIndex));
    }

    private String get_element_text(Element parent, String tag) {
        Element ch_elem = (Element) parent.getElementsByTagName(tag).item(0);
        return ch_elem.getTextContent();
    }

    private String get_text_content(Element art_elem, String tag) {
        NodeList sentence_list = art_elem.getElementsByTagName("sentence");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentence_list.getLength(); ++i) {
            Node sent_elem = (Element) sentence_list.item(i);
            sb.append(sent_elem.getTextContent());
        }
        return sb.toString();
    }

    private String lucene_date_format(String timestamp) {
        String lum_date_str = null;
        try {
            DateFormat format = new SimpleDateFormat("yyyy");
            lum_date_str = DateTools.dateToString(
                    format.parse(timestamp),
                    DateTools.Resolution.DAY);
        } catch (ParseException ex) {
            Calendar cal = Calendar.getInstance();
            cal.set(2010, 1, 1, 0, 0, 0);
            lum_date_str = DateTools.dateToString(
                    cal.getTime(), DateTools.Resolution.DAY);
        }

        return lum_date_str;
    }
}
