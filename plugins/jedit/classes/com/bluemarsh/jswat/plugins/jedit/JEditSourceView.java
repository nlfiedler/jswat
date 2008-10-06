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
 * $Id: JEditSourceView.java 14 2007-06-02 23:50:55Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.jswat.plugins.jedit;

import com.bluemarsh.jswat.ContextManager;
import com.bluemarsh.jswat.FileSource;
import com.bluemarsh.jswat.PathManager;
import com.bluemarsh.jswat.Session;
import com.bluemarsh.jswat.SourceSource;
import com.bluemarsh.jswat.ZipSource;
import com.bluemarsh.jswat.event.ContextChangeEvent;
import com.bluemarsh.jswat.event.ContextListener;
import com.bluemarsh.jswat.view.View;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.Log;
import java.awt.EventQueue;
import java.io.IOException;
import javax.swing.JComponent;

/**
 * Defines the JEditSourceView class which will be responsible for
 * displaying the source file on the screen using jEdit buffers.
 *
 * @author  Nathan Fiedler
 * @author  David Taylor
 * @author  Dirk Moebius
 */
public class JEditSourceView implements ContextListener, View {
    /** The buffer associated with the view. */
    private Buffer theBuffer;
    /** Source file we read from. */
    private String sourceFile;
    /** Session to which we belong. Set in <code>init()</code>. */
    private Session ourSession;
    /** The jEdit view we are associated with. */
    private org.gjt.sp.jedit.View jeditView;

    /**
     * Creates a JEditSourceView object.
     */
    public JEditSourceView() {
    }

    /**
     * Invoked when the current context has changed. The context
     * change event identifies which aspect of the context has
     * changed.
     *
     * @param  cce  context change event
     */
    public void contextChanged(ContextChangeEvent cce) {
        if (cce.isBrief()) {
            // Not interested in short-living events.
            return;
        }

        if (!cce.isType(ContextChangeEvent.TYPE_LOCATION)) {
            // Only interested in events that change location.
            return;
        }

        ContextManager cmgr = (ContextManager) cce.getSource();
        Location loc = cmgr.getCurrentLocation();
        if (loc == null) {
            setHighlighter(-1);
            return;
        }

        // Use source code manager to find location's source file.
        PathManager sourceManager = (PathManager)
            ourSession.getManager(PathManager.class);

        try {
            SourceSource src = sourceManager.mapSource(loc);

            // Set the source file and open the buffer.
            setSource(src);

            int line = loc.lineNumber();
            scrollToLine(line);

            // Set the highlighter to a new position if the debuggee is at
            // the top-most position of the stack frames.
            if (cmgr.getCurrentFrame() == 0) {
                setHighlighter(line);
            }
        } catch (Exception e) {
            // don't load anything, and hide the highlighter
            Log.log(Log.ERROR, "error changing context", e);
            setHighlighter(-1);
        }
    } // contextChanged

    /**
     * Look for the given string in this view. Uses the view's current
     * text selection as the starting point. Will wrap around if the
     * string was not found after the current selection.
     *
     * @param  query       string to look for.
     * @param  ignoreCase  true to ignore case.
     * @return  true if string was found somewhere, false if string
     *          does not exist in this view.
     */
    public boolean findString(String query, boolean ignoreCase) {
        // jEdit does this for us.
        return false;
    } // findString

    /**
     * Returns the long version of title of this view. This may be a
     * file name and path, a fully-qualified class name, or whatever is
     * appropriate for the type of view.
     *
     * @return  long view title.
     */
    public String getLongTitle() {
        // It is not expected for this to be called.
        return "jEdit View";
    } // getLongTitle

    /**
     * Returns the title of this view. This may be a file name, a class
     * name, or whatever is appropriate for the type of view.
     *
     * @return  view title.
     */
    public String getTitle() {
        // It is not expected for this to be called.
        return "jEdit View";
    } // getTitle

    /**
     * Returns a reference to the UI component which can be added to the
     * user interface component tree.
     *
     * @return  interface component.
     */
    public JComponent getUI() {
        // This is not used in jEdit.
        return null;
    } // getUI

