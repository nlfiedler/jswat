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
 * are Copyright (C) 2004-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
            @Override
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
