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
 * $Id: SessionNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.sessions;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.nodes.BaseNode;

/**
 * Represents a debugging session.
 *
 * @author  Nathan Fiedler
 */
public abstract class SessionNode extends BaseNode {
    /** Name of the debuggee address property. */
    public static final String PROP_HOST = "host";
    /** Name of the session state property. */
    public static final String PROP_STATE = "state";
    /** Name of the session stratum property. */
    public static final String PROP_LANG = "lang";

    /**
     * Returns the Session this node represents.
     *
     * @return  session.
     */
    public abstract Session getSession();
}
