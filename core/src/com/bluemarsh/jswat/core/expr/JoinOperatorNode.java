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
 * are Copyright (C) 2004-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VoidValue;
import org.openide.util.NbBundle;

/**
 * Class JoinOperatorNode represents the dot between parts of a name.
 * The name can be part of a class (e.g. java.lang.Object) or a variable
 * reference (e.g. this.field1.objA).
 *
 * @author  Nathan Fiedler
 */
class JoinOperatorNode extends BinaryOperatorNode implements JoinableNode, VariableNode {

    /** Reference to the local variable, field, object, or class. */
    private Object valueContainer;
    /** Reference to the object or class containing the field. */
    private Object fieldContainer;

    /**
     * Constructs a JoinOperatorNode associated with the given token.
     *
     * @param  node  lexical token.
     */
    JoinOperatorNode(Token node) {
        super(node);
    }

    @Override
    protected Object eval(EvaluationContext context)
            throws EvaluationException {

        Object result = null;
        Node n1 = getChild(0);
        Object o1 = n1.evaluate(context);
        Node n2 = getChild(1);
        String name = n2.getToken().getText();
        if (o1 instanceof PrimitiveValue) {
            String msg = NbBundle.getMessage(
                    JoinOperatorNode.class, "error.join.cannot.primitive");
            throw new EvaluationException(msg);
        } else if (o1 instanceof VoidValue) {
            String msg = NbBundle.getMessage(
                    JoinOperatorNode.class, "error.join.cannot.void");
            throw new EvaluationException(msg);
        } else if (o1 instanceof ArrayReference) {
            // name may be 'length'; anything else is an error
            if (name.equals("length")) {
                ArrayReference arr = (ArrayReference) o1;
                return new Integer(arr.length());
            } else {
                String msg = NbBundle.getMessage(
                        JoinOperatorNode.class, "error.join.array.length", name);
                throw new EvaluationException(msg);
            }
        } else if (o1 instanceof ObjectReference) {
            // name must be a field reference
            ObjectReference obj = (ObjectReference) o1;
            ReferenceType clazz = obj.referenceType();
            Field field = clazz.fieldByName(name);
            if (field == null) {
                String msg = NbBundle.getMessage(
                        JoinOperatorNode.class, "error.join.unknown.field", name,
                        n1.getToken().getText(), clazz.name());
                throw new EvaluationException(msg);
            }
            valueContainer = field;
            fieldContainer = obj;
            result = obj.getValue(field);
        } else if (o1 instanceof ReferenceType) {
            // name must be a field reference
            ReferenceType clazz = (ReferenceType) o1;
            Field field = clazz.fieldByName(name);
            if (field == null) {
                String msg = NbBundle.getMessage(
                        JoinOperatorNode.class, "error.join.unknown.field", name,
                        n1.getToken().getText(), clazz.name());
                throw new EvaluationException(msg);
            }
            valueContainer = field;
            fieldContainer = clazz;
            result = clazz.getValue(field);
        } else if (o1 instanceof ClassnamePart) {
            // merge the classname parts together and evaluate
            Node in = new IdentifierNode(n2.getToken(), o1 + "." + name);
            result = in.evaluate(context);
        } else {
            String msg = NbBundle.getMessage(
                    JoinOperatorNode.class, "error.join.unknown.value", name,
                    n1.getToken().getText());
            throw new EvaluationException(msg);
        }
        // Note that the method invocation case is handled at parse time;
        // this operator is only concerned with joining field references.

        return result;
    }

    @Override
    public Object getFieldContainer(EvaluationContext context) throws
            EvaluationException {
        if (fieldContainer == null) {
            evaluate(context);
            if (fieldContainer == null) {
                String name = getChild(1).getToken().getText();
                throw new UnknownReferenceException(
                        NbBundle.getMessage(JoinOperatorNode.class,
                        "error.var.notafield", name));
            }
        }
        return fieldContainer;
    }

    @Override
    public Object getValueContainer(EvaluationContext context) throws
            EvaluationException {
        if (valueContainer == null) {
            evaluate(context);
            if (valueContainer == null) {
                String name = getChild(1).getToken().getText();
                throw new UnknownReferenceException(
                        NbBundle.getMessage(JoinOperatorNode.class,
                        "error.var.cnamepart", name));
            }
        }
        return valueContainer;
    }

    /**
     * Merges the names of the children identifier nodes to create a single
     * string.
     *
     * @return  the children names, joined with periods (.).
     * @throws  EvaluationException
     *          if the children are not of the correct type.
     */
    public String mergeChildren() throws EvaluationException {
        StringBuilder buf = new StringBuilder();
        mergeChildren(buf, this);
        return buf.toString();
    }

    /**
     * Merges the names of the given node to the string buffer.
     *
     * @param  buf   string buffer to concatenate to.
     * @param  node  node to process.
     * @throws  EvaluationException
     *          if the children are not of the correct type.
     */
    private void mergeChildren(StringBuilder buf, Node node)
            throws EvaluationException {
        if (node instanceof JoinOperatorNode) {
            JoinOperatorNode jon = (JoinOperatorNode) node;
            if (childCount() > 0) {
                jon.mergeChildren(buf, jon.getChild(0));
                if (childCount() > 1) {
                    jon.mergeChildren(buf, jon.getChild(1));
                }
            }
        } else if (node instanceof IdentifierNode) {
            IdentifierNode in = (IdentifierNode) node;
            if (buf.length() > 0) {
                buf.append('.');
            }
            buf.append(in.getIdentifier());
        } else {
            String msg = NbBundle.getMessage(JoinOperatorNode.class,
                    "error.join.unknown.child", node.getClass().getName());
            throw new EvaluationException(msg);
        }
    }

    @Override
    public int precedence() {
        return 3;
    }
}
