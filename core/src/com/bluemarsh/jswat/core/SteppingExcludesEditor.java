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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SteppingExcludesEditor.java 15 2007-06-03 00:01:17Z nfiedler $
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
