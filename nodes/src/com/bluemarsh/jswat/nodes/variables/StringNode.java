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
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: StringNode.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.variables;

import com.sun.jdi.StringReference;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

/**
 * Represents a StringReference variable.
 *
 * @author Nathan Fiedler
 */
public class StringNode extends VariableNode {
    /** The string reference. */
    private StringReference sref;

    /**
     * Constructs a new instance of StringNode.
     *
     * @param  name  name of variable.
     * @param  type  type of variable.
     * @param  kind  kind of variable.
     * @param  sref  the StringReference.
     */
    public StringNode(String name, String type, VariableNode.Kind kind,
            StringReference sref) {
        super(Children.LEAF, name, type, kind);
        this.sref = sref;
    }

    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        Sheet.Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_VALUE, "\"" + sref.value() + "\""));
        return sheet;
    }
}
