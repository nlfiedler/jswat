/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SetFielder.java 15 2007-06-03 00:01:17Z nfiedler $
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
