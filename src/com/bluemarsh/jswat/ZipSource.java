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
 *      nf      04/29/02        Fixed bug 511
 *
 * $Id: ZipSource.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class ZipSource is a concrete implementation of SourceSource in which
 * the source is backed by a <code>java.util.zip.ZipFile</code>
 * instance.
 *
 * @author  Nathan Fiedler
 */
public class ZipSource extends AbstractSource {
    /** Archive file from which the source eminates. */
    private ZipFile zipFile;
    /** Entry in the zip file representing the source. */
    private ZipEntry zipEntry;
    /** The last part of the zip entry name. */
    private String entryName;
    /** Used for getting just the name of the zip entry. */
    private File entryAsFile;

    /**
     * Constructs a ZipSource from the given file and entry.
     *
     * @param  file   zip file.
     * @param  entry  entry within the zip file.
     */
    public ZipSource(ZipFile file, ZipEntry entry) {
        if (file == null || entry == null) {
            throw new IllegalArgumentException("arguments must be non-null");
        }
        zipFile = file;
        zipEntry = entry;

        // Convert the name to the local file system form, stripping
        // away everything but the name of the file itself (i.e.
        // "path/to/entry.file" becomes "entry.file").
        entryName = new File(zipEntry.getName()).getName();

        // Use both zip file name and entry name to make a
        // unique identifier for the hashCode() method.
        entryAsFile = new File(file.getName(), entry.getName());
    } // ZipSource

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param  o  the reference object with which to compare.
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof ZipSource) {
            ZipSource ozs = (ZipSource) o;
            return ozs.entryAsFile.equals(entryAsFile);
        }
        return false;
    } // equals

    /**
     * Attempts to ensure that the referenced source actually exists.
     *
     * @return  true if source exists, false if not found.
     */
    public boolean exists() {
        // we always exist
        return true;
    } // exists

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
     * Get the full name of the source object. This may be the path and
     * file name of a file (may or may not be a canonical path), the
     * fully-qualified name of a class, or a zip entry and file name.
     * This may be the same as the short name.
     *
     * @return  long name of source object.
     */
    public String getLongName() {
        return zipFile.getName() + "!" + zipEntry.getName();
    } // getLongName

    /**
     * Returns just the name of the source file, not including the path
     * to the file, if any.
     *
     * @return  name of source.
     */
    public String getName() {
        return entryName;
    } // getName

    /**
     * Returns the complete path to the source file, if the source
     * object is stored in a file that is not an archive.
     *
     * @return  file path, or null if not applicable.
     */
    public String getPath() {
        return null;
    } // getPath

    /**
     * Returns a hash code value for the object.
     *
     * @return  a hash code value for this object.
     */
    public int hashCode() {
        // This uses the zip file name and the entry name together.
        // Fortunately this works despite the fact that the path is
        // actually completely invalid.
        return entryAsFile.hashCode();
    } // hashCode

    /**
     * Indicates if this source object represents byte code, as
     * opposed to source code of a high-level language.
     *
     * @return  true if byte code, false otherwise.
     */
    public boolean isByteCode() {
        return false;
    } // isByteCode
} // SourceSource
