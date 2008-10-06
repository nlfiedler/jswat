/*********************************************************************
 *
 *      Copyright (C) 2002-2003 Nathan Fiedler
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
 * MODULE:      JSwat
 * FILE:        Defaults.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      04/17/02        Initial version
 *
 * $Id: Defaults.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat;

import com.bluemarsh.jswat.view.ViewDesktopFactory;
import java.util.Hashtable;

/**
 * Class to contain static final constant default values for the various
 * user preferences.
 *
 * @author  Nathan Fiedler
 */
public class Defaults {
    /** Indicates if locals panel should put fields inside 'this'. */
    public static final boolean LOCALS_HIDE_THIS = true;
    /** Indicates if locals panel should display final fields. */
    public static final boolean LOCALS_SHOW_FINALS = false;
    /** Default source file extension. */
    public static final String FILE_EXTENSION = ".java";
    /** Number of files to list in the recent files menu. */
    public static final int MRO_LIST_SIZE = 5;
    /** Default external source editor. */
    public static final String SOURCE_EDITOR = "notepad %f";
    /** Number of lines to save in messages area. */
    public static final int MESSAGE_LINES_SAVE = 1000;
    /** Number of lines to save in output area. */
    public static final int OUTPUT_LINES_SAVE = 1000;
    /** Indicates if window geometry should be saved. */
    public static final boolean REMEMBER_GEOMETRY = true;
    /** Indicates if window should be raised on event. */
    public static final boolean RAISE_WINDOW = true;
    /** Indicates if source view should be colorized. */
    public static final boolean VIEW_COLORIZE = true;
    /** Indicates if source view should be parsed. */
    public static final boolean VIEW_PARSE = true;
    /** Indicates if source view should be maximized. */
    public static final boolean VIEW_MAXIMIZE = true;
    /** Indicates if source view should display line numbers. */
    public static final boolean VIEW_LINE_NUMBERS = true;
    /** Family of the source view font. */
    public static final String VIEW_FONT_FAMILY = "Monospaced";
    /** Size of the source view font. */
    public static final int VIEW_FONT_SIZE = 12;
    /** Width (in characters) of tabs in source view. */
    public static final int VIEW_TAB_WIDTH = 8;
    /** Show short method descriptions in the source view popup. */
    public static final boolean VIEW_POPUP_SHORT_METHOD_DESC = true;
    /** Keep source view tabs (if desktop mode is tabbed) in one row. */
    public static final boolean VIEW_SINGLE_ROW_TABS = false;
    /** How the source views are displayed. */
    public static final int VIEW_DESKTOP_TYPE
        = ViewDesktopFactory.MODE_IFRAMES;
    /** Timeout in seconds for remote method invocation. */
    public static final int INVOCATION_TIMEOUT = 5000;
    /** Indicates if Classic VM should be used. */
    public static final boolean USE_CLASSIC_VM = false;
    /** Indicates if class names should be shortened. */
    public static final boolean SHORT_CLASS_NAMES = true;
    /** Indicates if breakpoints should get *. prepended to classnames. */
    public static final boolean ADD_STAR_DOT = false;
    /** True if breakpoint should be set in main() method on launch. */
    public static final boolean STOP_ON_MAIN = false;
    /** Indicates if toolbar should be shown. */
    public static final boolean SHOW_TOOLBAR = true;
    /** Indicates if toolbar should use small buttons. */
    public static final boolean SMALL_TOOLBAR_BUTTONS = false;
    /** Indicates if threads panel should always expand all paths. */
    public static final boolean THREADS_EXPAND_ALL = true;
    /** Indicates if threads panel should hide zombie threads. */
    public static final boolean THREADS_HIDE_ZOMBIES = false;
    /** Table of keyboard shortcuts, keyed by the action command name. */
    public static final Hashtable KEYBOARD_SHORTS;
    /** Table of view colors, keyed by the color name. */
    public static final Hashtable VIEW_COLORS;
    /** The default debugging stratum (leave blank so the classes can
     * provide the stratum for us). */
    public static final String STRATUM = "";

    static {
        // Initialize the shortcuts table.
        KEYBOARD_SHORTS = new Hashtable();
        KEYBOARD_SHORTS.put("keys.step", "F11");
        KEYBOARD_SHORTS.put("keys.next", "F12");
        KEYBOARD_SHORTS.put("keys.finish", "F2");
        KEYBOARD_SHORTS.put("keys.vmStart", "ctrl S");
        KEYBOARD_SHORTS.put("keys.vmSuspend", "F4");
        KEYBOARD_SHORTS.put("keys.vmResume", "F5");
        KEYBOARD_SHORTS.put("keys.setBreak", "F9");
        //KEYBOARD_SHORTS.put("keys.listBreak", "F7");
        KEYBOARD_SHORTS.put("keys.find", "ctrl F");
        KEYBOARD_SHORTS.put("keys.findAgain", "ctrl G");
        KEYBOARD_SHORTS.put("keys.refresh", "F3");
        KEYBOARD_SHORTS.put("keys.helpIndex", "F1");
        KEYBOARD_SHORTS.put("keys.openFile", "ctrl O");
        KEYBOARD_SHORTS.put("keys.evaluate", "ctrl E");
        KEYBOARD_SHORTS.put("keys.vmAttach", "ctrl A");
        KEYBOARD_SHORTS.put("keys.gotoLine", "ctrl L");

        // Initialize the source colors table.
        VIEW_COLORS = new Hashtable();
        VIEW_COLORS.put("colors.character", "0x00b200");
        VIEW_COLORS.put("colors.comment", "0xff0000");
        VIEW_COLORS.put("colors.highlight", "0x8080ff");
        VIEW_COLORS.put("colors.identifier", "0x404040");
        VIEW_COLORS.put("colors.keyword", "0x0000ff");
        VIEW_COLORS.put("colors.literal", "0x00b2b2");
        VIEW_COLORS.put("colors.number", "0xb27a7a");
        VIEW_COLORS.put("colors.primitive", "0xb2b200");
        VIEW_COLORS.put("colors.string", "0x00b200");
    }

    /**
     * This class cannot be instantiated.
     */
    private Defaults() {
        // None shall construct us.
    } // Defaults
} // Defaults
