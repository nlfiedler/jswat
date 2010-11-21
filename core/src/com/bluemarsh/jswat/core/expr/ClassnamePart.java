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
 * are Copyright (C) 2004-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
class ClassnamePart {

    /** The name part. */
    private String namepart;

    /**
     * Creates a new instance of ClassnamePart.
     *
     * @param  part  the part of a classname.
     */
    ClassnamePart(String part) {
        namepart = part;
    }

    /**
     * Returns the name part as-is.
     *
     * @return the name part.
     */
    @Override
    public String toString() {
        return namepart;
    }
}
