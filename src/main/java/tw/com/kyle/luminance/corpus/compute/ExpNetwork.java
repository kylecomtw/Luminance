/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.corpus.compute;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedDocValues;
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
    private final int N_TIME_STEPS = 5;
    private String start_date_str = "";
    private String end_date_str = "";
    private List<String> time_steps = new ArrayList<>();

    private class NodeInfo {

        public int weight = 0;
        public List<Integer> time_weights = new ArrayList<>();
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

    public static Map<String, Integer> LoadMweFromJson(String injsonpath) throws IOException {
        Map<String, Integer> json_nodes = new HashMap<>();
        String json_content = String.join("\n", Files.readAllLines(
                Paths.get(injsonpath), StandardCharsets.UTF_8));
        JsonElement root = new JsonParser().parse(json_content);
        JsonArray rarray = root.getAsJsonArray();
        for (int i = 0; i < rarray.size(); ++i) {
            JsonObject jnode = (JsonObject) rarray.get(i);
            int freq = jnode.get("f").getAsJsonObject().get("nT").getAsInt();
            json_nodes.put(jnode.get("L").getAsString(), freq);
        }
        return json_nodes;
    }

    public void AddNodes(Map<String, Integer> str_map) {
        for (Entry<String, Integer> ent : str_map.entrySet()) {
            node_map.put(ent.getKey(), new NodeInfo(ent.getValue()));
        }
    }
    
    public void KeepNodes(int threshold) {
        node_map = node_map.entrySet().stream()
                .filter((x)->x.getValue().weight > threshold)                
                .collect(Collectors.toMap((x) -> x.getKey(), (x) -> x.getValue()));
    }
    
    public void KeepFirstNNodes(int n) {
        node_map = node_map.entrySet().stream()
                .sorted(Comparator.comparing((x) -> -x.getValue().weight))
                .limit(n)
                .collect(Collectors.toMap((x) -> x.getKey(), (x) -> x.getValue()));
    }

    public void SetTimeRange(String sdate, String edate, int n_step) {
        start_date_str = sdate;
        end_date_str = edate;
        time_steps = compute_time_interval(n_step);
    }

    public void ComputeEdges(int temporal_threshold) {
        //! call for its side effect
        if (start_date_str.length() == 0 || end_date_str.length() == 0) {
            set_time_range();
        }

        List<String> node_list = node_map.entrySet().stream()
                .sorted(Comparator.comparing((x) -> x.getValue().weight))
                .map((x) -> x.getKey())
                .collect(Collectors.toList());

        int total_combo = (node_list.size() * (node_list.size() - 2)) / 2;
        int counter = 0;
        for (int i = 0; i < node_list.size(); ++i) {
            
            //! put node time range query here, might not be the best place,
            //! [TODO] fix it later            
            String node_x = node_list.get(i);      
            try {
                node_map.put(node_x, query_node_info(node_x));
            } catch (IOException ex) {
                Logger.getLogger(ExpNetwork.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            for (int j = i + 1; j < node_list.size(); ++j) {
                String node_y = node_list.get(j);
                try {
                    EdgeInfo einfo = query_edge_info(node_x, node_y, temporal_threshold);
                    if(einfo.weight >= temporal_threshold){
                        edge_set.add(einfo);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ExpNetwork.class.getName()).log(Level.SEVERE, null, ex);
                }
                counter += 1;

                if (counter % 100 == 0) {
                    System.out.printf("computing edges %d/%d%n", counter, total_combo);
                }
            }

        }
    }

    public void WriteJson(String outpath) throws IOException {
        JsonObject root = new JsonObject();
        Gson gson = new Gson();
        JsonArray jnodes = new JsonArray();
        JsonArray jedges = new JsonArray();
        node_map.forEach((k, v) -> {
            JsonObject node_x = new JsonObject();
            node_x.addProperty("txt", k);
            node_x.addProperty("f", v.weight);
            JsonArray wdata_t = new JsonArray();
            v.time_weights.forEach((wt)->wdata_t.add(wt));
            node_x.add("Wt", wdata_t);
            jnodes.add(node_x);
        });
        edge_set.forEach((x) -> {
            JsonObject edge_x = new JsonObject();
            edge_x.addProperty("a", x.term_a);
            edge_x.addProperty("b", x.term_b);
            JsonObject wdata = new JsonObject();
            JsonArray wdata_t = new JsonArray();
            wdata.addProperty("Wa", x.weight);
            x.time_weights.forEach((wt) -> wdata_t.add(wt));
            wdata.add("Wt", wdata_t);
            edge_x.add("W", wdata);
            jedges.add(edge_x);
        });
        root.add("nodes", jnodes);
        root.add("edges", jedges);
        try {
            JsonWriter writer = new JsonWriter(new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outpath), StandardCharsets.UTF_8)));
            writer.setIndent("  ");
            gson.toJson(root, writer);
            writer.close();
            logger.info(String.format("Write %d nodes, %d edges to Json", jnodes.size(), jedges.size()));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CollocateFromIndex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private NodeInfo query_node_info(String node_x) throws IOException {
        NodeInfo ninfo = node_map.get(node_x);
        for (int t = 0; t < time_steps.size() - 1; ++t) {
            int freq_t = 0;
            String start_date_x = time_steps.get(t);
            String end_date_x = time_steps.get(t + 1);
            try {
                freq_t = get_token_count_timerange(node_x, start_date_x, end_date_x);
            } catch (IOException ex) {
                logger.severe(ex.toString());
                freq_t = 0;
            }
            ninfo.time_weights.add(freq_t);
        }

        return ninfo;
    }
    
    private EdgeInfo query_edge_info(String node_x, String node_y, int weight_threshold) throws IOException {
        EdgeInfo einfo = new EdgeInfo(node_x, node_y);
        int freq = get_common_context_count(node_x, node_y);
        einfo.weight = freq;
        if (freq < weight_threshold) {
            return einfo;
        }
        
        for (int t = 0; t < time_steps.size() - 1; ++t) {
            int freq_t = 0;
            String start_date_x = time_steps.get(t);
            String end_date_x = time_steps.get(t + 1);
            try {
                freq_t = get_common_context_count_timeranged(node_x, node_y, start_date_x, end_date_x);
            } catch (IOException ex) {
                logger.severe(ex.toString());
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

    private int get_common_context_count_timeranged(String node_x, String node_y,
            String start_date_str, String end_date_str) throws IOException {
        int n_discourse = get_common_doc_count(node_x, node_y, "discourse", start_date_str, end_date_str);
        int n_fragment = get_common_doc_count(node_x, node_y, "fragment", start_date_str, end_date_str);
        return n_discourse + n_fragment * 2;
    }

    private int get_common_doc_count(String node_x, String node_y,
            String doc_type, String start_date_str, String end_date_str) throws IOException {
        int n_doc = 0;
        Query query_a = build_phrase_query(node_x);
        Query query_b = build_phrase_query(node_y);
        Query query_c = new TermQuery(new Term("class", doc_type));

        BooleanQuery.Builder bquery = new BooleanQuery.Builder();
        bquery.add(query_a, BooleanClause.Occur.MUST);
        bquery.add(query_b, BooleanClause.Occur.MUST);
        bquery.add(query_c, BooleanClause.Occur.MUST);

        Weight w = bquery.build().createWeight(searcher, false);

        for (LeafReaderContext ctx : reader.leaves()) {
            SortedDocValues sorted_dv = ctx.reader().getSortedDocValues("timestamp");
            Scorer scorer = w.scorer(ctx);
            if (scorer == null) {
                continue;
            }
            DocIdSetIterator doc_it = scorer.iterator();
            int nxtDoc = 0;
            while ((nxtDoc = doc_it.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                String timestamp = new String(sorted_dv.get(nxtDoc).bytes, StandardCharsets.UTF_8);
                //! note that both timestamp, (start|end)_date_str are both
                //! formatted so can be ordered lexically
                int dbg1 = timestamp.compareTo(start_date_str);
                int dbg2 = timestamp.compareTo(end_date_str);
                if (timestamp.compareTo(start_date_str) >= 0 && timestamp.compareTo(end_date_str) < 0) {
                    n_doc += 1;
                }
            }
        }
        return n_doc;
    }

    private int get_common_doc_count(String node_x, String node_y, String doc_type) throws IOException {
        return get_common_doc_count(node_x, node_y, doc_type, start_date_str, end_date_str);
    }
    
    private int get_token_count_timerange(String node_x,
            String start_date_str, String end_date_str) throws IOException {
        int n_doc = 0;
        Query query_a = build_phrase_query(node_x);
        Weight w = query_a.createWeight(searcher, false);

        for (LeafReaderContext ctx : reader.leaves()) {
            SortedDocValues sorted_dv = ctx.reader().getSortedDocValues("timestamp");
            Scorer scorer = w.scorer(ctx);
            if (scorer == null) {
                continue;
            }
            DocIdSetIterator doc_it = scorer.iterator();
            int nxtDoc = 0;
            while ((nxtDoc = doc_it.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
                String timestamp = new String(sorted_dv.get(nxtDoc).bytes, StandardCharsets.UTF_8);
                //! note that both timestamp, (start|end)_date_str are both
                //! formatted so can be ordered lexically
                int dbg1 = timestamp.compareTo(start_date_str);
                int dbg2 = timestamp.compareTo(end_date_str);
                if (timestamp.compareTo(start_date_str) >= 0 && timestamp.compareTo(end_date_str) < 0) {
                    n_doc += 1;
                }
            }
        }
        return n_doc;
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
        final String FIELD_NAME = "timestamp";
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

        //! get time bins
        time_steps = compute_time_interval(N_TIME_STEPS);
    }

    private List<String> compute_time_interval(int time_bins) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        List<String> tsteps = new ArrayList<>();

        try {
            Date start_date = sdf.parse(start_date_str);
            Date end_date = sdf.parse(end_date_str);
            long start_time = start_date.getTime();
            long end_time = end_date.getTime();
            for (int i = 0; i <= time_bins; i++) {
                long step_x = (long) Math.round(
                        ((double) (end_time - start_time)) / time_bins * i + start_time);
                Date step_date = new Date(step_x);
                tsteps.add(sdf.format(step_date));
            }
        } catch (ParseException ex) {
            logger.severe("Date parse error when using yyyyMMdd");
            logger.severe(ex.toString());
        }

        return tsteps;
    }
}
