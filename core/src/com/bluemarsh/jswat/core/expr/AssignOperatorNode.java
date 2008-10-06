/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core Module. The Initial Developer of the
 * Original Code is Antonia Kwok. Portions created by Antonia Kwok
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Antonia Kwok, Nathan Fiedler.
 *
 * $Id: AssignOperatorNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import com.bluemarsh.jswat.core.util.Types;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.TypeComponent;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import org.openide.util.NbBundle;

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
    
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        Node lChild = getChild(0);
        Node rChild = getChild(1);
        String lChildSig = lChild.getType(context);
        String rChildSig = rChild.getType(context);
        VirtualMachine vm = null;
        try {
            vm = context.getLocation().virtualMachine();
        } catch (IncompatibleThreadStateException itse) {
            throw new EvaluationException(NbBundle.getMessage(
                    AssignOperatorNode.class, "error.thread.state"));
        }
        Type rChildType = Types.signatureToType(rChildSig, vm);
        Object value = null;
        // Compare the argument types for similarity (allow null literal).
        if (rChildSig == null || rChildSig.equals(lChildSig) ||
                (rChildType instanceof ReferenceType &&
                Types.isCompatible(lChildSig, (ReferenceType) rChildType)) ||
                Types.canWiden(lChildSig, rChildType)) {
            if (lChild instanceof VariableNode) {
                VariableNode vChild = (VariableNode) lChild;
                value = setValue(context, vChild, rChild.evaluate(context));
            } else if (lChild instanceof ArrayNode) {
                ArrayNode aChild = (ArrayNode) lChild;
                ArrayReference aref = aChild.getArray(context);
                int index = aChild.getIndex(context);
                Object rValue = rChild.evaluate(context);
                Value mirror;
                try {
                    mirror = Types.mirrorOf(rValue, vm);
                } catch (ClassCastException cce) {
                    throw new EvaluationException(cce);
                }
                try {
                    aref.setValue(index, mirror);
                    value = mirror;
                } catch (ClassNotLoadedException cnle) {
                    throw new EvaluationException(NbBundle.getMessage(
                            AssignOperatorNode.class, "error.assign.exception", cnle));
                } catch (IndexOutOfBoundsException ioobe) {
                    throw new EvaluationException(NbBundle.getMessage(
                            AssignOperatorNode.class, "error.assign.exception", ioobe));
                } catch (InvalidTypeException ite) {
                    throw new EvaluationException(NbBundle.getMessage(
                            AssignOperatorNode.class, "error.assign.exception", ite));
                }
            } else {
                throw new EvaluationException(NbBundle.getMessage(
                        AssignOperatorNode.class, "error.assign.invalid.target",
                        lChild.getClass()));
            }
        } else {
            // They don't match at all.
            throw new EvaluationException(NbBundle.getMessage(
                    AssignOperatorNode.class, "error.assign.type.mismatch",
                    lChildSig, rChildSig));
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
        StackFrame frame = null;
        try {
            frame = context.getStackFrame();
        } catch (IncompatibleThreadStateException itse) {
            throw new EvaluationException(NbBundle.getMessage(
                    AssignOperatorNode.class, "error.thread.state"));
        }

        // Check what type of identifier we are dealing with here.
        Object vcon = vNode.getValueContainer(context);
        if (vcon == null) {
            // This is unlikely.
            throw new EvaluationException();
        }
        VirtualMachine vm = th.virtualMachine();
        Value mirror;
        try {
            mirror = Types.mirrorOf(rValue, vm);
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
                    throw new EvaluationException(NbBundle.getMessage(
                            AssignOperatorNode.class, "error.assign.final",
                            vNode.getToken().getText()));
                }
                Object fcon = vNode.getFieldContainer(context);
                if (fcon instanceof ObjectReference) {
                    ((ObjectReference) fcon).setValue((Field) vcon, mirror);
                } else if (fcon instanceof ClassType) {
                    ((ClassType) fcon).setValue((Field) vcon, mirror);
                } else {
                    throw new EvaluationException(NbBundle.getMessage(
                            AssignOperatorNode.class, "error.assign.invalid.container",
                            fcon.getClass()));
                }
            } else {
                throw new EvaluationException(NbBundle.getMessage(
                        AssignOperatorNode.class, "error.assign.invalid.target",
                        vcon.getClass()));
            }
        } catch (ClassNotLoadedException cnle) {
            throw new EvaluationException(NbBundle.getMessage(
                    AssignOperatorNode.class, "error.assign.exception", cnle));
        } catch (InvalidTypeException ite) {
            throw new EvaluationException(NbBundle.getMessage(
                    AssignOperatorNode.class, "error.assign.exception", ite));
        }
        return mirror;
    }
    
    public int precedence() {
        return 17;
    }
}
