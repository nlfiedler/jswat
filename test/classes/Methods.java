// JSwat test for finding methods in classes.
// $Id: Methods.java 14 2007-06-02 23:50:55Z nfiedler $

public class Methods {

    public void print(Byte b) {
        System.out.println("B = " + b);
    }

    public void print(short s) {
        System.out.println("s = " + s);
    }

    public void print(Short s) {
        System.out.println("S = " + s);
    }

    public void print(int i) {
        System.out.println("i = " + i);
    }

    public void print(Integer i) {
        System.out.println("I = " + i);
    }

    public void print(long l) {
        System.out.println("l = " + l);
    }

    public void print(Long l) {
        System.out.println("L = " + l);
    }

    public void print(Object o) {
        System.out.println("o = " + o);
    }

    public void print(String s) {
        System.out.println("z = " + s);
    }

    public String toString() {
        return "me";
    }

    public static void main(String[] args) {
        Methods me = new Methods();
        // Note how this calls the short method, rather than Byte.
        me.print((byte) 5);
        // Integer method is the default.
        me.print(10);
        // Must typecast to get non-integer method.
        me.print((short) 20);
        // Longs can be specified with the L suffix.
        me.print(30L);
        // Again, have to typecast to get non-integer.
        me.print(new Short((short) 5));
        me.print(me);
        Methods2 me2 = new Methods2();
        me2.print(40);
        me2.print(50L);
        // Have to typecast null to a specific type.
        me2.print((String) null);
        me2.print(new Integer(101));
        me2.print("abc");
        me2.print(me2);
    }
}
