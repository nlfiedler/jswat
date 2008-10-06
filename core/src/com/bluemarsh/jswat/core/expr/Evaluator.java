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
 * $Id: Evaluator.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import com.bluemarsh.jswat.parser.lexer.Lexer;
import com.bluemarsh.jswat.parser.lexer.LexerException;
import com.bluemarsh.jswat.parser.node.TWhiteSpace;
import com.sun.jdi.ThreadReference;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import org.openide.util.NbBundle;

/**
 * Class Evaluator evaluates Java-like expressions and returns the result.
 * Method invocations and variable references are available only when the
 * current thread is set in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class Evaluator {
    /** The expression to evaluate. */
    private String expression;
    /** Root of the parsed abstract syntax tree. */
    private RootNode root;
    /** Debugging flag. */
    private boolean debugFlag;

    /**
     * Construct an Evaluator to evaluate the given expression.
     *
     * @param  expr  expression to evaluate.
     */
    public Evaluator(String expr) {
        expression = expr;
    }

    /**
     * Builds the tree of operators and operands.
     *
     * @return  root node of the parsed tree.
     * @throws  EvaluationException
     *          if a lexer or I/O exception occurs.
     */
    protected RootNode buildTree() throws EvaluationException {
        StringReader sr = new StringReader(expression);
        PushbackReader pbr = new PushbackReader(sr);
        Lexer lexer = new Lexer(pbr);
        TreeBuilder builder = new TreeBuilder();
        try {
            builder.setPrevToken(new TWhiteSpace(" "));
            while (!builder.doneProcessing()) {
                Token token = lexer.next();
                token.apply(builder);
                if (!(token instanceof TWhiteSpace)) {
                    // Don't save the whitespace as that will confuse the tree
                    // builder, which is designed without consideration for it.
                    builder.setPrevToken(token);
                }
            }
        } catch (LexerException le) {
            throw new EvaluationException(le);
        } catch (IOException ioe) {
            throw new EvaluationException(ioe);
        } finally {
            try {
                pbr.close();
            } catch (IOException ioe) {
                // Of no consequence.
            }
        }
        EvaluationException err = builder.getException();
        if (err != null) {
            if (debugFlag) {
                builder.dumpStacks(System.err);
            }
            throw err;
        } else {
            return builder.getRoot();
        }
    }

    /**
     * Evaluates the expression and returns the result as a string.
     *
     * @param  thread  thread used to access debuggee information;
     *                 may be null if no active debuggee available.
     * @param  frame   stack frame used to access debuggee information;
     *                 ignored if thread is null.
     * @return  result of evaluation; null if expression was null or
     *          the empty string.
     * @throws  EvaluationException
     *          if the expression could not be evaluated.
     */
    public Object evaluate(ThreadReference thread, int frame)
        throws EvaluationException {
        if (root == null) {
            if (expression == null) {
                // Simple base case: no expression whatsoever.
                return null;
            } else {
                root = buildTree();
            }
        }
        EvaluationContext context = new EvaluationContext(
            expression, root, thread, frame);
        Object val = root.evaluate(context);
        // Check that the final result is not something erroneous.
        if (val instanceof ClassnamePart) {
            // This indicates a reference to a non-existent variable.
            String msg = NbBundle.getMessage(getClass(), "error.var.cnamepart", val);
            throw new UnknownReferenceException(msg);
        }
        return val;
    }

    /**
     * Turn the debugging features of the expression evaluator on or off.
     *
     * @param  debug  true to begin debug mode, false otherwise.
     */
    public void setDebug(boolean debug) {
        debugFlag = debug;
    }

    /**
     * Test harness for the purpose of debugging.
     *
     * @param  args  command-line arguments.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: Evaluator '<expression>'");
        } else {
            String expr = args[0];
            Evaluator eval = new Evaluator(expr);
            try {
                Object o = eval.evaluate(null, 0);
                System.out.println("Result class: " + o.getClass());
                System.out.println("Result value: " + o);
            } catch (EvaluationException ee) {
                System.err.println(ee.getMessage());
            }
        }
    }
}
