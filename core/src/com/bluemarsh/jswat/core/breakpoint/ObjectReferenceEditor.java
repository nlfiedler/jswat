/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ObjectReferenceEditor.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.ObjectReference;
import java.beans.PropertyEditorSupport;

/**
 * Property editor for the object reference property.
 *
 * @author Nathan Fiedler
 */
public class ObjectReferenceEditor extends PropertyEditorSupport {

    /**
     * Creates a new instance of ObjectReferenceEditor.
     */
    public ObjectReferenceEditor() {
    }

    public String getAsText() {
        Object value = getValue();
        if (value instanceof ObjectReference) {
            return "#" + ((ObjectReference) value).uniqueID();
        }
        return "";
    }

    public void setAsText(String text) throws IllegalArgumentException {
        // Do nothing as the user cannot change the object reference.
    }
}
