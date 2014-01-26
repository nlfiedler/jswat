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
 * $Id: BasicTreeNode.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Class BasicTreeNode represents a node in the local variables tree.
 *
 * @author  David Lum
 * @author  Nathan Fiedler
 */
public class BasicTreeNode extends DefaultMutableTreeNode {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new <code>BasicTreeNode</code>.
     */
    public BasicTreeNode() {
    } // BasicTreeNode

    /**
     * Creates a new <code>BasicTreeNode</code> with a string label.
     */
    public BasicTreeNode(String label) {
        super(label);
    } // BasicTreeNode

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
     * @param  oldVar  DbgVar instance in the set of children.
     * @param  newVar  new DbgVar instance.
     * @param  index   child offset within this node.
     */
    void updateChild(DbgVar oldVar, DbgVar newVar, int index) {
        if (!oldVar.getClass().equals(newVar.getClass())) {
            // If new variable is a different type from old one,
            // replace the old one with the new one.
            remove(index);
            insert(newVar, index);

        } else if (oldVar instanceof ObjectDbgVar) {
            // See if the object reference has changed.
            ObjectDbgVar oldobj = (ObjectDbgVar) oldVar;
            ObjectDbgVar newobj = (ObjectDbgVar) newVar;
            if (!oldobj.equals(newobj)) {
                // If new object reference is different from old one,
                // replace the old one with the new one.
                remove(index);
                insert(newVar, index);
            } else {
                // Else, refresh the existing object.
                oldobj.refresh();
            }

        } else if (oldVar instanceof StringDbgVar) {
            // See if the string reference has changed.
            StringDbgVar oldstr = (StringDbgVar) oldVar;
            StringDbgVar newstr = (StringDbgVar) newVar;
            if (!oldstr.equals(newstr)) {
                // If new string reference is different from old one,
                // replace the old one with the new one.
                remove(index);
                insert(newVar, index);
            }

        } else if (oldVar instanceof PrimitiveDbgVar) {
            // If primitive variable, always set to the new value.
            PrimitiveDbgVar oldprim = (PrimitiveDbgVar) oldVar;
            oldprim.setValue(newVar.getValue());

        } else {
            // Otherwise just refresh the thing.
            oldVar.refresh();
        }
    } // updateChild
} // BasicTreeNode
