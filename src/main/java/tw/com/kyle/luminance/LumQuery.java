/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spans.SpanCollector;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.SpanWeight.Postings;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Sean_S325
 */
public class LumQuery implements AutoCloseable {

    private IndexReader idx_reader = null;

    public LumQuery(String index_dir) throws IOException {
        Directory index = FSDirectory.open(Paths.get(index_dir));
        idx_reader = DirectoryReader.open(index);
    }
    
    public LumQuery(LumReader reader) throws IOException {
        idx_reader = reader.GetReader();
    }
    
    public int getTermFreq(String term, String field) throws IOException {
        Term term_inst = new Term(field, term);
        return (int) (idx_reader.totalTermFreq(term_inst));
    }
    
    public List<Integer[]> queryGrams(String term) throws IOException {
        return query_for_offsets(term, "content", term.length() > 1);
    }
    
    public List<Integer[]> queryWord(String word) throws IOException {
        return query_for_offsets(word, "anno", false);
    }
    
    public List<Integer[]> queryPos(String pos) throws IOException {
        return query_for_offsets(pos, "anno", false);
    }
    
    public List<Integer[]> query_for_offsets(String term, String field, boolean useNearQuery) throws IOException {
        if (term.length() == 0) {
            return null;
        }

        SpanQuery sq = null;
        if (!useNearQuery) {
            sq = new SpanTermQuery(new Term(field, term));
        } else {

            SpanNearQuery.Builder builder = new SpanNearQuery.Builder(field, true);
            for (int i = 0; i < term.length(); ++i) {
                builder.addClause(new SpanTermQuery(new Term(field, term.substring(i, i + 1))));
            }
            sq = builder.build();
        }

        IndexSearcher searcher = new IndexSearcher(idx_reader);
        List<Integer[]> offs = new ArrayList<>();
        for (LeafReaderContext ctx : idx_reader.leaves()) {

            SpanWeight weights = sq.createWeight(searcher, false);
            if (weights == null) {
                continue;
            }
            Spans spans = weights.getSpans(ctx, Postings.OFFSETS);
            if (spans == null) {
                System.out.printf("Nothing found for %s%n", term);
                continue;
            }

            int nxtDoc = -1;
            while ((nxtDoc = spans.nextDoc()) != Spans.NO_MORE_DOCS) {
                final int doc_id = nxtDoc;
                while (spans.nextStartPosition() != Spans.NO_MORE_POSITIONS) {
                    final int start_pos = spans.startPosition();
                    final int end_pos = spans.endPosition();
                    Integer[] off_x = new Integer[] {doc_id, -1, -1};
                    spans.collect(new SpanCollector() {
                        @Override
                        public void collectLeaf(PostingsEnum pe, int i, Term term) throws IOException {
                            int s_off = pe.startOffset();
                            int e_off = pe.endOffset();                
                            if (i == start_pos) off_x[1] = s_off;
                            if (i+1 == end_pos) off_x[2] = e_off;
                        }

                        @Override
                        public void reset() {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    });
                    offs.add(off_x);
                }

            }

        }

        return offs;
    }

    public void ListTerm(int docId) throws IOException {

        Terms terms = idx_reader.getTermVector(docId, "content");
        TermsEnum term_enum = terms.iterator();
        while (term_enum.next() != null) {
            System.out.printf("%s", term_enum.term().utf8ToString());
            PostingsEnum post_enum = term_enum.postings(null, PostingsEnum.POSITIONS);
            post_enum.nextDoc();
            int freq = post_enum.freq();
            System.out.printf("%d: ", freq);
            for (int i = 0; i < freq; ++i) {
                System.out.printf("%d, ", post_enum.nextPosition());
            }
            System.out.printf("%n");
        }
    }

    @Override
    public void close() throws IOException {
        idx_reader.close();
    }
}
