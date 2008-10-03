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
 * $Id: ExceptionProperty.java 30 2008-06-30 01:12:15Z nfiedler $
 */

package com.bluemarsh.jswat.nodes;

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 * A node property that displays an exception.
 *
 * @author  Nathan Fiedler
 */
@SuppressWarnings("unchecked")
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
                NbBundle.getMessage(ExceptionProperty.class,
                "ERR_ExceptionProperty"), t.getMessage());
        errorCounter++;
        throwable = t;
    }

    public Object getValue() throws IllegalAccessException,
            InvocationTargetException {
        return throwable.getMessage();
    }
}
