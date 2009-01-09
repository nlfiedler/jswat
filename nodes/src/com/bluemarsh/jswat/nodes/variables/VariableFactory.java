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
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;

/**
 * A factory for the nodes representing variables in the debuggee.
 *
 * @author Nathan Fiedler
 */
public abstract class VariableFactory {
    /** The default instance of the VariableFactory. */
    private static VariableFactory defaultFactory;

    /**
     * Returns the default implementation of the VariableFactory.
     *
     * @return  a VariableFactory instance.
     */
    public static synchronized VariableFactory getDefault() {
        if (defaultFactory == null) {
            defaultFactory = new DefaultVariableFactory();
        }
        return defaultFactory;
    }

    /**
     * Creates a VariableNode with the given name and value.
     *
     * @param  name   the name to assign to the value.
     * @param  value  the variable value.
     * @return  instance of VariableNode.
     */
    public abstract VariableNode create(String name, Value value);

    /**
     * Creates a VariableNode from a local variable and its value.
     *
     * @param  local  a local variable.
     * @param  value  its value.
     * @param  mods   display modifiers.
     * @return  instance of VariableNode.
     */
    public abstract VariableNode create(LocalVariable local, Value value,
            String mods);

    /**
     * Creates a VariableNode from a field and its value.
     *
     * @param  field  a field.
     * @param  value  its value.
     * @param  mods   display modifiers.
     * @return  instance of VariableNode.
     */
    public abstract VariableNode create(Field field, Value value, String mods);

    /**
     * Creates a VariableNode to represent 'this' object.
     *
     * @param  obj   the 'this' object.
     * @param  mods  display modifiers.
     * @return instance of VariableNode.
     */
    public abstract VariableNode create(ObjectReference obj, String mods);

    /**
     * Creates a VariableNode based on name, type name, and value. All of the
     * other creation methods ultimately delegate to this method.
     *
     * @param  name   name of the variable.
     * @param  type   type of the variable.
     * @param  value  value of the variable.
     * @param  kind   kind of variable.
     * @param  mods   display modifiers.
     * @return instance of VariableNode.
     */
    public abstract VariableNode create(String name, String type, Value value,
            VariableNode.Kind kind, String mods);

    /**
     * Default implementation of VariableFactory.
     */
    private static class DefaultVariableFactory extends VariableFactory {

        @Override
        public VariableNode create(String name, Value value) {
            String tname = value != null ? value.type().name() : "<null>";
            return create(name, tname, value, VariableNode.Kind.LOCAL, null);
        }

        @Override
        public VariableNode create(LocalVariable local, Value value, String mods) {
            // Value.type().name() returns actual type name.
            // LocalVariable.typeName() returns declared type name.
            String tname = value != null ? value.type().name() : local.typeName();
            return create(local.name(), tname, value, VariableNode.Kind.LOCAL, mods);
        }

        @Override
        public VariableNode create(Field field, Value value, String mods) {
            // Value.type().name() returns actual type name.
            // Field.typeName() returns declared type name.
            String tname = value != null ? value.type().name() : field.typeName();
            VariableNode.Kind kind = field.isStatic() ? VariableNode.Kind.STATIC_FIELD
                    : VariableNode.Kind.FIELD;
            VariableNode node = create(field.name(), tname, value, kind, mods);
            node.setField(field);
            return node;
        }

        @Override
        public VariableNode create(ObjectReference obj, String mods) {
            String tname = obj.referenceType().name();
            return create("this", tname, obj, VariableNode.Kind.THIS, mods);
        }

        @Override
        public VariableNode create(String name, String type, Value value,
                VariableNode.Kind kind, String mods) {
            if (value instanceof StringReference) {
                return new StringNode(name, type, kind, (StringReference) value);
            } else if (value instanceof ClassObjectReference) {
                return new ClassObjectNode(name, type, kind, (ClassObjectReference) value);
            } else if (value instanceof ArrayReference) {
                return new ArrayNode(name, type, kind, (ArrayReference) value);
            } else if (value instanceof ObjectReference) {
                // Check what specific type of object this is and try to show
                // it in a more informative fashion.
                ObjectReference obj = (ObjectReference) value;
                String cname = obj.referenceType().name();
                if ((cname.equals("java.lang.StringBuffer") ||
                        cname.equals("java.lang.StringBuilder")) &&
                        StringBufferNode.canRepresent(obj)) {
                    return new StringBufferNode(name, type, kind, obj);
                } else if (cname.equals("java.util.BitSet") &&
                        BitSetNode.canRepresent(obj)) {
                    return new BitSetNode(name, type, kind, obj);
                } else if ((cname.equals("java.lang.Boolean") ||
                        cname.equals("java.lang.Byte") ||
                        cname.equals("java.lang.Character") ||
                        cname.equals("java.lang.Double") ||
                        cname.equals("java.lang.Float") ||
                        cname.equals("java.lang.Integer") ||
                        cname.equals("java.lang.Long") ||
                        cname.equals("java.lang.Short")) &&
                        PrimitiveWrapperNode.canRepresent(obj)) {
                    return new PrimitiveWrapperNode(name, type, kind, obj);
                } else {
                    return new ObjectNode(name, type, kind, obj);
                }
            } else {
                // The value is either primitive, or null.
                return new PrimitiveNode(name, type, kind, (PrimitiveValue) value, mods);
            }
        }
    }
}
