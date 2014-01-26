/*********************************************************************
 *
 *      Copyright (C) 2004 Nathan Fiedler
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
 * $Id: JoinOperatorNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VoidValue;

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
    public JoinOperatorNode(Token node) {
        super(node);
    }

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  the result.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        Object result = null;
        Node n1 = getChild(0);
        Object o1 = n1.evaluate(context);
        Node n2 = getChild(1);
        String name = n2.getToken().getText();
        if (o1 instanceof PrimitiveValue) {
            String msg = Bundle.getString(
                "error.join.cannot.primitive");
            throw new EvaluationException(msg);
        } else if (o1 instanceof VoidValue) {
            String msg = Bundle.getString(
                "error.join.cannot.void");
            throw new EvaluationException(msg);
        } else if (o1 instanceof ArrayReference) {
            // name may be 'length'; anything else is an error
            if (name.equals("length")) {
                ArrayReference arr = (ArrayReference) o1;
                return new Integer(arr.length());
            } else {
                String msg = Bundle.getString(
                    "error.join.array.length", name);
                throw new EvaluationException(msg);
            }
        } else if (o1 instanceof ObjectReference) {
            // name must be a field reference
            ObjectReference obj = (ObjectReference) o1;
            ReferenceType clazz = obj.referenceType();
            Field field = clazz.fieldByName(name);
            if (field == null) {
                String msg = Bundle.getString(
                    "error.join.unknown.field", name,
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
                String msg = Bundle.getString(
                    "error.join.unknown.field", name,
                    n1.getToken().getText(), clazz.name());
                throw new EvaluationException(msg);
            }
            valueContainer = field;
            fieldContainer = clazz;
            result = clazz.getValue(field);
        } else if (o1 instanceof ClassnamePart) {
            // merge the classname parts together
            result = new IdentifierNode(n2.getToken(), o1 + "." + name);
        } else {
            String msg = Bundle.getString(
                "error.join.unknown.value", name,
                n1.getToken().getText());
            throw new EvaluationException(msg);
        }
        // Note that the method invocation case is handled at parse time;
        // this operator is only concerned with joining field references.

        return result;
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
                String name = getChild(1).getToken().getText();
                throw new EvaluationException(
                    Bundle.getString("error.var.notafield", name));
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
                String name = getChild(1).getToken().getText();
                throw new EvaluationException(
                    Bundle.getString("error.var.cnamepart", name));
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
        StringBuffer buf = new StringBuffer();
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
    private void mergeChildren(StringBuffer buf, Node node)
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
            String msg = Bundle.getString(
                "error.join.unknown.child",
                node.getClass().getName());
            throw new EvaluationException(msg);
        }
    }

    /**
     * Returns this operator's precedence value. The lower the value the
     * higher the precedence. The values are equivalent to those
     * described in the Java Language Reference book (2nd ed.), p 106.
     *
     * @return  precedence value.
     */
    public int precedence() {
        return 3;
    }
}
