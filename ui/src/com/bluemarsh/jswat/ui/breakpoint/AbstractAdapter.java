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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AbstractAdapter.java 15 2007-06-03 00:01:17Z nfiedler $
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
        propSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
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
        propSupport.firePropertyChange(PROP_INPUTVALID, "DummyValue", msg);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        propSupport.removePropertyChangeListener(listener);
    }
}
