/*********************************************************************
 *
 *      Copyright (C) 2000-2002 Nathan Fiedler
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
 * MODULE:      JSwat
 * FILE:        Manager.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      06/24/00        Initial version
 *      nf      04/22/01        Turned into an interface.
 *      nf      05/12/02        Removed the redundant API
 *
 * $Id: Manager.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.event.SessionListener;

/**
 * The Manager interface defines the API required by all manager objects
 * in JSwat. Managers are used to control a subset of features in JSwat,
 * such as breakpoints, source files, debugging context, etc. This Manager
 * API makes it easy for the Session class to deal with several managers at
 * once, and to handle future Managers.
 *
 * <p>Try to avoid circular dependencies between Managers when possible.
 * Unpredictable behavior can occur if one manager's init calls on
 * a second manager, which calls on the first manager (which has not
 * completed its initialization).</p>
 *
 * @author  Nathan Fiedler
 */
public interface Manager extends SessionListener {
} // Manager
