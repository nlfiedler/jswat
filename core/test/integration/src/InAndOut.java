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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: InAndOut.java 15 2007-06-03 00:01:17Z nfiedler $
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Input and output tester for verifying the correct behavior of the
 * output tab in the debugger.
 *
 * @author  Nathan Fiedler
 */
public class InAndOut {

    /**
     * Invoked by the Java virtual machine.
     *
     * @param  args  the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("This is standard output.");
        System.err.println("This is standard error!");
        System.out.println("Please type something:");
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        try {
            String input = br.readLine();
            System.out.println("You entered: " + input);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        System.out.println("Will now dump lots of output...");
        for (int ii = 1; ii <= 1000; ii++) {
            System.out.println("This is standard output message number " + ii);
            System.err.println("This is standard error message number " + ii);
        }
        System.out.println("END TRANSMISSION");
    }
}
