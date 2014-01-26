/*********************************************************************
 *
 *      Copyright (C) 1999-2001 Nathan Fiedler
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
 * FILE:        LabelOptionElement.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/23/99        Initial version
 *      nf      09/01/01        Fixed so the width really works
 *
 * DESCRIPTION:
 *      Defines the label option element class.
 *
 * $Id: LabelOptionElement.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.awt.*;
import javax.swing.*;

/**
 * Class LabelOptionElement defines the characteristics and behavior for 
 * the label option element. The LabelOptionElement class builds its user
 * interface to consist of only a JLabel.
 *
 * @author  Nathan Fiedler
 */
public class LabelOptionElement extends OptionElement {
    /** The label component. */
    protected JLabel uiComponent;

    /**
     * Returns the type name for this option element.
     *
     * @return  Name of the option type ("label").
     */
    public String getTypeName() {
        return "label";
    } // getTypeName

    /**
     * Returns the reference to the UI component of this element.
     * The component will be created if necessary.
     *
     * @return  UI component.
     */
    public Object getUI() {
        if (uiComponent == null) {
            String l = getLabel();
            if (l != null && l.length() > 0) {
                uiComponent = new JLabel(l);
            } else {
                throw new RuntimeException("label element is missing label!");
            }
        }
        return uiComponent;
    } // getUI

    /**
     * Returns the value for this option, if set.
     *
     * @return  Option value, or null if not yet set.
     */
    public String getValue() {
        return "";
    } // getValue

    /**
     * Invalidates the user interface components for this element.
     * Invoked when the element context has changed significantly.
     * Calling <code>getUI()</code> should recreate the UI.
     */
    public void invalidateUI() {
        super.invalidateUI();
        uiComponent = null;
    } // invalidateUI

    /**
     * Sets the value for this option.
     *
     * @param  value  Option value.
     */
    public void setValue(String value) {
    } // setValue
} // LabelOptionElement
