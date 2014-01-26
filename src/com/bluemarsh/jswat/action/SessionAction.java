/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * PROJECT:     JSwat
 * MODULE:      JSwat Actions
 * FILE:        SessionAction.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/16/02        Initial version
 *
 * DESCRIPTION:
 *      This file defines the interface which actions implement when
 *      they want their corresponding GUI elements to be enabled and
 *      disabled when the session is activated and deactivated.
 *
 *      Note that there is only ever one instance of any particular
 *      action. It is not the action that is being disabled, it is the
 *      corresponding menu item or toolbar button.
 *
 * $Id: SessionAction.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.action;

/**
 * Marks an action that is ineffective when the Session is inactive.
 *
 * @author  Nathan Fiedler
 */
public interface SessionAction {
} // SessionAction
