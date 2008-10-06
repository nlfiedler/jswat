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
 * $Id: ConsoleAdapter.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.ui.console;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.Log;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.VMConnection;
import com.bluemarsh.jswat.command.CommandManager;
import com.bluemarsh.jswat.event.SessionEvent;
import com.bluemarsh.jswat.event.SessionListener;
import com.bluemarsh.jswat.ui.AbstractAdapter;
import com.bluemarsh.jswat.ui.Bundle;
import com.bluemarsh.jswat.ui.NoOpenViewException;
import com.bluemarsh.jswat.ui.StartupRunner;
import com.bluemarsh.jswat.view.View;
import com.sun.jdi.Location;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class ConsoleAdapter connects the Session with the user interface of
 * JSwat. It builds out the major interface components, connects them to
 * the Session and managers, and handles some user input. This subclass
 * of the <code>UIAdapter</code> class builds out a console interface,
 * which runs entirely based on <code>stdout</code> and
 * <code>stdin</code>.
 *
 * @author  Nathan Fiedler
 */
public class ConsoleAdapter extends AbstractAdapter {
    /** Session we are associated with. */
    private Session ourSession;
    /** Log to which messages are printed. */
    private Log statusLog;
    /** This output stream supports printing the command prompt. */
    private ConsoleOutputStream outputStream;
    /** Handles the output from the debuggee VM. */
    private ConsoleOutputAdapter outputAdapter;
    /** Map of SourceSource to String[] instances. The String arrays
     * hold the lines read in from the SourceSource. */
    private HashMap loadedFiles;
    /** The command input prompt string, must not be null. */
    private String inputPrompt;

    /**
     * Constructs a ConsoleAdapter.
     */
    public ConsoleAdapter() {
        loadedFiles = new HashMap();
        updateInputPrompt(null);
    } // ConsoleAdapter

    /**
     * In a graphical environment, bring the primary debugger window
     * forward so the user can see it. This is called primarily when a
     * debugger event has occurred and the debugger may be hidden behind
     * the debuggee application window.
     */
    public void bringForward() {
        // this is never called in console mode
    } // bringForward

    /**
     * Construct the appropriate user interface and connect all the
     * pieces together. The result should be a fully functional
     * interface that is ready to be used.
     */
    public void buildInterface() {
        statusLog.start(Thread.NORM_PRIORITY);
        // Stream for printing the Log output.
        outputStream = new ConsoleOutputStream(System.out);
        statusLog.attach(outputStream);
        // The adapter that reads from the debuggee VM's output.
        outputAdapter = new ConsoleOutputAdapter(statusLog);
        ourSession.addListener(outputAdapter);
    } // buildInterface

    /**
     * Indicate if this interface adapter has the ability to find a
     * string in the currently selected source view.
     *
     * @return  always returns false.
     */
    public boolean canFindString() {
        return false;
    } // canFindString

    /**
     * Indicate if this interface adapter has the ability to show source
     * files in a manner appropriate for the user to read.
     *
     * @return  always returns false.
     */
    public boolean canShowFile() {
        return true;
    } // canShowFile

    /**
     * Deconstruct the user interface such that all components are made
     * invisible and prepared for non-use.
     */
    public void destroyInterface() {
        ourSession.removeListener(outputAdapter);
        statusLog.detach(outputStream);
    } // destroyInterface

    /**
     * This is called when there are no more open Sessions. The adapter
     * should take the appropriate action at this time. In most cases
     * that will be to exit the JVM.
     */
    public void exit() {
        System.exit(0);
    } // exit

    /**
     * Search for the given string in the currently selected source
     * view. The search should continue from the last successful match,
     * and wrap around to the beginning when the end is reached. This
     * implementation throws <code>UnsupportedOperationException</code>
     * since the console adapter does not support views.
     *
     * @param  query       string to look for.
     * @param  ignoreCase  true to ignore case.
     * @return  true if string was found.
     * @throws  NoOpenViewException
     *          if there is not source view opened.
     */
    public boolean findString(String query, boolean ignoreCase)
        throws NoOpenViewException {
        // It is quite unlikely that this method will be called in the
        // console mode, but throw this anyway.
        throw new UnsupportedOperationException();
    } // findString

