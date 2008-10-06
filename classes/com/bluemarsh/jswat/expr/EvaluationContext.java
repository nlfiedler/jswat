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
 * $Id: EvaluationContext.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

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
    } // EvaluationContext

    /**
     * Returns the original expression.
     *
     * @return  original expression.
     */
    public String getExpression() {
        return expr;
    } // getExpression

    /**
     * Returns the thread stack frame index.
     *
     * @return  stack frame index.
     */
    public int getFrame() {
        return frame;
    } // getFrame

    /**
     * Get the current location (uses the thread and stack frame).
     *
     * @return  current point of execution in the debuggee.
     * @throws  EvaluationException
     *          if getting the current location failed.
     */
    public Location getLocation() throws EvaluationException {
        try {
            StackFrame stack = thread.frame(frame);
            return stack.location();
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
    } // getLocation

    /**
     * Returns the root of the AST.
     *
     * @return  root node.
     */
    public RootNode getRoot() {
        return root;
    } // getRoot

    /**
     * Get the stack frame.
     *
     * @return  stack frame in the debuggee.
     * @throws  EvaluationException
     *          if getting the stack frame failed.
     */
    public StackFrame getStackFrame() throws EvaluationException {
        try {
            return thread.frame(frame);
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
    } // getStackFrame

    /**
     * Returns the thread reference.
     *
     * @return  JDI thread.
     */
    public ThreadReference getThread() {
        return thread;
    } // getThread
} // EvaluationContext
