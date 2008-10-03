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
 * $Id: ReferentsNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.variables;

import com.sun.jdi.ObjectReference;
import java.awt.Image;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Class ReferentsNode represents the referring objects of an object
 * reference. That is, it displays the objects that refer to the given
 * object reference (i.e. back tracing).
 *
 * @author  Nathan Fiedler
 */
public class ReferentsNode extends AbstractNode implements Comparable<Node> {
    /** Name of this type of node, for sorting purposes. */
    public static final String NAME = "referents";

    /**
     * Creates a new instance of ReferentsNode.
     *
     * @param  reference  the object reference.
     */
    public ReferentsNode(ObjectReference reference) {
        super(new ReferentsChildren(reference));
    }

    public int compareTo(Node o) {
        // Try to keep this node after all others.
        return 1;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ReferentsNode.class,
                "LBL_ReferentsNode_Name");
    }

    @Override
    public Image getIcon(int type) {
        String url = NbBundle.getMessage(VariableNode.class,
                        "IMG_VariableNode_FieldNode");
        return Utilities.loadImage(url);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Children of the ReferentsNode, dynamically builds out the set of
     * referring objects.
     */
    private static class ReferentsChildren extends Children.Array {
        /** The object reference. */
        private ObjectReference reference;

        /**
         * Creates a new instance of ReferentsChildren.
         *
         * @param  referenceable  the object reference.
         */
        public ReferentsChildren(ObjectReference reference) {
            this.reference = reference;
        }

        protected void addNotify() {
            super.addNotify();
            try {
                // Find the referents and convert them to nodes.
                Set<VariableNode> kids = new HashSet<VariableNode>();
                // Limit the number to 100 for the time being.
                List<ObjectReference> referers = reference.referringObjects(100);
                VariableFactory vf = VariableFactory.getDefault();
                int count = 1;
                String prefix = NbBundle.getMessage(ReferentsNode.class,
                        "LBL_ReferentsNode_Referer");
                for (ObjectReference referer : referers) {
                    VariableNode vn = vf.create(prefix + count, referer);
                    kids.add(vn);
                    count++;
                }
                Node[] kidsArray = kids.toArray(new Node[kids.size()]);
                super.add(kidsArray);
            } catch (Exception e) {
                // In most cases, debuggee has resumed, just do nothing.
            }
        }
    }
}
