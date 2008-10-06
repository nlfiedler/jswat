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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: CommandException.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.command;

/**
 * Thrown when a Command experienced an error condition. The thrower must
 * provided a localized message when creating this type of exception.
 *
 * @author  Nathan Fiedler
 */
public class CommandException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the specified detail message. The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param  message  the detail message.
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param  message  the detail message.
     * @param  cause  the cause of this exception.
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
