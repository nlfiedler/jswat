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
 * $Id: ArrayVariable.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Value;
import java.net.URL;
import java.util.Enumeration;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

/**
 * A <code>ArrayVariable</code> represents a string debugger variable.
 *
 * @author  David Lum
 */
class ArrayVariable extends Variable {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The maximum number of array elements that will be represented by
     * 'this' variable's model.  If more than this number of elements are
     * present in the underlying array, they will be truncated. */
    public static final int MAX_ELEMENTS = 6;
    /** Our object icon. */
    private static ImageIcon icon;
    /** The array reference 'this' variable holds. */
    private ArrayReference arrRef;
    /** If true, show all of the array elements. */
    private boolean showAllElements;

    /**
     * Creates a new <code>ArrayVariable</code> from a name, type, and
     * value.
     *
     * @param  name  the name of the variable.
     * @param  type  the type of the variable.
     * @param  val   the value of the variable.
     */
    protected ArrayVariable(String name, String type, ArrayReference val) {
        super(name, type);
        arrRef = val;
    } // ArrayVariable

    /**
     * Redefined from parent to whip up a child list "just in time."
     *
     * @return  enumeration of the child nodes.
     */
    public Enumeration children() {
        concoctChildren();
        return super.children();
    } // children

    /**
     * Adds children from 'this' object's elements.
     */
    protected void concoctChildren() {
        if ((children != null) && (children.size() > 0)) {
            return;
        }
        int size = getChildCount();
        int realSize = arrRef.length();
        boolean needElipsis = (realSize > size);
        int offset = 0;
        for (int i = 0; i < size; i++) {
            Value val = arrRef.getValue(offset);
            Variable variable =
                Variable.create("[" + offset + "]",
                                (val == null ? "null" : val.type().name()),
                                val);
            insert(variable, i);
            offset++;

            if (needElipsis && (i == size - 3)) {
                i++;
                insert(new LocalsTreeNode("..."), i);
                offset = realSize - 1;
            }
        }
    } // concoctChildren

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param  obj  the reference object with which to compare.
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof ArrayVariable) {
            ArrayVariable them = (ArrayVariable) obj;
            if (arrRef == null && them.arrRef == null) {
                return true;
            }
            if (arrRef == null || them.arrRef == null) {
                return false;
            }
            return arrRef.equals(them.arrRef);
        }
        return false;
    } // equals

    /**
     * Redefined from parent to allow 'this' variable's elements to
     * appear as child nodes.
     *
     * @return  the minimum of (1) the number of elements in 'this'
     *          array and (2) 'ArrayVariable.maxElements' + 1.
     */
    public int getChildCount() {
        if (arrRef == null) {
            return 0;
        }
        if (showAllElements) {
            return arrRef.length();
        } else {
            return java.lang.Math.min(MAX_ELEMENTS + 1, arrRef.length());
        }
    } // getChildCount

    /**
     * Redefined from parent to whip up a child list "just in time."
     *
     * @param  index  position of child to get.
     * @return  child at <code>index</code>.
     */
    public TreeNode getChildAt(int index) {
        concoctChildren();
        return super.getChildAt(index);
    } // getChildAt

    /**
     * Redefined to return a suitable "array" icon.
     *
     * @param  isExpanded  true if node is expanded.
     * @return  an icon appropriate for 'this' variable.
     */
    public Icon getIcon(boolean isExpanded) {
        if (ArrayVariable.icon == null) {
            URL url = Bundle.getResource("Locals.image.array");
            ArrayVariable.icon = new ImageIcon(url);
        }
        return ArrayVariable.icon;
    } // getIcon

    /**
     * Retrieve the value this variable represents.
     *
     * @return  Value.
     */
    public Value getValue() {
        return arrRef;
    } // getValue

    /**
     * Refreshes the array variables.
     */
    public void refresh() {
        // See if we have been built out or not. If not, don't refresh
        // or we might get stuck in an infinite loop.
        if ((children == null) || (children.size() == 0)) {
            return;
        }

        // Run through the list of children and make sure they are refreshed.
        int size = getChildCount();
        int realSize = arrRef.length();
        boolean needElipsis = (realSize > size);
        int offset = 0;
        for (int ii = 0; ii < size; ii++) {
            Variable oldVar = (Variable) children.get(ii);
            oldVar.markChanged(false);
            Value val = arrRef.getValue(offset);
            Variable newVar =
                Variable.create("[" + offset + "]",
                                (val == null ? "null" : val.type().name()),
                                val);
            updateChild(oldVar, newVar, ii);
            if (needElipsis && (ii == size - 3)) {
                ii++;
                offset = realSize - 1;
            } else {
                offset++;
            }
        }
    } // refresh

    /**
     * Show all of the array elements.
     */
    public void showAll() {
        showAllElements = true;
        children = null;
        concoctChildren();
    } // showAll

    /**
     * Returns a string description of 'this' variable.
     *
     * @return  a description of 'this' variable.
     */
    public String toString() {
        StringBuffer val = new StringBuffer(varName);
        val.append(" (");
        val.append(typeName);
        val.append(')');
        if (arrRef == null) {
            val.append(": null");
        } else {
            val.append(" [");
            val.append(arrRef.length());
            val.append(']');
        }
        return val.toString();
    } // toString
} // ArrayVariable
