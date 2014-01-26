/*********************************************************************
 *
 *      Copyright (C) 2001-2005 Nathan Fiedler
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
 * $Id: SourceViewTextArea.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.util.PriorityList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.text.BadLocationException;

/**
 * Class SourceViewTextArea is a read-only text component.
 *
 * <p>In addition, this class has the ability to interpret the value of the
 * string under the mouse pointer. That is, it will read the string and
 * assume it is a variable reference. If it is a variable, the value is
 * displayed in the form of a tooltip.</p>
 *
 * @author  Nathan Fiedler
 * @author  David Taylor
 * @author  Masaru Ohba
 */
public class SourceViewTextArea extends JComponent implements ActionListener, Scrollable {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Color for the gutter background. */
    private static Color gutterColor = Color.lightGray;
    /** Content that contains the text. */
    private SourceContent content;
    /** Draw layer for drawing the current text selection. */
    private SelectionDrawLayer selectionDrawLayer;
    /** Draw layers in priority order. */
    private PriorityList drawLayersList;
    /** Draw layers in priority order. */
    private PriorityList gutterLayersList;
    /** Tooltip producers in priority order. */
    private PriorityList tooltipProducersList;
    /** Width of the view gutter. */
    private int gutterWidth;
    /** Width of the source code. */
    private int sourceWidth;

