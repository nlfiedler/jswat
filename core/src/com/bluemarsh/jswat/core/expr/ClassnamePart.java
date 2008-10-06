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
 * are Copyright (C) 2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ClassnamePart.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

/**
 * Class ClassnamePart represents a piece of a class name. That is, it may
 * be the "com", the "bluemarsh", or the "jswat" of the classname
 * "com.bluemarsh.jswat.Main". This mostly acts as a sentinel, to distinquish
 * a classname part from just an ordinary String.
 *
 * @author  Nathan Fiedler
 */
public class ClassnamePart {
    /** The name part. */
    private String namepart;

    /**
     * Creates a new instance of ClassnamePart.
     *
     * @param  part  the part of a classname.
     */
    public ClassnamePart(String part) {
        namepart = part;
    } // ClassnamePart

    /**
     * Returns the name part as-is.
     *
     * @return the name part.
     */
    public String toString() {
        return namepart;
    } // toString
} // ClassnamePart
