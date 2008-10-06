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
 * $Id: ClassBreakpointTestCode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package jswat.test;

/**
 * Test code for the ClassBreakpointTest.
 *
 * @author Nathan Fiedler
 */
public class ClassBreakpointTestCode {

    public static void main(String[] args) {
        loadCBSecond();
    }
    
    private static void loadCBSecond() {
        CBSecond.method();
    }
}

class CBSecond {

    public static String method() {
        return "abc";
    }
}
