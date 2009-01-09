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

package com.bluemarsh.jswat.nodes.threads;

import com.bluemarsh.jswat.core.context.ContextEvent;
import com.bluemarsh.jswat.core.context.ContextListener;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.nodes.Nodes;
import com.bluemarsh.jswat.nodes.ReadOnlyProperty;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import java.awt.Image;
import javax.swing.Action;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 * Represents a thread group in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class DefaultThreadGroupNode extends ThreadGroupNode
        implements ContextListener, ThreadConstants {
    /** The thread group reference we represent. */
    private ThreadGroupReference threadGroup;
    /** The name of the thread group. */
    private String groupName;
    /** Debugging context we are associated with. */
    private DebuggingContext debugContext;

    /**
     * Constructs a GroupNode to represent the given thread group.
     *
     * @param  group     the thread group.
     * @param  children  children of this group.
     * @param  context   debugging context.
     */
    public DefaultThreadGroupNode(ThreadGroupReference group, Children children,
            DebuggingContext context) {
        super(children);
        threadGroup = group;
        groupName = group.name();
        debugContext = context;
        context.addContextListener(WeakListeners.create(
                ContextListener.class, this, context));
    }

    public void changedFrame(ContextEvent ce) {
    }

    public void changedLocation(ContextEvent ce) {
    }

    public void changedThread(ContextEvent ce) {
        if (!ce.isSuspending() && ce.isCurrentSession()) {
            // Update our visual representation to reflect thread status.
            fireDisplayNameChange(null, null);
            fireIconChange();
        }
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
                ThreadGroupNode.class, "CTL_ThreadProperty_Desc_" + key);
        String name = NbBundle.getMessage(
                ThreadGroupNode.class, "CTL_ThreadProperty_Name_" + key);
        return new ReadOnlyProperty(key, type, name, desc, value);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(Node.PROP_NAME, String.class, groupName));
        // Thread groups have no status.
        set.put(createProperty(PROP_STATUS, String.class, ""));
        set.put(createProperty(PROP_ID, Long.class, Long.valueOf(
                threadGroup.uniqueID())));
        set.put(createProperty(PROP_CLASS, String.class, threadGroup.type().name()));
        return sheet;
    }

    @Override
    public String getDisplayName() {
        return groupName;
    }

    public ThreadGroupReference getThreadGroup() {
        return threadGroup;
    }

    @Override
    public String getHtmlDisplayName() {
        if (isCurrent()) {
            return Nodes.toHTML(groupName, true, false, null);
        } else {
            return null;
        }
    }

    @Override
    public Image getIcon(int type) {
        // Are we related to the current thread?
        String url;
        if (isCurrent()) {
            url = NbBundle.getMessage(ThreadGroupNode.class,
                    "IMG_CurrentThreadGroupNode");
        } else {
            url = NbBundle.getMessage(ThreadGroupNode.class,
                    "IMG_ThreadGroupNode");
        }
        return ImageUtilities.loadImage(url);
    }

    protected Action[] getNodeActions() {
        return new Action[] {
            SystemAction.get(ResumeAction.class),
            SystemAction.get(SuspendAction.class),
        };
    }

    @Override
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
