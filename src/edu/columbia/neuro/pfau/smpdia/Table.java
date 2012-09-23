/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.columbia.neuro.pfau.smpdia;

/**
 *
 * @author davidpfau
 */
public class Table<T> extends Customer<T> {
    private static final int blocksize = 64; // Allocate seats at the table in blocks, to avoid unnecessary memory allocation.
    private Customer[] customers;
    private int size;
    private T dish;

    public Table() {
        customers = new Customer[blocksize];
        size = 0;
    }

    public void setDish(T d) {
        dish = d;
    }

    public T getDish() {
        return dish;
    }

    public int size() {
        return size;
    }

    public void add(Customer c) {
        if (size == customers.length) {
            Customer[] foo = new Customer[size+blocksize];
            System.arraycopy(customers,0,foo,0,size);
            customers = foo;
        }
        c.add(this, size);
        customers[size] = c;
        size++;
    }

    public void remove(Customer c) throws Exception {
        if (this == c.table()) {
            if (c.seat >= 0 && c.seat < size) {
                if (customers[c.seat] == c) {
                    size--;
                    for (int i = c.seat; i < size; i++) {
                        customers[i] = customers[i+1];
                        customers[i].seat--;
                    }
                    customers[size] = null;
                } else {
                    throw new Exception("Customer is not seated at this table, though it believes it is.");
                }
            } else {
                throw new Exception("Customer believes it is seated out of bounds.");
            }
        } else {
            throw new Exception("Customer does not believe it is seated at this table.");
        }
    }
}
