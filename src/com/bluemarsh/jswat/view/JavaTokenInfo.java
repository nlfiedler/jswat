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
 * FILE:        JavaTokenInfo.java
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
 *      This file contains the JavaTokenInfo class definition.
 *
 * $Id: JavaTokenInfo.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

/**
 * Class JavaTokenInfo holds token information for Java source code.
 *
 * @author  Nathan Fiedler
 */
public class JavaTokenInfo extends BasicTokenInfo {
    /** Indicates that the token is a Java keyword. */
    public static final int TOKEN_KEYWORD = 1;
    /** Indicates that the token is a comment. */
    public static final int TOKEN_COMMENT = 2;
    /** Indicates that the token is a primitive type. */
    public static final int TOKEN_PRIMITIVE = 3;
    /** Indicates that the token is a number. */
    public static final int TOKEN_NUMBER = 4;
    /** Indicates that the token is a Java identifier. */
    public static final int TOKEN_IDENTIFIER = 5;
    /** Indicates that the token is a character. */
    public static final int TOKEN_CHARACTER = 6;
    /** Indicates that the token is a string. */
    public static final int TOKEN_STRING = 7;
    /** Indicates that the token is a literal (e.g. "null"). */
    public static final int TOKEN_LITERAL = 8;
    /** One fo the TOKEN_* constants. */
    protected int tokenType;

    /**
     * Constructs a JavaTokenInfo with the given token type.
     *
     * @param  offset  offset to the start of the token.
     * @param  length  length of the token string.
     * @param  token   token type (one of the TOKEN_* constants).
     */
    public JavaTokenInfo(int offset, int length, int token) {
        super(offset, length);
        tokenType = token;
    } // JavaTokenInfo

    /**
     * Returns this token's type.
     *
     * @return  one of the TOKEN_* constants.
     */
    public int getTokenType() {
        return tokenType;
    } // getTokenType

    /**
     * Returns a string representation of this.
     *
     * @return  a String.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(super.toString());
        buf.append(", type=");
        switch (tokenType) {
            case TOKEN_KEYWORD:
                buf.append("KWD");
                break;

            case TOKEN_COMMENT:
                buf.append("CMT");
                break;

            case TOKEN_PRIMITIVE:
                buf.append("PRM");
                break;

            case TOKEN_NUMBER:
                buf.append("NBR");
                break;

            case TOKEN_IDENTIFIER:
                buf.append("IDR");
                break;

            case TOKEN_CHARACTER:
                buf.append("CHR");
                break;

            case TOKEN_STRING:
                buf.append("STR");
                break;

            case TOKEN_LITERAL:
                buf.append("LIT");
                break;
        }
        return buf.toString();
    } // toString
} // JavaTokenInfo
