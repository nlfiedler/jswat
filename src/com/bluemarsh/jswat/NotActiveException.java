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
 * $Id: NotActiveException.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

/**
 * Thrown when a manager is not active and cannot perform the
 * operation requested.
 *
 * @author  Nathan Fiedler
 */
public class NotActiveException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an NotActiveException with no specified detailed
     * message.
     */
    public NotActiveException() {
        super();
    } // NotActiveException

    /**
     * Constructs an NotActiveException with the specified detailed
     * message.
     *
     * @param  s  the detail message
     */
    public NotActiveException(String s) {
        super(s);
    } // NotActiveException
} // NotActiveException
