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

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import ic.doc.ltsa.common.iface.IAnalysisFactory;
import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.ITransitionPrinter;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.EventClient;
import ic.doc.ltsa.common.infra.EventManager;
import ic.doc.ltsa.common.infra.LTSEvent;


public class PrintWindow extends JSplitPane implements LTSOutput, EventClient {

    public static boolean fontFlag = false;

    private IAnalysisFactory oAnalysisFactory;

    JTextArea output;
    JList list;
    JScrollPane left,right;
    EventManager eman;
    int Nmach;
    int selectedMachine = 0;
	ICompactState [] sm; //an array of machines
    Font f1 = new Font("Monospaced",Font.PLAIN,12);
    Font f2 = new Font("Monospaced",Font.BOLD,18);
    Font f3 = new Font("SansSerif",Font.PLAIN,12);
    Font f4 = new Font("SansSerif",Font.BOLD,18);
    PrintWindow thisWindow;
    private final static int MAXPRINT = 400;

    public PrintWindow (ICompositeState cs,EventManager eman , IAnalysisFactory pAnalFact ) {
      super();
      this.eman = eman;
      thisWindow = this;
      oAnalysisFactory = pAnalFact;
      //scrollable output pane
      output = new JTextArea(23,50);
      output.setEditable(false);
      right = new JScrollPane
                          (output,
                             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                            );
		  output.setBackground(Color.white);
      output.setBorder(new EmptyBorder(0,5,0,0));
      //scrollable list pane
      list = new JList();
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.addListSelectionListener(new PrintAction());
      left = new JScrollPane
                          (list,
                             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                            );
	    if (eman!=null) eman.addClient(this);
	    new_machines(cs);
      setLeftComponent(left);
      setRightComponent(right);
      setDividerLocation(200);
      setBigFont(fontFlag);
      validate();
    }

	//------------------------------------------------------------------------

  

  class PrintAction implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
        int machine = list.getSelectedIndex();
        if (machine<0 || machine >=Nmach) return;
        selectedMachine = machine;
        clearOutput();

        // now, CompactState knows how to do this itself
        ITransitionPrinter p = oAnalysisFactory.createTransitionPrinter(sm[selectedMachine]);
        p.print(thisWindow,MAXPRINT);

	//	sm[selectedMachine].printTransitions(thisWindow,MAXPRINT);
    }
  }

/*---------LTS event broadcast action-----------------------------*/
	public void ltsAction(LTSEvent e ) {
	    switch(e.kind){
	    case LTSEvent.NEWSTATE:
    	    break;
    	case LTSEvent.INVALID:
    	    new_machines((ICompositeState)e.info);
		    break;
		  case LTSEvent.KILL:
		    //this.dispose();
		    break;
        default:;
        }
	}

//------------------------------------------------------------------------

	public void out ( String str ) {
		output.append(str);
	}

	public void outln ( String str ) {
		output.append(str+"\n");
	}

	public void clearOutput () {
		output.setText("");
	}

	private void new_machines(ICompositeState cs){
     int hasC = (cs!=null && cs.getComposition()!=null)?1:0;
     if (cs !=null && cs.getMachines() !=null && cs.getMachines().size()>0) { // get set of machines
         sm = new ICompactState[cs.getMachines().size()+hasC];
         Enumeration e = cs.getMachines().elements();
         for(int i=0; e.hasMoreElements(); i++)
            sm[i] = (ICompactState)e.nextElement();
         Nmach = sm.length;
         if (hasC==1)
            sm[Nmach-1] = cs.getComposition();
     } else
         Nmach = 0;
	  DefaultListModel lm = new DefaultListModel();
		for(int i=0;i<Nmach;i++) {
		    if (hasC==1 && i== (Nmach-1))
		        lm.addElement("||"+sm[i].getName());
		    else
		        lm.addElement(sm[i].getName());
		}
    list.setModel(lm);
    if (selectedMachine>=Nmach) selectedMachine = 0;
	  clearOutput();
	}
    
  //--------------------------------------------------------------------
  public void setBigFont(boolean b) {
      fontFlag = b;
      if (fontFlag) {
         output.setFont(f2);
         list.setFont(f4);
      } else {
         output.setFont(f1);
         list.setFont(f3);
      }
  }

  //--------------------------------------------------------------------
  public void removeClient() {
   if (eman!=null) eman.removeClient(this);
  }
  
  public void copy() {
    output.copy();
  }
  
  //------------------------------------------------------------------------

    public void saveFile (String currentDirectory, String extension) {
    String message;
    if (extension.equals(".txt"))
      message = "Save text in:";
    else
      message = "Save as Aldebaran format (.aut) in:";
    FileDialog fd = new FileDialog ((Frame)getTopLevelAncestor(), message, FileDialog.SAVE);
    if (Nmach>0) {
        String fname = sm[selectedMachine].getName();
        int colon = fname.indexOf(':',0);
        if (colon>0) fname = fname.substring(0,colon);
        fd.setFile(fname+extension);
        fd.setDirectory(currentDirectory);
    }
    fd.setVisible(true);
    String sn;
    if ((sn= fd.getFile()) != null)
    try {
    	 int i = sn.indexOf('.',0);
      sn = sn.substring(0,i) + extension; 
      File file = new File(fd.getDirectory(),sn);
      FileOutputStream fout =  new FileOutputStream(file);
      // now convert the FileOutputStream into a PrintStream
      PrintStream myOutput = new PrintStream(fout);
      if (extension.equals(".txt")) {
          String text = output.getText();
          myOutput.print(text);
      } else {
         if (Nmach>0) sm[selectedMachine].printAUT(myOutput);
      }
      myOutput.close();
      fout.close();
      //outln("Saved in: "+ fd.getDirectory()+file);
    }
    catch (IOException e) {
      outln("Error saving file: " + e);
    }
  }
  
 
	public void saveAsPrism (String currentDirectory, String extension) {
		
		// removed when cleaning up stochastic stuff
		
		String message;
		if (extension.equals(".txt")) {
			message = "Save text in:";
		} else {
			message = "Save as PRISM format (.pm) in:";
		}
		
		FileDialog fd = new FileDialog ((Frame)getTopLevelAncestor(), message, FileDialog.SAVE);
		
		if (Nmach>0) {
			String fname = sm[selectedMachine].getName();
			int colon = fname.indexOf(':',0);
			if (colon>0) fname = fname.substring(0,colon);
			fd.setFile(fname+extension);
			fd.setDirectory(currentDirectory);
		}
		
		fd.setVisible(true);
		String sn;
		
		if ((sn= fd.getFile()) != null)
		
			try {
				int i = sn.indexOf('.',0);
				sn = sn.substring(0,i) + extension; 
				File file = new File(fd.getDirectory(),sn);
				FileOutputStream fout =  new FileOutputStream(file);
				//now convert the FileOutputStream into a PrintStream
				PrintStream myOutput = new PrintStream(fout);
				if (extension.equals(".txt")) {
					String text = output.getText();
					myOutput.print(text);
				} else {
					if (Nmach>0) sm[selectedMachine].printPrism(myOutput);
				}
				myOutput.close();
				fout.close();
				//outln("Saved in: "+ fd.getDirectory()+file);
			}
		catch (IOException e) {
			outln("Error saving file: " + e);
		}
		
	}
	
	
}
