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
 * $Id: ExceptionBreakpointTestCode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

/**
 * Test code for the ExceptionBreakpointTest.
 *
 * @author Nathan Fiedler
 */
public class ExceptionBreakpointTestCode {

    public static void main(String[] args) {
        throwIllArg();
        throwNullPt();
        throwIndexBounds();
    }

    private static void throwIllArg() {
        try {
            throw new IllegalArgumentException();
        } catch (Exception e) {
            // do nothing and continue on with the test
        }
    }

    private static void throwNullPt() {
        try {
            throw new NullPointerException();
        } catch (Exception e) {
            // do nothing and continue on with the test
        }
    }
    
    private static void throwIndexBounds() {
        try {
            throw new IndexOutOfBoundsException();
        } catch (Exception e) {
            // do nothing and continue on with the test
        }
    }
}
