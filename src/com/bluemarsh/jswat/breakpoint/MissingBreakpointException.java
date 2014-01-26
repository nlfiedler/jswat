/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: MissingBreakpointException.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

/**
 * MissingBreakpointException is thrown when a BreakpointRequest is
 * discovered that does not have the "breakpoint" property set.
 *
 * @author Nathan Fiedler
 */
public class MissingBreakpointException extends RuntimeException {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a MissingBreakpointException with no message.
     */
    public MissingBreakpointException() {
        super();
    } // MissingBreakpointException

    /**
     * Constructs a MissingBreakpointException with the given message.
     *
     * @param  s  Message.
     */
    public MissingBreakpointException(String s) {
        super(s);
    } // MissingBreakpointException
} // MissingBreakpointException
