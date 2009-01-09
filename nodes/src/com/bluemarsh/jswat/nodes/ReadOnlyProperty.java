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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.nodes;

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
    @SuppressWarnings("unchecked")
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
