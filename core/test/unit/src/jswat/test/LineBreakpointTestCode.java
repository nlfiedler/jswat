/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: LineBreakpointTestCode.java 6 2007-05-16 07:14:24Z nfiedler $
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
        String s = "ABC".substring(0, 1); // breakpoint, line 35
        Inner1 inn = new Inner1();
        inn.method1();
        method2();
        LBSecond.method1();
    }

    protected static class Inner1 {
        private String str;
        
        public void method1() {
            str = System.getProperty("user.home"); // breakpoint, line 46
        }
    }

    private static void method2() {
        Runnable r = new Runnable() {
            public void run() {
                sstr = System.getProperty("java.home"); // breakpoint, line 53
            }
        };
        r.run();
    }
}

class LBSecond {
    private static String str;

    public static void method1() {
        str = System.getProperty("user.dir"); // breakpoint, line 64
    }
}
