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
 * $Id: DefaultThreadNode.java 29 2008-06-30 00:41:09Z nfiedler $
 */

package com.bluemarsh.jswat.nodes.threads;

import com.bluemarsh.jswat.core.context.ContextEvent;
import com.bluemarsh.jswat.core.context.ContextListener;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.nodes.Nodes;
import com.bluemarsh.jswat.nodes.ReadOnlyProperty;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import java.awt.Image;
import javax.swing.Action;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 * Represents a thread in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class DefaultThreadNode extends ThreadNode
        implements ContextListener, ThreadConstants {
    /** The thread reference we represent. */
    private ThreadReference threadReference;
    /** The name of the thread. */
    private String threadName;
    /** Debugging context we are associated with. */
    private DebuggingContext debugContext;

    /**
     * Constructs a ThreadNode to represent the given ThreadReference.
     *
     * @param  thread   thread to be represented.
     * @param  context  debugging context.
     */
    public DefaultThreadNode(ThreadReference thread, DebuggingContext context) {
        threadReference = thread;
        threadName = thread.name();
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
                ThreadNode.class, "CTL_ThreadProperty_Desc_" + key);
        String name = NbBundle.getMessage(
                ThreadNode.class, "CTL_ThreadProperty_Name_" + key);
        return new ReadOnlyProperty(key, type, name, desc, value);
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(Node.PROP_NAME, String.class, threadName));
        int status;
        try {
            status = threadReference.status();
        } catch (VMDisconnectedException vmde) {
            status = ThreadReference.THREAD_STATUS_ZOMBIE;
        }
        String statusName = null;
        switch(status) {
            case ThreadReference.THREAD_STATUS_MONITOR:
                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadStatus_Monitor");
                break;
            case ThreadReference.THREAD_STATUS_NOT_STARTED:
                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadStatus_NotStarted");
                break;
            case ThreadReference.THREAD_STATUS_RUNNING:
                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadStatus_Running");
                break;
            case ThreadReference.THREAD_STATUS_SLEEPING:
                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadStatus_Sleeping");
                break;
            case ThreadReference.THREAD_STATUS_WAIT:
                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadStatus_Waiting");
                break;
            case ThreadReference.THREAD_STATUS_ZOMBIE:
                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadStatus_Zombie");
                break;
            default:
                // This covers the UNKNOWN status as well.
                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadStatus_Unknown");
                break;
        }
        set.put(createProperty(PROP_STATUS, String.class, statusName));
        set.put(createProperty(PROP_ID, Long.class, Long.valueOf(
                threadReference.uniqueID())));
        set.put(createProperty(PROP_CLASS, String.class, threadReference.type().name()));
        return sheet;
    }

    @Override
    public String getDisplayName() {
        return threadName;
    }

    @Override
    public String getHtmlDisplayName() {
        if (threadReference.equals(debugContext.getThread())) {
            return Nodes.toHTML(threadName, true, false, null);
        } else {
            return null;
        }
    }

    @Override
    public Image getIcon(int type) {
        String url;
        if (threadReference.equals(debugContext.getThread())) {
            url = NbBundle.getMessage(ThreadNode.class,
                    "IMG_CurrentThreadNode");
        } else if (threadReference.isSuspended()) {
            url = NbBundle.getMessage(ThreadNode.class,
                    "IMG_SuspendedThreadNode");
        } else {
            url = NbBundle.getMessage(ThreadNode.class,
                    "IMG_RunningThreadNode");
        }
        return Utilities.loadImage(url);
    }

    protected Action[] getNodeActions() {
        return new Action[] {
            SystemAction.get(SetCurrentAction.class),
            SystemAction.get(ResumeAction.class),
            SystemAction.get(SuspendAction.class),
            SystemAction.get(InterruptAction.class),
            SystemAction.get(TraceAction.class),
        };
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(SetCurrentAction.class);
    }

    public ThreadReference getThread() {
        return threadReference;
    }
}
