/*********************************************************************
 *
 *      Copyright (C) 2003-2004 Nathan Fiedler
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
 * $Id: TypeCastOperatorNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;
import com.bluemarsh.jswat.util.Types;
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
                Bundle.getString("error.cast.type", typeName));
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
        boolean isArray = false;
        if (type.charAt(0) == '[') {
            if (!vclass.isArray()) {
                throw new EvaluationException(Bundle.getString(
                    "error.cast.incompatible", type, vclass.getName()));
            }
            isArray = true;
            type = type.substring(1);
            vclass = vclass.getComponentType();
        }

        // Check for primitive types.
        char ch = type.charAt(0);
        if (ch == 'Z' && vclass.isAssignableFrom(Boolean.class)) {
            return value;
        } else if (ch == 'B' && vclass.isAssignableFrom(Byte.class)) {
            return value;
        } else if (ch == 'C' && vclass.isAssignableFrom(Character.class)) {
            return value;
        } else if (ch == 'D' && vclass.isAssignableFrom(Double.class)) {
            return value;
        } else if (ch == 'F' && vclass.isAssignableFrom(Float.class)) {
            return value;
        } else if (ch == 'I' && vclass.isAssignableFrom(Integer.class)) {
            return value;
        } else if (ch == 'J' && vclass.isAssignableFrom(Long.class)) {
            return value;
        } else if (ch == 'S' && vclass.isAssignableFrom(Short.class)) {
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
                throw new EvaluationException(Bundle.getString(
                    "error.cast.incompatible", type, vclass.getName()));
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
        boolean isArray = false;
        if (type.charAt(0) == '[') {
            if (!(vtype instanceof ArrayType)) {
                throw new EvaluationException(Bundle.getString(
                    "error.cast.incompatible", type, vtype.name()));
            }
            isArray = true;
            type = type.substring(1);
            try {
                vtype = ((ArrayType) vtype).componentType();
            } catch (ClassNotLoadedException cnle) {
                throw new EvaluationException(
                    Bundle.getString("error.cast.vm"), cnle);
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
                throw new EvaluationException(Bundle.getString(
                    "error.cast.incompatible", type, vtype.name()));
            }
            return result;
        } else {
            throw new EvaluationException(Bundle.getString(
                "error.cast.location", type, vtype.name()));
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
