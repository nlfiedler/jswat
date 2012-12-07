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
 * are Copyright (C) 2005-2012. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
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
import com.bluemarsh.jswat.nodes.MessageNode;
import com.bluemarsh.jswat.nodes.variables.VariableFactory;
import com.bluemarsh.jswat.nodes.variables.VariableNode;
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
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing our nodes. */
    private PersistentOutlineView nodeView;
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
        nodeView = new PersistentOutlineView();
        nodeView.getOutline().setRootVisible(false);
        columns = new Node.Property[] {
            new Column(VariableNode.PROP_NAME, true, true, false),
            new Column(VariableNode.PROP_TYPE, false, true, false),
            new Column(VariableNode.PROP_VALUE, false, true, false),
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
                VariablesView.class, "CTL_VariablesView_Column_Name_" +
                VariableNode.PROP_NAME));
        rootNode.setShortDescription(NbBundle.getMessage(
                VariablesView.class, "CTL_VariablesView_Column_Desc_" +
                VariableNode.PROP_NAME));
        explorerManager.setRootContext(rootNode);
    }

    /**
     * Builds the node tree for the current session.
     */
    private void buildTree() {
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        List<Node> list = new LinkedList<Node>();
//        Node rootNode = explorerManager.getRootContext();
//        final List<String[]> expanded = getExpanded(nodeView, rootNode);
        if (session.isConnected()) {
            DebuggingContext dc = ContextProvider.getContext(session);
            ThreadReference thread = dc.getThread();
            if (thread != null) {
                try {
                    StackFrame frame = dc.getStackFrame();
                    if (frame.location().codeIndex() == -1) {
                        throw new NativeMethodException("work around JPDA bug");
                    }
                    ReferenceType clazz = frame.location().declaringType();
                    ObjectReference thisObj = frame.thisObject();

                    // Build the set of visible variables, starting with fields.
                    Set<VariableNode> variables = new HashSet<VariableNode>();
                    VariableFactory vf = VariableFactory.getDefault();
                    if (thisObj != null) {
                        // This takes in all of the fields, static and instance.
                        VariableNode thisNode = vf.create(thisObj, null);
                        variables.add(thisNode);
                    } else {
                        // Must be in a static method, so show the static fields.
                        List<Field> fields = clazz.visibleFields();
                        for (Field field : fields) {
                            if (field.isStatic()) {
                                Value value = clazz.getValue(field);
                                VariableNode vn = vf.create(field, value, null);
                                variables.add(vn);
                            }
                        }
                    }

                    // Now collect the visible local variables.
                    try {
                        List<LocalVariable> locals = frame.visibleVariables();
                        for (LocalVariable local : locals) {
                            Value value = frame.getValue(local);
                            VariableNode vn = vf.create(local, value, null);
                            variables.add(vn);
                        }
                    } catch (AbsentInformationException aie) {
                        // Catch this here so we at least capture the available
                        // fields of this class, despite missing local variables.
                        list.add(new MessageNode(NbBundle.getMessage(
                                VariablesView.class, "EXC_AbsentInformation"),
                                NbBundle.getMessage(VariablesView.class,
                                "TIP_AbsentInformation")));
                        // Get the argument values, even without their names.
                        List<Value> arguments = frame.getArgumentValues();
                        int count = 1;
                        String prefix = NbBundle.getMessage(VariablesView.class,
                                "LBL_VariablesView_Argument");
                        for (Value arg : arguments) {
                            VariableNode vn = vf.create(prefix + count, arg);
                            variables.add(vn);
                            count++;
                        }
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
        // TODO: get node expansion working
//        EventQueue.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                // Need to refetch the root in case it was replaced.
//                Node rootNode = explorerManager.getRootContext();
//                expandPaths(expanded, nodeView, rootNode);
//            }
//        });
    }

    @Override
    public void changedFrame(ContextEvent ce) {
        if (!ce.isSuspending() && isCurrent(ce.getSession())) {
            buildTree();
        }
    }

    @Override
    public void changedLocation(ContextEvent ce) {
        if (!ce.isSuspending() && isCurrent(ce.getSession())) {
            buildTree();
        }
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
        return NbBundle.getMessage(VariablesView.class, "CTL_VariablesView_Name");
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-variables-view");
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage(VariablesView.class, "CTL_VariablesView_Tooltip");
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    protected String preferredID() {
        return getClass().getName();
    }

    @Override
    public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        restoreColumns(in, columns);
        nodeView.setProperties(columns);
        nodeView.restoreColumnWidths(in);
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

    @Override
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
        @SuppressWarnings("unchecked")
        public Column(String key, boolean tree, boolean sortable, boolean hidden) {
            super(key, String.class,
                    NbBundle.getMessage(Column.class, "CTL_VariablesView_Column_Name_" + key),
                    NbBundle.getMessage(Column.class, "CTL_VariablesView_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
            setValue("ComparableColumnTTV", Boolean.valueOf(sortable));
            setValue("InvisibleInTreeTableView", Boolean.valueOf(hidden));
        }

        @Override
        public Object getValue() throws IllegalAccessException,
                InvocationTargetException {
            return key;
        }
    }
}
