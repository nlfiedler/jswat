/*********************************************************************
 *
 *      Copyright (C) 1999-2002 Nathan Fiedler
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
 * FILE:         KMPMatcher.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/15/99        Initial version
 *      nf      11/11/01        Changed to use CharacterIterator
 *      nf      12/18/01        Don't reset the character iterator
 *      nf      01/06/02        Fixed bug 369
 *
 * DESCRIPTION:
 *      This file defines the class for performing the Knuth-Morris-
 *      Pratt string matching algorithm.
 *
 * $Id: KMPMatcher.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.util;

import java.text.CharacterIterator;
import java.text.Collator;
import java.text.CollationElementIterator;
import java.text.RuleBasedCollator;
import java.text.StringCharacterIterator;

/**
 * Implements the StringMatcher interface to perform a
 * Knuth-Morris-Pratt string matching algorithm.
 *
 * @author  Nathan Fiedler
 */
public class KMPMatcher implements StringMatcher {
    /** Size of the collator element map. We use 256 because it is
     * a reasonable size and collision will not be a problem so long
     * as the shifts are conservative. */
    protected static final int MAP_SIZE = 256;
    /** Set to true to ignore case when comparing. Default is false. */
    protected boolean ignoreCase;
    /** Number of elements in the pattern string. */
    protected int patternLength;
    /** Array of the pattern elements. */
    protected int[] patternElements;
    /** The prefix array. */
    protected int[] prefixTable;
    /** Collator used in performing string matches. */
    protected RuleBasedCollator coll;
    /** Strength of the collaction. One of <code>Collator.PRIMARY</code>,
     * <code>Collator.SECONDARY</code>, <code>Collator.TERTIARY</code>,
     * or <code>Collator.IDENTICAL</code>. */
    protected int strength;

    /**
     * Creates a new KMPMatcher object to perform the
     * Knuth-Morris-Pratt string matching algorithm. In order
     * to search a string, the caller must first invoke the
     * init() method with the pattern to look for.
     */
    public KMPMatcher() {
        coll = (RuleBasedCollator) Collator.getInstance();
    } // KMPMatcher

    /**
     * Computes the prefix array values according to the processed
     * pattern elements. This comes from the Compute-Prefix-Function
     * algorithm in "Introduction to Algorithms" by Cormen et al.
     * The processPattern() method must have already been called.
     *
     * @return  array of prefix values
     */
    protected int[] computePrefix() {
        int pTable[] = new int[patternLength];
        pTable[0] = 0;
        int k = 0;
        for (int q = 1; q < patternLength; q++) {
            while ((k > 0) && (patternElements[k] != patternElements[q])) {
                k = pTable[k];
            }
            if (patternElements[k] == patternElements[q]) {
                k++;
            }
            pTable[q] = k;
        }
        return pTable;
    } // computePrefix

    /**
     * Uninitialize now that searching is completely finished.
     * Useful if the string matcher needs to close open files
     * or other resources.
     */
    public void deinit() {
        prefixTable = null;
        patternElements = null;
        patternLength = 0;
    } // deinit

    /**
     * Search for the pattern in the given character sequence.
     * The pattern was given in the init() method call.
     * Uses the Knuth-Morris-Pratt string-matching algorithm.
     * This comes from the KMP-Matcher on page 871 of
     * "Introduction to Algorithms" by Cormen et al.
     *
     * @param  iter  iterator that provides the character sequence
     *               in which to look for the pattern.
     * @param  patt  pattern string to look for.
     * @return  offset into text where pattern was found, or -1
     *          if the pattern could not be found.
     */
    public int find(CharacterIterator iter, String patt) {
        CollationElementIterator textIter =
            coll.getCollationElementIterator(iter);
        // (see also init)
        int mask = getMask(strength);
        int pos = -1; // assume pattern won't be found

        int patIndex = 0;
        boolean getPattern = true;
        boolean getText = true;
        int textElem = 0;
        int patElem = 0;
        boolean done = false;
        while (!done) {
            // Fetch the text and pattern elements.
            if (getText) {
                textElem = textIter.next();
                if (textElem == CollationElementIterator.NULLORDER) {
                    // reached the end of the text elements
                    done = true;
                }
                textElem &= mask;
            }
            if (getPattern) {
                patElem = patternElements[patIndex];
            }
            getText = getPattern = true;

            if (textElem == 0) {
                // Text element was ignorable.
                getPattern = false;
            } else if (patElem == 0) {
                // Pattern element was ignorable, skip to next one.
                patIndex++;
                getText = false;
            } else {
                while ((patIndex > 0) && (patElem != textElem)) {
                    patIndex = prefixTable[patIndex];
                    patElem = patternElements[patIndex];
                    if (patElem == 0) {
                        patIndex++;
                        getText = false;
                    }
                }
                if (patElem == textElem) {
                    patIndex++;
                }
                if (patIndex == patternLength) {
                    if (patElem != 0) {
                        done = true;
                        pos = textIter.getOffset() - patternLength;
                    } else {
                        done = true;
                        // we didn't find pattern
                    }
                }
            }
        }
        return pos;
    } // find

