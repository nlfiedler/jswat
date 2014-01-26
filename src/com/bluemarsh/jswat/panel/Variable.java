/*********************************************************************
 *
 *      Copyright (C) 1999-2003 David Lum
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
 * $Id: Variable.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.panel;

import com.bluemarsh.jswat.util.Names;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;

/**
 * A <code>Variable</code> is an abstract class that represents a debugger
 * variable.
 *
 * @author  David Lum
 * @author  Nathan Fiedler
 */
public abstract class Variable extends LocalsTreeNode implements Comparable {
    /** Name of 'this' variable, e.g. "foo". */
    protected String varName;
    /** Short name of type, e.g. "int" or "String". */
    protected String typeName;
    /** Long name of type, e.g. "int" or "java.lang.String". */
    protected String longTypeName;
    /** True if variable changed recently. */
    protected boolean recentlyChanged;
    /** True if variable represents a static field. */
    protected boolean isStatic;

    /**
     * Creates a new <code>Variable</code> from a local variable and value.
     *
     * @param  var  the local variable.
     * @param  val  value of <code>var</code>.
     * @return  a new <code>Variable</code>.
     */
    public static Variable create(LocalVariable var, Value val) {
        // Value.type().name() returns actual type name.
        // LocalVariable.typeName() returns declared type name.
        String tname = val != null ? val.type().name() : var.typeName();
        return Variable.create(var.name(), tname, val);
    } // create

    /**
     * Creates a new <code>Variable</code> from a <code>Field</code> and a
     * <code>Value</code>.
     *
     * @param  field  the field.
     * @param  val    value of <code>field</code>.
     * @return  a new <code>Variable</code>.
     */
    public static Variable create(Field field, Value val) {
        // Value.type().name() returns actual type name.
        // Field.typeName() returns declared type name.
        String tname = val != null ? val.type().name() : field.typeName();
        Variable var = Variable.create(field.name(), tname, val);
        var.isStatic = field.isStatic();
        return var;
    } // create

    /**
     * Creates a new <code>Variable</code> based on name, type name,
     * and value.
     *
     * @param  name  name of the variable.
     * @param  type  type of the variable.
     * @param  val   value of the variable.
     * @return  a new <code>Variable</code>.
     */
    public static Variable create(String name, String type, Value val) {
        if (val instanceof StringReference) {
            return new StringVariable(name, type, (StringReference) val);
        } else if (val instanceof ClassObjectReference) {
            return new ClassVariable(name, type, (ClassObjectReference) val);
        } else if (val instanceof ArrayReference) {
            return new ArrayVariable(name, type, (ArrayReference) val);
        } else if (val instanceof ObjectReference) {
            return new ObjectVariable(name, type, (ObjectReference) val);
        } else {
            return new PrimitiveVariable(name, type, val);
        }
    } // create

    /**
     * Creates a Variable that, directly or indirectly, refers to itself.
     *
     * @param  field  the field.
     * @param  val    value of field.
     * @return  the newly created Variable.
     */
    public static Variable createLoop(Field field, ObjectReference val) {
        // Value.type().name() returns actual type name.
        // Field.typeName() returns declared type name.
        String tname = val != null ? val.type().name() : field.typeName();
        return new LoopVariable(field.name(), tname, val);
    } // createLoop

    /**
     * Creates a new <code>Variable</code> from an object reference.
     * This implies the value references 'this' in the stack frame,
     * and will be represented by the approprite 'Var' class.
     *
     * @param  thiz  object reference of 'this'.
     * @return  a new <code>Variable</code>.
     */
    public static Variable createThis(ObjectReference thiz) {
        String tname = thiz.referenceType().name();
        return new ThisVariable(tname, thiz);
    } // createThis

    /**
     * Creates a new <code>Variable</code> from a name and type.
     *
     * @param  name  the name of the variable.
     * @param  type  the type of the variable.
     */
    protected Variable(String name, String type) {
        varName = name;
        typeName = Names.justTheName(type);
        longTypeName = type;
    } // Variable

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
     */
    public int compareTo(Object o) {
        Variable otherVar = (Variable) o;
        return varName.compareTo(otherVar.varName);
    } // compareTo

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
     * Returns the long name of the type. May be the declared type versus
     * the actual type.
     *
     * @return  type name (e.g. "java.lang.String").
     */
    public String getTypeName() {
        return longTypeName;
    } // getTypeName

    /**
     * Retrieve the value this variable represents.
     *
     * @return  Value.
     */
    public abstract Value getValue();

    /**
     * Returns true if this variable has changed since the last time the
     * panel was refreshed.
     *
     * @return  true if variable changed recently.
     */
    public boolean isChanged() {
        return recentlyChanged;
    } // isChanged

    /**
     * Returns true if this variable represents a static field.
     *
     * @return  true if variable is static.
     */
    public boolean isStatic() {
        return isStatic;
    } // isStatic

    /**
     * Marks this variable as having been changed since the last refresh.
     *
     * @param  changed  true if this variable has recently changed.
     * @see #isChanged()
     */
    public void markChanged(boolean changed) {
        recentlyChanged = changed;
    } // markChanged

    /**
     * Refreshes the variable.
     */
    public abstract void refresh();
} // Variable
