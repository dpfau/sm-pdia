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
    private Table<T> t = null;
    public int seat = -1;

    public void add(Table t, int i) {
        this.t = t;
        this.seat = i;
    }

    public Table<T> table() {
        return t;
    }

    public void remove() throws Exception {
        if (t != null) {
            t.remove(this);
        } else {
            throw new Exception("Customer has no table from which to be remove!");
        }
    }
}
