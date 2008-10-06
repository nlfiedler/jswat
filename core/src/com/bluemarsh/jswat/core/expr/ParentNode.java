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
 * $Id: ParentNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

import com.bluemarsh.jswat.parser.node.Token;
import java.util.ArrayList;
import java.util.List;

/**
 * Class ParentNode is a basic node that can have children.
 *
 * @author  Nathan Fiedler
 */
class ParentNode extends AbstractNode {
    /** List of children nodes. */
    private List<Node> children;

    /**
     * Constructs a ParentNode with no children.
     *
     * @param  node  lexical token.
     */
    public ParentNode(Token node) {
        super(node);
        children = new ArrayList<Node>(2);
    }

    /**
     * Adds the node to this parent. Children must be stored in the
     * order in which they were added. Otherwise various operators
     * will perform incorrectly (such as - % / and others).
     *
     * @param  node  child node.
     */
    public void addChild(Node node) {
        ParentNode parent = node.getParent();
        if (parent != null) {
            parent.children.remove(node);
        }
        children.add(node);
        node.setParent(this);
    }

    /**
     * Returns the number of immediate children under this node.
     *
     * @return  count of children.
     */
    public int childCount() {
        return children.size();
    }

    /**
     * Returns the value of this node.
     *
     * @param  context  evaluation context.
     * @return  value.
     * @throws  EvaluationException
     *          if an error occurred during evaluation.
     */
    protected Object eval(EvaluationContext context)
        throws EvaluationException {

        if (children.size() == 0) {
            // No children, no value.
            return null;
        } else if (children.size() == 1) {
            // The expected case, one child.
            Node child = children.get(0);
            return child.evaluate(context);
        } else {
            // Hmm, multiple children is unusual.
            // Return the concatenation of their values.
            StringBuilder results = new StringBuilder();
            for (Node child : children) {
                Object val = child.evaluate(context);
                results.append(val);
            }
            return results.toString();
        }
    }

    /**
     * Retrieve the child node at the given offset.
     *
     * @param  i  child offset.
     * @return  child node.
     */
    public Node getChild(int i) {
        return children.get(i);
    }
}
