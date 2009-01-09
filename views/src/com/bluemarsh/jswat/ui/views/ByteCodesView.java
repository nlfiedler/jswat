/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is JSwat. The Initial Developer of the Original
 * Software is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2006-2008. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id$
 */

package com.bluemarsh.jswat.ui.views;

import com.bluemarsh.jswat.core.context.ContextEvent;
import com.bluemarsh.jswat.core.context.ContextListener;
import com.bluemarsh.jswat.core.context.ContextProvider;
import com.bluemarsh.jswat.core.context.DebuggingContext;
import com.bluemarsh.jswat.core.path.PathManager;
import com.bluemarsh.jswat.core.path.PathProvider;
import com.bluemarsh.jswat.core.session.Session;
import com.bluemarsh.jswat.core.session.SessionEvent;
import com.bluemarsh.jswat.core.session.SessionListener;
import com.bluemarsh.jswat.core.session.SessionManager;
import com.bluemarsh.jswat.core.session.SessionManagerEvent;
import com.bluemarsh.jswat.core.session.SessionManagerListener;
import com.bluemarsh.jswat.core.session.SessionProvider;
import com.bluemarsh.jswat.ui.editor.DebugAnnotation;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import java.awt.EventQueue;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.util.ByteSequence;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import javax.swing.JEditorPane;
import javax.swing.JToolBar;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.plain.PlainKit;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotation;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.TopComponent;

/**
 * Class ByteCodesView shows the byte codes for the current method.
 *
 * @author  Nathan Fiedler
 */
