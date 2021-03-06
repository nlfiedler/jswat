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
 * are Copyright (C) 2005-2013. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 */
package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.ContextEvent;
import com.bluemarsh.jswat.core.context.ContextListener;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.nodes.NodeFactory;
import com.bluemarsh.jswat.nodes.stack.StackFrameNode;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JScrollPane;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Class StackView displays the call stack of the current thread.
 * <p/>
 * @author Nathan Fiedler
 */
public class StackView extends AbstractView
        implements ExplorerManager.Provider, ContextListener, SessionListener,
        SessionManagerListener {

    /**
     * silence the compiler warnings
     */
    private static final long serialVersionUID = 1L;
    /**
     * Our explorer manager.
     */
    private ExplorerManager explorerManager;
    /**
     * Component showing node tree.
     */
    private PersistentOutlineView nodeView;
    /**
     * Columns for the tree-table view.
     */
    private transient Node.Property[] columns;

    /**
     * Constructs a StackView instance.
     */
    public StackView() {
        explorerManager = new ExplorerManager();
        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the view.
        String columnLabel = NbBundle.getMessage(
                StackView.class, "CTL_StackView_Column_Name_"
                + StackFrameNode.PROP_LOCATION);
        nodeView = new PersistentOutlineView(columnLabel);
        nodeView.getOutline().setRootVisible(false);
        nodeView.setPropertyColumnDescription(columnLabel, NbBundle.getMessage(
                StackView.class, "CTL_StackView_Column_Desc_"
                + StackFrameNode.PROP_LOCATION));
        addColumn(nodeView, StackFrameNode.PROP_SOURCE);
        addColumn(nodeView, StackFrameNode.PROP_CODEINDEX);
        // This, oddly enough, enables the column hiding feature.
        nodeView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setLayout(new BorderLayout());
        add(nodeView, BorderLayout.CENTER);
    }

    /**
     * Adds a column to the outline view, with attributes extracted from the
     * properties associated with the given name.
     * <p/>
     * @param view the outline view to modify.
     * @param name the name of the property column to add.
     */
    private void addColumn(OutlineView view, String name) {
        String displayName = NbBundle.getMessage(
                StackView.class, "CTL_StackView_Column_Name_" + name);
        String description = NbBundle.getMessage(
                StackView.class, "CTL_StackView_Column_Desc_" + name);
        view.addPropertyColumn(name, displayName, description);
    }

    /**
     * Build a new root node and set it to be the explorer's root context.
     * <p/>
     * @param kids root node's children, or Children.LEAF if none.
     */
    private void buildRoot(Children kids) {
        // Use a simple root node for which we can set the display name;
        // otherwise the logical root's properties affect the table headers.
        Node rootNode = new AbstractNode(kids);
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                StackView.class, "CTL_StackView_Column_Name_"
                + StackFrameNode.PROP_LOCATION));
        rootNode.setShortDescription(NbBundle.getMessage(
                StackView.class, "CTL_StackView_Column_Desc_"
                + StackFrameNode.PROP_LOCATION));
        explorerManager.setRootContext(rootNode);
    }

    /**
     * Builds the node tree for the current session.
     */
    private void buildTree() {
        // Populate the root node with FrameNode children.
        List<Node> list = new LinkedList<Node>();
        Node currentNode = null;
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        if (session.isConnected()) {
            DebuggingContext dc = ContextProvider.getContext(session);
            ThreadReference thread = dc.getThread();
            int currentFrame = dc.getFrame();
            if (thread != null) {
                try {
                    List<StackFrame> frames = thread.frames();
                    int index = 0;
                    NodeFactory factory = NodeFactory.getDefault();
                    for (StackFrame frame : frames) {
                        StackFrameNode node = factory.createStackFrameNode(
                                index, frame);
                        list.add(node);
                        if (index == currentFrame) {
                            currentNode = node;
                        }
                        index++;
                    }
                } catch (IncompatibleThreadStateException itse) {
                    // Do nothing and leave the node list empty.
                } catch (InvalidStackFrameException isfe) {
                    // Do nothing and leave the node list empty.
                } catch (VMDisconnectedException vmde) {
                    // Do nothing and leave the node list empty.
                }
            }
        }
        if (list.size() > 0) {
            Children children = new Children.Array();
            Node[] nodes = list.toArray(new Node[list.size()]);
            children.add(nodes);
            buildRoot(children);
            if (currentNode != null) {
                final PersistentOutlineView view = nodeView;
                final Node node = currentNode;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        view.scrollAndSelectNode(node);
                    }
                });
            }
        } else {
            buildRoot(Children.LEAF);
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Force the window title to be updated.
                setDisplayName(getDisplayName());
            }
        });
    }

    @Override
    public void changedFrame(ContextEvent ce) {
        if (!ce.isSuspending() && isCurrent(ce.getSession())) {
            buildTree();
        }
    }

    @Override
    public void changedLocation(ContextEvent ce) {
    }

    @Override
    public void changedThread(ContextEvent ce) {
        if (!ce.isSuspending() && isCurrent(ce.getSession())) {
            buildTree();
        }
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        // Clear the tree to release resources.
        buildRoot(Children.LEAF);
        // Stop listening to everything that affects our tree.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.removeSessionManagerListener(this);
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            Session session = iter.next();
            session.removeSessionListener(this);
            DebuggingContext dc = ContextProvider.getContext(session);
            dc.removeContextListener(this);
        }
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        // Build out the tree.
        buildTree();
        // Start listening to everything that affects our tree.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.addSessionManagerListener(this);
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            Session session = iter.next();
            session.addSessionListener(this);
            DebuggingContext dc = ContextProvider.getContext(session);
            dc.addContextListener(this);
        }
    }

    @Override
    public void connected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    @Override
    public String getDisplayName() {
        // Incorporate the thread name into the window title.
        String threadName = "";
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        if (session.isConnected()) {
            DebuggingContext dc = ContextProvider.getContext(session);
            ThreadReference thread = dc.getThread();
            if (thread != null) {
                threadName = thread.name();
            }
        }
        String nm = NbBundle.getMessage(StackView.class, "CTL_StackView_Name");
        if (threadName.length() > 0) {
            return nm + " - " + threadName;
        } else {
            return nm;
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-stack-view");
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage(StackView.class, "CTL_StackView_Tooltip");
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    protected String preferredID() {
        return getClass().getName();
    }

    // Secret, undocumented method that NetBeans calls?
    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        if (version.equals("1.0")) {
            nodeView.readSettings(p, "Stack");
        }
    }

    @Override
    public void resuming(SessionEvent sevt) {
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.addContextListener(this);
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        session.removeSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.removeContextListener(this);
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
        buildTree();
    }

    @Override
    public void suspended(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    // Secret, undocumented method that NetBeans calls?
    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        nodeView.writeSettings(p, "Stack");
    }
}
