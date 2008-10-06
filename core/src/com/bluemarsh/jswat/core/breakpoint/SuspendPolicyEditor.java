/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Core module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: SuspendPolicyEditor.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.core.breakpoint;

import com.sun.jdi.request.EventRequest;
import java.beans.PropertyEditorSupport;
import org.openide.util.NbBundle;

/**
 * Property editor for the suspend policy property.
 *
 * @author Nathan Fiedler
 */
public class SuspendPolicyEditor extends PropertyEditorSupport {

    /**
     * Creates a new instance of SuspendPolicyEditor.
     */
    public SuspendPolicyEditor() {
    }

    public String getAsText() {
        Object value = getValue();
        if (value instanceof Integer) {
            int i = ((Integer) value).intValue();
            if (i == EventRequest.SUSPEND_ALL) {
                return NbBundle.getMessage(SuspendPolicyEditor.class,
                        "SuspendPolicy.all");
            } else if (i == EventRequest.SUSPEND_EVENT_THREAD) {
                return NbBundle.getMessage(SuspendPolicyEditor.class,
                        "SuspendPolicy.event");
            } else if (i == EventRequest.SUSPEND_NONE) {
                return NbBundle.getMessage(SuspendPolicyEditor.class,
                        "SuspendPolicy.none");
            }
        }
        return "";
    }

    public String[] getTags() {
        String[] retValue = {
            NbBundle.getMessage(SuspendPolicyEditor.class, "SuspendPolicy.all"),
            NbBundle.getMessage(SuspendPolicyEditor.class, "SuspendPolicy.event"),
            NbBundle.getMessage(SuspendPolicyEditor.class, "SuspendPolicy.none"),
        };
        return retValue;
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (text.equals(NbBundle.getMessage(SuspendPolicyEditor.class,
                "SuspendPolicy.all"))) {
            setValue(new Integer(EventRequest.SUSPEND_ALL));
        } else if (text.equals(NbBundle.getMessage(SuspendPolicyEditor.class,
                "SuspendPolicy.event"))) {
            setValue(new Integer(EventRequest.SUSPEND_EVENT_THREAD));
        } else if (text.equals(NbBundle.getMessage(SuspendPolicyEditor.class,
                "SuspendPolicy.none"))) {
            setValue(new Integer(EventRequest.SUSPEND_NONE));
        }
    }
}
