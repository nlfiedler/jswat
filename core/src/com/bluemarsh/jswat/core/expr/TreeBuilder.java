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
 * The Original Software is the JSwat Core Module. The Initial Developer of the
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2002-2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.analysis.AnalysisAdapter;
import com.bluemarsh.jswat.parser.node.*;
import com.bluemarsh.jswat.core.util.Strings;
import java.io.PrintStream;
import java.util.EmptyStackException;
import java.util.Stack;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Class TreeBuilder builds out the abstract syntax tree based on the
 * tokenized infix expression.
 *
 * @author  Nathan Fiedler
 */
class TreeBuilder extends AnalysisAdapter {
    /** The states this finite machine can exist in. */
    private enum State { ARGUMENT, OPERATOR, APPEND }
    /** Current parser state. */
    private State searchState;
    /** True if processing is completed, either because the end of
     * the expression was reached, or there was an error. */
    private boolean doneProcessing;
    /** Indicates if an error occurred; null means no error. */
    private EvaluationException evalException;
    /** Root node of the tree. */
    private RootNode rootNode;
    /** Stack of arguments. */
    private Stack<Node> argumentStack;
    /** Stack of operators. */
    private Stack<Node> operatorStack;
    /** Counter for assisting in validating the expression. If non-zero,
     * parsing the (n-1)th nested method invocation. */
    private int methodCount;
    /** The last token we received (initially it's TWhitespace); used in
     * determining if we are encountering a method invocation or not. */
    private Token previousToken;

    // Infix expressions can be parsed from a series of tokens using two
    // stacks. One stack is used to hold parse trees under construction
    // (the argument stack), the other to hold operators (and left
    // parentheses, for matching purposes; the operator stack).

    // As we read in each new token (from left to right), we either push
    // the token (or a related tree) onto one of the stacks, or we
    // reduce the stacks by combining an operator with some arguments.
    // Along the way, it will be helpful to maintain a search state
    // which tells us whether we should see an argument or operator next
    // (the search state helps us to reject malformed expressions).

    /**
     * Translates the given string to a character. Handles character
     * escapes such as \r and Unicode escapes.
     *
     * @param  charStr  string representing a character.
     * @return  the character.
     */
    protected static char translateChar(String charStr) {
        if (charStr.length() == 0) {
            throw new IllegalArgumentException("empty character");
        }
        // May just be a single character.
        if (charStr.length() == 1) {
            return charStr.charAt(0);
        }
        if (charStr.charAt(0) == '\\') {
            char ch = charStr.charAt(1);
            if (ch == 'b') {
                return '\b';
            } else if (ch == 'f') {
                return '\f';
            } else if (ch == 't') {
                return '\t';
            } else if (ch == 'n') {
                return '\n';
            } else if (ch == 'r') {
                return '\r';

            } else if (ch == 'u') {
                // Unicode escape.
                String hex = charStr.substring(2);
                try {
                    int i = Integer.parseInt(hex, 16);
                    return (char) i;
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("invalid Unicode: "
                                                       + hex);
                }

            } else if (ch >= '0' && ch <= '3') {
                // Octal escape.
                String octal = charStr.substring(1);
                try {
                    int i = Integer.parseInt(octal, 8);
                    return (char) i;
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("invalid octal: "
                                                       + octal);
                }
            } else {
                return ch;
            }
        } else {
            throw new IllegalArgumentException("not a character: " + charStr);
        }
    } // translateChar

