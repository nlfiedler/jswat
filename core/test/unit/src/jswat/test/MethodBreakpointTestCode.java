/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: MethodBreakpointTestCode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package jswat.test;

/**
 * Test code for the MethodBreakpointTest.
 *
 * @author  Nathan Fiedler
 */
public class MethodBreakpointTestCode {
    private String value;

    private static void method_MBTC() {
        String s = "ABC".substring(0, 1);
    }

    public void method_params(String s, int i, boolean b) {
        value = s + String.valueOf(i) + String.valueOf(b);
    }

    public void method_params(char c, double d) {
        value = String.valueOf(c) + String.valueOf(d);
    }

    public static void main(String[] args) {
        method_MBTC();
        Inner inn = new Inner();
        inn.method_I();
        MBSecond.method_MBS();
        MethodBreakpointTestCode mbtc = new MethodBreakpointTestCode();
        mbtc.method_params("abc", 123, true);
        mbtc.method_params('c', 1.0d);
    }

    protected static class Inner {
        private String str;
        
        public Inner() {
            str = "abcdef";
        }

        // Breakpoint at this method.
        public void method_I() {
            str = str.substring(2, 4);
        }
    }
}

class MBSecond {
    private static String str;

    static {
        str = "12345";
    }

    // Breakpoint at this method.
    public static void method_MBS() {
        str = str.substring(0, 2);
    }
}
