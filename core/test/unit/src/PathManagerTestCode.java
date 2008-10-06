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
 * $Id: PathManagerTestCode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

/**
 * Test code for the PathManagerTest.
 *
 * @author  Nathan Fiedler
 */
public class PathManagerTestCode {
    private static String sstr;

    public static void main(String[] args) {
        method1();
        Inner inn = new Inner();
        inn.method_I();
        method2();
        PMSecond.method_PMS();
    }

    private static void method1() {
        sstr = "ABC".substring(0, 1);
    }

    private static void method2() {
        Runnable r = new Runnable() {
            public void run() {
                sstr = System.getProperty("java.home");
            }
        };
        r.run();
    }

    protected static class Inner {
        private String str;

        public void method_I() {
            str = System.getProperty("user.home");
        }
    }
}

class PMSecond {
    private static String str;

    public static void method_PMS() {
        str = System.getProperty("user.dir");
    }
}
