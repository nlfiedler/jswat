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
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JScrollPane;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Class StackView displays the call stack of the current thread.
 *
 * @author  Nathan Fiedler
 */
public class StackView extends AbstractView
        implements ExplorerManager.Provider, ContextListener, SessionListener,
        SessionManagerListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing node tree. */
    private PersistentTreeTableView nodeView;
    /** Columns for the tree-table view. */
    private transient Node.Property[] columns;

    /**
     * Constructs a StackView instance.
     */
    public StackView() {
        explorerManager = new ExplorerManager();
        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the stack view. We use a tree-table because it has
        // features that we want to utilize.
        nodeView = new PersistentTreeTableView();
        nodeView.setRootVisible(false);
        columns = new Node.Property[] {
            new Column(StackFrameNode.PROP_LOCATION, true, false),
            new Column(StackFrameNode.PROP_SOURCE, false, true),
            new Column(StackFrameNode.PROP_CODEINDEX, false, true),
        };
        nodeView.setProperties(columns);
        // This, oddly enough, enables the column hiding feature.
        nodeView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setLayout(new BorderLayout());
        add(nodeView, BorderLayout.CENTER);
    }

    /**
     * Build a new root node and set it to be the explorer's root context.
     *
     * @param  kids  root node's children, or Children.LEAF if none.
     */
    private void buildRoot(Children kids) {
        // Use a simple root node for which we can set the display name;
        // otherwise the logical root's properties affect the table headers.
        Node rootNode = new AbstractNode(kids);
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                StackView.class, "CTL_StackView_Column_Name_" +
                StackFrameNode.PROP_LOCATION));
        rootNode.setShortDescription(NbBundle.getMessage(
                StackView.class, "CTL_StackView_Column_Desc_" +
                StackFrameNode.PROP_LOCATION));
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
                final PersistentTreeTableView view = nodeView;
                final Node node = currentNode;
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        view.scrollAndSelectNode(node);
                    }
                });
            }
        } else {
            buildRoot(Children.LEAF);
        }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Force the window title to be updated.
                setDisplayName(getDisplayName());
            }
        });
    }

    public void changedFrame(ContextEvent ce) {
        if (!ce.isSuspending() && isCurrent(ce.getSession())) {
            buildTree();
        }
    }

    public void changedLocation(ContextEvent ce) {
    }

    public void changedThread(ContextEvent ce) {
        if (!ce.isSuspending() && isCurrent(ce.getSession())) {
            buildTree();
        }
    }

    public void closing(SessionEvent sevt) {
    }

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

    public void connected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    public void disconnected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

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

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-stack-view");
    }

    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    public String getToolTipText() {
        return NbBundle.getMessage(StackView.class, "CTL_StackView_Tooltip");
    }

    public void opened(Session session) {
    }

    protected String preferredID() {
        return getClass().getName();
    }

    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        restoreColumns(in, columns);
        nodeView.setProperties(columns);
        nodeView.restoreColumnWidths(in);
    }

    public void resuming(SessionEvent sevt) {
    }

    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.addContextListener(this);
    }

    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        session.removeSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.removeContextListener(this);
    }

    public void sessionSetCurrent(SessionManagerEvent e) {
        buildTree();
    }

    public void suspended(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        saveColumns(out, columns);
        nodeView.saveColumnWidths(out);
    }

    /**
     * A column for the session table.
     *
     * @author  Nathan Fiedler
     */
    protected class Column extends PropertySupport.ReadOnly {
        /** The keyword for this column. */
        private String key;

        /**
         * Constructs a new instance of Column.
         *
         * @param  key     keyword for this column.
         * @param  tree    true if this is the 'tree' column, false if 'table' column.
         * @param  hidden  true to hide this column initially.
         */
        @SuppressWarnings("unchecked")
        public Column(String key, boolean tree, boolean hidden) {
            super(key, String.class,
                  NbBundle.getMessage(Column.class, "CTL_StackView_Column_Name_" + key),
                  NbBundle.getMessage(Column.class, "CTL_StackView_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
            setValue("InvisibleInTreeTableView", Boolean.valueOf(hidden));
            // Stack should not be sortable as that makes no sense whatsoever.
        }

        public Object getValue()
                throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }
}
