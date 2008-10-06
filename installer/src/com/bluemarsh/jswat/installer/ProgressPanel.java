/*
 *                     Sun Public License Notice.
 *
 * The contents of this file are subject to the Sun Public License 
 * Version 1.0 (the "License"); you may not use this file except in 
 * compliance with the License. A copy of the License is available at 
 * http://www.sun.com/
 *
 * The Original Code is JSwat Installer. The Initial Developer of the 
 * Original Code is Nathan L. Fiedler. Portions created by Nathan L. Fiedler
 * are Copyright (C) 2005-2006. All Rights Reserved.
 *
 * Contributor(s): Nathan L. Fiedler.
 *
 * $Id: ProgressPanel.java 15 2007-06-03 00:01:17Z nfiedler $
 */

package com.bluemarsh.jswat.installer;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Performs the actual installation, showing the progress until completion.
 *
 * @author  Nathan Fiedler
 */
public class ProgressPanel extends InstallerPanel implements Runnable {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** If true, we can proceed to the next panel. */
    private boolean installSuccessful;
    /** Thread on which the installation is performed. */
    private Thread workerThread;
    /** Name of the current zip entry (for inner class to access). */
    private String currentEntryName;
    /** Percent complete of the installation process (for inner class). */
    private double percentComplete;

    /**
     * Creates new form ProgressPanel.
     */
    public ProgressPanel() {
        initComponents();
    }

    public void cancelInstall() {
        waitLabel.setText(Bundle.getString("MSG_Progress_CleanWait"));
        if (workerThread != null) {
            // Signal and wait for other thread to stop.
            workerThread.interrupt();
            try {
                workerThread.join();
            } catch (InterruptedException ie) {
                // Best just to ignore and move on.
            }
            workerThread = null;
        }
        String home = Controller.getDefault().getProperty("home");
        File dir = new File(home);
        if (dir.exists() && dir.isDirectory()) {
            deleteTree(dir);
        }
        installSuccessful = false;
    }

    /**
     * Deletes an entire directory structure.
     *
     * @param  dir  directory tree to be deleted.
     */
    private void deleteTree(File dir) {
        // Process this directory tree recursively, deleting along the way.
        File[] children = dir.listFiles();
        for (int ii = 0; ii < children.length; ii++) {
            File child = children[ii];
            if (child.isDirectory()) {
                // For the directories, recursively descend and delete.
                deleteTree(child);
            } else {
                // For the files in this directory, delete them immediately.
                child.delete();
            }
        }
        // Finally delete this directory.
        dir.delete();
    }

    public void doHide() {
    }

    public void doShow() {
        // Spawn a new thread and perform the work there.
        workerThread = new Thread(this, "installer");
        workerThread.start();
    }

    public String getNext() {
        if (installSuccessful) {
            return "summary";
        } else {
            return null;
        }
    }

    public String getPrevious() {
        // This is the point of no return, user cannot go back.
        return null;
    }

