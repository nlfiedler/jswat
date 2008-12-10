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
 * are Copyright (C) 2007-2008. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: InstanceofOperatorNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.core.util.Types;
import com.bluemarsh.jswat.parser.node.Token;
import com.sun.jdi.ArrayType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import org.openide.util.NbBundle;

/**
 * Class InstanceofOperatorNode implements the instanceof operator.
 *
 * @author  Nathan Fiedler
 */
class InstanceofOperatorNode extends BinaryOperatorNode {

    /**
     * Creates a new instance of InstanceofOperatorNode.
     *
     * @param  node  lexical token.
     */
    public InstanceofOperatorNode(Token node) {
        super(node);
    }

    @Override
    protected Object eval(EvaluationContext context) throws EvaluationException {
        Node lChildNode = getChild(0);
        Node rChildNode = getChild(1);
        Object lChild = lChildNode.evaluate(context);
        Object rChild = rChildNode.evaluate(context);
        VirtualMachine vm = null;
        try {
            vm = context.getLocation().virtualMachine();
        } catch (IncompatibleThreadStateException itse) {
            throw new EvaluationException(NbBundle.getMessage(
                    InstanceofOperatorNode.class, "error.thread.state"));
        }
        if (lChild == null) {
            // If left-side is null, then instanceof is false.
            return Boolean.FALSE;
        } else if (rChild instanceof ReferenceType) {
            if (lChild instanceof Value) {
                String lChildSig = ((Value) lChild).type().signature();
                // Is this an array reference? If so, compare the component types.
                if (rChild instanceof ArrayType) {
                    if (lChildSig.charAt(0) != '[') {
                        throw new EvaluationException(NbBundle.getMessage(
                                InstanceofOperatorNode.class, "error.instanceof.array",
                                lChildNode.getToken().getText(),
                                rChildNode.getToken().getText()));
                    }
                    lChildSig = lChildSig.substring(1);
                    // This will return false if the type is not yet loaded.
                    String sig = ((ArrayType) rChild).componentSignature();
                    rChild = Types.signatureToType(sig, vm);
                }
                if (Types.isCompatible(lChildSig, (ReferenceType) rChild)) {
                    return Boolean.TRUE;
                }
            } else {
                throw new EvaluationException(NbBundle.getMessage(
                        InstanceofOperatorNode.class, "error.instanceof.value",
                        lChildNode.getToken().getText()));
            }
        } else {
            throw new EvaluationException(NbBundle.getMessage(
                    InstanceofOperatorNode.class, "error.instanceof.type",
                    rChildNode.getToken().getText()));
        }
        return Boolean.FALSE;
    }

    @Override
    public int precedence() {
        return 9;
    }
}
