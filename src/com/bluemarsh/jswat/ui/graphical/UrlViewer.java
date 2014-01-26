/*********************************************************************
 *
 *      Copyright (C) 2002-2005 Nathan Fiedler
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
 * $Id: UrlViewer.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.graphical;

import com.bluemarsh.jswat.ui.Bundle;
import com.bluemarsh.jswat.util.Strings;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;

/**
 * Class <code>UrlViewer</code> handles the displaying of content
 * referenced by a URL. It handles the hyperlinks and automatically
 * follows them to the referenced content.
 *
 * @author  Nathan Fiedler
 */
public class UrlViewer implements ActionListener, HyperlinkListener {
    /** Suffix added to commnd strings to find images. */
    private static final String TOOLBAR_IMAGE_SUFFIX = "ToolbarImage";
    /** Suffix added to commnd strings to find tooltips. */
    private static final String TIP_SUFFIX = "Tooltip";
    /** Logger. */
    private static Logger logger;
    /** Frame the help is displayed in. */
    private JFrame frame;
    /** The editor pane displaying the content. */
    private JEditorPane editorPane;
    /** Button to go back through history. */
    private JButton backButton;
    /** Button to go forard through history. */
    private JButton forwardButton;
    /** History of visited URL or String objects. */
    private List historyList;
    /** Offset within the history marking the current position. */
    private int historyIndex;

    static {
        // Initialize the logger.
        logger = Logger.getLogger("com.bluemarsh.jswat.ui.viewer");
        com.bluemarsh.jswat.logging.Logging.setInitialState(logger);
    }

