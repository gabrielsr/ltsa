/******************************************************************************
 * LTSA (Labelled Transition System Analyser) - LTSA is a verification tool   *
 * for concurrent systems. It mechanically checks that the specification of a *
 * concurrent system satisfies the properties required of its behaviour.      *
 * Copyright (C) 2001-2004 Jeff Magee (with additions by Robert Chatley)      *
 *                                                                            *
 * This program is free software; you can redistribute it and/or              *
 * modify it under the terms of the GNU General Public License                *
 * as published by the Free Software Foundation; either version 2             *
 * of the License, or (at your option) any later version.                     *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program; if not, write to the Free Software                *
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA *
 *                                                                            *
 * The authors can be contacted by email at {jnm,rbc}@doc.ic.ac.uk            *
 *                                                                            *
 ******************************************************************************/

package ic.doc.ltsa.frontend.gui;

/* a specialised Applet button to launch LTSA
*/

import ic.doc.ltsa.frontend.HPWindow;

import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;
import javax.swing.UIManager;

public class AppletButton extends Applet implements Runnable {
    Button button;
    Thread windowThread;
    boolean pleaseCreate = false;
    HPWindow window = null;

    public void init() {

        setLayout(new BorderLayout());
        button = new Button("Launch LTSA");
        button.setFont(new Font("Helvetica", Font.BOLD, 18));
        button.addActionListener(new ButtonAction());
        add("Center",button);
    }

    public void start() {
        if (windowThread == null) {
            windowThread = new Thread(this);
            windowThread.start();
        }
    }

    public void stop() {
        windowThread=null;
        if (window!=null) window.dispose();
    }
    
    public void ended() {
    	if (window!=null) window=null;
    }

    public synchronized void run() {
        while (windowThread != null) {
          while (pleaseCreate == false) {
             try { wait();} catch (InterruptedException e) {}
          }
          pleaseCreate=false;
         try {
           String lf = UIManager.getSystemLookAndFeelClassName();
           UIManager.setLookAndFeel(lf);
         } catch(Exception e) {}
         if (window==null) {
         	  showStatus("Please wait while the window comes up...");
			    window = new HPWindow(this);
		 	    window.setTitle("LTS Analyser");
			    window.pack();
	         ic.doc.ltsa.frontend.HPWindow.centre(window);
	         window.setVisible(true);
	         showStatus("");
         }
        }
    }
    
    synchronized void triggerWindow() {
    	   pleaseCreate = true;
    	   notify();
    }
    
   class ButtonAction implements ActionListener {
    	public void actionPerformed(ActionEvent e) { triggerWindow();}
    }

}
