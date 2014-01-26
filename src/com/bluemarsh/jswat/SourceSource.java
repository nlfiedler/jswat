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
 * MODULE:      JSwat
 * FILE:        SourceSource.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/20/02        Initial version
 *
 * DESCRIPTION:
 *      Defines the interface for the source of source files.
 *
 * $Id: SourceSource.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import java.io.InputStream;

/**
 * SourceSource describes a source of a source file. That is, it is where
 * the contents of a source file come from. Concrete implementations
 * include <code>FileSource</code> which is backed by a
 * <code>java.io.File</code>.
 *
 * @author  Nathan Fiedler
 */
public interface SourceSource {

    /**
     * Returns just the name of the source file, not including the path
     * to the file, if any.
     *
     * @return  name of source.
     */
    public String getName();

    /**
     * Get the input stream for reading the source code.
     *
     * @return  input stream to the source code.
     */
    public InputStream getInputStream();
} // SourceSource
