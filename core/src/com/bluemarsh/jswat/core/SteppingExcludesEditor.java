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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.core;

import com.bluemarsh.jswat.core.util.Strings;
import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.util.List;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Property editor for the single-stepping exclusions property.
 *
 * @author Nathan Fiedler
 */
public class SteppingExcludesEditor extends PropertyEditorSupport
        implements ListDataListener {
    /** The custom editor. */
    private SteppingExcludesPanel customEditor;

    /**
     * Creates a new instance of SteppingExcludesEditor.
     */
    public SteppingExcludesEditor() {
        customEditor = new SteppingExcludesPanel();
        customEditor.addListDataListener(this);
    }

    public void contentsChanged(ListDataEvent e) {
        firePropertyChange();
    }

    public String getAsText() {
        List<String> list = customEditor.getValues();
        if (list != null) {
            return Strings.listToString(list);
        } else {
            return "";
        }
    }

    public Component getCustomEditor() {
        return customEditor;
    }

    public Object getValue() {
        return customEditor.getValues();
    }

    public void intervalAdded(ListDataEvent e) {
        firePropertyChange();
    }

    public void intervalRemoved(ListDataEvent e) {
        firePropertyChange();
    }

    public void setValue(Object value) {
        super.setValue(value);
        customEditor.setValues(value);
    }

    public boolean supportsCustomEditor() {
        return true;
    }
}
