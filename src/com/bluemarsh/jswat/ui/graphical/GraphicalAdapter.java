/*********************************************************************
 *
 *      Copyright (C) 2001-2004 Nathan Fiedler
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
 * $Id: GraphicalAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.Defaults;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Main;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.command.CommandManager;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.panel.Panel;
import com.bluemarsh.jswat.panel.PanelFactory;
import com.bluemarsh.jswat.ui.AbstractAdapter;
import com.bluemarsh.jswat.ui.Bundle;
import com.bluemarsh.jswat.ui.EditPopup;
import com.bluemarsh.jswat.ui.NoOpenViewException;
import com.bluemarsh.jswat.ui.SessionActionAdapter;
import com.bluemarsh.jswat.ui.SessionFrameMapper;
import com.bluemarsh.jswat.ui.StartupRunner;
import com.bluemarsh.jswat.util.Strings;
import com.bluemarsh.jswat.view.View;
import com.bluemarsh.jswat.view.ViewDesktop;
import com.bluemarsh.jswat.view.ViewDesktopFactory;
import com.bluemarsh.jswat.view.ViewException;
import com.bluemarsh.jswat.view.ViewFactory;
import com.bluemarsh.jswat.view.ViewManager;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

/**
 * Class GraphicalAdapter connects the Session with the user interface
 * of JSwat. It builds out the major interface components, connects them
 * to the Session and managers, and handles some user input. This
 * subclass of the <code>UIAdapter</code> class builds out a graphical
 * interface made up of JFC components.
 *
 * @author  Nathan Fiedler
 */
