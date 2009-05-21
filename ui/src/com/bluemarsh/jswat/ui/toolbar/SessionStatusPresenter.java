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
 * are Copyright (C) 2005-2009. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.toolbar;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * A component to display the current session name and its state, and allow
 * the user to set the current session.
 *
 * @author  Nathan Fiedler
 */
public class SessionStatusPresenter extends JComboBox implements ItemListener,
        PropertyChangeListener, SessionListener, SessionManagerListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The combobox model. */
    private DefaultComboBoxModel model;

    /**
     * Constructs a new instance of SessionStatusPresenter.
     */
    public SessionStatusPresenter() {
        super();
        // Set up list renderer to show the sessions.
        setRenderer(new SessionStatusRenderer());
        SessionManager sm = SessionProvider.getSessionManager();
        Iterator<Session> iter = sm.iterateSessions();
        model = new DefaultComboBoxModel();
        while (iter.hasNext()) {
            Session session = iter.next();
            model.addElement(session);
            session.addSessionListener(this);
            session.addPropertyChangeListener(this);
        }
        model.setSelectedItem(sm.getCurrent());
        setModel(model);
        sm.addSessionManagerListener(this);
        setMaximumSize(new Dimension(150, Integer.MAX_VALUE));
        addItemListener(this);
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    public void connected(SessionEvent sevt) {
        repaint();
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        repaint();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            Session session = (Session) e.getItem();
            SessionManager sm = SessionProvider.getSessionManager();
            sm.setCurrent(session);
        }
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(Session.PROP_SESSION_NAME)) {
            repaint();
        }
    }

    @Override
    public void resuming(SessionEvent sevt) {
        repaint();
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
        model.addElement(e.getSession());
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        model.removeElement(e.getSession());
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
        setSelectedItem(e.getSession());
    }

    @Override
    public void suspended(SessionEvent sevt) {
        repaint();
    }
}
