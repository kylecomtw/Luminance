/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.corpus.compute;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TotalHitCountCollector;
import tw.com.kyle.luminance.TextUtils;

/**
 *
 * @author Sean
 */
public class CorpusStat {
    private IndexReader reader = null;
    private IndexSearcher searcher = null;
    private Logger logger = Logger.getLogger(CorpusStat.class.getName());
    private long n_token = 0;
    private long n_doc = 0;
    private long n_frag = 0;
    private final int N_TIME_STEPS = 5;
    private String start_date_str = "";
    private String end_date_str = "";
    private List<String> time_steps = new ArrayList<>();
    private List<Integer> n_token_t = new ArrayList<>();
    
    public CorpusStat(IndexReader r) {
        reader = r;
        searcher = new IndexSearcher(reader);
    }
    
    public void SetTimeRange(String sdate, String edate, int n_step) {
        start_date_str = sdate;
        end_date_str = edate;
        time_steps = compute_time_interval(n_step);
    }
    
    public void Summarize() throws IOException {
        compute_doc_frag_token();
        compute_timed_tokens();
    }
    
    public void WriteJson(String outpath) throws IOException{
        JsonObject root = new JsonObject();
        Gson gson = new Gson();
        
        root.addProperty("nDiscourse", n_doc);
        root.addProperty("nFragments", n_frag);
        root.addProperty("nTokens", n_token);
        JsonArray jTokArr = new JsonArray();
        for(int i = 0; i < time_steps.size(); ++i){
            JsonObject jTick = new JsonObject();
            jTick.addProperty("start", time_steps.get(i));
            if(i < time_steps.size()-1){
                jTick.addProperty("end", time_steps.get(i+1));
            } else {
                jTick.addProperty("end", "");
            }
            jTick.addProperty("nToken", n_token_t.get(i));
            jTokArr.add(jTick);
        }
        root.add("nTok_time", jTokArr);
        
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
    
    private void compute_doc_frag_token() {
        try{
            n_token = get_token_freq("content");
            n_doc = get_discourse_count();
            n_frag = reader.maxDoc();            
        } catch (IOException ex) {
            logger.severe(ex.toString());
        }
        
    }
    
    private void compute_timed_tokens() throws IOException{
        for(int i = 0; i < time_steps.size(); ++i){
            n_token_t.add(0);
        }
        
        for(int i = 0; i < reader.maxDoc(); ++i){
            Document doc = reader.document(i);
            String timestamp = doc.get("timestamp");
            String content = doc.get("content");
            int time_slot = get_timeslot(timestamp);
            int tokens = get_CJK_token_count(content);
            n_token_t.set(time_slot, n_token_t.get(time_slot) + tokens);
        }
    }
    
    private int get_timeslot(String timestamp){
        for(int i = 0; i < time_steps.size() - 1; ++i){
            if (timestamp.compareTo(time_steps.get(i)) >= 0 &&
                timestamp.compareTo(time_steps.get(i+1)) < 0)
                return i;
        }
        
        return time_steps.size()-1;
    }
    
    private int get_CJK_token_count(String content){
        return (int)(content.chars().filter((x)->TextUtils.checkCJK((char)x)).count()); 
    }
    
    private long get_token_freq(String field_name) {        
        long total_freq = 0;
        try {
            for (LeafReaderContext leaf : reader.leaves()) {
                Terms terms = leaf.reader().terms("content");
                TermsEnum term_enum = terms.iterator();
                while (term_enum.next() != null) {                    
                    int freq = (int)term_enum.totalTermFreq();
                    String term_str = term_enum.term().utf8ToString();
                    if(term_str.length() == 1 && TextUtils.checkCJK(term_str.charAt(0))){                        
                        total_freq += freq;
                    }
                    
                }
                System.out.printf("%n");
            }
        } catch (IOException ex) {
            logger.severe(ex.toString());
        }

        return total_freq;
    }
        
    private int get_discourse_count() throws IOException {
        Query cq = new TermQuery(new Term("class", "discourse"));        
        TotalHitCountCollector hit_col = new TotalHitCountCollector();
        searcher.search(cq, hit_col);
        
        return hit_col.getTotalHits();
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
