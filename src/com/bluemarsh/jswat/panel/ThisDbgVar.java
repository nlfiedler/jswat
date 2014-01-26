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
 * $Id: ThisDbgVar.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

/**
 * A <code>ThisDbgVar</code> represents 'this' in the stack frame.
 *
 * @author  Nathan Fiedler
 */
class ThisDbgVar extends DbgVar {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Value of 'this' variable. */
    protected ObjectReference value;

    /**
     * Creates a new <code>ThisDbgVar</code> from a type and value.
     * The name is assumed to be 'this'.
     *
     * @param  type  the type of the variable.
     * @param  val   the value of the variable.
     */
    protected ThisDbgVar(String type, ObjectReference val) {
        super("this", type);
        value = val;
    } // ThisDbgVar

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
} //  ThisDbgVar
