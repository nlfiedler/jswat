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
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: CommandView.java 6 2007-05-16 07:14:24Z nfiedler $
 */

package com.bluemarsh.jswat.command.view;

import java.awt.BorderLayout;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Class CommandView shows the command input field and the output area.
 *
 * @author  Nathan Fiedler
 */
public class CommandView extends TopComponent {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Panel which provides the interface for command execution. */
    private CommandPanel commandPanel;

    /**
     * Creates a new instance of EvaluatorView.
     */
    public CommandView() {
        super();
        setLayout(new BorderLayout());
    }

    /**
     * Called only when top component was closed so that now it is closed
     * on all workspaces in the system.
     */
    protected void componentClosed() {
        super.componentClosed();
        if (commandPanel != null) {
            commandPanel.closing();
            remove(commandPanel);
            commandPanel = null;
        }
    }

    /**
     * Called only when top component was closed on all workspaces before
     * and now is opened for the first time on some workspace.
     */
    protected void componentOpened() {
        super.componentOpened();
        if (commandPanel == null) {
            commandPanel = new CommandPanel();
            add(commandPanel, BorderLayout.CENTER);
        }
    }

    /**
     * Returns the display name for this component.
     *
     * @return  display name.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "CTL_CommandView_Name");
    }

    /**
     * Returns the desired persistent type for this component.
     *
     * @return  desired persistence type.
     */
    public int getPersistenceType() {
        return PERSISTENCE_ONLY_OPENED;
    }

    /**
     * Returns the display name for this component.
     *
     * @return  tooltip text.
     */
    public String getToolTipText() {
        return NbBundle.getMessage(getClass(), "CTL_CommandView_Tooltip");
    }

    /**
     * Returns the unique identifier for this component.
     *
     * @return  unique component identifier.
     */
    protected String preferredID() {
        return getClass().getName();
    }
}
