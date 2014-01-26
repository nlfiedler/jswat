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
 * $Id: Evaluator.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;
import com.bluemarsh.jswat.parser.java.lexer.Lexer;
import com.bluemarsh.jswat.parser.java.lexer.LexerException;
import com.bluemarsh.jswat.parser.java.node.TWhiteSpace;
import com.sun.jdi.ThreadReference;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

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
    } // Evaluator

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
                throw new EvaluationException(ioe);
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
    } // buildTree

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
            String msg = Bundle.getString("error.var.cnamepart", val);
            throw new EvaluationException(msg);
        }
        return val;
    } // evaluate

    /**
     * Turn the debugging features of the expression evaluator on or off.
     *
     * @param  debug  true to begin debug mode, false otherwise.
     */
    public void setDebug(boolean debug) {
        debugFlag = debug;
    } // setDebug

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
    } // main
} // Evaluator
