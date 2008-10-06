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
 * $Id: WatchBreakpointTestCode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

/**
 * Test code for the WatchBreakpointTest.
 *
 * @author  Nathan Fiedler
 */
public class WatchBreakpointTestCode {
    private static int var_i;

    private static int getI() {
        return var_i;
    }

    private static void setI(int i) {
        var_i = i;
    }

    public static void main(String[] args) {
        int i = (int) Math.random() * 100;
        setI(i);
        int j = getI();
        System.out.println("j = " + j);
    }
}
