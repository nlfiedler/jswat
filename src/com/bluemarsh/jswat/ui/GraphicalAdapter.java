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
 * $Id: GraphicalAdapter.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui;

import com.bluemarsh.config.JConfigure;
import com.bluemarsh.config.ConfigureListener;
import com.bluemarsh.jswat.AppSettings;
import com.bluemarsh.jswat.CommandManager;
import com.bluemarsh.jswat.JSwat;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.Main;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SessionListener;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.panel.BreakPanel;
import com.bluemarsh.jswat.panel.ClassPanel;
import com.bluemarsh.jswat.panel.EditPopup;
import com.bluemarsh.jswat.panel.JSwatPanel;
import com.bluemarsh.jswat.panel.LocalsTreePanel;
import com.bluemarsh.jswat.panel.MethodsPanel;
import com.bluemarsh.jswat.panel.StackPanel;
import com.bluemarsh.jswat.panel.ThreadPanel;
import com.bluemarsh.jswat.panel.WatchPanel;
import com.bluemarsh.jswat.view.BasicView;
import com.bluemarsh.jswat.view.JSwatView;
import com.bluemarsh.jswat.view.SourceView;
import com.sun.jdi.VMDisconnectedException;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

/**
 * Class GraphicalAdapter connects the Session with the user interface
 * of JSwat. It builds out the major interface components, connects
 * them to the Session and managers, and handles some user input.
 * This subclass of the <code>UIAdapter</code> class builds out a
 * graphical interface made up of JFC components.
 *
 * @author  Nathan Fiedler
 */
public class GraphicalAdapter extends BasicUIAdapter implements ConfigureListener {
    /** Session we are associated with. */
    protected Session ourSession;
    /** Log to which messages are printed. */
    protected Log statusLog;
    /** Top-most frame that contains everything. */
    protected MainWindow mainWindow;
    /** Top horizontal window splitter. */
    protected JSplitPane topSplitter;
    /** Bottom horizontal window splitter. */
    protected JSplitPane bottomSplitter;
    /** Vertical window splitter. */
    protected JSplitPane vertSplitter;
    /** Desktop pane which holds the source file views. */
    protected JDesktopPane desktopPane;
    /** Handles the status log display. */
    protected GraphicalMessageAdapter messageAdapter;
    /** Handles the output from the debuggee VM. */
    protected GraphicalOutputAdapter outputAdapter;
    /** Handles the intput to the debuggee VM. */
    protected GraphicalInputAdapter inputAdapter;
    /** Source view refresher. */
    protected GraphicalSourceRefresher sourceRefresher;
    /** Label for prompt beside the command input box */
    protected JLabel commandLabel;
    /** Table of open sources. Keys are the SourceSource objects, values
     * are the JInternalFrame objects. */
    protected Hashtable openSources;
    /** Table of open windows. Keys are the JInternalFrame objects,
     * values are the SourceSource objects. */
    protected Hashtable openWindows;
    /** Table of open views. Keys are the JInternalFrame objects,
     * values are the SourceView objects. */
    protected Hashtable openViews;
    /** Object for listening to the internal frames. */
    protected GraphicalAdapter.FrameListener frameListener;
    /** List of all panels we've created. Used to refresh them
     * when needed. */
    protected List panelList;

    /**
     * Constructs a GraphicalAdapter, connected to the given Session.
     *
     * @param  session  Session we are associated with.
     */
    public GraphicalAdapter(Session session) {
        ourSession = session;
        statusLog = session.getStatusLog();
        statusLog.start(Thread.NORM_PRIORITY + 1);
        openSources = new Hashtable();
        openWindows = new Hashtable();
        openViews = new Hashtable();
        frameListener = new GraphicalAdapter.FrameListener();
        panelList = new LinkedList();
    } // GraphicalAdapter

    /**
     * Create the panel to contain the breakpoints panel.
     *
     * @param  tabbedPane  Tabbed pane to add command panel to.
     */
    protected void buildBreakpoints(JTabbedPane tabbedPane) {
        BreakPanel breakPanel = new BreakPanel();
        panelList.add(breakPanel);
        ourSession.addListener(breakPanel);
        tabbedPane.addTab(Bundle.getString("breakTab"),
                          breakPanel.getUI());
    } // buildBreakpoints

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
        inputField.add(popup);
        inputField.addMouseListener(popup);

