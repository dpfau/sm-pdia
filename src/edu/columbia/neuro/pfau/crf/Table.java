/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.columbia.neuro.pfau.crf;

import java.util.ArrayList;
import java.util.UUID;

/**
 *
 * @author pfau
 */
public class Table<T> {
    public final UUID id;
    private ArrayList<Customer<T>> val;
    public Table(UUID id) {
        this.id = id;
        val = new ArrayList<Customer<T>>();
    }
    
    public int size() {
        return val.size();
    }
    
    public void add(Customer<T> c) {
        val.add(c);
    }
    
    public Customer<T> get(int i) {
        return val.get(i);
    }
    
    public int indexOf(Customer<T> c) {
        return val.indexOf(c);
    }
    
    public boolean isEmpty() {
        return val.isEmpty();
    }
    
    public Customer<T> remove(int i) {
        return val.remove(i);
    }
}
