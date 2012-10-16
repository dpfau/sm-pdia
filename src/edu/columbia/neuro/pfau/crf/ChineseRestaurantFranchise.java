/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.neuro.pfau.crf;

import edu.columbia.neuro.pfau.smpdia.Distribution;
import edu.columbia.neuro.pfau.smpdia.Uniform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.apache.commons.math3.special.Gamma;

/**
 *
 * @author pfau
 */
public class ChineseRestaurantFranchise<T> {
    public int numFranchise;
    public float[] discounts;
    public double[] concentrations;
    public int[] N; // Number of customers in each restaurant
    public Distribution<T> base;
    private Random r = new Random();
    // private double[][] pdf; // For faster sampling, don't have to construct these on the fly
    
    // The following data structures are two ways of pointing to the same ArrayList<Customer<T>> objects
    public ArrayList<ArrayList<Customer<T>>>[] franchise; // Useful for tracking counts within a restaurant
    public ArrayList<T> dishes;
    public HashMap<T,ArrayList<ArrayList<Customer<T>>>> dishMap; // Useful for tracking all tables serving the same dish
    
    public ChineseRestaurantFranchise(int n, Distribution base) {
        this.base = base;
        numFranchise = n;
        discounts = new float[n+1];
        concentrations = new double[n+1];
        for (int i = 0; i < n+1; i++) {
            discounts[i] = (float)0.5;
            concentrations[i] = 1;
        }
        
        franchise = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            franchise[i] = new ArrayList<ArrayList<Customer<T>>>();
        }
        dishes = new ArrayList();
        dishMap = new HashMap();
        N = new int[n+1]; 
    }
    
    private int discreteSample(double[] pdf, double cumSum) {
        double samp = cumSum * r.nextFloat();
        double cdf = 0.0;
        for (int i = 0; i < pdf.length; i++) {
            cdf += pdf[i];
            if (samp < cdf) {
                return i;
            }
        }
        return pdf.length;
    }
    
    public T sample(int n) {
        if (n == -1) {
            double pdf[] = new double[dishMap.size()];
            for (int i = 0; i < dishes.size(); i++) {
                pdf[i] = dishMap.get(dishes.get(i)).size() - discounts[0];
            }
            int samp = discreteSample(pdf, concentrations[0] + N[0]);
            if (samp == dishes.size()) {
                return base.sample();
            } else {
                return dishes.get(samp);
            }
        } else {
            double pdf[] = new double[franchise[n].size()];
            for(int i = 0; i < franchise[n].size(); i++) {
                pdf[i] = franchise[n].get(i).size() - discounts[n+1];
            }
            int samp = discreteSample(pdf, concentrations[n+1] + N[n+1]);
            if (samp == franchise[n].size()) {
                return sample(0);
            } else {
                return franchise[n].get(samp).get(0).val;
            }
        }
    }
    
    // Sample lower-level restaurant
    public ArrayList<Customer<T>> sampleAndAdd(Customer<T> c, int n) {
        N[n + 1]++;
        double pdf[] = new double[franchise[n].size()];
        for (int i = 0; i < franchise[n].size(); i++) {
            pdf[i] = franchise[n].get(i).size() - discounts[n + 1];
        }
        int samp = discreteSample(pdf, concentrations[n + 1] + N[n + 1] - 1);
        ArrayList<Customer<T>> t;
        if (samp == franchise[n].size()) {
            t = new ArrayList<Customer<T>>();
            c.val = sampleAndAdd(t);
            t.add(c);
            franchise[n].add(t);
        } else {
            t = franchise[n].get(samp);
            c.val = t.get(0).val;
            t.add(c);
        }
        return t;
    }
    
    // Sample top-level restaurant
    public T sampleAndAdd(ArrayList<Customer<T>> t) {
        N[0]++;
        double pdf[] = new double[dishMap.size()];
        for (int i = 0; i < dishes.size(); i++) {
            pdf[i] = dishMap.get(dishes.get(i)).size() - discounts[0];
        }
        int samp = discreteSample(pdf, concentrations[0] + N[0] - 1);
        T sample;
        if (samp == dishes.size()) {
            sample = base.sample();
            dishes.add(sample);
            dishMap.put(sample, new ArrayList<ArrayList<Customer<T>>>());
        } else {
            sample = dishes.get(samp);
        }
        ArrayList<ArrayList<Customer<T>>> tables = dishMap.get(sample);
        tables.add(t);
        return sample;
    }
    
    public void remove(Customer<T> c, ArrayList<Customer<T>> t, int n) throws Exception {
        int idx = t.indexOf(c);
        if (idx == -1) {
            throw new Exception("Customer not seated at this table");
        } else {
            N[n+1]--;
            t.remove(idx);
            if (t.isEmpty()) {
                N[0]--;
                franchise[n].remove(t);
                ArrayList<ArrayList<Customer<T>>> tables = dishMap.get(c.val);
                tables.remove(t);
                if (tables.isEmpty()) {
                    dishMap.remove(c.val);
                    dishes.remove(c.val);
                }
            }
        }
    }
    
    public double score() {
        double score = Gamma.logGamma(concentrations[0]) - Gamma.logGamma(concentrations[0] + N[0]);
        if (discounts[0] != 0) {
            score += dishes.size() * Math.log(discounts[0])
                  +  Gamma.logGamma(concentrations[0] / discounts[0] + dishes.size())
                  -  Gamma.logGamma(concentrations[0] / discounts[0])
                  - dishes.size() * Gamma.logGamma(1 - discounts[0]);
        } else {
            score += dishes.size() * Math.log(concentrations[0]);
        }
        for (Object t: dishMap.keySet()) {
            score += Gamma.logGamma(((ArrayList)dishMap.get(t)).size() - discounts[0]);
        }
        
        for (int i = 0; i < numFranchise; i++) {
            int size = franchise[i].size();
            score += Gamma.logGamma(concentrations[i+1]) - Gamma.logGamma(concentrations[i+1] + N[i+1]);
            if (discounts[i+1] != 0) {
                score += size * Math.log(discounts[i+1])
                      + Gamma.logGamma(concentrations[i+1] / discounts[i+1] + size)
                      - Gamma.logGamma(concentrations[i+1] / discounts[i+1])
                      - size * Gamma.logGamma(1 - discounts[i+1]);
            } else {
                score += size * Math.log(concentrations[i+1]);
            }
            for (int j = 0; j < size; j++) {
                score += Gamma.logGamma(franchise[i].get(j).size() - discounts[i+1]);
            }
        }
        return score;
    }
    
    public double score(Customer<T>[] c, ArrayList<Customer<T>>[] t, int[] n) {
        double score = 0.0;
        assert(c.length == t.length);
        assert(c.length == n.length);
        HashMap<T,Integer>       newDishes = new HashMap();
        HashMap<Integer,Integer> oldDishes = new HashMap();
        
        HashMap<ArrayList<Customer<T>>, Integer>[] newTables = new HashMap[numFranchise];
        HashMap<Integer,Integer>[]                 oldTables = new HashMap[numFranchise];
        for (int i = 0; i < numFranchise; i++) {
            newTables[i] = new HashMap();
            oldTables[i] = new HashMap();
        }
        for (int i = 0; i < c.length; i++) {
            int idx = franchise[n[i]].indexOf(t[i]);
            if (idx != -1) {
                incHash(oldTables[n[i]], idx);
            } else {
                incHash(newTables[n[i]], t[i]);
                idx = dishes.indexOf(c[i].val);
                if (idx != -1) {
                    incHash(oldDishes, idx);
                } else {
                    incHash(newDishes, c[i].val);
                }
            }
        }
        // Compute the actual score here.
        return score;
    }
    
    private <E> void incHash(HashMap<E,Integer> hm, E key) {
        if (hm.containsKey(key)) {
            Integer ct = hm.get(key);
            hm.put(key, ct++);
        } else {
            hm.put(key, 1);
        }
    }
    
    public static void main(String args[]) {
        ChineseRestaurantFranchise crf = new ChineseRestaurantFranchise<Float>(10, new Uniform());
        Customer[] cust = new Customer[1000000];
        int[] franch = new int[1000000];
        ArrayList<Customer<Float>>[] tables = new ArrayList[1000000]; 
        for(int i = 0; i < 1000000; i++) {
            cust[i] = new Customer();
            tables[i] = crf.sampleAndAdd(cust[i],i%10);
            franch[i] = i%10;
        }
//        for (int i = 0; i < crf.N.length; i++) {
//            System.out.println(crf.N[i]);
//        }
//        System.out.println(crf.dishes.size());
//        int ntable = 0;
//        int ntable1 = 0;
//        for (int i = 0; i < 10; i++) {
//            ntable  += crf.franchise[i].size();
//        }
//        for (Object t: crf.dishMap.keySet()) {
//            ntable1 += ((ArrayList)crf.dishMap.get(t)).size();
//        }
//        System.out.println(ntable);
//        System.out.println(ntable1);
        System.out.println(crf.score());
        for (int i = 0; i < 1000000; i++) {
            try {
                crf.remove(cust[i],tables[i],i%10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(crf.score(cust,tables,franch));
        
//        ntable = 0;
//        ntable1 = 0;
//        for (int i = 0; i < 10; i++) {
//            ntable  += crf.franchise[i].size();
//        }
//        for (Object t: crf.dishMap.keySet()) {
//            ntable1 += ((ArrayList)crf.dishMap.get(t)).size();
//        }
//        System.out.println(ntable);
//        System.out.println(ntable1);
    }
}