    /**
     * Retrieves the currently active view in JSwat. This implementation
     * throws <code>UnsupportedOperationException</code> since the
     * console adapter does not support views.
     *
     * @return  selected view, or null if none selected.
     */
    public View getSelectedView() {
        // It is quite unlikely that this method will be called in the
        // console mode, but throw this anyway.
        throw new UnsupportedOperationException();
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
        // Have the command manager process any startup files.
        CommandManager cmdman = (CommandManager)
            ourSession.getManager(CommandManager.class);
        String err = StartupRunner.runRCFiles(cmdman);
        if (err != null) {
            statusLog.writeln(err);
        }

        statusLog.writeln(Bundle.getString("initialMsg"));

        // Create the adapter to handle the command input.
        new ConsoleInputAdapter(System.in, cmdman);
    } // initComplete

    /**
     * Loads the contents of the given source into an array of String
     * objects.
     *
     * @param  src  source to load into memory.
     * @return  array of Strings (without line terminators), or null
     *          if source has no input stream.
     * @throws  IOException
     *          if something goes wrong.
     */
    protected String[] loadFile(SourceSource src) throws IOException {
        InputStream is = src.getInputStream();
        if (is == null) {
            return null;
        }
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        ArrayList lines = new ArrayList(100);
        String line = br.readLine();
        while (line != null) {
            lines.add(line);
            line = br.readLine();
        }
        br.close();
        return (String[]) lines.toArray(new String[lines.size()]);
    } // loadFile

