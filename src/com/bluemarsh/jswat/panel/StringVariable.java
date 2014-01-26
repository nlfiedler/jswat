/*********************************************************************
 *
 *      Copyright (C) 1999-2005 David Lum
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
 * $Id: StringVariable.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import com.bluemarsh.jswat.util.Strings;

/**
 * A <code>StringVariable</code> represents a string debugger variable.
 *
 * @author  David Lum
 */
class StringVariable extends Variable {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Value of 'this' variable. */
    protected StringReference value;

    /**
     * Creates a new <code>StringVariable</code> from a name, type, and
     * value.
     *
     * @param  name  the name of the variable.
     * @param  type  the type of the variable.
     * @param  val   the value of the variable.
     */
    protected StringVariable(String name, String type, StringReference val) {
        super(name, type);
        value = val;
    } // StringVariable

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param  obj  the reference object with which to compare.
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof StringVariable) {
            String ours = value.value();
            String theirs = ((StringVariable) obj).value.value();
            return ours.equals(theirs);
        }
        return false;
    } // equals

    /**
     * Retrieve the value this variable represents.
     *
     * @return  Value.
     */
    public Value getValue() {
        return value;
    } // getValue

    /**
     * Refreshes the variable. This particular implementation does nothing.
     */
    public void refresh() {
    } // refresh

    /**
     * Returns a string description of 'this' variable.
     *
     * @return  a description of 'this' variable.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(128);
        buf.append(varName);
        buf.append(": \"");
        if (value == null) {
            buf.append("null");
        } else {
            buf.append(Strings.cleanForPrinting(value.value(), 100));
        }
        buf.append("\"");
        return buf.toString();
    } // toString
} // StringVariable
