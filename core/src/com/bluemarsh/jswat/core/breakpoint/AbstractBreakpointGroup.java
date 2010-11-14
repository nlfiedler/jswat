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
 * are Copyright (C) 2001-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.core.breakpoint;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Class AbstractBreakpointGroup is an abstract implementation of the
 * BreakpointGroup interface. It implements some of the basic behavior
 * of breakpoint groups.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractBreakpointGroup implements BreakpointGroup {

    /** True if this breakpoint group is enabled. */
    private boolean isEnabled;
    /** Name of our breakpoint group. Used for display. */
    private String groupName;
    /** The breakpoint group to which we belong (always non-null). */
    private BreakpointGroup parentGroup;
    /** Handles property change listeners and sending events. */
    private PropertyChangeSupport propSupport;
    /** List of breakpoint group listeners. */
    private BreakpointGroupListener listeners;

    /**
     * Creates a new instance of AbstractBreakpointGroup.
     */
    public AbstractBreakpointGroup() {
        isEnabled = true;
        propSupport = new PropertyChangeSupport(this);
    }

    @Override
    public void addBreakpointGroupListener(BreakpointGroupListener listener) {
        if (listener != null) {
            synchronized (this) {
                listeners = BreakpointGroupEventMulticaster.add(listeners, listener);
            }
            propSupport.addPropertyChangeListener(listener);
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    /**
     * Notify breakpoint group listeners that this group has changed.
     *
     * @param  type  type of change.
     */
    protected void fireChange(BreakpointGroupEventType type) {
        BreakpointGroupEvent e = new BreakpointGroupEvent(this, type);
        BreakpointGroupListener gl;
        synchronized (this) {
            gl = listeners;
        }
        if (gl != null) {
            e.getType().fireEvent(e, gl);
        }
    }

    @Override
    public String getName() {
        return groupName;
    }

    @Override
    public BreakpointGroup getParent() {
        return parentGroup;
    }

    @Override
    public boolean isEnabled() {
        if (parentGroup == null) {
            return isEnabled;
        } else {
            return parentGroup.isEnabled() && isEnabled;
        }
    }

    @Override
    public void removeBreakpointGroupListener(BreakpointGroupListener listener) {
        if (listener != null) {
            synchronized (this) {
                listeners = BreakpointGroupEventMulticaster.remove(listeners, listener);
            }
            propSupport.removePropertyChangeListener(listener);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void setEnabled(boolean enabled) {
        boolean old = isEnabled;
        isEnabled = enabled;
        propSupport.firePropertyChange(PROP_ENABLED, old, enabled);
    }

    @Override
    public void setName(String name) {
        String old = groupName;
        groupName = name;
        propSupport.firePropertyChange(PROP_NAME, old, name);
    }

    @Override
    public void setParent(BreakpointGroup parent) {
        BreakpointGroup old = parentGroup;
        parentGroup = parent;
        propSupport.firePropertyChange(PROP_PARENT, old, parent);
    }
}
