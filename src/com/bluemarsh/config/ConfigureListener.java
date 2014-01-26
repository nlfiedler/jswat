/*********************************************************************
 *
 *      Copyright (C) 2000-2001 Nathan Fiedler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * PROJECT:     JConfigure
 * FILE:        ConfigureListener.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      10/01/00        Initial version
 *      nf      04/07/01        Changed to just one method.
 *
 * DESCRIPTION:
 *      Defines the configure listener interface.
 *
 * $Id: ConfigureListener.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.util.EventListener;

/**
 * The listener interface for receiving configure events.
 *
 * @author  Nathan Fiedler
 * @version 1.1  4/7/01
 */
public interface ConfigureListener extends EventListener {

    /**
     * Invoked when the configuration has been accepted by the user.
     */
    public void configurationChanged();
} // ConfigureListener
