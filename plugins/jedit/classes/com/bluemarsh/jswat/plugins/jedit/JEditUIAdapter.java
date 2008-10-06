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
 * $Id: JEditUIAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.plugins.jedit;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.command.CommandManager;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.panel.Panel;
import com.bluemarsh.jswat.panel.PanelFactory;
import com.bluemarsh.jswat.ui.AbstractAdapter;
import com.bluemarsh.jswat.ui.Bundle;
import com.bluemarsh.jswat.ui.EditPopup;
import com.bluemarsh.jswat.ui.SessionActionAdapter;
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import com.bluemarsh.jswat.ui.StartupRunner;
import com.bluemarsh.jswat.ui.graphical.GraphicalCommandAdapter;
import com.bluemarsh.jswat.ui.graphical.GraphicalInputAdapter;
import com.bluemarsh.jswat.ui.graphical.GraphicalMessageAdapter;
import com.bluemarsh.jswat.ui.graphical.GraphicalOutputAdapter;
import com.bluemarsh.jswat.ui.graphical.UrlViewer;
import com.bluemarsh.jswat.util.Strings;
import com.bluemarsh.jswat.view.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

/**
 * Class JEditUIAdapter connects the Session with the user interface of
 * JSwat. It builds the major interface components, connects them to the
 * Session and managers, and handles some user input. This subclass of
 * the <code>UIAdapter</code> class builds a graphical interface using
 * jEdit buffers and dockable windows.
 *
 * @author  David Taylor
 * @author  Stefano Maestri
 * @author  Nathan Fiedler
 * @author  Dirk Moebius
 */
public class JEditUIAdapter extends AbstractAdapter {
    /** Handles the intput to the target VM. */
    private GraphicalInputAdapter inputAdapter;
    /** Handles the status log display. */
    private GraphicalMessageAdapter messageAdapter;
    /** Handles the output from the target VM. */
    private GraphicalOutputAdapter outputAdapter;
    /** Handles session events to update the toolbar components */
    private SessionActionAdapter sessionActionAdapter;
    /** Vertical window splitter. */
    private JSplitPane vertSplitter;
    /** Top-most panel that contains everything. */
    private JPanel sessionPanel;
    /** List of all panels we've created. Used to refresh them when
     * needed. */
    private List panelList;
    /** The top tabbed pane, containing the threads, classes, locals and
     * watches panels. */
    private JTabbedPane topTabbedPane;
    /** The bottom tabbed pane, containing the messages, output, breakpoint,
     * stack and methods panels. */
    private JTabbedPane bottomTabbedPane;
    /** The "Command:" input label. */
    private JLabel commandLabel;
    /** Log to which messages are printed. */
    private com.bluemarsh.jswat.Log statusLog;
    /** Session we are associated with. */
    private Session ourSession;
    /** The jEdit View this adapter is associated with. */
    private org.gjt.sp.jedit.View jeditView;
    /** The JSwat View delivered by getSelectedView(). */
    private JEditSourceView jswatView;
    /** Preferences node for this interface adapter. */
    private Preferences preferences;
    /** A session listener for this interface adapter. */
    private JEditUISessionListener ourSessionListener;
    /** True if the containing frame of the panel has been registered. */
    private boolean isFrameRegistered;

    /**
     * Constructs a JEditUIAdapter.
     */
    public JEditUIAdapter() {
        super();
        panelList = new LinkedList();
        preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/plugins/jedit");
        jswatView = new JEditSourceView();
        isFrameRegistered = false;
    } // JEditUIAdapter

    /**
     * In a graphical environment, bring the primary debugger window
     * forward so the user can see it. This is called primarily when a
     * debugger event has occurred and the debugger may be hidden behind
     * the debuggee application window.
     */
    public void bringForward() {
        if (sessionPanel != null) {
            Frame frame = SessionFrameMapper.getOwningFrame(sessionPanel);
            if (frame != null) {
                frame.toFront();
            }
        }
    } // bringForward

