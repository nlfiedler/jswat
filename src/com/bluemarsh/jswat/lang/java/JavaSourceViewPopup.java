/*********************************************************************
 *
 *      Copyright (C) 2003-2005 Nathan Fiedler
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
 * $Id: JavaSourceViewPopup.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.lang.java;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.breakpoint.Breakpoint;
import com.bluemarsh.jswat.breakpoint.BreakpointManager;
import com.bluemarsh.jswat.breakpoint.LineBreakpoint;
import com.bluemarsh.jswat.breakpoint.ResolveException;
import com.bluemarsh.jswat.lang.ClassDefinition;
import com.bluemarsh.jswat.lang.MethodDefinition;
import com.bluemarsh.jswat.view.Bundle;
import com.bluemarsh.jswat.view.SourceViewPopup;
import com.bluemarsh.jswat.view.ViewException;
import com.bluemarsh.jswat.ui.UIAdapter;
import java.awt.Component;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Class JavaSourceViewPopup extends SourceViewPopup and provides the
 * Java-specific features.
 *
 * @author  Nathan Fiedler
 */
class JavaSourceViewPopup extends SourceViewPopup {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Name for "Java parser error" menu item. */
    private static final String PARSER_ERROR_STRING = "parserError";
    /** List of class definition objects. */
    private List classLines;
    /** The "Methods" submenu. */
    private JMenu methodsMenu;
    /** If non-null, name of class user last right-clicked within. */
    private String lastClickedClass;

    /**
     * Creates a JavaSourceViewPopup object.
     *
     * @param  src  source object.
     */
    public JavaSourceViewPopup(SourceSource src) {
        super(src);
        menuItemTable.put(
            PARSER_ERROR_STRING,
            new JMenuItem(Bundle.getString(
                              "SourceViewPopup.parserErrorMsg")));
    } // JavaSourceViewPopup

    /**
     * Try to add a breakpoint at the last clicked class and line.
     * Displays an appropriate error message if needed.
     *
     * @param  session  Session.
     * @param  bpman    breakpoint manager.
     * @return  new breakpoint if successful, null if error.
     */
    protected Breakpoint addBreakpoint(Session session,
                                       BreakpointManager bpman) {
        if (lastClickedClass == null) {
            // Fall back on default behavior.
            return super.addBreakpoint(session, bpman);
        }

        try {
            String fname = sourceSrc.getName();
            Breakpoint bp = new LineBreakpoint(
                lastClickedClass, fname, lastClickedLine);
            bpman.addNewBreakpoint(bp);
            return bp;
        } catch (ClassNotFoundException cnfe) {
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR,
                Bundle.getString("AddBreak.invalidClassMsg") + ' '
                + lastClickedClass);
            return null;
        } catch (ResolveException re) {
            session.getUIAdapter().showMessage(
                UIAdapter.MESSAGE_ERROR, re.errorMessage());
            return null;
        }
    } // addBreakpoint

    /**
     * Attempt to find the breakpoint the user has clicked on, using as
     * much information as is available to this popup.
     *
     * @param  bpman  breakpoint manager to search through.
     * @return  breakpoint if found, null if not.
     * @throws  ViewException
     *          if the line does not contain code.
     */
    protected Breakpoint findBreakpoint(BreakpointManager bpman)
        throws ViewException {
        if (classLines == null) {
            // Fall back on the default behavior.
            return super.findBreakpoint(bpman);
        }

        // Get the class at this line.
        lastClickedClass = ClassDefinition.findClassForLine(
            classLines, lastClickedLine);
        if (lastClickedClass == null) {
            throw new NonCodeLineException();
        }

        // Query existing set of breakpoints for this class and line.
        return bpman.getBreakpoint(lastClickedClass, lastClickedLine);
    } // findBreakpoint

    /**
     * Remove all of the children and add the ever-present items.
     */
    protected void removeAllItems() {
        super.removeAllItems();
        if (methodsMenu != null) {
            add(methodsMenu);
        }
    } // removeAllItems

    /**
     * Set the lists of class and method definitions.
     *
     * @param  classes  list of ClassDefinition objects.
     * @param  methods  list of MethodDefinition objects.
     */
    void setClassDefinitions(List classes, List methods) {
        classLines = classes;
        if (classLines == null || methods == null) {
            methodsMenu = null;
            return;
        }

        // Add the class names to the method definitions. Granted, this
        // is slow, but hopefully there are few classes and few methods
        // in those classes.
        Iterator iter = methods.iterator();
        while (iter.hasNext()) {
            MethodDefinition def = (MethodDefinition) iter.next();
            if (def.getClassName() == null) {
                // Set the class name if not already set.
                String cname = ClassDefinition.findClassForLine(
                    classLines, def.getLine());
                def.setClassName(cname);
            }
        }

        // Build a submenu of the sorted method definitions.
        JMenu menu = new JMenu(Bundle.getString(
                                   "SourceViewPopup.methodsMenuLabel"), true);
        Collections.sort(methods, new MDComparator());
        iter = methods.iterator();
        Preferences prefs = Preferences.userRoot().node(
            "com/bluemarsh/jswat/view");
        boolean shortDesc = prefs.getBoolean(
            "shortMethodDescInPopup", Defaults.VIEW_POPUP_SHORT_METHOD_DESC);
        while (iter.hasNext()) {
            MethodDefinition def = (MethodDefinition) iter.next();
            // Keep the description short, if so desired.
            String desc = shortDesc ? def.getMethodDescShort()
                : def.getMethodDesc();
            if (desc.length() > 80) {
                desc = desc.substring(0, 78) + "...";
            }
            JMenuItem item = new JMenuItem(desc);
            menu.add(item);
            item.setActionCommand(String.valueOf(def.getLine()));
            item.addActionListener(this);
        }
        if (menu.getMenuComponentCount() > 0) {
            methodsMenu = menu;
        }
    } // setClassDefinitions

    /**
     * Resets the UI property to a value from the current look and feel.
     */
    public void updateUI() {
        super.updateUI();
        // Now update our disconnected children.
        if (methodsMenu != null) {
            methodsMenu.updateUI();
            Component[] children = methodsMenu.getMenuComponents();
            if (children != null && children.length > 0) {
                for (int ii = 0; ii < children.length; ii++) {
                    JComponent jcomp = (JComponent) children[ii];
                    jcomp.updateUI();
                    // We assume this items have no children.
                }
            }
        }
    } // updateUI

    /**
     * Comparator for MethodDefinitions.
     *
     * @author  Neeraj Apte
     */
    private static class MDComparator implements java.util.Comparator {

        /**
         * Comparator implementation used to quickly sort methods based
         * on their signature (excluding the modifiers). The Comparator
         * purposely doesn't throw any exceptions because the
         * functionality is not mission-critical.
         *
         * @param  o1  an <code>Object</code> value.
         * @param  o2  an <code>Object</code> value.
         * @return  an <code>int</code> value.
         */
        public int compare(Object o1, Object o2) {
            if (o1 == null || o2 == null) {
                return (o1 == null && o2 == null) ? 0 : -1;
            } else if (o1 instanceof MethodDefinition
                       && o2 instanceof MethodDefinition) {
                String s1 = ((MethodDefinition) o1).getMethodDescShort();
                String s2 = ((MethodDefinition) o2).getMethodDescShort();
                return s1.compareToIgnoreCase(s2);
            } else {
                return -1;
            }
        } // compare
    } // MDComparator
} // JavaSourceViewPopup
