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
 * $Id: ParentNode.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.expr;

import com.bluemarsh.jswat.parser.java.node.Token;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class ParentNode is a basic node that can have children.
 *
 * @author  Nathan Fiedler
 */
class ParentNode extends AbstractNode {
    /** List of children nodes. */
    private List children;

    /**
     * Constructs a ParentNode with no children.
     *
     * @param  node  lexical token.
     */
    public ParentNode(Token node) {
        super(node);
        children = new ArrayList();
    } // ParentNode

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
    } // addChild

    /**
     * Returns the number of immediate children under this node.
     *
     * @return  count of children.
     */
    public int childCount() {
        return children.size();
    } // childCount

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
            Node child = (Node) children.get(0);
            return child.evaluate(context);
        } else {
            // Hmm, multiple children is unusual.
            // Return the concatenation of their values.
            StringBuffer results = new StringBuffer();
            Iterator iter = children.iterator();
            while (iter.hasNext()) {
                Node child = (Node) iter.next();
                Object val = child.evaluate(context);
                results.append(val);
            }
            return results.toString();
        }
    } // eval

    /**
     * Retrieve the child node at the given offset.
     *
     * @param  i  child offset.
     * @return  child node.
     */
    public Node getChild(int i) {
        return (Node) children.get(i);
    } // getChild
} // ParentNode
