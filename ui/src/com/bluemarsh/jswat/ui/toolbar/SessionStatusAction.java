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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SessionStatusAction.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.toolbar;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.VMDisconnectedException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;

/**
 * Class SessionStatusAction implements a toolbar action with a custom
 * presenter that displays the list of sessions and their status.
 *
 * @author Nathan Fiedler
 */
public class SessionStatusAction extends CallableSystemAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Indicates if this action can be invoked on any thread.
     *
     * @return  true if asynchronous, false otherwise.
     */
    protected boolean asynchronous() {
        return false;
    }

    /**
     * Returns the help context for this action.
     *
     * @return  help context.
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Returns the name of this action.
     *
     * @return  name of action.
     */
    public String getName() {
        return NbBundle.getMessage(getClass(), "LBL_SessionStatusAction");
    }

    /**
     * Get a component that can present this action in a JToolBar.
     *
     * @return  the representation for this action.
     */
    public Component getToolbarPresenter() {
        // Use a GridBagLayout so that the combobox is not stretched
        // to fill the space created by the toolbar.
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 0);
        gbc.weightx = 1.0d;
        panel.add(new SessionStatusPresenter(), gbc);
        return panel;
    }

    /**
     * Performs the action.
     */
    public void performAction() {
        // nothing to do
    }
}

/**
 * A component to display the current session name and its state, and allow
 * the user to set the current session.
 *
 * @author  Nathan Fiedler
 */
class SessionStatusPresenter extends JComboBox implements ItemListener,
        SessionListener, SessionManagerListener {
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
        }
        model.setSelectedItem(sm.getCurrent());
        setModel(model);
        sm.addSessionManagerListener(this);
        setMaximumSize(new Dimension(150, Integer.MAX_VALUE));
        addItemListener(this);
    }

    public void closing(SessionEvent sevt) {
    }

    public void connected(SessionEvent sevt) {
        repaint();
    }

    public void disconnected(SessionEvent sevt) {
        repaint();
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            Session session = (Session) e.getItem();
            SessionManager sm = SessionProvider.getSessionManager();
            sm.setCurrent(session);
        }
    }

    public void opened(Session session) {
    }

    public void resuming(SessionEvent sevt) {
        repaint();
    }

    public void sessionAdded(SessionManagerEvent e) {
        model.addElement(e.getSession());
    }

    public void sessionRemoved(SessionManagerEvent e) {
        model.removeElement(e.getSession());
    }

    public void sessionSetCurrent(SessionManagerEvent e) {
        setSelectedItem(e.getSession());
    }

    public void suspended(SessionEvent sevt) {
        repaint();
    }
}

/**
 * Renders the list cell to indicate the session state and its name.
 *
 * @author  Nathan Fiedler
 */
class SessionStatusRenderer extends DefaultListCellRenderer {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The icon for when the session is suspended. */
    private static Icon pausedIcon;
    /** The icon for when the session is running. */
    private static Icon startingIcon;
    /** The icon for when the session is disconnected. */
    private static Icon stoppedIcon;

    static {
        pausedIcon = new ImageIcon(Utilities.loadImage(NbBundle.getMessage(
                SessionStatusRenderer.class, "IMG_SessionStatus_Paused")));
        startingIcon = new ImageIcon(Utilities.loadImage(NbBundle.getMessage(
                SessionStatusRenderer.class, "IMG_SessionStatus_Starting")));
        stoppedIcon = new ImageIcon(Utilities.loadImage(NbBundle.getMessage(
                SessionStatusRenderer.class, "IMG_SessionStatus_Stopped")));
    }

    /**
     * Return a component that has been configured to display the specified value.
     *
     * @param  list          the JList we're painting.
     * @param  value         the value returned by list.getModel().getElementAt(index).
     * @param  index         the cell's index.
     * @param  isSelected    true if the specified cell was selected.
     * @param  cellHasFocus  true if the specified cell has the focus.
     */
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list,
                value, index, isSelected, cellHasFocus);
        // Set the label icon and text to suit the session state.
        Session session = (Session) value;
        if (session.isConnected()) {
            try {
                if (session.isSuspended()) {
                    label.setIcon(pausedIcon);
                } else {
                    label.setIcon(startingIcon);
                }
            } catch (IllegalStateException ise) {
                // Session disconnected between 'isConnected' and 'isSuspended'.
                label.setIcon(stoppedIcon);
	    } catch (VMDisconnectedException vmde) {
                label.setIcon(stoppedIcon);
            }
        } else {
            label.setIcon(stoppedIcon);
        }
        label.setText(session.getProperty(Session.PROP_SESSION_NAME));
        return label;
    }
}
