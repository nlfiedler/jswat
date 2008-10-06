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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: OutputProvider.java 29 2008-06-30 00:41:09Z nfiedler $
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
            writer = Lookup.getDefault().lookup(OutputWriter.class);
        }
        return writer;
    }
}
