/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: NoAttachingConnectorException.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

/**
 * Thrown when an attaching connector could not be found by
 * VMConnection.
 *
 * @author  Nathan Fiedler
 */
public class NoAttachingConnectorException extends Exception {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a NoAttachingConnectorException with no specified
     * detailed message.
     */
    public NoAttachingConnectorException() {
        super();
    } // NoAttachingConnectorException

    /**
     * Constructs a NoAttachingConnectorException with the specified
     * detailed message.
     *
     * @param  s  the detail message
     */
    public NoAttachingConnectorException(String s) {
        super(s);
    } // NoAttachingConnectorException
} // NoAttachingConnectorException
