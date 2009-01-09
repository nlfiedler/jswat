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

package com.bluemarsh.jswat.core.path;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * AbstractPathManager provides an abstract PathManager implementation for
 * concrete implementations to subclass. This class implements the
 * SessionListener interface so that the path values are loaded and saved
 * at the appropriate times.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractPathManager implements PathManager, SessionListener {
    /** Handles property change listeners and sending events. */
    private PropertyChangeSupport propSupport;
    /** The classpath defined by the user, used to restore the user's
     * defined classpath after disconnecting from a remote debuggee. */
    private List<String> userDefinedClassPath;

    /**
     * Creates a new instance of PathManager.
     */
    protected AbstractPathManager() {
        propSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    public void closing(SessionEvent sevt) {
        savePaths(sevt.getSession());
    }

    public void connected(SessionEvent sevt) {
        // Save this for later to restore the classpath defined by the user.
        userDefinedClassPath = getUserClassPath();
        // Retrieve the actual classpath from the debuggee and have the
        // concrete implementation officially register that classpath.
        List<String> cp = getClassPath();
        setClassPath(cp);
    }

    public void disconnected(SessionEvent sevt) {
        // Restore the classpath to what the user had intended.
        setClassPath(userDefinedClassPath);
    }

    /**
     * Notifies the registered property change listeners of a property change.
     *
     * @param  name      name of changed property.
     * @param  oldValue  the old property value.
     * @param  newValue  the new property value.
     */
    protected void firePropertyChange(String name, Object oldValue,
            Object newValue) {
        propSupport.firePropertyChange(name, oldValue, newValue);
    }

    /**
     * Retrieves the classpath defined by the user, rather than the actual
     * classpath of the debuggee, as in the case of getClassPath().
     *
     * @return  the user-defined classpath, may be null.
     */
    protected abstract List<String> getUserClassPath();

    /**
     * Load the class and source path values from storage.
     *
     * @param  session  associated Session instance.
     */
    protected abstract void loadPaths(Session session);

    public void opened(Session session) {
        loadPaths(session);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    public void resuming(SessionEvent sevt) {
    }

    /**
     * Save the class and source path values to persistent storage.
     *
     * @param  session  associated Session instance.
     */
    protected abstract void savePaths(Session session);

    public void suspended(SessionEvent sevt) {
    }
}
