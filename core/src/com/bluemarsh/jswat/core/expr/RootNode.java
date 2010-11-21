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
 * are Copyright (C) 2002-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
    RootNode() {
        super(null);
    }

    /**
     * Sets the parent node of this node.
     *
     * @param  parent  parent node.
     */
    public void setParent(Node parent) {
        throw new UnsupportedOperationException("root has no parent");
    }
}
