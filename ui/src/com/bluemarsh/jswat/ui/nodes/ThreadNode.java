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
 * $Id: ThreadNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import java.awt.Image;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents a thread in the debuggee.
 *
 * @author  Nathan Fiedler
 */
public class ThreadNode extends BaseNode {
    /** Name of the name property. */
    private static final String PROP_NAME = "name";
    /** Name of the status property. */
    private static final String PROP_STATUS = "status";
    /** Name of the identifer property. */
    private static final String PROP_ID = "id";
    /** Name of the class property. */
    private static final String PROP_CLASS = "class";
    /** The thread reference we represent. */
    private ThreadReference threadReference;
    /** The name of the thread. */
    private String threadName;
    /** Debugging context we are associated with. */
    private DebuggingContext debugContext;

    /**
     * Constructs a ThreadNode to represent the given ThreadReference.
     *
     * @param  dc      debugging context.
     * @param  thread  thread to be represented.
     */
    public ThreadNode(DebuggingContext dc, ThreadReference thread) {
        super();
        threadReference = thread;
        threadName = thread.name();
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
        set.put(createProperty(PROP_NAME, String.class, threadName));
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
                        "CTL_ThreadsView_Status_Monitor");

                break;
            case ThreadReference.THREAD_STATUS_NOT_STARTED:

                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadsView_Status_NotStarted");

                break;
            case ThreadReference.THREAD_STATUS_RUNNING:

                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadsView_Status_Running");

                break;
            case ThreadReference.THREAD_STATUS_SLEEPING:

                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadsView_Status_Sleeping");

                break;
            case ThreadReference.THREAD_STATUS_WAIT:

                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadsView_Status_Waiting");

                break;
            case ThreadReference.THREAD_STATUS_ZOMBIE:

                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadsView_Status_Zombie");

                break;
            default:

                // This covers the UNKNOWN status as well.
                statusName = NbBundle.getMessage(ThreadNode.class,
                        "CTL_ThreadsView_Status_Unknown");

                break;
        }
        set.put(createProperty(PROP_STATUS, String.class, statusName));
        set.put(createProperty(PROP_ID, Long.class, Long.valueOf(
                threadReference.uniqueID())));
        set.put(createProperty(PROP_CLASS, String.class, threadReference.type().name()));
        return sheet;
    }

    public String getDisplayName() {
        return threadName;
    }

    public String getHtmlDisplayName() {
        if (threadReference.equals(debugContext.getThread())) {
            return Nodes.toHTML(threadName, true, false, null);
        }  else {
            return null;
        }
    }

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

    /**
     * Returns the ThreadReference this node represents.
     *
     * @return  thread.
     */
    public ThreadReference getThread() {
        return threadReference;
    }
}