    /**
     * Read the source data into the text component. The view must be
     * added to the Session as a session listener before calling this
     * method.
     *
     * @param  src   source from which to read data.
     * @param  line  line to make visible (one-based).
     * @throws  IOException
     *          if an I/O error occurs in reading the input stream.
     */
    public void refresh(final SourceSource src, final int line)
        throws IOException
    {
        if (EventQueue.isDispatchThread()) {
            refresh0(src, line);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        refresh0(src, line);
                    } catch (IOException ex) {
                        // alas, we cannot do much here besides logging
                        Log.log(Log.ERROR, JEditSourceView.class,
                            "Error opening source file: " + src + " at line: "
                            + line);
                        Log.log(Log.ERROR, JEditSourceView.class, ex);
                    }
                }
            });
        }
    } // refresh

    /**
     * Does the real work of reading source data into the text component.
     *
     * @param  src   source from which to read data.
     * @param  line  line to make visible (one-based).
     */
    private void refresh0(SourceSource src, int line) throws IOException {
        setSource(src);

        if (theBuffer.isDirty()) {
            theBuffer.reload(jeditView);
        }

        scrollToLine(line);
    } // refresh0

    /**
     * Scrolls the source view to the given line, if possible. This will
     * wait for the buffer I/O activity to finish. The method also ensures that
     * the actual scrolling is performed in the AWT event thread.
     *
     * @param  line  line to scroll to (one-based).
     */
    public void scrollToLine(int line) {
        final int ourLine = (line > 0) ? (line - 1) : 0;

        if (theBuffer.isPerformingIO() || !EventQueue.isDispatchThread()) {
            VFSManager.runInAWTThread(new Runnable() {
                public void run() {
                    scrollToLine0(ourLine);
                }
            });
        } else {
            scrollToLine0(ourLine);
        }
    } // scrollToLine

    /**
     * Does the real work for scrolling to a line.
     *
     * @param  line  line to scroll to (zero-based).
     */
    private void scrollToLine0(int line) {
        Buffer currentBuffer = jeditView.getBuffer();

        if (currentBuffer == null) {
            jeditView.getEditPane().setBuffer(theBuffer);
        } else if (currentBuffer.equals(theBuffer) != true) {
            jeditView.getEditPane().setBuffer(theBuffer);
        }

        JEditTextArea textArea = jeditView.getTextArea();
        try {
            int caret = textArea.getCaretLine();
            textArea.moveCaretPosition(textArea.getLineStartOffset(line));
            textArea.invalidateLine(caret);
        } catch (ArrayIndexOutOfBoundsException e) {
            // This happens if the source file is shorter than we'd expected.
            try {
                textArea.goToBufferEnd(false);
            } catch (ArrayIndexOutOfBoundsException e2) {
                // This indicates a bug in jEdit.
                // In that case, don't set any cursor position at all.
            }
        }
    } // scrollToLine0

    /**
     * Set the location of the CurrentLineHighlight for the text area
     * associated with the jEdit view of this instance.
     *
     * @param  line  the new line number, or -1 if there is no current line.
     */
    protected void setHighlighter(int line) {
        CurrentLineHighlight highlight = CurrentLineHighlight.getHighlighter(
            this.jeditView.getTextArea());
        highlight.setCurrentLine(this.sourceFile, line);
    } // setHighlighter

    /**
     * Set the current session.
     *
     * @param  session  the session.
     */
    public void setSession(Session session) {
        this.ourSession = session;
    } // setSession

    /**
     * Set the new source file for this view, and open a new buffer in jEdit.
     * Note that jEdit first looks in its internal hashmap of open buffers to
     * see if the buffer is already loaded. jEdit won't reload the buffer then.
     *
     * @param  src  the source file.
     * @throws  IOException
     *          if error reading source file.
     */
    protected void setSource(final SourceSource src) throws IOException {
        if (EventQueue.isDispatchThread()) {
            setSource0(src);
        } else {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    try {
                        setSource0(src);
                    } catch (IOException ex) {
                        // alas, we cannot do much here besides logging
                        Log.log(Log.ERROR, JEditSourceView.class,
                            "Error opening source file: " + src);
                        Log.log(Log.ERROR, JEditSourceView.class, ex);
                    }
                }
            });
        }
    } // setSource

    /**
     * Set the new source file for this view, and open a new buffer in jEdit.
     * Note that jEdit first looks in its internal hashmap of open buffers to
     * see if the buffer is already loaded. jEdit won't reload the buffer then.
     *
     * @param  src  the source file.
     * @throws  IOException
     *          if error reading source file.
     */
    private void setSource0(SourceSource src) throws IOException {
        if (src instanceof FileSource) {
            this.sourceFile = src.getLongName();
        } else if (src instanceof ZipSource) {
            // Check whether the Archive plugin is installed.
            if (jEdit.getPlugin("archive.ArchivePlugin") != null) {
                // Append the "archive:" protocol specifier before the source
                // name so that the Archive plugin can open it.
                this.sourceFile = "archive:" + src.getLongName();
            } else {
                throw new IOException(
                    "The source file is contained inside a source archive.\n"
                    + "Please install the Archive plugin, so that jEdit can\n"
                    + "open the file automatically.");
            }
        } else {
            throw new IOException("Cannot open non-file sources.");
        }

        Log.log(Log.DEBUG, this, "setSource(): jeditView=" + this.jeditView
            + " sourceFile=" + this.sourceFile
            + " thread=" + Thread.currentThread());
        this.theBuffer = jEdit.openFile(this.jeditView, this.sourceFile);
    } // setSource0

    /**
     * Saves the jEdit view reference for our use later on.
     *
     * @param  view  the associated view.
     */
    public void setView(org.gjt.sp.jedit.View view) {
        this.jeditView = view;
    } // setView
} // JEditSourceView
