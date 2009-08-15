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
 * are Copyright (C) 2004-2009. All Rights Reserved.
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
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 * An AbstractNode subclass that offers features needed in many cases.
 *
 * @author  Nathan Fiedler
 */
public abstract class BaseNode extends AbstractNode implements Comparable {
    /** Where objects can be registered for lookup. */
    private InstanceContent lookupContent;

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
        this(children, Lookup.EMPTY);
    }

    /**
     * Creates a new instance of BaseNode.
     *
     * @param  lookup  Lookup to incorporate into Node's Lookup.
     */
    public BaseNode(Lookup lookup) {
        this(Children.LEAF, lookup);
    }

    /**
     * Creates a new instance of BaseNode.
     *
     * @param  children  children heirarchy.
     * @param  lookup    Lookup to incorporate into Node's Lookup.
     */
    public BaseNode(Children children, Lookup lookup) {
        this(children, lookup, new InstanceContent());
    }

    /**
     * Constructor hack to allow us to create our own lookup.
     *
     * @param  children  children heirarchy.
     * @param  lookup    lookup to incorporate into Node's Lookup.
     * @param  content   initial lookup content.
     */
    private BaseNode(Children children, Lookup lookup, InstanceContent content) {
        super(children, createLookup(lookup, content));
        lookupContent = content;
    }

    @Override
    public int compareTo(Object o) {
        // Default implementation so that some nodes can delay the
        // invocation of Children.SortedArray.setComparator() since
        // that can lead to the readAccess/writeAccess upgrade error.
        return 0;
    }

    /**
     * Creates the Lookup for this node.
     *
     * @param  lookup   the Lookup provided by the client.
     * @param  content  to which this node adds objects.
     * @return  the Lookup for this node.
     */
    private static Lookup createLookup(Lookup lookup, InstanceContent content) {
        return new ProxyLookup(new Lookup[] {
            lookup,
            new AbstractLookup(content),
        });
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] superA = super.getActions(context);
        Action[] subA = getNodeActions();
        return (Action[]) Arrays.join(superA, subA);
    }

    /**
     * Returns the lookup content to which objects can be added.
     *
     * @return  lookup content.
     */
    protected InstanceContent getLookupContent() {
        return lookupContent;
    }

    /**
     * Returns the actions for this Node.
     *
     * @return  node actions, may be null.
     */
    protected abstract Action[] getNodeActions();
}
