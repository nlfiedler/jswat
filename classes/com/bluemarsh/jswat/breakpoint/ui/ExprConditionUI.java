/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * FILE:        ExprConditionUI.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/11/02        Initial version
 *
 * $Id: ExprConditionUI.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.breakpoint.ui;

import com.bluemarsh.jswat.breakpoint.Condition;
import com.bluemarsh.jswat.breakpoint.ExprCondition;
import java.awt.Component;

/**
 * Class ExprConditionUI provides the interface adapter for the
 * ExprCondition.
 *
 * @author  Nathan Fiedler
 */
public class ExprConditionUI implements ConditionUI {
    /** The expression condition. */
    private ExprCondition expressionCond;

    /**
     * Constructs a ExprConditionUI with the given condition.
     *
     * @param  cond  ExprCondition object.
     */
    public ExprConditionUI(ExprCondition cond) {
        expressionCond = cond;
    } // ExprConditionUI

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
        return expressionCond.getExprString();
    } // descriptor

    /**
     * Returns the Condition object this ui adapter represents.
     *
     * @return  Condition object.
     */
    public Condition getCondition() {
        return expressionCond;
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
     */
    public void undo() {
    } // undo

    /**
     * Returns a description of the expression condition.
     *
     * @return  Expression condition descriptor.
     */
    public String toString() {
        return descriptor();
    } // toString
} // ExprConditionUI
