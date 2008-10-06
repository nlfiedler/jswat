/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2002-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: EvaluationContext.java 15 2007-06-03 00:01:17Z nfiedler $
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
