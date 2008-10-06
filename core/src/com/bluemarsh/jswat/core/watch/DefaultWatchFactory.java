/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: DefaultWatchFactory.java 15 2007-06-03 00:01:17Z nfiedler $
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
