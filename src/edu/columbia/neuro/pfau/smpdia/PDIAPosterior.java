/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.neuro.pfau.smpdia;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author davidpfau
 */
public class PDIAPosterior<T> {
    private PDIAPrior<T> pdia;
    private T[][] data;
    private HashMap<Node<T>,ArrayList<Pair<Integer,Integer>>> counts;
    private double alpha = 1.0; // Dirichlet distribution concentration for data likelihood

    public PDIAPosterior(T[][] t) {
        data = t;
        pdia = new PDIAPrior();
        countAll();
    }

    public void countAll() {
        counts = new HashMap();
        for (int i = 0; i < data.length; i++) {
            Node<T> n = pdia.start();
            for (int j = 0; j < data[i].length; j++) {
                ArrayList<Pair<Integer,Integer>> indices;
                if (counts.containsKey(n)) {
                    indices = counts.get(n);
                } else {
                    indices = new ArrayList();
                    counts.put(n, indices);
                }
                indices.add(new Pair<Integer,Integer>(i,j));
                n = pdia.next(n, data[i][j]);
            }
        }
    }

    public void split(Node<T> n, T[] move) {

    }

    public void merge(Node<T> n1, Node<T> n2) {
        
    }

    public double scoreData() {
        return 0.0;
    }

    public double scorePrior() {
        return pdia.score();
    }

    // Two tests that would be good to do:
    // (1) after resampling for a while, run countAll() and make sure the value in counts doesn't change.
    // (2) build the transition matrix by looking at all the dishes in the restaurant franchise, and compare them to the next HashMap in each Node object
}