    /**
     * Refresh the display to reflect changes in the program. Generally
     * this means refreshing the panels.
     */
    public void refreshDisplay() {
        // Simply clear the cached files. Next time showFile() is
        // called, it will read the file contents again.
        loadedFiles.clear();
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
        String[] lines = (String[]) loadedFiles.get(src);
        if (lines == null) {
            if (!src.exists()) {
                return false;
            }

            try {
                lines = loadFile(src);
                if (lines == null) {
                    statusLog.writeln(
                        Bundle.getString("couldntMapSrcFile")
                        + " (" + src.getLongName() + ")");
                    return false;
                }
            } catch (IOException ioe) {
                statusLog.writeStackTrace(ioe);
                return false;
            }
            loadedFiles.put(src, lines);
        }

        // Get the current location, if any.
        ContextManager conman = (ContextManager)
            ourSession.getManager(ContextManager.class);
        Location loc = conman.getCurrentLocation();
        int currentLine = -1;
        if (loc != null) {
            // Determine the file containing this location.
            PathManager pathman = (PathManager)
                ourSession.getManager(PathManager.class);
            try {
                SourceSource source = pathman.mapSource(loc.declaringType());
                if (source.equals(src)) {
                    currentLine = loc.lineNumber();
                }
            } catch (IOException ioe) {
                // ignored
            }
        }

        if (line <= 0) {
            line = 1;
            if (count <= 0) {
                // Default to displaying the first 10 lines.
                statusLog.writeln(Bundle.getString("console.showFirstLines"));
                count = 10;
            }
        } else if (count <= 0) {
            // Default to displaying one line.
            count = 1;
        }

        int max = Math.min(line + count, lines.length + 1);
        for (int ii = line; ii < max; ii++) {
            statusLog.write(String.valueOf(ii));
            statusLog.write(": ");
            if (ii == currentLine) {
                statusLog.write("===>");
            } else {
                statusLog.write("    ");
            }
            statusLog.writeln(lines[ii - 1]);
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
        // this is never called in console mode
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
        if (statusLog != null) {
            statusLog.writeln(msg);
        } else {
            System.out.println(msg);
        }
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
        try {
            // Get the text from the URL, assuming it is HTML.
            InputStream is = url.openStream();
            InputStreamReader isr = new InputStreamReader(is);
            char[] buf = new char[8192];
            StringWriter sw = new StringWriter();
            int bytesRead = isr.read(buf);
            while (bytesRead > 0) {
                sw.write(buf, 0, bytesRead);
                bytesRead = isr.read(buf);
            }
            isr.close();

            // Strip away all of the tags and display what's left.
            String s = sw.toString();
            // Note that this simple regex won't work if the
            // tag has an attribute value with a > in it.
            s = s.replaceAll("<[^>]+>", "");
            s = s.trim();
            statusLog.writeln(s);
        } catch (IOException ioe) {
            statusLog.writeln("Error reading URL in showURL(): "
                              + ioe.getMessage());
        }
    } // showURL

    /**
     * Change the prompt displayed beside the command input field.
     *
     * @param  prompt  new input prompt, or null to display default.
     */
    public void updateInputPrompt(String prompt) {
        if (prompt == null) {
            prompt = "> ";
        }
        inputPrompt = prompt;
    } // updateInputPrompt

    /**
     * <p>Class ConsoleInputAdapter adapts the standard input stream to
     * the CommandManager.</p>
     *
     * @author  Nathan Fiedler
     */
    protected class ConsoleInputAdapter implements Runnable {
        /** Where input is sent. */
        private CommandManager commandManager;
        /** Where input comes from. */
        private BufferedReader inputReader;

        /**
         * Constructs a ConsoleInputAdapter to read from the given input
         * stream and send the input to the given command manager.
         *
         * @param  input   input stream.
         * @param  cmdman  CommandManager to send input to.
         */
        public ConsoleInputAdapter(InputStream input,
                                   CommandManager cmdman) {
            inputReader = new BufferedReader(new InputStreamReader(input));
            commandManager = cmdman;
            // Start a thread that will read from the input stream.
            Thread th = new Thread(this);
            // Note that this priority is the same as the one that
            // starts the Log above. This is purposeful and helps
            // to make sure the input prompt is printed only after
            // the Log has flushed.
            th.setPriority(Thread.NORM_PRIORITY);
            th.start();
        } // ConsoleInputAdapter

        // If you attempt to make this a LogListener of the Log, you
        // will need to make a new prompt-printer thread that always
        // sleeps 1/10th of a second before printing the prompt.
        // Otherwise you end up printing the prompt _way_ too often.

        /**
         * Read from the input stream and send the input to the command
         * manager.
         */
        public void run() {
            try {
                while (true) {
                    // Flush the log so it comes out before the prompt.
                    // On some systems this is necessary or the buffer
                    // will not flush at all.
                    statusLog.flush();
                    // Now give the Log a chance to flush any output. On
                    // some systems, the prompt can get ahead of the the
                    // flushed output.
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        // Ignore it.
                    }
                    outputStream.printPrompt(inputPrompt);
                    String str = inputReader.readLine();
                    // User hitting enter resets this flag.
                    outputStream.needPrompt();
                    if (str.length() > 0) {
                        // Send input to command manager.
                        try {
                            commandManager.handleInput(str);
                        } catch (Exception e) {
                            statusLog.writeStackTrace(e);
                        }
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } // run
    } // ConsoleInputAdapter
} // ConsoleAdapter

/**
 * Class ConsoleOutputAdapter is responsible for displaying the output
 * of a debuggee process to the Log. It reads both the standard output
 * and standard error streams from the debuggee VM. For it to operate
 * correctly it must be added as a session listener.
 *
 * @author  Nathan Fiedler
 */
class ConsoleOutputAdapter implements SessionListener {
    /** Log to send output to. */
    private Log outputLog;

    /**
     * Constructs a ConsoleOutputAdapter to output to the given Log.
     *
     * @param  log  Log to output to.
     */
    public ConsoleOutputAdapter(Log log) {
        outputLog = log;
    } // ConsoleOutputAdapter

    /**
     * Called when the Session has activated. This occurs when the
     * debuggee has launched or has been attached to the debugger.
     *
     * @param  sevt  session event.
     */
    public void activated(SessionEvent sevt) {
        VMConnection vmc = sevt.getSession().getConnection();
        if (!vmc.isRemote()) {
            // Create readers for the input and error streams.
            displayOutput(vmc.getProcess().getErrorStream());
            displayOutput(vmc.getProcess().getInputStream());
        }
    } // activated

    /**
     * Called when the Session is about to be closed.
     *
     * @param  sevt  session event.
     */
    public void closing(SessionEvent sevt) {
    } // closing

    /**
     * Called when the Session has deactivated. The debuggee VM is no
     * longer connected to the Session.
     *
     * @param  sevt  session event.
     */
    public synchronized void deactivated(SessionEvent sevt) {
        // Let the output readers finish on their own.
    } // deactivated

    /**
     * Create a thread that will retrieve and display any output from
     * the given input stream.
     *
     * @param  is  InputStream to read from.
     */
    protected void displayOutput(final InputStream is) {
        Thread thr = new Thread("output reader") {
            public void run() {
                try {
                    BufferedReader br =
                        new BufferedReader(new InputStreamReader(is));
                    String line = br.readLine();
                    // Dump until there's nothing left.
                    while (line != null) {
                        outputLog.writeln(line);
                        line = br.readLine();
                    }
                } catch (IOException ioe) {
                    outputLog.writeln(Bundle.getString("errorReadingOutput"));
                }
            }
        };
        thr.setPriority(Thread.MIN_PRIORITY);
        thr.start();
    } // displayOutput

    /**
     * Called after the Session has added this listener to the Session
     * listener list.
     *
     * @param  session  the Session.
     */
    public void opened(Session session) {
    } // opened

    /**
     * Called when the debuggee is about to be resumed.
     *
     * @param  sevt  session event.
     */
    public void resuming(SessionEvent sevt) {
    } // resuming

    /**
     * Called when the debuggee has been suspended.
     *
     * @param  sevt  session event.
     */
    public void suspended(SessionEvent sevt) {
    } // suspended
} // ConsoleOutputAdapter

/**
 * <p>Class ConsoleOutputStream is responsible for printing the Log
 * output to the console stream. It has an additonal operation for
 * printing a command input prompt.</p>
 *
 * @author  Nathan Fiedler
 */
class ConsoleOutputStream extends OutputStream {
    /** The output stream to which we print. */
    private OutputStream sink;
    /** True if the prompt was the last thing we printed. */
    private boolean promptPrinted;
    /** The system line separator in byte form. */
    private byte[] lineSeparator;

    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream. This output stream has an additional
     * operation for printing a given command input prompt.
     *
     * @param  out  the underlying output stream to be assigned
     *              to the field <code>this.out</code> for later
     *              use, or null if this instance is to be created
     *              without an underlying stream.
     */
    public ConsoleOutputStream(OutputStream out) {
        sink = out;
        String ls = System.getProperty("line.separator");
        lineSeparator = ls.getBytes();
    } // ConsoleOutputStream

    /**
     * The prompt needs to be printed at some point.
     */
    public synchronized void needPrompt() {
        promptPrinted = false;
    } // needPrompt

    /**
     * Print the previously set command prompt to the output stream.
     * This method bypasses the Log and prints directly to the
     * underlying output stream.
     *
     * @param  prompt  comand input prompt.
     *
     * @throws  IOException
     *          if an I/O error occurs.
     */
    public void printPrompt(String prompt) throws IOException {
        synchronized (this) {
            // Don't print the prompt if we already did.
            if (!promptPrinted) {
                sink.write(prompt.getBytes());
                promptPrinted = true;
            }
        }
    } // printPrompt

    /**
     * Writes the specified byte to this output stream. This
     * implementation determines if the command input prompt was the
     * last thing printed. If so, a line separator is printed before the
     * given byte is sent to the underlying stream.
     *
     * @param  b  the byte.
     * @throws  IOException
     *          if an I/O error occurs.
     */
    public void write(int b) throws IOException {
        synchronized (this) {
            if (promptPrinted) {
                // Print the line separator first.
                sink.write(lineSeparator);
                promptPrinted = false;
            }
            sink.write(b);
        }
    } // write
} // ConsoleOutputStream
