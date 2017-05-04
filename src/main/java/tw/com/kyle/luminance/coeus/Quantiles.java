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
public class Quantiles {
    private Double[] arr = null;
    public double Q25() {return arr[0]; }
    public double Q50() {return  arr[1]; }
    public double Q75() {return arr[2]; }
    public Quantiles(Double[] darr){
        if (darr.length == 3) {
            arr = darr;
        } else {
            arr = new Double[] {Double.NaN, Double.NaN, Double.NaN};
        }        
    }
}
