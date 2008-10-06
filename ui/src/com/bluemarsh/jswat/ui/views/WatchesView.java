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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: WatchesView.java 15 2007-06-03 00:01:17Z nfiedler $
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
import com.bluemarsh.jswat.ui.components.NewWatchPanel;
import com.bluemarsh.jswat.ui.nodes.BaseNode;
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
import javax.swing.JScrollPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
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
    /** Name of the expression property. */
    private static final String PROP_EXPR = "expr";
    /** Name of the type property. */
    private static final String PROP_TYPE = "type";
    /** Name of the value property. */
    private static final String PROP_VALUE = "value";
    /** The singleton instance of this class. */
    private static WatchesView theInstance;
    /** Preferred window system identifier for this window. */
    public static final String PREFERRED_ID = "watches";
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing our nodes. */
    private PersistentTreeTableView nodeView;
    /** Array of actions for the root node. */
    private Action[] rootActions;
    /** Array of actions for the watch nodes. */
    private Action[] nodeActions;
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

        rootActions = new Action[] {
            SystemAction.get(NewWatchAction.class)
        };
        nodeActions = new Action[] {
            SystemAction.get(NewFixedWatchAction.class),
            SystemAction.get(DeleteAction.class)
        };
        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the nodes view.
        nodeView = new PersistentTreeTableView();
        nodeView.setRootVisible(false);
        columns = new Node.Property[] {
            new Column(PROP_EXPR, true, true, false),
            new Column(PROP_TYPE, false, true, false),
            new Column(PROP_VALUE, false, true, false),
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
        BaseNode rootNode = new BaseNode(kids);
        // Surprisingly, this becomes the name and description of the first column.
        rootNode.setDisplayName(NbBundle.getMessage(
                WatchesView.class, "CTL_WatchesView_Column_Name_" + PROP_EXPR));
        rootNode.setShortDescription(NbBundle.getMessage(
                WatchesView.class, "CTL_WatchesView_Column_Desc_" + PROP_EXPR));
        rootNode.setActions(rootActions);
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
        VariableFactory vf = VariableFactory.getInstance();
        WatchManager wm = WatchProvider.getWatchManager(session);
        Iterator<Watch> iter = wm.watchIterator();
        while (iter.hasNext()) {
            Watch w = iter.next();
            if (w instanceof ExpressionWatch) {
                String expr = ((ExpressionWatch) w).getExpression();
                Node node = evaluate(expr, thread, frame);
                if (node != null) {
                    node = new WatchNode(node, w);
                    nodes.add(node);
                }
            } else if (w instanceof FixedWatch) {
                ObjectReference obj = ((FixedWatch) w).getObjectReference();
                String name = "#" + obj.uniqueID();
                Node node = vf.create(name, obj.type().name(), obj,
                        VariableNode.Kind.LOCAL);
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
            WatchManager wm = WatchProvider.getWatchManager(session);
            wm.removeWatchListener(this);
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
     * @return  node representing the evaluation, or null if error.
     */
    private Node evaluate(String expr, ThreadReference thread, int frame) {
        String msg = null;
        Value result = null;

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
                    msg = o == null ? "null" : o.toString();
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
        if (msg != null) {
            node = new ExpressionNode(orgexpr, msg);
        } else if (result != null) {
            if (result instanceof VoidValue) {
                node = new ExpressionNode(orgexpr, "void");
            } else {
                VariableFactory vf = VariableFactory.getInstance();
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

    /**
     * Returns the display name for this component.
     *
     * @return  display name.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(WatchesView.class, "CTL_WatchesView_Name");
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
        return new HelpCtx("jswat-watches-view");
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
        return NbBundle.getMessage(WatchesView.class, "CTL_WatchesView_Tooltip");
    }

    public void opened(Session session) {
    }

    /**
     * Returns the unique identifier for this component.
     *
     * @return  unique component identifier.
     */
    protected String preferredID() {
        return PREFERRED_ID;
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
        WatchManager wm = WatchProvider.getWatchManager(session);
        wm.addWatchListener(this);
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
        WatchManager wm = WatchProvider.getWatchManager(session);
        wm.removeWatchListener(this);
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

    public void watchAdded(WatchEvent event) {
        // Likely this is for the current session, so don't bother checking.
        buildTree();
    }

    public void watchRemoved(WatchEvent event) {
        // Likely this is for the current session, so don't bother checking.
        buildTree();
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
                    NbBundle.getMessage(Column.class, "CTL_WatchesView_Column_Name_" + key),
                    NbBundle.getMessage(Column.class, "CTL_WatchesView_Column_Desc_" + key));
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

        public boolean canDestroy() {
            return true;
        }

        public void destroy() throws IOException {
            Session session = SessionProvider.getCurrentSession();
            WatchManager wm = WatchProvider.getWatchManager(session);
            wm.removeWatch(watch);
            super.destroy();
        }

        /**
         * Get the set of actions that are associated with this node.
         *
         * @param  context  whether to find actions for context meaning or for
         *                  the node itself.
         * @return  a list of actions.
         */
        public Action[] getActions(boolean context) {
            Action[] retValue = super.getActions(context);
            retValue = (Action[]) Arrays.join(retValue, nodeActions);
            return retValue;
        }

        /**
         * Find an icon for this node (in the closed state).
         *
         * @param  type  constant from BeanInfo
         * @return  icon to use to represent the node.
         */
        public Image getIcon(int type) {
            String url = NbBundle.getMessage(WatchNode.class,
                    "IMG_WatchesView_WatchNode");
            return Utilities.loadImage(url);
        }

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

        public boolean canDestroy() {
            return true;
        }

        public void destroy() throws IOException {
            Session session = SessionProvider.getCurrentSession();
            WatchManager wm = WatchProvider.getWatchManager(session);
            wm.removeWatch(watch);
            super.destroy();
        }

        /**
         * Get the set of actions that are associated with this node.
         *
         * @param  context  whether to find actions for context meaning or for
         *                  the node itself.
         * @return  a list of actions.
         */
        public Action[] getActions(boolean context) {
            Action[] retValue = super.getActions(context);
            retValue = (Action[]) Arrays.join(retValue, nodeActions);
            return retValue;
        }

        /**
         * Find an icon for this node (in the closed state).
         *
         * @param  type  constant from BeanInfo
         * @return  icon to use to represent the node.
         */
        public Image getIcon(int type) {
            String url = NbBundle.getMessage(FixedWatchNode.class,
                    "IMG_WatchesView_FixedWatchNode");
            return Utilities.loadImage(url);
        }

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
         * Create the properties for this node.
         *
         * @return  property sheet.
         */
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            Sheet.Set set = sheet.get(Sheet.PROPERTIES);
            set.put(createProperty(PROP_VALUE, exprValue));
            return sheet;
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

        /**
         * Indicates if this action can be invoked on any thread.
         *
         * @return  true if asynchronous, false otherwise.
         */
        protected boolean asynchronous() {
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
            return true;
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
            return NbBundle.getMessage(NewWatchAction.class,
                    "LBL_WatchesView_NewWatchAction");
        }

        /**
         * Actually perform the action.
         *
         * @param  activatedNodes  activated nodes.
         */
        protected void performAction(Node[] activatedNodes) {
            String title = NbBundle.getMessage(NewWatchAction.class,
                    "CTL_WatchesView_NewWatchTitle");
            NewWatchPanel panel = new NewWatchPanel();
            DialogDescriptor desc = new DialogDescriptor(panel, title);
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
