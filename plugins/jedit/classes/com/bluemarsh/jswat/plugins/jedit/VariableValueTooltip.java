/*********************************************************************
 *
 *      Copyright (C) 2001-2004 David Taylor
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
 * $Id: VariableValueTooltip.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.plugins.jedit;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.FieldNotObjectException;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.util.Variables;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.TextUtilities;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.TextAreaExtension;
import org.gjt.sp.util.Log;
import java.awt.Graphics2D;

/**
 * This text area extension handles the variable value tooltip support.
 *
 * @author  David Taylor
 * @author  Stefano Maestri
 * @author  Nathan Fiedler
 * @author  Dirk Moebius
 */
public class VariableValueTooltip extends TextAreaExtension
    implements EBComponent {
    /** Our associated text area. */
    private JEditTextArea textArea;

    /**
     * Constructor for VariableValueTooltip class.
     *
     * @param  textArea  our new text area.
     */
    public VariableValueTooltip(JEditTextArea textArea) {
        this.textArea = textArea;
        EditBus.addToBus(this);
    } // VariableValueTooltip

    /**
     * Gets the tooltip text.
     *
     * @param  x  x coordinates.
     * @param  y  y coordinates.
     * @return  the toolTipText value.
     */
    public String getToolTipText(int x, int y) {
        Session session = JSwatPlugin.getInstance().getSession();
        if (!session.isActive()) {
            return null;
        }
        Buffer buffer = textArea.getBuffer();
        if (!buffer.isLoaded()) {
            return null;
        }
        String name = buffer.getName();
        if (!name.endsWith(".java")) {
            return null;
        }

        int offset = textArea.xyToOffset(x, y);
        int col = offset - textArea.getLineStartOffset(
            textArea.getLineOfOffset(offset));
        String lineText = textArea.getLineText(
            textArea.getLineOfOffset(offset));

        if (col < 0 || col >= lineText.length()) {
            return null;
        }

        int start = TextUtilities.findWordStart(lineText, col, "_");
        int end = TextUtilities.findWordEnd(lineText, col, "_");

        if (start >= 0 && start <= end && end < lineText.length()) {
            String id = null;

            if (start == end) {
                // special case for single letter vars
                if (end < lineText.length()) {
                    id = lineText.substring(start, end + 1);
                }
            } else {
                id = lineText.substring(start, end);
            }

            if (id != null && Character.isJavaIdentifierStart(id.charAt(0))) {
                ContextManager cxt = (ContextManager)
                    session.getManager(ContextManager.class);
                ThreadReference thread = cxt.getCurrentThread();

                if (thread != null) {
                    int frame = cxt.getCurrentFrame();

                    try {
                        // Try to interpret the value.
                        Value v = Variables.getValue(id, thread, frame);
                        if (v == null) {
                            return id + " = null";
                        } else {
                            return id + " = " + v.toString();
                        }
                    } catch (AbsentInformationException aie) {
                        // ignored
                    } catch (ClassNotPreparedException cnpe) {
                        // ignored
                    } catch (FieldNotObjectException fnoe) {
                        // ignored
                    } catch (IncompatibleThreadStateException itse) {
                        // ignored
                    } catch (IllegalThreadStateException itse2) {
                        // ignored
                    } catch (InvalidStackFrameException isfe) {
                        // ignored
                    } catch (NativeMethodException nme) {
                        // ignored
                    } catch (NoSuchFieldException nsfe) {
                        // ignored
                    } catch (ObjectCollectedException oce) {
                        // ignored
                    } catch (Exception e) {
                        // Report anything out of the ordinary.
                        Log.log(Log.ERROR, this,
                                "exception interpreting value '"
                                + id + "': " + e);
                    }
                }
            }
        }

        return null;
    } // getToolTipText

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
            }
        }
    } // handleMessage

    /**
     * Paint the decoration on the given line of the text area.
     *
     * @param  gfx           graphics context.
     * @param  screenlLine   line of the text area relative to the top of
     *                       the view..
     * @param  physicalLine  actual text line.
     * @param  start         start of something.
     * @param  end           end of something.
     * @param  y             y coordinates.
     */
    public void paintValidLine(Graphics2D gfx, int screenlLine,
                               int physicalLine, int start, int end, int y) {
        // we draw nothing
    } // paintValidLine
} // VariableValueTooltip
