/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Sean_S325
 */
public class LumUtils {

    public static long BytesRefToLong(BytesRef bref) {
        ByteBuffer bbuf = ByteBuffer.allocate(bref.length);
        bbuf.put(bref.bytes);
        bbuf.flip();
        return bbuf.getLong();
    }

    public static BytesRef LongToBytesRef(long val) {
        ByteBuffer bbuf = ByteBuffer.allocate(Long.SIZE);
        bbuf.putLong(val);
        bbuf.flip();
        return new BytesRef(bbuf.array());
    }

    public static int GetDocId(Document doc, IndexReader reader) throws IOException {
        IndexSearcher searcher = new IndexSearcher(reader);
        BytesRef uuid = doc.getBinaryValue("uuid");
        TopDocs docs = searcher.search(new TermQuery(new Term("uuid", uuid)), 3);
        if (docs.totalHits > 0) {
            return docs.scoreDocs[0].doc;
        } else {
            return -1;
        }
    }

    public static int GetDocId(long uuid, IndexReader reader) throws IOException {
        IndexSearcher searcher = new IndexSearcher(reader);
        ByteBuffer bbuf = ByteBuffer.allocate(Long.BYTES);
        bbuf.putLong(uuid);
        BytesRef bref = new BytesRef(bbuf.array());
        TopDocs docs = searcher.search(new TermQuery(new Term("uuid", bref)), 3);
        if (docs.totalHits > 0) {
            return docs.scoreDocs[0].doc;
        } else {
            return -1;
        }
    }

    public static String get_lucene_timestamp() {
        Calendar cal = Calendar.getInstance();
        String lum_date_str = DateTools.dateToString(
                cal.getTime(), DateTools.Resolution.DAY);

        return lum_date_str;
    }
}
