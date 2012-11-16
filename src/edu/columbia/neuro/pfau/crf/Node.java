/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.neuro.pfau.crf;

/**
 *
 * @author pfau
 */
public class Node {
    public Customer<Node>[] next;
    public int[] count;
    
    public Node(int n) {
        next = new Customer[n];
        count = new int[n];
    }
}
