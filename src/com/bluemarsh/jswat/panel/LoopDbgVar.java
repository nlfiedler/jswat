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
 * $Id: LoopDbgVar.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.JSwat;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Class LoopDbgVar represents a debugger variable that refers to the
 * same variable further up the local variables tree. That is, an
 * object that eventually refers to itself, either directly or
 * indirectly. Such link will form a loop in the tree and that results
 * in branches that are essentially infinite.
 *
 * @author  Nathan Fiedler
 */
class LoopDbgVar extends DbgVar {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Our loop icon. */
    protected static ImageIcon icon;
    /** Object reference 'this' represents. */
    protected ObjectReference objRef;

    /**
     * Creates a LoopDbgVar.
     *
     * @param  name  the name of the variable.
     * @param  type  the type of the variable.
     * @param  val   the value of the variable.
     */
    protected LoopDbgVar(String name, String type, ObjectReference val) {
        super(name, type);
        objRef = val;
    } // LoopDbgVar

    /**
     * Redefined to return a suitable "object" icon.
     *
     * @param  isExpanded  true if tree node is expanded.
     * @return  Icon of the tree node.
     */
    public Icon getIcon(boolean isExpanded) {
        if (LoopDbgVar.icon == null) {
            JSwat js = JSwat.instanceOf();
            URL url = js.getResource("lvtreeLoopImage");
            LoopDbgVar.icon = new ImageIcon(url);
        }
        return LoopDbgVar.icon;
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
        //buf.append(" -forms loop-");
        return buf.toString();
    } // toString
} // LoopDbgVar
