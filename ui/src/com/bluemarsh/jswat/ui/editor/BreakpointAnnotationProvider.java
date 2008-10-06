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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: BreakpointAnnotationProvider.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.editor;

import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointGroup;
import com.bluemarsh.jswat.core.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.core.breakpoint.BreakpointProvider;
import com.bluemarsh.jswat.core.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.sun.jdi.Location;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.text.Annotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.util.Lookup;

/**
 * An annotation provider that supplies the annotations relating to
 * breakpoints in a newly opened file.
 *
 * @author Nathan Fiedler
 */
public class BreakpointAnnotationProvider implements AnnotationProvider {

    /**
     * Creates a new instance of BreakpointAnnotationProvider.
     */
    public BreakpointAnnotationProvider() {
    }

    /**
     * Attach annotations to the Line.Set for given context.
     *
     * @param  set     the Line.Set to attach annotations to.
     * @param  lookup  a Lookup describing the context for the Line.Set.
     *                 It shall contain the FileObject the LineSet is
     *                 associated with.
     */
    public void annotate(Line.Set set, Lookup lookup) {
        FileObject fo = (FileObject) lookup.lookup(FileObject.class);
        if (fo != null) {
            EditorSupport es = EditorSupport.getDefault();
            // Check if the program counter annotation belongs in this file.
            // Session must be suspended and current location must match the
            // file that was just opened.
            SessionManager sm = SessionProvider.getSessionManager();
            Session session = sm.getCurrent();
            DebuggingContext dc = ContextProvider.getContext(session);
            Location loc = dc.getLocation();
            if (loc != null) {
                // There is a location, see if we can get the source file.
                PathManager pm = PathProvider.getPathManager(session);
                FileObject fobj = pm.findSource(loc);
                if (fobj != null && fo.equals(fobj)) {
                    // It is indeed this file, show the annotation.
                    SteppingSupport.getDefault().showProgramCounter(session, true);
                }
            }

            try {
                String url = fo.getURL().toString();
                // Scan for breakpoints in this file and add the annotations,
                // but only for the current session, to avoid confusion.
                BreakpointManager bm = BreakpointProvider.getBreakpointManager(session);
                BreakpointGroup defaultGroup = bm.getDefaultGroup();
                Iterator<Breakpoint> breakpoints = defaultGroup.breakpoints(true);
                List<Breakpoint> deleteList = null;
                while (breakpoints.hasNext()) {
                    Breakpoint bp = breakpoints.next();
                    if (bp instanceof LineBreakpoint) {
                        LineBreakpoint lb = (LineBreakpoint) bp;
                        if (lb.getURL().equals(url)) {
                            Annotation ann;
                            if (lb.isEnabled()) {
                                ann = es.annotate(
                                        url, lb.getLineNumber(),
                                        DebugAnnotation.BREAKPOINT_TYPE, lb);
                            } else {
                                ann = es.annotate(
                                        url, lb.getLineNumber(),
                                        DebugAnnotation.DISABLED_BREAKPOINT_TYPE, lb);
                            }
                            // Check if annotation is null, which indicates the
                            // source line no longer exists (file shrank).
                            if (ann == null) {
                                // In which case we delete the orphaned breakpoint.
                                if (deleteList == null) {
                                    deleteList = new LinkedList<Breakpoint>();
                                }
                                deleteList.add(lb);
                            } else {
                                lb.setProperty(EditorConstants.PROP_ANNOTATION, ann);
                            }
                        }
                    }
                }
                if (deleteList != null) {
                    // There are breakpoints to be deleted.
                    for (Breakpoint bp : deleteList) {
                        bm.removeBreakpoint(bp);
                    }
                }
            } catch (FileStateInvalidException fsie) {
                ErrorManager.getDefault().notify(fsie);
            }
        }
    }
}
