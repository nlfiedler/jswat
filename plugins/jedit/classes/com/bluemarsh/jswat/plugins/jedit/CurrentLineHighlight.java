/*********************************************************************
 *
 *      Copyright (C) 2004 Dirk Moebius
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
 * $Id: CurrentLineHighlight.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.plugins.jedit;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.textarea.DisplayManager;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.TextAreaExtension;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class CurrentLineHighlight is a text area decorator that indicates
 * the location of the current debugging line.
 *
 * @author  Dirk Moebius
 */
public class CurrentLineHighlight extends TextAreaExtension
    implements EBComponent {
    /** Color for rendering the current debugging line */
    private static Color currentLineHighlightColor;
    /** Map which maps text areas to current line highlighters. */
    private static HashMap textAreaToHighlighter;
    /** Our associated text area. */
    private JEditTextArea textArea;
    /** The current file to be painted with a highlight. */
    private String currentPath;
    /** The current line to be highlighted, or -1 if there is no highlight. */
    private int currentLine;

    static {
        currentLineHighlightColor = new Color(255, 0, 0);
        textAreaToHighlighter = new HashMap();
    }

    /**
     * Constructor for CurrentLineHighlight class.
     *
     * @param  textArea  our new text area.
     */
    public CurrentLineHighlight(JEditTextArea textArea) {
        this.textArea = textArea;
        this.currentLine = -1;
        EditBus.addToBus(this);
    } // CurrentLineHighlight

    /**
     * Get the CurrentLineHighlight for the specified text area, or create a
     * new one if no highlighter exists.
     *
     * @param  textArea  text area for which to return highlighter.
     * @return  a current line highlighter for the text area
     */
    static CurrentLineHighlight getHighlighter(JEditTextArea textArea) {
        CurrentLineHighlight highlight = (CurrentLineHighlight)
            textAreaToHighlighter.get(textArea);
        if (highlight == null) {
            highlight = new CurrentLineHighlight(textArea);
            textAreaToHighlighter.put(textArea, highlight);
            textArea.getPainter().addExtension(highlight);
        }
        return highlight;
    } // getHighlighter

    /**
     * Handle a message from the jEdit edit bus.
     *
     * @param  message  the message.
     */
    public void handleMessage(EBMessage message) {
        if (message instanceof EditPaneUpdate) {
            EditPaneUpdate epu = (EditPaneUpdate) message;
            if (epu.getWhat() == EditPaneUpdate.DESTROYED) {
                // The text area is going away, so are we.
                EditBus.removeFromBus(this);
                textAreaToHighlighter.remove(epu.getEditPane().getTextArea());
            }
        }
    } // handleMessage

    /**
     * Marks the text area associated with this highlighter for repaint.
     */
    private void markForRedraw() {
        DisplayManager displayMgr = textArea.getDisplayManager();
        int physicalFirst = displayMgr.getFirstVisibleLine();
        int physicalLast = displayMgr.getLastVisibleLine();
        textArea.invalidateLineRange(physicalFirst, physicalLast);
    } // markForRedraw

    /**
     * Paint the decoration on the given line of the text area.
     *
     * @param  gfx           graphics context.
     * @param  screenlLine   line from top of buffer, affected by folding.
     * @param  physicalLine  line from top of file, not affected by folding.
     * @param  start         start of something.
     * @param  end           end of something.
     * @param  y             y coordinates.
     */
    public void paintValidLine(Graphics2D gfx,
                               int screenlLine,
                               int physicalLine,
                               int start,
                               int end,
                               int y) {
        if (currentPath == null) {
            return;
        }
        if (currentLine == -1) {
            return;
        }
        if (currentLine != physicalLine + 1) {
            return;
        }

        Buffer buffer = textArea.getBuffer();
        if (!buffer.isLoaded()) {
            return;
        }
        if (!buffer.getPath().equals(currentPath)) {
            return;
        }

        FontMetrics fm = textArea.getPainter().getFontMetrics();
        int lineHeight = fm.getHeight();
        int descent = fm.getDescent();

        gfx.setColor(currentLineHighlightColor);
        gfx.drawRect(0, y, textArea.getWidth() - 1, lineHeight - 1);
    } // paintValidLine

    /**
     * Remove all known line highlighters.
     * This gets called when a session is deactived or resumed.
     */
    static void removeAllHighlighters() {
        Iterator it = textAreaToHighlighter.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            JEditTextArea textArea = (JEditTextArea) entry.getKey();
            CurrentLineHighlight clh = (CurrentLineHighlight) entry.getValue();
            textArea.getPainter().removeExtension(clh);
            EditBus.removeFromBus(clh);
        }
        textAreaToHighlighter.clear();
    } // removeAllHighlighters

    /**
     * Set the new location.
     *
     * @param  path  the new file, or null if no highlight should be painted.
     * @param  line  the new line number, or -1.
     */
    void setCurrentLine(String path, int line) {
        if (path != this.currentPath || line != this.currentLine) {
            markForRedraw();
        }
        this.currentPath = path;
        this.currentLine = line;
    } // setCurrentLine
} // CurrentLineHighlight
