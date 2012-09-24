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
    private Customer root = null;
    private int size;
    private T dish;

    public Table() {
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

    public Customer root() {
        return root;
    }

    public void add(Customer c) {
        c.table = this;
        c.custLeft = null;
        c.custRight = root;
        if (root != null) {
            root.custLeft = c;
        }
        root = c;
        size++;
    }

    public void remove(Customer c) throws Exception {
        if (c.table == this) {
            if (c.custLeft == null) {
                if (root == c) {
                    root = c.custRight;
                } else {
                    throw new Exception("Non-root customer has no left neighbor!");
                }
            } else {
                if (c.custLeft.table == this) {
                    c.custLeft.custRight = c.custRight;
                } else {
                    throw new Exception("Customer to the left is seated at a different table!");
                }
            }
            if (c.custRight != null) {
                if (c.custRight.table == this) {
                    c.custRight.custLeft = c.custLeft;
                } else {
                    throw new Exception("Customer to the right is seated at a different table!");
                }
            }
            size--;
            c.custLeft  = null;
            c.custRight = null;
        } else {
            throw new Exception("Customer is not seated at this table!");
        }
    }
}
