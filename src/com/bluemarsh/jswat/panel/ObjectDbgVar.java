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
 * $Id: ObjectDbgVar.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.JSwat;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreeNode;

/**
 * Class ObjectDbgVar represents an ObjectReference variable.
 *
 * @author  David Lum
 * @author  Nathan Fiedler
 */
public class ObjectDbgVar extends DbgVar {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Our object icon. */
    protected static ImageIcon icon;
    /** Object reference 'this' represents. */
    protected ObjectReference objRef;
    /** This will eventually be set to the map of values for it.
     * It should always be accessed via the {@link #getValuesMap} method. */
    protected Map valuesMap;

    /**
     * Creates a new ObjectDbgVar from a name, type, and value.
     *
     * @param  name  the name of the variable.
     * @param  type  the type of the variable.
     * @param  val   the value of the variable.
     */
    protected ObjectDbgVar(String name, String type, ObjectReference val) {
        super(name, type);
        objRef = val;
    } // ObjectDbgVar

    /**
     * Redefined from parent to whip up a child list "just in time."
     */
    public Enumeration children() {
        concoctChildren();
        return super.children();
    } // children

    /**
     * Adds children from 'this' object's fields.
     */
    protected void concoctChildren() {
        if (((children != null) && (children.size() > 0)) ||
            (objRef == null)) {
            return;
        }
        Map map = getValuesMap();
        int size = map.size();
        Iterator iter = map.entrySet().iterator();
        int ii = 0;
        while (iter.hasNext()) {
            Map.Entry e = (Map.Entry) iter.next();
            Field f = (Field) e.getValue();
            Value v = objRef.getValue(f);
            // Make sure the object references don't form a loop.
            DbgVar dbgVar;
            if ((v instanceof ObjectReference) &&
                (formsLoop((ObjectReference) v))) {
                // Insert a special 'loop' node.
                dbgVar = DbgVar.createLoop(f, (ObjectReference) v);
            } else {
                dbgVar = DbgVar.create(f, v);
            }
            insert(dbgVar, ii);
            ii++;
        }
    } // concoctChildren

    /**
     * Test if the given object reference already exists in the
     * parent heirarchy.
     *
     * @param  objref  object reference to test.
     * @return  true if 'objref' links to this or a parent var.
     */
    protected boolean formsLoop(ObjectReference objref) {
        // For all object reference parents
        ObjectDbgVar parent = this;
        long orid = objref.uniqueID();
        do {
            // Does parent's objRef equal this objref?
            if (parent.objRef.uniqueID() == orid) {
                // Uh oh, found a loop in the tree.
                return true;
            }
            // Find next ObjectDbgVar parent, if any.
            TreeNode p = parent.getParent();
            if (p instanceof ObjectDbgVar) {
                parent = (ObjectDbgVar) p;
            } else {
                parent = null;
            }
        } while (parent != null);
        // No loop here.
        return false;
    } // formsLoop

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param  obj  the reference object with which to compare.
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof ObjectDbgVar) {
            ObjectDbgVar other = (ObjectDbgVar) obj;
            return other.objRef.equals(objRef);
        }
        return false;
    } // equals

    /**
     * Redefined from parent to whip up a child list "just in time."
     *
     * @param  index  index of child.
     * @return  Child node.
     */
    public TreeNode getChildAt(int index) {
        concoctChildren();
        return super.getChildAt(index);
    } // getChildAt

    /**
     * Redefined from parent to allow 'this' variable's fields to appear
     * as child nodes.
     *
     * @return  the number of fields in 'this' object reference variable.
     */
    public int getChildCount() {
        if (objRef == null) {
            return 0;
        }
        return getValuesMap().size();
    } // getChildCount

    /**
     * Redefined to return a suitable "object" icon.
     *
     * @param  isExpanded  true if tree node is expanded.
     * @return  Icon of the tree node.
     */
    public Icon getIcon(boolean isExpanded) {
        if (ObjectDbgVar.icon == null) {
            JSwat js = JSwat.instanceOf();
            URL url = js.getResource("lvtreeMemberImage");
            ObjectDbgVar.icon = new ImageIcon(url);
        }
        return ObjectDbgVar.icon;
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
     * Returns the values Map for 'this' (creates it if necessary).
     * Any static final fields will be ignored, as they are constant
     * and not interesting.
     *
     * @return  a <code>Map</code> with the fields and values
     *          for 'this' (object) variable.
     */
    protected Map getValuesMap() {
        if ((valuesMap == null) && (objRef != null)) {
            ReferenceType refType = objRef.referenceType();
            ListIterator iter = refType.allFields().listIterator();
            // Use a TreeMap so the fields are sorted.
            valuesMap = new TreeMap();

            while (iter.hasNext()) {
                Field field = (Field) iter.next();
                // Skip over constants, which are boring.
                if (!field.isStatic() || !field.isFinal()) {
                    // Use the field name as the key so it sorts nicely.
                    valuesMap.put(field.name(), field);
                }
            }

        }
        return valuesMap;
    } // getValuesMap

    /**
     * Refreshes the variable.
     */
    public void refresh() {
        // See if we have been built out or not. If not, don't refresh
        // or we might get stuck in an infinite loop.
        if ((children == null) || (children.size() == 0)) {
            return;
        }

        //  Simply refresh our children.
        int size = getChildCount();
        Map values = getValuesMap();
        for (int ii = 0; ii < size; ii++) {
            DbgVar oldVar = (DbgVar) children.get(ii);
            Field f = (Field) values.get(oldVar.varName);
            Value v = objRef.getValue(f);
            DbgVar newVar = DbgVar.create(f, v);
            updateChild(oldVar, newVar, ii);
        }
    } // refresh

    /**
     * Returns a string description of 'this' variable.
     *
     * @return  a description of 'this' variable.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(varName);
        buf.append(" (");
        buf.append(typeName);
        buf.append("): ");
        if (objRef == null) {
            buf.append("null");
        } else {
            buf.append(objRef.uniqueID());
        }
        return buf.toString();
    } // toString
} // ObjectDbgVar
