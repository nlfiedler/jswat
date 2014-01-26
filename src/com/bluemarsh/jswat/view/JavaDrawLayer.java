/*********************************************************************
 *
 *      Copyright (C) 2001-2002 Nathan Fiedler
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
 * FILE:        JavaDrawLayer.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      12/29/01        Initial version
 *      nf      01/10/02        Fixed bug 387
 *
 * DESCRIPTION:
 *      This file contains the JavaDrawLayer class definition.
 *
 * $Id: JavaDrawLayer.java 629 2002-10-26 23:03:26Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.adt.SkipList;
import com.bluemarsh.config.JConfigure;
import java.awt.Color;

/**
 * JavaDrawLayer is responsible for syntax colorizing Java source code.
 *
 * @author  Nathan Fiedler
 */
public class JavaDrawLayer extends BasicDrawLayer {
    /** The color for drawing characters. */
    protected static Color characterColor;
    /** The color for drawing comments. */
    protected static Color commentColor;
    /** The color for drawing identifiers. */
    protected static Color identifierColor;
    /** The color for drawing keywords. */
    protected static Color keywordColor;
    /** The color for drawing literals. */
    protected static Color literalColor;
    /** The color for drawing numbers. */
    protected static Color numberColor;
    /** The color for drawing primitives. */
    protected static Color primitiveColor;
    /** The color for drawing strings. */
    protected static Color stringColor;
    /** Turn on to enable extra debugging. */
    protected static final boolean DEBUG = false;
    /** List of token info objects. */
    protected SkipList tokenInfo;

    /**
     * Sets the list of Java tokens. If <code>tokens</code> is non-null
     * then this draw layer will set itself active; otherwise the layer
     * will become inactive.
     *
     * @param  tokens  the set of Java tokens.
     */
    void setTokens(SkipList tokens) {
        if (tokens == null) {
            // Nothing to do if no tokens.
            active = false;
        } else {
            active = true;
        }
        tokenInfo = tokens;

        // Useful debugging info.
        if (DEBUG) {
            java.util.Iterator iter = tokens.iterator();
            System.out.println("Token information:");
            while (iter.hasNext()) {
                Object ti = iter.next();
                System.out.println(ti);
            }
        }
    } // setTokens

    /**
     * The program configuration was modified and the preferred colors
     * may have changed. Update appropriately.
     */
    static void updateColors(JConfigure config) {
        String color = config.getProperty("view.colors.character");
        try {
            characterColor = Color.decode(color);
            color = config.getProperty("view.colors.comment");
            commentColor = Color.decode(color);
            color = config.getProperty("view.colors.identifier");
            identifierColor = Color.decode(color);
            color = config.getProperty("view.colors.keyword");
            keywordColor = Color.decode(color);
            color = config.getProperty("view.colors.literal");
            literalColor = Color.decode(color);
            color = config.getProperty("view.colors.number");
            numberColor = Color.decode(color);
            color = config.getProperty("view.colors.primitive");
            primitiveColor = Color.decode(color);
            color = config.getProperty("view.colors.string");
            stringColor = Color.decode(color);
        } catch (NumberFormatException nfe) {
            // This is very unlikely.
            nfe.printStackTrace();
        }
    } // updateColors

    /**
     * Update the draw context by setting colors, fonts and possibly
     * other draw properties. After making the changes, the draw
     * layer should return of the offset at which it would like to
     * update the context again. This is an efficiency heuristic.
     *
     * @param  ctx     draw context.
     * @param  offset  offset into character buffer indicating where
     *                 drawing is presently taking place.
     * @return  offset into character buffer at which this draw
     *          layer would like to update the draw context again.
     *          In other words, how long this updated context is valid
     *          for in terms of characters in the buffer.
     */
    public int updateContext(DrawContext ctx, int offset) {
        // Search for a token at the given offset.
        JavaTokenInfo info = (JavaTokenInfo)
            tokenInfo.searchLeastSmaller(offset);

        // Did we find a correct match?
        if ((info != null) &&
            (info.getStartOffset() <= offset) &&
            (info.getEndOffset() > offset)) {

            // Modify the draw context appropriately.
            int type = info.getTokenType();

            switch (type) {
            case JavaTokenInfo.TOKEN_KEYWORD:
                ctx.setForeColor(keywordColor);
                break;

            case JavaTokenInfo.TOKEN_COMMENT:
                ctx.setForeColor(commentColor);
                break;

            case JavaTokenInfo.TOKEN_PRIMITIVE:
                ctx.setForeColor(primitiveColor);
                break;

            case JavaTokenInfo.TOKEN_NUMBER:
                ctx.setForeColor(numberColor);
                break;

            case JavaTokenInfo.TOKEN_IDENTIFIER:
                ctx.setForeColor(identifierColor);
                break;

            case JavaTokenInfo.TOKEN_CHARACTER:
                ctx.setForeColor(characterColor);
                break;

            case JavaTokenInfo.TOKEN_STRING:
                ctx.setForeColor(stringColor);
                break;

            case JavaTokenInfo.TOKEN_LITERAL:
                ctx.setForeColor(literalColor);
                break;
            }
        } else {

            // Have to find the next token for the next update offset.
            info = (JavaTokenInfo) tokenInfo.searchNextLarger(offset);
            if (info == null) {
                return Integer.MAX_VALUE;
            } else {
                return info.getStartOffset();
            }
        }

        // The next time we'd like to change is just after this token.
        return info.getStartOffset() + info.getLength();
    } // updateContext
} // JavaDrawLayer
