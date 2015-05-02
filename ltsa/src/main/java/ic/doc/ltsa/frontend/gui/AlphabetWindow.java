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
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import ic.doc.ltsa.common.iface.IAlphabet;
import ic.doc.ltsa.common.iface.IAnalysisFactory;
import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.EventClient;
import ic.doc.ltsa.common.infra.EventManager;
import ic.doc.ltsa.common.infra.LTSEvent;

public class AlphabetWindow extends JSplitPane implements LTSOutput, EventClient {

    public static boolean fontFlag = false;
    private IAnalysisFactory oAnalysisFactory;

    JTextArea output;
    JList list;
    JScrollPane left,right;
    EventManager eman;
    int Nmach;
    int selectedMachine = 0;
    IAlphabet current = null;
    int expandLevel = 0;
	  ICompactState [] sm; //an array of machines
    Font f1 = new Font("Monospaced",Font.PLAIN,12);
    Font f2 = new Font("Monospaced",Font.BOLD,18);
    Font f3 = new Font("SansSerif",Font.PLAIN,12);
    Font f4 = new Font("SansSerif",Font.BOLD,18);
    AlphabetWindow thisWindow;
    private final static int MAXPRINT = 400;

    public AlphabetWindow (ICompositeState cs,EventManager eman,IAnalysisFactory pAnalFact) {
      super();
      this.eman = eman;
      oAnalysisFactory = pAnalFact;
      thisWindow = this;
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
      JPanel fortools = new JPanel(new BorderLayout());
      fortools.add("Center",right);
      // tool bar
      JToolBar tools = new JToolBar();
      tools.setOrientation(JToolBar.VERTICAL);
      fortools.add("West",tools);
      tools.add(createTool("icon/expanded.gif","Expand Most",new ExpandMostAction()));
      tools.add(createTool("icon/expand.gif","Expand",new ExpandMoreAction()));
      tools.add(createTool("icon/collapse.gif","Collapse",new ExpandLessAction()));
      tools.add(createTool("icon/collapsed.gif","Most Concise",new ExpandLeastAction()));
	    if (eman!=null) eman.addClient(this);
	    new_machines(cs);
      setLeftComponent(left);
      setRightComponent(fortools);
      setDividerLocation(150);
      setBigFont(fontFlag);
      validate();
    }

	//------------------------------------------------------------------------

  class ExpandMoreAction implements ActionListener {
     public void actionPerformed(ActionEvent e) { 
       if (current == null) return;
       if(expandLevel<current.getMaxLevel()) ++expandLevel;
       clearOutput();
       current.print(thisWindow,expandLevel);
     }
  }
  
 class ExpandLessAction implements ActionListener {
     public void actionPerformed(ActionEvent e) { 
       if (current == null) return;
       if(expandLevel>0) --expandLevel;
       clearOutput();
       current.print(thisWindow,expandLevel);
     }
  }
 
  class ExpandMostAction implements ActionListener {
     public void actionPerformed(ActionEvent e) { 
       if (current == null) return;
       expandLevel = current.getMaxLevel();
       clearOutput();
       current.print(thisWindow,expandLevel);
     }
  }

  class ExpandLeastAction implements ActionListener {
     public void actionPerformed(ActionEvent e) { 
       if (current == null) return;
       expandLevel = 0;
       clearOutput();
       current.print(thisWindow,expandLevel);
     }
  }

  class PrintAction implements ListSelectionListener {
   
    public void valueChanged(ListSelectionEvent e) {
        int machine = list.getSelectedIndex();
        if (machine<0 || machine >=Nmach) return;
        selectedMachine = machine;
        clearOutput();
        current= oAnalysisFactory.createAlphabet( sm[machine] );
        if (expandLevel>current.getMaxLevel()) expandLevel = current.getMaxLevel();
        current.print(thisWindow,expandLevel);
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
            sm[Nmach-1] = (ICompactState)cs.getComposition();
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
    current=null;
	  clearOutput();
	}

//------------------------------------------------------------------------

  protected JButton createTool(String icon, String tip, ActionListener act) {
      JButton b 
          = new JButton(new ImageIcon(this.getClass().getResource(icon))){
            public float getAlignmentY() { return 0.5f; }
      };
      b.setRequestFocusEnabled(false);
      b.setMargin(new Insets(0,0,0,0));
      b.setToolTipText(tip);
      b.addActionListener(act);
      return b;
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

    public void saveFile () {

    FileDialog fd = new FileDialog ((Frame)getTopLevelAncestor(), "Save text in:", FileDialog.SAVE);
      if (Nmach>0) {
        String fname = sm[selectedMachine].getName();
        int colon = fname.indexOf(':',0);
        if (colon>0) fname = fname.substring(0,colon);
        fd.setFile(fname+".txt");
    }
    fd.setVisible(true);
    String file = fd.getFile();
        if (file != null)
    try {
        int i = file.indexOf('.',0);
        file = file.substring(0,i) + "."+"txt";
      FileOutputStream fout =  new FileOutputStream(fd.getDirectory()+file);
      // now convert the FileOutputStream into a PrintStream
      PrintStream myOutput = new PrintStream(fout);
      String text = output.getText();
      myOutput.print(text);
      myOutput.close();
      fout.close();
      //outln("Saved in: "+ fd.getDirectory()+file);
    }
    catch (IOException e) {
      outln("Error saving file: " + e);
    }
  }

}
