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
 * $Id: RootNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.expr;

/**
 * Class RootNode is a parent node that has no parent.
 *
 * @author  Nathan Fiedler
 */
class RootNode extends ParentNode {

    /**
     * Constructs a RootNode.
     */
    public RootNode() {
        super(null);
    } // RootNode

    /**
     * Sets the parent node of this node.
     *
     * @param  parent  parent node.
     */
    public void setParent(Node parent) {
        throw new UnsupportedOperationException("root has no parent");
    } // setParent
} // RootNode
