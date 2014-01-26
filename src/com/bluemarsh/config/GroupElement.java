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
 * FILE:        GroupElement.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/23/99        Initial version
 *
 * DESCRIPTION:
 *      Defines the group element class.
 *
 * $Id: GroupElement.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import javax.swing.JPanel;

/**
 * Class GroupElement defines the characteristics and behavior for 
 * the top-level elements in the options group tree. A group element
 * has a reference to the options element panel, has a description,
 * has a label, has group and option elements as children, and has
 * only group siblings.
 *
 * @author  Nathan Fiedler
 * @version 1.0  12/23/99
 */
public class GroupElement extends LabeledElement {
    /** Group textual description. */
    protected String groupDescription;
    /** Reference to our visual component. */
    protected GroupTreeNode treeNode;
    /** Group options panel. */
    protected JPanel groupPanel;

    /**
     * Returns the description for this group, if any.
     *
     * @return  Group description, or null if none.
     */
    public String getDescription() {
        return groupDescription;
    } // getDescription

    /**
     * Returns the name for this option, if set.
     *
     * @return  Option name.
     * @exception  IllegalStateException
     *             Thrown if the name has not yet been set.
     */
    public String getName() {
        String n = super.getName();
        if (n == null) {
            throw new IllegalStateException("name not specified");
        }
        return n;
    } // getName

    /**
     * Returns the options panel for this group.
     *
     * @return  Options panel for this group.
     */
    public JPanel getPanel() {
        return groupPanel;
    } // getPanel

    /**
     * Returns the reference to the UI component of this element.
     * The component will be created if necessary.
     *
     * @return  UI component.
     */
    public Object getUI() {
        if (treeNode == null) {
            String label = getLabel();
            if (label != null) {
                treeNode = new GroupTreeNode(this, getPanel(), label);
            } else {
                label = getName();
                if (label != null) {
                    treeNode = new GroupTreeNode(this, getPanel(), label);
                } else {
                    treeNode = new GroupTreeNode(this, getPanel());
                }
            }
        }
        return treeNode;
    } // getUI

    /**
     * Sets the description for this group.
     *
     * @param  description  Group description.
     */
    public void setDescription(String description) {
        this.groupDescription = description;
    } // setDescription

    /**
     * Sets the options panel for this group.
     *
     * @param  panel  Options panel for this group.
     */
    public void setPanel(JPanel panel) {
        this.groupPanel = panel;
    } // setPanel

    /**
     * Sets the first sibling element of this element. Only
     * other <code>GroupElement</code> objects may be added
     * as siblings.
     *
     * @param  sibling  First group sibling element.
     * @exception  IllegalArgumentException
     *             Thrown if sibling is not of type GroupElement.
     */
    public void setSibling(Element sibling) {
        if (!(sibling instanceof GroupElement)) {
            throw new IllegalArgumentException("only group siblings allowed");
        }
        super.setSibling(sibling);
    } // setSibling

    /**
     * Returns a String representation of this object's state.
     *
     * @return  Current state.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer(super.toString());
        buff.append("GroupElement:\n");
        buff.append("  label: ");
        String label = getLabel();
        if ((label == null) || (label.length() == 0)) {
            buff.append("(none)");
        } else {
            buff.append(label);
        }
        buff.append('\n');
        buff.append("  description: ");
        if ((groupDescription == null) || (groupDescription.length() == 0)) {
            buff.append("(none)");
        } else {
            buff.append(groupDescription);
        }
        buff.append('\n');
        return buff.toString();
    } // toString
} // GroupElement
