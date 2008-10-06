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
 * $Id: ThisVariable.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

/**
 * A <code>ThisVariable</code> represents 'this' in the stack frame.
 *
 * @author  Nathan Fiedler
 */
class ThisVariable extends Variable {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Value of 'this' variable. */
    protected ObjectReference value;

    /**
     * Creates a new <code>ThisVariable</code> from a type and value.
     * The name is assumed to be 'this'.
     *
     * @param  type  the type of the variable.
     * @param  val   the value of the variable.
     */
    protected ThisVariable(String type, ObjectReference val) {
        super("this", type);
        value = val;
    } // ThisVariable

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param  obj  the reference object with which to compare.
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof ThisVariable) {
            ThisVariable other = (ThisVariable) obj;
            return other.value.equals(value);
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
     * Marks this variable as having been changed since the last refresh.
     *
     * @param  changed  true if this variable has recently changed.
     * @see #isChanged()
     */
    public void markChanged(boolean changed) {
        // 'this' never changes as far as we are concerned
    } // markChanged

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
        StringBuffer buf = new StringBuffer(32);
        buf.append("this (");
        buf.append(typeName);
        buf.append("): ");
        if (value == null) {
            buf.append("null");
        } else {
            buf.append(value.uniqueID());
        }
        return buf.toString();
    } // toString
} //  ThisVariable
