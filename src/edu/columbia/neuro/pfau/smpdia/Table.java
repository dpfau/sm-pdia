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
    private Customer[] customers;
    private T dish;

    public Table() {
        customers = new Customer[0];
    }

    public void setDish(T d) {
        dish = d;
    }

    public T getDish() {
        return dish;
    }

    public int size() {
        return customers.length;
    }

    public void seat(Customer c) {
        Customer[] foo = new Customer[customers.length+1];
        System.arraycopy(customers,0,foo,0,customers.length);
        foo[customers.length] = c;
        c.t = this;
        c.seat = customers.length;
        System.out.println(foo.length);
        customers = foo;
    }

    public void unseat(Customer c) throws Exception {
        if (c.t == this) {
            if (c.seat >= 0 && c.seat < customers.length) {
                if (customers[c.seat] == c) {
                    Customer[] foo = new Customer[customers.length - 1];
                    // System.out.println(customers.length-1);
                    // System.out.println(c.seat);
                    System.arraycopy(customers, 0, foo, 0, c.seat - 1);
                    System.arraycopy(customers, c.seat + 1, foo, c.seat, customers.length - (c.seat + 1));
                    customers = foo;
                    for (int i = c.seat; i < customers.length; i++) {
                        customers[i].seat--;
                    }
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
