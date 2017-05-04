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
public class StructureFeatures extends DocumentFeatures {
    protected String get_category() {
        return "Structures";
    }
    
    private DocData doc_data = null;
    public StructureFeatures(DocData dd) {
        doc_data = dd;        
    }
    
    @FeatureAnnot("Ratio of function words to content words")
    public double get_ratio_func_content_words() {
        return 0.0f;        
    }
    
    @FeatureAnnot("Number of causal verbs")
    public int get_n_causal_verbs() {
        return 0;        
    }
        
    @FeatureAnnot("Number of intentional particles")
    public int get_n_intentional_particles() {
        return 0;
    }
                
    @FeatureAnnot("Words per sentence")
    public Quantiles get_words_per_sentence() {
        return null;
    }
    
    @FeatureAnnot("Syntactic Similarity")
    public Quantiles get_syntactic_similarity() {
        return null;
    }
    
    @FeatureAnnot("Words before main verb")
    public Quantiles get_words_before_main_verb() {
        return null;
    }
    
    @FeatureAnnot("Proposition Depth")
    public Quantiles get_prop_depth() {
        return null;
    }
}
