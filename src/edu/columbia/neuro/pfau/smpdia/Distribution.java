/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.neuro.pfau.smpdia;

/**
 *
 * @author davidpfau
 */
public abstract class Distribution<T> {
    public abstract T sample(); // Does not change the state of the Distribution object
    public abstract T sampleAndAdd(Customer c); // Changes the state of the Distribution object
    public abstract void remove(Customer c) throws Exception; // Also changes the state of the Distribution object
    public abstract double score(Customer[] c);
}
