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

    private InAndOut() {
    }
}
