/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is JSwat. The Initial Developer of the Original
 * Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2001-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AbstractBreakpointGroup.java 15 2007-06-03 00:01:17Z nfiedler $
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

    public void addBreakpointGroupListener(BreakpointGroupListener listener) {
        if (listener != null) {
            synchronized (this) {
                listeners = BreakpointGroupEventMulticaster.add(listeners, listener);
            }
            propSupport.addPropertyChangeListener(listener);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    /**
     * Notify breakpoint group listeners that this group has changed.
     *
     * @param  type  type of change.
     */
    protected void fireChange(BreakpointGroupEvent.Type type) {
        BreakpointGroupEvent e = new BreakpointGroupEvent(this, type);
        BreakpointGroupListener gl;
        synchronized (this) {
            gl = listeners;
        }
        if (gl != null) {
            e.getType().fireEvent(e, gl);
        }
    }

    public String getName() {
        return groupName;
    }

    public BreakpointGroup getParent() {
        return parentGroup;
    }

    public boolean isEnabled() {
        if (parentGroup == null) {
            return isEnabled;
        } else {
            return parentGroup.isEnabled() && isEnabled;
        }
    }

    public void removeBreakpointGroupListener(BreakpointGroupListener listener) {
        if (listener != null) {
            synchronized (this) {
                listeners = BreakpointGroupEventMulticaster.remove(listeners, listener);
            }
            propSupport.removePropertyChangeListener(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    public void setEnabled(boolean enabled) {
        boolean old = isEnabled;
        isEnabled = enabled;
        propSupport.firePropertyChange(PROP_ENABLED, old, enabled);
    }

    public void setName(String name) {
        String old = groupName;
        groupName = name;
        propSupport.firePropertyChange(PROP_NAME, old, name);
    }

    public void setParent(BreakpointGroup parent) {
        BreakpointGroup old = parentGroup;
        parentGroup = parent;
        propSupport.firePropertyChange(PROP_PARENT, old, parent);
    }
}
