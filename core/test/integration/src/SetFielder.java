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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

/**
 * Code that sets several different fields in order to test the watch
 * breakpoint from within the debugger.
 *
 * @author Nathan Fiedler
 */
public class SetFielder {
    private static double static_rand;
    private double inst_rand;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SetFielder sf = new SetFielder();
        // Modify and access the instance field.
        sf.inst_rand = Math.random();
        if (sf.inst_rand > 0.5d) {
            System.out.println("Odds");
        } else {
            System.out.println("Evens");
        }
        // Modify and access the static field.
        static_rand = Math.random();
        if (static_rand < 0.5d) {
            System.out.println("Lows");
        } else {
            System.out.println("Highs");
        }
    }
}
