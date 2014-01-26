/*********************************************************************
 *
 *      Copyright (C) 2004 Antonia Kwok
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
 * $Id: AssignOperatorNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;
import com.bluemarsh.jswat.util.Types;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.TypeComponent;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;

/**
 * Class AssignOperatorNode implements the assign operator (=).
 *
 * @author  Antonia Kwok
 */
class AssignOperatorNode extends BinaryOperatorNode {
    
    /** 
     * Constructs a AssignOperatorNode.
     *
     * @param  node  lexical token.
     */
    public AssignOperatorNode(Token node) {
        super(node);
    }
    
    /**
     * Assign operator is a binary operator. Its left child must be an 
     * IdentiferNode or ArrayNode. The IdentifierNode may refer to a local 
     * variable or field. The right child must have data type that matches the 
     * left child.
     *
     * @param  context  evaluation context.
     * @return  value.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        // Left child should be an identifier node.
        Node lChild = getChild(0);
        Object value = null;
        if (lChild instanceof VariableNode) {
            // Check type of identifier and compare to assigment value.
            VariableNode vChild = (VariableNode) lChild;
            String lChildSig = lChild.getType(context);
            String rChildSig = getChild(1).getType(context);

            // Compare the argument types for similarity.
            VirtualMachine vm = context.getLocation().virtualMachine();
            Type rChildType = Types.signatureToType(rChildSig, vm);
            if (rChildSig.equals(lChildSig)
                || (rChildType instanceof ReferenceType
                    && Types.isCompatible(lChildSig, (ReferenceType) rChildType))
                || (Types.canWiden(lChildSig, rChildType))) {
                value = setValue(context, vChild, getChild(1).evaluate(context));
            } else {
                // They don't match at all.
                throw new EvaluationException(Bundle.getString(
                        "error.assign.type.mismatch",
                        lChildSig, rChildSig));
            }
        } else {
            throw new EvaluationException(Bundle.getString(
                    "error.assign.invalid.target",
                    lChild.getClass()));
        }
        
        return value;
    }
    
    /**
     * Assign the value to the variable reference. This function does not
     * handle array reference.
     *
     * @param  context  evaluation context.
     * @param  vNode    assignment target.
     * @param  rValue   assignment value.
     */
    private Object setValue(EvaluationContext context, VariableNode vNode,
                            Object rValue)
        throws EvaluationException {
                   
        // Get the referenced field or local variable.
        ThreadReference th = context.getThread();
        StackFrame frame = context.getStackFrame();

        // Check what type of identifier we are dealing with here.
        Object vcon = vNode.getValueContainer(context);
        if (vcon == null) {
            // This is unlikely.
            throw new EvaluationException();
        }
        VirtualMachine vm = th.virtualMachine();
        Value mirror;
        try {
            mirror = (Value) Types.mirrorOf(rValue, vm);
        } catch (ClassCastException cce) {
            throw new EvaluationException(cce);
        }
        try {
            // Set the value to the identifier accordingly.
            if (vcon instanceof LocalVariable) { 
                frame.setValue((LocalVariable) vcon, mirror);
            } else if (vcon instanceof Field) {
                // Prohibit assignment to final fields
                if (((TypeComponent)vcon).isFinal()) {
                    throw new EvaluationException(Bundle.getString(
                            "error.assign.final",
                            vNode.getToken().getText()));
                }
                Object fcon = vNode.getFieldContainer(context);
                if (fcon instanceof ObjectReference) {
                    ((ObjectReference) fcon).setValue((Field) vcon, mirror);
                } else if (fcon instanceof ClassType) {
                    ((ClassType) fcon).setValue((Field) vcon, mirror);
                } else {
                    throw new EvaluationException(Bundle.getString(
                            "error.assign.invalid.container",
                            fcon.getClass()));
                }
            } else {
                throw new EvaluationException(Bundle.getString(
                        "error.assign.invalid.target",
                        vcon.getClass()));
            }
        } catch (Exception e) {
            throw new EvaluationException(Bundle.getString(
                    "error.assign.exception", e));
        }
        return mirror;
    }
    
    /**
     * Returns this operator's precedence value. The lower the value the
     * higher the precedence. The values are equivalent to those
     * described in the Java Language Reference book (2nd ed.), p 106.
     *
     * @return  precedence value.
     */
    public int precedence() {
        return 17;
    }
}
