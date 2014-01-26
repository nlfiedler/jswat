/*********************************************************************
 *
 *	Copyright (C) 1999-2001 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:	JSwat
 * MODULE:	JSwat Actions
 * FILE:	JSwatAction.java
 *
 * AUTHOR:	Nathan Fiedler
 *
 * REVISION HISTORY:
 *	Name	Date		Description
 *	----	----		-----------
 *	nf	02/22/99	Initial version
 *	nf	10/07/01	Moved methods to SessionFrameMapper
 *
 * DESCRIPTION:
 *	This file defines the abstract class used to define actions
 *	that perform various program operations.
 *
 * $Id: JSwatAction.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * Base action class which all other actions subclass. Provides some
 * utility functions needed by nearly all of the actions in JSwat.
 *
 * @author  Nathan Fiedler
 */
public abstract class JSwatAction extends AbstractAction {
    /** Instance of JSwat. */
    protected static JSwat swat = JSwat.instanceOf();

    /**
     * Creates a new JSwatAction command with the given
     * action command string.
     *
     * @param  name  action command string
     */
    public JSwatAction(String name) {
	super(name);
    } // JSwatAction

    /**
     * Display an error message in a dialog.
     *
     * @param  o    Object with which to find the parent frame.
     *		    Could be a subclass of EventObject or Component.
     * @param  msg  error message to be displayed.
     */
    public static void displayError(Object o, String msg) {
	JOptionPane.showMessageDialog(
	    getFrame(o), msg,
	    swat.getResourceString("Dialog.Error.title"),
	    JOptionPane.ERROR_MESSAGE);
    } // displayError

    /**
     * Find the hosting frame for this object. Often used
     * when displaying dialogs which require a host frame.
     *
     * @param  o  Object with which to find the parent frame.
     *		  Could be a subclass of EventObject or Component.
     * @return	hosting frame or null if none.
     */
    public static Frame getFrame(Object o) {
	// Use the SessionFrameMapper to get the Session for the object.
	return SessionFrameMapper.getOwningFrame(o);
    } // getFrame

    /**
     * Finds the Session that is associated with the window
     * that contains the component that is the source of the
     * given action event.
     *
     * @param  e  action event with which to find Session.
     * @return	Session instance, or null if error.
     */
    public static Session getSession(ActionEvent e) {
	// Use the SessionFrameMapper to get the Session for the event.
	return SessionFrameMapper.getSessionForEvent(e);
    } // getSession
} // JSwatAction
