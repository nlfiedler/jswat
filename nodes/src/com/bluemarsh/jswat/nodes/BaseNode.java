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
 * are Copyright (C) 2004-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.nodes;

import com.bluemarsh.jswat.core.util.Arrays;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * An AbstractNode subclass that offers features needed in many cases.
 *
 * <p>This class implements {@code Node.Cookie} since it is common for
 * the NetBeans OutlineView to wrap our nodes inside a FilterNode, and
 * by using getCookie() we can get the original node. Thus subclasses
 * that have actions which need to get the original node can register
 * via {@code AbstractNode.getCookieSet().add(node)}.</p>
 *
 * @author  Nathan Fiedler
 */
public abstract class BaseNode extends AbstractNode implements Comparable {

    /**
     * Creates a new instance of BaseNode.
     */
    public BaseNode() {
        this(Children.LEAF);
    }

    /**
     * Creates a new instance of BaseNode.
     *
     * @param  children  children heirarchy.
     */
    public BaseNode(Children children) {
        super(children);
    }

    @Override
    public int compareTo(Object o) {
        // Default implementation so that some nodes can delay the
        // invocation of Children.SortedArray.setComparator() since
        // that can lead to the readAccess/writeAccess upgrade error.
        return 0;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] superA = super.getActions(context);
        Action[] subA = getNodeActions();
        return (Action[]) Arrays.join(superA, subA);
    }

    /**
     * Returns the actions for this Node.
     *
     * @return  node actions, may be null.
     */
    protected abstract Action[] getNodeActions();
}
