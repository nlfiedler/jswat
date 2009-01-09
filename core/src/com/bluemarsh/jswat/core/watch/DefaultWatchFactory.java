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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core.watch;

import com.sun.jdi.ObjectReference;

/**
 *
 * @author Nathan Fiedler
 */
public class DefaultWatchFactory implements WatchFactory {

    /**
     * Creates a new instance of DefaultWatchFactory.
     */
    public DefaultWatchFactory() {
    }

    public ExpressionWatch createExpressionWatch(String expr) {
        ExpressionWatch w = new DefaultExpressionWatch();
        w.setExpression(expr);
        return w;
    }

    public FixedWatch createFixedWatch(ObjectReference obj) {
        FixedWatch w = new DefaultFixedWatch();
        w.setObjectReference(obj);
        return w;
    }
}
