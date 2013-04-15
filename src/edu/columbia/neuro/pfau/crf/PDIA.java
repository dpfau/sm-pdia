/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.neuro.pfau.crf;

import java.util.Random;

/**
 *
 * @author pfau
 */
public class PDIA {
    public ChineseRestaurantFranchise<Node> crf;
    int[][] data;
    int nSymbols;
    Node start;
    
    public PDIA(int[][] data, int nSymbols) {
        this.data = data;
        this.nSymbols = nSymbols;
        start = new Node(nSymbols);
        crf = new ChineseRestaurantFranchise(this.nSymbols, new NodeDist(this.nSymbols));
        for (int i = 0; i < this.data.length; i++) {
            for (int j = 0; j < this.data[i].length; j++) {
                int datum = this.data[i][j];
                assert datum < 0 || datum >= this.nSymbols : "Data is out of range specified by nSymbols";
            }
        }
    }
    
    public void run() {
        start.count = new int[nSymbols];
        for (Node n: crf.dishes) {
            n.count = new int[nSymbols];
        }
        Node current;
        Customer<Node> next;
        for (int i = 0; i < this.data.length; i++) {
            current = start;
            for (int j = 0; j < this.data[i].length; j++) {
                int datum = this.data[i][j];
                current.count[datum]++;
                next = current.next[datum];
                if (next == null) {
                    next = new Customer<Node>();
                    current.next[datum] = next;
                    current.tables[datum] = crf.sampleAndAdd(next, datum);
                }
                next.val.back.put(current, datum);
                current = next.val;
            }
        }    
    }
    
    public void merge(Node a, Node b) {
        assert crf.dishes.contains(a) : "Node not found in PDIA";
        assert crf.dishes.contains(b) : "Node not found in PDIA";
        for (int i = 0; i < nSymbols; i++) {
            a.count[i] += b.count[i];
            if (a.next[i] == null) {
                a.next[i] = b.next[i];
            } else {
                if (b.next[i] != null) {
                    if (crf.r.nextBoolean()) {
                        merge(b.next[i].val, a.next[i].val);
                    } else {
                        merge(a.next[i].val, b.next[i].val);
                    }
                }
            }
        }
    }
    
    public void split(Node a, Node b) {
        assert crf.dishes.contains(a)  : "Node not found in PDIA";
        assert !crf.dishes.contains(b) : "Cannot split into an existing node";
    }
    
    public static void main(String[] args) {
        Random r = new Random();
        int n = 10000;
        int m = 1000;
        int s = 20;
        int[][] data = new int[n][];
        for (int i = 0; i < n; i++) {
            data[i] = new int[n];
            for (int j = 0; j < m; j++) {
                data[i][j] = r.nextInt(s);
            }
        }
        
        PDIA p = new PDIA(data, s);
        p.run();
        System.out.println(p.crf.score());
        System.out.println(p.crf.dishes.size());
        p.run();
        p.crf.score();
        System.out.println("foo");
        p.run();
        p.crf.score();
        System.out.println("foo");
        p.run();
        p.crf.score();
        System.out.println("foo");
        p.run();
        p.crf.score();
        System.out.println("foo");
        p.run();
        p.crf.score();
        System.out.println("foo");
    }
}
