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
 * FILE:         CharArrayIterator.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      02/14/99        Initial version
 *
 * DESCRIPTION:
 *      This file contains the class that implements a character
 *      iterator.
 *
 * $Id: CharArrayIterator.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.util;

import java.text.CharacterIterator;

/**
 * A CharacterIterator that works on character arrays.
 *
 * @author  Nathan Fiedler
 */
public class CharArrayIterator implements CharacterIterator, Cloneable {
    /**
     * First logical position in the array. We will not
     * allow iteration before this position.
     */
    protected int begin;
    /**
     * Last logical position in the array. We will not
     * allow iteration after this position.
     */
    protected int end;
    /**
     * Current offset into the character array from which we
     * are reading characters.
     */
    protected int pos;
    /**
     * The character array that is the source of characters
     * for this iterator.
     */
    protected char text[];

    /**
     * Creates a new CharArrayIterator with the given character
     * array as its text to iterate over. Sets the initial iterator
     * position to zero.
     *
     * @param  text  array of characters to iterate over
     */
    public CharArrayIterator(char text[]) {
        this(text, 0);
    } // CharArrayIterator

    /**
     * Creates a new CharArrayIterator with the given character
     * array as its text to iterate over.
     *
     * @param  text  array of characters to iterate over
     * @param  pos   initial iterator position
     */
    public CharArrayIterator(char text[], int pos) {
        this(text, 0, text.length, pos);
    } // CharArrayIterator

    /**
     * Creates a new CharArrayIterator with the given character
     * array as its text to iterate over.
     *
     * @param  text   array of characters to iterate over
     * @param  begin  index of the first character
     * @param  end    index of the character following the last character
     * @param  pos    initial iterator position
     */
    public CharArrayIterator(char text[], int begin, int end, int pos) {
        setText(text, begin, end, pos);
    } // CharArrayIterator

    /**
     * Sets the text within this given character array iterator so
     * it can be reused.
     *
     * @param  text   array of characters to iterate over
     * @param  begin  index of the first character
     * @param  end    index of the character following the last character
     * @param  pos    initial iterator position
     */
    public void setText(char text[], int begin, int end, int pos) {
        if (text == null) {
            throw new NullPointerException();
        }
        this.text = text;
        if ((begin < 0) || (begin > end) || (end > text.length)) {
            throw new IllegalArgumentException("Invalid substring range");
        }
        if ((pos < begin) || (pos > end)) {
            throw new IllegalArgumentException("Invalid position");
        }
        this.begin = begin;
        this.end = end;
        this.pos = pos;
    } // setText

    /**
     * Create a copy of this iterator.
     *
     * @return A copy of this
     */
    public Object clone() {
        try {
            CharArrayIterator other = (CharArrayIterator)super.clone();
            return other;
        } catch (CloneNotSupportedException cnse) {
            return null;
        }
    } // clone

    /**
     * Gets the character at the current position (as returned by
     * getIndex()).
     *
     * @return the character at the current position or DONE if the current
     *         position is off the end of the text.
     * @see #getIndex
     */
    public char current() { 
        if ((pos >= begin) && (pos < end)) {
            return text[pos];
        } else {
            return DONE;
        }
    } // current

    /**
     * Sets the position to getBeginIndex() and returns the character
     * at that position.
     *
     * @return the first character in the text, or DONE if the text is empty
     * @see #getBeginIndex
     */
    public char first() {
        pos = getBeginIndex();
        return current();
    } // first

    /**
     * Returns the start index of the text.
     *
     * @return the index at which the text begins.
     */
    public int getBeginIndex() {
        return begin;
    } // getBeginIndex

    /**
     * Returns the end index of the text. This index is the index
     * of the first character following the end of the text.
     *
     * @return the index after the last character in the text
     */
    public int getEndIndex() {
        return end;
    } // getEndIndex

    /**
     * Returns the current index.
     *
     * @return the current index.
     */
    public int getIndex() {
        return pos;
    } // getIndex

    /**
     * Sets the position to getEndIndex()-1 (getEndIndex() if the
     * text is empty) and returns the character at that position.
     *
     * @return the last character in the text, or DONE if the text is empty
     * @see #getEndIndex
     */
    public char last() {
        if (end != begin) {
            pos = end - 1;
        } else {
            pos = end;
        }
        return current();
    } // last

    /**
     * Increments the iterator's index by one and returns the character
     * at the new index. If the resulting index is greater or equal
     * to getEndIndex(), the current index is reset to getEndIndex() and
     * a value of DONE is returned.
     *
     * @return the character at the new position or DONE if the new
     *         position is off the end of the text range.
     */
    public char next() {
        if (pos < end - 1) {
            pos++;
            return current();
        } else {
            pos = end;
            return DONE;
        }
    } // next

    /**
     * Decrements the iterator's index by one and returns the character
     * at the new index. If the current index is getBeginIndex(), the index
     * remains at getBeginIndex() and a value of DONE is returned.
     *
     * @return the character at the new position or DONE if the current
     *         position is equal to getBeginIndex().
     */
    public char previous() {
        if (pos > begin) {
            pos--;
            return current();
        } else {
            pos = begin;
            return DONE;
        }
    } // previous

    /**
     * Sets the position to the specified position in the text and
     * returns that character.
     *
     * @param  position  the position within the text. Valid values
     *                   range from getBeginIndex() to getEndIndex().
     *                   An IllegalArgumentException is thrown if an
     *                   invalid value is supplied.
     * @return the character at the specified position or DONE if the
     *         specified position is equal to getEndIndex()
     */
    public char setIndex(int position) {
//      if ((position < getBeginIndex()) ||
//           (position > getEndIndex())) {
//          throw new IllegalArgumentException();
//      }
        pos = position;
        return current();
    } // setIndex

    /**
     * Test wrapper for this class.
     *
     * @param  args  array of command-line arguments
     */
    public static void main(String args[]) {
        String tstr = "silly spring string";
        char t[] = new char[tstr.length()];
        tstr.getChars(0, tstr.length(), t, 0);
        CharArrayIterator cai = new CharArrayIterator(t);

        System.out.println("first = " + cai.first());
        System.out.println("last = " + cai.last());
        System.out.println("beginIndex = " + cai.getBeginIndex());
        System.out.println("endIndex = " + cai.getEndIndex());
        cai.setIndex(5);
        System.out.println("index = " + cai.getIndex());
        System.out.println("next 3 = " + cai.next() +
                           cai.next() +
                           cai.next());
        cai.setIndex(5);
        System.out.println("previous 3 = " + cai.previous() +
                           cai.previous() +
                           cai.previous());
    } // main
} // CharArrayIterator
