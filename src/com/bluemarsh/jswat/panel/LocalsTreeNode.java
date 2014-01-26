/*********************************************************************
 *
 *      Copyright (C) 1999-2005 David Lum
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: LocalsTreeNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class LocalsTreeNode represents a node in the local variables tree.
 *
 * @author  David Lum
 * @author  Nathan Fiedler
 */
public class LocalsTreeNode extends DefaultMutableTreeNode {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new <code>LocalsTreeNode</code>.
     */
    public LocalsTreeNode() {
    } // LocalsTreeNode

    /**
     * Creates a new <code>LocalsTreeNode</code> with a string label.
     *
     * @param  label  node label.
     */
    public LocalsTreeNode(String label) {
        super(label);
    } // LocalsTreeNode

    /**
     * Returns an appropriate icon for 'this' variable.
     *
     * @param  isExpanded  true if node is expanded
     * @return  an icon appropriate for 'this' variable
     */
    public Icon getIcon(boolean isExpanded) {
        return null;
    } // getIcon

    /**
     * Returns the name of this node. The basic implementation returns
     * the empty string.
     *
     * @return  name of this node.
     */
    public String getName() {
        return "";
    } // getName

    /**
     * Decide what to do with the given child variable -- either refresh
     * the old variable's contents or replace it with the new one.
     *
     * @param  oldVar  Variable instance in the set of children.
     * @param  newVar  new Variable instance.
     * @param  index   child offset within this node.
     */
    void updateChild(Variable oldVar, Variable newVar, int index) {
        if (!oldVar.equals(newVar)) {
            // Variable type or value has changed.
            remove(index);
            insert(newVar, index);
            newVar.markChanged(true);
        } else {
            // Otherwise just refresh the thing.
            oldVar.refresh();
        }
    } // updateChild
} // LocalsTreeNode
