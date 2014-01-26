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
 * $Id: ResolveException.java 1814 2005-07-17 05:56:32Z nfiedler $
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
    /** Nested exception, which is likely the true cause of the problem. */
    protected Exception nestedException;

    /**
     * Constructs a ResolveException with no message.
     */
    public ResolveException() {
        super();
    } // ResolveException

    /**
     * Constructs a ResolveException with the given message.
     *
     * @param  s  Message.
     */
    public ResolveException(String s) {
        super(s);
    } // ResolveException

    /**
     * Constructs a ResolveException with no message.
     *
     * @param  e  nested exception.
     */
    public ResolveException(Exception e) {
        super();
        nestedException = e;
    } // ResolveException

    /**
     * Constructs a ResolveException with the given message.
     *
     * @param  s  Message.
     * @param  e  nested exception.
     */
    public ResolveException(String s, Exception e) {
        super(s);
        nestedException = e;
    } // ResolveException

    /**
     * Return a more humanly readable message for the nested exception.
     *
     * @return  hopefully a better explanation.
     */
    public String errorMessage() {
        if (nestedException instanceof AbsentInformationException) {
            return Bundle.getString("noLineNumberInfo");
        } else if (nestedException instanceof AmbiguousClassSpecException) {
            return Bundle.getString("ambiguousClass");
        } else if (nestedException instanceof AmbiguousMethodException) {
            return Bundle.getString("ambiguousMethod");
        } else if (nestedException instanceof IllegalArgumentException) {
            return Bundle.getString("invalidSyntax");
        } else if (nestedException instanceof InvalidTypeException) {
            return Bundle.getString("brkOnlyInClass");
        } else if (nestedException instanceof LineNotFoundException) {
            return Bundle.getString("noCodeAtLine");
        } else if (nestedException instanceof NoSuchMethodException) {
            return Bundle.getString("noSuchMethod");
        } else {
            return nestedException.toString();
        }
    } // errorMessage

    /**
     * Returns the nested exception, if any.
     */
    public Exception getNestedException() {
        return nestedException;
    } // getNestedException

    /**
     * Returns a String representation of this.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getClass().getName());
        String message = getLocalizedMessage();
        if (message != null) {
            buf.append(": ");
            buf.append(message);
        }
        if (nestedException != null) {
            buf.append('\n');
            buf.append("nested exception: ");
            buf.append(nestedException.toString());
        }
        return buf.toString();
    } // toString
} // ResolveException
