/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the JSwat Command module. The Initial Developer of the
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: CommandView.java 15 2007-06-03 00:01:17Z nfiedler $
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
