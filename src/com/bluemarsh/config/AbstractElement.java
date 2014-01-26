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
 * FILE:        AbstractElement.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/23/99        Initial version
 *
 * DESCRIPTION:
 *      Defines the abstract element class.
 *
 * $Id: AbstractElement.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

/**
 * Class AbstractElement provides the basic functionality for all
 * element classes. Element subclasses may override these methods
 * to provide customized behavior.
 *
 * @author  Nathan Fiedler
 * @version 1.0  12/23/99
 */
public abstract class AbstractElement implements Element {
    /** Element name. */
    protected String elementName;
    /** Reference to the first child element. */
    protected Element firstChild;
    /** Reference to the parent element. */
    protected Element parentElement;
    /** The comment buffer which contains this elements comments. */
    protected StringBuffer commentBuffer;
    /** Cached String value of comment buffer. Invalidated any time
     * the comment buffer is modified. */
    protected String commentString;
    /** Reference to the next sibling element. */
    protected Element nextSibling;

    /**
     * Default constructor for the AbstractElement class.
     */
    public AbstractElement() {
        commentBuffer = new StringBuffer();
    } // AbstractElement

    /**
     * Adds the given element as a child of this element.
     * This method will scan to the end of the child list
     * and add the given element to the end of the list.
     *
     * @param  child  Child to add to this element.
     */
    public void addChild(Element child) {
        Element c = getChild();
        if (c == null) {
            setChild(child);
        } else {
            while (c.hasSibling()) {
                c = c.getSibling();
            }
            c.setSibling(child);
        }
        child.setParent(this);
    } // addChild

    /**
     * Appends the given string to the comment block of this element.
     *
     * @param  comment  Comment string.
     */
    public void appendComment(String comment) {
        commentBuffer.append(comment);
        // Invalidate the cached comment string.
        commentString = null;
    } // appendComment

    /**
     * Returns the first child element of this element.
     *
     * @return  First child element.
     */
    public Element getChild() {
        return firstChild;
    } // getChild

    /**
     * Returns the comment of this element.
     *
     * @return  Comment string.
     */
    public String getComment() {
        if (commentString == null) {
            // Cache the comment string for future reuse.
            commentString = commentBuffer.toString();
        }
        return commentString;
    } // getComment

    /**
     * Returns the name for this option, if set.
     *
     * @return  Option name, or null if not yet set.
     */
    public String getName() {
        return elementName;
    } // getName

    /**
     * Returns the parent element of this element.
     *
     * @return  Parent element.
     */
    public Element getParent() {
        return parentElement;
    } // getParent

    /**
     * Returns the first sibling element of this element.
     *
     * @return  First sibling element.
     */
    public Element getSibling() {
        return nextSibling;
    } // getSibling

    /**
     * Returns the reference to the UI component of this element.
     * The component will be created if necessary.
     *
     * @return  UI component.
     */
    public abstract Object getUI();

    /**
     * Returns true if this element has a child, or false otherwise.
     *
     * @return  True if element has a child.
     */
    public boolean hasChild() {
        return firstChild != null;
    } // hasChild

    /**
     * Returns true if this element has a sibling, or false otherwise.
     *
     * @return  True if element has a sibling.
     */
    public boolean hasSibling() {
        return nextSibling != null;
    } // hasSibling

    /**
     * Invalidates the user interface components for this element.
     * Invoked when the element context has changed significantly.
     * Calling <code>getUI()</code> should recreate the UI.
     */
    public void invalidateUI() {
        // Iterate over the children to invalidate them.
        Element e = getChild();
        while (e != null) {
            e.invalidateUI();
            e = e.getSibling();
        }
    } // invalidateUI

    /**
     * Sets the first child element of this element.
     *
     * @param  child  First child element.
     */
    public void setChild(Element child) {
        firstChild = child;
    } // setChild

    /**
     * Sets the name for this option.
     *
     * @param  name  Option name.
     */
    public void setName(String name) {
        this.elementName = name;
    } // setName

    /**
     * Sets the parent element of this element.
     *
     * @param  parent  Parent element.
     */
    public void setParent(Element parent) {
        this.parentElement = parent;
    } // setParent

    /**
     * Sets the first sibling element of this element.
     *
     * @param  sibling  First sibling element.
     */
    public void setSibling(Element sibling) {
        nextSibling = sibling;
    } // setSibling

    /**
     * Returns a String representation of this object's state.
     *
     * @return  Current state.
     */
    public String toString() {
        StringBuffer buff = new StringBuffer("AbstractElement:\n");
        buff.append("  name: ");
        if ((elementName == null) || (elementName.length() == 0)) {
            buff.append("(none)");
        } else {
            buff.append(elementName);
        }
        buff.append('\n');

        buff.append("  parent: ");
        if (parentElement != null) {
            String name = parentElement.getName();
            if (name == null) {
                buff.append(parentElement.getClass());
            } else {
                buff.append(name);
            }
        } else {
            buff.append("(none)");
        }
        buff.append('\n');

        buff.append("  firstChild: ");
        if (hasChild()) {
            String name = getChild().getName();
            if (name == null) {
                buff.append(getChild().getClass());
            } else {
                buff.append(name);
            }
        } else {
            buff.append("(none)");
        }
        buff.append('\n');

        buff.append("  nextSibling: ");
        if (hasSibling()) {
            String name = getSibling().getName();
            if (name == null) {
                buff.append(getSibling().getClass());
            } else {
                buff.append(name);
            }
        } else {
            buff.append("(none)");
        }
        buff.append('\n');

        buff.append("  comment: ");
        if (getComment().length() == 0) {
            buff.append("(none)");
        } else {
            buff.append(getComment());
        }
        return buff.toString();
    } // toString
} // AbstractElement
