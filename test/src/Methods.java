// JSwat test for finding methods in classes.
// $Author: nfiedler $ $Date: 2002-10-13 00:03:33 -0700 (Sun, 13 Oct 2002) $ $Rev: 604 $

public class Methods {

    public void meth(short s) {
        System.out.println("s = " + s);
    }

    public void meth(Short s) {
        System.out.println("S = " + s);
    }

    public void meth(int i) {
        System.out.println("i = " + i);
    }

    public void meth(Integer i) {
        System.out.println("I = " + i);
    }

    public void meth(long l) {
        System.out.println("l = " + l);
    }

    public void meth(Long l) {
        System.out.println("L = " + l);
    }

    public void print(Object o) {
        System.out.println("o = " + o);
    }

    public void print(String s) {
        System.out.println("s = " + s);
    }

    public static void main(String[] args) {
        Methods me = new Methods();
        me.meth(10);
        me.meth((short) 20);
        me.meth(30L);
        Methods2 me2 = new Methods2();
        me2.meth(40);
        me2.print(null);
        me2.print(new Integer(101));
        me2.print("abc");
    }
}

class Methods2 extends Methods {

    public void meth(int i) {
        super.meth(i);
        System.out.println("j = " + (i + i));
    }
}
