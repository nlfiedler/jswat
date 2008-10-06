/*********************************************************************
 *
 *      Copyright (C) 2004-2005 Nathan Fiedler
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
 * $Id: NonCodeLineException.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang.java;

import com.bluemarsh.jswat.view.ViewException;

/**
 * NonCodeLineException is thrown when the user attempted to set a
 * breakpoint at a line that does not contain code (e.g. the line
 * is outside of the class definition).
 *
 * @author  Nathan Fiedler
 */
public class NonCodeLineException extends ViewException {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a NonCodeLineException with no message.
     */
    public NonCodeLineException() {
        super();
    } // NonCodeLineException

    /**
     * Constructs a NonCodeLineException with the given message.
     *
     * @param  msg  message.
     */
    public NonCodeLineException(String msg) {
        super(msg);
    } // NonCodeLineException

    /**
     * Constructs a NonCodeLineException with the given message and cause.
     *
     * @param  msg    message.
     * @param  cause  the real cause of the problem.
     */
    public NonCodeLineException(String msg, Throwable cause) {
        super(msg, cause);
    } // NonCodeLineException

    /**
     * Constructs a NonCodeLineException with the given cause.
     *
     * @param  cause  the real cause of the problem.
     */
    public NonCodeLineException(Throwable cause) {
        super(cause);
    } // NonCodeLineException
} // NonCodeLineException
