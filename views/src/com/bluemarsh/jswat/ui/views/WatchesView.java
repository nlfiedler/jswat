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
 * are Copyright (C) 2005-2008. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: WatchesView.java 30 2008-06-30 01:12:15Z nfiedler $
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.context.ContextEvent;
import com.bluemarsh.jswat.core.context.ContextListener;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.expr.EvaluationException;
import com.bluemarsh.jswat.core.expr.Evaluator;
import com.bluemarsh.jswat.core.expr.MissingContextException;
import com.bluemarsh.jswat.core.expr.UnknownReferenceException;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Arrays;
import com.bluemarsh.jswat.core.watch.ExpressionWatch;
import com.bluemarsh.jswat.core.watch.FixedWatch;
import com.bluemarsh.jswat.core.watch.Watch;
import com.bluemarsh.jswat.core.watch.WatchEvent;
import com.bluemarsh.jswat.core.watch.WatchFactory;
import com.bluemarsh.jswat.core.watch.WatchListener;
import com.bluemarsh.jswat.core.watch.WatchManager;
import com.bluemarsh.jswat.core.watch.WatchProvider;
import com.bluemarsh.jswat.nodes.variables.VariableFactory;
import com.bluemarsh.jswat.nodes.variables.VariableNode;
import com.bluemarsh.jswat.ui.components.NewWatchPanel;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Class WatchesView evaluates a set of expressions and shows their values.
 *
 * @author  Nathan Fiedler
 */
