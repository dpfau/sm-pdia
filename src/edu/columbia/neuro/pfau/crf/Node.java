/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.neuro.pfau.crf;

import java.util.HashMap;

/**
 *
 * @author pfau
 */
public class Node {
    public Customer<Node>[] next;
    public Table<Node>[] tables;
    public int[] count;
    public HashMap<Node,Integer> back;
    
    public Node(int n) {
        next = new Customer[n];
        tables = new Table[n];
        count = new int[n];
        back = new HashMap();
    }
}
