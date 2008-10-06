/*********************************************************************
 *
 *      Copyright (C) 2002 Nathan Fiedler
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
 * MODULE:      Unit Tests
 * FILE:        DummyUIAdapter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      07/30/02        Initial version
 *
 * $Id: DummyUIAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.view.View;
import java.net.URL;
import java.util.Hashtable;

/**
 * An empty implementation of the UIAdapter interface.
 *
 * @author  Nathan Fiedler
 */
public class DummyUIAdapter extends AbstractAdapter {

    /**
     * Constructor for DummyUIAdapter class.
     */
    public DummyUIAdapter() {
        super();
    } // DummyUIAdapter

    /**
     * In a graphical environment, bring the primary debugger window
     * forward so the user can see it. This is called primarily when a
     * debugger event has occurred and the debugger may be hidden behind
     * the debuggee application window.
     */
    public void bringForward() {
    } // bringForward

    /**
     * Construct the appropriate user interface and connect all the
     * pieces together. The result should be a fully functional
     * interface that is ready to be used.
     */
    public void buildInterface() {
    } // buildInterface

    /**
     * Indicate if this interface adapter has the ability to find a
     * string in the currently selected source view.
     *
     * @return  true if the ability exists, false otherwise.
     */
    public boolean canFindString() {
        return false;
    } // canFindString

    /**
     * Indicate if this interface adapter has the ability to show source
     * files in a manner appropriate for the user to read.
     *
     * @return  true if the ability exists, false otherwise.
     */
    public boolean canShowFile() {
        return false;
    } // canShowFile

    /**
     * Deconstruct the user interface such that all components are made
     * invisible and prepared for non-use.
     */
    public void destroyInterface() {
    } // destroyInterface

    /**
     * This is called when there are no more open Sessions. The adapter
     * should take the appropriate action at this time. In most cases
     * that will be to exit the JVM.
     */
    public void exit() {
    } // exit

    /**
     * Search for the given string in the currently selected source
     * view. The search should continue from the last successful match,
     * and wrap around to the beginning when the end is reached.
     *
     * @param  query       string to look for.
     * @param  ignoreCase  true to ignore case.
     * @return  true if string was found.
     * @throws  NoOpenViewException
     *          if there is no view to be searched.
     */
    public boolean findString(String query, boolean ignoreCase)
        throws NoOpenViewException {
        throw new NoOpenViewException("unsupported operation");
    } // findString

    /**
     * Retrieves the currently active view in JSwat.
     *
     * @return  selected view, or null if none selected.
     */
    public View getSelectedView() {
        return null;
    } // getSelectedView

    /**
     * Perform any initialization that requires a Session instance. This is
     * called after the object is constructed and before
     * <code>buildInterface()</code> is called.
     *
     * @param  session  session to associate with.
     */
    public void init(Session session) {
        // we ignore the session
    } // init

    /**
     * Called when the Session initialization has completed.
     */
    public void initComplete() {
    } // initComplete

    /**
     * Refresh the display to reflect changes in the program. Generally
     * this means refreshing the panels.
     */
    public void refreshDisplay() {
    } // refreshDisplay

    /**
     * Save any settings to the appropriate places, the program is about
     * to terminate.
     */
    public void saveSettings() {
    } // saveSettings

    /**
     * Show the given file in the appropriate view and make the given
     * line visible in that view.
     *
     * @param  src    source to be displayed.
     * @param  line   one-based line to be made visible, or zero for
     *                a reasonable default.
     * @param  count  number of lines to display, or zero for a
     *                reasonable default. Some adapters will ignore
     *                this value if, for instance, they utilize a
     *                scrollable view.
     * @return  true if successful, false if error.
     */
    public boolean showFile(SourceSource src, int line, int count) {
        throw new UnsupportedOperationException();
    } // showFile

    /**
     * Show a help screen written in HTML. This is may be implemented
     * like the <code>showURL()</code> method, but should have buttons
     * for navigating the help content.
     *
     * @param  url  help screen to be shown to the user.
     */
    public void showHelp(URL url) {
    } // showHelp

    /**
     * Show a message in an appropriate location.
     *
     * @param  type  one of the message types defined in this class.
     * @param  msg   message to be shown to the user.
     */
    public void showMessage(int type, String msg) {
    } // showMessage

    /**
     * Show a URL in a reasonable manner. This will likely involve using
     * a <code>JEditorPane</code> or some similar class to display the
     * file referenced by the <code>URL</code>.
     *
     * @param  url    URL to be shown to the user.
     * @param  title  title for the window showing the URL, if any.
     */
    public void showURL(URL url, String title) {
    } // showURL

    /**
     * Change the prompt displayed beside the command input field.
     *
     * @param  prompt  new input prompt, or null to display default.
     */
    public void updateInputPrompt(String prompt) {
    } // updateinputprompt
} // DummyUIAdapter
