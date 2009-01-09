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
 * are Copyright (C) 2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
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
