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
 * are Copyright (C) 1999-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

/**
 * MalformedMemberNameException is thrown when the user gives an invalid
 * method name.
 *
 * @author  Nathan Fiedler
 */
public class MalformedMemberNameException extends Exception {

    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a MalformedMemberNameException with no message.
     */
    public MalformedMemberNameException() {
        super();
    }

    /**
     * Constructs a MalformedMemberNameException with the given message.
     *
     * @param  s  Message.
     */
    public MalformedMemberNameException(String s) {
        super(s);
    }
}
