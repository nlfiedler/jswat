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
 * FILE:        ZipSource.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/20/02        Initial version
 *      nf      04/29/02        Fixed bug #511
 *
 * DESCRIPTION:
 *      Defines the class that reads source code from a Zip or Jar file.
 *
 * $Id: ZipSource.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import java.io.*;
import java.util.zip.*;

/**
 * Class ZipSource is a concrete implementation of SourceSource in
 * which the source is backed by a <code>java.util.zip.ZipFile</code>
 * instance.
 *
 * @author  Nathan Fiedler
 */
public class ZipSource implements SourceSource {
    /** Archive file from which the source eminates. */
    protected ZipFile zipFile;
    /** Entry in the zip file representing the source. */
    protected ZipEntry zipEntry;
    /** Used for getting just the name of the zip entry. */
    protected File entryAsFile;

    /**
     * Constructs a ZipSource from the given file and entry.
     *
     * @param  file   zip file.
     * @param  entry  entry within the zip file.
     * @exception  IllegalArgumentException
     *             if arguments are null.
     */
    public ZipSource(ZipFile file, ZipEntry entry) {
        if (file == null || entry == null) {
            throw new IllegalArgumentException("arguments must be non-null");
        }
        zipFile = file;
        zipEntry = entry;
        // Use both zip file name and entry name to make a
        // unique identifier for the hashCode() method.
        entryAsFile = new File(file.getName(), entry.getName());
    } // ZipSource

    /**
     * Returns just the name of the source file, not including the path
     * to the file, if any.
     *
     * @return  name of source.
     */
    public String getName() {
        // Lop off the path to the file.
        return entryAsFile.getName();
    } // getName

    /**
     * Get the input stream for reading the source code.
     *
     * @return  input stream to the source code, or null if error.
     */
    public InputStream getInputStream() {
        try {
            return zipFile.getInputStream(zipEntry);
        } catch (IOException ioe) {
            return null;
        } catch (IllegalStateException ise) {
            // Zip file was apparently closed.
            try {
                // Try to reopen the zip file.
                zipFile = new ZipFile(zipFile.getName());
                zipEntry = zipFile.getEntry(zipEntry.getName());
                return zipFile.getInputStream(zipEntry);
            } catch (IOException ioe) {
                return null;
            }
        }
    } // getInputStream

    /**
     * Returns a hash code value for the object.
     *
     * @return  a hash code value for this object.
     */
    public int hashCode() {
        // This uses the zip file name and the entry name together.
        // Fortunately this works despite the fact the that path is
        // actually completely invalid.
        return entryAsFile.hashCode();
    } // hashCode
} // SourceSource
