/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: ClassnamePart.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

/**
 * Class ClassnamePart represents a piece of a class name. That is, it may
 * be the "com", the "bluemarsh", or the "jswat" of the classname
 * "com.bluemarsh.jswat.Main". This mostly acts as a sentinel, to distinquish
 * a classname part from just an ordinary String.
 *
 * @author  Nathan Fiedler
 */
public class ClassnamePart {
    /** The name part. */
    private String namepart;

    /**
     * Creates a new instance of ClassnamePart.
     *
     * @param  part  the part of a classname.
     */
    public ClassnamePart(String part) {
        namepart = part;
    } // ClassnamePart

    /**
     * Returns the name part as-is.
     *
     * @return the name part.
     */
    public String toString() {
        return namepart;
    } // toString
} // ClassnamePart