public class ByteCodesView extends CloneableEditor
        implements ContextListener, Runnable, SessionListener,
        SessionManagerListener {
    /** silence the compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The mime-type of the editor displayed herein. */
    private static final String MIME_TYPE = "text/bytecode";
    /** Each entry corresponds to a line in the text area, in which the
     * value is the byte code index displayed on that line of text (the
     * index into this array is the line number minus one). */
    private long[] lineCodeIndices;
    /** Offset into the lineCodeIndices array of the first empty entry. */
    private int lineCodeOffset;
    /** A unique identifier of the method shown in the text area. */
    private String previousMethod;

    static {
        // Matches the mime-type returned by the editor environment.
        JEditorPane.registerEditorKitForContentType(MIME_TYPE,
                "com.bluemarsh.jswat.ui.views.ByteCodesView$ByteCodesEditorKit");
    }

    /**
     * Creates a new instance of ByteCodesView.
     */
    public ByteCodesView() {
        super(new ByteCodesEditorSupport());
        setLayout(new BorderLayout());
    }

    @Override
    public void changedFrame(ContextEvent ce) {
    }

    @Override
    public void changedLocation(ContextEvent ce) {
        updateDisplay();
    }

    @Override
    public void changedThread(ContextEvent ce) {
    }

    /**
     * Hide all indications of the current byte code display.
     */
    private void clearDisplay() {
        getByteCodesPanel().hideLocation();
        ByteCodesEditorSupport support =
                (ByteCodesEditorSupport) cloneableEditorSupport();
        support.removeAnnotation();
        final ByteCodesDocument doc = (ByteCodesDocument) support.getDocument();
        NbDocument.runAtomic(doc, new Runnable() {
            @Override
            public void run() {
                doc.clear();
            }
        });
    }

    @Override
    public void closing(SessionEvent sevt) {
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        // By this time the editor components have been destroyed.
        // Stop listening to everything that affects our display.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.removeSessionManagerListener(this);
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            Session session = iter.next();
            session.removeSessionListener(this);
            DebuggingContext dc = ContextProvider.getContext(session);
            dc.removeContextListener(this);
        }
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        // Start listening to everything that affects our display.
        SessionManager sm = SessionProvider.getSessionManager();
        sm.addSessionManagerListener(this);
        Iterator<Session> iter = sm.iterateSessions();
        while (iter.hasNext()) {
            Session session = iter.next();
            session.addSessionListener(this);
            DebuggingContext dc = ContextProvider.getContext(session);
            dc.addContextListener(this);
        }
        // Update the display, in case there is a location, but do it on
        // the AWT thread since it involves changes to the interface.
        EventQueue.invokeLater(this);
    }

    @Override
    public void connected(SessionEvent sevt) {
    }

    @Override
    public void disconnected(SessionEvent sevt) {
        clearDisplay();
    }

    /**
     * Retrieve the byte codes display panel.
     *
     * @return  byte codes panel.
     */
    private ByteCodesPanel getByteCodesPanel() {
        ByteCodesEditorSupport bes =
                (ByteCodesEditorSupport) cloneableEditorSupport();
        ByteCodesDocument doc = (ByteCodesDocument) bes.getDocument();
        if (doc == null) {
            // In this unlikely case, just return a new instance to
            // avoid a null pointer exception.
            return new ByteCodesPanel();
        }
        return doc.getByteCodesPanel();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ByteCodesView.class, "CTL_ByteCodesView_Name");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("jswat-bytecodes-view");
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public String getToolTipText() {
        return NbBundle.getMessage(ByteCodesView.class, "CTL_ByteCodesView_Tooltip");
    }

    @Override
    public void opened(Session session) {
    }

    @Override
    protected String preferredID() {
        return getClass().getName();
    }

    @Override
    public void resuming(SessionEvent sevt) {
        // Leave the display as-is since we may see it again very soon,
        // and it would be annoying to clear it and then rebuild the
        // exact same contents for the same method.
        ByteCodesEditorSupport support =
                (ByteCodesEditorSupport) cloneableEditorSupport();
        support.removeAnnotation();
    }

    @Override
    public void run() {
        updateDisplay();
    }

    @Override
    public void sessionAdded(SessionManagerEvent e) {
        Session session = e.getSession();
        session.addSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.addContextListener(this);
    }

    @Override
    public void sessionRemoved(SessionManagerEvent e) {
        Session session = e.getSession();
        session.removeSessionListener(this);
        DebuggingContext dc = ContextProvider.getContext(session);
        dc.removeContextListener(this);
    }

    @Override
    public void sessionSetCurrent(SessionManagerEvent e) {
        updateDisplay();
    }

    /**
     * Show the byte codes for the method.
     *
     * @param  constants  the constants pool for the containing class.
     * @param  code       the byte codes to be displayed.
     * @param  location   the Location being displayed.
     */
    private void showByteCodes(final ConstantPool constants, final Code code,
            final Location location) {

        // Construct the code indices table if it has not been already.
        if (lineCodeIndices == null) {
            lineCodeIndices = new long[16];
        }
        final ByteCodesEditorSupport support =
                (ByteCodesEditorSupport) cloneableEditorSupport();
        final ByteCodesDocument doc = (ByteCodesDocument) support.getDocument();

        // Determine if we are showing a new method or the same one.
        String key = location.declaringType().name() + '.' +
                location.method().name() + location.method().signature();
        if (previousMethod != null && key.equals(previousMethod)) {
            // This method is being shown already, adjust the highlight.
            long codeIndex = location.codeIndex();
            support.removeAnnotation();
            boolean foundIndex = false;
            int line = 0;
            while (!foundIndex && line < lineCodeOffset) {
                if (lineCodeIndices[line] == codeIndex) {
                    // Found the line, now highlight it, adding one to
                    // translate from relative to absolute numbering.
                    support.annotate(line + 1);
                    foundIndex = true;
                }
                line++;
            }
            if (!foundIndex) {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "ByteCodesView: did not found code index: " + key +
                        " @ " + codeIndex);
            }
            return;
        }
        // We have not seen this method before.
        previousMethod = key;
        // Must reset this as we will be rebuilding the table soon.
        lineCodeOffset = 0;
        
        // Interpret the byte codes and append them to the text area.
        NbDocument.runAtomic(doc, new Runnable() {
            @Override
            public void run() {
                try {
                    doc.clear();
                    ByteSequence bytes = new ByteSequence(code.getCode());
                    long codeIndex = location.codeIndex();
                    int pcLine = -1;
                    while (bytes.available() > 0) {
                        int byteIndex = bytes.getIndex();
                        // Create opcode label, padded to six spaces, right-aligned.
                        StringBuilder line = new StringBuilder(64);
                        line.append(byteIndex);
                        line.append(':');
                        if (line.length() < 6) {
                            char[] fill = new char[6 - line.length()];
                            Arrays.fill(fill, ' ');
                            line.insert(0, fill);
                        }
                        line.append(' ');
                        // Don't bother with the constant pool indices as they
                        // are useless for our simple output.
                        String bc = Utility.codeToString(bytes, constants, false);
                        line.append(bc);
                        line.append('\n');
                        doc.append(line.toString());

                        // Ensure the code indices table is big enough.
                        if (lineCodeIndices.length == lineCodeOffset) {
                            long[] temp = new long[lineCodeOffset * 2];
                            System.arraycopy(lineCodeIndices, 0, temp, 0, lineCodeIndices.length);
                            lineCodeIndices = temp;
                        }
                        // Add the byte code offset to the line table.
                        lineCodeIndices[lineCodeOffset] = byteIndex;
                        lineCodeOffset++;

                        if (byteIndex == codeIndex) {
                            pcLine = lineCodeOffset;
                        }
                    }
                    support.annotate(pcLine);
                } catch (IOException ioe) {
                    ByteCodesPanel panel = doc.getByteCodesPanel();
                    panel.displayError(ioe.toString());
                }
            }
        });
    }

    @Override
    public void suspended(SessionEvent sevt) {
        Session session = sevt.getSession();
        SessionManager sm = SessionProvider.getSessionManager();
        Session current = sm.getCurrent();
        if (current.equals(session)) {
            updateDisplay();
        }
    }

    /**
     * Get the current debugging location and display that method as
     * interpreted byte codes. If there is no curent location, clear
     * the display.
     */
    private void updateDisplay() {
        Session session = SessionProvider.getCurrentSession();
        DebuggingContext dc = ContextProvider.getContext(session);
        Location location = dc.getLocation();
        if (location == null) {
            clearDisplay();
        } else {
            // Without the constants pool, the BCEL interpreter will not
            // generate anything, so we must have the .class file in order
            // to show the method byte code.
            ByteCodesPanel bytecodesPanel = getByteCodesPanel();
            ReferenceType clazz = location.declaringType();
            PathManager pm = PathProvider.getPathManager(session);
            FileObject fobj = pm.findByteCode(clazz);
            if (fobj == null) {
                bytecodesPanel.displayError(NbBundle.getMessage(
                        ByteCodesView.class, "ERR_ByteCodesView_MissingFile",
                        clazz.name()));
                return;
            }
            try {
                InputStream classData = fobj.getInputStream();
                // Use the BCEL conveniently provided by the JDK.
                ClassParser parser = new ClassParser(classData, clazz.name());
                JavaClass jc = parser.parse();
                Method[] methods = jc.getMethods();
                String name = location.method().name();
                String sign = location.method().signature();
                // Search for the method within the class.
                for (Method method : methods) {
                    if (method.getName().equals(name) &&
                            method.getSignature().equals(sign)) {
                        Code code = method.getCode();
                        bytecodesPanel.showLocation(fobj, code, location);
                        showByteCodes(jc.getConstantPool(), code, location);
                        break;
                    }
                }
            } catch (FileNotFoundException fnfe) {
                bytecodesPanel.displayError(NbBundle.getMessage(
                        ByteCodesView.class, "ERR_ByteCodesView_MissingFile",
                        clazz.name()));
            } catch (IOException ioe) {
                bytecodesPanel.displayError(NbBundle.getMessage(
                        ByteCodesView.class, "ERR_ByteCodesView_CorruptFile",
                        clazz.name()));
            }
        }
    }

    /**
     * EditorSupport that handles the current program counter annotation.
     *
     * @author  Nathan Fiedler
     */
    private static class ByteCodesEditorSupport extends CloneableEditorSupport {
        /** Program counter annotation, if set. */
        private Annotation counterAnnotation;

        /**
         * Creates a new instance of ByteCodesEditorSupport.
         */
        public ByteCodesEditorSupport() {
            super(new ByteCodesEnvironment());
            ((ByteCodesEnvironment) env).setSupport(this);
        }

        /**
         * Adds the program counter annotation to the given line.
         *
         * @param  line  line number for annotation.
         * @return  the newly created annotation, or null if error.
         */
        public void annotate(int line) {
            // Clear out the old annotation first.
            removeAnnotation();
            final Line ln = getLine(line);
            if (ln != null) {
                counterAnnotation = new DebugAnnotation(
                        DebugAnnotation.CURRENT_PC_TYPE, ln);
                counterAnnotation.attach(ln);
//                // Show the line, but without disturbing the other open
//                // editors, such as the source editor, which is more
//                // valueable than this view. Must be done on EQ thread.
//                // For this to work, the caret must be installed.
//                EventQueue.invokeLater(new Runnable() {
//                    public void run() {
//// Forces the editor to the front, despite the description.
//// Seems to open a duplicate view, which causes problems.
//// Apparently our view is not uniquely identifiable.
//                        ln.show(Line.SHOW_TRY_SHOW);
//                    }
//                });
            }
        }

        /**
         * Get the Line instance at the given line.
         *
         * @param  line  desired line number.
         * @return  line from the line set, or null if not available.
         */
        private Line getLine(int line) {
            Line.Set ls = getLineSet();
            if (ls != null) {
                try {
                    return ls.getCurrent(line - 1);
                } catch (IndexOutOfBoundsException ioobe) {
                } catch (IllegalArgumentException iae) {
                }
            }
            return null;
        }

        @Override
        protected void loadFromStreamToKit(StyledDocument doc,
                InputStream stream, EditorKit kit)
                throws IOException, BadLocationException {
            // cleverly do nothing
        }

        @Override
        protected String messageName() {
            return NbBundle.getMessage(ByteCodesEditorSupport.class,
                    "CTL_ByteCodesView_Name");
        }

        @Override
        protected String messageOpened() {
            return "opened";
        }

        @Override
        protected String messageOpening() {
            return "opening";
        }

        @Override
        protected String messageSave() {
            return "save";
        }

        @Override
        protected String messageToolTip() {
            return NbBundle.getMessage(ByteCodesEditorSupport.class,
                    "CTL_ByteCodesView_Tooltip");
        }

        /**
         * Removes the program counter annotation.
         */
        public void removeAnnotation() {
            if (counterAnnotation != null) {
                counterAnnotation.detach();
                counterAnnotation = null;
            }
        }

        @Override
        protected void saveFromKitToStream(StyledDocument doc,
                EditorKit kit, OutputStream stream)
                throws IOException, BadLocationException {
            // cleverly do nothing
        }
    }

    /**
     * Simple editor support environment implementation.
     *
     * @author  Nathan Fiedler
     */
    private static class ByteCodesEnvironment implements CloneableEditorSupport.Env {
        /** silence the compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Our support instance. */
        private ByteCodesEditorSupport support;

        /**
         * Creates a new instance of ByteCodesEnvironment.
         */
        public ByteCodesEnvironment() {
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void addVetoableChangeListener(VetoableChangeListener l) {
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return support;
        }

        @Override
        public String getMimeType() {
            return MIME_TYPE;
        }

        @Override
        public Date getTime() {
            return new Date();
        }

        @Override
        public InputStream inputStream() throws IOException {
            return null;
        }

        @Override
        public boolean isModified() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void markModified() throws IOException {
        }

        @Override
        public OutputStream outputStream() throws IOException {
            return null;
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }

        public void setSupport(ByteCodesEditorSupport support) {
            this.support = support;
        }

        @Override
        public void unmarkModified() {
        }
    }

    /**
     * This class must remain static so the JEditorPane factory can access
     * it when creating the view. All it does is create a document to show
     * the byte codes.
     *
     * @author  Nathan Fiedler
     */
    public static class ByteCodesEditorKit extends PlainKit {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Public constructor for the JEditorPane factory to create us.
         */
        public ByteCodesEditorKit() {
        }

        @Override
        public Object clone() {
            return new ByteCodesEditorKit();
        }

        @Override
        public Document createDefaultDocument() {
            return new ByteCodesDocument(this.getClass());
        }

        @Override
        public void install(JEditorPane c) {
            super.install(c);
            // Remove the caret to enhance the "read-only" feeling of this
            // editor. However, this prevents the Line.show() from working.
            // But since that doesn't work right, either, it doesn't matter.
            c.getCaret().deinstall(c);
        }
    }

    /**
     * The document which contains the byte codes. This class just provides
     * a custom toolbar, which is where the location information is shown.
     *
     * @author  Nathan Fiedler
     */
    private static class ByteCodesDocument extends NbEditorDocument
            implements NbDocument.CustomToolbar {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;
        /** Where some of the information is displayed. */
        private ByteCodesPanel byteCodesPanel;

        /**
         * Creates a new instance of ByteCodesDocument.
         *
         * @param  kitClass  the editor kit class.
         */
        public ByteCodesDocument(Class kitClass) {
            super(kitClass);
            byteCodesPanel = new ByteCodesPanel();
        }

        @Override
        public void addUndoableEditListener(UndoableEditListener listener) {
            // Do nothing so as to be read-only.
        }

        /**
         * Append to the end of the document, using the default style.
         *
         * @param  text  text to be appended to document.
         */
        public void append(String text) {
            try {
                super.insertString(getLength(), text, null);
            } catch (BadLocationException ble) {
                // This is very unlikely.
                ErrorManager.getDefault().notify(ble);
            }
        }

        /**
         * Clear the document contents.
         */
        public void clear() {
            try {
                super.remove(0, getLength());
            } catch (BadLocationException ble) {
                // This is very unlikely.
                ErrorManager.getDefault().notify(ble);
            }
        }

        @Override
        public JToolBar createToolbar(JEditorPane pane) {
            JToolBar tbar = new JToolBar();
            tbar.add(byteCodesPanel);
            return tbar;
        }

        /**
         * Retrieves the display panel associated with this document.
         *
         * @return  the ByteCodesPanel instance.
         */
        public ByteCodesPanel getByteCodesPanel() {
            return byteCodesPanel;
        }

        @Override
        public void insertString(int offset, String str, AttributeSet a)
                throws BadLocationException {
            // Do nothing so as to be read-only.
        }

        @Override
        public void remove(int offs, int len) throws BadLocationException {
            // Do nothing so as to be read-only.
        }
    }
}
