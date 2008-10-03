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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: AttachDebuggeeAction.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.ui.actions;

import com.bluemarsh.jswat.core.connect.ConnectionEvent;
import com.bluemarsh.jswat.core.connect.ConnectionListener;
import com.bluemarsh.jswat.core.connect.JvmConnection;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionFactory;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.ActionEnabler;
import com.bluemarsh.jswat.ui.components.AttachDebuggeePanel;
import com.sun.jdi.event.VMStartEvent;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Provides a means for attaching the debugger to a debuggee.
 *
 * @author Nathan Fiedler
 */
public class AttachDebuggeeAction extends CallableSystemAction {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of AttachDebuggeeAction.
     */
    public AttachDebuggeeAction() {
        ActionEnabler ae = ActionEnabler.getDefault();
        ae.registerInactiveAction(this);
    }

    /**
     * Indicates if this action can be invoked on any thread.
     *
     * @return  true if asynchronous, false otherwise.
     */
    protected boolean asynchronous() {
        // performAction() should run in event thread
        return false;
    }

    /**
     * Returns the help context for this action.
     *
     * @return  help context.
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-attach-debuggee");
    }

    /**
     * Returns the name of this action.
     *
     * @return  name of action.
     */
    public String getName() {
        return NbBundle.getMessage(getClass(), "LBL_AttachDebuggeeAction");
    }

    /**
     * Specify the proper resource name for the action's icon.
     *
     * @return  the resource name for the icon.
     */
    protected String iconResource() {
        return NbBundle.getMessage(getClass(), "IMG_AttachDebuggeeAction");
    }

    /**
     * Performs the action.
     */
    public void performAction() {
        final AttachDebuggeePanel adp = new AttachDebuggeePanel();
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        adp.loadParameters(session);
        // Display dialog and get the user response.
        if (adp.display()) {
            if (session.isConnected()) {
                // See if the user wants to open a new session.
                String msg = NbBundle.getMessage(getClass(), "MSG_ActivateNewSession");
                NotifyDescriptor desc = new NotifyDescriptor.Confirmation(msg);
                Object ans = DialogDisplayer.getDefault().notify(desc);
                if (ans == NotifyDescriptor.YES_OPTION) {
                    SessionFactory sf = SessionProvider.getSessionFactory();
                    String id = sm.generateIdentifier();
                    session = sf.createSession(id);
                    sm.add(session);
                    sm.setCurrent(session);
                } else if (ans == NotifyDescriptor.CANCEL_OPTION) {
                    return;
                } else {
                    session.disconnect(true);
                }
            }
            // Regardless of whether or not an error occurs below, save the
            // settings the user provided. Must do this before building the
            // connection in order for the "ignore" setting to take affect
            // before the path manager connects to the session.
            adp.saveParameters(session);
            try {
                final Session theSession = session;
                // Build the connection and attach to the debuggee.
                final JvmConnection connection = adp.buildConnection();
                // The actual connection may be made some time from now,
                // so set up a listener to be notified at that time.
                connection.addConnectionListener(new ConnectionListener() {
                    public void connected(ConnectionEvent event) {
                        if (theSession.isConnected()) {
                            // The user already connected to something else.
                            JvmConnection c = event.getConnection();
                            c.getVM().dispose();
                            c.disconnect();
                        } else {
                            theSession.connect(connection);
                            if (adp.shouldResume()) {
                                // Make sure the session does not suspend due to the
                                // remote VM starting after we tell it to resume.
                                theSession.addSessionListener(new SessionResumer());
                                theSession.resumeVM();
                            }
                        }
                    }
                });
                connection.connect();
            } catch (Exception e) {
                String msg = NbBundle.getMessage(
                        getClass(), "ERR_AttachFailed", e.toString());
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                    msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }

    /**
     * Waits for the session to suspend, and if it suspends due to a
     * VMStartEvent, instructs the session to resume.
     *
     * @author Nathan Fiedler
     */
    private static class SessionResumer implements SessionListener {

        public void closing(SessionEvent sevt) {
        }

        public void connected(SessionEvent sevt) {
        }

        public void disconnected(SessionEvent sevt) {
            // We're done listening to this session.
            sevt.getSession().removeSessionListener(this);
        }

        public void opened(Session session) {
        }

        public void resuming(SessionEvent sevt) {
        }

        public void suspended(SessionEvent sevt) {
            if (sevt.getEvent() instanceof VMStartEvent) {
                // Thread timing caused the VM to suspend after we told it
                // to resume, so ask it to resume once more.
                sevt.getSession().resumeVM();
            }
        }
    }
}