    /**
     * Create the panel to contain the message panel and
     * command input fields.
     *
     * @param  tabbedPane  Tabbed pane to add command panel to.
     */
    protected void buildCommand(JTabbedPane tabbedPane) {
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        JPanel commandPanel = new JPanel(gb);

        // Message text area for the status log.
        messageAdapter = new GraphicalMessageAdapter();
        messageAdapter.init(statusLog);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gb.setConstraints(messageAdapter.getUI(), gc);
        commandPanel.add(messageAdapter.getUI());

        // "Command:" input label.
        commandLabel = new JLabel(Bundle.getString("commandField"));
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0.0;
        gc.weighty = 0.0;
        gc.gridwidth = GridBagConstraints.RELATIVE;
        gb.setConstraints(commandLabel, gc);
        commandPanel.add(commandLabel);

        // Command input field.
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        JTextField inputField = new JTextField();
        Font font = new Font("Monospaced", Font.PLAIN, 12);
        inputField.setFont(font);

        // Set up the edit popup menu.
        EditPopup popup = new EditPopup(inputField, true, false);
        inputField.add(popup);
        inputField.addMouseListener(popup);

        CommandManager cmdman = (CommandManager)
            ourSession.getManager(CommandManager.class);
        new GraphicalCommandAdapter(inputField, commandLabel, cmdman,
                                    statusLog);
        gb.setConstraints(inputField, gc);
        commandPanel.add(inputField);
        tabbedPane.addTab(Bundle.getString("messagesTab"), commandPanel);
    } // buildCommand

    /**
     * Construct the appropriate user interface and connect all the
     * pieces together. The result should be a fully functional
     * interface that is ready to be used.
     */
    public void buildInterface() {
        // Construct the top-level panel that will hold everything.
        sessionPanel = new JPanel(new BorderLayout()) {
                // Overloaded to be notified when the sessionPanel is
                // displayed.
                public void addNotify() {
                    super.addNotify();
                    // HACK: register the frame where the sessionPanel
                    // is displayed in. We need the frame so that JSwat
                    // can invoke its actions.
                    if (ourSession != null) {
                        Frame frame = SessionFrameMapper.getOwningFrame(this);
                        if (frame != null) {
                            SessionFrameMapper.addFrameSessionMapping(
                                frame, ourSession);
                            isFrameRegistered = true;
                        }
                    }
                }
            };

        // Make the toolbar which provides the essential functionality.
        sessionActionAdapter = new SessionActionAdapter();
        ourSession.addListener(sessionActionAdapter);
        JToolBar toolbar = new ToolbarCreator().createToolbar(
            sessionActionAdapter);
        sessionPanel.add(toolbar, BorderLayout.NORTH);

        // Add the central panel to which most of the UI will be added.
        JPanel mainPanel = new JPanel(new BorderLayout());
        sessionPanel.add(mainPanel, BorderLayout.CENTER);

        // Create the top tabbed pane for half of the panels.
        topTabbedPane = new JTabbedPane();
        PanelFactory panelFactory = PanelFactory.getInstance();
        buildPanel(Bundle.getString("threadTab"), topTabbedPane,
                   PanelFactory.PANEL_THREADS, panelFactory);
        buildPanel(Bundle.getString("classTab"), topTabbedPane,
                   PanelFactory.PANEL_CLASSES, panelFactory);
        buildPanel(Bundle.getString("localsTab"), topTabbedPane,
                   PanelFactory.PANEL_LOCALS, panelFactory);
        buildPanel(Bundle.getString("watchTab"), topTabbedPane,
                   PanelFactory.PANEL_WATCHES, panelFactory);

        // Create the bottom tabbed pane for the other half of the panels.
        bottomTabbedPane = new JTabbedPane();
        buildCommand(bottomTabbedPane);
        buildStandard(bottomTabbedPane);

        // Create the breakpoints panel.
        buildPanel(Bundle.getString("breakTab"), bottomTabbedPane,
                   PanelFactory.PANEL_BREAKPOINTS, panelFactory);

        buildPanel(Bundle.getString("stackTab"), bottomTabbedPane,
                   PanelFactory.PANEL_STACK, panelFactory);

        // Create the panel that displays class methods.
        buildPanel(Bundle.getString("methodsTab"), bottomTabbedPane,
                   PanelFactory.PANEL_METHODS, panelFactory);

        // Splitter for top and bottom portions of screen.
        vertSplitter = new JSplitPane(getSplitOrientation(), false,
                                      topTabbedPane, bottomTabbedPane);
        vertSplitter.setOneTouchExpandable(true);

        int pos = preferences.getInt("vertSplitter", 250);
        vertSplitter.setDividerLocation(pos);

        // Add all that stuff to the central panel.
        mainPanel.add(vertSplitter, BorderLayout.CENTER);

        // Add our session listener
        ourSessionListener = new JEditUISessionListener();
        ourSession.addListener(ourSessionListener);
    } // buildInterface

