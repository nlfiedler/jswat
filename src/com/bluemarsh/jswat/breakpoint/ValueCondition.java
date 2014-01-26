/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Nathan Fiedler
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
 * PROJECT:     JSwat
 * MODULE:      Breakpoints
 * FILE:        ValueCondition.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/22/01        Initial version
 *      nf      10/10/01        Fixed bug #261, implemented RFE #254
 *      nf      04/25/02        Fixed bug #481 (mostly)
 *      nf      04/30/02        Fixed bug #477
 *
 * DESCRIPTION:
 *      Defines the ValueCondition class.
 *
 * $Id: ValueCondition.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.breakpoint.ui.ConditionUI;
import com.bluemarsh.jswat.breakpoint.ui.ValueConditionUI;
import com.bluemarsh.jswat.util.FieldAndValue;
import com.bluemarsh.jswat.util.StringUtils;
import com.bluemarsh.jswat.util.VariableUtils;
import com.sun.jdi.*;
import com.sun.jdi.event.*;

/**
 * Class ValueCondition implements a breakpoint conditional that is
 * satisfied when a field or local variable is equal to a particular
 * value.
 *
 * @author  Nathan Fiedler
 */
public class ValueCondition implements Condition {
    /** Name of the variable to examine. */
    protected String variableName;
    /** Original value to compare against, as a String. */
    protected String valueString;
    /** serial version */
    static final long serialVersionUID = -2712373181938760680L;

    /**
     * Constructs a ValueCondition that is satisfied when the
     * named variable equals the given value.
     *
     * @param  expr   variable name expression (e.g. "this.counter").
     * @param  value  value to compare against.
     */
    public ValueCondition(String expr, String value) {
        this.variableName = expr;
        this.valueString = value;
    } // ValueCondition

    /**
     * Constructs a ValueCondition that is satisfied when the given
     * expression is satisfied. The expression is of the form:
     * <code>variable = value</code>
     * where 'variable' is the name of a variable and 'value' is
     * string, character, boolean, or number.
     *
     * @param  expr   variable name, equals sign, and value. The
     *                expression may contain escaped equals signs,
     *                which will be ignored.
     * @exception  IllegalArugmentException
     *             if expression lacks the equals sign (=).
     */
    public ValueCondition(String expr) {
        int eqidx = StringUtils.indexOfUnescaped(expr, '=');
        if (eqidx > 0) {
            this.variableName = expr.substring(0, eqidx).trim();
            this.valueString = expr.substring(eqidx + 1).trim();
        } else {
            throw new IllegalArgumentException("missing = separator");
        }
    } // ValueCondition

    /**
     * Retrieves the value this condition tests for.
     *
     * @return  value this condition tests for.
     */
    public String getValueString() {
        return valueString;
    } // getValueString

    /**
     * Retrieves the variable name of this condition.
     *
     * @return  name of variable this condition tests.
     */
    public String getVariableName() {
        return variableName;
    } // getVariableName

    /**
     * Returns the user interface widget for customizing this condition.
     *
     * @return  Condition user interface adapter.
     */
    public ConditionUI getUIAdapter() {
        return new ValueConditionUI(this);
    } // getUIAdapter

    /**
     * Returns true if this condition is satisfied.
     *
     * @param  event  JDI Event that brought us here.
     * @return  True if satisfied, false otherwise.
     */
    public boolean isSatisfied(Event event) throws Exception {
        if (!(event instanceof LocatableEvent)) {
            // Cannot evaluate condition without a thread.
            return false;
        }
        FieldAndValue fieldValue = null;
        Type valueType;
        ThreadReference thread = ((LocatableEvent) event).thread();
        if ((thread == null) || !thread.isSuspended() ||
            !thread.isAtBreakpoint()) {
            // Not a valid thread or not suspended.
            return false;
        }
        fieldValue = VariableUtils.getField(variableName, thread, 0);
        if (fieldValue.field != null) {
            valueType = fieldValue.field.type();
        } else {
            valueType = fieldValue.localVar.type();
        }

        Value value = fieldValue.value;

        // Evaluate the valueString to see if it equals the value.
        if (value == null) {
            return valueString.equals("null");
        } else if (value instanceof BooleanValue) {
            boolean b = Boolean.valueOf(valueString).booleanValue();
            return b == ((BooleanValue) value).value();
        } else if (value instanceof ByteValue) {
            byte b = Byte.parseByte(valueString);
            return b == ((ByteValue) value).value();
        } else if (value instanceof CharValue) {
            char c = valueString.charAt(0);
            return c == ((CharValue) value).value();
        } else if (value instanceof DoubleValue) {
            double d = Double.parseDouble(valueString);
            return d == ((DoubleValue) value).value();
        } else if (value instanceof FloatValue) {
            float f = Float.parseFloat(valueString);
            return f == ((FloatValue) value).value();
        } else if (value instanceof IntegerValue) {
            int i = Integer.parseInt(valueString);
            return i == ((IntegerValue) value).value();
        } else if (value instanceof LongValue) {
            long l = Long.parseLong(valueString);
            return l == ((LongValue) value).value();
        } else if (value instanceof ShortValue) {
            short s = Short.parseShort(valueString);
            return s == ((ShortValue) value).value();
        } else if (value instanceof VoidValue) {
            // Never satisfied.
            return false;
        } else if (value instanceof StringReference) {
            return valueString.equals(((StringReference) value).value());
        } else {
            throw new IllegalArgumentException("type is not supported");
        }
    } // isSatisfied

    /**
     * Sets the value this condition tests for.
     *
     * @param  value  new value to test for.
     */
    public void setValueString(String value) {
        valueString = value;
    } // setValueString

    /**
     * Sets the variable name of this condition.
     *
     * @param  name  new name of variable to test.
     */
    public void setVariableName(String name) {
        variableName = name;
    } // setVariableName

    /**
     * Returns a string representation of this.
     *
     * @return  String representing this.
     */
    public String toString() {
        return "ValueCondition=[" + variableName + " = " + valueString + "]";
    } // toString
} // ValueCondition
