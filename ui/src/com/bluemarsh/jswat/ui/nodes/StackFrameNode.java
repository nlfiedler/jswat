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
 * $Id: StackFrameNode.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.nodes;

import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.core.util.Names;
import com.bluemarsh.jswat.ui.editor.EditorSupport;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import java.awt.Image;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents a strack frame in the node tree.
 *
 * @author  Nathan Fiedler
 */
public class StackFrameNode extends BaseNode {
    /** Name of the location property. */
    public static final String PROP_LOCATION = "location";
    /** Name of the source property. */
    public static final String PROP_SOURCE = "source";
    /** Name of the code index property. */
    public static final String PROP_CODEINDEX = "codeIndex";
    /** The stack frame index for the frame we represent (zero-based). */
    private int frameIndex;
    /** The class and method of the frame location. */
    private String methodName;
    /** The location of the stack frame (source and line). */
    private String locationName;
    /** The byte code index of the frame. */
    private long codeIndex;

    /**
     * Constructs a FrameNode to represent the given StackFrame.
     *
     * @param  index  index of the stack frame (zero-based).
     * @param  frame  the stack frame.
     */
    public StackFrameNode(int index, StackFrame frame) {
        super();
        frameIndex = index;
        Location loc = frame.location();
        codeIndex = loc.codeIndex();
        Method method = loc.method();
        String cname = method.declaringType().name();
        cname = Names.getShortClassName(cname);
        int line = loc.lineNumber();
        if (line >= 0) {
            methodName = cname + '.' + method.name() + ':' + line;
        } else {
            methodName = cname + '.' + method.name();
        }

        if (method.isNative()) {
            locationName = NbBundle.getMessage(
                    StackFrameNode.class, "LBL_StackView_frame_native");
        }  else {
            try {
                locationName = loc.sourceName();
            } catch (AbsentInformationException aie) {
                locationName = NbBundle.getMessage(
                        StackFrameNode.class, "ERR_StackView_frame_no_info");
            }
        }

        getCookieSet().add(new SourceCookie());
    }

    /**
     * Creates a node property of the given key (same as the column keys).
     *
     * @param  key    property name (same as matching column).
     * @param  value  display value.
     * @return  new property.
     */
    private Node.Property createProperty(String key, String value) {
        String name = NbBundle.getMessage(
                StackFrameNode.class, "CTL_StackView_Column_Name_" + key);
        String desc = NbBundle.getMessage(
                StackFrameNode.class, "CTL_StackView_Column_Desc_" + key);
        return new ReadOnlyProperty(key, String.class, name, desc, value);
    }

    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set set = sheet.get(Sheet.PROPERTIES);
        set.put(createProperty(PROP_LOCATION, methodName));
        set.put(createProperty(PROP_SOURCE, locationName));
        set.put(createProperty(PROP_CODEINDEX, String.valueOf(codeIndex)));
        return sheet;
    }

    public String getDisplayName() {
        return methodName;
    }

    public String getHtmlDisplayName() {
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        DebuggingContext dc = ContextProvider.getContext(session);
        if (frameIndex == dc.getFrame()) {
            return Nodes.toHTML(methodName, true, false, null);
        } else {
            return null;
        }
    }

    public Image getIcon(int type) {
        SessionManager sm = SessionProvider.getSessionManager();
        Session session = sm.getCurrent();
        DebuggingContext dc = ContextProvider.getContext(session);
        String url;
        if (frameIndex == dc.getFrame()) {
            url = NbBundle.getMessage(StackFrameNode.class, "IMG_CurrentStackFrameNode");
        }  else {
            url = NbBundle.getMessage(StackFrameNode.class, "IMG_StackFrameNode");
        }
        return Utilities.loadImage(url);
    }

    /**
     * Returns the index of the frame this node represents.
     *
     * @return  frame index for this node.
     */
    public int getFrameIndex() {
        return frameIndex;
    }

    /**
     * Displays the source code for the corresponding stack frame.
     */
    private class SourceCookie implements ShowSourceCookie {

        public boolean canShowSource() {
            SessionManager sm = SessionProvider.getSessionManager();
            Session session = sm.getCurrent();
            if (session.isSuspended()) {
                DebuggingContext dc = ContextProvider.getContext(session);
                return dc.getThread() != null;
            }
            return false;
        }

        public void showSource() {
            SessionManager sm = SessionProvider.getSessionManager();
            Session session = sm.getCurrent();
            DebuggingContext dc = ContextProvider.getContext(session);
            ThreadReference tr = dc.getThread();
            int index = StackFrameNode.this.getFrameIndex();
            try {
                StackFrame frame = tr.frame(index);
                Location location = frame.location();
                PathManager pm = PathProvider.getPathManager(session);
                FileObject fobj = pm.findSource(location);
                if (fobj != null) {
                    String url = fobj.getURL().toString();
                    EditorSupport es = EditorSupport.getDefault();
                    int line = location.lineNumber();
                    es.showSource(url, line);
                } else {
                    ReferenceType rt = location.declaringType();
                    String msg = NbBundle.getMessage(SourceCookie.class,
                            "ERR_SourceMissing", rt.name());
                    NotifyDescriptor desc = new NotifyDescriptor.Message(
                            msg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }
            } catch (FileStateInvalidException fsie) {
                ErrorManager.getDefault().notify(fsie);
            } catch (IncompatibleThreadStateException itse) {
                ErrorManager.getDefault().notify(itse);
            } catch (IndexOutOfBoundsException ioobe) {
                ErrorManager.getDefault().notify(ioobe);
            }
        }
    }
}
