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
 * are Copyright (C) 2007. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: RuntimeEvent.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.core.runtime;

import java.util.EventObject;

/**
 * An event which indicates that the runtime list has changed.
 *
 * @author  Nathan Fiedler
 */
public class RuntimeEvent extends EventObject {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The runtime that changed. */
    private transient JavaRuntime runtime;
    /** The type of runtime change. */
    private Type type;

    /**
     * Type of runtime event.
     */
    public static enum Type {
        /** Runtime was added (to the RuntimeManager). */
        ADDED {
            public void fireEvent(RuntimeEvent e, RuntimeListener l) {
                l.runtimeAdded(e);
            }
        },
        /** Runtime was removed (from the RuntimeManager). */
        REMOVED {
            public void fireEvent(RuntimeEvent e, RuntimeListener l) {
                l.runtimeRemoved(e);
            }
        };

        /**
         * Dispatches the event to the listener.
         *
         * @param  e  event to dispatch.
         * @param  l  listener to receive event.
         */
        public abstract void fireEvent(RuntimeEvent e, RuntimeListener l);
    }

    /**
     * Constructs a new RuntimeEvent.
     *
     * @param  runtime  runtime that changed.
     * @param  type     type of runtime change.
     */
    public RuntimeEvent(JavaRuntime runtime, Type type) {
        super(runtime);
        this.runtime = runtime;
        this.type = type;
    }

    /**
     * Get the runtime that changed.
     *
     * @return  runtime.
     */
    public JavaRuntime getRuntime() {
        return runtime;
    }

    /**
     * Get the runtime event type.
     *
     * @return  runtime event type.
     */
    public Type getType() {
        return type;
    }
}
