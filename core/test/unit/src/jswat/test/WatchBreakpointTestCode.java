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
 * $Id: WatchBreakpointTestCode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package jswat.test;

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