    /**
     * Constructs a frame to show the given help screen.
     *
     * @param  url    help screen to be shown.
     * @param  frame  parent window.
     */
    public static void showHelp(URL url, Frame frame) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("showHelp(URL,Frame): " + url);
        }
        new UrlViewer(frame, url, Bundle.getString("UrlViewer.helpTitle"));
    } // showHelp

    /**
     * Constructs a frame to show the given content.
     *
     * @param  url    content to be shown.
     * @param  title  title of the content.
     * @param  frame  parent window.
     */
    public static void showURL(URL url, String title, Frame frame) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("showURL(URL,String,Frame): '" + title + "' - " + url);
        }
        new UrlViewer(frame, title, url);
    } // showURL

    /**
     * Constructs a UrlViewer to show plain content.
     *
     * @param  topFrame  frame on which to center.
     * @param  title     title of the content.
     * @param  url       content to be shown.
     */
    public UrlViewer(Frame topFrame, String title, URL url) {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(gbl);
        gbc.insets = new Insets(3, 3, 3, 3);

        // Use the mighty and fragrant editor pane to do the hard work.
        editorPane = new JEditorPane();
        showPage(url);
        editorPane.setEditable(false);

        // Naturally it must be scrollable.
        JScrollPane scroller = new JScrollPane(editorPane);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbl.setConstraints(scroller, gbc);
        pane.add(scroller);

        JButton button = new JButton(Bundle.getString("okLabel"));
        button.addActionListener(this);
        button.setActionCommand("close");
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbl.setConstraints(button, gbc);
        pane.add(button);

        frame.setSize(400, 300);
        frame.setLocationRelativeTo(topFrame);
        frame.setVisible(true);
    } // UrlViewer

    /**
     * Constructs a UrlViewer to display help screens.
     *
     * @param  topFrame  frame on which to center.
     * @param  url       content to be shown.
     * @param  title     title of the content.
     */
    public UrlViewer(Frame topFrame, URL url, String title) {
        historyList = new ArrayList();

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(gbl);
        gbc.insets = new Insets(3, 3, 3, 3);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        gbl.setConstraints(toolbar, gbc);
        pane.add(toolbar);

        // Add the buttons for controlling the browser.
        backButton = createButton("back");
        toolbar.add(backButton);
        forwardButton = createButton("forward");
        toolbar.add(forwardButton);
        toolbar.add(createButton("reload"));
        toolbar.add(createButton("home"));
        toolbar.add(createButton("copy"));
        toolbar.add(createButton("search"));

        // Spacer to eat the rest of the row's space.
        Component glue = Box.createGlue();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbl.setConstraints(glue, gbc);
        pane.add(glue);

        // Use the mighty and fragrant editor pane to do the hard work.
        editorPane = new JEditorPane();
        showPage(url);
        historyList.add(url);
        editorPane.setEditable(false);
        editorPane.addHyperlinkListener(this);

        // Naturally it must be scrollable.
        JScrollPane scroller = new JScrollPane(editorPane);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbl.setConstraints(scroller, gbc);
        pane.add(scroller);

        JButton button = new JButton(Bundle.getString("closeLabel"));
        button.addActionListener(this);
        button.setActionCommand("close");
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbl.setConstraints(button, gbc);
        pane.add(button);

        enableControls();

        int h = topFrame.getHeight() * 80 / 100;
        int w = topFrame.getWidth() * 60 / 100;
        frame.setSize(w, h);
        frame.setLocationRelativeTo(topFrame);
        frame.setVisible(true);
    } // UrlViewer

    /**
     * Invoked when a button has been pressed.
     *
     * @param  e  action event.
     */
    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        String action = button.getActionCommand();
        if (action.equals("close")) {
            frame.dispose();
        } else if (action.equals("copy")) {
            // Any text selected?
            if (editorPane.getSelectionStart()
                == editorPane.getSelectionEnd()) {
                // No, select all text.
                editorPane.selectAll();
                // Copy the text.
                editorPane.copy();
                // Unselect the text.
                editorPane.select(0, 0);
            } else {
                // Copy the selected text.
                editorPane.copy();
            }
        } else if (action.equals("back")) {
            historyIndex--;
            showPage(historyList.get(historyIndex));
            enableControls();
        } else if (action.equals("forward")) {
            historyIndex++;
            showPage(historyList.get(historyIndex));
            enableControls();
        } else if (action.equals("reload")) {
            showPage(historyList.get(historyIndex));
        } else if (action.equals("home")) {
            URL url = com.bluemarsh.jswat.Bundle.getResource("helpIndex");
            showPageWithHistory(url);
        } else if (action.equals("search")) {
            search();
        }
    } // actionPerformed

    /**
     * Creates a button.
     *
     * @param  key  action command name.
     * @return  new button.
     */
    protected JButton createButton(String key) {
        URL url = Bundle.getResource("UrlViewer." + key
                                     + TOOLBAR_IMAGE_SUFFIX);
        JButton b = url != null
            ? new JButton(new ImageIcon(url)) : new JButton(key);
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1, 1, 1, 1));

        b.setActionCommand(key);
        b.addActionListener(this);

        // attach tooltip to button
        String tip = Bundle.getString("UrlViewer." + key + TIP_SUFFIX);
        if (tip != null) {
            // Use HTML for multi-line tooltips.
            // The font seems awfully big, so let's shrink it.
            tip = "<html><small>" + tip + "</small></html>";
            b.setToolTipText(tip);
        }
        return b;
    } // createButton

    /**
     * Enable or disable the browser controls as appropriate.
     */
    protected void enableControls() {
        if (historyIndex <= 0) {
            backButton.setEnabled(false);
        } else {
            backButton.setEnabled(true);
        }
        if (historyIndex >= (historyList.size() - 1)) {
            forwardButton.setEnabled(false);
        } else {
            forwardButton.setEnabled(true);
        }
    } // enableControls

    /**
     * Called when a hypertext link is updated.
     *
     * @param  he  hyperlink event.
     */
    public void hyperlinkUpdate(HyperlinkEvent he) {
        if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            URL url = he.getURL();
            if (url != null) {
                showPageWithHistory(url);
            }
        }
    } // hyperlinkUpdate

    /**
     * Reports the given exception to a new page.
     *
     * @param  e  exception to report.
     */
    protected void reportException(Exception e) {
        showPage(Strings.exceptionToString(e));
    } // reportException

    /**
     * Implements the help search feature.
     */
    protected void search() {
        // This really ought to be in a subclass that handles
        // help files in particular, not this generic class.

        // Get the previous search phrase, if any.
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        String query = prefs.get("searchString", "");
        boolean ignoreCase = prefs.getBoolean("searchIgnoreCase", false);

        Object[] messages = {
            Bundle.getString("UrlViewer.searchStringField"),
            new JTextField(query, 25),
            new JCheckBox(Bundle.getString("UrlViewer.searchIgnoreCase"),
                          ignoreCase)
        };

        // Ask the user for the search phrase.
        int response = JOptionPane.showOptionDialog(
            frame, messages, Bundle.getString("UrlViewer.searchTitle"),
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, null, null);
        query = ((JTextField) messages[1]).getText();
        ignoreCase = ((JCheckBox) messages[2]).isSelected();

        // Did the user press OK with an input string?
        if (response != JOptionPane.OK_OPTION || query.length() == 0) {
            return;
        }

        // Save the values for next time.
        prefs.put("searchString", query);
        prefs.putBoolean("searchIgnoreCase", ignoreCase);

        Pattern pattern = Pattern.compile(query, ignoreCase
                                          ? Pattern.CASE_INSENSITIVE : 0);

        // Get the names of the help files.
        String[] names = Strings.tokenize(Bundle.getString("helpFiles"));

        // List of SearchResultEntry instances.
        List results = new ArrayList();

        for (int ii = 0; ii < names.length; ii++) {
            // Open each of the help files.
            InputStream is =
                com.bluemarsh.jswat.Bundle.class.getResourceAsStream(
                    "resources/" + names[ii]);
            String contents = null;

            try {
                InputStreamReader isr = new InputStreamReader(is);
                char[] cbuf = new char[8192];
                StringWriter sw = new StringWriter();
                int bytesRead = isr.read(cbuf);
                while (bytesRead > 0) {
                    sw.write(cbuf, 0, bytesRead);
                    bytesRead = isr.read(cbuf);
                }
                isr.close();
                contents = sw.toString();
            } catch (IOException ioe) {
                reportException(ioe);
                return;
            }

            // Get the page title.
            int i1 = contents.indexOf("<title>");
            int i2 = contents.indexOf("</title>");
            String title = contents.substring(i1 + 7, i2).trim();

            // Strip away all of the HTML tags.
            // Note that this simple regex won't work if the
            // tag has an attribute value with a > in it.
            contents = contents.replaceAll("<[^>]+>", "");

            Matcher matcher = pattern.matcher(contents);
            // Use regex to find search phrase.
            if (matcher.find()) {
                // Make a URL and add it to the list of results.
                URL url = com.bluemarsh.jswat.Bundle.class.getResource(
                    "resources/" + names[ii]);
                results.add(new SearchResultEntry(title, url));
            }
        }

        if (results.size() > 0) {
            // Show the search results in a dialog.
            SearchResultsDialog srd = new SearchResultsDialog(
                frame, results.toArray());
            srd.pack();
            srd.setLocationRelativeTo(frame);
            srd.setResizable(false);
            srd.setVisible(true);
        } else {
            // No matches were found.
            JOptionPane.showMessageDialog(
                frame, Bundle.getString("UrlViewer.searchNoMatch"),
                Bundle.getString("UrlViewer.searchTitle"),
                JOptionPane.ERROR_MESSAGE);
        }
    } // search

    /**
     * Show the given page.
     *
     * @param  page  a URL or String.
     */
    protected void showPage(Object page) {
        if (page instanceof URL) {
            showPage((URL) page);
        } else if (page instanceof String) {
            showPage((String) page);
        } else {
            showPage("ERROR: 'page' is not a URL or String!");
        }
    } // showPage

    /**
     * Show the given page. Using this method to show HTML will result
     * in links that do not work. Apparently JEditorPane only supports
     * hyperlinks if the setPage() method is used.
     *
     * @param  page  a String.
     */
    protected void showPage(String page) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("showPage(String): " + page.substring(0, 50));
        }
        EditorKit kit = editorPane.getEditorKit();
        Document doc = kit.createDefaultDocument();
        try {
            kit.read(new StringReader((String) page), doc, 0);
        } catch (BadLocationException ble) {
            // zero cannot be a bad location
        } catch (IOException ioe) {
            // this is impossible with a string reader
        }
        editorPane.setDocument(doc);
    } // showPage

    /**
     * Show the given page.
     *
     * @param  page  a URL.
     */
    protected void showPage(URL page) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("showPage(URL): " + page);
        }
        try {
            // Force the editor to show something else before setting it
            // to what we want. This clears out the current URL and lets
            // us show the same one again, if so desired.
            editorPane.setPage(Bundle.getResource("UrlViewer.blankHtml"));
            editorPane.setPage(page);
        } catch (IOException ioe) {
            reportException(ioe);
        }
    } // showPage

    /**
     * Show the given page, adding it to the history and dealing with
     * erasing forward history, and enabling the controls as
     * appropriate.
     *
     * @param  page  a URL or String.
     */
    protected void showPageWithHistory(Object page) {
        // Are we at the end of the history?
        if ((historyList.size() - 1) > historyIndex) {
            // No, have to erase lost history.
            ListIterator iter = historyList.listIterator(historyIndex + 1);
            while (iter.hasNext()) {
                iter.next();
                iter.remove();
            }
        }
        historyList.add(page);
        historyIndex++;
        showPage(page);
        // Update the browser controls.
        enableControls();
    } // showPageWithHistory

    /**
     * Class SearchResultEntry represents a search result.
     */
    protected class SearchResultEntry {
        /** Title from the help file. */
        private String title;
        /** Help file. */
        private URL file;

        /**
         * Constructs a SearchResultEntry instance.
         */
        public SearchResultEntry() {
        } // SearchResultEntry

        /**
         * Constructs a SearchResultEntry instance.
         *
         * @param  title  title of results.
         * @param  file   file of results.
         */
        public SearchResultEntry(String title, URL file) {
            this.title = title;
            this.file = file;
        } // SearchResultEntry

        /**
         * Returns the URL pointing to the file of this search result.
         *
         * @return  URL of resulting file.
         */
        public URL getFile() {
            return file;
        } // getFile

        /**
         * Returns a String representation of the result.
         *
         * @return  String representation of this.
         */
        public String toString() {
            return title;
        } // toString
    } // SearchResultEntry

    /**
     * Class SearchResultsDialog displays the results of a search.
     */
    protected class SearchResultsDialog extends JDialog
        implements ActionListener, ListSelectionListener {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** List showing the results. */
        private JList resultsList;

        /**
         * Constructs a SearchResultsDialog instance.
         *
         * @param  owner    parent component.
         * @param  results  list of search results.
         */
        public SearchResultsDialog(Frame owner, Object[] results) {
            super(owner, Bundle.getString("UrlViewer.searchResultsTitle"));

            // Create a panel with insets of 10 pixels all around.
            GridBagLayout gbl = new GridBagLayout();
            GridBagConstraints gbc = new GridBagConstraints();
            Container contentPane = getContentPane();
            contentPane.setLayout(gbl);
            JPanel allPanel = new JPanel(new GridBagLayout());
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.insets = new Insets(3, 3, 3, 3);
            gbl.setConstraints(allPanel, gbc);
            contentPane.add(allPanel);
            gbl = (GridBagLayout) allPanel.getLayout();

            resultsList = new JList(results);
            resultsList.setVisibleRowCount(8);
            resultsList.addListSelectionListener(this);

            JScrollPane listScroller = new JScrollPane(resultsList);
            gbc.fill = GridBagConstraints.BOTH;
            gbl.setConstraints(listScroller, gbc);
            allPanel.add(listScroller);

            JButton button = new JButton(Bundle.getString("closeLabel"));
            button.addActionListener(this);
            gbl.setConstraints(button, gbc);
            allPanel.add(button);

            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        } // SearchResultsDialog

        /**
         * Invoked when a button has been pressed.
         *
         * @param  e  action event.
         */
        public void actionPerformed(ActionEvent e) {
            dispose();
        } // actionPerformed

        /**
         * Handle list selection events.
         *
         * @param  e  the list selection event.
         */
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                SearchResultEntry sre = (SearchResultEntry)
                    resultsList.getSelectedValue();
                showPageWithHistory(sre.getFile());
            }
        } // valueChanged
    } // SearchResultsDialog
} // UrlViewer
