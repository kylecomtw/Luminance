/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.corpus.compute;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Sean
 */
public class ExpNetwork {

    private IndexReader reader = null;
    private IndexSearcher searcher = null;
    private Logger logger = Logger.getLogger(ExpNetwork.class.getName());
    private Map<String, NodeInfo> node_map = new HashMap<>();
    private Set<EdgeInfo> edge_set = new HashSet<>();
    private final int TIME_STEPS = 5;
    private String start_date_str = "";
    private String end_date_str = "";
    
    private class NodeInfo {

        public int weight = 0;

        public NodeInfo(int w) {
            weight = w;
        }
    }

    private class EdgeInfo {

        public String term_a;
        public String term_b;
        public int weight;
        public List<Integer> time_weights = new ArrayList<>();

        public EdgeInfo(String a, String b) {
            term_a = a;
            term_b = b;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (other == null) {
                return false;
            }
            if (other.getClass() != getClass()) {
                return false;
            }

            EdgeInfo obj0 = (EdgeInfo) other;
            return obj0.term_a.equals(term_a) && obj0.term_b.equals(term_b);
        }

        @Override
        public int hashCode() {
            return term_a.hashCode() ^ term_b.hashCode();
        }
    }

    public ExpNetwork(IndexReader r) {
        reader = r;
        searcher = new IndexSearcher(reader);
    }

    public static Map<String, NodeInfo> LoadMweFromJson(String injsonpath) throws IOException {
        Map<String, NodeInfo> json_nodes = new HashMap<>();
        String json_content = String.join("\n", Files.readAllLines(
                Paths.get(injsonpath), StandardCharsets.UTF_8));
        JsonElement root = new JsonParser().parse(json_content);
        JsonArray rarray = root.getAsJsonArray();
        return json_nodes;
    }

    public void AddNodes(Map<String, NodeInfo> str_map) {
        node_map.put("服貿協議", new NodeInfo(990));
        node_map.put("太陽花學運", new NodeInfo(890));
        node_map.put("監督條例", new NodeInfo(790));
        node_map.put("退回服貿", new NodeInfo(690));
        node_map.put("兩岸協議", new NodeInfo(590));
    }

    public void ComputeEdges() {
        set_time_range();
        List<String> node_list = node_map.entrySet().stream()
                .sorted(Comparator.comparing((x) -> x.getValue().weight))
                .map((x) -> x.getKey())
                .collect(Collectors.toList());
        for (int i = 0; i < node_list.size(); ++i) {
            String node_x = node_list.get(i);
            for (int j = i + 1; j < node_list.size(); ++j) {
                String node_y = node_list.get(j);                
                try {
                    EdgeInfo einfo = query_edge_info(node_x, node_y);
                    edge_set.add(einfo);
                } catch (IOException ex) {
                    Logger.getLogger(ExpNetwork.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
        }
    }

    public void WriteJson(String outpath) throws IOException {
        JsonArray root = new JsonArray();
        Gson gson = new Gson();

        try {
            JsonWriter writer = new JsonWriter(new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outpath), StandardCharsets.UTF_8)));
            writer.setIndent("  ");
            gson.toJson(root, writer);
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CollocateFromIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private EdgeInfo query_edge_info(String node_x, String node_y) throws IOException{
        EdgeInfo einfo = new EdgeInfo(node_x, node_y);
        int freq = get_common_context_count(node_x, node_y);
        einfo.weight = freq;
        for(int t = 0; t < TIME_STEPS; ++t){
            int freq_t = 0;
            try {
                freq_t = get_common_context_count(node_x, node_y);
            } catch (IOException ex) {
                freq_t = 0;
            }
            einfo.time_weights.add(freq_t);
        }                
        
        return einfo;
    }

    private int get_common_context_count(String node_x, String node_y) throws IOException {
        int n_discourse = get_common_doc_count(node_x, node_y, "discourse");
        int n_fragment = get_common_doc_count(node_x, node_y, "fragment");
        return n_discourse + n_fragment * 2;
    }
    
    private int get_common_doc_count(String node_x, String node_y, 
            String doc_type, String start_date_str, String end_date_str) throws IOException {
        int n_doc = 0;
        Query query_a = build_phrase_query(node_x);
        Query query_b = build_phrase_query(node_y);
        Query query_c = new TermQuery(new Term("class", doc_type));
        
        Term begin_time = new Term("timestamp", lucene_date_format(start_date_str));
        Term end_time = new Term("timestamp", lucene_date_format(end_date_str));
        Query query_d = new TermRangeQuery("timestamp",
                        begin_time.bytes(), end_time.bytes(),
                        true, false);
        BooleanQuery.Builder bquery = new BooleanQuery.Builder();
        bquery.add(query_a, BooleanClause.Occur.MUST);
        bquery.add(query_b, BooleanClause.Occur.MUST);
        bquery.add(query_c, BooleanClause.Occur.MUST);
        bquery.add(query_d, BooleanClause.Occur.MUST);

        Weight w = bquery.build().createWeight(searcher, false);

        for (LeafReaderContext ctx : reader.leaves()) {
            Scorer scorer = w.scorer(ctx);
            if (scorer == null) {
                continue;
            }
            DocIdSetIterator doc_it = scorer.iterator();
            int nxtDoc = 0;
            while ((nxtDoc = doc_it.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                n_doc += 1;
            }
        }
        return n_doc;
    }
    
    private int get_common_doc_count(String node_x, String node_y, String doc_type) throws IOException {
        return get_common_doc_count(node_x, node_y, doc_type, start_date_str, end_date_str);
    }

    private PhraseQuery build_phrase_query(String in_str) {
        PhraseQuery.Builder builder = new PhraseQuery.Builder();
        for (int i = 0; i < in_str.length(); ++i) {
            builder.add(new Term("content", in_str.substring(i, i + 1)));
        }
        return builder.build();
    }
    
    private String lucene_date_format(String timestamp) {                    
        String lum_date_str = null;
        try {
            DateFormat format = new SimpleDateFormat("yyyyMMdd");
            lum_date_str = DateTools.dateToString(
                                    format.parse(timestamp),
                                    DateTools.Resolution.DAY);            
        } catch (ParseException ex) { 
            Calendar cal = Calendar.getInstance();
            cal.set(2010, 0, 0, 0, 0, 0);
            lum_date_str = DateTools.dateToString(
                                    cal.getTime(), DateTools.Resolution.DAY);            
        }
        
        return lum_date_str;                
    }
    
    //! this function purely works for side effect
    //! it set class properties: start_date_str, end_date_str
    private void set_time_range() {
        final String FIELD_NAME = "dvTimestamp";
        Term begin_time = new Term(FIELD_NAME, lucene_date_format("19800101"));
        Term end_time = new Term(FIELD_NAME, lucene_date_format("20991231"));
        
        Query query = new TermRangeQuery(FIELD_NAME, 
                            begin_time.bytes(), end_time.bytes(),
                            false, true);
        try {
            TopDocs hit_first = searcher.search(query, 1, new Sort(new SortField(FIELD_NAME, SortField.Type.STRING)));
            TopDocs hit_last = searcher.search(query, 1, new Sort(new SortField(FIELD_NAME, SortField.Type.STRING, true)));
            start_date_str = reader.document(hit_first.scoreDocs[0].doc).get(FIELD_NAME);
            end_date_str = reader.document(hit_last.scoreDocs[0].doc).get(FIELD_NAME);
        } catch (IOException ex) {
            Logger.getLogger(ExpNetwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
