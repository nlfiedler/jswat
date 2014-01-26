/*********************************************************************
 *
 *      Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: ResolveException.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.InvalidTypeException;

/**
 * ResolveException is thrown whenever a breakpoint fails to resolve.
 *
 * @author Nathan Fiedler
 */
public class ResolveException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a ResolveException with no message.
     */
    public ResolveException() {
        super();
    } // ResolveException

    /**
     * Constructs a ResolveException with the given message.
     *
     * @param  message  the detail message.
     */
    public ResolveException(String message) {
        super(message);
    } // ResolveException

    /**
     * Constructs a ResolveException with the given message and cause.
     *
     * @param  message  the detail message.
     * @param  cause    the cause.
     */
    public ResolveException(String message, Throwable cause) {
        super(message, cause);
    } // ResolveException

    /**
     * Constructs a ResolveException with the given cause.
     *
     * @param  cause  the cause.
     */
    public ResolveException(Throwable cause) {
        super(cause);
    } // ResolveException

    /**
     * Return a more humanly readable message for the cause.
     *
     * @return  hopefully a better explanation.
     */
    public String errorMessage() {
        Throwable cause = getCause();
        if (cause instanceof AbsentInformationException) {
            return Bundle.getString("noLineNumberInfo");
        } else if (cause instanceof AmbiguousClassSpecException) {
            return Bundle.getString("ambiguousClass");
        } else if (cause instanceof AmbiguousMethodException) {
            return Bundle.getString("ambiguousMethod");
        } else if (cause instanceof IllegalArgumentException) {
            return Bundle.getString("invalidSyntax");
        } else if (cause instanceof InvalidArgumentTypeException) {
            return Bundle.getString("invalidArgumentType")
                + ' ' + cause.getMessage();
        } else if (cause instanceof InvalidTypeException) {
            return Bundle.getString("brkOnlyInClass");
        } else if (cause instanceof LineNotFoundException) {
            return Bundle.getString("noCodeAtLine");
        } else if (cause instanceof NoSuchMethodException) {
            return Bundle.getString("noSuchMethod");
        } else {
            return cause.toString();
        }
    } // errorMessage

    /**
     * Returns a String representation of this.
     *
     * @return  string of this.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getClass().getName());
        String message = getLocalizedMessage();
        if (message != null) {
            buf.append(": ");
            buf.append(message);
        }
        Throwable cause = getCause();
        if (cause != null) {
            buf.append('\n');
            buf.append("cause: ");
            buf.append(cause.toString());
        }
        return buf.toString();
    } // toString
} // ResolveException