        CommandManager cmdman = (CommandManager)
            ourSession.getManager(CommandManager.class);
        GraphicalCommandAdapter cia =
            new GraphicalCommandAdapter(inputField, commandLabel,
                                        cmdman, statusLog);
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
        AppSettings props = AppSettings.instanceOf();
        try {
            String lafName = props.getString("lookAndFeel");
            if ((lafName != null) && (lafName.length() > 0)) {
                UIManager.setLookAndFeel(lafName);
            } else {
                // If nothing saved, use cross-platform look & feel.
                UIManager.setLookAndFeel
                    (UIManager.getCrossPlatformLookAndFeelClassName());
            }
        } catch (Exception e) {
            System.err.println("Could not load look & feel: " + e);
        }

        // Create the tabbedpane for thread and classes panels.
        JTabbedPane topTabbedPane = new JTabbedPane();
        ThreadPanel threadPanel = new ThreadPanel();
        panelList.add(threadPanel);
        ourSession.addListener(threadPanel);
        topTabbedPane.addTab(Bundle.getString("threadTab"),
                             threadPanel.getUI());
        ClassPanel classPanel = new ClassPanel();
        panelList.add(classPanel);
        ourSession.addListener(classPanel);
        topTabbedPane.addTab(Bundle.getString("classTab"),
                             classPanel.getUI());
        LocalsTreePanel localsPanel = new LocalsTreePanel();
        panelList.add(localsPanel);
        ourSession.addListener(localsPanel);
        topTabbedPane.addTab(Bundle.getString("localsTab"),
                             localsPanel.getUI());
        WatchPanel watchPanel = new WatchPanel();
        panelList.add(watchPanel);
        ourSession.addListener(watchPanel);
        topTabbedPane.addTab(Bundle.getString("watchTab"),
                             watchPanel.getUI());

        // Create the desktop pane that will manage the multiple
        // internal source code windows.
        desktopPane = new JDesktopPane();

