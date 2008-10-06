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
 * $Id: EvaluationContext.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

/**
 * Class EvaluationContext encapsulates the information pertaining
 * to the evaluation of an expression. It includes references to the
 * original expression, parsed abstract syntax tree, and elements
 * needed for evaluation by some operators.
 *
 * @author  Nathan Fiedler
 */
public class EvaluationContext {
    /** The original expression. */
    private String expr;
    /** Root of node tree. */
    private RootNode root;
    /** JDI thread. */
    private ThreadReference thread;
    /** Thread stack frame index. */
    private int frame;

    /**
     * Constructs a EvaluationContext with the given information.
     *
     * @param  expr    original expression.
     * @param  root    parsed AST root node.
     * @param  thread  JDI thread; null if no thread set.
     * @param  frame   stack frame index, if thread is given.
     */
    public EvaluationContext(String expr, RootNode root,
                             ThreadReference thread, int frame) {
        this.expr = expr;
        this.root = root;
        this.thread = thread;
        this.frame = frame;
    }

    /**
     * Returns the original expression.
     *
     * @return  original expression.
     */
    public String getExpression() {
        return expr;
    }

    /**
     * Returns the thread stack frame index.
     *
     * @return  stack frame index.
     */
    public int getFrame() {
        return frame;
    }

    /**
     * Get the current location (uses the thread and stack frame).
     *
     * @return  current point of execution in the debuggee.
     * @throws  IncompatibleThreadStateException
     *          if thread is not suspended properly.
     */
    public Location getLocation() throws IncompatibleThreadStateException {
        return thread.frame(frame).location();
    }

    /**
     * Returns the root of the AST.
     *
     * @return  root node.
     */
    public RootNode getRoot() {
        return root;
    }

    /**
     * Get the stack frame.
     *
     * @return  stack frame in the debuggee.
     * @throws  IncompatibleThreadStateException
     *          if thread is not suspended properly.
     */
    public StackFrame getStackFrame() throws IncompatibleThreadStateException {
        return thread.frame(frame);
    }

    /**
     * Returns the thread reference.
     *
     * @return  JDI thread.
     */
    public ThreadReference getThread() {
        return thread;
    }
}
