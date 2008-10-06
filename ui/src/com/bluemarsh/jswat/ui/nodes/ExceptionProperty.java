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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ExceptionProperty.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 * A node property that displays an exception.
 *
 * @author  Nathan Fiedler
 */
public class ExceptionProperty extends PropertySupport.ReadOnly {
    /** Number of exception properties created so far. */
    private static int errorCounter = 1;
    /** The cause of the problem. */
    private Throwable throwable;

    /**
     * Creates a new instance of ExceptionProperty.
     *
     * @param  t  throwable for which to construct a property.
     */
    public ExceptionProperty(Throwable t) {
        super("error-" + errorCounter, String.class,
                NbBundle.getMessage(ExceptionProperty.class, "ERR_BadProperty"),
                t.getMessage());
        errorCounter++;
        throwable = t;
    }

    public Object getValue() throws IllegalAccessException,
            InvocationTargetException {
        return throwable.getMessage();
    }
}
