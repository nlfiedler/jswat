/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: LoopVariable.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Class LoopVariable represents a debugger variable that refers to the
 * same variable further up the local variables tree. That is, an object
 * that eventually refers to itself, either directly or indirectly. Such
 * link will form a loop in the tree and that results in branches that
 * are essentially infinite.
 *
 * @author  Nathan Fiedler
 */
class LoopVariable extends Variable {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Our loop icon. */
    protected static ImageIcon icon;
    /** Object reference 'this' represents. */
    protected ObjectReference objRef;

    /**
     * Creates a LoopVariable.
     *
     * @param  name  the name of the variable.
     * @param  type  the type of the variable.
     * @param  val   the value of the variable.
     */
    protected LoopVariable(String name, String type, ObjectReference val) {
        super(name, type);
        objRef = val;
    } // LoopVariable

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param  obj  the reference object with which to compare.
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof LoopVariable) {
            LoopVariable other = (LoopVariable) obj;
            return other.objRef.equals(objRef);
        }
        return false;
    } // equals

    /**
     * Redefined to return a suitable "object" icon.
     *
     * @param  isExpanded  true if tree node is expanded.
     * @return  Icon of the tree node.
     */
    public Icon getIcon(boolean isExpanded) {
        if (LoopVariable.icon == null) {
            URL url = Bundle.getResource("Locals.image.loop");
            LoopVariable.icon = new ImageIcon(url);
        }
        return LoopVariable.icon;
    } // getIcon

    /**
     * Retrieve the value this variable represents.
     *
     * @return  Value.
     */
    public Value getValue() {
        return objRef;
    } // getValue

    /**
     * Marks this variable as having been changed since the last refresh.
     *
     * @param  changed  true if this variable has recently changed.
     * @see #isChanged()
     */
    public void markChanged(boolean changed) {
        // A loop variable never changes.
    } // markChanged

    /**
     * Refreshes the variable. This particular implementation does nothing.
     */
    public void refresh() {
    } // refresh

    /**
     * Returns a string description of this.
     *
     * @return  a description of this.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(varName);
        buf.append(" (");
        buf.append(typeName);
        buf.append("): ");
        buf.append(objRef.uniqueID());
        return buf.toString();
    } // toString
} // LoopVariable
