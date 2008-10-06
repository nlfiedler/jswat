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
 * are Copyright (C) 2001-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: NoAttachingConnectorException.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.connect;

/**
 * Thrown when an attaching connector could not be found.
 *
 * @author  Nathan Fiedler
 */
public class NoAttachingConnectorException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a NoAttachingConnectorException with no specified
     * detailed message.
     */
    public NoAttachingConnectorException() {
        super();
    }

    /**
     * Constructs a NoAttachingConnectorException with the specified
     * detailed message.
     *
     * @param  s  the detail message
     */
    public NoAttachingConnectorException(String s) {
        super(s);
    }
}
