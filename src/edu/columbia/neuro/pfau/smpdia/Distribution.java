/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.neuro.pfau.smpdia;

/**
 *
 * @author davidpfau
 */
public abstract class Distribution<D> {
    public abstract D sample(); // Does not change the state of the Distribution object
    public abstract D sampleAndAdd(Customer c); // Changes the state of the Distribution object
    public abstract double score(Customer[] c);
}
