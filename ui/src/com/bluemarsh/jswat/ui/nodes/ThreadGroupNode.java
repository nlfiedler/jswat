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
 * $Id: ThreadGroupNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import java.awt.Image;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents a thread group in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class ThreadGroupNode extends BaseNode {
    /** Name of the name property. */
    private static final String PROP_NAME = "name";
    /** Name of the status property. */
    private static final String PROP_STATUS = "status";
    /** Name of the identifer property. */
    private static final String PROP_ID = "id";
    /** Name of the class property. */
    private static final String PROP_CLASS = "class";
    /** The thread group reference we represent. */
    private ThreadGroupReference threadGroup;
    /** The name of the thread group. */
    private String groupName;
    /** Debugging context we are associated with. */
    private DebuggingContext debugContext;

    /**
     * Constructs a GroupNode to represent the given thread group.
     *
     * @param  kids   children of this group.
     * @param  dc     debugging context.
     * @param  group  the thread group.
     */
    public ThreadGroupNode(Children kids, DebuggingContext dc, ThreadGroupReference group) {
        super(kids);
        threadGroup = group;
        groupName = group.name();
        debugContext = dc;
    }

    /**
     * Creates a node property of the given key (same as the column keys).
     *
     * @param  key    property name (same as matching column).
     * @param  type   type of the property (e.g. String.class, Integer.class).
     * @param  value  display value.
     * @return  new property.
     */
    private Node.Property createProperty(String key, Class type, Object value) {
        String desc = NbBundle.getMessage(
                ThreadGroupNode.class, "CTL_ThreadsView_Column_Desc_" + key);
        String name = NbBundle.getMessage(
                ThreadGroupNode.class, "CTL_ThreadsView_Column_Name_" + key);
        return new ReadOnlyProperty(key, type, name, desc, value);
    }

    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_NAME, String.class, groupName));
        // Thread groups have no status.
        set.put(createProperty(PROP_STATUS, String.class, ""));
        set.put(createProperty(PROP_ID, Long.class, Long.valueOf(
                threadGroup.uniqueID())));
        set.put(createProperty(PROP_CLASS, String.class, threadGroup.type().name()));
        return sheet;
    }

    public String getDisplayName() {
        return groupName;
    }

    /**
     * Returns the ThreadGroupReference this node represents.
     *
     * @return  thread group.
     */
    public ThreadGroupReference getGroup() {
        return threadGroup;
    }

    public String getHtmlDisplayName() {
        if (isCurrent()) {
            return Nodes.toHTML(groupName, true, false, null);
        }  else {
            return null;
        }
    }

    public Image getIcon(int type) {
        // Are we related to the current thread?
        String url;
        if (isCurrent()) {
            url = NbBundle.getMessage(ThreadGroupNode.class,
                    "IMG_CurrentThreadGroupNode");
        }  else {
            url = NbBundle.getMessage(ThreadGroupNode.class,
                    "IMG_ThreadGroupNode");
        }
        return Utilities.loadImage(url);
    }

    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    /**
     * Determines if this group contains the current thread.
     *
     * @return  true if current, false otherwise.
     */
    private boolean isCurrent() {
        ThreadReference thread = debugContext.getThread();
        if (thread != null) {
            ThreadGroupReference parent = thread.threadGroup();
            while (parent != null) {
                if (parent.equals(threadGroup)) {
                    return true;
                }
                parent = parent.parent();
            }
        }
        return false;
    }
}
