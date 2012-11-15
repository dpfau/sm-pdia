/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.neuro.pfau.smpdia;

import java.util.HashMap;

/**
 *
 * @author davidpfau
 */
public class Node<T> extends Customer{
    private HashMap<T,Node<T>> next;
    // Note that in the PDIA, we track this in two places: within the Node
    // object itself, and the dish served at the table in the restaurant
    // franchise that this Node is seated at.  When changing edges, we need to
    // be careful that these two are kept consistent.

    public Node() {
        next = new HashMap();
    }

    public Node<T> next(T t) {
        return next.get(t);
    }

    public void set(T t, Node<T> n) {
        next.put(t, n);
    }
}
