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
 * $Id: LineBreakpointTestCode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package jswat.test;

/**
 * Test code for the LineBreakpointTest.
 *
 * @author  Nathan Fiedler
 */
public class LineBreakpointTestCode {
    private static String sstr;

    public static void main(String[] args) {
        String s = "ABC".substring(0, 1); // breakpoint, line 29
        Inner1 inn = new Inner1();
        inn.method1();
        method2();
        LBSecond.method1();
    }

    protected static class Inner1 {
        private String str;
        
        public void method1() {
            str = System.getProperty("user.home"); // breakpoint, line 40
        }
    }

    private static void method2() {
        Runnable r = new Runnable() {
            public void run() {
                sstr = System.getProperty("java.home"); // breakpoint, line 47
            }
        };
        r.run();
    }
}

class LBSecond {
    private static String str;

    public static void method1() {
        str = System.getProperty("user.dir"); // breakpoint, line 58
    }
}
