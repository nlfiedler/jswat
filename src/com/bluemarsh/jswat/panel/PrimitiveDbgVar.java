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
 * $Id: PrimitiveDbgVar.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.util.StringUtils;
import com.sun.jdi.CharValue;
import com.sun.jdi.Value;

/**
 * A <code>PrimitiveDbgVar</code> represents a primitive debugger variable.
 *
 * @author  David Lum
 */
class PrimitiveDbgVar extends DbgVar {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Value of 'this' variable. */
    protected Value value;

    /**
     * Creates a new <code>PrimitiveDbgVar</code> from a name, type,
     * and value.
     *
     * @param  name  the name of the variable.
     * @param  type  the type of the variable.
     * @param  val   the value of the variable.
     */
    protected PrimitiveDbgVar(String name, String type, Value val) {
        super(name, type);
        value = val;
    } // PrimitiveDbgVar

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
     * Sets the value of this primitive variable.
     *
     * @param  value  new value.
     */
    public void setValue(Value value) {
        this.value = value;
    } // setValue

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
            buf.append(StringUtils.cleanForPrinting(value.toString(), 0));
        }
        if (value instanceof CharValue) {
            // Print hex Unicode value of character.
            CharValue charval = (CharValue) value;
            buf.append(" (\\u");
            buf.append(StringUtils.toHexString(charval.value()));
            buf.append(')');
        }
        return buf.toString();
    } // toString
} // PrimitiveDbgVar
