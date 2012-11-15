/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.neuro.pfau.crf;

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
        Node current;
        Customer<Node> next;
        for (int i = 0; i < this.data.length; i++) {
            current = start;
            for (int j = 0; j < this.data[i].length; j++) {
                int datum = this.data[i][j];
                assert datum < 0 || datum >= this.nSymbols : "Data is out of range specified by nSymbols";
                next = current.next[datum];
                if (next == null) {
                    crf.sampleAndAdd(next, datum);
                }
                current = next.val;
            }
        }
    }
}
