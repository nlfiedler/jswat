/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nodes.variables;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.LongType;
import com.sun.jdi.LongValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Type;
import java.util.Arrays;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

/**
 * Represents a BitSet variable.
 *
 * @author Nathan Fiedler
 */
public class BitSetNode extends VariableNode {
    /** The object reference. */
    private ObjectReference oref;

    /**
     * Constructs a new instance of BitSetNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  oref  the ObjectReference.
     */
    public BitSetNode(String name, String type, VariableNode.Kind kind,
            ObjectReference oref) {
        super(Children.LEAF, name, type, kind);
        this.oref = oref;
    }

    /**
     * Indicates if the given object is a value that this class can display.
     *
     * @param  oref  the object reference.
     * @return  true if object okay, false if not appropriate type.
     */
    public static boolean canRepresent(ObjectReference oref) {
        boolean valid = false;
        ReferenceType clazz = oref.referenceType();
        try {
            // Check the availability and types of the fields.
            Field field = clazz.fieldByName("bits");
            Type type = field.type();
            if (type instanceof ArrayType) {
                ArrayType at = (ArrayType) type;
                if (at.componentType() instanceof LongType) {
                    valid = true;
                }
            }
        } catch (ClassNotLoadedException cnle) {
            // Then return false
        } catch (RuntimeException re) {
            // Then return false
        }
        return valid;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        ReferenceType clazz = oref.referenceType();
        try {
            // The strategy is to avoid invoking methods and to get the
            // elements of the long array up to the unitsInUse value.
            Field field = clazz.fieldByName("bits");
            ArrayReference array = (ArrayReference) oref.getValue(field);
            StringBuilder sb = new StringBuilder();
            for (int index = 0; index < array.length(); index++) {
                LongValue v = (LongValue) array.getValue(index);
                String s = Long.toBinaryString(v.value());
                sb.insert(0, s);
                if (s.length() < 64) {
                    char[] zeros = new char[64 - s.length()];
                    Arrays.fill(zeros, '0');
                    sb.insert(0, zeros);
                }
            }
            set.put(createProperty(PROP_VALUE, sb.toString()));
        } catch (RuntimeException re) {
            // Fall back to the safest possible operation.
            set.put(createProperty(PROP_VALUE, oref.toString()));
        }
//        set.put(createProperty(PROP_STRING, oref.toString()));
        return sheet;
    }
}
