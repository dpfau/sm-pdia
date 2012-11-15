/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.neuro.pfau.smpdia;

import java.util.Random;

/**
 *
 * @author pfau
 */
public class Uniform extends Distribution<Float> {
    private Random r = new Random();
    public Float sample() {
        return r.nextFloat();
    }
    
    public Float sampleAndAdd(Customer c) {
        return sample();
    }
    
    public double score() {
        return 0.0;
    }
    
    public double score(Customer[] c) {
        return 0.0;
    }
    
    public void remove(Customer c) {
        
    }
}
