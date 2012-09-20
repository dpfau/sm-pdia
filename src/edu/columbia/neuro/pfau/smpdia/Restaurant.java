/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.neuro.pfau.smpdia;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author davidpfau
 */
public class Restaurant<T> extends Distribution<T> {
    public ArrayList<Table<T>> tables;
    private Distribution<T> base;
    private double concentration;
    private float discount;
    private Random r;

    public Restaurant(Distribution d) {
        base = d;
        r = new Random();
        tables = new ArrayList<Table<T>>();
        concentration = 1.0;
        discount = (float)0.5;
    }

    private int discreteSample(double[] cdf) {
        double samp = cdf[cdf.length-1] * r.nextFloat();
        for (int i = 0; i < cdf.length; i++) {
            if (samp < cdf[i]) {
                return i;
            }
        }
        return cdf.length;
    }

    public T sample() {
        double[] cdf = new double[tables.size() + 1];
        double total = 0.0;
        for (int i = 0; i < tables.size(); i++) {
            total += tables.get(i).size() - discount;
            cdf[i] = total;
        }
        cdf[tables.size()] = total + concentration + tables.size()*discount;
        int i = discreteSample(cdf);
        if (i > tables.size()) {
            return base.sample();
        } else {
            return tables.get(i).getDish();
        }
    }

    public T sampleAndAdd(Customer c) {
        double[] cdf = new double[tables.size() + 1];
        double total = 0.0;
        for (int i = 0; i < tables.size(); i++) {
            total += tables.get(i).size() - discount;
            cdf[i] = total;
        }
        cdf[tables.size()] = total + concentration + tables.size()*discount;
        int i = discreteSample(cdf);
        if (i > tables.size()) {
            Table t = new Table();
            t.seat(c);
            return base.sampleAndAdd(t);
        } else {
            tables.get(i).seat(c);
            return tables.get(i).getDish();
        }
    }

    public double score(Customer[] c) {
        return base.score(c);
    }

    // Return the log likelihood of the restaurant's configuration
    public double score() {
        return 0.0;
    }
}
