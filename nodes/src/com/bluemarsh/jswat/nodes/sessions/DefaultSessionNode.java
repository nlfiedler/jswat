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
 * are Copyright (C) 2004-2010. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */
package com.bluemarsh.jswat.nodes.sessions;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.nodes.ExceptionProperty;
import com.bluemarsh.jswat.ui.components.SessionPropertiesPanel;
import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.actions.DeleteAction;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 * Represents a Session in the node tree.
 *
 * @author  Nathan Fiedler
 */
public class DefaultSessionNode extends SessionNode implements
        PropertyChangeListener, SessionListener, SessionManagerListener {

    /** Our Session. */
    private Session session;

    /**
     * Constructs a SessionNode to represent the given Session.
     *
     * @param  session  the Session.
     */
    public DefaultSessionNode(Session session) {
        super();
        this.session = session;
        session.addPropertyChangeListener(this);
        session.addSessionListener(this);
        SessionManager sm = SessionProvider.getSessionManager();
        sm.addSessionManagerListener(WeakListeners.create(
                SessionManagerListener.class, this, sm));
        getCookieSet().add(this);
    }

    @Override
    public boolean canDestroy() {
        // Make sure this session is not marked as current.
        SessionManager sm = SessionProvider.getSessionManager();
        Session current = sm.getCurrent();
        return !session.equals(current);
    }

    /**
     * Creates a node property of the given key (same as the column keys)
     * and a specific getter method on the given object.
     *
     * @param  key     property name (same as matching column).
     * @param  inst    object on which to reflect.
     * @param  getter  name of getter method for property value.
     * @param  setter  name of setter method for property value (may be null).
     * @return  new property.
     */
    @SuppressWarnings("unchecked")
    private Node.Property createProperty(String key, Object inst, String getter, String setter) {
        Property prop = null;
        try {
            // Each node property needs its own name, and the display properties
            // that were given to the corresponding table column.
            prop = new Reflection(inst, String.class, getter, setter);
            prop.setName(key);
            prop.setDisplayName(NbBundle.getMessage(
                    SessionNode.class, "CTL_SessionsView_Column_Name_" + key));
            prop.setShortDescription(NbBundle.getMessage(
                    SessionNode.class, "CTL_SessionsView_Column_Desc_" + key));
        } catch (NoSuchMethodException nsme) {
            ErrorManager.getDefault().notify(nsme);
            prop = new ExceptionProperty(nsme);
        }
        return prop;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        SessionName name = new SessionName();
        set.put(name);
        set.put(createProperty(PROP_HOST, session, "getAddress", null));
        set.put(createProperty(PROP_STATE, session, "getState", null));
        set.put(createProperty(PROP_LANG, session, "getStratum", null));
        return sheet;
    }

    @Override
    public void destroy() throws IOException {
        SessionManager sm = SessionProvider.getSessionManager();
        if (session.isConnected()) {
            boolean exit = !session.getConnection().isRemote();
            session.disconnect(exit);
        }
        session.close();
        sm.remove(session);
        super.destroy();
    }

    @Override
    public Component getCustomizer() {
        return new SessionPropertiesPanel(session);
    }

    @Override
    public String getDisplayName() {
        return session.getProperty(Session.PROP_SESSION_NAME);
    }

    @Override
    public Image getIcon(int type) {
        String url;
        SessionManager sm = SessionProvider.getSessionManager();
        if (session == sm.getCurrent()) {
            url = NbBundle.getMessage(SessionNode.class,
                    "IMG_CurrentSessionNode");
        } else {
            url = NbBundle.getMessage(SessionNode.class, "IMG_SessionNode");
        }
        return ImageUtilities.loadImage(url);
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public boolean hasCustomizer() {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (name.equals(Session.PROP_SESSION_NAME)) {
            fireDisplayNameChange(null, null);
        }
        // Need to ignore the other random properties that are not
        // in our "defined property set".
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    public void connected(SessionEvent sevt) {
        firePropertyChange(PROP_STATE, null, null);
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        firePropertyChange(PROP_STATE, null, null);
    }

    @Override
    protected Action[] getNodeActions() {
        return new Action[]{
                    SystemAction.get(CopySessionAction.class),
                    SystemAction.get(DeleteAction.class),
                    SystemAction.get(SetCurrentAction.class),
                    SystemAction.get(FinishSessionAction.class),
                    SystemAction.get(FinishAllAction.class),
                    SystemAction.get(DescribeDebuggeeAction.class),};
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(SetCurrentAction.class);
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    public void resuming(SessionEvent sevt) {
        firePropertyChange(PROP_STATE, null, null);
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
        // Our "current" status may have changed.
        fireIconChange();
    }

    @Override
    public void suspended(SessionEvent sevt) {
        firePropertyChange(PROP_STATE, null, null);
    }

    /**
     * Wacky PropertySupport.Reflection cannot find methods in node class
     * or in this inner class, so do it the hard way.
     */
    protected class SessionName extends PropertySupport.ReadWrite {

        /**
         * Constructs a new instance of SessionName.
         */
        @SuppressWarnings("unchecked")
        public SessionName() {
            super(PROP_NAME, String.class, NbBundle.getMessage(SessionName.class,
                    "CTL_SessionsView_Column_Name_" + PROP_NAME),
                    NbBundle.getMessage(SessionName.class,
                    "CTL_SessionsView_Column_Desc_" + PROP_NAME));
        }

        @Override
        public Object getValue() {
            return session.getProperty(Session.PROP_SESSION_NAME);
        }

        @Override
        public void setValue(Object val) {
            String name = val.toString();
            session.setProperty(Session.PROP_SESSION_NAME, name);
            // Property change event will cause display name to be updated.
        }
    }
}
