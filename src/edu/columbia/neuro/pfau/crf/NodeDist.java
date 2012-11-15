/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.neuro.pfau.crf;

/**
 *
 * @author pfau
 */
public class NodeDist extends Distribution<Node> {
    int n;
    public NodeDist(int n) {
        this.n = n;
    }
    
    public Node sample() {
        return new Node(this.n);
    }

    public double score(Node t) {
        return 0.0;
    }
}
