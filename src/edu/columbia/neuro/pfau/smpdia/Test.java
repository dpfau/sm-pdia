/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.neuro.pfau.smpdia;

import java.util.TreeSet;

/**
 *
 * @author pfau
 */
public class Test {
    public static void main(String[] args) {
        int n = 1000000;
        Object[] foo = new Object[2*n];
        TreeSet<Object> bar = new TreeSet();
        for (int i = 0; i < n; i++) {
            foo[i] = new Object();
            foo[i+n] = foo[i];
        }
        for(int i = 0; i < foo.length; i++) {
            bar.add(foo[i]);
        }
        System.out.println(bar.size());
    }
}
