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
 * FILE:        TokenInfo.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/08/01        Initial version
 *      nf      01/06/02        Added toString()
 *
 * DESCRIPTION:
 *      This file contains the TokenInfo interface definition.
 *
 * $Id: TokenInfo.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

/**
 * This interface provides information about each individual token
 * being drawn to the text area. A token is usually a single word
 * or character that has meaning in one programming language or
 * another.
 *
 * @author  Nathan Fiedler
 */
public interface TokenInfo {

    /**
     * Get the length of this token.
     *
     * @return  length of the token.
     */
    public int getLength();

    /**
     * Get the character offset within the document of the first
     * character in this token.
     *
     * @return  first character offset.
     */
    public int getStartOffset();

    /**
     * Returns a string representation of this token information.
     *
     * @return  a String.
     */
    public String toString();
} // TokenInfo
