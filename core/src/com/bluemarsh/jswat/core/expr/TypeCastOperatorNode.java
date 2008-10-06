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
 * The Original Software is the JSwat Core Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2003-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: TypeCastOperatorNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import com.bluemarsh.jswat.core.util.Types;
import com.sun.jdi.ArrayType;
import com.sun.jdi.BooleanType;
import com.sun.jdi.ByteType;
import com.sun.jdi.CharType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.DoubleType;
import com.sun.jdi.FloatType;
import com.sun.jdi.IntegerType;
import com.sun.jdi.LongType;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ShortType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import org.openide.util.NbBundle;

/**
 * Class TypeCastOperatorNode implements the type-cast operator.
 *
 * @author  Nathan Fiedler
 */
class TypeCastOperatorNode extends UnaryOperatorNode {
    /** The type of the cast ("byte", "Boolean", "com.sun.jdi.Bootstrap"). */
    private String typeName;

    /**
     * Constructs a TypeCastOperatorNode for the given type.
     *
     * @param  node  lexical token.
     * @param  type  type for the cast (e.g. "byte", "com.sun.jdi.Bootstrap").
     */
    public TypeCastOperatorNode(Token node, String type) {
        super(node);
        typeName = type;
    } // TypeCastOperatorNode

    /**
     * Returns the value of this node as the desired type.
     *
     * @param  context  evaluation context.
     * @return  a value of the desired type.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        // Get the type of the cast.
        String type = getType(context);
        if (type == null) {
            throw new EvaluationException(
                NbBundle.getMessage(getClass(), "error.cast.type", typeName));
        }

        // Value to be cast.
        Node n = getChild(0);
        Object value = n.evaluate(context);

        // Null values are simply passed on. This type-cast is
        // meaningful only in the method invocation context.
        if (value == null) {
            return null;
        }

        if (value instanceof Value) {
            // Handling JDI values takes a whole other approach.
            return evalJdi(context, type, (Value) value);
        }

        // This is a non-JDI value.
        Class vclass = value.getClass();

        // Check if expecting an array. Technically we can't create
        // arrays with the expression evaluator, and since this is not a
        // JDI value, this is all moot. Nonetheless, do it anyway.
        if (type.charAt(0) == '[') {
            if (!vclass.isArray()) {
                throw new EvaluationException(NbBundle.getMessage(
                    getClass(), "error.cast.incompatible", type, vclass.getName()));
            }
            type = type.substring(1);
            vclass = vclass.getComponentType();
        }

        // Check for primitive types.
        char ch = type.charAt(0);
        if (ch == 'Z' && Boolean.class.isAssignableFrom(vclass)) {
            return value;
        } else if (ch == 'B' && Byte.class.isAssignableFrom(vclass)) {
            return value;
        } else if (ch == 'C' && Character.class.isAssignableFrom(vclass)) {
            return value;
        } else if (ch == 'D' && Double.class.isAssignableFrom(vclass)) {
            return value;
        } else if (ch == 'F' && Float.class.isAssignableFrom(vclass)) {
            return value;
        } else if (ch == 'I' && Integer.class.isAssignableFrom(vclass)) {
            return value;
        } else if (ch == 'J' && Long.class.isAssignableFrom(vclass)) {
            return value;
        } else if (ch == 'S' && Short.class.isAssignableFrom(vclass)) {
            return value;
        } else if (ch == 'L') {
            // Check for class/interface type match.
            if (Types.isCompatible(type, vclass)) {
                return value;
            }
        }

        // If not the same type, can it be converted?
        if (type.equals("Ljava/lang/String;")) {
            // We allow this in the non-JDI case.
            return value.toString();
        } else {
            // Try to upcast the value to the desired type.
            Object result = Types.cast(type, value);
            if (result == null) {
                throw new EvaluationException(NbBundle.getMessage(
                    getClass(), "error.cast.incompatible", type, vclass.getName()));
            }
            return result;
        }
    } // eval

    /**
     * Attempts to perform the type-cast on the JDI Value.
     *
     * @param  context  evaluation context.
     * @param  type     cast type.
     * @param  value    the value to convert.
     * @return  a value of the desired type.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object evalJdi(EvaluationContext context,
                             String type, Value value)
        throws EvaluationException {

        Type vtype = value.type();

        // Check if expecting an array.
        if (type.charAt(0) == '[') {
            if (!(vtype instanceof ArrayType)) {
                throw new EvaluationException(NbBundle.getMessage(
                    getClass(), "error.cast.incompatible", type, vtype.name()));
            }
            type = type.substring(1);
            try {
                vtype = ((ArrayType) vtype).componentType();
            } catch (ClassNotLoadedException cnle) {
                throw new EvaluationException(
                    NbBundle.getMessage(getClass(), "error.cast.vm"), cnle);
            }
        }

        // Check for primitive types.
        char ch = type.charAt(0);
        if (ch == 'Z' && vtype instanceof BooleanType) {
            return value;
        } else if (ch == 'B' && vtype instanceof ByteType) {
            return value;
        } else if (ch == 'C' && vtype instanceof CharType) {
            return value;
        } else if (ch == 'D' && vtype instanceof DoubleType) {
            return value;
        } else if (ch == 'F' && vtype instanceof FloatType) {
            return value;
        } else if (ch == 'I' && vtype instanceof IntegerType) {
            return value;
        } else if (ch == 'J' && vtype instanceof LongType) {
            return value;
        } else if (ch == 'S' && vtype instanceof ShortType) {
            return value;

        } else if (ch == 'L') {
            if (vtype instanceof ReferenceType) {
                // Check for class/interface type match.
                if (Types.isCompatible(type, (ReferenceType) vtype)) {
                    return value;
                }
            }
        }

        // If not the same type, can it be converted? Note, we do not
        // support (String) here as that involves invoking methods in
        // the debuggee and it is better to have the user do that
        // explicitly.
        ThreadReference tref = context.getThread();
        if (tref != null) {
            VirtualMachine vm = tref.virtualMachine();
            // Try to upcast the value to the desired type.
            Object result = Types.cast(type, value, vm);
            if (result == null) {
                throw new EvaluationException(NbBundle.getMessage(
                    getClass(), "error.cast.incompatible", type, vtype.name()));
            }
            return result;
        } else {
            throw new EvaluationException(NbBundle.getMessage(
                getClass(), "error.cast.location", type, vtype.name()));
        }
    } // evalJdi

    /**
     * Returns this operator's precedence value. The lower the value the
     * higher the precedence. The values are equivalent to those
     * described in the Java Language Reference book (2nd ed.), p 106.
     *
     * @return  precedence value.
     */
    public int precedence() {
        return 5;
    } // precedence

    /**
     * Returns the signature of the type this node represents. If the type
     * is void, or otherwise unrecognizable, an exception is thrown.
     *
     * @param  context  evaluation context.
     * @return  type signature.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected String type(EvaluationContext context) throws EvaluationException {
        return Types.typeNameToJNI(typeName);
    } // type
} // TypeCastOperatorNode
