/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tw.com.kyle.luminance.coeus;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Sean_S325
 */
public class TopicData {    
    public List<Double> weights = null;
    public List<Integer> sorted_idx = null;
    public TopicData(List<Double> w){
        weights = w;
        build_sorted_idx();
    }
    
    private void build_sorted_idx() {
        IntStream idx_stream = IntStream.range(0, weights.size());
        sorted_idx = idx_stream.boxed()
                .sorted(Comparator.comparing((Integer x)->weights.get(x)).reversed())
                .collect(Collectors.toList());
    }
}
