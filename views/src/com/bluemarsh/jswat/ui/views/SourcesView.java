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
 * $Id$
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.components.PathEditorPanel;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Class SourcesView displays the source path entries for the current Session.
 *
 * @author  Nathan Fiedler
 */
public class SourcesView extends AbstractView
        implements ExplorerManager.Provider, PropertyChangeListener,
        SessionManagerListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The singleton instance of this class. */
    private static SourcesView theInstance;
    /** Preferred window system identifier for this window. */
    public static final String PREFERRED_ID = "sources";
    /** Our explorer manager. */
    private ExplorerManager explorerManager;
    /** Component showing node tree. */
    private TreeView nodeView;

    /**
     * Constructs a new instance of SourcesView. Clients should not construct
     * this class but rather use the findInstance() method to get the single
     * instance from the window system.
     */
    public SourcesView() {
        explorerManager = new ExplorerManager();
        buildRoot(Children.LEAF);
        addSelectionListener(explorerManager);

        // Create the view.
        nodeView = new BeanTreeView();
        nodeView.setRootVisible(false);
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
        Node rootNode = new AbstractNode(kids) {
            @Override
            public Action[] getActions(boolean b) {
                return new Action[] {
                    SystemAction.get(EditAction.class),
                    SystemAction.get(RefreshAction.class),
                };
            }
        };
        explorerManager.setRootContext(rootNode);
    }

    /**
     * Builds the node tree for the current session.
     */
    private void buildTree() {
        // Populate the root node with children.
        List<Node> list = new LinkedList<Node>();
        Node rootNode = explorerManager.getRootContext();
        final List<String[]> expanded = getExpanded(nodeView, rootNode);
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();

        PathManager pm = PathProvider.getPathManager(session);
        List<FileObject> roots = pm.getSourcePath();
        if (roots != null) {
            for (FileObject root : roots) {
                Node node = new SourceRootNode(root);
                list.add(node);
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
            PathManager pm = PathProvider.getPathManager(session);
            pm.removePropertyChangeListener(this);
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
            PathManager pm = PathProvider.getPathManager(session);
            pm.addPropertyChangeListener(this);
        }
    }

    /**
     * Obtain the window instance, first by looking for it in the window
     * system, then if not found, creating the instance.
     *
     * @return  the window instance.
     */
    public static synchronized SourcesView findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(
                PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Cannot find '" + PREFERRED_ID +
                    "' component in the window system");
            return getDefault();
        }
        if (win instanceof SourcesView) {
            return (SourcesView) win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING,
                "There seem to be multiple components with the '" +
                PREFERRED_ID + "' ID, this a potential source of errors");
        return getDefault();
    }

    /**
     * Scans the directory structure starting at root, looking for folders
     * that are either empty or contain files, adding them to the set.
     *
     * @param  children  set to which nodes are added.
     * @param  fo        file object to examine.
     * @param  root      root of the package hierarchy.
     * @param  query     true to query for visibility of files.
     */
    private static void findVisiblePackages(Set<Node> children,
            FileObject fo, FileObject root, boolean query) {

        VisibilityQuery vq = VisibilityQuery.getDefault();
        if (query && !vq.isVisible(fo)) {
            return;
        }

        FileObject[] kids = fo.getChildren();
        boolean hasSubfolders = false;
        boolean hasFiles = false;
        for (int ii = 0; ii < kids.length; ii++) {            
            if (!query || vq.isVisible(kids[ii])) {
                if (kids[ii].isFolder()) {
                    findVisiblePackages(children, kids[ii], root, query);
                    hasSubfolders = true;
                } else {
                    hasFiles = true;
                }
            }
        }
        if (hasFiles || !hasSubfolders) {
            DataFolder df = DataFolder.findFolder(fo);
            PackageNode pn = new PackageNode(root, df);
            children.add(pn);
        }
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     * Clients should not call this method, but instead use findInstance().
     *
     * @return  instance of this class.
     */
    public static synchronized SourcesView getDefault() {
        if (theInstance == null) {
            theInstance = new SourcesView();
        }
        return theInstance;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SourcesView.class, "CTL_SourcesView_Name");
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-sources-view");
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage(SourcesView.class, "CTL_SourcesView_Tooltip");
    }

    /**
     * Check whether a package is empty (devoid of files except for subpackages).
     *
     * @param  fo  file object to check.
     */
    private static boolean isEmpty(FileObject fo) {
        return isEmpty(fo, true);
    }

    /**
     * Check whether a package is empty (devoid of files except for subpackages).
     *
     * @param  fo       file object to check.
     * @param  recurse  specifies whether to check if subpackages are empty too.
     */
    private static boolean isEmpty(FileObject fo, boolean recurse) {
        if (fo != null) {
            FileObject[] kids = fo.getChildren();
            for (int ii = 0; ii < kids.length; ii++) {
                if (!kids[ii].isFolder() &&
                        VisibilityQuery.getDefault().isVisible(kids[ii])) {
                    return false;
                } else if (recurse && !isEmpty(kids[ii])) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // See if the current session's sourcepath has changed.
        Session session = SessionProvider.getCurrentSession();
        PathManager pm = PathProvider.getPathManager(session);
        Object src = evt.getSource();
        String prop = evt.getPropertyName();
        if (src.equals(pm) && prop.equals(PathManager.PROP_SOURCEPATH)) {
            buildTree();
        }
    }

    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        PathManager pm = PathProvider.getPathManager(session);
        pm.addPropertyChangeListener(this);
    }

    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        PathManager pm = PathProvider.getPathManager(session);
        pm.removePropertyChangeListener(this);
    }

    public void sessionSetCurrent(SessionManagerEvent e) {
        buildTree();
    }

    /**
     * Represents a source root in the node tree.
     *
     * @author  Nathan Fiedler
     */
    private static class SourceRootNode extends AbstractNode {
        /** Root of this subtree. */
        private FileObject sourceRoot;
        /** The display name for this node. */
        private String displayName;

        /**
         * Constructs a new instance of SourceRootNode.
         *
         * @param  root  a sourcepath entry.
         */
        public SourceRootNode(FileObject root) {
            super(new SRNChildren(root));
            setIconBaseWithExtension(
                    "com/bluemarsh/jswat/views/resources/PackageRoot.gif");
            sourceRoot = root;
            displayName = FileUtil.getFileDisplayName(sourceRoot);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Children of the source root node.
     *
     * @author  Nathan Fiedler
     */
    private static class SRNChildren extends Children.SortedArray {
        /** Root of this subtree. */
        private FileObject sourceRoot;

        /**
         * Constructs a new instance of SRNChildren.
         *
         * @param  root  a sourcepath entry.
         */
        public SRNChildren(FileObject root) {
            sourceRoot = root;
        }

        @Override
        protected void addNotify() {
            super.addNotify();

            Set<Node> children = new TreeSet<Node>();
            FileObject[] kids = sourceRoot.getChildren();
            boolean archive = FileUtil.isArchiveFile(sourceRoot);
            VisibilityQuery vq = VisibilityQuery.getDefault();
            for (int ii = 0; ii < kids.length; ii++) {            
                if (archive || vq.isVisible(kids[ii])) {
                    if (kids[ii].isFolder()) {
                        findVisiblePackages(children, kids[ii],
                                sourceRoot, !archive);
                    } else {
                        try {
                            DataObject data = DataObject.find(kids[ii]);
                            // For sorting, wrap a filter around the node.
                            Node node = new SortableNode(data.getNodeDelegate());
                            children.add(node);
                        } catch (DataObjectNotFoundException donfe) {
                            // in that case, ignore the file
                        }
                    }
                }
            }

            // Add the children to our own set (which should be empty).
            Node[] kidsArray = children.toArray(new Node[children.size()]);
            super.add(kidsArray);
        }
    }

    /**
     * Represents a package in the node tree.
     *
     * @author  Nathan Fiedler
     */
    private static class PackageNode extends FilterNode
            implements Comparable<Node> {
        /** Filter for hiding child folders, which are already shown. */
        private static final DataFilter FILTER = new NoFoldersDataFilter();
        /** Root of this subtree. */
        private FileObject sourcePkg;
        /** The display name for this node. */
        private String displayName;

        /**
         * Constructs a new instance of PackageNode.
         *
         * @param  root    root of the package.
         * @param  folder  a package within root.
         */
        public PackageNode(FileObject root, DataFolder folder) {
            // DataFolder children are loaded on demand.
            super(folder.getNodeDelegate(), isEmpty(folder) ? Children.LEAF :
                    folder.createNodeChildren(FILTER));
            sourcePkg = folder.getPrimaryFile();
            String path = FileUtil.getRelativePath(root, sourcePkg);
            displayName = path.replace('/', '.');
        }

        public int compareTo(Node o) {
            if (!(o instanceof PackageNode)) {
                return -1;
            } else {
                String on = o.getDisplayName();
                String tn = getDisplayName();
                return tn.compareTo(on);
            }
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Image getIcon(int type) {
            String url;
            if (isLeaf()) {
                url = NbBundle.getMessage(SourcesView.class,
                        "IMG_SourcesView_PackageEmptyNode");
            } else {
                url = NbBundle.getMessage(SourcesView.class,
                        "IMG_SourcesView_PackageNode");
            }
            return ImageUtilities.loadImage(url);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        /**
         * Determines if the given folder is empty or not.
         *
         * @param  folder  data folder to check.
         * @return  true if folder is empty, false otherwise.
         */
        private static boolean isEmpty(DataFolder folder) {
            if (folder == null) {
                return true;
            }
            return SourcesView.isEmpty(folder.getPrimaryFile());
        }
    }

    /**
     * Makes any node into a sortable node.
     */
    private static class SortableNode extends FilterNode
            implements Comparable<Node> {

        /**
         * Constructs a new instance of SortableNode.
         */
        public SortableNode(Node original) {
            super(original);
        }

        public int compareTo(Node o) {
            String on = o.getDisplayName();
            String tn = getDisplayName();
            return tn.compareTo(on);
        }
    }

    /**
     * Copied from PackageViewChildren in NetBeans IDE.
     */
    private static class NoFoldersDataFilter
            implements ChangeListener, ChangeableDataFilter {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        EventListenerList ell = new EventListenerList();

        public NoFoldersDataFilter() {
            VisibilityQuery.getDefault().addChangeListener(this);
        }

        public boolean acceptDataObject(DataObject obj) {
            if (obj instanceof DataFolder) {
                return false;
            }
            FileObject fo = obj.getPrimaryFile();
            return VisibilityQuery.getDefault().isVisible(fo);
        }

        public void stateChanged(ChangeEvent e) {
            Object[] listeners = ell.getListenerList();
            ChangeEvent event = null;
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ChangeListener.class) {
                    if (event == null) {
                        event = new ChangeEvent(this);
                    }
                    ((ChangeListener) listeners[i + 1]).stateChanged(event);
                }
            }
        }

        public void addChangeListener(ChangeListener listener) {
            ell.add(ChangeListener.class, listener);
        }

        public void removeChangeListener(ChangeListener listener) {
            ell.remove(ChangeListener.class, listener);
        }
    }

    /**
     * Implements the action of displaying the source path editor.
     *
     * @author  Nathan Fiedler
     */
    public static class EditAction extends NodeAction {
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
            return NbBundle.getMessage(SourcesView.class,
                    "LBL_SourcesView_EditAction");
        }

        protected void performAction(Node[] activatedNodes) {
            PathEditorPanel editor = new PathEditorPanel();
            Session session = SessionProvider.getCurrentSession();
            PathManager pm = PathProvider.getPathManager(session);
            // Load the current sourcepath into the editor.
            List<FileObject> sourcepath = pm.getSourcePath();
            if (sourcepath != null && sourcepath.size() > 0) {
                List<String> srcpath = new LinkedList<String>();
                for (FileObject fo : sourcepath) {
                    String path = FileUtil.getFileDisplayName(fo);
                    srcpath.add(path);
                }
                editor.setPath(srcpath);
            }

            // Display the editor in a simple dialog.
            String title = NbBundle.getMessage(SourcesView.class,
                    "LBL_SourcesView_EditTitle");
            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                    editor, title, NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE);
            Object result = DialogDisplayer.getDefault().notify(desc);
            if (result != NotifyDescriptor.OK_OPTION) {
                // User cancelled the editor.
                return;
            }

            // Save the new sourcepath setting.
            List<String> paths = editor.getPath();
            List<FileObject> roots = new LinkedList<FileObject>();
            for (String path : paths) {
                File file = new File(path);
                // It is possible for the user to add paths that don't exist.
                if (file.exists() && file.canRead()) {
                    file = FileUtil.normalizeFile(file);
                    FileObject fo = FileUtil.toFileObject(file);
                    roots.add(fo);
                }
            }
            pm.setSourcePath(roots);
        }
    }

    /**
     * Implements the action of refreshing the node tree.
     *
     * @author  Nathan Fiedler
     */
    public static class RefreshAction extends NodeAction {
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
            return NbBundle.getMessage(RefreshAction.class,
                    "LBL_RefreshAction_Name");
        }

        protected void performAction(Node[] activatedNodes) {
            SourcesView view = SourcesView.findInstance();
            view.buildTree();
        }
    }
}
