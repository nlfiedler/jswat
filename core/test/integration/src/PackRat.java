/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: PackRat.java 15 2007-06-03 00:01:17Z nfiedler $
 */

import java.awt.Point;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Code that has numerous fields of various types, and methods with various
 * local variables -- useful for testing the Variables view.
 *
 * @author  Nathan Fiedler
 */
public class PackRat {
    // static members
    private static boolean static_boolean;
    private static byte static_byte;
    private static char static_char;
    private static double static_double;
    private static float static_float;
    private static int static_int;
    private static long static_long;
    private static short static_short;
    private static String static_String;
    private static String[] static_strarr;
    private static char[] static_char_array;
    private static final int STATIC_CONST_INT = 5;
    private static Point static_Point;
    private static Class static_Class = PackRat.class;
    // instance members
    private boolean inst_boolean;
    private byte inst_byte;
    private char inst_char;
    private double inst_double;
    private float inst_float;
    private int inst_int;
    private long inst_long;
    private short inst_short;
    private String inst_String;
    private String[] inst_strarr;
    private char[] inst_char_array;
    private String[] inst_strarr_empty;
    private Object inst_String_as_Obj;
    private Object[] inst_strarr_as_Obj;
    private Object inst_array_as_Obj;
    private final int INST_CONST_INT = 10;
    private Point inst_Point;

    static {
        static_char_array = new char[] { 'a', 'b', 'c', '1', '2', '3' };
        static_strarr = new String[] { "abc", "123", "!@#" };
    }

    /**
     * Tests the creation and assignment of arrays.
     */
    protected void array_test() {
        Point[] arr = new Point[20];
        int index = 0;
        arr[index++] = new Point();
        arr[index++] = new Point();
        arr[index] = new Point();
        index++;
        arr[index++] = new Point();
        arr[index++] = new Point();
        arr[--index].x = 50;
        index--;
        arr[index].x = 40;
        arr[index].y = 100;
        arr[--index].x = 30;
        arr[--index].x = 20;
        arr[--index].x = 10;
        arr = null;

        // Use an odd size to ensure view handles it properly.
        int[] intarr = new int[401];
        for (int ii = 0; ii < intarr.length; ii++) {
            intarr[ii] = ii;
        }
        // Now zero out some of them to watch them change.
        for (int ii = intarr.length - 1; ii >= 0; ii -= 2) {
            intarr[ii] = 0;
        }
        intarr = null;

        // Array of objects, where some are null.
        Object[] objarr = new Object[20];
        for (int ii = 0; ii < objarr.length; ii += 2) {
            if (ii % 4 == 0) {
                objarr[ii] = new Long(ii);
            } else {
                objarr[ii] = new Integer(ii);
            }
        }
        objarr = null;

        // Two dimensional array.
        int[][] twodim = new int[13][13];
        for (int ii = 1; ii < 13; ii++) {
            for (int jj = 1; jj < 13; jj++) {
                twodim[ii][jj] = ii * jj;
            }
        }
        twodim = null;
    }

    /**
     * Tests field access.
     */
    protected void instance_test() {
        inst_array_as_Obj = new char[] { 'A', 'B', 'C' };
        inst_boolean = true;
        inst_byte = 1;
        inst_char = 'a';
        inst_char_array = new char[] { 'a', 'b', 'c', '\n', '1', '2', '3' };
        inst_double = 1.2345;
        inst_float = 3.14F;
        inst_int = 1;
        inst_long = 12345890L;
        inst_short = 10;
        inst_strarr = new String[] { "abc", "123", "!@#" };
        inst_strarr_empty = new String[0];
        inst_Point = new Point(10, 20);
        inst_String = "Abc123";
        inst_String_as_Obj = "an object string";

        inst_array_as_Obj = null;
        inst_boolean = false;
        inst_byte = 0;
        inst_char = 0;
        inst_char_array = null;
        inst_double = 0.0;
        inst_float = 0.0F;
        inst_int = 0;
        inst_long = 0L;
        inst_short = 0;
        inst_strarr = null;
        inst_strarr_empty = null;
        inst_Point = null;
        inst_String = null;
        inst_String_as_Obj = null;
    }