    /**
     * Builds the panel of the given name, adding it into the interface.
     *
     * @param  label    textual label for this panel.
     * @param  pane     tabbed pane to add to.
     * @param  name     name of the panel to create.
     * @param  factory  factory to create the panel.
     */
    protected void buildPanel(String label, JTabbedPane pane,
                              String name, PanelFactory factory) {
        try {
            Panel panel = factory.get(name, ourSession);
            panelList.add(panel);
            pane.addTab(label, panel.getUI());
        } catch (Exception e) {
            // All sorts of things could go wrong.
            String msg = MessageFormat.format(
                Bundle.getString("panelBuildFailed"),
                new Object[] { name, e });
            statusLog.writeln(msg);
        }
    } // buildPanel

    /**
     * Create the panel to contain the stdout, stderr, and
     * stdin fields.
     *
     * @param  tabbedPane  Tabbed pane to add command panel to.
     */
    protected void buildStandard(JTabbedPane tabbedPane) {
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        JPanel standardPanel = new JPanel(gb);

        // stdout/stderr text area.
        outputAdapter = new GraphicalOutputAdapter();
        ourSession.addListener(outputAdapter);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gb.setConstraints(outputAdapter.getUI(), gc);
        standardPanel.add(outputAdapter.getUI());

        // "Input:" input label.
        JLabel inputLabel = new JLabel(Bundle.getString("inputField"));
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0.0;
        gc.weighty = 0.0;
        gc.gridwidth = GridBagConstraints.RELATIVE;
        gb.setConstraints(inputLabel, gc);
        standardPanel.add(inputLabel);

        // stdin input field.
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        inputAdapter = new GraphicalInputAdapter(
            outputAdapter.getOutputArea());
        ourSession.addListener(inputAdapter);
        gb.setConstraints(inputAdapter.getUI(), gc);
        standardPanel.add(inputAdapter.getUI());
        tabbedPane.addTab(Bundle.getString("standardTab"), standardPanel);
    } // buildStandard

    /**
     * Indicate if this interface adapter has the ability to find a
     * string in the currently selected source view.
     *
     * @return  always returns true.
     */
    public boolean canFindString() {
        return true;
    } // canFindString

    /**
     * Indicate if this interface adapter has the ability to show source
     * files in a manner appropriate for the user to read.
     *
     * @return  always returns true.
     */
    public boolean canShowFile() {
        return true;
    } // canShowFile

    /**
     * Deconstruct the user interface such that all components are made
     * invisible and prepared for non-use.
     */
    public void destroyInterface() {
        // Remove all of the panels from the Session.
        Iterator iter = panelList.iterator();

        while (iter.hasNext()) {
            Panel panel = (Panel) iter.next();
            if (panel instanceof SessionListener) {
                ourSession.removeListener((SessionListener) panel);
            }
        }

        // Remove some more listeners from the session.
        ourSession.removeListener(outputAdapter);
        ourSession.removeListener(inputAdapter);
        ourSession.removeListener(sessionActionAdapter);
        ourSession.removeListener(ourSessionListener);

        // Deregister the JEditSourceView instance as a ContextListener
        ContextManager cmgr = (ContextManager)
            ourSession.getManager(ContextManager.class);
        cmgr.removeContextListener(jswatView);

        // Destroy the status log adapter.
        messageAdapter.destroy(statusLog);
    } // destroyInterface

    /**
     * This is called when there are no more open Sessions. The adapter
     * should take the appropriate action at this time. In most cases
     * that will be to call <code>System.exit()</code> to exit the JVM.
     */
    public void exit() {
        // do nothing
    } // exit

    /**
     * Return the split orientation of the main panel.
     *
     * @return  either <code>JSplitPane.HORIZONTAL_SPLIT</code> or
     *          <code>JSplitPane.VERTICAL_SPLIT</code> depending on where
     *          the main panel is docked.
     */
    private int getSplitOrientation() {
        String dockPosition = jEdit.getProperty(JSwatPlugin.NAME
            + ".dock-position", DockableWindowManager.LEFT);
        if (dockPosition.equals(DockableWindowManager.TOP) ||
            dockPosition.equals(DockableWindowManager.BOTTOM)) {
            return JSplitPane.HORIZONTAL_SPLIT;
        } else {
            return JSplitPane.VERTICAL_SPLIT;
        }
    } // getSplitOrientation

