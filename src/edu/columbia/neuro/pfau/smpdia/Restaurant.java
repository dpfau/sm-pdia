/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.neuro.pfau.smpdia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.apache.commons.math3.special.Gamma;

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
    private double[] pdf;
    private double cumSum;
    private static final int BLOCKSIZE = 32;

    public Restaurant(Distribution d) {
        base = d;
        r = new Random();
        tables = new ArrayList<Table<T>>();
        concentration = 1.0;
        discount = (float) 0.5;

        pdf = new double[BLOCKSIZE];
        pdf[0] = concentration;
        cumSum = concentration;
    }

    public int size() {
        return tables.size();
    }

    public int customers() {
        int i = 0;
        for (Table t : tables) {
            i += t.size;
        }
        return i;
    }

    private int discreteSample() {
        double samp = cumSum * r.nextFloat();
        double cdf = 0.0;
        for (int i = 0; i < size() + 1; i++) {
            cdf += pdf[i];
            if (samp < cdf) {
                return i;
            }
        }
        return size();
    }

    public T sample() {
        int i = discreteSample();
        if (i > tables.size()) {
            return base.sample();
        } else {
            return tables.get(i).getDish();
        }
    }

    public T sampleAndAdd(Customer c) {
        cumSum++;
        int i = discreteSample();
        if (i >= tables.size()) {
            Table t = new Table();
            assign(c,t);
            tables.add(t);
            growPDF();
            return base.sampleAndAdd(t);
        } else {
            assign(c,tables.get(i));
            pdf[i]++;
            return tables.get(i).getDish();
        }
    }

    public void add(Customer c, Table t) {
        cumSum++;
        assign(c,t);
        int i = tables.indexOf(t);
        if (i != -1) {
            pdf[i]++;
        } else {
            tables.add(t);
            growPDF();
        }
    }
    
    public void assign(Customer c, Table t) {
        c.table = t;
        c.custLeft = null;
        c.custRight = t.root;
        if (t.root != null) {
            t.root.custLeft = c;
        }
        t.root = c;
        t.size++;
    }

    private void growPDF() {
        // If necessary, increase the size of pdf, and update with new entry
        if (tables.size() == pdf.length) {
            double[] foo = new double[pdf.length + BLOCKSIZE];
            System.arraycopy(pdf, 0, foo, 0, tables.size());
            pdf = foo;
        }
        if (size() != 0) {
            pdf[size()] = pdf[size() - 1] + discount;
            pdf[size() - 1] = 1 - discount;
        } else {
            pdf[0] = concentration;
        }
    }

    public void remove(Customer c) throws Exception {
        Table t = c.table;
        int i = tables.indexOf(t);
        if (i == -1) {
            throw new Exception("Customer is seated at a table in a different restaurant!");
        } else {
            if (c.table == t) {
                if (c.custLeft == null) {
                    if (t.root == c) {
                        t.root = c.custRight;
                    } else {
                        throw new Exception("Non-root customer has no left neighbor!");
                    }
                } else {
                    if (c.custLeft.table == t) {
                        c.custLeft.custRight = c.custRight;
                    } else {
                        throw new Exception("Customer to the left is seated at a different table!");
                    }
                }
                if (c.custRight != null) {
                    if (c.custRight.table == t) {
                        c.custRight.custLeft = c.custLeft;
                    } else {
                        throw new Exception("Customer to the right is seated at a different table!");
                    }
                }
                t.size--;
                c.custLeft = null;
                c.custRight = null;
            } else {
                throw new Exception("Customer is not seated at this table!");
            }
            pdf[i]--;
            cumSum--;
            if (t.size == 0) {
                tables.remove(i);
                for (int j = i; j < size() + 1; j++) {
                    pdf[j] = pdf[j + 1];
                }
                pdf[size()] -= discount;
                base.remove(t);
            }
        }
    }

    public double score(Customer[] c) {
        // Current method just adds every customer to this restaurant and scores one by one.  Slow?
//        double score = 0.0;
//        ArrayList<Integer> newTables = new ArrayList();
//        for (int i = 0; i < c.length; i++) {
//            if (!tables.contains(c[i].table)) {
//                score += Math.log(pdf[size()]) - Math.log(cumSum);
//                growPDF();
//                tables.add(c[i].table);
//                newTables.add(i);
//            } else {
//                int j = tables.indexOf(c[i].table);
//                score += Math.log(pdf[j]) - Math.log(cumSum);
//                pdf[j]++;
//            }
//            assign(c[i], c[i].table);
//            if (base instanceof Restaurant) {
//                Restaurant r = (Restaurant) base;
//                if (!tables.contains(c[i].table)) {
//                    r.assign(c[i].table,c[i].table.table);
//                }
//            }
//            cumSum++;
//        }
//        for (int i = 0; i < c.length; i++) {
//            try {
//                remove(c[i]);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        Customer[] t = new Customer[newTables.size()];
//        for (int i = 0; i < newTables.size(); i++) {
//            t[i] = c[newTables.get(i)].table;
//        }
//        return score + base.score(t);
        double score = Gamma.logGamma(cumSum) - Gamma.logGamma(cumSum + c.length);
        HashMap<Integer,Integer>  oldTables = new HashMap(); // Count of customers added to existing tables
        HashMap<Table<T>,Integer> newTables = new HashMap(); // Count of customers added to new tables
        for (int i = 0; i < c.length; i++) {
            int idx = tables.indexOf(c[i].table);
            if (idx == -1) {
                if (newTables.containsKey(c[i].table)) {
                    int j = newTables.get(c[i].table);
                    newTables.put(c[i].table, j++);
                } else {
                    newTables.put(c[i].table, 1);
                }
            } else {
                // Still not sure if it's faster to do it this way or just make a big, mostly sparse array the same length as the number of instantiated tables
                if (oldTables.containsKey(idx)) {
                    int j = oldTables.get(idx);
                    oldTables.put(idx, j++);
                } else {
                    oldTables.put(idx, 1);
                }
            }
        }
        for (Integer i: oldTables.keySet()) {
            score += Gamma.logGamma(oldTables.get(i) + pdf[i]) 
                   - Gamma.logGamma(pdf[i]);
        }
        for (Table<T> t: newTables.keySet()) {
            score += Gamma.logGamma(newTables.get(t) - discount);
        }
        if (discount != 0) {
            score += newTables.size()*Math.log(discount)
                  + Gamma.logGamma(concentration/discount + tables.size() + newTables.size())
                  - Gamma.logGamma(concentration/discount + tables.size())
                  - newTables.size()*Gamma.logGamma(1 - discount);
        } else {
            score += newTables.size()*Gamma.logGamma(concentration);
        }
        score += base.score((Customer[])(newTables.keySet().toArray(new Customer[newTables.size()])));
        return score;
    }

    // Return the log likelihood of the restaurant's configuration
    public double score() {
        double score = Gamma.logGamma(concentration) - Gamma.logGamma(cumSum);
        if (discount != 0) {
            score += tables.size()*Math.log(discount)
                  + Gamma.logGamma(concentration/discount + tables.size())
                  - Gamma.logGamma(concentration/discount)
                  - tables.size()*Gamma.logGamma(1 - discount);
        } else {
            score += tables.size()*Math.log(concentration);
        }
        for (int i = 0; i < size(); i++) {
            score += Gamma.logGamma(pdf[i]);
        }
        return score;
    }

    // Check that the fields pdf and cumSum are consistent.
    public boolean testPDF() {
        double cumSum2 = 0.0;
        for (int i = 0; i < size() + 1; i++) {
            cumSum2+= pdf[i];
        }
        if (cumSum != cumSum2) {
            return false;
        }
        for (int i = 0; i < size(); i++ ) {
            if (pdf[i] != tables.get(i).size - discount) {
                return false;
            }
        }
        if (pdf[size()] != concentration + discount*size() ) {
            return false;
        }
        return true;
    }
}
