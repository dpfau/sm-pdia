/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.neuro.pfau.smpdia;

/**
 *
 * @author davidpfau
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        try {
//            int[][] data = Util.loadText("/Users/davidpfau/Documents/Wood Group/SM-PDIA/data/AliceInWonderland.txt");
//            Integer[][] dat2 = new Integer[data.length][];
//            for (int i = 0; i < data.length; i++) {
//                dat2[i] = new Integer[data[i].length];
//                for (int j = 0; j < data[i].length; j++) {
//                    dat2[i][j] = data[i][j];
//                }
//            }
//            PDIAPosterior<Integer> pdia = new PDIAPosterior(dat2);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        PDIAPrior<Integer> pdia = new PDIAPrior();
        Node<Integer> nodes[] = new Node[1000000];
        for (int i = 0; i < 10; i++) {
            pdia.franchises.put(i, new Restaurant<Node<Integer>>(pdia.base));
        }
        for (int i = 0; i < 1000000; i++) {
            nodes[i] = new Node<Integer>();
            pdia.franchises.get(i % 10).sampleAndAdd(nodes[i]);
            if (i%1000 == 0) {
                System.out.println(i + " - " + pdia.franchises.get(i % 10).size() + ": " + pdia.franchises.get(i % 10).score());
            }
        }
        for (int i = 0; i < 100000; i++) {
            try {
                pdia.franchises.get(i % 10).remove(nodes[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
