/*********************************************************************
 *
 *      Copyright (C) 2001-2004 Nathan Fiedler
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
 * $Id: VariableValue.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import com.sun.jdi.Type;

/**
 * Encapsulates a Field, its Value, and the Object containing the Field.
 * May also represent a local variable, so the field will be null, and
 * the object will be the LocalVariable. Additionally, this wrapper may
 * represent an array .length reference.
 *
 * @author  Nathan Fiedler
 */
public class VariableValue {
    /** Field reference, or null if local variable. */
    private Field field;
    /** Value of Field (always non-null). */
    private Value value;
    /** Object containing field, or null if static field or local variable. */
    private ObjectReference object;
    /** Local variable, if object and field are null. */
    private LocalVariable localVar;
    /** The array, if variable was an array element reference. */
    private ArrayReference arrayRef;
    /** The array reference offset, if variable was an array element
     * reference. */
    private int arrayIndex;

    /**
     * Constructs a VariableValue to hold a field and its value.
     *
     * @param  field    Field reference.
     * @param  value    Value of Field.
     * @param  object   ObjectReference.
     */
    public VariableValue(Field field, Value value, ObjectReference object) {
        this.field = field;
        this.value = value;
        this.object = object;
    } // VariableValue

    /**
     * Constructs a VariableValue to hold an array reference.
     *
     * @param  field   Field reference.
     * @param  array   ArrayReference.
     */
    public VariableValue(Field field, ArrayReference array) {
        // Value is the array length.
        this(field, array.virtualMachine().mirrorOf(array.length()), array);
    } // VariableValue

    /**
     * Constructs a VariableValue to hold a local variable and
     * its value.
     *
     * @param  local    LocalVariable.
     * @param  value    Value of LocalVariable.
     */
    public VariableValue(LocalVariable local, Value value) {
        this.localVar = local;
        this.value = value;
    } // VariableValue

    /**
     * Constructs a VariableValue to hold an array reference.
     *
     * @param  local  LocalVariable.
     * @param  array   ArrayReference.
     */
    public VariableValue(LocalVariable local, ArrayReference array) {
        // Value is the array length.
        this(local, array.virtualMachine().mirrorOf(array.length()));
    } // VariableValue

    /**
     * Returns the array reference offset if this variable is an array
     * element reference.
     *
     * @return  the array reference offset, or null if none.
     */
    public int arrayIndex() {
        return arrayIndex;
    } // arrayIndex

    /**
     * Returns the array reference if this variable is an array element
     * reference.
     *
     * @return  the array reference, or null if none.
     */
    public ArrayReference arrayRef() {
        return arrayRef;
    } // arrayRef

    /**
     * Returns the Field object, if this represents a field.
     *
     * @return  field.
     */
    public Field field() {
        return field;
    } // field

    /**
     * Returns the LocalVariable object, if this represents a local
     * variable.
     *
     * @return  local variable, or null if field.
     */
    public LocalVariable localVariable() {
        return localVar;
    } // localVariable

    /**
     * Returns the ObjectReference that contains the field, if this
     * represents a non-static field.
     *
     * @return  object, or null if local variable or static field.
     */
    public ObjectReference object() {
        return object;
    } // object

    /**
     * Sets this variable value as a reference into an array.
     *
     * @param  ref  the array element reference, or null.
     * @param  idx  offset into the array of element referenced.
     */
    void setArrayRef(ArrayReference ref, int idx) {
        arrayRef = ref;
        arrayIndex = idx;
    } // setArrayRef

    /**
     * Type of the value if non-null, or the type of the field or local
     * variable if the value is null.
     *
     * @return  type of value.
     */
    public Type type() {
        try {
            if (value != null) {
                return value.type();
            } else if (field != null) {
                return field.type();
            } else if (localVar != null) {
                return localVar.type();
            } else {
                return null;
            }
        } catch (ClassNotLoadedException cnle) {
            return null;
        }
    } // type

    /**
     * Returns the Value object.
     *
     * @return  value, or null if local variable.
     */
    public Value value() {
        return value;
    } // value
} // VariableValue