        // Splitter for tabbed pane and desktop pane.
        topSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
                                     topTabbedPane, desktopPane);
        topSplitter.setOneTouchExpandable(true);
        int pos = props.getInteger("topSplitter", 300);
        topSplitter.setDividerLocation(pos);

        // Create the tabbedpane for command and stdout/stdin panels.
        JTabbedPane leftTabbedPane = new JTabbedPane();
        buildCommand(leftTabbedPane);
        buildStandard(leftTabbedPane);
        buildBreakpoints(leftTabbedPane);

        JTabbedPane rightTabbedPane = new JTabbedPane();
        // Splitter for messages/output and stack/methods panels.
        bottomSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                        false, leftTabbedPane,
                                        rightTabbedPane);
        bottomSplitter.setOneTouchExpandable(true);
        pos = props.getInteger("bottomSplitter", 500);
        bottomSplitter.setDividerLocation(pos);

        StackPanel stackPanel = new StackPanel();
        panelList.add(stackPanel);
        ourSession.addListener(stackPanel);
        rightTabbedPane.addTab(Bundle.getString("stackTab"),
                               stackPanel.getUI());

        // Create the panel that displays class methods.
        MethodsPanel methodsPanel = new MethodsPanel();
        panelList.add(methodsPanel);
        ourSession.addListener(methodsPanel);
        rightTabbedPane.addTab(Bundle.getString("methodsTab"),
                               methodsPanel.getUI());

        // Splitter for top and bottom portions of screen.
        vertSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false,
                                      topSplitter, bottomSplitter);
        vertSplitter.setOneTouchExpandable(true);
        pos = props.getInteger("vertSplitter", 250);
        vertSplitter.setDividerLocation(pos);

        SessionActionAdapter saa = new SessionActionAdapter();
        ourSession.addListener(saa);

        // Construct the window that will hold everything.
        mainWindow = new MainWindow(Bundle.getString("AppTitle"), saa);
        SessionFrameMapper.addFrameSessionMapping(mainWindow, ourSession);
        WindowMenu windowMenu =
            new WindowMenu(Bundle.getString("windowLabel"), desktopPane);
        mainWindow.menubar.add(windowMenu);

        // Add all that stuff to the main window.
        Container pane = mainWindow.getContentPane();
        pane.add(vertSplitter, "Center");

        // Set up the window close adapter.
        mainWindow.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    // Close the session.
                    Main.endSession(ourSession);
                }
            });

        // Show the window finally.
        mainWindow.setVisible(true);
    } // buildInterface

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
        inputAdapter = new GraphicalInputAdapter();
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
     * Indicate if this interface adapter has the ability to show
     * the status in a manner appropriate for the user to view.
     *
     * @return  always returns true.
     */
    public boolean canShowStatus() {
        return true;
    } // canShowStatus

    /**
     * Invoked when the configuration has been accepted by the user.
     */
    public void configurationChanged() {
        setPreferences();
    } // configurationChanged

    /**
     * Deconstruct the user interface such that all components
     * are made invisible and prepared for non-use.
     */
    public void destroyInterface() {
        // Remove all of the panels from the Session.
        Iterator iter = panelList.iterator();
        while (iter.hasNext()) {
            JSwatPanel panel = (JSwatPanel) iter.next();
            ourSession.removeListener(panel);
        }

        // Remove all of the open source views.
        Enumeration enmr = openViews.elements();
        while (enmr.hasMoreElements()) {
            SourceView view = (SourceView) enmr.nextElement();
            ourSession.removeListener(view);
        }

        // Remove some more listeners from the session.
        ourSession.removeListener(outputAdapter);
        ourSession.removeListener(inputAdapter);
        ourSession.removeListener(sourceRefresher);

        // Destroy the status log adapter.
        messageAdapter.destroy(statusLog);

        SessionFrameMapper.removeFrameSessionMapping(mainWindow);

        // Close the main window.
        mainWindow.close();
    } // destroyInterface

    /**
     * This is called when there are no more open Sessions. The
     * adapter should take the appropriate action at this time.
     * In most cases that will be to exit the JVM.
     */
    public void exit() {
        System.exit(0);
    } // exit

    /**
     * Search for the given string in the currently selected source view.
     * The search should continue from the last successful match, and
     * wrap around to the beginning when the end is reached.
     *
     * @param  query       string to look for.
     * @param  ignoreCase  true to ignore case.
     * @return  true if string was found.
     * @exception  NoOpenViewException
     *             Thrown if there is no view to be searched.
     */
    public boolean findString(String query, boolean ignoreCase)
        throws NoOpenViewException {
        // Find the currently active source view, if any.
        BasicView view = (BasicView) getSelectedView();
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
    public JSwatView getSelectedView() {
        // Find the currently open and active source view, if any.
        // This method is JDK 1.3 specific.
//          JInternalFrame fr = desktopPane.getSelectedFrame();
        JInternalFrame[] frames = desktopPane.getAllFrames();
        JInternalFrame fr = null;
        for (int ii = 0; ii < frames.length; ii++) {
            if (frames[ii].isSelected()) {
                fr = frames[ii];
                break;
            }
        }
        if (fr == null) {
            return null;
        }
        return (JSwatView) openViews.get(fr);
    } // getSelectedView

    /**
     * Called when the Session initialization has completed.
     */
    public void initComplete() {
        // Set up the configuration preferences.
        JConfigure config = JSwat.instanceOf().getJConfigure();
        config.addListener(this);
        setPreferences();

        // Set up the source view refresher.
        sourceRefresher = new GraphicalSourceRefresher();
        ourSession.addListener(sourceRefresher);

        // Have the command manager process any .jswatrc files.
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
     * Refresh the display to reflect changes in the program.
     * Generally this means refreshing the panels.
     */
    public void refreshDisplay() {
        Runnable runnable = new Runnable() {
                public void run() {
                    refreshDisplay0();
                }
            };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    } // refreshDisplay

    /**
     * Refresh the panels on the current thread.
     */
    protected void refreshDisplay0() {
        Iterator iter = panelList.iterator();
        while (iter.hasNext()) {
            JSwatPanel panel = (JSwatPanel) iter.next();
            try {
                panel.refresh(ourSession);
            } catch (VMDisconnectedException vmde) {
                // this is normal
                break;
            } catch (Exception e) {
                // This is not expected. Build up a contiguous string
                // so the whole message is printed at once.
                StringBuffer buf = new StringBuffer
                    ("Exception or Error occurred: ");
                buf.append(e.toString());
                buf.append("\nStack trace printed to console.");
                buf.append("\nProcessing the rest of the listeners...");
                statusLog.writeln(buf.toString());
                statusLog.writeStackTrace(e);
            }
        }
    } // refreshDisplay0

    /**
     * Save any settings to the appropriate places, the program
     * is about the terminate.
     */
    public void saveSettings() {
        JConfigure config = JSwat.instanceOf().getJConfigure();

        // See if we should remember the main window geometry.
        String s = config.getProperty("appearance.rememberGeometry");
        if ((s != null) && s.equalsIgnoreCase("true")) {
            AppSettings props = AppSettings.instanceOf();
            // Save main window position and size.
            props.setInteger("windowTop", mainWindow.getY());
            props.setInteger("windowLeft", mainWindow.getX());
            props.setInteger("windowWidth", mainWindow.getWidth());
            props.setInteger("windowHeight", mainWindow.getHeight());
            // Save the splitpane positions.
            props.setInteger("topSplitter",
                             topSplitter.getDividerLocation());
            props.setInteger("bottomSplitter",
                             bottomSplitter.getDividerLocation());
            props.setInteger("vertSplitter",
                             vertSplitter.getDividerLocation());
        }
    } // saveSettings

    /**
     * Use the configured preferences to modify our interface.
     */
    protected void setPreferences() {
        // Set the max line counts for the messages and output areas.
        JConfigure config = JSwat.instanceOf().getJConfigure();
        int linecount = 1000;
        try {
            linecount = Integer.parseInt
                (config.getProperty("appearance.messageLines"));
        } catch (NumberFormatException nfe) {
            statusLog.writeStackTrace(nfe);
        }
        messageAdapter.setMaxLineCount(linecount);

        linecount = 1000;
        try {
            linecount = Integer.parseInt
                (config.getProperty("appearance.outputLines"));
        } catch (NumberFormatException nfe) {
            statusLog.writeStackTrace(nfe);
        }
        outputAdapter.setMaxLineCount(linecount);

        boolean showMenubar =
            config.getBooleanProperty("appearance.layout.showMenubar");
        if (showMenubar) {
            mainWindow.showMenubar();
        } else {
            mainWindow.hideMenubar();
        }

        boolean showToolbar =
            config.getBooleanProperty("appearance.layout.showToolbar");
        if (showToolbar) {
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
        Runnable runnable = new Runnable() {
                public void run() {
                    boolean b = showFile0(src, line);
                    if (!b) {
                        throw new RuntimeException("showFile failed");
                    }
                }
            };

        InputStream is = src.getInputStream();
        if (is == null) {
            // File probably does not exist.
            JSwat swat = JSwat.instanceOf();
            statusLog.write(swat.getResourceString("fileNotFound"));
            statusLog.write(": ");
            statusLog.writeln(src.getName());
            return false;
        }
        try {
            // Be a good citizen and close the input stream.
            is.close();
        } catch (IOException ioe) { /* ignored */ }

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
        // See if the file has already been opened.
        JInternalFrame win = (JInternalFrame) openSources.get(src);
        SourceView view;
        if (win != null) {
            // File is already there, bring it to the front.
            win.moveToFront();
            try {
                win.setIcon(false);
                win.setSelected(true);
            } catch (java.beans.PropertyVetoException pve) {
                statusLog.writeStackTrace(pve);
            }
            view = (SourceView) openViews.get(win);
            // Make the requested line of code visible in the source view.
            view.scrollToLine(line);
        } else {

            // Create a new source view and add it to the Session as
            // a SessionListener.
            // Show a busy cursor while we open the view.
            mainWindow.setCursor(Cursor.getPredefinedCursor(
                Cursor.WAIT_CURSOR));
            view = new SourceView(src.getName());
            ourSession.addListener(view);
            boolean loadedOkay = view.refresh(src.getInputStream(), line);
            mainWindow.setCursor(Cursor.getDefaultCursor());

            if (loadedOkay) {
                // Add the view to the main window.
                win = new JInternalFrame(src.getName(), true, true,
                                         true, true);
                win.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                win.setBounds(10, 10, 200, 200);
                win.setContentPane(view.getUI());
                // Add the window to the default layer of the desktop pane.
                desktopPane.add(win, JLayeredPane.DEFAULT_LAYER);
                try {
                    win.setSelected(true);
                    // Maximize the window if the user has so chosen.
                    JConfigure config = JSwat.instanceOf().getJConfigure();
                    Boolean max = Boolean.valueOf(
                        config.getProperty("view.maximize"));
                    win.setMaximum(max.booleanValue());
                } catch (java.beans.PropertyVetoException pve) {
                    statusLog.writeStackTrace(pve);
                }
                win.setVisible(true);

                // We want to be notified when the window closes so we
                // can clean up.
                win.addInternalFrameListener(frameListener);
                openSources.put(src, win);
                openWindows.put(win, src);
                openViews.put(win, view);
                sourceRefresher.addView(view, src);
            } else {
                ourSession.removeListener(view);
                return false;
            }
        }

        return true;
    } // showFile0

    /**
     * Show a status message in a reasonable location.
     *
     * @param  status  message to be shown to the user.
     */
    public void showStatus(String status) {
        // It's possible we get called after the interface was destroyed.
        if (mainWindow != null) {
            mainWindow.setTitle(Bundle.getString("AppTitle") + ": " + status);
        }
    } // showStatus

    /**
     * Class to listen to internal frames for closure. We use
     * this to make sure the hashtables are kept up to date.
     */
    protected class FrameListener extends InternalFrameAdapter {

        /**
         * An internal frame has been closed. Let's remove it
         * from the hashtables.
         *
         * @param  e  Internal frame event.
         */
        public void internalFrameClosed(InternalFrameEvent e) {
            Object src = e.getSource();
            // Remove the open window from the table.
            Object file = openWindows.remove(src);
            if (file != null) {
                // Remove the open file from the table.
                Object win = openSources.remove(file);
                // Remove the view from the Session.
                SourceView view = (SourceView) openViews.remove(win);
                ourSession.removeListener(view);
                sourceRefresher.removeView(view);
            }
        } // internalFrameClosed
    } // FrameListener
} // GraphicalAdapter

/**
 * Class GraphicalSourceRefresher is responsible for automatically
 * refreshing the open source views whenever the session activates.
 *
 * @author  Nathan Fiedler
 */
class GraphicalSourceRefresher implements SessionListener {
    /** Table of open SourceView objects, where the keys are the
     * SourceViews and the values are the SourceSources. */
    protected Hashtable openViews;

    /**
     * Constructs a GraphicalSourceRefresher to automatically
     * refresh the open source views.
     */
    public GraphicalSourceRefresher() {
        openViews = new Hashtable();
    } // GraphicalSourceRefresher

    /**
     * Called when the Session is about to begin an active debugging
     * session. That is, JSwat is about to debug a debuggee VM.
     *
     * @param  session  Session being activated.
     */
    public void activate(Session session) {
        // Manually refresh all of the open views.
        // Better this than having the views do it themselves; in
        // that case our openFile() causes the view refresh() to
        // be invoked twice, which is dumb.
        Runnable runnable = new Runnable() {
                public void run() {
                    Enumeration enmr = openViews.keys();
                    while (enmr.hasMoreElements()) {
                        SourceView view = (SourceView) enmr.nextElement();
                        SourceSource src = (SourceSource) openViews.get(view);
                        view.refresh(src.getInputStream(), 0);
                    }
                }
            };
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    } // activate

    /**
     * Add the given source view and matching source file to the list.
     *
     * @param  view  source view object.
     * @param  src   source of the view.
     */
    void addView(SourceView view, SourceSource src) {
        openViews.put(view, src);
    } // addView

    /**
     * Called when the Session is about to close down.
     *
     * @param  session  Session being closed.
     */
    public void close(Session session) {
    } // close

    /**
     * Called when the Session is about to end an active debugging
     * session. That is, JSwat is about to terminate the connection
     * with the debuggee VM.
     *
     * @param  session  Session being deactivated.
     */
    public void deactivate(Session session) {
    } // deactivate

    /**
     * Called after the Session has added this listener to the
     * Session listener list.
     *
     * @param  session  Session adding this listener.
     */
    public void init(Session session) {
    } // init

    /**
     * Remove the given source view from the list.
     *
     * @param  view  source view object to remove.
     */
    void removeView(SourceView view) {
        openViews.remove(view);
    } // addView
} // GraphicalSourceRefresher
