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
 * $Id: DefaultFixedWatch.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.watch;

import com.sun.jdi.ObjectReference;

/**
 * A default implementation of the FixedWatch interface.
 *
 * @author Nathan Fiedler
 */
public class DefaultFixedWatch extends AbstractWatch
        implements FixedWatch {
    /** Name of 'objectReference' property. */
    public static final String PROP_OBJECTREFERENCE = "objectReference";
    /** The object being watched. */
    private ObjectReference objectReference;

    /**
     * Creates a new instance of DefaultFixedWatch.
     */
    public DefaultFixedWatch() {
    }

    public ObjectReference getObjectReference() {
        return objectReference;
    }

    public void setObjectReference(ObjectReference obj) {
        ObjectReference old = objectReference;
        objectReference = obj;
        propSupport.firePropertyChange(PROP_OBJECTREFERENCE, old, objectReference);
    }
}
