/*********************************************************************
 *
 *      Copyright (C) 1999-2001 David Lum
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
 * PROJECT:     JSwat
 * MODULE:      Panel
 * FILE:        DbgVar.java
 *
 * AUTHOR:      David Lum
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      dl      06/03/00        Initial version
 *      nf      03/18/01        Added the equals() method.
 *      nf      07/10/01        Used ? operator in create() methods.
 *      nf      07/21/01        Added createLoop()
 *      as      02/01/02        Added ClassDbgVar support
 *
 * DESCRIPTION:
 *      Extends BasicTreeNode class to represent a debugger variable.
 *
 * $Id: DbgVar.java 638 2002-10-27 22:06:47Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.util.ClassUtils;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;

/**
 * A <code>DbgVar</code> is an abstract class that represents a debugger
 * variable.
 *
 * @author  David Lum
 * @see     ObjectDbgVar
 */
public abstract class DbgVar extends BasicTreeNode implements Comparable {
    /** Name of 'this' variable, e.g. "foo". */
    protected String varName;
    /** Type of 'this' variable, e.g. "int". */
    protected String typeName;

    /**
     * Creates a new <code>DbgVar</code> from a name and type.
     *
     * @param  name  the name of the variable.
     * @param  type  the type of the variable.
     */
    protected DbgVar(String name, String type) {
        this.varName = name;
        this.typeName = type;
    } // DbgVar

    /**
     * Compares this object with the specified object for order.
     * Returns a negative integer, zero, or a positive integer as
     * this object is less than, equal to, or greater than the
     * specified object.
     *
     * @param  o  the Object to be compared.
     * @return  a negative integer, zero, or a positive integer as this
     *          object is less than, equal to, or greater than the
     *          specified object.
     * @exception  ClassCastException
     *             if the specified object's type prevents it from
     *             being compared to this Object.
     */
    public int compareTo(Object o) {
        DbgVar otherVar = (DbgVar) o;
        return varName.compareTo(otherVar.varName);
    } // compareTo

    /**
     * Creates a new <code>DbgVar</code> from an object reference.
     * This implies the value references 'this' in the stack frame,
     * and will be represented by the approprite 'Var' class.
     *
     * @param  thiz  object reference of 'this'.
     * @return  a new <code>DbgVar</code>.
     */
    public static DbgVar create(ObjectReference thiz) {
        String tname = thiz.referenceType().name();
        tname = ClassUtils.justTheName(tname);
        return new ThisDbgVar(tname, thiz);
    } // create

    /**
     * Creates a new <code>DbgVar</code> from a local variable and value.
     *
     * @param  var  the local variable.
     * @param  val  value of <code>var</code>.
     * @return  a new <code>DbgVar</code>.
     */
    public static DbgVar create(LocalVariable var, Value val) {
        // Value.type().name() returns actual type name.
        // LocalVariable.typeName() returns declared type name.
        String tname = val != null ? val.type().name() : var.typeName();
        tname = ClassUtils.justTheName(tname);
        return DbgVar.create(var.name(), tname, val);
    } // create

    /**
     * Creates a new <code>DbgVar</code> from a <code>Field</code> and a
     * <code>Value</code>.
     *
     * @param  field  the field.
     * @param  val    value of <code>field</code>.
     * @return  a new <code>DbgVar</code>.
     */
    public static DbgVar create(Field field, Value val) {
        // Value.type().name() returns actual type name.
        // Field.typeName() returns declared type name.
        String tname = val != null ? val.type().name() : field.typeName();
        tname = ClassUtils.justTheName(tname);
        return DbgVar.create(field.name(), tname, val);
    } // create

    /**
     * Creates a new <code>DbgVar</code> based on name, type name,
     * and value.
     *
     * @param  name  name of the variable.
     * @param  type  type of the variable.
     * @param  val   value of the variable.
     * @return  a new <code>DbgVar</code>.
     */
    public static DbgVar create(String name, String type, Value val) {
        if (val instanceof StringReference) {
            return new StringDbgVar(name, type, (StringReference) val);
        } else if (val instanceof ClassObjectReference) {
            return new ClassDbgVar(name, type, (ClassObjectReference) val);
        } else if (val instanceof ArrayReference) {
            return new ArrayDbgVar(name, type, (ArrayReference) val);
        } else if (val instanceof ObjectReference) {
            return new ObjectDbgVar(name, type, (ObjectReference) val);
        } else {
            return new PrimitiveDbgVar(name, type, val);
        }
    } // create

    /**
     * Creates a DbgVar that, directly or indirectly, refers to itself.
     *
     * @param  field  the field.
     * @param  val    value of field.
     * @return  the newly created DbgVar.
     */
    public static DbgVar createLoop(Field field, ObjectReference val) {
        // Value.type().name() returns actual type name.
        // Field.typeName() returns declared type name.
        String tname = val != null ? val.type().name() : field.typeName();
        tname = ClassUtils.justTheName(tname);
        return new LoopDbgVar(field.name(), tname, val);
    } // createLoop

    /**
     * Returns the name of this node. The basic implementation returns
     * the empty string.
     *
     * @return  name of this node.
     */
    public String getName() {
        return varName;
    } // getName

    /**
     * Retrieve the value this variable represents.
     *
     * @return  Value.
     */
    public abstract Value getValue();

    /**
     * Refreshes the variable.
     */
    public abstract void refresh();
} // DbgVar
