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
 * FILE:        SubGroupElement.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/24/99        Initial version
 *
 * DESCRIPTION:
 *      Defines the subgroup element class.
 *
 * $Id: SubGroupElement.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

/**
 * Class SubGroupElement defines the characteristics and behavior for 
 * the elements in group elements, which contain options. A subgroup
 * element has a label and has only option elements as children.
 *
 * @author  Nathan Fiedler
 * @version 1.0  12/24/99
 */
public class SubGroupElement extends LabeledElement {

    /**
     * Returns the reference to the UI component of this element.
     * The component will be created if necessary.
     *
     * @return  UI component.
     */
    public Object getUI() {
        // XXX - should create the thing
        return null;
    } // getUI

    /**
     * Sets the first child element of this element. The subgroup
     * only allows option elements as children.
     *
     * @param  child  First option child element.
     * @exception  IllegalArgumentException
     *             Thrown if child is not of type OptionElement.
     */
    public void setChild(Element child) {
        if (!(child instanceof OptionElement)) {
            throw new IllegalArgumentException("only option children allowed");
        }
        super.setChild(child);
    } // setChild
} // SubGroupElement
