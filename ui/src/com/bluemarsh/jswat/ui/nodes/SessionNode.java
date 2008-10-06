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
 * are Copyright (C) 2004-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.components.SessionPropertiesPanel;
import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.PropertySupport.Reflection;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents a Session in the node tree.
 *
 * @author  Nathan Fiedler
 */
public class SessionNode extends BaseNode implements
        PropertyChangeListener, SessionListener {
    /** Name of the session name property. */
    public static final String PROP_NAME = "name";
    /** Name of the debuggee address property. */
    public static final String PROP_HOST = "host";
    /** Name of the session state property. */
    public static final String PROP_STATE = "state";
    /** Name of the session stratum property. */
    public static final String PROP_LANG = "lang";
    /** Our Session. */
    private Session session;

    /**
     * Constructs a SessionNode to represent the given Session.
     *
     * @param  session  the Session.
     */
    public SessionNode(Session session) {
        super();
        this.session = session;
        session.addPropertyChangeListener(this);
        session.addSessionListener(this);
    }

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
        }  catch (NoSuchMethodException nsme) {
            ErrorManager.getDefault().notify(nsme);
            prop = new ExceptionProperty(nsme);
        }
        return prop;
    }

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

    public Component getCustomizer() {
        SessionPropertiesPanel spp = new SessionPropertiesPanel();
        return spp.customize(session);
    }

    public String getDisplayName() {
        return session.getProperty(Session.PROP_SESSION_NAME);
    }

    public Image getIcon(int type) {
        String url;
        SessionManager sm = SessionProvider.getSessionManager();
        if (session == sm.getCurrent()) {
            url = NbBundle.getMessage(SessionNode.class,
                    "IMG_CurrentSessionNode");
        }  else {
            url = NbBundle.getMessage(SessionNode.class, "IMG_SessionNode");
        }
        return Utilities.loadImage(url);
    }

    /**
     * Returns the Session this node represents.
     *
     * @return  session.
     */
    public Session getSession() {
        return session;
    }

    public boolean hasCustomizer() {
        return true;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (name.equals(Session.PROP_SESSION_NAME)) {
            displayNameChanged();
        }
        // Need to ignore the other random properties that are not
        // in our "defined property set" or NB throws exceptions.
    }

    public void closing(SessionEvent sevt) {
        // don't need to do anything here
    }

    public void connected(SessionEvent sevt) {
        propertyChanged(PROP_STATE, null, null);
    }

    public void disconnected(SessionEvent sevt) {
        propertyChanged(PROP_STATE, null, null);
    }

    public void opened(Session session) {
        // don't need to do anything here
    }

    public void resuming(SessionEvent sevt) {
        propertyChanged(PROP_STATE, null, null);
    }

    public void suspended(SessionEvent sevt) {
        propertyChanged(PROP_STATE, null, null);
    }

    /**
     * Wacky PropertySupport.Reflection cannot find methods in node class
     * or in this inner class, so do it the hard way.
     */
    protected class SessionName extends PropertySupport.ReadWrite {

        /**
         * Constructs a new instance of SessionName.
         */
        public SessionName() {
            super(PROP_NAME, String.class, NbBundle.getMessage(SessionName.class,
                    "CTL_SessionsView_Column_Name_" + PROP_NAME),
                    NbBundle.getMessage(SessionName.class,
                    "CTL_SessionsView_Column_Desc_" + PROP_NAME));
        }

        public Object getValue() {
            return session.getProperty(Session.PROP_SESSION_NAME);
        }

        public void setValue(Object val) {
            String name = val.toString();
            session.setProperty(Session.PROP_SESSION_NAME, name);
            // Property change event will cause display name to be updated.
        }
    }
}
