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
 * are Copyright (C) 2002-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: IdentifierNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Class IdentifierNode represents an identifier. It may refer to a local
 * variable, field, or a class name.
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
            throw new MissingContextException(
                NbBundle.getMessage(IdentifierNode.class, "error.ident.thread"));
        }
        StackFrame frame = null;
        try {
            frame = context.getStackFrame();
        } catch (IncompatibleThreadStateException itse) {
            throw new MissingContextException(NbBundle.getMessage(
                    IdentifierNode.class, "error.thread.state"));
        }
        if (frame == null) {
            String msg = NbBundle.getMessage(
                IdentifierNode.class, "error.ident.stack");
            throw new MissingContextException(msg);
        }
        Location location = frame.location();

        // Could it be 'this'?
        if (identifierName.equals("this")) {
            ObjectReference obj = frame.thisObject();
            if (obj == null) {
                throw new UnknownReferenceException(NbBundle.getMessage(
                    IdentifierNode.class, "error.ident.this.none"));
            }
            valueContainer = obj;
            return obj;
        }
        // Check if name is a visible local variable or a field.
        ReferenceType clazz = location.declaringType();
        Field field = clazz.fieldByName(identifierName);
        LocalVariable localVar = null;
        try {
            localVar = frame.visibleVariableByName(identifierName);
        } catch (AbsentInformationException aie) {
            // Missing local variable info is not a fatal scenario.
        }
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
            valueContainer = localVar;
            return frame.getValue(localVar);

        } else {
            ObjectReference thiso = frame.thisObject();
            if (!field.isStatic() && thiso == null) {
                String mname = location.method().name();
                String msg = NbBundle.getMessage(IdentifierNode.class,
                    "error.staticAccess", identifierName, mname);
                throw new UnknownReferenceException(msg);
            } else {
                fieldContainer = thiso == null ? clazz : thiso;
                valueContainer = field;
                return thiso == null
                    ? clazz.getValue(field) : thiso.getValue(field);
            }
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
                throw new UnknownReferenceException(
                    NbBundle.getMessage(IdentifierNode.class,
                        "error.var.notafield", identifierName));
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
                throw new UnknownReferenceException(
                    NbBundle.getMessage(IdentifierNode.class,
                        "error.var.cnamepart", identifierName));
            }
        }
        return valueContainer;
    }
}
