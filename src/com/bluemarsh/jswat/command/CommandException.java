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
 * $Id: CommandException.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.command;

/**
 * Thrown when a command has experienced a problem. Exceptions of this
 * type must have a localized message. The command manager displays that
 * message to the user when it catches the exception.
 *
 * @author  Nathan Fiedler
 */
public class CommandException extends RuntimeException {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the specified detail message. The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param  message  the detail message. The detail message is saved
     *                  for later retrieval by the {@link #getMessage()}
     *                  method.
     */
    public CommandException(String message) {
        super(message);
    } // CommandException

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <tt>(cause==null ? null : cause.toString())</tt>
     * (which typically contains the class and detail message of
     * <tt>cause</tt>). This constructor is useful for exceptions that
     * are little more than wrappers for other throwables (for example,
     * {@link java.security.PrivilegedActionException}).
     *
     * @param  cause  the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <tt>null</tt> value
     *                is permitted, and indicates that the cause is
     *                nonexistent or unknown.)
     */
    public CommandException(Throwable cause) {
        super(cause);
    } // CommandException

    /**
     * Constructs a new exception with the specified detail message and
     * cause. <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param  message  the detail message (which is saved for later retrieval
     *                  by the {@link #getMessage()} method).
     * @param  cause    the cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method).  (A <tt>null</tt> value
     *                  is permitted, and indicates that the cause is
     *                  nonexistent or unknown.)
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    } // CommandException
} // CommandException
