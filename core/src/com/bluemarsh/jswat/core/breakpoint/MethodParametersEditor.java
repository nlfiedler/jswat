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
 * are Copyright (C) 2007-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

import com.bluemarsh.jswat.core.util.Strings;
import java.beans.PropertyEditorSupport;
import java.util.List;

/**
 * Property editor for the methodParameters property.
 *
 * @author Nathan Fiedler
 */
public class MethodParametersEditor extends PropertyEditorSupport {

    /**
     * Creates a new instance of MethodParametersEditor.
     */
    public MethodParametersEditor() {
    }

    @Override
    public String getAsText() {
        Object value = getValue();
        if (value instanceof List<?>) {
            // Do not change the list values, these are used in persistence
            // and need to be resolvable upon deserialization.
            return Strings.listToString((List<?>) value);
        }
        return "";
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        // Do nothing since we disallow editing in this manner.
    }
}