    /**
     * Return a mask for the part of the order we're interested in.
     *
     * @param  weight  part of the order we're interested in
     * @return  mask to select only that part of the order
     */
    protected static int getMask(int weight) {
        switch (weight) {
        case Collator.PRIMARY:
            return 0xFFFF0000;
        case Collator.SECONDARY:
            return 0xFFFFFF00;
        default:
            return 0xFFFFFFFF;
        } // switch
    } // getMask

    /**
     * Initialize any tables that are needed for finding a pattern
     * within some unknown text. For example, the Knuth-Morris-Pratt
     * algorithm will require setting up the pattern prefix table
     * before searching can be performed.
     *
     * @param  patt  pattern to look for.
     */
    public void init(String patt) {
        patternElements = processPattern(patt);
        patternLength = patternElements.length;
        maskPattern(getMask(strength));
        prefixTable = computePrefix();
    } // init

    /**
     * Sets this matcher to be case-sensitive or case-insensitive,
     * depending on the argument. This method must be called before
     * {@link #init} for the setting to take effect for the search.
     *
     * @param  ignore  true for case-insensitive.
     */
    public void ignoreCase(boolean ignore) {
        ignoreCase = ignore;
        // Using Collator.PRIMARY means that spaces are ignorable
        // and looking for "then" in "pick the number" will succeed.
        strength = ignore ? Collator.SECONDARY : Collator.TERTIARY;
        coll.setStrength(strength);
    } // ignoreCase

    /**
     * AND all the pattern elements with the given mask.
     *
     * @param  mask  mask to AND pattern elements with
     */
    protected void maskPattern(int mask) {
        for (int i = 0; i < patternLength; i++) {
            patternElements[i] &= mask;
        }
    } // maskPattern

    /**
     * Analyzes the pattern and builds a pattern elements table
     * and returns it.
     *
     * @param  P  pattern string
     * @return  array of collator elements from the pattern P
     */
    protected int[] processPattern(String P) {
        // First find out how many elements we're dealing with
        int pLen = 0;
        CollationElementIterator iter = coll.getCollationElementIterator(P);
        while (iter.next() != CollationElementIterator.NULLORDER) {
            pLen++;
        }

        // Allocate space to store the pattern elements.
        int pElems[] = new int[pLen];

        // Save the elements for quick access.
        iter.reset();
        for (int i = 0; i < pLen; i++) {
            pElems[i] = iter.next();
        }
        return pElems;
    } // processPattern

    /**
     * Test wrapper for this class. Tests the find() method
     * with a simple test case to ensure it is basically working.
     *
     * @param  args  array of command-line arguments
     */
    public static void main(String args[]) {
        KMPMatcher s = new KMPMatcher();
        s.ignoreCase(true);
        String[] patterns = {
            "strssng",
            "white",
            "henu",
            "hello world"
        };
        String[] targets = {
            "str\u00DFng", // ae = \u00E6, ss = \u00DF
            "whose fleece was white as snow",
            "pick the number",
            "print hello world again"
        };
        boolean[] expected = { true, true, false, true };

        for (int ii = 0; ii < patterns.length; ii++) {
            String p = patterns[ii];
            String t = targets[ii];
            System.out.println("Looking for '" + p + "' in '" + t + "'");
            s.init(p);
            int o = s.find(new StringCharacterIterator(t), p);
            if (o >= 0) {
                System.out.print("found at " + o);
            } else {
                System.out.print("not found");
            }
            if ((o >= 0) == expected[ii]) {
                System.out.println(", good");
            } else {
                System.out.println(", bad");
            }
            s.deinit();
        }
    } // main
} // KMPMatcher
