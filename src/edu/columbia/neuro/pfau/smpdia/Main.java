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
        }
        double score1 = pdia.base.score();
        for (int i = 0; i < 10; i++) {
            score1 += pdia.franchises.get(i).score();
        }
        System.out.println(score1);
        Node<Integer> removed[] = new Node[100000];
        for (int i = 0; i < 100000; i++) {
            try {
                pdia.franchises.get(0).remove(nodes[i*10]);
                removed[i] = nodes[i*10];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        double score2 = pdia.base.score();
        for (int i = 0; i < 10; i++) {
            score2 += pdia.franchises.get(i).score();
        }
        System.out.println(score2 + pdia.franchises.get(0).score(removed));
        for (int i = 0; i < 100000; i++) {
            pdia.franchises.get(0).add(removed[i], removed[i].table);
            for (Table t: pdia.franchises.get(0).tables) {
                pdia.base.add(t, t.table);
            }
        }
        for (int i = 0; i < 100000; i++) {
            try {
                pdia.franchises.get(0).remove(removed[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        score2 = pdia.base.score();
        for (int i = 0; i < 10; i++) {
            score2 += pdia.franchises.get(i).score();
        }
        System.out.println(score2 + pdia.franchises.get(0).score(removed));
    }

}
