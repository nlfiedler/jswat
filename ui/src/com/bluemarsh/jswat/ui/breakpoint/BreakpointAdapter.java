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
 * $Id: BreakpointAdapter.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.ui.breakpoint;

import com.bluemarsh.jswat.core.breakpoint.Breakpoint;
import com.bluemarsh.jswat.core.breakpoint.BreakpointFactory;
import java.beans.PropertyChangeListener;

/**
 * A BreakpointAdapter defines the methods necessary for a breakpoint
 * editor implementation. Concrete implementations may not receive a
 * Breakpoint to be edited, when creating a new breakpoint. That is, the
 * loadParameters() method may or may not be called, but the editor is
 * still expected to be able to save the field values to a new breakpoint
 * when the saveParameters() method is invoked.
 *
 * <p>When the loadParameters() method is called, the adapter is required
 * to apply changes to that breakpoint immediately. That is, as the user
 * makes changes via the input fields, the breakpoint should be updated.
 * In this situation, the saveParameters() method will not be called.</p>
 *
 * @author  Nathan Fiedler
 */
public interface BreakpointAdapter {
    /** The valid input property name. */
    public static final String PROP_INPUTVALID = "validInput";

    /**
     * Add the listener to be notified of property changes.
     *
     * @param  listener  property change listener.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Indicates if this adapter is the sort that can construct a new
     * Breakpoint instance from the user-provided information.
     *
     * @return  true if breakpoint creation is possible, false otherwise.
     */
    boolean canCreateBreakpoint();

    /**
     * Create a Breakpoint instance that encapsulates the information
     * provided by the user. This may not be entirely complete since
     * some of the information is contained in other adapters. The caller
     * is responsible for invoking <code>saveParameters(Breakpoint)</code>
     * on the other adapters to make the Breakpoint instance complete.
     *
     * @param  factory  breakpoint factory to construct breakpoint.
     * @return  new Breakpoint, or null if creation not supported.
     */
    Breakpoint createBreakpoint(BreakpointFactory factory);

    /**
     * Read the values from the given Breakpoint to populate the fields
     * of this editor. Begin persisting the changes to the breakpoint as
     * soon as the user edits the input fields. If the input is invalid,
     * fire an "validInput" property change with the error message as the
     * property value.
     *
     * @param  bp  Breakpoint to edit.
     */
    void loadParameters(Breakpoint bp);

    /**
     * Remove the previously registered listener.
     *
     * @param  listener  property change listener.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Saves the values from the fields of this editor to a new Breakpoint.
     *
     * @param  bp  newly created Breakpoint.
     */
    void saveParameters(Breakpoint bp);

    /**
     * Validate the user-provided input.
     *
     * @return  error message if input invalid, null if valid.
     */
    String validateInput();
}
