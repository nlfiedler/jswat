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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Test code for exception breakpoints.
 *
 * @author Nathan Fiedler
 */
public class Pitcher {

    /**
     * Creates a new instance of Pitcher.
     */
    private Pitcher() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // Purposely cause an exception to be thrown.
            URL url = new URL("blah;%$@)%");
        } catch (MalformedURLException ioe) {
            // Ignore so we can test caught exceptions.
        }

        // Create a new thread that we can attempt to kill.
        Thread th = new Thread(new Runnable() {

            @Override
            public void run() {
                Throwable t = new Throwable();
                try {
                    // Sleep for a while to get a chance to die.
                    // Set a breakpoint here, then use the kill
                    // command to terminate this thread, passing
                    // the thread name and a reference to 't'.
                    int count = 0;
                    while (count < 3600) {
                        Thread.sleep(1000);
                        count++;
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }, "killer");
        th.start();

        // Throw uncaught exceptions so we can test that as well.
        if (args.length == 0) {
            throw new IllegalArgumentException();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
}