public class WatchesView extends AbstractView
        implements ContextListener, ExplorerManager.Provider, SessionListener,
        SessionManagerListener, WatchListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The singleton instance of this class. */
    private static WatchesView theInstance;
    /** Preferred window system identifier for this window. */
    public static final String PREFERRED_ID = "watches";
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing our nodes. */
    private PersistentTreeTableView nodeView;
    /** Columns for the tree-table view. */
    private transient Node.Property[] columns;

    /**
     * Constructs a new instance of WatchesView. Clients should not construct
     * this class but rather use the findInstance() method to get the single
     * instance from the window system.
     */
    public WatchesView() {
        explorerManager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put("delete", ExplorerUtils.actionDelete(explorerManager, false));
        associateLookup(ExplorerUtils.createLookup(explorerManager, map));
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        keys.put(KeyStroke.getKeyStroke("DELETE"), "delete");

        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the nodes view.
        nodeView = new PersistentTreeTableView();
        nodeView.setRootVisible(false);
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
    void buildRoot(Children kids) {
        // Use a simple root node for which we can set the display name;
        // otherwise the logical root's properties affect the table headers.
        Node rootNode = new AbstractNode(kids) {
            @Override
            public Action[] getActions(boolean b) {
                return new Action[] {
                    SystemAction.get(NewWatchAction.class)
                };
            }
        };
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                WatchesView.class, "CTL_WatchesView_Column_Name_" +
                VariableNode.PROP_NAME));
        rootNode.setShortDescription(NbBundle.getMessage(
                WatchesView.class, "CTL_WatchesView_Column_Desc_" +
                VariableNode.PROP_NAME));
        explorerManager.setRootContext(rootNode);
    }

    /**
     * Build the tree of watch expressions, including their values.
     */
    private void buildTree() {
        Node rootNode = explorerManager.getRootContext();
        final List<String[]> expanded = getExpanded(nodeView, rootNode);
        Session session = SessionProvider.getCurrentSession();
        DebuggingContext dc = ContextProvider.getContext(session);
        ThreadReference thread = dc.getThread();
        int frame = dc.getFrame();

        List<Node> nodes = new ArrayList<Node>();
        // Build out the watches.
        VariableFactory vf = VariableFactory.getDefault();
        WatchManager wm = WatchProvider.getWatchManager(session);
        Iterator<Watch> iter = wm.watchIterator();
        while (iter.hasNext()) {
            Watch w = iter.next();
            if (w instanceof ExpressionWatch) {
                String expr = ((ExpressionWatch) w).getExpression();
                Node node = evaluate(expr, thread, frame);
                node = new WatchNode(node, w);
                nodes.add(node);
            } else if (w instanceof FixedWatch) {
                ObjectReference obj = ((FixedWatch) w).getObjectReference();
                String name = "#" + obj.uniqueID();
                Node node = vf.create(name, obj.type().name(), obj,
                        VariableNode.Kind.LOCAL, null);
                node = new FixedWatchNode(node, w);
                nodes.add(node);
            }
        }
        Node[] array = nodes.toArray(new Node[nodes.size()]);
        Children children = new Children.Array();
        children.add(array);
        buildRoot(children);

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
            WatchManager wm = WatchProvider.getWatchManager(session);
            wm.removeWatchListener(this);
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
            WatchManager wm = WatchProvider.getWatchManager(session);
            wm.addWatchListener(this);
        }
    }

    public void connected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    /**
     * Creates a watch and adds it to the current watch manager.
     *
     * @param  expression  the expression to evaluate.
     * @param  fixed       if true, create a fixed watch.
     * @return  error message, or null if successful.
     */
    private static String createWatch(String expression, boolean fixed) {
        Session session = SessionProvider.getCurrentSession();
        WatchManager wm = WatchProvider.getWatchManager(session);
        WatchFactory wf = WatchProvider.getWatchFactory();
        String msg = null;
        if (fixed) {
            // Evaluate expression and ensure it refers to an object.
            DebuggingContext dc = ContextProvider.getContext(session);
            ThreadReference thread = dc.getThread();
            int frame = dc.getFrame();
            if (thread == null) {
                msg = NbBundle.getMessage(WatchesView.class,
                        "CTL_WatchesView_NoContext");
            } else {
                Evaluator eval = new Evaluator(expression);
                try {
                    Object o = eval.evaluate(thread, frame);
                    if (o instanceof ObjectReference) {
                        wm.addWatch(wf.createFixedWatch((ObjectReference) o));
                    } else {
                        msg = NbBundle.getMessage(WatchesView.class,
                                "CTL_WatchesView_NotObject");
                    }
                } catch (EvaluationException ee) {
                    msg = NbBundle.getMessage(WatchesView.class,
                            "ERR_Evaluation_error", ee.toString());
                } catch (Exception e) {
                    msg = NbBundle.getMessage(WatchesView.class,
                            "ERR_Evaluation_error", e.toString());
                }
            }
        } else {
            wm.addWatch(wf.createExpressionWatch(expression));
        }
        return msg;
    }

    public void disconnected(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    /**
     * Evaluates the given expression, converting it to a Node as created
     * by the VariableFactory class.
     *
     * @param  expr    expression to evaluate.
     * @param  thread  debuggee thread on which to perform evaluation.
     * @param  frame   frame in thread in which to access variables.
     * @return  node representing the evaluation.
     */
    private Node evaluate(String expr, ThreadReference thread, int frame) {
        String msg = null;
        Value result = null;
        String resultSimple = null;

        String orgexpr = expr;
        int comma = expr.indexOf(",");
        String modifiers = comma >= 0 ? expr.substring(comma + 1).trim() : null;
        expr = comma > 0 ? expr.substring(0, comma) : (comma == 0 ? "" : expr);

        if (expr.length() == 0) {
            msg = "";
        } else {
            Evaluator eval = new Evaluator(expr);
            try {
                Object o = eval.evaluate(thread, frame);
                if (o instanceof Value) {
                    // From the debuggee, build out the object tree.
                    result = (Value) o;
                } else {
                    // Not from the debuggee, just convert to a string.
                    resultSimple = o == null ? "null" : o.toString();
                }
            } catch (MissingContextException mce) {
                msg = NbBundle.getMessage(WatchesView.class,
                        "CTL_WatchesView_NoContext");
            } catch (UnknownReferenceException ure) {
                msg = NbBundle.getMessage(WatchesView.class,
                        "CTL_WatchesView_NotResolved");
            } catch (EvaluationException ee) {
                msg = NbBundle.getMessage(WatchesView.class,
                        "ERR_Evaluation_error", ee.toString());
            } catch (Exception e) {
                msg = NbBundle.getMessage(WatchesView.class,
                        "ERR_Evaluation_error", e.toString());
            }
        }

        Node node = null;
        if (resultSimple != null) {
            node = new ExpressionNode(orgexpr, resultSimple);
        } else if (msg != null) {
            node = new ExpressionNode(orgexpr, msg, true);
        } else if (result != null) {
            if (result instanceof VoidValue) {
                node = new ExpressionNode(orgexpr, "void");
            } else {
                VariableFactory vf = VariableFactory.getDefault();
                if (modifiers != null && modifiers.length() == 0) {
                    modifiers = null;
                }
                node = vf.create(orgexpr, result.type().name(), result,
                        VariableNode.Kind.LOCAL, modifiers);
            }
        }
        return node;
    }

    /**
     * Obtain the window instance, first by looking for it in the window
     * system, then if not found, creating the instance.
     *
     * @return  the window instance.
     */
    public static synchronized WatchesView findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(
                PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Cannot find '" + PREFERRED_ID +
                    "' component in the window system");
            return getDefault();
        }
        if (win instanceof WatchesView) {
            return (WatchesView) win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "There seem to be multiple components with the '" +
                PREFERRED_ID + "' ID, this a potential source of errors");
        return getDefault();
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     * Clients should not call this method, but instead use findInstance().
     *
     * @return  instance of this class.
     */
    public static synchronized WatchesView getDefault() {
        if (theInstance == null) {
            theInstance = new WatchesView();
        }
        return theInstance;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(WatchesView.class, "CTL_WatchesView_Name");
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-watches-view");
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage(WatchesView.class, "CTL_WatchesView_Tooltip");
    }

    public void opened(Session session) {
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
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
        WatchManager wm = WatchProvider.getWatchManager(session);
        wm.addWatchListener(this);
    }

    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        session.removeSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.removeContextListener(this);
        WatchManager wm = WatchProvider.getWatchManager(session);
        wm.removeWatchListener(this);
    }

    public void sessionSetCurrent(SessionManagerEvent e) {
        buildTree();
    }

    public void suspended(SessionEvent sevt) {
        if (isCurrent(sevt)) {
            buildTree();
        }
    }

    public void watchAdded(WatchEvent event) {
        // Likely this is for the current session, so don't bother checking.
        buildTree();
    }

    public void watchRemoved(WatchEvent event) {
        // Likely this is for the current session, so don't bother checking.
        buildTree();
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
                    NbBundle.getMessage(Column.class, "CTL_WatchesView_Column_Name_" + key),
                    NbBundle.getMessage(Column.class, "CTL_WatchesView_Column_Desc_" + key));
            this.key = key;
            setValue("TreeColumnTTV", Boolean.valueOf(tree));
            setValue("ComparableColumnTTV", Boolean.valueOf(sortable));
            setValue("InvisibleInTreeTableView", Boolean.valueOf(hidden));
        }

        public Object getValue() throws IllegalAccessException,
                InvocationTargetException {
            return key;
        }
    }

    /**
     * Class WatchNode wraps a node from the VariableFactory and shows
     * a different icon and provides an action to remove the watch.
     *
     * @author  Nathan Fiedler
     */
    protected class WatchNode extends FilterNode {
        /** The watch for this node. */
        private Watch watch;

        /**
         * Constructs a new instance of WatchNode.
         *
         * @param  original  Node being filtered.
         * @param  watch     the watch.
         */
        public WatchNode(Node original, Watch watch) {
            super(original);
            this.watch = watch;
        }

        @Override
        public boolean canDestroy() {
            return true;
        }

        @Override
        public void destroy() throws IOException {
            Session session = SessionProvider.getCurrentSession();
            WatchManager wm = WatchProvider.getWatchManager(session);
            wm.removeWatch(watch);
            super.destroy();
        }

        @Override
        public Action[] getActions(boolean context) {
            Action[] actions = new Action[] {
                SystemAction.get(NewFixedWatchAction.class),
                SystemAction.get(DeleteAction.class)
            };
            return (Action[]) Arrays.join(super.getActions(context), actions);
        }

        @Override
        public Image getIcon(int type) {
            Node original = getOriginal();
            String url = null;
            if (original instanceof ExpressionNode) {
                ExpressionNode node = (ExpressionNode) original;
                if (node.isMessageNode()) {
                    url = NbBundle.getMessage(WatchNode.class,
                            "IMG_MessageNode");
                }
            }
            if (url == null) {
                url = NbBundle.getMessage(WatchNode.class,
                        "IMG_WatchesView_WatchNode");
            }
            return ImageUtilities.loadImage(url);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        /**
         * Returns the watch this node represents.
         *
         * @return  watch.
         */
        public Watch getWatch() {
            return watch;
        }
    }

    /**
     * Class FixedWatchNode wraps a node from the VariableFactory and shows
     * a different icon and provides an action to remove the fixed watch.
     *
     * @author  Nathan Fiedler
     */
    protected class FixedWatchNode extends FilterNode {
        /** The watch for this node. */
        private Watch watch;

        /**
         * Constructs a new instance of WatchNode.
         *
         * @param  original  Node being filtered.
         * @param  watch     the watch.
         */
        public FixedWatchNode(Node original, Watch watch) {
            super(original);
            this.watch = watch;
        }

        @Override
        public boolean canDestroy() {
            return true;
        }

        @Override
        public void destroy() throws IOException {
            Session session = SessionProvider.getCurrentSession();
            WatchManager wm = WatchProvider.getWatchManager(session);
            wm.removeWatch(watch);
            super.destroy();
        }

        @Override
        public Action[] getActions(boolean context) {
            Action[] actions = new Action[] {
                SystemAction.get(DeleteAction.class)
            };
            return (Action[]) Arrays.join(super.getActions(context), actions);
        }

        @Override
        public Image getIcon(int type) {
            Node original = getOriginal();
            String url = null;
            if (original instanceof ExpressionNode) {
                ExpressionNode node = (ExpressionNode) original;
                if (node.isMessageNode()) {
                    url = NbBundle.getMessage(WatchNode.class,
                            "IMG_MessageNode");
                }
            }
            if (url == null) {
                url = NbBundle.getMessage(FixedWatchNode.class,
                        "IMG_WatchesView_FixedWatchNode");
            }
            return ImageUtilities.loadImage(url);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }
    }

    /**
     * Class ExpressionNode represents the evaluated expression results.
     *
     * @author  Nathan Fiedler
     */
    protected class ExpressionNode extends VariableNode {
        /** The value of the expression. */
        private String exprValue;
        /** If true, node represents a message to the user. */
        private boolean isMessage;

        /**
         * Constructs a new instance of ExpressionNode.
         *
         * @param  expr   evaluated expression.
         * @param  value  the value of the expression.
         */
        public ExpressionNode(String expr, String value) {
            super(Children.LEAF, expr, "", VariableNode.Kind.FIELD);
            exprValue = value;
        }

        /**
         * Constructs a new instance of ExpressionNode.
         *
         * @param  expr     evaluated expression.
         * @param  value    the value of the expression.
         * @param  message  true if this node is used for a message.
         */
        public ExpressionNode(String expr, String value, boolean message) {
            this(expr, value);
            isMessage = message;
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Sheet.Set set = sheet.get(Sheet.PROPERTIES);
            set.put(createProperty(PROP_VALUE, exprValue));
            return sheet;
        }

        /**
         * Indicates if this node is a message node, rather than one that
         * displays an evaluated expression.
         *
         * @return  true if merely a message, false if evaluated result.
         */
        public boolean isMessageNode() {
            return isMessage;
        }
    }

    /**
     * Implements the action of adding a watch expression.
     *
     * @author  Nathan Fiedler
     */
    public static class NewWatchAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean asynchronous() {
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(NewWatchAction.class,
                    "LBL_WatchesView_NewWatchAction");
        }

        protected void performAction(Node[] activatedNodes) {
            String title = NbBundle.getMessage(NewWatchAction.class,
                    "CTL_WatchesView_NewWatchTitle");
            NewWatchPanel panel = new NewWatchPanel();
            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                    panel, title, NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE);
            Object ans = DialogDisplayer.getDefault().notify(desc);
            if (ans == NotifyDescriptor.OK_OPTION) {
                String expr = panel.getExpression();
                if (expr.length() > 0) {
                    String msg = createWatch(expr, panel.isFixed());
                    if (msg != null) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                msg, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                }
            }
        }
    }

    /**
     * Implements the action of creating a fixed watch from an
     * expression watch.
     *
     * @author  Nathan Fiedler
     */
    public static class NewFixedWatchAction extends NodeAction {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean asynchronous() {
            return false;
        }

        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1 &&
                    activatedNodes[0] instanceof WatchNode) {
                return true;
            }
            return false;
        }

        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        public String getName() {
            return NbBundle.getMessage(NewFixedWatchAction.class,
                    "LBL_WatchesView_NewFixedWatchAction");
        }

        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes != null && activatedNodes.length == 1 &&
                    activatedNodes[0] instanceof WatchNode) {
                WatchNode node = (WatchNode) activatedNodes[0];
                Watch watch = node.getWatch();
                if (watch instanceof ExpressionWatch) {
                    ExpressionWatch ew = (ExpressionWatch) watch;
                    String expr = ew.getExpression();
                    String msg = createWatch(expr, true);
                    if (msg != null) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(
                                msg, NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                }
            }
        }
    }
}
