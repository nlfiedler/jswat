/*********************************************************************
 *
 *      Copyright (C) 2000 Nathan Fiedler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * PROJECT:     JConfigure
 * FILE:        ButtonGroupElement.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/10/00        Initial version
 *
 * DESCRIPTION:
 *      Defines the button group element class.
 *
 * $Id: ButtonGroupElement.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import javax.swing.ButtonGroup;

/**
 * Class ButtonGroupElement acts as a grouping mechanism for a set
 * of button elements. Just as with <code>javax.swing.ButtonGroup</code>
 * only one button within the group may be selected at any one time.
 *
 * @author  Nathan Fiedler
 * @version 1.0  9/10/00
 */
public class ButtonGroupElement extends AbstractElement {
    /** Our UI component: a radio button group */
    protected ButtonGroup buttonGroup;

    /**
     * Returns the reference to the UI component of this element.
     * The component will be created if necessary.
     *
     * @return  UI component.
     */
    public Object getUI() {
        if (buttonGroup == null) {
            buttonGroup = new ButtonGroup();
        }
        return buttonGroup;
    } // getUI

    /**
     * Returns a String representation of this object's state.
     *
     * @return  Current state.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer(super.toString());
        buff.append("ButtonGroupElement:\n");
        buff.append("  buttonGroup: ");
        if (buttonGroup == null) {
            buff.append("(none)");
        } else {
            buff.append(buttonGroup.getClass());
        }
        buff.append('\n');
        return buff.toString();
    } // toString
} // ButtonGroupElement
