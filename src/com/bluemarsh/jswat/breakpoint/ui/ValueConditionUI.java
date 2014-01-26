/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * FILE:        ValueConditionUI.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/23/01        Initial version
 *
 * DESCRIPTION:
 *      Defines the ValueConditionUI class.
 *
 * $id:$
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.Condition;
import com.bluemarsh.jswat.breakpoint.ValueCondition;
import java.awt.Component;

/**
 * Class ValueConditionUI provides the interface adapter for the
 * ValueCondition.
 *
 * @author  Nathan Fiedler
 */
public class ValueConditionUI implements ConditionUI {
    /** The value condition. */
    protected ValueCondition valueCond;

    /**
     * Constructs a ValueConditionUI with the given condition.
     *
     * @param  cond  ValueCondition object.
     */
    public ValueConditionUI(ValueCondition cond) {
        valueCond = cond;
    } // ValueConditionUI

    /**
     * Commit the values stored in the user interface elements to the
     * object this adapter is representing (breakpoint, condition, or
     * monitor).
     */
    public void commit() {
    } // commit

    /**
     * Generates a string descriptor of this condition.
     *
     * @return  description.
     */
    public String descriptor() {
        return valueCond.getVariableName() + " = " +
            valueCond.getValueString();
    } // descriptor

    /**
     * Returns the Condition object this ui adapter represents.
     *
     * @return  Condition object.
     */
    public Condition getCondition() {
        return valueCond;
    } // getCondition

    /**
     * Return a reference to the user interface element that this
     * adapter uses to graphically represent the breakpoint, condition,
     * or monitor. This may be a container that has several user
     * interface elements inside it.
     *
     * @return  user interface ocmponent.
     */
    public Component getUI() {
        return null;
    } // getUI

    /**
     * Reverse the changes made to the object this adapter is
     * representing (breakpoint, condition, or monitor). This must
     * not modify the user interface widgets.
     *
     * @exception  UnsupportedOperationException
     *             Thrown if this operation is not supported.
     */
    public void undo() {
    } // undo

    /**
     * Returns a description of the value condition.
     *
     * @return  Value condition descriptor.
     */
    public String toString() {
        return descriptor();
    } // toString
} // ValueConditionUI
