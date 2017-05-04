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
public class SurfaceFeatures extends DocumentFeatures  {
    protected String get_category() {
        return "Surface";
    }
    
    private DocData doc_data = null;
    public SurfaceFeatures(DocData dd) {
        doc_data = dd;        
    }
    
    @FeatureAnnot("Character Frequency")
    public Quantiles get_charac_freq() {
        Double[] qarr = new Double[]{.0, .0, .0};
        return new Quantiles(qarr);
    }
    
    @FeatureAnnot("Word Frequency")
    public Quantiles get_word_freq() {
        Double[] qarr = new Double[]{.0, .0, .0};
        return new Quantiles(qarr);
    }
    
    @FeatureAnnot("Clause Length")
    public Quantiles get_clause_length() {
        Double[] qarr = new Double[]{.0, .0, .0};
        return new Quantiles(qarr);
    }
    
    @FeatureAnnot("Sentence Length")
    public Quantiles get_sentence_length() {
        Double[] qarr = new Double[]{.0, .0, .0};
        return new Quantiles(qarr);
    }
    
    @FeatureAnnot("Character Count")
    public int get_character_count() {
        return doc_data.token_list.stream().mapToInt((x)->x.word.length()).sum();
    }
    
    @FeatureAnnot("Word Count")
    public int get_word_count() {
        return doc_data.token_list.size();
    }
}
