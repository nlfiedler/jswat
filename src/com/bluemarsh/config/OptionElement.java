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
 * FILE:        OptionElement.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/23/99        Initial version
 *      nf      11/10/01        Fixing bug 292
 *
 * DESCRIPTION:
 *      Defines the option element class.
 *
 * $Id: OptionElement.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.awt.*;
import javax.swing.JPopupMenu;
import java.util.EventObject;

/**
 * Class OptionElement defines the characteristics and behavior for 
 * the option elements. An option element has an implicit type based
 * on the implementing class, a value, a means for setting the option
 * value in the UI component, and a means for getting the option value
 * from the UI component.
 *
 * @author  Nathan Fiedler
 */
public abstract class OptionElement extends LabeledElement {
    /** Width attribute for this option. */
    protected String widthAttr;
    /** Name this option had before it moved, if it moved. */
    protected String movedFrom;

    /**
     * Returns the name that this option was previously known as,
     * if any. This is part of the "moved options" support.
     */
    public String getMovedFrom() {
        return movedFrom;
    } // getMovedFrom

    /**
     * Find the hosting window for this object. The search begins with
     * the source of the event or the passed component, traverses the
     * parent links upward, and stops when the first Window instance
     * is encountered.
     *
     * @param  o  Object with which to find the parent frame.
     *            Could be a subclass of EventObject or Component.
     * @return hosting window or null if none.
     */
    public static Window getOwningWindow(Object o) {
        // Get the Component object.
        if (o instanceof EventObject) {
            o = ((EventObject) o).getSource();
        }
        if (!(o instanceof Component)) {
            throw new IllegalArgumentException(
                "o is not an instance of EventObject or Component");
        }
        if (o instanceof Window) {
            return (Window) o;
        }

        // Find the top window parent of the component.
        Container p = ((Component) o).getParent();
        while (p != null) {
            if (p instanceof JPopupMenu) {
                // Special case for popup menus which do not
                // have parents but have invokers instead.
                p = ((JPopupMenu) p).getInvoker().getParent();
            }
            if (p instanceof Window) {
                return (Window) p;
            }
            p = p.getParent();
        }

        // If we got here, the child simply has no parent window.
        throw new IllegalArgumentException("o is not a child of any Window");
    } // getOwningWindow

    /**
     * Returns the type name for this option element.
     * For example, the TextOptionElement class returns "text".
     *
     * @return  Name of the option type.
     */
    public abstract String getTypeName();

    /**
     * Returns the value for this option, if set.
     *
     * @return  Option value, or null if not yet set.
     */
    public abstract String getValue();

    /**
     * Returns the width for this option, if set.
     *
     * @return  Option width, or null if not yet set.
     */
    public String getWidth() {
        return widthAttr;
    } // getWidth

    /**
     * Throws an exception, as option elements may not have children.
     *
     * @param  child  First child element.
     * @exception  UnsupportedOperationException
     *             Always thrown, as options have no children.
     */
    public void setChild(Element child) {
        throw new UnsupportedOperationException("options have no children");
    } // setChild

    /**
     * Sets the name that this option was previously known as.
     * This is part of the "moved options" support.
     */
    public void setMovedFrom(String old) {
        movedFrom = old;
    } // setMovedFrom

    /**
     * Sets the value for this option.
     *
     * @param  value  Option value.
     */
    public abstract void setValue(String value);

    /**
     * Sets the width for this option.
     *
     * @param  width  Option width.
     */
    public void setWidth(String width) {
        widthAttr = width;
    } // setWidth

    /**
     * Returns a String representation of this object's state.
     *
     * @return  Current state.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer(super.toString());
        buff.append("OptionElement:\n");
        buff.append("  value: ");
        buff.append(getValue());
        buff.append('\n');
        buff.append("  width: ");
        if ((widthAttr == null) || (widthAttr.length() == 0)) {
            buff.append("(none)");
        } else {
            buff.append(widthAttr);
        }
        buff.append('\n');
        return buff.toString();
    } // toString
} // OptionElement
