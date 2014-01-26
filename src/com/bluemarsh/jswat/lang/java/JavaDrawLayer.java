/*********************************************************************
 *
 *      Copyright (C) 2001-2003 Nathan Fiedler
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
 *      nf      01/10/02        Fixed bug #387
 *      nf      11/17/03        Moved to new package
 *
 * $Id: JavaDrawLayer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang.java;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.view.AbstractDrawLayer;
import com.bluemarsh.jswat.view.DrawContext;
import com.bluemarsh.jswat.util.SkipList;
import java.awt.Color;
import java.util.prefs.Preferences;

/**
 * JavaDrawLayer is responsible for syntax colorizing Java source code.
 *
 * @author  Nathan Fiedler
 */
public class JavaDrawLayer extends AbstractDrawLayer {
    /** Our draw layer priority. */
    private static final int PRIORITY = 256;
    /** The color for drawing characters. */
    private static Color characterColor;
    /** The color for drawing comments. */
    private static Color commentColor;
    /** The color for drawing identifiers. */
    private static Color identifierColor;
    /** The color for drawing keywords. */
    private static Color keywordColor;
    /** The color for drawing literals. */
    private static Color literalColor;
    /** The color for drawing numbers. */
    private static Color numberColor;
    /** The color for drawing primitives. */
    private static Color primitiveColor;
    /** The color for drawing strings. */
    private static Color stringColor;
    /** List of token info objects. */
    private SkipList tokenList;

    /**
     * Gets the priority level of this particular draw layer. Typically
     * each type of draw layer has its own priority. Lower values are
     * higher priority.
     *
     * @return  priority level.
     */
    public int getPriority() {
        return PRIORITY;
    } // getPriority

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
            setActive(false);
        } else {
            setActive(true);
        }
        tokenList = tokens;
    } // setTokens

    /**
     * The user preferences have changed and the preferred colors
     * may have been modified. Update appropriately.
     *
     * @param  prefs  view Preferences node.
     * @throws  NumberFormatException
     *          if the specified color is improperly encoded.
     */
    static void updateColors(Preferences prefs) throws NumberFormatException {
        String color = prefs.get(
            "colors.character",
            (String) Defaults.VIEW_COLORS.get("colors.character"));
        characterColor = Color.decode(color);
        color = prefs.get(
            "colors.comment",
            (String) Defaults.VIEW_COLORS.get("colors.comment"));
        commentColor = Color.decode(color);
        color = prefs.get(
            "colors.identifier",
            (String) Defaults.VIEW_COLORS.get("colors.identifier"));
        identifierColor = Color.decode(color);
        color = prefs.get(
            "colors.keyword",
            (String) Defaults.VIEW_COLORS.get("colors.keyword"));
        keywordColor = Color.decode(color);
        color = prefs.get(
            "colors.literal",
            (String) Defaults.VIEW_COLORS.get("colors.literal"));
        literalColor = Color.decode(color);
        color = prefs.get(
            "colors.number",
            (String) Defaults.VIEW_COLORS.get("colors.number"));
        numberColor = Color.decode(color);
        color = prefs.get(
            "colors.primitive",
            (String) Defaults.VIEW_COLORS.get("colors.primitive"));
        primitiveColor = Color.decode(color);
        color = prefs.get(
            "colors.string",
            (String) Defaults.VIEW_COLORS.get("colors.string"));
        stringColor = Color.decode(color);
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
            tokenList.searchLeastSmaller(offset);

        // Did we find a correct match?
        if ((info != null)
            && (info.getStartOffset() <= offset)
            && (info.getEndOffset() > offset)) {

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
            default:
                // Do nothing.
                break;
            }
        } else {

            // Have to find the next token for the next update offset.
            info = (JavaTokenInfo) tokenList.searchNextLarger(offset);
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
