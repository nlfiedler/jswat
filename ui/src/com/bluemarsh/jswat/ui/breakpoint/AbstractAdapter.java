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
 * are Copyright (C) 2006-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.breakpoint;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;

/**
 * An abstract implementation of a BreakpointAdapter.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractAdapter extends JPanel implements BreakpointAdapter {
    /** Manages property change listeners. */
    private PropertyChangeSupport propSupport;

    /**
     * Creates a new instance of AbstractAdapter.
     */
    public AbstractAdapter() {
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        initPropSupport();
        propSupport.addPropertyChangeListener(listener);
    }

    /**
     * Fires the validInput property change in which the new property value
     * is that given in the parameter. If that value is null, then the
     * input is considered to be valid. Otherwise, the message will be made
     * visible to the user.
     *
     * @param  msg  invalid input message, or null if input is valid.
     */
    protected void fireInputPropertyChange(String msg) {
        initPropSupport();
        propSupport.firePropertyChange(PROP_INPUTVALID, "DummyValue", msg);
    }

    /**
     * Create the PropertyChangeSupport instance on demand.
     */
    private synchronized void initPropSupport() {
        // For some reason, the Synth LAF causes problems with invoking
        // methods before the object is completely constructed, so need
        // to ensure propery support is built on demand.
        if (propSupport == null) {
            propSupport = new PropertyChangeSupport(this);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        initPropSupport();
        propSupport.removePropertyChangeListener(listener);
    }
}
