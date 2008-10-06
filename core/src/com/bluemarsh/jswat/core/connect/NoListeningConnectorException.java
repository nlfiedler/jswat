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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: NoListeningConnectorException.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.connect;

/**
 * Thrown when a listening connector could not be found.
 *
 * @author  Nathan Fiedler
 */
public class NoListeningConnectorException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a NoListeningConnectorException with no specified
     * detailed message.
     */
    public NoListeningConnectorException() {
        super();
    }

    /**
     * Constructs a NoListeningConnectorException with the specified
     * detailed message.
     *
     * @param  s  the detail message
     */
    public NoListeningConnectorException(String s) {
        super(s);
    }
}
