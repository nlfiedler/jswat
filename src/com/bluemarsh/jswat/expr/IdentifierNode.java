/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Nathan Fiedler
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
 * $Id: IdentifierNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VoidType;
import java.util.List;

/**
 * Class IdentifierNode represents an identifier. It may refer to a
 * local variable, field, or a class name.
 *
 * @author  Nathan Fiedler
 */
class IdentifierNode extends AbstractNode implements JoinableNode, VariableNode {
    /** Identifier name of this identifier node. */
    private String identifierName;
    /** Reference to the local variable, field, object, or class. */
    private Object valueContainer;
    /** Reference to the object or class containing the field. */
    private Object fieldContainer;

    /**
     * Constructs a IdentifierNode with the given identifier name.
     *
     * @param  node  lexical token.
     * @param  name  identifier name.
     */
    public IdentifierNode(Token node, String name) {
        super(node);
        identifierName = name;
    }

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  value.
     * @throws  EvaluationException
     *          if there was an evaluation error.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        // Evaluate the name as a variable reference.
        ThreadReference th = context.getThread();
        if (th == null) {
            throw new EvaluationException(
                Bundle.getString("error.ident.thread"));
        }
        try {
            StackFrame frame = context.getStackFrame();
            if (frame == null) {
                String msg = Bundle.getString(
                    "error.ident.stack");
                throw new EvaluationException(msg);
            }
            Location location = frame.location();

            // Could it be 'this'?
            if (identifierName.equals("this")) {
                ObjectReference obj = frame.thisObject();
                if (obj == null) {
                    throw new EvaluationException(Bundle.getString(
                        "error.ident.this.none"));
                }
                valueContainer = obj;
                return obj;
            }
            // Check if name is a visible local variable or a field.
            ReferenceType clazz = location.declaringType();
            Field field = clazz.fieldByName(identifierName);
            LocalVariable localVar = frame.visibleVariableByName(identifierName);
            if (localVar == null && field == null) {
                // Maybe it is a classname, or part of one.
                VirtualMachine vm = th.virtualMachine();
                List classes = vm.classesByName(identifierName);
                if (classes.size() > 0) {
                    valueContainer = classes.get(0);
                    return valueContainer;
                } else {
                    // It may be a 'core' package class.
                    classes = vm.classesByName("java.lang." + identifierName);
                    if (classes.size() > 0) {
                        valueContainer = classes.get(0);
                        return valueContainer;
                    } else {
                        // Possibly this is just a classname part.
                        return new ClassnamePart(identifierName);
                    }
                }
            } else if (localVar != null) {
                // Locals shadow fields so handle them first.
                Type type = localVar.type();
                if (type instanceof VoidType) {
                    String msg = Bundle.getString(
                        "error.unsupportedType", identifierName, type.name());
                    throw new EvaluationException(msg);
                }
                valueContainer = localVar;
                return frame.getValue(localVar);

            } else {
                ObjectReference thiso = frame.thisObject();
                if (!field.isStatic() && thiso == null) {
                    String mname = location.method().name();
                    String msg = Bundle.getString(
                        "error.staticAccess", identifierName, mname);
                    throw new EvaluationException(msg);
                } else {
                    Type type = field.type();
                    if (thiso == null) {
                        fieldContainer = clazz;
                    } else {
                        fieldContainer = thiso;
                    }
                    if (type instanceof VoidType) {
                        String msg = Bundle.getString(
                            "error.unsupportedType", identifierName, type.name());
                        throw new EvaluationException(msg);
                    }
                    valueContainer = field;
                    return thiso == null
                        ? clazz.getValue(field) : thiso.getValue(field);
                }
            }
        } catch (AbsentInformationException aie) {
            throw new EvaluationException(
                Bundle.getString("error.ident.failed", aie), aie);
        } catch (ClassNotLoadedException cnle) {
            throw new EvaluationException(
                Bundle.getString("error.ident.failed", cnle), cnle);
        }
    }

    /**
     * Simply returns the name of the identifier, without evaluation.
     *
     * @return  identifier name.
     */
    public String getIdentifier() {
        return identifierName;
    }

    /**
     * Returns the thing the field is contained in, either an ObjectReference
     * or a ReferenceType.
     *
     * @param  context  evaluation context.
     * @return  object or class.
     * @throws  EvaluationException
     *          if there was an evaluation error.
     */
    public Object getFieldContainer(EvaluationContext context)
        throws EvaluationException {
        if (fieldContainer == null) {
            evaluate(context);
            if (fieldContainer == null) {
                throw new EvaluationException(
                    Bundle.getString("error.var.notafield",
                        identifierName));
            }
        }
        return fieldContainer;
    }

    /**
     * Returns the thing this identifier refers to rather than its value;
     * either a Field, LocalVariable, ObjectReference, or ReferenceType.
     *
     * @param  context  evaluation context.
     * @return  field, variable, object, or class.
     * @throws  EvaluationException
     *          if there was an evaluation error.
     */
    public Object getValueContainer(EvaluationContext context)
        throws EvaluationException {
        if (valueContainer == null) {
            evaluate(context);
            if (valueContainer == null) {
                throw new EvaluationException(
                    Bundle.getString("error.var.cnamepart",
                        identifierName));
            }
        }
        return valueContainer;
    }
}
