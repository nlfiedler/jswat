/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: MissingArgumentsException.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

/**
 * Thrown when a command has not been given sufficient arguments to
 * perform its task.
 *
 * @author  Nathan Fiedler
 */
public class MissingArgumentsException extends RuntimeException {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with <code>null</code> as its detail
     * message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public MissingArgumentsException() {
        super();
    } // MissingArgumentsException

    /**
     * Constructs a new exception with the specified detail message. The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param  message  the detail message. The detail message is saved
     *                  for later retrieval by the {@link #getMessage()}
     *                  method.
     */
    public MissingArgumentsException(String message) {
        super(message);
    } // MissingArgumentsException
} // MissingArgumentsException
