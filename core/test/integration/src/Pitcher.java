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
 * $Id: Pitcher.java 15 2007-06-03 00:01:17Z nfiedler $
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
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // Purposely cause an exception to be thrown.
            URL url = new URL("blah;%$@)%");
        } catch (MalformedURLException ioe) {
            // Ignore so we can test caught exceptions.
        }
        // Throw uncaught exceptions so we can test that as well.
        if (args.length == 0) {
            throw new IllegalArgumentException();
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
}
