/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.coeus;

/**
 *
 * @author Sean_S325
 */
public class TopicFeatures extends DocumentFeatures {
    protected String get_category() {
        return "Topics";
    }
    
    private DocData doc_data = null;
    public TopicFeatures(DocData dd) {
        doc_data = dd;        
    }
    
    @FeatureAnnot("First Topic Index")
    public int get_first_topic_index() {
        return -1;
    }
    
    @FeatureAnnot("Second Topic Index")
    public int get_second_topic_index() {
        return -1;
    }
    
    @FeatureAnnot("Third Topic Index")
    public int get_third_topic_index() {
        return 0;
    }
    
    @FeatureAnnot("Top 5 Cumulative Probability")
    public Double get_first_five_cumul_prob() {
        return Double.NaN;
    }
    
    @FeatureAnnot("Top 5 Entropy")
    public Double get_first_five_entropy() {
        return Double.NaN;
    }
}
