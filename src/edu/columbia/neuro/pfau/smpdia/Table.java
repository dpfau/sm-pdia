/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.neuro.pfau.smpdia;

/**
 *
 * @author davidpfau
 */
public class Table<T> extends Customer<T> {
    public Customer root = null;
    public int size = 0;
    private T dish;

    public void setDish(T d) {
        dish = d;
    }

    public T getDish() {
        return dish;
    }
}