    /**
     * Perform the actual installation.
     */
    public void run() {
        Runnable labelSetter = new Runnable() {
            public void run() {
                fileLabel.setText(currentEntryName);
            }
        };
        Runnable progressor = new Runnable() {
            public void run() {
                int value = (int) percentComplete;
                progressBar.setValue(value);
            }
        };
        // Calculate percentage for each file in the .zip.
        double percent = 0.0;
        try {
            // This value was set in the review panel.
            String countStr = Controller.getDefault().getProperty("fileCount");
            int count = Integer.parseInt(countStr);
            percent = 100.0 / count;
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }
        percentComplete = 0;

        // Perform the installation.
        String home = Controller.getDefault().getProperty("home");
        InputStream is = ClassLoader.getSystemResourceAsStream("jswat.zip");
        ZipInputStream zis = new ZipInputStream(is);
        try {
            // Create the destination directory so that if the first entry
            // in the .zip is a file, we have a place to put it.
            new File(home).mkdirs();
            byte[] buffer = new byte[1024];
            ZipEntry entry = zis.getNextEntry();
            // Process each of the entries in the .zip file, unless we
            // were interrupted by the user.
            while (entry != null && !Thread.interrupted()) {
                if (entry.isDirectory()) {
                    String path = entry.getName();
                    File dir = new File(home, path);
                    dir.mkdirs();
                } else {
                    currentEntryName = entry.getName();
                    // Set the fileLabel to the entry name.
                    EventQueue.invokeLater(labelSetter);
                    // Extract the file to 'home' directory.
                    File file = new File(home, currentEntryName);
                    FileOutputStream fos = new FileOutputStream(file);
                    int bytesRead = zis.read(buffer);
                    while (bytesRead != -1) {
                        fos.write(buffer, 0, bytesRead);
                        bytesRead = zis.read(buffer);
                    }
                    fos.close();
                    // Increment the progress by the percentage value.
                    percentComplete += percent;
                    EventQueue.invokeLater(progressor);
                }
                zis.closeEntry();
                entry = zis.getNextEntry();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                zis.close();
            } catch (IOException ioe) { }
        }

        // Fix up the .conf file to work on this system.
        try {
            // First slurp the file into memory so we can easily overwrite it.
            File file = new File(home + File.separator +
                    "etc" + File.separator + "jswat.conf");
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            StringWriter sw = new StringWriter(1024);
            char[] buffer = new char[1024];
            int charsRead = isr.read(buffer);
            while (charsRead != -1) {
                sw.write(buffer, 0, charsRead);
                charsRead = isr.read(buffer);
            }
            isr.close();

            // Now process the file as we write it back out again.
            // - Change the EOL character to the appropriate value.
            // - Set the userdir value with the right file separator.
            // - Set the JDK home value per user-input.
            StringReader sr = new StringReader(sw.toString());
            BufferedReader br = new BufferedReader(sr);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            String line = br.readLine();
            while (line != null) {
                if (line.indexOf("netbeans_default_userdir") >= 0) {
                    bw.write("netbeans_default_userdir=\"${HOME}");
                    bw.write(File.separator);
                    bw.write(".jswat\"");
                } else if (line.indexOf("netbeans_jdkhome") >= 0) {
                    bw.write("netbeans_jdkhome=\"");
                    String jdk = Controller.getDefault().getProperty("jdk");
                    bw.write(jdk);
                    bw.write("\"");
                } else {
                    bw.write(line);
                }
                bw.newLine();
                line = br.readLine();
            }
            bw.close();
            osw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // Fix the permissions on the the executable files.
        if (File.separatorChar == '/') {
            // Chances are this is Unix and chmod is available.
            try {
                Runtime rt = Runtime.getRuntime();
                rt.exec("chmod 755 " + home + "/bin/jswat");
                rt.exec("chmod 755 " + home + "/bin/jpdalaunch");
                rt.exec("chmod 755 " + home + "/platform6/lib/nbexec");
            } catch (IOException ioe) {
                System.err.println(Bundle.getString("MSG_Progress_ExecError"));
            }
        }

        // Indicate completeness.
        installSuccessful = true;
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Controller.getDefault().next();
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        waitLabel = new javax.swing.JLabel();
        fileLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        spacerPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 12, 12)));
        waitLabel.setText(java.util.ResourceBundle.getBundle("com/bluemarsh/jswat/installer/Form").getString("LBL_Progress_Wait"));
        waitLabel.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 18, 0);
        add(waitLabel, gridBagConstraints);

        fileLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        fileLabel.setLabelFor(progressBar);
        fileLabel.setText("   ");
        fileLabel.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(fileLabel, gridBagConstraints);

        progressBar.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(progressBar, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(spacerPanel1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel fileLabel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel spacerPanel1;
    private javax.swing.JLabel waitLabel;
    // End of variables declaration//GEN-END:variables
}
