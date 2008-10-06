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
 * $Id: DefaultFixedWatch.java 15 2007-06-03 00:01:17Z nfiedler $
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