public class GraphicalAdapter extends AbstractAdapter
    implements PreferenceChangeListener {
    /** User preferences for this package. */
    private Preferences preferences;
    /** Session we are associated with. */
    private Session ourSession;
    /** Log to which messages are printed. */
    private Log statusLog;
    /** Top-most frame that contains everything. */
    private MainWindow mainWindow;
    /** Top horizontal window splitter. */
    private JSplitPane topSplitter;
    /** Bottom horizontal window splitter. */
    private JSplitPane bottomSplitter;
    /** Vertical window splitter. */
    private JSplitPane vertSplitter;
    /** Component which holds the open views. */
    private ViewDesktop viewDesktop;
    /** Handles the status log display. */
    private GraphicalMessageAdapter messageAdapter;
    /** Handles the output from the debuggee VM. */
    private GraphicalOutputAdapter outputAdapter;
    /** Handles the intput to the debuggee VM. */
    private GraphicalInputAdapter inputAdapter;
    /** Handles the command input from the user. */
    private GraphicalCommandAdapter commandAdapter;
    /** Label for prompt beside the command input box */
    private JLabel commandLabel;
    /** List of all panels we've created. Used to refresh them
     * when needed. */
    private List panelList;
    /** Label used for showing the notice messages. */
    private JLabel noticeMessageLabel;

    /**
     * Constructs a GraphicalAdapter.
     */
    public GraphicalAdapter() {
        panelList = new LinkedList();
        preferences = Preferences.userRoot().node(
            "com/bluemarsh/jswat/ui/graphical");
        // Create this now in the event that showMessage() is called
        // before the interface is built out. The space prevents the
        // label from becoming miniscule.
        noticeMessageLabel = new JLabel(" ");
    } // GraphicalAdapter

    /**
     * In a graphical environment, bring the primary debugger window
     * forward so the user can see it. This is called primarily when
     * a debugger event has occurred and the debugger may be hidden
     * behind the debuggee application window.
     */
    public void bringForward() {
        mainWindow.toFront();
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
        commandLabel = new JLabel();
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
        inputField.addMouseListener(popup);

        CommandManager cmdman = (CommandManager)
            ourSession.getManager(CommandManager.class);
        commandAdapter = new GraphicalCommandAdapter(
            inputField, commandLabel, cmdman, statusLog);
        gb.setConstraints(inputField, gc);
        commandPanel.add(inputField);
        tabbedPane.addTab(Bundle.getString("messagesTab"), commandPanel);
    } // buildCommand

    /**
     * Construct the appropriate user interface and connect all
     * the pieces together. The result should be a fully
     * functional interface that is ready to be used.
     */
    public void buildInterface() {
        // Set the look & feel to what the user had last selected.
        // Do this before building out any of the user interface.
        try {
            String lafName = preferences.get("lookAndFeel", null);
            if (lafName != null && lafName.length() > 0) {
                UIManager.setLookAndFeel(lafName);
            } else {
                // If nothing saved, use cross-platform look & feel.
                UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
            }
        } catch (Exception e) {
            // Just plod along in the hopes that it's nothing serious.
        }

        // Start the status log flusher thread.
        statusLog.start(Thread.NORM_PRIORITY + 1);

        // Create the tabbedpane for some of the panels.
        JTabbedPane topTabbedPane = new JTabbedPane();
        PanelFactory panelFactory = PanelFactory.getInstance();
        buildPanel(Bundle.getString("threadTab"), topTabbedPane,
            PanelFactory.PANEL_THREADS, panelFactory);
        buildPanel(Bundle.getString("classTab"), topTabbedPane,
            PanelFactory.PANEL_CLASSES, panelFactory);
        buildPanel(Bundle.getString("localsTab"), topTabbedPane,
            PanelFactory.PANEL_LOCALS, panelFactory);
        buildPanel(Bundle.getString("watchTab"), topTabbedPane,
            PanelFactory.PANEL_WATCHES, panelFactory);

        // Create the desktop pane that will manage the multiple
        // internal source code windows.
        ViewManager vm = (ViewManager)
            ourSession.getManager(ViewManager.class);
        int mode = preferences.getInt("viewDesktopType",
                                      Defaults.VIEW_DESKTOP_TYPE);
        viewDesktop = ViewDesktopFactory.create(mode, vm);
        JMenu viewDesktopMenu = viewDesktop.getMenu();
        if (viewDesktopMenu != null) {
            SpecialMenuTable.addMenu("window", viewDesktopMenu);
        }

        // Splitter for tabbed pane and desktop pane.
        topSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
                                     topTabbedPane, viewDesktop.getUI());
        topSplitter.setOneTouchExpandable(true);
        int pos = preferences.getInt("topSplitter", 300);
        topSplitter.setDividerLocation(pos);

        // Create the tabbedpane for command and stdout/stdin panels.
        JTabbedPane leftTabbedPane = new JTabbedPane();
        buildCommand(leftTabbedPane);
        buildStandard(leftTabbedPane);

        // Create the breakpoints panel.
        buildPanel(Bundle.getString("breakTab"), leftTabbedPane,
            PanelFactory.PANEL_BREAKPOINTS, panelFactory);

        JTabbedPane rightTabbedPane = new JTabbedPane();
        // Splitter for messages/output and stack/methods panels.
        bottomSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                        false, leftTabbedPane,
                                        rightTabbedPane);
        bottomSplitter.setOneTouchExpandable(true);
        pos = preferences.getInt("bottomSplitter", 500);
        bottomSplitter.setDividerLocation(pos);

        buildPanel(Bundle.getString("stackTab"), rightTabbedPane,
            PanelFactory.PANEL_STACK, panelFactory);

        // Create the panel that displays class methods.
        buildPanel(Bundle.getString("methodsTab"), rightTabbedPane,
            PanelFactory.PANEL_METHODS, panelFactory);

        // Splitter for top and bottom portions of screen.
        vertSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false,
                                      topSplitter, bottomSplitter);
        vertSplitter.setOneTouchExpandable(true);
        pos = preferences.getInt("vertSplitter", 250);
        vertSplitter.setDividerLocation(pos);

        SessionActionAdapter saa = new SessionActionAdapter();
        ourSession.addListener(saa);

        // Instantiate this special menu with a reference to us.
        SpecialMenuTable.addMenu("openedFiles",
                                 new OpenedFilesMenu(ourSession));

        // Construct the window that will hold everything.
        try {
            mainWindow = new MainWindow("JSwat", saa);
        } catch (Exception e) {
            // Report the problem and return.
            showMessage(MESSAGE_ERROR, Strings.exceptionToString(e));
            return;
        }
        SessionFrameMapper.addFrameSessionMapping(mainWindow, ourSession);

        // Add all that stuff to the main window in the inner pane.
        Container pane = mainWindow.getInnerPane();
        pane.add(vertSplitter, BorderLayout.CENTER);

        // Add the message label at the bottom of the content pane.
        noticeMessageLabel.setBorder(BorderFactory.createBevelBorder(
            BevelBorder.LOWERED));
        // Make sure the label font is in the plain style.
        Font f = noticeMessageLabel.getFont();
        if (!f.isPlain()) {
            f = f.deriveFont(Font.PLAIN);
            noticeMessageLabel.setFont(f);
        }
        pane = mainWindow.getContentPane();
        pane.add(noticeMessageLabel, BorderLayout.SOUTH);

        // Set up the window close adapter.
        mainWindow.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    // Close the session.
                    Main.endSession(ourSession);
                }
            });

        // Show the window finally.
        Runnable runnable = new Runnable() {
                public void run() {
                    mainWindow.setVisible(true);
                }
            };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException ie) {
                // ignored
            } catch (InvocationTargetException ite) {
                // ignored
            }
        }
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
        JLabel inputLabel = new JLabel
            (Bundle.getString("inputField"));
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
        tabbedPane.addTab(Bundle.getString("standardTab"),
                          standardPanel);
    } // buildStandard

    /**
     * Indicate if this interface adapter has the ability to find
     * a string in the currently selected source view.
     *
     * @return  always returns true.
     */
    public boolean canFindString() {
        return true;
    } // canFindString

    /**
     * Indicate if this interface adapter has the ability to show
     * source files in a manner appropriate for the user to read.
     *
     * @return  always returns true.
     */
    public boolean canShowFile() {
        return true;
    } // canShowFile

    /**
     * Deconstruct the user interface such that all components
     * are made invisible and prepared for non-use.
     */
    public void destroyInterface() {
        preferences.removePreferenceChangeListener(this);

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

        // Destroy the status log adapter.
        messageAdapter.destroy(statusLog);

        // Stop the status log flusher thread.
        statusLog.stop();

        SessionFrameMapper.removeFrameSessionMapping(mainWindow);

        // Close the main window.
        mainWindow.close();
    } // destroyInterface

    /**
     * This is called when there are no more open Sessions. The adapter
     * should take the appropriate action at this time. In most cases that
     * will be to exit the JVM.
     */
    public void exit() {
        System.exit(0);
    } // exit

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
    public boolean findString(String query, boolean ignoreCase)
        throws NoOpenViewException {
        // Find the currently active source view, if any.
        View view = getSelectedView();
        if (view != null) {
            return view.findString(query, ignoreCase);
        } else {
            throw new NoOpenViewException("no selected view to search");
        }
    } // findString

    /**
     * Retrieves the currently active view in JSwat.
     *
     * @return  selected view, or null if none selected.
     */
    public View getSelectedView() {
        return viewDesktop.getSelectedView();
    } // getSelectedView

    /**
     * Perform any initialization that requires a Session instance. This is
     * called after the object is constructed and before
     * <code>buildInterface()</code> is called.
     *
     * @param  session  session to associate with.
     */
    public void init(Session session) {
        ourSession = session;
        statusLog = session.getStatusLog();
    } // init

    /**
     * Called when the Session initialization has completed.
     */
    public void initComplete() {
        // Set up the user preferences.
        setPreferences();
        preferences.addPreferenceChangeListener(this);

        // Set up the source view manager.
        ourSession.getManager(ViewManager.class);

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

        // Show our initial message in the message area.
        statusLog.writeln(Bundle.getString("initialMsg"));
    } // initComplete

    /**
     * This method gets called when a preference is added, removed or when
     * its value is changed.
     *
     * @param  evt  a PreferenceChangeEvent object describing the event
     *              source and the preference that has changed.
     */
    public void preferenceChange(PreferenceChangeEvent evt) {
        setPreferences();
    } // preferenceChange

    /**
     * Refresh the display to reflect changes in the program. Generally
     * this means refreshing the panels and source views.
     */
    public void refreshDisplay() {
        Iterator iter = panelList.iterator();
        while (iter.hasNext()) {
            Panel panel = (Panel) iter.next();
            panel.refreshLater();
        }
        ViewManager vm = (ViewManager)
            ourSession.getManager(ViewManager.class);
        vm.refreshViews();
    } // refreshDisplay

    /**
     * Save any settings to the appropriate places, the program is about to
     * terminate.
     */
    public void saveSettings() {
        // See if we should remember the main window geometry.
        boolean remember = preferences.getBoolean("rememberGeometry",
                                                  Defaults.REMEMBER_GEOMETRY);
        if (remember) {
            // Save main window position and size.
            preferences.putInt("windowTop", mainWindow.getY());
            preferences.putInt("windowLeft", mainWindow.getX());
            preferences.putInt("windowWidth", mainWindow.getWidth());
            preferences.putInt("windowHeight", mainWindow.getHeight());

            // Save the splitpane positions.
            preferences.putInt("topSplitter",
                               topSplitter.getDividerLocation());
            preferences.putInt("bottomSplitter",
                               bottomSplitter.getDividerLocation());
            preferences.putInt("vertSplitter",
                               vertSplitter.getDividerLocation());
        }
    } // saveSettings

    /**
     * Use the user preferences to modify our interface.
     */
    protected void setPreferences() {
        // Set the max line counts for the messages and output areas.
        int linecount = preferences.getInt("messageLines",
                                           Defaults.MESSAGE_LINES_SAVE);
        messageAdapter.setMaxLineCount(linecount);

        linecount = preferences.getInt("outputLines",
                                       Defaults.OUTPUT_LINES_SAVE);
        outputAdapter.setMaxLineCount(linecount);

        // Adjust the view desktop, if necessary.
        ViewManager vm = (ViewManager)
            ourSession.getManager(ViewManager.class);
        int mode = preferences.getInt("viewDesktopType",
                                      Defaults.VIEW_DESKTOP_TYPE);
        if (mode != viewDesktop.getMode()) {
            // Dispose of the old view desktop and remove its menu.
            topSplitter.remove(viewDesktop.getUI());
            JMenu menu = viewDesktop.getMenu();
            viewDesktop.dispose();
            if (menu != null) {
                mainWindow.removeSpecialMenu("window", menu);
            }
            // Create the new view desktop and add its menu.
            viewDesktop = ViewDesktopFactory.create(mode, vm);
            topSplitter.setRightComponent(viewDesktop.getUI());
            // Have to reset the divider location because it moves when
            // you add and remove the components.
            int pos = preferences.getInt("topSplitter", 300);
            topSplitter.setDividerLocation(pos);
            menu = viewDesktop.getMenu();
            if (menu != null) {
                mainWindow.addSpecialMenu("window", menu);
            }

            // Re-open all of the views that just disappeared.
            Enumeration views = vm.getViews();
            while (views.hasMoreElements()) {
                View view = (View) views.nextElement();
                try {
                    viewDesktop.addView(view);
                } catch (ViewException ve) {
                    ourSession.getUIAdapter().showMessage(
                        MESSAGE_WARNING,
                        "GraphicalAdapter.setPreferences() error: "
                        + ve.getCause());
                }
            }
        }
        viewDesktop.setPreferences();

        if (preferences.getBoolean("showToolbar", Defaults.SHOW_TOOLBAR)) {
            mainWindow.showToolbar();
        } else {
            mainWindow.hideToolbar();
        }

        mainWindow.validate();
    } // setPreferences

    /**
     * Show the given file in the appropriate view and make the
     * given line visible in that view.
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
    public boolean showFile(final SourceSource src, final int line,
                            int count) {
        if (!src.exists()) {
            return false;
        }

        Runnable runnable = new Runnable() {
                public void run() {
                    if (!showFile0(src, line)) {
                        throw new RuntimeException("showFile failed");
                    }
                }
            };
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                runnable.run();
            } catch (RuntimeException re) {
                statusLog.writeStackTrace(re);
                return false;
            }
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException ie) {
                return false;
            } catch (InvocationTargetException ite) {
                statusLog.writeStackTrace(ite);
                return false;
            }
        }
        return true;
    } // showFile

    /**
     * Show the given file in the appropriate view and make the
     * given line visible in that view.
     *
     * @param  src   source to be displayed.
     * @param  line  line to be made visible.
     * @return  true if successful, false if error.
     */
    protected boolean showFile0(SourceSource src, int line) {
        boolean success = true;
        String errorMsg = null;

        ViewManager vm = (ViewManager)
            ourSession.getManager(ViewManager.class);
        View view = vm.getView(src);
        if (view != null) {
            try {
                viewDesktop.selectView(view);
            } catch (ViewException ve) {
                errorMsg = "GraphicalAdapter.showFile() error: "
                    + ve.getCause();
            }
            view.scrollToLine(line);
        } else {

            // Show a busy cursor while we open the view.
            mainWindow.setCursor(Cursor.getPredefinedCursor(
                Cursor.WAIT_CURSOR));
            // Let the factory do the hard work.
            ViewFactory factory = ViewFactory.getInstance();
            view = factory.create(src);
            vm.addView(view, src);
            try {
                view.refresh(src, line);
            } catch (IOException ioe) {
                success = false;
                errorMsg = "GraphicalAdapter.showFile() error: " + ioe;
            }

            if (success) {
                try {
                    viewDesktop.addView(view);
                } catch (ViewException ve) {
                    success = false;
                    errorMsg = "GraphicalAdapter.showFile() error: "
                        + ve.getCause();
                }
            } else {
                vm.removeView(view);
            }
            mainWindow.setCursor(Cursor.getDefaultCursor());
        }

        // Not really a property, but it works the same.
        // Note that the values must be different for an event to fire.
        if (success) {
            getChangeSupport().firePropertyChange("fileOpened", null, src);
        } else {
            ourSession.getUIAdapter().showMessage(MESSAGE_WARNING, errorMsg);
        }
        return success;
    } // showFile0

    /**
     * Show a help screen written in HTML. This is may be implemented
     * like the <code>showURL()</code> method, but should have buttons
     * for navigating the help content.
     *
     * @param  url  help screen to be shown to the user.
     */
    public void showHelp(URL url) {
        UrlViewer.showHelp(url, mainWindow);
    } // showHelp

    /**
     * Show a message in an appropriate location.
     *
     * @param  type  one of the message types defined in this class.
     * @param  msg   message to be shown to the user.
     */
    public void showMessage(int type, String msg) {
        if (type == MESSAGE_ERROR) {
            msg = Bundle.getString("msg.error.prefix") + ' ' + msg;
        } else if (type == MESSAGE_WARNING) {
            msg = Bundle.getString("msg.warn.prefix") + ' ' + msg;
        }
        // Note: we may be called when mainWindow is null.
        if (type == MESSAGE_NOTICE || type == MESSAGE_WARNING) {
            // We are not dealing with long lines yet.
            if (msg.indexOf('\n') >= 0) {
                msg = msg.replace('\n', ' ');
            }
            noticeMessageLabel.setText(msg);
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
                mainWindow, message, Bundle.getString("Error.title"),
                JOptionPane.ERROR_MESSAGE);
        }
    } // showMessage

    /**
     * Show a URL in a reasonable manner. This will likely involve
     * using a <code>JEditorPane</code> or some similar class to
     * display the file referenced by the <code>URL</code>.
     *
     * @param  url    URL to be shown to the user.
     * @param  title  title for the window showing the URL, if any.
     */
    public void showURL(URL url, String title) {
        UrlViewer.showURL(url, title, mainWindow);
    } // showURL

    /**
     * Change the prompt displayed beside the command input field.
     *
     * @param  prompt  new input prompt, or null to display default.
     */
    public void updateInputPrompt(String prompt) {
        commandAdapter.updateInputPrompt(prompt);
    } // updateInputPrompt
} // GraphicalAdapter
