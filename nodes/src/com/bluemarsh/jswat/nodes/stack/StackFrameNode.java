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

package com.bluemarsh.jswat.nodes.stack;

import com.bluemarsh.jswat.nodes.BaseNode;

/**
 * Represents a strack frame in the node tree.
 *
 * @author  Nathan Fiedler
 */
public abstract class StackFrameNode extends BaseNode {
    /** Name of the location property. */
    public static final String PROP_LOCATION = "location";
    /** Name of the source property. */
    public static final String PROP_SOURCE = "source";
    /** Name of the code index property. */
    public static final String PROP_CODEINDEX = "codeIndex";

    /**
     * Returns the index of the frame this node represents.
     *
     * @return  frame index for this node.
     */
    public abstract int getFrameIndex();
}
