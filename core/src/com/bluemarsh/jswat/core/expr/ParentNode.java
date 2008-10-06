/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core Module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2002-2004. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ParentNode.java 15 2007-06-03 00:01:17Z nfiedler $
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
