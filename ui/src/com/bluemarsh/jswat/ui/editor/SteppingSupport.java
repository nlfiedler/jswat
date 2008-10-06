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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SteppingSupport.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.ui.editor;

import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.sun.jdi.Location;
import java.util.HashMap;
import java.util.Map;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

/**
 * Controls the editor annotations for the program counter for each Session.
 *
 * @author Nathan Fiedler
 */
public class SteppingSupport {
    /** The singleton instance of this class. */
    private static SteppingSupport theInstance;
    /** Map of program counter annotations, keyed by Session instance. */
    private Map<Session, DebugAnnotation> pcAnnotations;

    /**
     * Creates a new instance of SteppingSupport.
     */
    private SteppingSupport() {
        pcAnnotations = new HashMap<Session, DebugAnnotation>();
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return  instance of this class.
     */
    public static synchronized SteppingSupport getDefault() {
        if (theInstance == null) {
            theInstance = new SteppingSupport();
        }
        return theInstance;
    }

    /**
     * Removes the program counter annotation for the given session, if
     * one exists.
     *
     * @param  session  Session for which to clear the program counter.
     */
    public void hideProgramCounter(Session session) {
        EditorSupport es = EditorSupport.getDefault();
        DebugAnnotation ann = pcAnnotations.remove(session);
        if (ann != null) {
            es.removeAnnotation(ann);
        }
    }

    /**
     * Adds the program counter annotation to the source file corresponding
     * to the current program counter. If there is no current location, or
     * the source file for that location cannot be found, then nothing will
     * happen.
     *
     * @param  session     Session for which to show program counter.
     * @param  showSource  true to open source and scroll to line.
     */
    public void showProgramCounter(Session session, boolean showSource) {
        DebuggingContext dc = ContextProvider.getContext(session);
        Location loc = dc.getLocation();
        if (loc != null) {
            // There is a location, see if we can get the source file.
            PathManager pm = PathProvider.getPathManager(session);
            FileObject fobj = pm.findSource(loc);
            if (fobj != null) {
                // There is a source file, get the URL.
                String url = null;
                try {
                    url = fobj.getURL().toString();
                } catch (FileStateInvalidException fsie) {
                    // Highly unlikely, but log it anyway.
                    ErrorManager.getDefault().log(
                            ErrorManager.WARNING, fsie.toString());
                    return;
                }
                EditorSupport es = EditorSupport.getDefault();
                // Remove the existing program counter annotation.
                DebugAnnotation ann = pcAnnotations.remove(session);
                if (ann != null) {
                    es.removeAnnotation(ann);
                }
                // Try to show current location in the editor.
                int line = loc.lineNumber();
                if (showSource) {
                    es.showSource(url, line);
                }
                // Annotate the program counter.
                ann = es.annotate(url, line, DebugAnnotation.CURRENT_PC_TYPE);
                pcAnnotations.put(session, ann);
            }
        }
    }
}
