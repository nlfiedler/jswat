/*********************************************************************
 *
 *      Copyright (C) 2000-2005 Nathan Fiedler
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
 * $Id: NumberOptionElement.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Class NumberOptionElement defines the characteristics and behavior for 
 * the number option element. The NumberOptionElement class builds its user
 * interface to consist of a Box with a JLabel and a JTextField. The
 * JLabel consists of the option element's label with a colon (:) appended
 * to the end. The label is placed to the left of the text field.
 *
 * <p>This class differs from TextOptionElement in that it strictly enforces
 * only numeric input (0-9, -, and .).</p>
 *
 * @author  Nathan Fiedler
 */
public class NumberOptionElement extends OptionElement {
    /** Our UI component - a text field. */
    protected JTextField textField;
    /** The wrapper UI component - a panel with border layout. */
    protected JPanel uiComponent;
    /** Saved copy of the value from the text field. */
    protected String cachedValue;

    /**
     * Returns the type name for this option element.
     *
     * @return  Name of the option type ("number").
     */
    public String getTypeName() {
        return "number";
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
            if ((l != null) && (l.length() > 0)) {
                uiComponent = new JPanel();
                // Add a colon to the end of the label.
                JLabel label = new JLabel(l + ":");
                uiComponent.add(label);
            } else {
                uiComponent = new JPanel();
            }

            try {
                int width = Integer.parseInt(getWidth());
                // Set the width of the field using the width attr.
                textField = new JTextField(width);
            } catch (NumberFormatException nfe) {
                // Create text field with default width.
                textField = new JTextField();
            }
            textField.setDocument(new NumericDocument());

            if (cachedValue != null) {
                // Set the text field value to the one we saved earlier.
                setValue(cachedValue);
            }

            uiComponent.add(textField);
        }
        return uiComponent;
    } // getUI

    /**
     * Returns the value for this option, if set.
     *
     * @return  Option value, or null if not yet set.
     */
    public String getValue() {
        if (textField != null) {
            cachedValue = textField.getText();
        }
        return cachedValue;
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
        textField = null;
        uiComponent = null;
    } // invalidateUI

    /**
     * Sets the value for this option.
     *
     * @param  value  Option value.
     */
    public void setValue(String value) {
        if (textField != null) {
            textField.setText(value);
        }
        cachedValue = value;
    } // setValue

    /**
     * Implements a text document that only accepts numeric characters
     * (0-9, -, and .).
     *
     * @author  Nathan Fiedler
     */
    protected class NumericDocument extends PlainDocument {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Insert a string into the document.
         *
         * @param  offs  Offset in which to insert.
         * @param  str   String to insert.
         * @param  a     Attribute set.
         */
        public void insertString(int offs, String str, AttributeSet a) 
            throws BadLocationException {

            if (str == null) {
                return;
            }
            char[] num = str.toCharArray();
            int j = 0;
            for (int i = 0; i < num.length; i++) {
                if (Character.isDigit(num[i]) ||
                    (num[i] == '-') ||
                    (num[i] == '.')) {
                    // copy the digit to new location
                    num[j] = num[i];
                    // character is okay, advance count
                    j++;
                }
            }
            super.insertString(offs, new String(num, 0, j), a);
        } // insertString
    } // NumericDocument
} // NumberOptionElement
