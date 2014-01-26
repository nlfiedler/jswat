/*********************************************************************
 *
 *      Copyright (C) 1999 Nathan Fiedler
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
 * FILE:        LabeledElement.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/24/99        Initial version
 *
 * DESCRIPTION:
 *      Defines the labeled element class.
 *
 * $Id: LabeledElement.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

/**
 * Class LabeledElement defines the characteristics and behavior for 
 * the label elements. A labeled element has a label and possibly
 * other elements as children.
 *
 * @author  Nathan Fiedler
 * @version 1.0  12/24/99
 */
public abstract class LabeledElement extends AbstractElement {
    /** Element textual label. */
    protected String elementLabel;

    /**
     * Returns the label for this element, or the element name
     * if the label is not yet set.
     *
     * @return  Element label, or name if no label available.
     */
    public String getLabel() {
        if (elementLabel == null) {
            // Use name if label is not yet set.
            elementLabel = getName();
        }
        return elementLabel;
    } // getLabel

    /**
     * Sets the label for this element.
     *
     * @param  label  Element label.
     */
    public void setLabel(String label) {
        this.elementLabel = label;
    } // setLabel

    /**
     * Returns a String representation of this object's state.
     *
     * @return  Current state.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer(super.toString());
        buff.append("LabeledElement:\n");
        buff.append("  label: ");
        if ((elementLabel == null) || (elementLabel.length() == 0)) {
            buff.append("(none)");
        } else {
            buff.append(elementLabel);
        }
        buff.append('\n');
        return buff.toString();
    } // toString
} // LabeledElement
