/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.neuro.pfau.smpdia;

/**
 *
 * @author davidpfau
 */
public class NodeDist<D> extends Distribution<Node<D>> {
    public Node<D> sample() {
        return new Node<D>();
    }

    public Node<D> sampleAndAdd(Customer c) {
        return new Node<D>();
    }

    public void remove(Customer c) {}

    public double score(Customer[] c) {
        return 0.0;
    }
}
