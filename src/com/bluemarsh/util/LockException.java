/*********************************************************************
 *
 *	Copyright (C) 2000-2005 Nathan Fiedler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id: LockException.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.util;

/**
 * Thrown by methods in the Lock classes to indicate failure to acquire
 * or enter a lock.
 *
 * @author  Nathan Fiedler
 */
public class LockException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a LockException with no specified detail message.
     */
    public LockException() {
        super();
    } // LockException

    /**
     * Constructs a LockException with the specified detail message.
     *
     * @param  msg  Detail message for the exception.
     */
    public LockException(String msg) {
        super(msg);
    } // LockException
} // LockException