    /**
     * Constructs a SourceViewTextArea for the given text.
     *
     * @param  buf  array of text, sized to fit.
     */
    public SourceViewTextArea(char[] buf) {
        content = new SourceContent(buf);
        ToolTipManager.sharedInstance().registerComponent(this);
        drawLayersList = new PriorityList();
        gutterLayersList = new PriorityList();
        tooltipProducersList = new PriorityList();
        selectionDrawLayer = new SelectionDrawLayer();
        addDrawLayer(selectionDrawLayer);
        MouseSelector selector = new MouseSelector();
        addMouseListener(selector);
        addMouseMotionListener(selector);
        setBackground(Color.white);
        // This is only half of it; we still need to do the work in the
        // mouseDragged() method.
        setAutoscrolls(true);

        // Enable copy functionality.
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,
            ActionEvent.CTRL_MASK, false);
        this.registerKeyboardAction(this, "COPY", copy, JComponent.WHEN_FOCUSED);
    } // SourceViewTextArea

    /**
     * An action is being performed.
     *
     * @param  e  action event.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("COPY")) {
            String selText = getSelectedText();
            if (!selText.equals("")) {
                StringSelection strsel  = new StringSelection(selText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    strsel, strsel);
            }
        }
    } // actionPerformed

    /**
     * Adds the given draw layer to this text area.
     *
     * @param  layer  draw layer to add.
     */
    public void addDrawLayer(DrawLayer layer) {
        // Priorities are lower to higher in our world.
        int prio = layer.getPriority();
        if (prio < DrawLayer.PRIORITY_HIGHEST
            || prio > DrawLayer.PRIORITY_LOWEST) {
            throw new IllegalArgumentException("priority out of range");
        }
        drawLayersList.add(layer, prio);
    } // addDrawLayer

    /**
     * Adds the given gutter draw layer to this text area.
     *
     * @param  layer  gutter draw layer to add.
     */
    public void addGutterLayer(GutterDrawLayer layer) {
        // Priorities are lower to higher in our world.
        int prio = layer.getPriority();
        if (prio < GutterDrawLayer.PRIORITY_HIGHEST
            || prio > GutterDrawLayer.PRIORITY_LOWEST) {
            throw new IllegalArgumentException("priority out of range");
        }
        gutterLayersList.add(layer, prio);
    } // addGutterLayer

    /**
     * Adds the given tooltip producer to this text area.
     *
     * @param  prod  tooltip producer to add.
     */
    public void addTooltipProducer(TooltipProducer prod) {
        // Priorities are lower to higher in our world.
        int prio = prod.getPriority();
        if (prio < TooltipProducer.PRIORITY_HIGHEST
            || prio > TooltipProducer.PRIORITY_LOWEST) {
            throw new IllegalArgumentException("priority out of range");
        }
        tooltipProducersList.add(prod, prio);
    } // addTooltipProducer

    /**
     * Returns the source view text area content object.
     *
     * @return  content.
     */
    public SourceContent getContent() {
        return content;
    } // getContent

    /**
     * Calculate the width of the gutter and return the value in pixels.
     *
     * @return  gutter width in pixels.
     */
    protected int getGutterWidth() {
        if (gutterWidth <= 0) {
            return getGutterWidth(getFontMetrics(getFont()));
        } else {
            return gutterWidth;
        }
    } // getGutterWidth

    /**
     * Calculate the width of the gutter and return the value in pixels.
     *
     * @param  metrics  font metrics for calculating width.
     * @return  gutter width in pixels.
     */
    protected int getGutterWidth(FontMetrics metrics) {
        if (gutterWidth <= 0) {
            Preferences prefs = Preferences.userRoot().node(
                "com/bluemarsh/jswat/view");
            if (prefs.getBoolean("viewLineNumbers",
                    Defaults.VIEW_LINE_NUMBERS)) {
                gutterWidth = metrics.stringWidth("999999");
            } else {
                // Give a little space for the breakpoint indicators.
                gutterWidth = metrics.stringWidth("WW");
            }
        }
        return gutterWidth;
    } // getGutterWidth

    /**
     * Returns the word (if any) that contains position offset.
     *
     * @param  offset  the offset into the document.
     * @return  the word that contains offset, or null if no word is nearby.
     * @throws  BadLocationException
     *          if the text location is invalid.
     */
    public String getIdentifierAt(int offset)
        throws BadLocationException {
        // This method could stand optimisation in terms or speed, and
        // also improvement to recognise x[i], etc.
        int line = content.getLineOfOffset(offset) + 1;
        int lineEnd = content.getLineEndOffset(line);
        char[] lineText = content.getBuffer();

        if (Character.isWhitespace(lineText[offset])) {
            return null;
        }

        boolean foundMatch = false;
        int endOfWord = offset;

        // Find the end of the Java identifier.
        for (int i = offset; i < lineEnd; i++) {
            char ch = lineText[i];
            if (!Character.isJavaIdentifierPart(ch) && ch != ']') {
                endOfWord = i;
                foundMatch = true;
                break;
            }
        }

        if (!foundMatch) {
            endOfWord = lineEnd - 1;
        }

        // Find the start of the Java identifier.
        foundMatch = false;
        int startOfWord = offset;
        for (int i = offset; i >= 0; i--) {
            char ch = lineText[i];
            // Accept '.', '[', and ']' while going backwards so we
            // build up a full variable name (e.g. "obj[i].field").
            if (!Character.isJavaIdentifierPart(ch)
                && ch != '.' && ch != '[' && ch != ']') {
                startOfWord = i + 1;
                foundMatch = true;
                break;
            }
        }

        if (!foundMatch) {
            startOfWord = 0;
        }
        if ((startOfWord < 0) || ((endOfWord - startOfWord + 1) < 1)) {
            return null;
        }
        return new String(lineText, startOfWord, endOfWord - startOfWord);
    } // getIdentifierAt

    /**
     * Returns the preferred size of the viewport for a view component.
     * For example the preferredSize of a JList component is the size
     * required to acommodate all of the cells in its list however the
     * value of preferredScrollableViewportSize is the size required for
     * JList.getVisibleRowCount() rows. A component without any properties
     * that would effect the viewport size should just return
     * getPreferredSize() here.
     *
     * @return  The preferredSize of a JViewport whose view is this Scrollable.
     * @see JViewport#getPreferredSize
     */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    } // getPreferredScrollableViewportSize

    /**
     * Calculates and returns the preferred size of the text area.
     *
     * @return  the preferred size.
     */
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(getFont());
        // height = numLines * fontHeight
        int lineHeight = metrics.getHeight();
        int height = content.getLineCount() * lineHeight;
        // Masaru Ohba's proportional font code.
        // width = gutterWidth + sourceWidth;
        int gutterWidth = getGutterWidth(metrics);
        if (sourceWidth <= 0) {
            char[] buffer = content.getBuffer();
            int startOfLine = 0;
            int endOfLine = 0;
            int lineWidth = 0;
            for (int line = 0 ; line < content.getLineCount() ; line++) {
                try {
                    startOfLine = content.getLineStartOffset(line);
                    endOfLine = content.getLineEndOffset(line);
                } catch (BadLocationException ble) {
                    startOfLine = 0;
                    endOfLine = 0;
                }
                lineWidth = metrics.charsWidth(buffer, startOfLine,
                    endOfLine - startOfLine);
                if (lineWidth > sourceWidth) {
                    sourceWidth = lineWidth;
                }
            }
        }
        return new Dimension(gutterWidth + sourceWidth, height);
    } // getPreferredSize

    /**
     * Components that display logical rows or columns should compute
     * the scroll increment that will completely expose one new row
     * or column, depending on the value of orientation. Ideally,
     * components should handle a partially exposed row or column by
     * returning the distance required to completely expose the item.
     *
     * <p>Scrolling containers, like JScrollPane, will use this method
     * each time the user requests a unit scroll.</p>
     *
     * @param  visibleRect  The view area visible within the viewport
     * @param  orientation  Either SwingConstants.VERTICAL or
     *                      SwingConstants.HORIZONTAL.
     * @param  direction    Less than zero to scroll up/left, greater
     *                      than zero for down/right.
     * @return  The "unit" increment for scrolling in the specified direction.
     * @see JScrollBar#setUnitIncrement
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect,
        int orientation, int direction) {
        FontMetrics metrics = getFontMetrics(getFont());
        if (orientation == SwingConstants.VERTICAL) {
            return metrics.getHeight();
        } else {
            return metrics.charWidth('a');
        }
    } // getScrollableUnitIncrement

    /**
     * Components that display logical rows or columns should compute
     * the scroll increment that will completely expose one block
     * of rows or columns, depending on the value of orientation.
     *
     * <p>Scrolling containers, like JScrollPane, will use this method
     * each time the user requests a block scroll.</p>
     *
     * @param  visibleRect  The view area visible within the viewport
     * @param  orientation  Either SwingConstants.VERTICAL or
     *                      SwingConstants.HORIZONTAL.
     * @param  direction    Less than zero to scroll up/left, greater
     *                      than zero for down/right.
     * @return  The "block" increment for scrolling in the specified direction.
     * @see JScrollBar#setBlockIncrement
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect,
        int orientation, int direction) {
        Rectangle vis = getVisibleRect();
        if (orientation == SwingConstants.VERTICAL) {
            return vis.height;
        } else {
            return vis.width;
        }
    } // getScrollableBlockIncrement

    /**
     * Return true if a viewport should always force the width of this
     * Scrollable to match the width of the viewport. For example a normal
     * text view that supported line wrapping would return true here, since it
     * would be undesirable for wrapped lines to disappear beyond the right
     * edge of the viewport. Note that returning true for a Scrollable
     * whose ancestor is a JScrollPane effectively disables horizontal
     * scrolling.
     *
     * <p>Scrolling containers, like JViewport, will use this method each
     * time they are validated.</p>
     *
     * @return  True if a viewport should force the Scrollable's width to
     *          match its own.
     */
    public boolean getScrollableTracksViewportWidth() {
        if (getParent() instanceof JViewport) {
            return ((JViewport) getParent()).getWidth()
                > getPreferredSize().width;
        }
        return false;
    } // getScrollableTracksViewportWidth

    /**
     * Return true if a viewport should always force the height of this
     * Scrollable to match the height of the viewport. For example a
     * columnar text view that flowed text in left to right columns
     * could effectively disable vertical scrolling by returning true here.
     *
     * <p>Scrolling containers, like JViewport, will use this method each
     * time they are validated.</p>
     *
     * @return  True if a viewport should force the Scrollable's height
     *          to match its own.
     */
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            return ((JViewport) getParent()).getHeight()
                > getPreferredSize().height;
        }
        return false;
    } // getScrollableTracksViewportHeight

    /**
     * This function returns the text that is currently selected in the source
     * viewer. Returns an empty string when there is no selection.
     *
     * @return  the selected text, or empty string if no selection.
     */
    public String getSelectedText() {
        if (getSelectionStart() != getSelectionEnd()) {
            String selText = new String(getContent().getBuffer(),
                getSelectionStart(), getSelectionEnd() -
                getSelectionStart());
            return selText;
        } else {
            return "";
        }
    } // getSelectedText

    /**
     * Returns the selected text's end position. Return 0 if the document
     * is empty, or the value of dot if there is no selection.
     *
     * @return  the end position >= 0
     */
    public int getSelectionEnd() {
        return selectionDrawLayer.getSelectionEnd();
    } // getSelectionEnd

    /**
     * Returns the selected text's start position. Return 0 if the document
     * is empty, or the value of dot if there is no selection.
     *
     * @return  the start position >= 0
     */
    public int getSelectionStart() {
        return selectionDrawLayer.getSelectionStart();
    } // getSelectionStart

    /**
     * Return the appropriate tooltip per the mouse position.
     *
     * @param  event  the event that caused the tooltip to appear.
     * @return  the text for the tooltip.
     */
    public String getToolTipText(MouseEvent event) {
        Iterator iter = tooltipProducersList.iterator();
        while (iter.hasNext()) {
            TooltipProducer prod = (TooltipProducer) iter.next();
            String tip = prod.produceTooltip(event, this);
            if (tip != null) {
                // Found a good tip, return it.
                return tip;
            }
        }

        // No one had anything to say.
        return null;
    } // getToolTipText

    /**
     * Tests if the point falls within the text area gutter.
     *
     * @param  x  x position of point within text area.
     * @return  true if point is within gutter.
     */
    public boolean isWithinGutter(int x) {
        return x < getGutterWidth();
    } // isWithinGutter

    /**
     * Find the point that defines the upper-left corner of the
     * character at the given offset.
     *
     * @param  offset  offset within the character buffer.
     * @return  point in the view, or null if error.
     */
    public Point modelToView(int offset) {
        int line = 0;
        int start = 0;
        try {
            line = content.getLineOfOffset(offset);
            start = content.getLineStartOffset(line);
        } catch (BadLocationException ble) {
            return null;
        }
        FontMetrics metrics = getFontMetrics(getFont());
        Point p = new Point();
        p.y = line * metrics.getHeight();
        p.x = metrics.charsWidth(content.getBuffer(), start, offset - start);
        // Can't forget to add the gutter width.
        p.x += getGutterWidth(metrics);
        return p;
    } // modelToView

    /**
     * Invoked by Swing to draw components.
     *
     * @param  g  the Graphics context in which to paint.
     */
    public void paint(Graphics g) {
        // Draws all the fancy Swing stuff.
        super.paint(g);

        if (!isVisible()) {
            return;
        }

        // May change to a debug graphics. Also sets font and color.
        g = getComponentGraphics(g);

        // Set up the clipping bounds.
        Rectangle clipRect = g.getClipBounds();
        int clipX;
        int clipY;
        int clipW;
        int clipH;
        int width = getWidth();
        int height = getHeight();
        if (clipRect == null) {
            clipX = getX();
            clipY = getY();
            clipW = width;
            clipH = height;
        } else {
            clipX = clipRect.x;
            clipY = clipRect.y;
            clipW = clipRect.width;
            clipH = clipRect.height;
        }

        if (clipW > width) {
            clipW = width;
        }
        if (clipH > height) {
            clipH = height;
        }

        // Determine the width of the gutter.
        int gutterWidth = getGutterWidth();
        if (clipX + clipW > gutterWidth) {
            // The text area needs to be redrawn.
            if (clipX < gutterWidth) {
                int w = clipX + clipW - gutterWidth;
                g.setClip(gutterWidth, clipY, w, clipH);
            } else {
                g.setClip(clipX, clipY, clipW, clipH);
            }
            paintText(g);
        }

        if (clipX < gutterWidth) {
            // The gutter needs to be redrawn.
            if (clipX + clipW > gutterWidth) {
                g.setClip(clipX, clipY, gutterWidth - clipX, clipH);
            } else {
                g.setClip(clipX, clipY, clipW, clipH);
            }
            paintGutter(g);
        }
    } // paint

    /**
     * Draws the line numbers in the gutter area.
     *
     * @param  g  graphics context to draw to.
     */
    protected void paintGutter(Graphics g) {
        Rectangle clipRect = g.getClipBounds();
        int clipX = clipRect.x;
        int clipY = clipRect.y;
        int clipW = clipRect.width;
        int clipH = clipRect.height;

        // Draw the component background.
        g.setColor(gutterColor);
        g.fillRect(clipX, clipY, clipW, clipH);

        // Compute the first line to be drawn.
        FontMetrics metrics = g.getFontMetrics(getFont());
        int ascent = metrics.getMaxAscent();
        int lineHeight = metrics.getHeight();
        // Always round down for first line.
        int firstLine = clipY / lineHeight;
        // Always round up for last line.
        int lastLine = (clipY + clipH) / lineHeight + 1;

        // Get an array of just the active layers.
        ArrayList activeLayers = new ArrayList();
        for (int ii = 0; ii < gutterLayersList.size(); ii++) {
            GutterDrawLayer layer = (GutterDrawLayer) gutterLayersList.get(ii);
            if (layer.isActive()) {
                activeLayers.add(layer);
            }
        }
        GutterDrawLayer[] drawLayers = (GutterDrawLayer[])
            activeLayers.toArray(new GutterDrawLayer[activeLayers.size()]);

        // Create the initial draw context.
        BasicDrawContext ctx = new BasicDrawContext(
            getForeground(), gutterColor, getFont());

        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/view");
        boolean drawLineNums = prefs.getBoolean("viewLineNumbers",
            Defaults.VIEW_LINE_NUMBERS);

        int gutterWidth = getGutterWidth(metrics);
        int line = firstLine;
        while (line < lastLine) {
            // Have each draw layer update the draw context.
            for (int ii = 0; ii < drawLayers.length; ii++) {
                drawLayers[ii].updateContext(ctx, line);
            }

            // We assume the left margin is at zero.
            int y = line * lineHeight;

            // First fill in the background.
            g.setColor(ctx.getBackColor());
            g.fillRect(0, y, gutterWidth, lineHeight);

            if (drawLineNums) {
                // Draw in the line number for this line.
                String str = Integer.toString(line + 1);
                // Draw string so it is right-justified, but slightly
                // away from the right border of the gutter.
                int w = metrics.stringWidth(str);
                y += ascent;
                g.setColor(ctx.getForeColor());
                g.drawString(str, gutterWidth - w - 2, y);
            }

            // Advance to the next line.
            line++;

            // Reset the draw context to the defaults.
            ctx.reset();
        }
    } // paintGutter

    /**
     * Draws the text of the text area.
     *
     * @param  g  graphics context to draw to.
     */
    protected void paintText(Graphics g) {
        Rectangle clipRect = g.getClipBounds();
        int clipX = clipRect.x;
        int clipY = clipRect.y;
        int clipW = clipRect.width;
        int clipH = clipRect.height;

        // Draw the component background.
        g.setColor(getBackground());
        g.fillRect(clipX, clipY, clipW, clipH);

        // Compute the first line to be drawn.
        FontMetrics metrics = g.getFontMetrics(getFont());
        int ascent = metrics.getMaxAscent();
        int lineHeight = metrics.getHeight();
        // Always round down for first line.
        int firstLine = clipY / lineHeight;
        // Always round up for last line.
        int lastLine = (clipY + clipH) / lineHeight + 1;
        if (lastLine >= content.getLineCount()) {
            lastLine = content.getLineCount() - 1;
        }

        // Start at the beginning of the first line.
        int nextUpdate = 0;
        try {
            nextUpdate = content.getLineStartOffset(firstLine);
        } catch (BadLocationException ble) {
            return;
        }

        // Get an array of just the active layers.
        ArrayList activeLayers = new ArrayList();
        for (int ii = 0; ii < drawLayersList.size(); ii++) {
            DrawLayer layer = (DrawLayer) drawLayersList.get(ii);
            if (layer.isActive()) {
                activeLayers.add(layer);
            }
        }
        DrawLayer[] drawLayers = (DrawLayer[]) activeLayers.toArray(
            new DrawLayer[activeLayers.size()]);

        // Create the initial draw context.
        BasicDrawContext ctx = new BasicDrawContext(
            getForeground(), getBackground(), getFont());

        int charWidth = metrics.charWidth('a');
        char[] buffer = content.getBuffer();

        int gutterWidth = getGutterWidth(metrics);
        int x = gutterWidth;
        int line = firstLine;
        while (line < lastLine) {
            // Remember the last place where the context changed.
            int lastUpdate = nextUpdate;

            // Get the start and end of this line.
            int startOfLine = 0;
            int endOfLine = 0;
            try {
                startOfLine = content.getLineStartOffset(line);
                endOfLine = content.getLineEndOffset(line);
            } catch (BadLocationException ble) {
                return;
            }

            // Assume that we can draw until the end of the line.
            nextUpdate = endOfLine;

            // Have each draw layer update the draw context and give us
            // the number of characters we can draw using this context.
            for (int ii = 0; ii < drawLayers.length; ii++) {
                int next = drawLayers[ii].updateContext(ctx, lastUpdate);
                if (next < nextUpdate) {
                    // Have to use the shortest length available.
                    nextUpdate = next;
                }
            }

            // Draw text using current context until nextUpdate.
            // Assumes that the left margin is at gutterWidth.
            int updateLength = nextUpdate - lastUpdate;
            if (updateLength > 0) {
                // Draw something only if we have something to draw.
                int w = metrics.charsWidth(buffer, lastUpdate, updateLength);
                int y = line * lineHeight;

                // First fill in the background.
                g.setColor(ctx.getBackColor());
                g.fillRect(x, y, w, lineHeight);

                // Draw in the characters.
                y += ascent;
                g.setColor(ctx.getForeColor());
                g.drawChars(buffer, lastUpdate, updateLength, x, y);

                // Advance to the next string draw position.
                x += w;
            }

            if (nextUpdate == endOfLine) {
                // See if we should draw the background color past the
                // end of the line of text.
                boolean extendsEOL = false;
                for (int ii = 0; ii < drawLayers.length; ii++) {
                    if (drawLayers[ii].extendsEOL()) {
                        extendsEOL = true;
                        break;
                    }
                }

                if (extendsEOL) {
                    // Yes, paint in the background to the right edge.
                    int y = line * lineHeight;
                    int w = getWidth() - x;
                    g.setColor(ctx.getBackColor());
                    g.fillRect(x, y, w, lineHeight);
                }

                // Advance to the next line.
                line++;
                x = gutterWidth;
                try {
                    nextUpdate = content.getLineStartOffset(line);
                } catch (BadLocationException ble) {
                    // We're probably at the end of the view.
                }
            }

            // Reset the draw context to the defaults.
            ctx.reset();
        }
    } // paintText

    /**
     * Causes the visible portion of the gutter to be repainted.
     */
    void repaintGutter() {
        // Find visible region.
        Rectangle vis = getVisibleRect();
        if (gutterWidth == 0) {
            // This is only temporary, to force a complete repaint.
            gutterWidth = vis.width;
        }
        repaint(0, vis.y, gutterWidth, vis.height);
    } // repaintGutter

    /**
     * Supports deferred automatic layout. Called when the size or internal
     * layout of the component has changed.
     */
    public void revalidate() {
        // Clear the gutter width so it is recalculated.
        gutterWidth = 0;
        super.revalidate();
    } // revalidate

    /**
     * Selects the text found between the specified start and end locations.
     *
     * @param  selectionStart  the start position of the text >= 0
     * @param  selectionEnd    the end position of the text >= 0
     */
    public void select(int selectionStart, int selectionEnd) {
        selectionDrawLayer.setSelection(selectionStart, selectionEnd);
        repaint();
    } // select

    /**
     * Sets the source view text area content object to the one given.
     *
     * @param  content  new content object.
     */
    public void setContent(SourceContent content) {
        this.content = content;
        repaint();
    } // setContent

    /**
     * Turns a view coordinate into a one-based line number.
     *
     * @param  pt  Point within the view coordinates.
     * @return  One-based line number corresponding to the point.
     *          If the returned value is -1 then there was an error.
     */
    public int viewToLine(Point pt) {
        // Deal with out of bounds.
        if (pt.y > getHeight()) {
            return content.getLineCount();
        }

        // Use point to determine line number.
        FontMetrics metrics = getFontMetrics(getFont());
        int lineHeight = metrics.getHeight();
        return pt.y / lineHeight + 1;
    } // viewToLine

    /**
     * Turns a view coordinate into a zero-based character offset.
     *
     * @param  pt  Point within the view coordinates.
     * @return  Zero-based character offset corresponding to the point.
     *          If the returned value is -1 then there was an error.
     */
    public int viewToModel(Point pt) {
        // Deal with out of bounds.
        if (pt.y > getHeight()) {
            return content.getLength() - 1;
        }

        FontMetrics metrics = getFontMetrics(getFont());
        // Minus one because we want a zero-based line number.
        int line = viewToLine(pt) - 1;
        try {
            // Rest of Masaru Ohba's proportional font support.
            // Use a binary search to find the character offset.
            char[] buffer = content.getBuffer();
            int lowOffset = content.getLineStartOffset(line);
            int highOffset = content.getLineEndOffset(line);
            int midOffset = (lowOffset + highOffset) / 2;
            int textWidth = 0;
            int offsetWidth = getGutterWidth(metrics);
            while (midOffset > lowOffset) {
                textWidth = metrics.charsWidth(buffer, lowOffset,
                    midOffset - lowOffset);
                if (offsetWidth + textWidth < pt.x) {
                    lowOffset = midOffset;
                    offsetWidth += textWidth;
                } else {
                    highOffset = midOffset;
                }
                midOffset = (lowOffset + highOffset) / 2;
            }
            return lowOffset;
        } catch (BadLocationException ble) {
            return -1;
        }
    } // viewToModel

    /**
     * Class MouseSelector is responsible for tracking the mouse to
     * provide the text selection capability.
     *
     * @author  Nathan Fiedler
     */
    class MouseSelector implements MouseListener, MouseMotionListener {
        /** First place the mouse was pressed. A value of -1 indicates
         * that selection is not in progress (and mouse drag events
         * should be ignored). */
        private int firstPos = -1;

        /**
         * Invoked when the mouse has been clicked on a component.
         *
         * @param  e  mouse event.
         */
        public void mouseClicked(MouseEvent e) {
            // ignored
        } // mouseClicked

        /**
         * Invoked when a mouse button is pressed on a component and then
         * dragged. Mouse drag events will continue to be delivered to the
         * component where the first originated until the mouse button is
         * released (regardless of whether the mouse position is within the
         * bounds of the component).
         *
         * @param  e  mouse event.
         */
        public void mouseDragged(MouseEvent e) {
            if (firstPos >= 0 && !e.isConsumed()) {
                // Do the actual work of autoscrolling.
                Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
                ((JComponent) e.getSource()).scrollRectToVisible(r);

                // Now deal with the text selection.
                int p0 = firstPos;
                int p1 = viewToModel(e.getPoint());
                if (p1 < 0) {
                    // out of bounds
                    return;
                }
                if (p0 > p1) {
                    int t = p0;
                    p0 = p1;
                    p1 = t;
                }
                select(p0, p1 + 1);
                e.consume();
            }
        } // mouseDragged

        /**
         * Invoked when the mouse enters a component.
         *
         * @param  e  mouse event.
         */
        public void mouseEntered(MouseEvent e) {
            // ignored
        } // mouseEntered

        /**
         * Invoked when the mouse exits a component.
         *
         * @param  e  mouse event.
         */
        public void mouseExited(MouseEvent e) {
            // ignored
        } // mouseExited

        /**
         * Invoked when the mouse button has been moved on a component
         * (with no buttons down).
         *
         * @param  e  mouse event.
         */
        public void mouseMoved(MouseEvent e) {
            // ignored
        } // mouseMoved

        /**
         * Invoked when a mouse button has been pressed on a component.
         *
         * @param  e  mouse event.
         */
        public void mousePressed(MouseEvent e) {
            // Put the focus in the text area.
            SourceViewTextArea.this.requestFocus();
            if (!e.isConsumed() && !e.isPopupTrigger()) {
                firstPos = viewToModel(e.getPoint());
                if (firstPos < 0) {
                    // out of bounds
                    return;
                }
                // Start by selecting nothing at all.
                select(firstPos, firstPos);
                e.consume();
            } else {
                // Indicate that selection is not in progress.
                firstPos = -1;
            }
        } // mousePressed

        /**
         * Invoked when a mouse button has been released on a component.
         *
         * @param  e  mouse event.
         */
        public void mouseReleased(MouseEvent e) {
            // ignored
        } // mouseReleased
    } // MouseSelector
} // SourceViewTextArea
