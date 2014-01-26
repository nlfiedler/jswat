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
 * FILE:        FileSource.java
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
 *      Defines the class that reads source code from a File.
 *
 * $Id: FileSource.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import java.io.*;

/**
 * Class FileSource is a concrete implementation of SourceSource
 * in which the source is backed by a <code>java.io.File</code>
 * instance.
 *
 * @author  Nathan Fiedler
 */
public class FileSource implements SourceSource {
    /** The file that is the source of the source. */
    protected File fileSource;

    /**
     * Construct a FileSource using the given path and filename.
     *
     * @param  name  path and filename.
     * @exception  IllegalArgumentException
     *             if name is null.
     */
    public FileSource(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must be non-null");
        }
        fileSource = new File(name);
    } // FileSource

    /**
     * Constructs a FileSource object for the given File.
     *
     * @param  src  file source.
     * @exception  IllegalArgumentException
     *             if src is null.
     */
    public FileSource(File src) {
        if (src == null) {
            throw new IllegalArgumentException("src must be non-null");
        }
        fileSource = src;
    } // FileSource

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param  o  the reference object with which to compare.
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof FileSource) {
            FileSource ofs = (FileSource) o;
            return ofs.fileSource.equals(fileSource);
        }
        return false;
    } // equals

    /**
     * Returns the File object.
     *
     * @return  File.
     */
    public File getFile() {
        return fileSource;
    } // getFile

    /**
     * Returns just the name of the source file, not including the path
     * to the file, if any.
     *
     * @return  name of source.
     */
    public String getName() {
        return fileSource.getName();
    } // getName

    /**
     * Get the input stream for reading the source code. This may
     * fail, in which case it will return a <code>null</code>.
     *
     * @return  input stream to the source code, or null if error.
     */
    public InputStream getInputStream() {
        try {
            return new FileInputStream(fileSource);
        } catch (FileNotFoundException fnfe) {
            return null;
        }
    } // getInputStream

    /**
     * Returns a hash code value for the object.
     *
     * @return  a hash code value for this object.
     */
    public int hashCode() {
        // Hopefully the file is resolved to the canonical form
        // before the hashcode is calculated.
        return fileSource.hashCode();
    } // hashCode
} // FileSource
