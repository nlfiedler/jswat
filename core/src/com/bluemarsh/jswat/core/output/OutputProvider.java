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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: OutputProvider.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.output;

import org.openide.util.Lookup;

/**
 * Class OutputProvider manages a set of OutputWriter instances, one for
 * each unique Session passed to the <code>getWriter()</code> method.
 *
 * @author Nathan Fiedler
 */
public class OutputProvider {
    /** The OutputWriter instance, if created. */
    private static OutputWriter writer;

    /**
     * Creates a new instance of OutputProvider.
     */
    private OutputProvider() {
    }

    /**
     * Retrieve the OutputWriter instance for the given Session, creating
     * one if necessary.
     *
     * @return  OutputWriter instance.
     */
    public static synchronized OutputWriter getWriter() {
        if (writer == null) {
            // Perform lookup to find the OutputWriter instance.
            writer = (OutputWriter) Lookup.getDefault().lookup(OutputWriter.class);
        }
        return writer;
    }
}
