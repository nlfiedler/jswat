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
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.editor;

import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 * Represents a debugger annotation in an editor view.
 *
 * @author  Nathan Fiedler
 */
public class DebugAnnotation extends Annotation {
    /** Type of annotation representing an enabled breakpoint. */
    public static final String BREAKPOINT_TYPE = "Breakpoint";
    /** Type of annotation representing the current program counter. */
    public static final String CURRENT_PC_TYPE = "CurrentPC";
    /** Type of annotation representing a disabled breakpoint. */
    public static final String DISABLED_BREAKPOINT_TYPE = "DisabledBreakpoint";
    /** Line to which we are attached. */
    private Line line;
    /** The type of annotation we represent (.xml filename). */
    private String type;
    /** An optional object associated with this annotation (e.g. breakpoint). */
    private Object userObject;

    /**
     * Constructs a new instance of DebugAnnotation.
     *
     * @param  type  the type of annotation.
     * @param  line  the line to annotate.
     */
    public DebugAnnotation(String type, Line line) {
        this.type = type;
        this.line = line;
    }
    
    /**
     * Constructs a new instance of DebugAnnotation.
     *
     * @param  type  the type of annotation.
     * @param  line  the line to annotate.
     * @param  obj   an object to associate with this annotation.
     */
    public DebugAnnotation(String type, Line line, Object obj) {
        this(type, line);
        userObject = obj;
    }

    public String getAnnotationType() {
        return type;
    }

    /**
     * Returns the line number of this annotation.
     *
     * @return  line number.
     */
    public Line getLine() {
        return line;
    }

    public String getShortDescription () {
        if (type.equals(BREAKPOINT_TYPE)) {
            return NbBundle.getMessage(getClass(), "HINT_BREAKPOINT");
        } else if (type.equals(CURRENT_PC_TYPE)) {
            return NbBundle.getMessage(getClass(), "HINT_CURRENT_PC");
        } else if (type.equals(DISABLED_BREAKPOINT_TYPE)) {
            return NbBundle.getMessage(getClass(), "HINT_DISABLED_BREAKPOINT");
        } else {
            return "";
        }
    }

    /**
     * Returns the user object associated with this annotation, if any.
     *
     * @return  user object, possibly null.
     */
    public Object getUserObject() {
        return userObject;
    }

    public String toString() {
        return "DebugAnnotation=[Type=" + type + ",line=" + line +
                ",userObject=" + userObject + "]";
    }
}
