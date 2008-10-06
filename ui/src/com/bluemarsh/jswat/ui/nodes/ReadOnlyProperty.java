/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat UI module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ReadOnlyProperty.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import java.lang.reflect.InvocationTargetException;
import org.openide.nodes.PropertySupport;

/**
 * A node property that is read-only.
 *
 * @author  Nathan Fiedler
 */
public class ReadOnlyProperty extends PropertySupport.ReadOnly {
    /** The read-only value. */
    private Object value;

    /**
     * Constructs a read-only property to represent the given error.
     *
     * @param  name              the name of the property.
     * @param  type              the class type of the property.
     * @param  displayName       the display name of the property.
     * @param  shortDescription  a short description of the property.
     * @param  value             the read-only value.
     */
    public ReadOnlyProperty(String name, Class type, String displayName,
            String shortDescription, Object value) {
        super(name, type, displayName, shortDescription);
        this.value = value;
    }

    public Object getValue() throws IllegalAccessException,
            InvocationTargetException {
        return value;
    }
}
