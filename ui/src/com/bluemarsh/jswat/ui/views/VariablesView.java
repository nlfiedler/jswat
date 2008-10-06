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
 * $Id: VariablesView.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.context.ContextEvent;
import com.bluemarsh.jswat.core.context.ContextListener;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.nodes.MessageNode;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InconsistentDebugInfoException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JScrollPane;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Class VariablesView displays the class loaders and their defined classes.
 *
 * @author  Nathan Fiedler
 */
public class VariablesView extends AbstractView
        implements ContextListener, ExplorerManager.Provider, SessionListener,
        SessionManagerListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Name of the name property. */
    private static final String PROP_NAME = "name";
    /** Name of the type property. */
    private static final String PROP_TYPE = "type";
    /** Name of the value property. */
    private static final String PROP_VALUE = "value";
//    /** Name of the string property. */
//    protected static final String PROP_STRING = "string";
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing our nodes. */
    private PersistentTreeTableView nodeView;
    /** Columns for the tree-table view. */
    private transient Node.Property[] columns;

    /**
     * Constructs a VariablesView instance.
     */
    public VariablesView() {
        explorerManager = new ExplorerManager();
        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the nodes view.
        nodeView = new PersistentTreeTableView();
        nodeView.setRootVisible(false);
        columns = new Node.Property[] {
            new Column(PROP_NAME, true, true, false),
            new Column(PROP_TYPE, false, true, false),
            new Column(PROP_VALUE, false, true, false),
// Invoking methods in JDI is too error prone to be performed frequently.
//            new Column(PROP_STRING, false, true, true),
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
                VariablesView.class, "CTL_VariablesView_Column_Name_" + PROP_NAME));
        rootNode.setShortDescription(NbBundle.getMessage(
                VariablesView.class, "CTL_VariablesView_Column_Desc_" + PROP_NAME));
        explorerManager.setRootContext(rootNode);
    }

    /**
     * Builds the node tree for the current session.
     */
    private void buildTree() {
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        List<Node> list = new LinkedList<Node>();
        Node rootNode = explorerManager.getRootContext();
        final List<String[]> expanded = getExpanded(nodeView, rootNode);
        if (session.isConnected()) {
            DebuggingContext dc = ContextProvider.getContext(session);
            ThreadReference thread = dc.getThread();
            if (thread != null) {
                try {
                    StackFrame frame = dc.getStackFrame();
                    ReferenceType clazz = frame.location().declaringType();
                    ObjectReference thisObj = frame.thisObject();

                    // Build the set of visible variables, starting with fields.
                    Set<VariableNode> variables = new HashSet<VariableNode>();
                    VariableFactory vf = VariableFactory.getInstance();
                    if (thisObj != null) {
                        // This takes in all of the fields, static and instance.
                        VariableNode thisNode = vf.create(thisObj);
                        variables.add(thisNode);
                    } else {
                        // Must be in a static method, so show the static fields.
                        List<Field> fields = clazz.visibleFields();
                        for (Field field : fields) {
                            if (field.isStatic()) {
                                Value value = clazz.getValue(field);
                                VariableNode vn = vf.create(field, value);
                                variables.add(vn);
                            }
                        }
                    }

                    // Now collect the visible local variables.
                    try {
                        List<LocalVariable> locals = frame.visibleVariables();
                        for (LocalVariable local : locals) {
                            Value value = frame.getValue(local);
                            VariableNode vn = vf.create(local, value);
                            variables.add(vn);
                        }
                    } catch (AbsentInformationException aie) {
                        // Catch this here so we at least capture the available
                        // fields of this class, despite missing local variables.
                        list.add(new MessageNode(NbBundle.getMessage(
                                VariablesView.class, "EXC_AbsentInformation"),
                                NbBundle.getMessage(VariablesView.class,
                                "TIP_AbsentInformation")));
                    }

                    list.addAll(variables);
                } catch (IncompatibleThreadStateException itse) {
                    list.add(new MessageNode(NbBundle.getMessage(VariablesView.class,
                            "EXC_ThreadNotSuspended")));
                } catch (InconsistentDebugInfoException idie) {
                    list.add(new MessageNode(NbBundle.getMessage(VariablesView.class,
                            "EXC_InconsistentInfo")));
                } catch (IndexOutOfBoundsException ioobe) {
                    list.add(new MessageNode(NbBundle.getMessage(VariablesView.class,
                            "EXC_ThreadNotStarted")));
                } catch (InvalidStackFrameException isfe) {
                    list.add(new MessageNode(NbBundle.getMessage(VariablesView.class,
                            "EXC_InvalidStackFrame")));
                } catch (NativeMethodException nme) {
                    list.add(new MessageNode(NbBundle.getMessage(VariablesView.class,
                            "EXC_NativeMethod")));
                } catch (VMDisconnectedException vmde) {
                    // This happens often, nothing we can do.
                }
            } else {
                list.add(new MessageNode(NbBundle.getMessage(VariablesView.class,
                        "EXC_NoCurrentThread")));
            }
        }
        if (list.size() > 0) {
            Children children = new Children.Array();
            Node[] nodes = list.toArray(new Node[list.size()]);
            children.add(nodes);
            buildRoot(children);
        } else {
            buildRoot(Children.LEAF);
        }

        // Must expand the nodes on the AWT event thread.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                // Need to refetch the root in case it was replaced.
                Node rootNode = explorerManager.getRootContext();
                expandPaths(expanded, nodeView, rootNode);
            }
        });
    }

    public void changedFrame(ContextEvent ce) {
        if (!ce.isSuspending() && isCurrent(ce.getSession())) {
            buildTree();
        }
    }

    public void changedLocation(ContextEvent ce) {
        if (!ce.isSuspending() && isCurrent(ce.getSession())) {
            buildTree();
        }
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
        return NbBundle.getMessage(VariablesView.class, "CTL_VariablesView_Name");
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
        return new HelpCtx("jswat-variables-view");
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
        return NbBundle.getMessage(VariablesView.class, "CTL_VariablesView_Tooltip");
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
    private class Column extends PropertySupport.ReadOnly {
        /** The keyword for this column. */
        private String key;

        /**
         * Constructs a new instance of Column.
         *
         * @param  key       keyword for this column.
         * @param  tree      true if this is the 'tree' column, false if 'table' column.
         * @param  sortable  true if this is sortable column, false otherwise.
         * @param  hidden    true to hide this column initially.
         */
        public Column(String key, boolean tree, boolean sortable, boolean hidden) {
            super(key, String.class,
                    NbBundle.getMessage(Column.class, "CTL_VariablesView_Column_Name_" + key),
                    NbBundle.getMessage(Column.class, "CTL_VariablesView_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
            setValue("ComparableColumnTTV", Boolean.valueOf(sortable));
            setValue("InvisibleInTreeTableView", Boolean.valueOf(hidden));
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
}
