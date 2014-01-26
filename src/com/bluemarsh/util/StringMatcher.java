/*********************************************************************
 *
 *      Copyright (C) 1999-2001 Nathan Fiedler
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
 * License along with this library; if not, write to the Free
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * PROJECT:      Utils
 * MODULE:       String Matching
 * FILE:         StringMatcher.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/15/99        Initial version
 *      nf      11/11/01        Changed to use CharacterIterator
 *
 * DESCRIPTION:
 *      This file defines the interface for all string-matching
 *      algorithms.
 *
 * $Id: StringMatcher.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.util;

import java.text.CharacterIterator;

/**
 * Defines the interface that all string-matching algorithms should
 * implement. Used to allow the user to select which string-matching
 * algorithm they want to use when searching files.
 *
 * @author Nathan Fiedler
 */
public interface StringMatcher {

    /**
     * Uninitialize now that searching is completely finished. Useful
     * if the string matcher needs to close open files or other
     * resources.
     */
    public void deinit();

    /**
     * Search for the pattern in the given character sequence.
     * The pattern was given in the init() method call.
     *
     * @param  iter  iterator that provides the character sequence
     *               in which to look for the pattern.
     * @param  patt  pattern string to look for.
     * @return  offset into text where pattern was found, or -1
     *          if the pattern could not be found.
     */
    public int find(CharacterIterator iter, String patt);

    /**
     * Sets this matcher to be case-sensitive or case-insensitive,
     * depending on the argument.
     *
     * @param  ignore  true for case-insensitive.
     */
    public void ignoreCase(boolean ignore);

    /**
     * Initialize any tables that are needed for finding a pattern
     * within some unknown text. For example, the Boyer-Moore
     * algorithm will require setting up the last-occurrence and
     * good-suffix tables before searching can be performed.
     *
     * @param  patt  pattern to look for.
     */
    public void init(String patt);
} // StringMatcher