    /**
     * Search for the given string in the currently selected source
     * view. The search should continue from the last successful match,
     * and wrap around to the beginning when the end is reached.
     *
     * @param  query       string to look for.
     * @param  ignoreCase  true to ignore case.
     * @return  true if string was found.
     */
    public boolean findString(String query, boolean ignoreCase) {
        // Find the currently active source view, if any.
        View view = (View) getSelectedView();
        if (view != null) {
            return view.findString(query, ignoreCase);
        }
        return false;
    } // findString

    /**
     * Retrieves the currently active view in JSwat. In this case, it is
     * of type <code>JEditSourceView</code>.
     *
     * @return  selected view, or null if none selected.
     */
    public View getSelectedView() {
        return jswatView;
    } // getSelectedView

    /**
     * Returns the single panel which contains everything.
     *
     * @return  our top-most interface component.
     */
    public JPanel getPanel() {
        return sessionPanel;
    } // getPanel

    /**
     * Returns whether the frame containing the panel has been registered.
     * This happens when the panel is being displayed.
     *
     * @return  true if the frame has been registered.
     */
    public boolean isFrameRegistered() {
        return isFrameRegistered;
    } // isFrameRegistered

    /**
     * Perform any initialization that requires a Session instance. This
     * is called after the object is constructed and before
     * <code>buildInterface()</code> is called.
     *
     * @param  session  session to associate with.
     */
    public void init(Session session) {
        ourSession = session;
        jswatView.setSession(ourSession);
        statusLog = session.getStatusLog();
        statusLog.start();
    } // init

    /**
     * Called when the Session initialization has completed.
     */
    public void initComplete() {
        // Cause the panels to refresh in the event they display
        // data just after starting up.
        refreshDisplay();

        // Have the command manager process any startup files.
        CommandManager cmdman = (CommandManager)
            ourSession.getManager(CommandManager.class);
        String err = StartupRunner.runRCFiles(cmdman);
        if (err != null) {
            statusLog.writeln(err);
        }

        // Register the JEditSourceView instance as a ContextListener
        ContextManager cmgr = (ContextManager)
            ourSession.getManager(ContextManager.class);
        cmgr.addContextListener(jswatView);

        // Show our initial message in the message area.
        statusLog.writeln(Bundle.getString("initialMsg"));
    } // initComplete

    /**
     * Called when the jEdit properties have been changed.
     */
    public void propertiesChanged() {
        vertSplitter.setOrientation(getSplitOrientation());
        saveSettings();
    } // propertiesChanged

    /**
     * Refresh the display to reflect changes in the program.
     * Generally this means refreshing the panels.
     */
    public void refreshDisplay() {
        Iterator iter = panelList.iterator();
        while (iter.hasNext()) {
            Panel panel = (Panel) iter.next();
            panel.refreshLater();
        }
    } // refreshDisplay

    /**
     * Save any settings to the appropriate places, the program
     * is about the terminate.
     */
    public void saveSettings() {
        preferences.putInt("vertSplitter", vertSplitter.getDividerLocation());
    } // saveSettings

    /**
     * Saves the jEdit view reference for our use later on.
     *
     * @param  view  the associated view.
     */
    public void setView(org.gjt.sp.jedit.View view) {
        this.jeditView = view;
        this.jswatView.setView(view);
    } // setView

    /**
     * Return the jEdit view associated with this ui adapter instance.
     *
     * @return  the associated view.
     */
    public org.gjt.sp.jedit.View getView() {
        return jeditView;
    } // getView

    /**
     * Show the given file in the appropriate view and make the
     * given line visible in that view.
     *
     * @param  src    source to be displayed.
     * @param  line   line to be made visible.
     * @param  count  number of lines to show.
     * @return  true if successful, false if error.
     */
    public boolean showFile(SourceSource src, int line, int count) {
        // This is used by the stack panel to make a particular
        // line of code visible in the source view.
        if (line == 0) {
            line = 1;
        }

        // Show file and scroll to line
        try {
            jswatView.refresh(src, line);
        } catch (IOException ioe) {
            statusLog.writeln("Could not open source file "
                              + src.getLongName() + ": " + ioe);
            return false;
        }

        return true;
    } // showFile

