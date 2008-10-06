/*********************************************************************
 *
 *      Copyright (C) 2003 Nathan Fiedler
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
 * FILE:        ByteCodeSource.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      03/04/03        Initial version
 *      nf      04/07/03        Fixed bug 749
 *
 * $Id: ByteCodeSource.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.sun.jdi.ReferenceType;
import java.io.InputStream;

/**
 * ByteCodeSource provides a means of reading byte code from a class in
 * debuggee VM. It provides a reference to the ReferenceType which
 * represents the class of interest.
 *
 * @author  Nathan Fiedler
 */
public class ByteCodeSource extends AbstractSource {
    /** Short name of the class. */
    private String shortName;
    /** Full name of the class. */
    private String longName;

    /**
     * Constructs a ByteCodeSource instance for the given class.
     *
     * @param  clazz  class to read bytecode from.
     */
    public ByteCodeSource(ReferenceType clazz) {
        longName = clazz.name();
        int dotIdx = longName.lastIndexOf('.');
        if (dotIdx > 0) {
            shortName = longName.substring(dotIdx + 1);
        } else {
            shortName = longName;
        }
    } // ByteCodeSource

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param  o  the reference object with which to compare.
     * @return  true if this object is the same as the obj argument;
     *          false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof ByteCodeSource) {
            ByteCodeSource obcs = (ByteCodeSource) o;
            return obcs.longName.equals(longName);
        }
        return false;
    } // equals

    /**
     * Attempts to ensure that the referenced source actually exists.
     *
     * @return  true if source exists, false if not found.
     */
    public boolean exists() {
        return true;
    } // exists

    /**
     * Get the input stream for reading the source code.
     *
     * @return  input stream to the source code.
     */
    public InputStream getInputStream() {
        return null;
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
        return longName;
    } // getLongName

    /**
     * Returns just the name of the source file, not including the path
     * to the file, if any.
     *
     * @return  name of source.
     */
    public String getName() {
        return shortName;
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
        return longName.hashCode();
    } // hashCode

    /**
     * Indicates if this source object represents byte code, as
     * opposed to source code of a high-level language.
     *
     * @return  true if byte code, false otherwise.
     */
    public boolean isByteCode() {
        return true;
    } // isByteCode
} // ByteCodeSource