    /**
     * Tests using only local variables.
     */
    protected void local_test() {
        Boolean bool = Boolean.TRUE;
        Integer num = new Integer(1973);
        Double dbl = new Double(3.14);
        String hello = "hello world";
        StringBuffer buff = new StringBuffer(hello);
        buff.append(' ');
        buff.append(bool);
        dbl = new Double(1.234);
        buff.append(dbl);
        bool = Boolean.FALSE;
        num = new Integer(100);
        buff.append(num);
        bool = null;
        num = null;
        dbl = null;
        StringBuilder sb = new StringBuilder("abc123");
        String s = sb.toString();
        sb = null;
        BitSet bits = new BitSet(150);
        for (int i = 0; i < 150; i += 3) {
            bits.set(i);
        }
        s = bits.toString();
        bits = null;
    }

    /**
     * Tests with a variable that is always changing.
     */
    protected void loop_test() {
        int j = 0;
        for (int i = 1; i <= 20; i++) {
            j += i;
        }
    }

    /**
     * Tests how well null values are handled.
     */
    protected void null_test() {
        Object o = null;
        String s = null;
        Class c = null;
        o = new Object();
        s = "a string";
        c = this.getClass();
        o = null;
        s = null;
        c = null;
        Object[] arr = null;
        arr = new Object[3];
        arr[0] = new Integer(3);
        arr[1] = new Integer(2);
        arr[2] = new Integer(1);
        arr[2] = null;
        arr[1] = null;
        arr[0] = null;
        arr = null;
    }

    private void params_test(String s, char c, int i, boolean b) {
        System.out.println("s = " + s);
        System.out.println("c = " + c);
        System.out.println("i = " + i);
        System.out.println("b = " + b);
    }

    /**
     * Tests the static condition.
     */
    protected static void static_test() {
        static_boolean = true;
        static_byte = 1;
        static_char = 'a';
        static_double = 1.2345;
        static_float = 3.14F;
        static_int = 1;
        static_long = 123456890L;
        static_short = 123;
        static_Point = new Point(10, 20);
        static_String = "static string";

        static_boolean = false;
        static_byte = 0;
        static_char = 0;
        static_double = 0.0;
        static_float = 0.0F;
        static_int = 0;
        static_long = 0L;
        static_short = 0;
        static_Point = null;
        static_String = null;
    }

    private void synthetic_test(final long l) {
        final int i = 10;
        final String s = "synth";
        Runnable r = new Runnable() {
            public void run() {
                // Should see the i and s fields but for some reason they
                // are not in any of the field lists.
                // Oddly enough, the parameter 'l' appears as "this.val$l".
                int j = i / 2;
                long k = l - 10;
                String t = s + '/' + j + ':' + k;
            }
        };
        r.run();
    }

    private void type_test() {
        // What is the type shown for these values, the declared or actual?
        List<Integer> list = new ArrayList<Integer>();
        for (int ii = 0; ii < 10; ii++) {
            list.add(new Integer(ii));
            list.add(new Integer(ii));
        }
        Set<Integer> set = new HashSet<Integer>();
        Iterator<Integer> iter = list.iterator();
        while (iter.hasNext()) {
            set.add(iter.next());
        }
        if (set.size() != 10) {
            throw new IllegalStateException("set didn't work");
        }
    }

    /**
     * Entry point of this application.
     *
     * @param  args  command line arguments.
     */
    public static void main(String[] args) {
        PackRat me = new PackRat();
        me.array_test();
        me.instance_test();
        me.local_test();
        me.loop_test();
        me.null_test();
        me.params_test("abc", '1', 2, false);
        me.synthetic_test(1234567890L);
        me.type_test();
        static_test();
    };
}
