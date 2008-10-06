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
 * $Id: SourceNameTestCode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package jswat.testa;

/**
 * Test code for the LineBreakpointTest.
 *
 * @author  Nathan Fiedler
 */
public class SourceNameTestCode {

    public static void packageA() {
        System.out.println(); // line breakpoint 28
    }

    public static void main(String[] args) {
        packageA();
        // Invoke a method in another class whose source name is the same,
        // the line breakpoint should only hit one of these two.
        jswat.test.SourceNameTestCode.packageB();
    }
}
