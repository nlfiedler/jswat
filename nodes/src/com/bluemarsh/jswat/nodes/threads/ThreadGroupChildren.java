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

package com.bluemarsh.jswat.nodes.threads;

import com.bluemarsh.jswat.core.CoreSettings;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.nodes.NodeFactory;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import java.util.LinkedList;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Contains a set of nodes that represent the constituents of a thread group.
 *
 * @author Nathan Fiedler
 */
public class ThreadGroupChildren extends Children.Array {
    /** The thread group we represent. */
    private ThreadGroupReference threadGroup;
    /** The debugging context for our threads. */
    private DebuggingContext debugContext;

    /**
     * Creates a new instance of ThreadGroupChildren.
     *
     * @param  group  thread group to represent.
     * @param  dc     debugging context.
     */
    public ThreadGroupChildren(ThreadGroupReference group, DebuggingContext dc) {
        threadGroup = group;
        debugContext = dc;
    }

    protected void addNotify() {
        super.addNotify();
        CoreSettings cs = CoreSettings.getDefault();
        boolean showAll = cs.getShowAllThreads();

        // Iterate over the thread groups.
        List<ThreadGroupReference> groups = threadGroup.threadGroups();
        List<Node> newKids = new LinkedList<Node>();
        NodeFactory factory = NodeFactory.getDefault();
        for (ThreadGroupReference group : groups) {
            newKids.add(factory.createThreadGroupNode(group, debugContext));
        }

        // Iterate over the threads.
        List<ThreadReference> threads = threadGroup.threads();
        for (ThreadReference thread : threads) {
            int status = thread.status();
            // Ignore threads that haven't started or have already finished,
            // unless user wants to see them all anyway.
            if (showAll || (status != ThreadReference.THREAD_STATUS_NOT_STARTED &&
                    status != ThreadReference.THREAD_STATUS_ZOMBIE)) {
                newKids.add(factory.createThreadNode(thread, debugContext));
            }
        }

        int size = newKids.size();
        if (size > 0) {
            Node[] nodes = newKids.toArray(new Node[size]);
            super.add(nodes);
        }
    }
}
