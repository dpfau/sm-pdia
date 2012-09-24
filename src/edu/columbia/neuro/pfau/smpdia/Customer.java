/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.neuro.pfau.smpdia;

/**
 *
 * @author davidpfau
 */
public class Customer<T> {
    public Table<T> table = null;
    public Customer custRight = null;
    public Customer custLeft  = null;

    public void remove() throws Exception {
        if (table != null) {
            table.remove(this);
        } else {
            throw new Exception("Customer has no table from which to be remove!");
        }
    }
}
