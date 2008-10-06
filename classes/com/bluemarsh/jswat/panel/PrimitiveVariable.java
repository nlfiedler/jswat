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
 * $Id: PrimitiveVariable.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.util.Strings;
import com.sun.jdi.CharValue;
import com.sun.jdi.Value;

/**
 * A <code>PrimitiveVariable</code> represents a primitive debugger variable.
 *
 * @author  David Lum
 */
class PrimitiveVariable extends Variable {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Value of 'this' variable. */
    protected Value value;

    /**
     * Creates a new <code>PrimitiveVariable</code> from a name, type,
     * and value.
     *
     * @param  name  the name of the variable.
     * @param  type  the type of the variable.
     * @param  val   the value of the variable.
     */
    protected PrimitiveVariable(String name, String type, Value val) {
        super(name, type);
        value = val;
    } // PrimitiveVariable

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param  obj  the reference object with which to compare.
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof PrimitiveVariable) {
            PrimitiveVariable them = (PrimitiveVariable) obj;
            if (value == null && them.value == null) {
                return true;
            }
            if (value == null || them.value == null) {
                return false;
            }
            return value.equals(them.value);
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
     * Primitives cannot be refreshed, they must be replaced.
     */
    public void refresh() {
    } // refresh

    /**
     * Returns a string description of 'this' variable.
     *
     * @return  a description of 'this' variable.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(varName);
        buf.append(" (");
        buf.append(typeName);
        buf.append("): ");
        if (value == null) {
            buf.append("null");
        } else {
            buf.append(Strings.cleanForPrinting(value.toString(), 0));
        }
        if (value instanceof CharValue) {
            // Print hex Unicode value of character.
            CharValue charval = (CharValue) value;
            buf.append(" (\\u");
            buf.append(Strings.toHexString(charval.value()));
            buf.append(')');
        }
        return buf.toString();
    } // toString
} // PrimitiveVariable