    /**
     * Processes the given string, looking for character escapes and
     * translating them to their actual values. Handles single character
     * escapes, Unicode, and octal escapes.
     *
     * @param  str  string to be processed.
     * @return  processed string.
     */
    protected static String translateString(String str) {
        int strlen = str.length();
        StringBuilder buf = new StringBuilder(strlen);
        for (int ii = 0; ii < strlen; ii++) {
            char ch = str.charAt(ii);
            if (ch == '\\') {
                ii++;
                ch = str.charAt(ii);
                if (ch == 'b') {
                    buf.append('\b');
                } else if (ch == 'f') {
                    buf.append('\f');
                } else if (ch == 't') {
                    buf.append('\t');
                } else if (ch == 'n') {
                    buf.append('\n');
                } else if (ch == 'r') {
                    buf.append('\r');

                } else if (ch == 'u') {
                    // Unicode character.
                    ii++;
                    String hex = str.substring(ii, ii + 4);
                    try {
                        int i = Integer.parseInt(hex, 16);
                        buf.append((char) i);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException(
                            "invalid Unicode: " + hex);
                    }
                    ii += 3; // for loop will increment i again

                } else if (ch >= '0' && ch <= '7') {
                    // Octal escape.
                    int jj = ii;
                    while (jj < strlen) {
                        char oc = str.charAt(jj);
                        if (oc < '0' || oc > '7') {
                            break;
                        }
                        jj++;
                    }
                    String octal = str.substring(ii, jj);
                    try {
                        int i = Integer.parseInt(octal, 8);
                        buf.append((char) i);
                    } catch (NumberFormatException nfe) {
                        throw new IllegalArgumentException("invalid octal: "
                                                           + octal);
                    }
                    ii = jj - 1; // for loop will increment i again

                } else {
                    buf.append(ch);
                }
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    } // translateString

    /**
     * Constructs a TreeBuilder.
     *
     * @param  context  parser context.
     */
    public TreeBuilder() {
        searchState = State.ARGUMENT;
        rootNode = new RootNode();
        argumentStack = new Stack<Node>();
        operatorStack = new Stack<Node>();
    } // TreeBuilder

    /**
     * Are we done yet?
     *
     * @return  True if done processing the expression.
     */
    public boolean doneProcessing() {
        return doneProcessing;
    } // doneProcessing

    /**
     * Dumps (and empties) the argument and operator stacks to the given
     * output stream.
     *
     * @param  out  output stream to dump to.
     */
    void dumpStacks(PrintStream out) {
        out.println("Argument stack...");
        while (!argumentStack.empty()) {
            Node n = argumentStack.pop();
            out.print(Utilities.getShortClassName(n.getClass()));
            out.print(": ");
            out.println(n.getToken().getText());
        }

        out.println();
        out.println("Operator stack...");
        while (!operatorStack.empty()) {
            Node n = operatorStack.pop();
            out.print(Utilities.getShortClassName(n.getClass()));
            out.print(": ");
            out.println(n.getToken().getText());
        }
    } // dumpStacks

    /**
     * Returns the parser exception, if any.
     *
     * @return  exception; null indicates no error.
     */
    public EvaluationException getException() {
        return evalException;
    } // getException

    /**
     * Returns the root node of the tree.
     *
     * @return  root node.
     */
    public RootNode getRoot() {
        return rootNode;
    } // getRoot

    /**
     * Handles the given literal node appropriately.
     *
     * @param  lit  literal node.
     */
    protected void handleLiteral(LiteralNode lit) {
        if (searchState == State.APPEND) {
            setError(Errors.Code.DOT_REQUIRES_ID, lit.getToken());
            return;
        }
        argumentStack.push(lit);
        searchState = State.OPERATOR;
    } // handleLiteral

    /**
     * Handles the given type node appropriately.
     *
     * @param  type  type node.
     */
    protected void handleType(TypeNode type) {
        if (searchState == State.APPEND) {
            setError(Errors.Code.DOT_REQUIRES_ID, type.getToken());
            return;
        }
        argumentStack.push(type);
        searchState = State.OPERATOR;
    } // handleType

    /**
     * Parse the string as a number, either integer or floating.
     *
     * @param  token  token to be parsed as a number.
     * @param  radix  radix to parse token as; if zero, token is
     *                treated as floating point.
     * @param  node   original node from the lexer.
     */
    protected void handleNumber(String token, int radix, Token node) {
        Number n;
        int last = token.length() - 1;
        char ch = token.charAt(last);
        if (radix > 0) {
            if (ch == 'l' || ch == 'L') {
                token = token.substring(0, last);
                try {
                    n = Long.valueOf(token, radix);
                } catch (NumberFormatException nfe) {
                    setError(Errors.Code.NUMBER_FORMAT, node);
                    return;
                }

            } else {
                try {
                    // Use integer by default...
                    n = Integer.valueOf(token, radix);
                } catch (NumberFormatException nfe) {
                    try {
                        // but try long if that didn't work.
                        n = Long.valueOf(token, radix);
                    } catch (NumberFormatException nfe2) {
                        setError(Errors.Code.NUMBER_FORMAT, node);
                        return;
                    }
                }
            }

        } else {
            if (ch == 'f' || ch == 'F') {
                token = token.substring(0, last);
                try {
                    n = Float.valueOf(token);
                } catch (NumberFormatException nfe) {
                    setError(Errors.Code.NUMBER_FORMAT, node);
                    return;
                }
            } else if (ch == 'd' || ch == 'D') {
                token = token.substring(0, last);
                try {
                    n = Double.valueOf(token);
                } catch (NumberFormatException nfe) {
                    setError(Errors.Code.NUMBER_FORMAT, node);
                    return;
                }

            } else {
                try {
                    n = Float.valueOf(token);
                } catch (NumberFormatException nfe) {
                    try {
                        n = Double.valueOf(token);
                    } catch (NumberFormatException nfe2) {
                        setError(Errors.Code.NUMBER_FORMAT, node);
                        return;
                    }
                }
            }
        }
        handleLiteral(new LiteralNode(node, n));
    } // handleNumber

    /**
     * Handles the given binary or unary operator node.
     *
     * @param  op  operator node.
     */
    protected void handleOperator(OperatorNode op) {
        if (searchState == State.APPEND) {
            setError(Errors.Code.DOT_REQUIRES_ID, op.getToken());
            return;
        }

        // If the operator stack is empty, push the new operator. If it
        // has an operator on top, compare the precedence of the two and
        // push the new one if it has lower precedence (or equal
        // precedence: this will force left associativity). Otherwise
        // reduce the two stacks.
        if (!operatorStack.empty()) {
            OperatorNode top = (OperatorNode) operatorStack.peek();
            if (!top.isSentinel() && op.precedence() >= top.precedence()) {
                reduce();
            }
        }
        operatorStack.push(op);
        searchState = State.ARGUMENT;
    } // handleOperator

    /**
     * Handles the given postfix operator node.
     *
     * @param  op  postfix operator node.
     */
    protected void handlePostfixOp(OperatorNode op) {
        // check that there is a variable leaf node on the argument
        // stack, then pop it off and push a postfix operation node
        // (built from the variable and the postfix operator) back onto
        // the argument stack.
    } // handlePostfixOp

    /**
     * Handles the given variable node appropriately.
     *
     * @param  var variable node.
     */
    protected void handleVariable(Node var) {
        // Push the variable onto the tree stack.
        argumentStack.push(var);
        searchState = State.OPERATOR;
    } // handleVariable

    /**
     * Reduce the operator stack by one. If the operator stack top is
     * a left paren, no change is made.
     */
    protected void reduce() {
        // If there is a binary operator on top of the operator stack,
        // there should be two trees on top of the argument stack, both
        // representing expressions. Pop the operator and two trees off,
        // combining them into a single tree node, which is then pushed
        // back on the argument stack. Note that the trees on the
        // argument stack represent the right and left arguments,
        // respectively.
        OperatorNode top = (OperatorNode) operatorStack.pop();
        if (top.isSentinel()) {
            // Cleverly do nothing and let the caller handle it.
        } else if (top instanceof BinaryOperatorNode) {
            try {
                Node arg2 = argumentStack.pop();
                Node arg1 = argumentStack.pop();
                top.addChild(arg1);
                top.addChild(arg2);
                argumentStack.push(top);
            } catch (EmptyStackException ese) {
                setError(Errors.Code.MISSING_ARGS, top.getToken());
            }
        } else if (top instanceof UnaryOperatorNode) {
            try {
                Node arg = argumentStack.pop();
                top.addChild(arg);
                argumentStack.push(top);
            } catch (EmptyStackException ese) {
                setError(Errors.Code.MISSING_ARGS, top.getToken());
            }
        } else {
            setError(Errors.Code.UNEXPECTED_TOKEN, top.getToken());
        }
    } // reduce

    /**
     * Set the error message and set the done flag to true.
     *
     * @param  msg  error message.
     */
    protected void setError(String msg) {
        evalException = new EvaluationException(msg);
        doneProcessing = true;
    } // setError

    /**
     * Set the error value and set the done flag to true.
     *
     * @param  error  error value.
     */
    protected void setError(Errors.Code error) {
        setError(Errors.getMessage(error));
    } // setError

    /**
     * Set the error value and set the done flag to true.
     * Appends the 'add' string to the error message, separated
     * by a single space character.
     *
     * @param  error  error value.
     * @param  add    additional error suffix.
     */
    protected void setError(Errors.Code error, String add) {
        setError(Errors.getMessage(error) + ": " + add);
    } // setError

    /**
     * Set the error value and set the done flag to true.
     * Appends the token information to the message.
     *
     * @param  error  error value.
     * @param  token  token providing additional details.
     */
    protected void setError(Errors.Code error, Token token) {
        setError(Errors.getMessage(error) + ": " + token.getText()
                 + " @ " + token.getPos());
    } // setError

    /**
     * Set the error value and set the done flag to true.
     * Appends the token information to the message.
     *
     * @param  msg    error message.
     * @param  token  token providing additional details.
     */
    protected void setError(String msg, Token token) {
        setError(msg + ": " + token.getText() + " @ " + token.getPos());
    } // setError

    /**
     * Set the error value and set the done flag to true.
     * Appends the token information to the message.
     *
     * @param  error  error value.
     * @param  token  token providing additional details.
     * @param  add    additional error message.
     */
    protected void setError(Errors.Code error, Token token, String add) {
        setError(Errors.getMessage(error) + ": " + token.getText()
                 + " @ " + token.getPos() + ": " + add);
    } // setError

    /**
     * Sets the token that was just applied, which means it is the
     * previous token for the next application.
     *
     * @param  token  the previous token.
     */
    protected void setPrevToken(Token token) {
        previousToken = token;
    } // setPrevToken

    //
    // Methods inherited from AnalysisAdapter.
    //

    /**
     * @param  node  end-of-file node.
     */
    public void caseEOF(EOF node) {
        doneProcessing = true;
        if (searchState == State.APPEND) {
            setError(Errors.Code.DOT_REQUIRES_ID, node);
            return;
        }

        // If there is only one tree on the argument stack and the
        // operator stack is empty, return the single tree as the
        // result. If there are more trees and/or operators, reduce the
        // stacks as far as possible.

        int count = 0;
        while (!operatorStack.empty()) {
            OperatorNode top = (OperatorNode) operatorStack.peek();
            if (top instanceof LeftParen) {
                setError(Errors.Code.UNMATCHED_LPAREN, top.getToken());
                return;
            } else if (top instanceof LeftBracket) {
                setError(Errors.Code.UNMATCHED_LBRACKET, top.getToken());
                return;
            } else if (top.isSentinel()) {
                setError(Errors.Code.INVALID_EXPR, top.getToken());
                return;
            }
            reduce();
            count++;
            if (count > 500) {
                setError(Errors.Code.LARGE_OPER_STACK);
                return;
            }
        }

        if (!argumentStack.empty()) {
            Node topArg = argumentStack.pop();
            if (argumentStack.empty() && operatorStack.empty()) {
                rootNode.addChild(topArg);
            } else {
                setError(Errors.Code.ARG_STACK_NON_EMPTY, topArg.getToken());
            }
        }
    }

    //
    // Identifiers
    //

    /**
     *
     * @param  token  identifier token.
     */
    public void caseTIdentifier(TIdentifier token) {
        String name = token.getText();
        if (searchState == State.APPEND) {
            // Both argument and operator stacks are non-empty at this point.
            Node node = argumentStack.peek();
            if (node instanceof JoinableNode) {
                Node opr = operatorStack.peek();
                if (opr instanceof JoinOperatorNode) {
                    // Add the second operand for our join operator.
                    handleVariable(new IdentifierNode(token, name));
                    // Reduce the operator stack once to get the join
                    // operator onto the argument stack where it belongs.
                    reduce();
                    searchState = State.OPERATOR;
                } else {
                    setError(Errors.Code.UNKNOWN_STATE, token, "append without join");
                }
            } else {
                setError(Errors.Code.UNKNOWN_STATE, token, "cannot append to "
                    + node.getToken().getText());
            }
        } else {
            handleVariable(new IdentifierNode(token, name));
        }
    }

    /**
     *
     * @param  node  'this' node.
     */
    public void caseTThis(TThis node) {
        if (searchState == State.APPEND) {
            // this is where RFE 881 implementation would begin
            setError(Errors.Code.DOT_REQUIRES_ID, node);
            return;
        }
        handleVariable(new IdentifierNode(node, "this"));
    }

    //
    // Literal values.
    //

    /**
     *
     * @param  node  decimal integer literal node.
     */
    public void caseTDecimalIntegerLiteral(TDecimalIntegerLiteral node) {
        handleNumber(node.getText(), 10, node);
    }

    /**
     *
     * @param  node  hexadecimal integer literal node.
     */
    public void caseTHexIntegerLiteral(THexIntegerLiteral node) {
        String t = node.getText();
        // Chop off the 0x prefix.
        t = t.substring(2);
        handleNumber(t, 16, node);
    }

    /**
     *
     * @param  node  octal integer literal node.
     */
    public void caseTOctalIntegerLiteral(TOctalIntegerLiteral node) {
        handleNumber(node.getText(), 8, node);
    }

    /**
     *
     * @param  node  floating point literal node.
     */
    public void caseTFloatingPointLiteral(TFloatingPointLiteral node) {
        handleNumber(node.getText(), 0, node);
    }

    /**
     *
     * @param  node  character literal node.
     */
    public void caseTCharacterLiteral(TCharacterLiteral node) {
        String t = node.getText();
        if (t.length() > 0) {
            t = Strings.trimQuotes(t);
            handleLiteral(new LiteralNode(node, new Character(
                                              translateChar(t))));
        } else {
            setError(Errors.Code.INVALID_EXPR, node);
        }
    }

    /**
     *
     * @param  node  string literal node.
     */
    public void caseTStringLiteral(TStringLiteral node) {
        String t = node.getText();
        if (t.length() > 0) {
            t = Strings.trimQuotes(t);
            t = translateString(t);
            handleLiteral(new LiteralNode(node, t));
        } else {
            setError(Errors.Code.INVALID_EXPR, node);
        }
    }

    /**
     *
     * @param  node  true keyword node.
     */
    public void caseTTrue(TTrue node) {
        handleLiteral(new LiteralNode(node, Boolean.TRUE));
    }

    /**
     *
     * @param  node  false keyword node.
     */
    public void caseTFalse(TFalse node) {
        handleLiteral(new LiteralNode(node, Boolean.FALSE));
    }

    /**
     *
     * @param  node  null keyword node.
     */
    public void caseTNull(TNull node) {
        handleLiteral(new LiteralNode(node, null));
    }

    //
    // Operators
    //

    /**
     *
     * @param  node  left parenthesis.
     */
    public void caseTLParenthese(TLParenthese node) {
        // Append state is definitely wrong, but argument and operator
        // states are perfectly exceptable.
        if (searchState == State.APPEND) {
            setError(Errors.Code.DOT_REQUIRES_ID, node);
            return;
        }
        LeftParen lp = new LeftParen(node);
        // If the last token was an identifier, then this is a method call.
        if (previousToken instanceof TIdentifier) {
            // The argument stack is assumed to be empty.
            Node n = argumentStack.pop();
            MethodNode method = null;
            if (n instanceof IdentifierNode) {
                IdentifierNode inode = (IdentifierNode) n;
                method = new MethodNode(inode.getToken(), inode.getIdentifier());
            } else {
                JoinOperatorNode onode = (JoinOperatorNode) n;
                Node object = onode.getChild(0);
                String name = onode.getChild(1).getToken().getText();
                method = new MethodNode(onode.getToken(), object, name);
            }
            // Put it on the argument stack as a sentinel, to mark
            // the beginning of the method arguments.
            argumentStack.push(method);
            // Put it on the operator stack so when we find the
            // right parenthesis, we can determine that we were
            // making a method call.
            operatorStack.push(method);
            searchState = State.ARGUMENT;
            methodCount++;
        }
        // Else, it is the start of a type-cast or a subgroup.
        operatorStack.push(lp);
    }

    /**
     *
     * @param  node  right parenthesis.
     */
    public void caseTRParenthese(TRParenthese node) {
        if (operatorStack.empty()) {
            setError(Errors.Code.UNMATCHED_RPAREN, node);
            return;
        }
        // If there is a left parenthesis on the operator stack, we can
        // "cancel" the pair. If the operator stack contains some other
        // operator on top, reduce the stacks. This also covers the case
        // where the parentheses were used for grouping only.
        OperatorNode top = (OperatorNode) operatorStack.peek();
        while (!(top instanceof LeftParen)) {
            reduce();
            if (operatorStack.empty() || top.isSentinel()) {
                setError(Errors.Code.UNMATCHED_RPAREN, node);
                return;
            }
            top = (OperatorNode) operatorStack.peek();
        }
        operatorStack.pop();

        // Now check for the method invocation case.
        if (!operatorStack.empty()
            && operatorStack.peek() instanceof MethodNode) {
            // It was a method invocation.
            MethodNode method = (MethodNode) operatorStack.pop();
            // Pop off the arguments and add them in reverse order.
            Node n = argumentStack.pop();
            Stack<Node> args = new Stack<Node>();
            while (n != method) {
                args.push(n);
                n = argumentStack.pop();
            }
            while (!args.empty()) {
                Node arg = args.pop();
                method.addChild(arg);
            }
            // Put the method invocation back on the argument stack
            // because it is treated as a value, not an operator.
            argumentStack.push(method);
            methodCount--;

        } else {
            // Maybe it is a type-cast operation; otherwise it was a
            // grouping operator and that has been taken care of.
            try {
                Node n = argumentStack.peek();
                if (n instanceof TypeNode) {
                    argumentStack.pop();
                    TypeNode tn = (TypeNode) n;
                    TypeCastOperatorNode tcon = new TypeCastOperatorNode(
                        tn.getToken(), tn.getTypeName());
                    handleOperator(tcon);
                } else if (n instanceof IdentifierNode) {
                    argumentStack.pop();
                    IdentifierNode in = (IdentifierNode) n;
                    TypeCastOperatorNode tcon = new TypeCastOperatorNode(
                        in.getToken(), in.getIdentifier());
                    handleOperator(tcon);
                } else if (n instanceof JoinOperatorNode) {
                    argumentStack.pop();
                    JoinOperatorNode jon = (JoinOperatorNode) n;
                    TypeCastOperatorNode tcon = new TypeCastOperatorNode(
                        jon.getToken(), jon.mergeChildren());
                    handleOperator(tcon);
                }
            } catch (EvaluationException ee) {
                setError(ee.getMessage(), top.getToken());
            } catch (EmptyStackException ese) {
                setError(Errors.Code.MISSING_ARGS, top.getToken());
            }
        }
    }

    /**
     *
     * @param  node  left bracket.
     */
    public void caseTLBracket(TLBracket node) {
        // Make sure there is something reasonable on the stack, since
        // a left bracket without a preceding type or identifier is
        // incorrect syntax.
        if (argumentStack.isEmpty()) {
            setError(Errors.Code.UNEXPECTED_TOKEN, node);
        } else {
            Node n = argumentStack.peek();
            if (!(n instanceof JoinOperatorNode)
                && !(n instanceof IdentifierNode)
                && !(n instanceof TypeNode)) {
                setError(Errors.Code.UNEXPECTED_TOKEN, node);
            }
        }
        // Push the left bracket on the operator stack. We can't tell yet
        // if this is going to be a typecast or an array access.
        Node lb = new LeftBracket(node);
        operatorStack.push(lb);
        // Put it on the argument stack as a sentinel for the array index,
        // if that is indeed what this turns out to be.
        argumentStack.push(lb);
        // We may have been looking for an argument or an operator,
        // and we can't be sure what we will find next.
    }

    /**
     *
     * @param  node  right bracket.
     */
    public void caseTRBracket(TRBracket node) {
        // If there is a left bracket on the operator stack, we can
        // "cancel" the pair. If the operator stack contains some other
        // operator on top, reduce the stacks.
        if (operatorStack.empty()) {
            setError(Errors.Code.UNMATCHED_RBRACKET, node);
            return;
        }
        OperatorNode top = (OperatorNode) operatorStack.peek();
        while (!(top instanceof LeftBracket)) {
            reduce();
            if (operatorStack.empty() || top.isSentinel()) {
                setError(Errors.Code.UNMATCHED_RBRACKET, node);
                return;
            }
            top = (OperatorNode) operatorStack.peek();
        }
        operatorStack.pop();

        // Was there anything between the brackets?
        // (we know there will be a left bracket and something else on the
        // argument stack, given that we are here).
        Node index = argumentStack.pop();
        Node name = argumentStack.pop();
        if (index instanceof LeftBracket) {
            // It was probably part of a typecast, but we can't be sure.
            // In any case, make a TypeNode out of this and put it on
            // the argument stack.
            Token token = name.getToken();
            argumentStack.push(new TypeNode(token, token.getText() + "[]"));
        } else {
            // It was an array reference.
            // Make sure that only one array index was provided.
            if (name instanceof LeftBracket) {
                ArrayNode arrayref = new ArrayNode(name.getToken());
                // Retrieve the thing that was there before the left bracket
                // (an identifier or type).
                name = argumentStack.pop();
                arrayref.addChild(name);
                arrayref.addChild(index);
                // Put the array reference on the argument stack; it's a value.
                argumentStack.push(arrayref);
            } else {
                setError(Errors.Code.ARRAY_MULTI_INDEX, name.getToken());
            }
        }
    }

    /**
     *
     * @param  node  comma.
     */
    public void caseTComma(TComma node) {
        if (methodCount == 0) {
            setError(Errors.Code.UNEXPECTED_TOKEN, node);
        } else {
            // Reduce the operator stack to the left parenthesis.
            if (operatorStack.empty()) {
                setError(Errors.Code.UNEXPECTED_TOKEN, node);
                return;
            }
            OperatorNode top = (OperatorNode) operatorStack.peek();
            while (!top.isSentinel()) {
                reduce();
                top = (OperatorNode) operatorStack.peek();
            }
        }
        // Was there anything before this comma?
        // Doesn't really matter, simply invokes the wrong method
        // or fails to find a matching method. Stupid user error.
    }

    /**
     *
     * @param  node  period.
     */
    public void caseTDot(TDot node) {
        // Dot is special and needs processing before evaluation.
        if (!argumentStack.empty()
            && argumentStack.peek() instanceof JoinableNode) {
            // We have a joinable, which makes this a join in progress.
            handleOperator(new JoinOperatorNode(node));
            searchState = State.APPEND;
        } else {
            setError(Errors.Code.UNEXPECTED_TOKEN, node);
        }
    }

    /**
     *
     * @param  node  plus sign.
     */
    public void caseTPlus(TPlus node) {
        // The plus is unary if we are expecting an argument or if
        // the argument stack is empty (ie. search state is 'start').
        if (searchState == State.ARGUMENT) {
            handleOperator(new PlusUnaryOperatorNode(node));
        } else {
            handleOperator(new PlusBinaryOperatorNode(node));
        }
    }

    /**
     *
     * @param  node  minus sign.
     */
    public void caseTMinus(TMinus node) {
        // The minus is unary if we are expecting an argument or if
        // the argument stack is empty (ie. search state is 'start').
        if (searchState == State.ARGUMENT) {
            handleOperator(new MinusUnaryOperatorNode(node));
        } else {
            handleOperator(new MinusBinaryOperatorNode(node));
        }
    }

    /**
     *
     * @param  node  asterisk.
     */
    public void caseTStar(TStar node) {
        handleOperator(new MultBinaryOperatorNode(node));
    }

    /**
     *
     * @param  node  division sign.
     */
    public void caseTDiv(TDiv node) {
        handleOperator(new DivBinaryOperatorNode(node));
    }

    /**
     *
     * @param  node  modulus sign.
     */
    public void caseTMod(TMod node) {
        handleOperator(new ModBinaryOperatorNode(node));
    }

    /**
     *
     * @param  node  shift left.
     */
    public void caseTShiftLeft(TShiftLeft node) {
        handleOperator(new LeftShiftOperatorNode(node));
    }

    /**
     *
     * @param  node  signed shift right.
     */
    public void caseTSignedShiftRight(TSignedShiftRight node) {
        handleOperator(new RightShiftOperatorNode(node));
    }

    /**
     *
     * @param  node  unsigned shift right.
     */
    public void caseTUnsignedShiftRight(TUnsignedShiftRight node) {
        handleOperator(new UnsignedRightShiftOperatorNode(node));
    }

    /**
     *
     * @param  node  bitwise and.
     */
    public void caseTBitAnd(TBitAnd node) {
        handleOperator(new BitwiseAndOperatorNode(node));
    }

    /**
     *
     * @param  node  bitwise or.
     */
    public void caseTBitOr(TBitOr node) {
        handleOperator(new BitwiseOrOperatorNode(node));
    }

    /**
     *
     * @param  node  bitwise exclusive or.
     */
    public void caseTBitXor(TBitXor node) {
        handleOperator(new BitwiseXorOperatorNode(node));
    }

    /**
     *
     * @param  node  bitwise complement.
     */
    public void caseTBitComplement(TBitComplement node) {
        handleOperator(new BitwiseNegOperatorNode(node));
    }

    /**
     *
     * @param  node  logical complement.
     */
    public void caseTComplement(TComplement node) {
        handleOperator(new BooleanNegOperatorNode(node));
    }

    /**
     *
     * @param  node  logical and.
     */
    public void caseTAnd(TAnd node) {
        handleOperator(new BooleanAndOperatorNode(node));
    }

    /**
     *
     * @param  node  logical or.
     */
    public void caseTOr(TOr node) {
        handleOperator(new BooleanOrOperatorNode(node));
    }

    /**
     *
     * @param  node  equality.
     */
    public void caseTEq(TEq node) {
        handleOperator(new EqualsOperatorNode(node));
    }

    /**
     *
     * @param  node  negation.
     */
    public void caseTNeq(TNeq node) {
        handleOperator(new NotEqualsOperatorNode(node));
    }

    /**
     *
     * @param  node  greater than.
     */
    public void caseTGt(TGt node) {
        handleOperator(new GtOperatorNode(node));
    }

    /**
     *
     * @param  node  less than.
     */
    public void caseTLt(TLt node) {
        handleOperator(new LtOperatorNode(node));
    }

    /**
     *
     * @param  node  less than or equal.
     */
    public void caseTLteq(TLteq node) {
        handleOperator(new LtEqOperatorNode(node));
    }

    /**
     *
     * @param  node  greater than or equal.
     */
    public void caseTGteq(TGteq node) {
        handleOperator(new GtEqOperatorNode(node));
    }

    //
    // Primitive data types
    //

    /**
     *
     * @param  node  boolean keyword.
     */
    public void caseTBoolean(TBoolean node) {
        handleType(new TypeNode(node, "boolean"));
    }

    /**
     *
     * @param  node  byte keyword.
     */
    public void caseTByte(TByte node) {
        handleType(new TypeNode(node, "byte"));
    }

    /**
     *
     * @param  node  char keyword
     */
    public void caseTChar(TChar node) {
        handleType(new TypeNode(node, "char"));
    }

    /**
     *
     * @param  node  class keyword
     */
    public void caseTClass(TClass node) {
        setError(Errors.Code.UNSUPPORTED_FEATURE, node);
    }

    /**
     *
     * @param  node  double keyword
     */
    public void caseTDouble(TDouble node) {
        handleType(new TypeNode(node, "double"));
    }

    /**
     *
     * @param  node  float keyword
     */
    public void caseTFloat(TFloat node) {
        handleType(new TypeNode(node, "float"));
    }

    /**
     *
     * @param  node  int keyword
     */
    public void caseTInt(TInt node) {
        handleType(new TypeNode(node, "int"));
    }

    /**
     *
     * @param  node  long keyword
     */
    public void caseTLong(TLong node) {
        handleType(new TypeNode(node, "long"));
    }

    /**
     *
     * @param  node  short keyword
     */
    public void caseTShort(TShort node) {
        handleType(new TypeNode(node, "short"));
    }

    //
    // Unsupported features (that may be supported in the future)
    //

    /**
     *
     * @param  node  super keyword.
     */
    public void caseTSuper(TSuper node) {
        setError(Errors.Code.UNSUPPORTED_FEATURE, node);
    }

    /**
     *
     * @param  node  question mark.
     */
    public void caseTQuestion(TQuestion node) {
        setError(Errors.Code.UNSUPPORTED_FEATURE, node);
    }

    /**
     *
     * @param  node  colon.
     */
    public void caseTColon(TColon node) {
        setError(Errors.Code.UNSUPPORTED_FEATURE, node);
    }

    //
    // Unsupported tokens.
    //

    /**
     *
     * @param  node  comment
     */
    public void caseTTraditionalComment(TTraditionalComment node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  comment
     */
    public void caseTDocumentationComment(TDocumentationComment node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  comment
     */
    public void caseTEndOfLineComment(TEndOfLineComment node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  void keyword
     */
    public void caseTVoid(TVoid node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  left brace.
     */
    public void caseTLBrace(TLBrace node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  right brace.
     */
    public void caseTRBrace(TRBrace node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  abstract keyword.
     */
    public void caseTAbstract(TAbstract node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  assert keyword.
     */
    public void caseTAssert(TAssert node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  break keyword.
     */
    public void caseTBreak(TBreak node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  case keyword.
     */
    public void caseTCase(TCase node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  catch keyword.
     */
    public void caseTCatch(TCatch node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  const keyword.
     */
    public void caseTConst(TConst node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  continue keyword.
     */
    public void caseTContinue(TContinue node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  default keyword.
     */
    public void caseTDefault(TDefault node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  do keyword.
     */
    public void caseTDo(TDo node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  else keyword.
     */
    public void caseTElse(TElse node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  extends keyword.
     */
    public void caseTExtends(TExtends node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  final keyword.
     */
    public void caseTFinal(TFinal node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  finally keyword.
     */
    public void caseTFinally(TFinally node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  for keyword.
     */
    public void caseTFor(TFor node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  goto keyword.
     */
    public void caseTGoto(TGoto node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  if keyword.
     */
    public void caseTIf(TIf node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  implements keyword.
     */
    public void caseTImplements(TImplements node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  import keyword.
     */
    public void caseTImport(TImport node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  instanceof keyword.
     */
    public void caseTInstanceof(TInstanceof node) {
        handleOperator(new InstanceofOperatorNode(node));
    }

    /**
     *
     * @param  node  interface keyword.
     */
    public void caseTInterface(TInterface node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  native keyword.
     */
    public void caseTNative(TNative node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  new keyword.
     */
    public void caseTNew(TNew node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  package keyword.
     */
    public void caseTPackage(TPackage node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  private keyword.
     */
    public void caseTPrivate(TPrivate node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  protected keyword.
     */
    public void caseTProtected(TProtected node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  public keyword.
     */
    public void caseTPublic(TPublic node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  return keyword.
     */
    public void caseTReturn(TReturn node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  static keyword.
     */
    public void caseTStatic(TStatic node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  strictfp keyword.
     */
    public void caseTStrictfp(TStrictfp node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  switch keyword.
     */
    public void caseTSwitch(TSwitch node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  synchronized keyword.
     */
    public void caseTSynchronized(TSynchronized node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  throw keyword.
     */
    public void caseTThrow(TThrow node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  throws keyword.
     */
    public void caseTThrows(TThrows node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  transient keyword.
     */
    public void caseTTransient(TTransient node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  try keyword.
     */
    public void caseTTry(TTry node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  volatile keyword.
     */
    public void caseTVolatile(TVolatile node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  while keyword.
     */
    public void caseTWhile(TWhile node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  semicolon keyword.
     */
    public void caseTSemicolon(TSemicolon node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  assign keyword.
     */
    public void caseTAssign(TAssign node) {
        handleOperator(new AssignOperatorNode(node));
    }

    /**
     *
     * @param  node  plus plus sign.
     */
    public void caseTPlusPlus(TPlusPlus node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  minus minus sign.
     */
    public void caseTMinusMinus(TMinusMinus node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  plus assign.
     */
    public void caseTPlusAssign(TPlusAssign node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  minus assign.
     */
    public void caseTMinusAssign(TMinusAssign node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  multiply assign.
     */
    public void caseTStarAssign(TStarAssign node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  divide assign.
     */
    public void caseTDivAssign(TDivAssign node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  bitwise and assign.
     */
    public void caseTBitAndAssign(TBitAndAssign node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  bitwise or assign.
     */
    public void caseTBitOrAssign(TBitOrAssign node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  bitwise exclusive or assign.
     */
    public void caseTBitXorAssign(TBitXorAssign node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  modulus assign.
     */
    public void caseTModAssign(TModAssign node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  shift left assign.
     */
    public void caseTShiftLeftAssign(TShiftLeftAssign node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  signed shift right assign.
     */
    public void caseTSignedShiftRightAssign(TSignedShiftRightAssign node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }

    /**
     *
     * @param  node  unsigned shift right assign.
     */
    public void caseTUnsignedShiftRightAssign(TUnsignedShiftRightAssign node) {
        setError(Errors.Code.UNSUPPORTED_TOKEN, node);
    }
} // TreeBuilder
