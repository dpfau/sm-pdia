/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.neuro.pfau.smpdia;

/**
 *
 * @author pfau
 */
public class Count extends Distribution<int[]> {
    private int n;
    
    public Count(int n) {
        this.n = n;
    }
    
    public int[] sample() {
        return new int[n];
    }
    
    public int[] sampleAndAdd(Customer c) {
        return new int[n];
    }
    
    public void remove(Customer c) throws Exception {
        
    }
    
    public double score(Customer[] c) {
        return 0.0;
    }
}
