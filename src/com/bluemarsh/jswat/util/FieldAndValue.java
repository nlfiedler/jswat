/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * MODULE:      Utilities
 * FILE:        FieldAndValue.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      09/03/01        Initial version
 *
 * DESCRIPTION:
 *      This file defines the class that contains a Field and Value.
 *
 * $Id: FieldAndValue.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.util;

import com.sun.jdi.*;

/**
 * Encapsulates a Field, its Value, and the Object containing the
 * Field. May also represent a local variable, so the field will
 * be null, and the object will be the LocalVariable.
 *
 * @author  Nathan Fiedler
 */
public class FieldAndValue {
    /** Field reference, or null if local variable. */
    public Field field;
    /** Value of Field (always non-null). */
    public Value value;
    /** Object containing field, or null if static field or local variable. */
    public ObjectReference object;
    /** Local variable, if object and field are null. */
    public LocalVariable localVar;

    /**
     * Constructs a FieldAndValue to hold a field and its value.
     *
     * @param  field   Field reference.
     * @param  value   Value of Field.
     * @param  object  ObjectReference.
     */
    public FieldAndValue(Field field, Value value, ObjectReference object) {
        this.field = field;
        this.value = value; 
        this.object = object;
    } // FieldAndValue

    /**
     * Constructs a FieldAndValue to hold a local variable and
     * its value.
     *
     * @param  value  Value of LocalVariable.
     * @param  local  LocalVariable.
     */
    public FieldAndValue(LocalVariable local, Value value) {
        this.localVar = local;
        this.value = value; 
    } // FieldAndValue
} // FieldAndValue
