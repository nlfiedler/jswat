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
 * MODULE:      JSwat UI
 * FILE:        UIAdapter.java
 *
 * AUTHOR:      Nathan Fiedler
 *
 * REVISION HISTORY:
 *      Name    Date            Description
 *      ----    ----            -----------
 *      nf      08/07/01        Initial version
 *
 * $Id: UIAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.view.View;
import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * Interface UIAdapter connects the Session with the user interface of
 * JSwat. It builds out the major interface components, connects them to
 * the Session and managers, and handles most user input.
 *
 * @author  Nathan Fiedler
 */
public interface UIAdapter {
    /** A notice type of message. This is typically displayed in a label at
     * the bottom of the application window. */
    public static final int MESSAGE_NOTICE = 1;
    /** A warning type of message. This may be displayed in the same label
     * area as with notices, or may be displayed in a dialog box. */
    public static final int MESSAGE_WARNING = 2;
    /** An error type of message. This is typically displayed in a dialog
     * box. */
    public static final int MESSAGE_ERROR = 3;

    /**
     * Add a PropertyChangeListener to the listener list. The listener is
     * registered for all properties.
     *
     * @param  listener  the PropertyChangeListener to be added.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Add a PropertyChangeListener for a specific property. The listener
     * will be invoked only when a call on firePropertyChange names that
     * specific property.
     *
     * @param  propertyName  the name of the property to listen on.
     * @param  listener      the PropertyChangeListener to be added.
     */
    void addPropertyChangeListener(String propertyName,
                                   PropertyChangeListener listener);

    /**
     * In a graphical environment, bring the primary debugger window
     * forward so the user can see it. This is called primarily when a
     * debugger event has occurred and the debugger may be hidden behind
     * the debuggee application window.
     */
    void bringForward();

    /**
     * Construct the appropriate user interface and connect all the pieces
     * together. The result should be a fully functional interface that is
     * ready to be used.
     */
    void buildInterface();

    /**
     * Indicate if this interface adapter has the ability to find a string
     * in the currently selected source view.
     *
     * @return  true if the ability exists, false otherwise.
     */
    boolean canFindString();

    /**
     * Indicate if this interface adapter has the ability to show source
     * files in a manner appropriate for the user to read.
     *
     * @return  true if the ability exists, false otherwise.
     */
    boolean canShowFile();

    /**
     * Deconstruct the user interface such that all components are made
     * invisible and prepared for non-use.
     */
    void destroyInterface();

    /**
     * This is called when there are no more open Sessions. The adapter
     * should take the appropriate action at this time. In most cases that
     * will be to exit the JVM.
     */
    void exit();

    /**
     * Search for the given string in the currently selected source view.
     * The search should continue from the last successful match, and wrap
     * around to the beginning when the end is reached.
     *
     * @param  query       string to look for.
     * @param  ignoreCase  true to ignore case.
     * @return  true if string was found.
     * @throws  NoOpenViewException
     *          if there is no view to be searched.
     */
    boolean findString(String query, boolean ignoreCase)
        throws NoOpenViewException;

    /**
     * Searches for the property with the specified key in the property
     * list. The method returns null if the property is not found.
     *
     * @param  key  the property key.
     * @return  the value in the property list with the specified key value.
     */
    Object getProperty(String key);

    /**
     * Retrieves the currently active view in JSwat.
     *
     * @return  selected view, or null if none selected.
     */
    View getSelectedView();

    /**
     * Perform any initialization that requires a Session instance. This is
     * called after the object is constructed and before
     * <code>buildInterface()</code> is called.
     *
     * @param  session  session to associate with.
     */
    void init(Session session);

    /**
     * Called when the Session initialization has completed.
     */
    void initComplete();

    /**
     * Refresh the display to reflect changes in the program. Generally
     * this means refreshing the panels.
     */
    void refreshDisplay();

    /**
     * Remove a PropertyChangeListener from the listener list. This removes
     * a PropertyChangeListener that was registered for all properties.
     *
     * @param  listener  the PropertyChangeListener to be removed.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove a PropertyChangeListener for a specific property.
     *
     * @param  propertyName  the name of the property that was listened on.
     * @param  listener      the PropertyChangeListener to be removed.
     */

    void removePropertyChangeListener(String propertyName,
                                      PropertyChangeListener listener);

    /**
     * Save any settings to the appropriate places, the program is about to
     * terminate.
     */
    void saveSettings();

    /**
     * Stores the given value in the properties list with the given key as
     * a reference. If the value is null, then the key and value will be
     * removed from the properties.
     *
     * @param  key    the key to be placed into this property list.
     * @param  value  the value corresponding to key, or null to remove
     *                the key and value from the properties.
     * @return  previous value stored using this key.
     */
    Object setProperty(String key, Object value);

    /**
     * Show the given file in the appropriate view and make the given line
     * visible in that view.
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
    boolean showFile(SourceSource src, int line, int count);

    /**
     * Show a help screen written in HTML. This is may be implemented like
     * the <code>showURL()</code> method, but should have buttons for
     * navigating the help content.
     *
     * @param  url  help screen to be shown to the user.
     */
    void showHelp(URL url);

    /**
     * Show a message in an appropriate location.
     *
     * @param  type  one of the message types defined in this class.
     * @param  msg   message to be shown to the user.
     */
    void showMessage(int type, String msg);

    /**
     * Show a URL in a reasonable manner. This will likely involve using a
     * <code>JEditorPane</code> or some similar class to display the file
     * referenced by the <code>URL</code>.
     *
     * @param  url    URL to be shown to the user.
     * @param  title  title for the window showing the URL, if any.
     */
    void showURL(URL url, String title);

    /**
     * Change the prompt displayed beside the command input field.
     *
     * @param  prompt  new input prompt, or null to display default.
     */
    void updateInputPrompt(String prompt);
} // UIAdapter
