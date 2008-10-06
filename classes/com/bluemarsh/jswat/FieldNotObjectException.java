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
 * $Id: FieldNotObjectException.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

/**
 * FieldNotObjectException is thrown when the user specifies a field
 * that is not an object, but tried to reference that field as if it
 * had fields of its own.
 *
 * @author  Nathan Fiedler
 */
public class FieldNotObjectException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a FieldNotObjectException with no message.
     */
    public FieldNotObjectException() {
        super();
    } // FieldNotObjectException

    /**
     * Constructs a FieldNotObjectException with the given message.
     *
     * @param  s  Message.
     */
    public FieldNotObjectException(String s) {
        super(s);
    } // FieldNotObjectException
} // FieldNotObjectException
