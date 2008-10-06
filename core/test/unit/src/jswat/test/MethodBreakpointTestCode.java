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
 * $Id: MethodBreakpointTestCode.java 6 2007-05-16 07:14:24Z nfiedler $
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
