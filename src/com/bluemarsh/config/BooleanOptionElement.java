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
 * FILE:        BooleanOptionElement.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/15/00        Initial version
 *
 * DESCRIPTION:
 *      Defines the checkbox option element class.
 *
 * $Id: BooleanOptionElement.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import javax.swing.*;

/**
 * Class BooleanOptionElement defines the characteristics and behavior for 
 * the checkbox option element. The BooleanOptionElement class builds its
 * user interface to consist simply of a checkbox with textual a label
 * using the element's label or name.
 * The label is placed to the right of the checkbox.
 *
 * @author  Nathan Fiedler
 * @version 1.0  9/15/00
 */
public class BooleanOptionElement extends OptionElement {
    /** Our UI component - a text field. */
    protected JCheckBox checkbox;
    /** Saved copy of the value from the checkbox. */
    protected Boolean cachedValue;

    /**
     * Returns the type name for this option element.
     *
     * @return  Name of the option type ("boolean").
     */
    public String getTypeName() {
        return "boolean";
    } // getTypeName

    /**
     * Returns the reference to the UI component of this element.
     * The component will be created if necessary.
     *
     * @return  UI component.
     */
    public Object getUI() {
        if (checkbox == null) {
            checkbox = new JCheckBox();
            String label = getLabel();
            if ((label != null) && (label.length() > 0)) {
                // Set the label of the button.
                checkbox.setText(label);
            }

            // Set the text field value to the one we saved earlier.
            if (cachedValue != null) {
                setValue(cachedValue.toString());
            }
        }
        return checkbox;
    } // getUI

    /**
     * Returns the value for this option, if set.
     *
     * @return  Option value, or null if not yet set.
     */
    public String getValue() {
        if (checkbox != null) {
            cachedValue = checkbox.isSelected() ? Boolean.TRUE : Boolean.FALSE;
        }
        return cachedValue == null ? null : cachedValue.toString();
    } // getValue

    /**
     * Invalidates the user interface components for this element.
     * Invoked when the element context has changed significantly.
     * Calling <code>getUI()</code> should recreate the UI.
     */
    public void invalidateUI() {
        super.invalidateUI();
        // Call getValue() to save the value to the cache.
        getValue();
        checkbox = null;
    } // invalidateUI

    /**
     * Sets the value for this option.
     *
     * @param  value  Option value.
     */
    public void setValue(String value) {
        cachedValue = Boolean.valueOf(value);
        if (checkbox != null) {
            checkbox.setSelected(cachedValue.booleanValue());
        }
    } // setValue
} // BooleanOptionElement
