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
public class CohesiveFeatures extends DocumentFeatures {
    protected String get_category() {
        return "Cohesive";
    }
    private DocData doc_data = null;
    public CohesiveFeatures(DocData dd) {
        doc_data = dd;        
    }
    
    @FeatureAnnot("Content word overlap, adjacent sentences")
    public Quantiles get_words_before_main_verb() {
        return null;
    }
    
    @FeatureAnnot("Type-token ratio")
    public Double get_type_token_ratio() {
        return Double.NaN;
    }
    
    @FeatureAnnot("Semantic overlap, given-new")
    public Quantiles get_semantic_overlap() {
        return null;
    }
            
    @FeatureAnnot("Verb overlap")
    public double get_verb_overlap() {
        return Double.NaN;
    }
    
    @FeatureAnnot("Connetives: Causal, Temporal, Logical, Additive, Adversative")
    public int get_connectives() {
        return 0;
    }
    
    @FeatureAnnot("Pronoun/Noun Ratio, given-new")
    public Quantiles get_pronoun_noun() {
        return null;
    }
}
