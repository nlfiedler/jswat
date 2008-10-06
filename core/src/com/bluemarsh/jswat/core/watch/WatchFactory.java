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
 * $Id: WatchFactory.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.watch;

import com.sun.jdi.ObjectReference;

/**
 * A WatchFactory creates Watch instances. A concrete implementation
 * can be acquired from the <code>WatchProvider</code> class.
 *
 * @author Nathan Fiedler
 */
public interface WatchFactory {

    /**
     * Creates a watch for the given expression.
     *
     * @param  expr  expression to evaluate.
     * @return  expression watch.
     */
    ExpressionWatch createExpressionWatch(String expr);

    /**
     * Creates a watch for the given object reference.
     *
     * @param  obj  object reference to watch.
     * @return  fixed watch.
     */
    FixedWatch createFixedWatch(ObjectReference obj);
}
