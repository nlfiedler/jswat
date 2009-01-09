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
 * are Copyright (C) 2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nodes.breakpoints;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.nodes.NodeFactory;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Contains a set of nodes that represent the constituents of a breakpoint
 * group.
 *
 * @author Nathan Fiedler
 */
public class BreakpointGroupChildren extends Children.Array {
    /** The breakpoint group we represent. */
    private BreakpointGroup breakpointGroup;

    /**
     * Creates a new instance of BreakpointGroupChildren.
     *
     * @param  group  breakpoint group to represent.
     */
    public BreakpointGroupChildren(BreakpointGroup group) {
        breakpointGroup = group;
    }

    protected void addNotify() {
        super.addNotify();

        // Iterate over the breakpoint groups.
        Iterator<BreakpointGroup> groups = breakpointGroup.groups(false);
        List<Node> newKids = new LinkedList<Node>();
        NodeFactory factory = NodeFactory.getDefault();
        while (groups.hasNext()) {
            BreakpointGroup group = groups.next();
            newKids.add(factory.createBreakpointGroupNode(group));
        }

        // Iterate over the breakpoints.
        Iterator<Breakpoint> brks = breakpointGroup.breakpoints(false);
        while (brks.hasNext()) {
            Breakpoint bp = brks.next();
            newKids.add(factory.createBreakpointNode(bp));
        }

        int size = newKids.size();
        if (size > 0) {
            Node[] nodes = newKids.toArray(new Node[size]);
            super.add(nodes);
        }
    }
}
