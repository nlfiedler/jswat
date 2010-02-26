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
 * are Copyright (C) 2002-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.core.util.Classes;
import com.bluemarsh.jswat.core.util.Strings;
import com.bluemarsh.jswat.parser.node.Token;
import com.bluemarsh.jswat.parser.lexer.Lexer;
import com.bluemarsh.jswat.parser.lexer.LexerException;
import com.bluemarsh.jswat.parser.node.TWhiteSpace;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.CharValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * Class Evaluator evaluates Java-like expressions and returns the result.
 * Method invocations and variable references are available only when the
 * current thread is set in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class Evaluator {

    /** Logger for gracefully reporting unexpected errors. */
    private static final Logger logger = Logger.getLogger(
            Evaluator.class.getName());
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
    private RootNode buildTree() throws EvaluationException {
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
     * Generate a String to represent the given value. For arrays, this
     * will print the index and value for each element, for Strings it
     * will return the string value, and for Objects, it will invoke the
     * <code>toString()</code> method on the object.
     *
     * @param  value   the value to be printed.
     * @param  thread  thread on which to invoke methods.
     * @return  the pretty form of the value.
     */
    public static String prettyPrint(Object value, ThreadReference thread) {
        StringBuilder buf = new StringBuilder();
        if (value instanceof ArrayReference) {
            ArrayReference array = (ArrayReference) value;
            buf.append('[');
            if (array.length() > 0) {
                buf.append("0: ");
                Value element = array.getValue(0);
                buf.append(element == null ? "null" : element.toString());
                for (int index = 1; index < array.length(); index++) {
                    buf.append(", ");
                    buf.append(index);
                    buf.append(": ");
                    element = array.getValue(index);
                    buf.append(element == null ? "null" : element.toString());
                }
            }
            buf.append(']');

        } else if (value instanceof StringReference) {
            buf.append(value.toString());

        } else if (value instanceof ObjectReference) {
            try {
                ObjectReference object = (ObjectReference) value;
                ReferenceType clazz = object.referenceType();
                List<Method> methods = clazz.methodsByName("toString",
                        "()Ljava/lang/String;");
                List<Value> arguments = Collections.emptyList();
                Value result = Classes.invokeMethod(object, null, thread,
                        methods.get(0), arguments);
                buf.append(result != null ? Strings.trimQuotes(result.toString()) : "null");
            } catch (ExecutionException ee) {
                logger.log(Level.SEVERE, null, ee);
            }

        } else if (value instanceof CharValue) {
            buf.append("\\u");
            buf.append(Strings.toHexString(((CharValue) value).value()));

        } else if (value == null) {
            buf.append("null");

        } else {
            buf.append(value.toString());
        }
        return buf.toString();
    }

    /**
     * Turn the debugging features of the expression evaluator on or off.
     *
     * @param  debug  true to begin debug mode, false otherwise.
     */
    public void setDebug(boolean debug) {
        debugFlag = debug;
    }
}
