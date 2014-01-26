/*********************************************************************
 *
 *	Copyright (C) 1999-2005 Nathan Fiedler
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
 * $Id: GroupTreeNode.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class GroupTreeNode represents a node in the options tree that
 * holds a group of options. It fits in a JTree and links the tree
 * node with the group panel and element objects.
 *
 * @author  Nathan Fiedler
 * @version 1.0  12/24/99
 */
class GroupTreeNode extends DefaultMutableTreeNode {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Panel of options that we're connected to. */
    protected JPanel groupPanel;
    /** Group element this node represents. */
    protected GroupElement groupElement;

    /**
     * Constructs a new GroupTreeNode object.
     *
     * @param  group  GroupElement associated with this group node.
     * @param  panel  Group's panel container.
     */
    public GroupTreeNode(GroupElement group, JPanel panel) {
        super();
        groupElement = group;
        groupPanel = panel;
    } // GroupTreeNode

    /**
     * Constructs a new GroupTreeNode object with the given label.
     *
     * @param  group  GroupElement associated with this group node.
     * @param  panel  Group's panel container.
     * @param  label  Label for this node.
     */
    public GroupTreeNode(GroupElement group, JPanel panel, String label) {
        super(label);
        groupElement = group;
        groupPanel = panel;
    } // GroupTreeNode

    /**
     * Returns our group element that this group represents.
     *
     * @return  Element of options.
     */
    public GroupElement getElement() {
        return groupElement;
    } // getElement

    /**
     * Returns our panel of options that this group represents.
     *
     * @return  Panel of options.
     */
    public JPanel getPanel() {
        return groupPanel;
    } // getPanel
} // GroupTreeNode
