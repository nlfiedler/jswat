/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2002-2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: MissingArgumentsException.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

import org.openide.util.NbBundle;

/**
 * Thrown when a command was not given the required number of arguments.
 *
 * @author  Nathan Fiedler
 */
public class MissingArgumentsException extends CommandException {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with a default detail message.
     */
    public MissingArgumentsException() {
        super(NbBundle.getMessage(MissingArgumentsException.class,
                "ERR_MissingArguments"));
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param  message  the detail message.
     */
    public MissingArgumentsException(String message) {
        super(message);
    }
}
