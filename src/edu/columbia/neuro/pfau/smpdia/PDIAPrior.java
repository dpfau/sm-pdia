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
public class PDIAPrior<T> {
    public Restaurant<Node<T>> base;
    public HashMap<T,Restaurant<Node<T>>> franchises;
    private Node<T> start;

    public PDIAPrior() {
        base = new Restaurant(new NodeDist<T>());
        franchises = new HashMap<T,Restaurant<Node<T>>>();
        start = new Node<T>();
    }

    public Node<T> next(Node<T> n, T t) {
        Node<T> m = n.next(t);
        if (m == null) {
            Restaurant r;
            if (franchises.containsKey(t)) {
                r = franchises.get(t);
            } else {
                r = new Restaurant<Node<T>>(base);
                franchises.put(t, r);
            }
            m = (Node<T>)r.sampleAndAdd(n);
            n.set(t, m);
        }
        return m;
    }

    public double score() {
        double score = base.score();
        for (T t : franchises.keySet()) {
            score += franchises.get(t).score();
        }
        return score;
    }

    public Node<T> start() {
        return start;
    }
}
