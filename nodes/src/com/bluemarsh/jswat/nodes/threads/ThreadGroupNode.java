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
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nodes.threads;

import com.bluemarsh.jswat.nodes.BaseNode;
import com.sun.jdi.ThreadGroupReference;
import org.openide.nodes.Children;

/**
 * Represents a thread group in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public abstract class ThreadGroupNode extends BaseNode {

    /**
     * Creates a new instance of ThreadGroupNode.
     *
     * @param  children  children of this group.
     */
    public ThreadGroupNode(Children children) {
        super(children);
    }

    /**
     * Returns the ThreadGroupReference this node represents.
     *
     * @return  thread group.
     */
    public abstract ThreadGroupReference getThreadGroup();
}
