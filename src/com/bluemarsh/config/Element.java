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
 * FILE:        Element.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/23/99        Initial version
 *
 * DESCRIPTION:
 *      Defines the element interface.
 *
 * $Id: Element.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

/**
 * Interface Element defines the API common to all elements in the
 * JConfigure properties file. Elements may have a name, a comment string,
 * a reference to a parent element, a reference to a sibling element,
 * a reference to a single child element, and a reference to a UI component.
 *<p>
 * A tree of elements is built out through the use of the child and sibling
 * references. An element with children will have a reference to the first
 * child. This first child will have a reference to the second child through
 * the sibling reference, and so on.
 *
 * @author  Nathan Fiedler
 * @version 1.0  12/23/99
 */
public interface Element {

    /**
     * Adds the given element as a child of this element.
     *
     * @param  child  Child to add to this element.
     */
    public void addChild(Element child);

    /**
     * Appends the given string to the comment block of this element.
     *
     * @param  comment  Comment string.
     */
    public void appendComment(String comment);

    /**
     * Returns the first child element of this element.
     *
     * @return  First child element.
     */
    public Element getChild();

    /**
     * Returns the comment of this element.
     *
     * @return  Comment string.
     */
    public String getComment();

    /**
     * Returns the name of this element.
     *
     * @return  Name of element.
     */
    public String getName();

    /**
     * Returns the parent element of this element.
     *
     * @return  Parent element.
     */
    public Element getParent();

    /**
     * Returns the first sibling element of this element.
     *
     * @return  First sibling element.
     */
    public Element getSibling();

    /**
     * Returns a reference to the UI component of this element.
     * The component will be created if necessary.
     *
     * @return  UI component.
     */
    public Object getUI();

    /**
     * Returns true if this element has a child, or false otherwise.
     *
     * @return  True if element has a child.
     */
    public boolean hasChild();

    /**
     * Returns true if this element has a sibling, or false otherwise.
     *
     * @return  True if element has a sibling.
     */
    public boolean hasSibling();

    /**
     * Invalidates the user interface components for this element.
     * Invoked when the element context has changed significantly.
     * Calling <code>getUI()</code> should recreate the UI.
     */
    public void invalidateUI();

    /**
     * Sets the first child element of this element.
     *
     * @param  child  First child element.
     */
    public void setChild(Element child);

    /**
     * Sets the name of this element.
     *
     * @param  name  Name of element.
     */
    public void setName(String name);

    /**
     * Sets the parent element of this element.
     *
     * @param  parent  Parent element.
     */
    public void setParent(Element parent);

    /**
     * Sets the first sibling element of this element.
     *
     * @param  sibling  First sibling element.
     */
    public void setSibling(Element sibling);
} // Element
