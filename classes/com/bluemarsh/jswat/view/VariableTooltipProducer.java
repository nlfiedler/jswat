/*********************************************************************
 *
 *      Copyright (C) 2003 Nathan Fiedler
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
 * $Id: VariableTooltipProducer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.view;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.FieldNotObjectException;
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import com.bluemarsh.jswat.ui.UIAdapter;
import com.bluemarsh.jswat.util.Strings;
import com.bluemarsh.jswat.util.Variables;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.awt.Point;
import java.awt.event.MouseEvent;

/**
 * Produces tooltips that display the value of variables under the mouse
 * pointer.
 *
 * @author  Nathan Fiedler
 */
public class VariableTooltipProducer implements TooltipProducer {
    /** Our tooltip producer priority. */
    private static final int PRIORITY = 128;

    /**
     * Constructs a variable value tooltip producer.
     */
    public VariableTooltipProducer() {
    } // VariableTooltipProducer

    /**
     * Gets the priority level of this particular tooltip producer.
     * Typically each type of producer has its own priority. Lower
     * values are higher priority.
     *
     * @return  priority level.
     */
    public int getPriority() {
        return PRIORITY;
    } // getPriority

    /**
     * Generate the appropriate tooltip for the given mouse event.
     * Checks to see if the mouse is hovering over a variable; if so,
     * finds the value of that variable.
     *
     * @param  event  the event that caused the tooltip to appear.
     * @param  area   source view text area making the request.
     * @return  the tooltip, or null if none.
     */
    public String produceTooltip(MouseEvent event, SourceViewTextArea area) {
        Session session = SessionFrameMapper.getSessionForEvent(event);

        // Get the current thread and stack frame.
        ContextManager cxt = (ContextManager)
            session.getManager(ContextManager.class);
        ThreadReference thread = cxt.getCurrentThread();
        if (thread != null) {
            int frame = cxt.getCurrentFrame();

            // Identify the string under the mouse.
            Point pt = event.getPoint();
            int offset = area.viewToModel(pt);
            if (offset < 0) {
                // out of bounds
                return null;
            }

            String id = null;
            try {
                id = area.getIdentifierAt(offset);
                if (id != null) {
                    // Try to interpret the value.
                    Value v = Variables.getValue(id, thread, frame);
                    String s = Variables.printValue(v, thread, ", ");
                    s = Strings.cleanForPrinting(s, 200);
                    return id + " = " + s;
                }
            } catch (AbsentInformationException aie) {
                // Catch and ignore the expected exceptions.
            } catch (ClassNotPreparedException cnpe) {
                // ignored
            } catch (FieldNotObjectException fnoe) {
                // ignored
            } catch (IncompatibleThreadStateException itse) {
                // ignored
            } catch (IndexOutOfBoundsException ioobe) {
                // ignored
            } catch (IllegalThreadStateException itse) {
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
                session.getUIAdapter().showMessage(
                    UIAdapter.MESSAGE_WARNING,
                    "Error reading value for " + id + ": " + e);
            }
        }

        // We have nothing to say.
        return null;
    } // produceTooltip
} // VariableTooltipProducer
