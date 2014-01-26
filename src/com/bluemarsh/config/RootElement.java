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
 * FILE:        RootElement.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/23/99        Initial version
 *
 * DESCRIPTION:
 *      Defines the root element class.
 *
 * $Id: RootElement.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.util.Enumeration;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class RootElement defines the characteristics and behavior for
 * the top-most element in the options group tree. A root element
 * has no parent, no siblings, and only group element children.
 *
 * @author  Nathan Fiedler
 * @version 1.0  12/23/99
 */
public class RootElement extends LabeledElement {
    /** Reference to our visual component. */
    protected DefaultMutableTreeNode treeNode;
    /** List of type definitions. */
    protected Vector typedefs;
    /** Version number of this root element. Used for comparing two
     * sets of elements to decide if upgrading is necessary. */
    protected int version;

    /**
     * Constructs a new RootElement object.
     */
    public RootElement() {
        typedefs = new Vector();
    } // RootElement

    /**
     * Adds the given type definition to the root element,
     * to be persisted back to the preferences file later.
     *
     * @param  typedef  Type to add to root element.
     */
    public void addType(Typedef typedef) {
        typedefs.add(typedef);
    } // addType

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
                treeNode = new DefaultMutableTreeNode(label);
            } else {
                label = getName();
                if (label != null) {
                    treeNode = new DefaultMutableTreeNode(label);
                } else {
                    treeNode = new DefaultMutableTreeNode();
                }
            }
        }
        return treeNode;
    } // getUI

    /**
     * Returns the version of this root element. Versions are used
     * for comparing to preferences files for upgrading.
     */
    public int getVersion() {
        return version;
    } // getVersion

    /**
     * Sets the first child element of this element. The root node
     * only allows group elements as children.
     *
     * @param  child  First group child element.
     * @exception  IllegalArgumentException
     *             Thrown if child is not of type GroupElement.
     */
    public void setChild(Element child) {
        if (!(child instanceof GroupElement)) {
            throw new IllegalArgumentException("only group children allowed");
        }
        super.setChild(child);
    } // setChild

    /**
     * Sets the parent element of this element.
     *
     * @param  parent  Parent element.
     * @exception  UnsupportedOperationException
     *             Always thrown, as roots have no parents.
     */
    public void setParent(Element parent) {
        throw new UnsupportedOperationException("root cannot have parent");
    } // setParent

    /**
     * Sets the first sibling element of this element.
     *
     * @param  sibling  First sibling element.
     * @exception  UnsupportedOperationException
     *             Always thrown, as roots have no siblings
     */
    public void setSibling(Element sibling) {
        throw new UnsupportedOperationException("root cannot have sibling");
    } // setSibling

    /**
     * Sets the version number for this root element, used in upgrading.
     *
     * @param  version  Version number.
     */
    public void setVersion(int version) {
        this.version = version;
    } // setVersion

    /**
     * Returns a String representation of this object's state.
     *
     * @return  Current state.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer(super.toString());
        buff.append("RootElement:\n");
        buff.append("  treeNode: ");
        if (treeNode != null) {
            buff.append(treeNode);
        } else {
            buff.append("(none)");
        }
        buff.append('\n');
        return buff.toString();
    } // toString

    /**
     * Returns an enumeration of the type definitions.
     */
    public Enumeration typedefs() {
        return typedefs.elements();
    } // typedefs
} // RootElement
