/*********************************************************************
 *
 *      Copyright (C) 2002-2004 Stefano Maestri
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
 * $Id: BreakpointHighlight.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.plugins.jedit;

import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointEvent;
import com.bluemarsh.jswat.breakpoint.BreakpointListener;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.LocatableBreakpoint;
import com.bluemarsh.jswat.lang.ClassDefinition;
import com.bluemarsh.jswat.parser.java.lexer.LexerException;
import com.bluemarsh.jswat.parser.java.parser.ParserException;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.textarea.DisplayManager;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.TextAreaExtension;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Class BreakpointHighlight is a text area decorator that indicates the
 * location of breakpoints set by the plugin. This extension acts as a
 * breakpoint listener in order to stay abrest of changes in the
 * breakpoints. It does this in order to maintain a table of lines at
 * which breakpoints are set. This table greatly reduces the time needed
 * in the paintValidLine() method had the table not been utilized.
 *
 * @author  Stefano Maestri
 * @author  Nathan Fiedler
 * @author  Dirk Moebius
 */
public class BreakpointHighlight extends TextAreaExtension
    implements BreakpointListener, EBComponent {
    /** Color for rendering resolved breakpoints. */
    private static Color resolvedBreakpointHighlightColor;
    /** Color for rendering unresolved breakpoints. */
    private static Color unresolvedBreakpointHighlightColor;
    /** Color for rendering disabled breakpoints. */
    private static Color disabledBreakpointHighlightColor;
    /** Our associated text area. */
    private JEditTextArea textArea;
    /** Table of Integer line numbers and Booleans indicating where
     * breakpoints are located. Lines without breakpoints are not in the
     * table, to conserve memory. Note that the line numbers are
     * zero-based to match jEdit's line numbering scheme. */
    private HashMap linesWithBreakpoints;

    static {
        resolvedBreakpointHighlightColor = new Color(255, 0, 0);
        unresolvedBreakpointHighlightColor = new Color(0, 0, 255);
        disabledBreakpointHighlightColor = new Color(128, 128, 128);
    }

    /**
     * Constructor for BreakpointHighlight class.
     *
     * @param  textArea  our new text area.
     */
    public BreakpointHighlight(JEditTextArea textArea) {
        this.textArea = textArea;
        linesWithBreakpoints = new HashMap();
        // add yourself to EditBus
        EditBus.addToBus(this);
        // add yourself to the BreakpointManager
        BreakpointManager breakMgr = (BreakpointManager)
            JSwatPlugin.getInstance().getSession().getManager(
                BreakpointManager.class);
        breakMgr.addBreakListener(this);
        init();
    } // BreakpointHighlight

    /**
     * Invoked when a breakpoint has been added.
     *
     * @param  event  breakpoint change event
     */
    public void breakpointAdded(BreakpointEvent event) {
        try {
            Buffer buffer = textArea.getBuffer();
            if (buffer.isLoaded()) {
                Breakpoint bp = event.getBreakpoint();
                if (bp instanceof LocatableBreakpoint) {
                    LocatableBreakpoint lbp = (LocatableBreakpoint) bp;
                    int line = lbp.getLineNumber();
                    String classname = JSwatPlugin.getInstance()
                        .getClassnameAtLine(textArea, line);
                    if (classname != null) {
                        if (lbp.matchesClassName(classname)) {
                            linesWithBreakpoints.put(
                                new Integer(line - 1),
                                bp.isEnabled() ? bp.isResolved()
                                ? resolvedBreakpointHighlightColor
                                : unresolvedBreakpointHighlightColor
                                : disabledBreakpointHighlightColor);
                            textArea.invalidateLine(line - 1);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // has already been handled elsewhere
        } catch (LexerException e) {
            // has already been handled elsewhere
        } catch (ParserException e) {
            // has already been handled elsewhere
        }
    } // breakpointAdded

    /**
     * Invoked when a breakpoint has been changed in some way.
     *
     * @param  event  breakpoint change event
     */
    public void breakpointModified(BreakpointEvent event) {
        try {
            Buffer buffer = textArea.getBuffer();
            if (buffer.isLoaded()) {
                Breakpoint bp = event.getBreakpoint();
                if (bp instanceof LocatableBreakpoint) {
                    LocatableBreakpoint lbp = (LocatableBreakpoint) bp;
                    int line = lbp.getLineNumber();
                    String classname = JSwatPlugin.getInstance()
                        .getClassnameAtLine(textArea, line);
                    if (classname != null) {
                        Integer l = new Integer(line - 1);
                        if (linesWithBreakpoints.get(l) != null) {
                            if (lbp.matchesClassName(classname)) {
                                linesWithBreakpoints.put(
                                    l,
                                    bp.isEnabled() ? bp.isResolved()
                                    ? resolvedBreakpointHighlightColor
                                    : unresolvedBreakpointHighlightColor
                                    : disabledBreakpointHighlightColor);
                                textArea.invalidateLine(line - 1);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // has already been handled elsewhere
        } catch (LexerException e) {
            // has already been handled elsewhere
        } catch (ParserException e) {
            // has already been handled elsewhere
        }
    } // breakpointModified

    /**
     * Invoked when a breakpoint has been removed.
     *
     * @param  event  breakpoint change event
     */
    public void breakpointRemoved(BreakpointEvent event) {
        try {
            Buffer buffer = textArea.getBuffer();
            if (buffer.isLoaded()) {
                Breakpoint bp = event.getBreakpoint();
                if (bp instanceof LocatableBreakpoint) {
                    LocatableBreakpoint lbp = (LocatableBreakpoint) bp;
                    int line = lbp.getLineNumber();
                    String classname = JSwatPlugin.getInstance()
                        .getClassnameAtLine(textArea, line);
                    if (classname != null) {
                        if (lbp.matchesClassName(classname)) {
                            linesWithBreakpoints.remove(new Integer(line - 1));
                            textArea.invalidateLine(line - 1);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // has already been handled elsewhere
        } catch (LexerException e) {
            // has already been handled elsewhere
        } catch (ParserException e) {
            // has already been handled elsewhere
        }
    } // breakpointRemoved

    /**
     * Handle a message from the jEdit edit bus.
     *
     * @param  message  the message.
     */
    public void handleMessage(EBMessage message) {
        if (message instanceof EditPaneUpdate) {
            EditPaneUpdate epu = (EditPaneUpdate) message;
            if (epu.getEditPane().getTextArea() == textArea) {
                if (epu.getWhat() == EditPaneUpdate.BUFFER_CHANGED) {
                    // Have to re-initialize when the buffer changes.
                    init();
                } else if (epu.getWhat() == EditPaneUpdate.DESTROYED) {
                    // The text area is going away, so are we.
                    BreakpointManager breakMgr = (BreakpointManager)
                        JSwatPlugin.getInstance().getSession().getManager(
                            BreakpointManager.class);
                    breakMgr.removeBreakListener(this);
                    EditBus.removeFromBus(this);
                }
            }

        } else if (message instanceof BufferUpdate) {
            BufferUpdate bu = (BufferUpdate) message;
            Buffer buffer = bu.getBuffer();
            if (buffer == textArea.getBuffer()) {
                // the updated buffer is the one being currently displayed.
                if (bu.getWhat() == BufferUpdate.LOADED
                    || bu.getWhat() == BufferUpdate.DIRTY_CHANGED) {
                    // Have to re-initialize when the buffer is loaded, edited
                    // or saved.
                    init();
                }
            }
        }
    } // handleMessage

    /**
     * Initializes the list of breakpoint lines.
     */
    protected void init() {
        linesWithBreakpoints.clear();

        BreakpointManager breakMgr = (BreakpointManager)
            JSwatPlugin.getInstance().getSession().getManager(
                BreakpointManager.class);
        Iterator bps = breakMgr.breakpoints(true);

        // Do we have at least one breakpoint?
        if (!bps.hasNext()) {
            return;
        }

        // Get class definitions in this text area.
        List classLines = null;
        try {
            classLines = JSwatPlugin.getInstance().getClassDefinitions(textArea);
        } catch (IOException e) {
            // has already been handled elsewhere
        } catch (LexerException e) {
            // has already been handled elsewhere
        } catch (ParserException e) {
            // has already been handled elsewhere
        }

        if (classLines == null) {
            return;
        }

        while (bps.hasNext()) {
            Breakpoint bp = (Breakpoint) bps.next();
            if (bp instanceof LocatableBreakpoint) {
                LocatableBreakpoint lbp = (LocatableBreakpoint) bp;
                int line = lbp.getLineNumber();
                String classname = ClassDefinition.findClassForLine(classLines,
                                                                    line);
                if (classname != null && lbp.matchesClassName(classname)) {
                    linesWithBreakpoints.put(
                        new Integer(line - 1),
                        bp.isEnabled() ? bp.isResolved()
                        ? resolvedBreakpointHighlightColor
                        : unresolvedBreakpointHighlightColor
                        : disabledBreakpointHighlightColor);
                }
            }
        }
    } // init

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
    public void paintValidLine(Graphics2D gfx, int screenlLine,
                               int physicalLine, int start, int end, int y) {
        Buffer buffer = textArea.getBuffer();
        if (!buffer.isLoaded()) {
            return;
        }

        Color c = (Color) linesWithBreakpoints.get(
            new Integer(physicalLine));
        if (c == null) {
            return;
        }

        FontMetrics fm = textArea.getPainter().getFontMetrics();
        int w = Math.min(textArea.getGutter().getWidth(),
                         fm.getHeight()) - 4;

        gfx.setColor(c);
        gfx.fillRect(2, y + 2, w, w);
        gfx.setColor(Color.BLACK);
        gfx.drawRect(2, y + 2, w, w);
    } // paintValidLine
} // BreakpointHighlight
