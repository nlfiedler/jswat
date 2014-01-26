/*********************************************************************
 *
 *      Copyright (C) 2001 Nathan Fiedler
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
 * MODULE:      View
 * FILE:        BasicTokenInfo.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/09/01        Initial version
 *      nf      01/06/02        Added toString()
 *
 * DESCRIPTION:
 *      This file contains the BasicTokenInfo class definition.
 *
 * $Id: BasicTokenInfo.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

/**
 * This class provides a basic implementation of the <code>TokenInfo</code>
 * interface.
 *
 * @author  Nathan Fiedler
 */
public class BasicTokenInfo implements TokenInfo {
    /** Offset into the character buffer to the start of the token. */
    protected int offset;
    /** Length of the token in characters. */
    protected int length;

    /**
     * Constructs a BasicTokenInfo object using the given length and offset.
     *
     * @param  offset  offset to the start of the token.
     * @param  length  length of the token string.
     */
    public BasicTokenInfo(int offset, int length) {
        this.offset = offset;
        this.length = length;
    } // BasicTokenInfo

    /**
     * Get the length of this token in characters.
     *
     * @return  length of the token.
     */
    public int getLength() {
        return length;
    } // getLength

    /**
     * Get the character offset within the document of the last
     * character in this token.
     *
     * @return  last character offset.
     */
    public int getEndOffset() {
        return offset + length;
    } // getEndOffset

    /**
     * Get the character offset within the document of the first
     * character in this token.
     *
     * @return  first character offset.
     */
    public int getStartOffset() {
        return offset;
    } // getStartOffset

    /**
     * Returns a string representation of this.
     *
     * @return  a String.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("offset=");
        buf.append(offset);
        buf.append(", length=");
        buf.append(length);
        return buf.toString();
    } // toString
} // BasicTokenInfo
