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
 * are Copyright (C) 2004-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BreakpointGroupNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.breakpoints;

import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.nodes.BaseNode;
import org.openide.nodes.Children;

/**
 * Represents a group of breakpoints.
 *
 * @author  Nathan Fiedler
 */
public abstract class BreakpointGroupNode extends BaseNode {

    /**
     * Constructs a new instance of GroupNode.
     *
     * @param  children  children heirarchy for this node.
     */
    public BreakpointGroupNode(Children children) {
        super(children);
    }

    /**
     * Returns the BreakpointGroup this node represents.
     *
     * @return  breakpoint group.
     */
    public abstract BreakpointGroup getBreakpointGroup();
}
