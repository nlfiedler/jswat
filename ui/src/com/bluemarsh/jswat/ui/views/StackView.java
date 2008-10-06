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
 * are Copyright (C) 2005-2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: StackView.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
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
import com.bluemarsh.jswat.ui.nodes.BaseNode;
import com.bluemarsh.jswat.ui.nodes.ShowSourceAction;
import com.bluemarsh.jswat.ui.nodes.StackFrameNode;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.Location;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JScrollPane;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

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
    /** Array of node actions. */
    private Action[] nodeActions;
    /** Columns for the tree-table view. */
    private transient Node.Property[] columns;

    /**
     * Constructs a StackView instance.
     */
    public StackView() {
        explorerManager = new ExplorerManager();
        buildRoot(Children.LEAF);
        nodeActions = new Action[] {
            SystemAction.get(SetCurrentFrameAction.class),
            SystemAction.get(ShowSourceAction.class),
            SystemAction.get(PopFramesAction.class),
            SystemAction.get(SetBreakpointAction.class),
        };
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
        Node rootNode = new BaseNode(kids);
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
                    for (StackFrame frame : frames) {
                        StackFrameNode node = createStackFrameNode(index, frame);
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

    /**
     * Called only when top component was closed so that now it is closed
     * on all workspaces in the system.
     */
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

    /**
     * Called only when top component was closed on all workspaces before
     * and now is opened for the first time on some workspace.
     */
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

    /**
     * Constructs a Node to represent the given stack frame.
     *
     * @param  index  zero-based offset of this frame.
     * @param  frame  stack frame.
     * @return  node for stack frame.
     */
    private StackFrameNode createStackFrameNode(int index, StackFrame frame) {
        StackFrameNode node = new StackFrameNode(index, frame);
        node.setActions(nodeActions);
        node.setPreferredAction(SystemAction.get(SetCurrentFrameAction.class));
        return node;
    }

    public void disconnected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    /**
     * Returns the display name for this component.
     *
     * @return  display name.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(StackView.class, "CTL_StackView_Name");
    }

    /**
     * Returns the explorer manager for this view.
     *
     * @return  explorer manager.
     */
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    /**
     * Get the help context for this component.
     *
     * @return  the help context.
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-stack-view");
    }

    /**
     * Returns the desired persistent type for this component.
     *
     * @return  desired persistence type.
     */
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    /**
     * Returns the display name for this component.
     *
     * @return  tooltip text.
     */
    public String getToolTipText() {
        return NbBundle.getMessage(StackView.class, "CTL_StackView_Tooltip");
    }

    public void opened(Session session) {
    }

    /**
     * Returns the unique identifier for this component.
     *
     * @return  unique component identifier.
     */
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

    /**
     * Called when a Session has been added to the SessionManager.
     *
     * @param  e  session manager event.
     */
    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.addContextListener(this);
    }

    /**
     * Called when a Session has been removed from the SessionManager.
     *
     * @param  e  session manager event.
     */
    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        session.removeSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.removeContextListener(this);
    }

    /**
     * Called when a Session has been made the current session.
     *
     * @param  e  session manager event.
     */
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
        public Column(String key, boolean tree, boolean hidden) {
            super(key, String.class,
                  NbBundle.getMessage(Column.class, "CTL_StackView_Column_Name_" + key),
                  NbBundle.getMessage(Column.class, "CTL_StackView_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
            setValue("InvisibleInTreeTableView", Boolean.valueOf(hidden));
            // Stack should not be sortable as that makes no sense whatsoever.
        }

        /**
         * Returns the column key value.
         *
         * @return  column value.
         */
        public Object getValue()
                throws IllegalAccessException, InvocationTargetException {
            return key;
        }
    }

    /**
     * Implements the action of popping frames from the stack.
     *
     * @author  Nathan Fiedler
     */
    public static class PopFramesAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

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
         * Test whether the action should be enabled based on the currently
         * activated nodes.
         *
         * @param  activatedNodes  target nodes.
         * @return  true if enabled, false otherwise.
         */
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1) {
                SessionManager sm = SessionProvider.getSessionManager();
                Session session = sm.getCurrent();
                if (session.isSuspended()) {
                    DebuggingContext dc = ContextProvider.getContext(session);
                    if (dc.getThread() != null) {
                        VirtualMachine vm = session.getConnection().getVM();
                        return vm.canPopFrames() && vm.canBeModified();
                    }
                }
            }
            return false;
        }

        /**
         * Get a help context for the action.
         *
         * @return  help context.
         */
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        /**
         * Get a human presentable name of the action.
         */
        public String getName() {
            return NbBundle.getMessage(PopFramesAction.class,
                    "LBL_StackView_PopFramesAction");
        }

        /**
         * Actually perform the action.
         *
         * @param  activatedNodes  activated nodes.
         */
        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1) {
                Node n = activatedNodes[0];
                if (n instanceof StackFrameNode) {
                    StackFrameNode fn = (StackFrameNode) n;
                    int index = fn.getFrameIndex();
                    SessionManager sm = SessionProvider.getSessionManager();
                    Session session = sm.getCurrent();
                    DebuggingContext dc = ContextProvider.getContext(session);
                    ThreadReference thread = dc.getThread();
                    try {
                        StackFrame frame = thread.frame(index);
                        thread.popFrames(frame);
                        // Cause the context to be reset.
                        dc.setThread(thread, false);
                    } catch (NativeMethodException nme) {
                        NotifyDescriptor desc = new NotifyDescriptor.Message(
                                NbBundle.getMessage(PopFramesAction.class,
                                "ERR_StackView_PoppingNativeMethod"),
                                NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    } catch (IncompatibleThreadStateException itse) {
                        // view should have been cleared already
                        ErrorManager.getDefault().notify(itse);
                    }
                }
            }
        }
    }

    /**
     * Implements the action of setting the current frame.
     *
     * @author  Nathan Fiedler
     */
    public static class SetCurrentFrameAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

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
         * Test whether the action should be enabled based on the currently
         * activated nodes.
         *
         * @param  activatedNodes  target nodes.
         * @return  true if enabled, false otherwise.
         */
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1) {
                SessionManager sm = SessionProvider.getSessionManager();
                Session session = sm.getCurrent();
                if (session.isSuspended()) {
                    DebuggingContext dc = ContextProvider.getContext(session);
                    return dc.getThread() != null;
                }
            }
            return false;
        }

        /**
         * Get a help context for the action.
         *
         * @return  help context.
         */
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        /**
         * Get a human presentable name of the action.
         */
        public String getName() {
            return NbBundle.getMessage(SetCurrentFrameAction.class,
                    "LBL_StackView_SetCurrentFrameAction");
        }

        /**
         * Actually perform the action.
         *
         * @param  activatedNodes  activated nodes.
         */
        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1) {
                Node n = activatedNodes[0];
                if (n instanceof StackFrameNode) {
                    StackFrameNode fn = (StackFrameNode) n;
                    int frame = fn.getFrameIndex();
                    SessionManager sm = SessionProvider.getSessionManager();
                    Session session = sm.getCurrent();
                    DebuggingContext dc = ContextProvider.getContext(session);
                    try {
                        dc.setFrame(frame);
                    } catch (IncompatibleThreadStateException itse) {
                        // eek, view should have been cleared already
                        ErrorManager.getDefault().notify(itse);
                    }
                }
            }
        }
    }

    /**
     * Implements the action of setting a breakpoint at the location
     * for the selected frame.
     *
     * @author  Nathan Fiedler
     */
    public static class SetBreakpointAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

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
         * Test whether the action should be enabled based on the currently
         * activated nodes.
         *
         * @param  activatedNodes  target nodes.
         * @return  true if enabled, false otherwise.
         */
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1) {
                SessionManager sm = SessionProvider.getSessionManager();
                Session session = sm.getCurrent();
                if (session.isSuspended()) {
                    DebuggingContext dc = ContextProvider.getContext(session);
                    return dc.getThread() != null;
                }
            }
            return false;
        }

        /**
         * Get a help context for the action.
         *
         * @return  help context.
         */
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        /**
         * Get a human presentable name of the action.
         */
        public String getName() {
            return NbBundle.getMessage(SetBreakpointAction.class,
                    "LBL_StackView_SetBreakpointAction");
        }

        /**
         * Actually perform the action.
         *
         * @param  activatedNodes  activated nodes.
         */
        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1) {
                Node n = activatedNodes[0];
                if (n instanceof StackFrameNode) {
                    StackFrameNode fn = (StackFrameNode) n;
                    SessionManager sm = SessionProvider.getSessionManager();
                    Session session = sm.getCurrent();
                    BreakpointManager bm =
                            BreakpointProvider.getBreakpointManager(session);
                    BreakpointFactory bf =
                            BreakpointProvider.getBreakpointFactory();
                    DebuggingContext dc = ContextProvider.getContext(session);
                    ThreadReference tr = dc.getThread();
                    int index = fn.getFrameIndex();
                    try {
                        StackFrame frame = tr.frame(index);
                        Location location = frame.location();
                        Breakpoint bp = bf.createLocationBreakpoint(location);
                        bm.addBreakpoint(bp);
                    } catch (IncompatibleThreadStateException itse) {
                        ErrorManager.getDefault().notify(itse);
                    } catch (IndexOutOfBoundsException ioobe) {
                        ErrorManager.getDefault().notify(ioobe);
                    }
                }
            }
        }
    }
}
