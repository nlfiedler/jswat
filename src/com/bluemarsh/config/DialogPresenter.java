/*********************************************************************
 *
 *	Copyright (C) 2000-2005 Nathan Fiedler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id: DialogPresenter.java 1814 2005-07-17 05:56:32Z nfiedler $
 *
 ********************************************************************/

package com.bluemarsh.config;

import java.awt.Dialog;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Class DialogPresenter is responsible for listening to the Ok and
 * Cancel buttons, then notifying a configure listener of the results.
 *
 * @author  Nathan Fiedler
 */
class DialogPresenter implements ActionListener {
    /** Dialog information. */
    protected DialogInfo dialogInfo;
    /** True if preferences were accepted by the user. */
    protected boolean preferencesAccepted;
    /** Configure listener listening to the dialog. */
    protected ConfigureListener configureListener;

    /**
     * Constructs a new DialogPresenter object. It will be added
     * to the Ok and cancel buttons as an action listener.
     *
     * @param  info  Dialog information.
     */
    public DialogPresenter(DialogInfo info) {
        this.dialogInfo = info;
        dialogInfo.okButton.addActionListener(this);
        dialogInfo.cancelButton.addActionListener(this);
        dialogInfo.dialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    // Dialog is being closed, preferences are not accepted.
                    preferencesAccepted = false;
                }
            });
    } // DialogPresenter

    /**
     * Invoked when a button has been pressed.
     *
     * @param  e  Action event.
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == dialogInfo.okButton) {
            preferencesAccepted = true;
            if (configureListener != null) {
                configureListener.configurationChanged();
            }
        } else {
            preferencesAccepted = false;
        }
        // Close the dialog.
        dialogInfo.dialog.dispose();
    } // actionPerformed

    /**
     * Checks if the preferences dialog is showing on screen.
     *
     * @return  true if dialog is visible, false otherwise.
     */
    public boolean isShowing() {
        if (dialogInfo != null) {
            return dialogInfo.dialog.isShowing();
        }
        return false;
    } // isShowing

    /**
     * Presents the preferences dialog to the user. If for whatever
     * reason the dialog is non-modal, the return value of this
     * method will not be meaningful.
     *
     * @return  True if preferences were accepted, false otherwise.
     * @exception  IllegalStateException
     *             Thrown if dialog has not been built.
     */
    public boolean present() {
        if (dialogInfo.dialog != null) {
            preferencesAccepted = false;

            Dialog dialog = dialogInfo.dialog;

            // Center the dialog on the owning frame.
            Rectangle windowBounds = dialog.getOwner().getBounds();
            Rectangle dialogBounds = dialog.getBounds();
            int x = ((windowBounds.width - dialogBounds.width) / 2) +
                windowBounds.x;
            int y = ((windowBounds.height - dialogBounds.height) / 2) +
                windowBounds.y;
            dialog.setLocation(x, y);

            // Show the dialog.
            dialog.setVisible(true);
        } else {
            throw new IllegalStateException("dialog not yet built");
        }
        // If the dialog is modal, this will return a meaningful value.
        return preferencesAccepted;
    } // present

    /**
     * Presents the preferences dialog in non-modal form. If a configure
     * listener is provided, it will be notified if the preferences
     * have been accepted.
     *
     * @param  listener  Configure listener, or null if notification
     *                   is not necessary.
     * @exception  IllegalStateException
     *             Thrown if dialog has not been built.
     */
    public void present(ConfigureListener listener) {
        configureListener = listener;
        // We can call this method and ignore the return value.
        present();
    } // present
} // DialogPresenter
