// Local variables test for JSwat.
// This code is used in the unit testing.
// $Author: nfiedler $ $Date: 2002-10-13 00:03:33 -0700 (Sun, 13 Oct 2002) $ $Rev: 604 $

import java.text.DateFormat;
import java.util.*;

/**
 * Code to test local variables display in jswat.
 */
public class locals {
    protected boolean fieldBoolean;
    protected static boolean staticBoolean;
    protected static int staticCounter;
    protected int counter;
    protected String aString;
    protected Class myClass;

    /**
     * Constructor.
     */
    public locals(boolean arg1) {
	fieldBoolean = arg1;
    }

    public locals(boolean arg1, String arg2) {
	this(arg1);
        aString = arg2;
    }

    protected void listtest() {
	List myList = new ArrayList();
	myList.add(new Integer(10));
	myList.add(new Integer(20));
	myList.add(new Integer(30));
	myList.add(new Integer(40));
        // A test for field access.
        if (fieldBoolean) {
            myList.add(new Integer(50));
        }
        for (int ii = 0; ii < 100; ii++) {
            myList.add(new Integer(ii + 100));
        }
        System.out.println("list size = " + myList.size());  // unit test breakpoint here
    }

    protected void maptest() {
        Map myMap = new HashMap();
	myMap.put(new Integer(10), "ten");
	myMap.put(new Integer(20), "twenty");
	myMap.put(new Integer(30), "thirty");
	myMap.put(new Integer(40), "forty");
        myMap.put(new Integer(50), "fifty");
        for (int ii = 0; ii < 10; ii++) {
            myMap.put(new Integer(ii + 100), String.valueOf(ii));
        }
        System.out.println("map size = " + myMap.size()); // unit test breakpoint here
    }

    protected void nulltest() {
        Object o = null;
        String s = null;
        Class c = null;
        o = new Object();
        s = "string";
        c = this.getClass();
        o = null;
        s = null;
        c = null;
        myClass = this.getClass();
        myClass = null;
        Object[] arr = null;
        arr = new Object[3]; // unit test breakpoint here
        arr[0] = new Integer(3);
        arr[1] = new Integer(2);
        arr[2] = new Integer(1);
        arr[2] = null;
        arr[1] = null;
        arr[0] = null;
        System.out.println("nulltest() done");
    }

    protected void test() {
	String o1 = null;
	int counter = 0;
	int[] intarray = new int[10];
	for (int i = 0; i < intarray.length; i++) {
	    intarray[i] = i + 1;
	    this.counter++;
	    counter--;
	}
	o1 = "	A \b string is a \n\n\n\n\n splendid \r thing  ";
	o1 = o1.trim();
	char c = '\n';
	c = 'X';
	c = ' ';
	c = '\b';
        String s = "abc \u2028 stuff \030 more";
        s = "abc \030 more";
        s = "abc \u2028 stuff"; // unit test breakpoint here
        System.out.println("test() done");
    }

    protected void arrayTest() {
        DataHolder[] arr = new DataHolder[20];
        int index = 0;
        arr[index++] = new DataHolder();
        arr[index++] = new DataHolder();
        arr[index] = new DataHolder();
        index++;
        arr[index++] = new DataHolder();
        arr[index++] = new DataHolder();
        arr[--index].ival = 50;
        index--;
        Object[] arr2 = new Object[0];
        arr[index].ival = 40;
        arr[--index].ival = 30;
        arr[--index].ival = 20;
        arr[--index].ival = 10;
    }

    protected void objectTest() {
	Boolean bool = Boolean.TRUE;
	Integer num = new Integer(5);
	Double dbl = new Double(4.12);
	DateFormat format = DateFormat.getDateInstance();
	String hello = "hello world";
	StringBuffer buff = new StringBuffer(hello);
	buff.append(' ');
	buff.append(bool);
        dbl = new Double(1.234);
        bool = Boolean.FALSE;
        num = new Integer(100);
	bool = null;
	num = null;
	dbl = null;

	for (int i = 0; i < 10; i++) {
	    if (i == 5) {
		hello = null;
	    }
	}

        Class cls = this.getClass();
    }

    /**
     * Tester method tests stuff.
     *
     * @param  arg1  argument one.
     */
    protected static void tester(String arg1) {
	staticBoolean = true;
	staticCounter++;
	boolean localBoolean = true;
	int localInt = 5;
	staticCounter++;
	staticBoolean = false;
	float localFloat = 4.12f;
	String localString = "Mary had a little lamb";
	staticCounter++;
    }

    protected String invoke1(String s, char c, int i, boolean b) {
        StringBuffer buf = new StringBuffer(80);
        buf.append(s);
        buf.append(", ");
        buf.append(c);
        buf.append(", ");
        buf.append(i);
        buf.append(", ");
        buf.append(b);
        return buf.toString();
    }

    protected int invoke2(int i) {
        return i * 2;
    }

    protected static int recurse(int i) {
        if (i == 20) {
            return i; // unit tests set line breakpoint here
        }
        return recurse(++i);
    }

    protected int times10(int i) {
        for (int j = 0; j < 10; j++) {
            i += i; // unit tests set line breakpoint here
        }
        return i;
    }

    protected int times20(int i) {
        i = i * 2;
        return times10(i);
    }

    protected int times40(int i) {
        i = i * 2;
        return times20(i);
    }

    protected int times80(int i) {
        i = i * 2;
        return times40(i);
    }

    protected int times160(int i) {
        i = i * 2;
        return times80(i);
    }

    protected int times320(int i) {
        i = i * 2;
        return times160(i);
    }

    protected int times640(int i) {
        i = i * 2;
        return times320(i);
    }

    /**
     * Main method.
     */
    public static void main(String[] args) {
	if (args.length > 0) {
	    tester(args[0]);
	} else {
	    tester("blah blah blah");
	}

        // Force the class to load.
        StaticHolder.i = recurse(1);

	locals me = new locals(false, "do re mi");
        me.times640(1);
	me.test();
	me.objectTest();
	me.listtest();
	me.maptest();
	me.nulltest();
        me.arrayTest();
        // A test for field modification.
	me.fieldBoolean = !me.fieldBoolean;

        embedded emb = new embedded();
        emb.callme(); // unit test breakpoint here

        Runnable runnable = new Runnable() {
                public void run() {
                    int j = 0;
                    for (int ii = 0; ii < 10; ii++) {
                        j += ii;
                    }
                }
            };
        runnable.run();
    };

    protected class DataHolder {
        public int ival;
        public String sval;

        public String toString() {
            return "DataHolder=[" + ival + ", " + sval + ']';
        }
    }
}

/**
 * Can jswat see these values?
 */
class StaticHolder {
    static int i = 10;
    static boolean b = true;
    static String s = "test";
}

/**
 * Can we set a breakpoint here and step through this code?
 */
class embedded {

    public int callme() {
        int j = 0;
        for (int ii = 0; ii < 10; ii++) {
            j += ii;
        }
        return j;
    }

    public static int callme2(int i) {
        int k = 0;
        for (int j = 0; j < i; j++) {
            k += j;
        }
        return k;
    }
}