    /**
     * Show a help screen written in HTML. This is may be implemented
     * like the <code>showURL()</code> method, but should have buttons
     * for navigating the help content.
     *
     * @param  url  help screen to be shown to the user.
     */
    public void showHelp(URL url) {
        UrlViewer.showHelp(url, jeditView);
    } // showHelp

    /**
     * Show a message in an appropriate location.
     *
     * @param  type  one of the message types defined in this class.
     * @param  msg   message to be shown to the user.
     */
    public void showMessage(int type, String msg) {
        // Note: we may be called when jeditView is null.
        if (type == MESSAGE_NOTICE || type == MESSAGE_WARNING) {
            // For the sake of event history, write it to the log.
            statusLog.writeln(msg);

        } else if (type == MESSAGE_ERROR) {
            Object message = msg;
            if (msg.indexOf('\n') >= 0 || msg.length() > 80) {
                // Multi-line messages must be split.
                String[] arr = Strings.splitOnNewline(msg);
                boolean longLines = false;
                for (int ii = 0; ii < arr.length; ii++) {
                    if (arr[ii].length() > 80) {
                        longLines = true;
                        break;
                    }
                }

                if (longLines) {
                    // Show the message in a scrollable area.
                    JTextArea textArea = new JTextArea(msg);
                    textArea.setEditable(false);
                    JScrollPane scroller = new JScrollPane(textArea);
                    scroller.setPreferredSize(new Dimension(400, 200));
                    message = scroller;
                } else {
                    message = arr;
                }
            }
            JOptionPane.showMessageDialog(
                jeditView, message, Bundle.getString("Error.title"),
                JOptionPane.ERROR_MESSAGE);
        }
    } // showMessage

    /**
     * Show the messages pane. Bring it to front if it is currently not being
     * displayed.
     */
    public void showMessagesPane() {
        if (sessionPanel != null) {
            bringForward();
            bottomTabbedPane.setSelectedIndex(0);
        }
    }

    /**
     * Show a URL in a reasonable manner. This will likely involve using
     * a <code>JEditorPane</code> or some similar class to display the
     * file referenced by the <code>URL</code>.
     *
     * @param  url    URL to be shown to the user.
     * @param  title  title for the window showing the URL, if any.
     */
    public void showURL(URL url, String title) {
        UrlViewer.showURL(url, title, jeditView);
    } // showURL

    /**
     * Change the prompt displayed beside the command input field.
     *
     * @param  prompt  new input prompt, or null to display default.
     */
    public void updateInputPrompt(String prompt) {
        commandLabel.setText((prompt != null ? prompt
                              : Bundle.getString("commandField")) + ": ");
    } // updateInputPrompt

    /**
     * A listener interface for receiving Session events that configures
     * some aspects of this UIAdapter.
     */
    private class JEditUISessionListener implements SessionListener {
        /**
         * Called when the Session has activated. This occurs when the
         * debuggee has launched or has been attached to the debugger.
         * This implementation does nothing.
         *
         * @param  sevt  session event.
         */
        public void activated(SessionEvent sevt) {
        }

        /**
         * Called when the Session is about to be closed.
         * This implementation does nothing.
         *
         * @param  sevt  session event.
         */
        public void closing(SessionEvent sevt) {
        }

        /**
         * Called when the Session has deactivated. The debuggee VM is no
         * longer connected to the Session.
         * This implementation removes all current line highlighters.
         *
         * @param  sevt  session event.
         */
        public void deactivated(SessionEvent sevt) {
            CurrentLineHighlight.removeAllHighlighters();
        }

        /**
         * Called after the Session has added this listener to the Session
         * listener list.
         * This implementation does nothing.
         *
         * @param  session  the Session.
         */
        public void opened(Session session) {
        }

        /**
         * Called when the debuggee is about to be resumed.
         * This implementation removes all current line highlighters.
         *
         * @param  sevt  session event.
         */
        public void resuming(SessionEvent sevt) {
            CurrentLineHighlight.removeAllHighlighters();
        }

        /**
         * Called when the debuggee has been suspended.
         * This implementation does nothing.
         *
         * @param  sevt  session event.
         */
        public void suspended(SessionEvent sevt) {
        }
    } // SessionUIAdapter
} // JEditUIAdapter
