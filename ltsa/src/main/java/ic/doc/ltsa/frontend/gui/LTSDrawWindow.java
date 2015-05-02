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
import javax.swing.event.*;

import ic.doc.ltsa.common.iface.IAnalysisFactory;
import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.IDrawMachine;
import ic.doc.ltsa.common.infra.EventClient;
import ic.doc.ltsa.common.infra.EventManager;
import ic.doc.ltsa.common.infra.LTSEvent;

import ic.doc.ltsa.frontend.dclap.*;

public class LTSDrawWindow extends JSplitPane implements EventClient {

    LTSCanvas output;
    EventManager eman;
    ICompositeState cs;
    int[] lastEvent,prevEvent;
    String lastName;
    int Nmach = 0;  //the number of machines
    int hasC = 0;   //1 if last machine is composition
    ICompactState [] sm; //an array of machines
    boolean[] machineHasAction;
    boolean[] machineToDrawSet;
  
    private IAnalysisFactory oAnalysisFactory;
    
    public static boolean fontFlag = false;
    public static boolean singleMode = false;

    JList list;
    JScrollPane left,right;

    Font f1 = new Font("Monospaced",Font.PLAIN,12);
    Font f2 = new Font("Monospaced",Font.BOLD,16);
    Font f3 = new Font("SansSerif",Font.PLAIN,12);
    Font f4 = new Font("SansSerif",Font.BOLD,16);
    
    ImageIcon drawIcon = new ImageIcon(this.getClass().getResource("/ic/doc/ltsa/frontend/gui/icon/draw.gif"));
    
    public LTSDrawWindow (ICompositeState cs, EventManager eman, IAnalysisFactory pAnalFact) {
      super();
      this.eman = eman;
      //output canvas
      output = new LTSCanvas(singleMode); // , oAnalysisFactory );
      JPanel outPane = new JPanel();
      outPane.setLayout(new BorderLayout());
      outPane.add("Center",output);
      right = new JScrollPane
                          (outPane,
                             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                            );
      //right.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);
      //scrollable list pane
      list = new JList();
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.addListSelectionListener(new PrintAction());
      list.setCellRenderer(new MyCellRenderer()); 
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
      tools.add(createTool("icon/stretchHorizontal.gif","Stretch Horizontal",new HStretchAction(10)));
      tools.add(createTool("icon/compressHorizontal.gif","Compress Horizontal",new HStretchAction(-10)));
      tools.add(createTool("icon/stretchVertical.gif","Stretch Vertical",new VStretchAction(10)));
      tools.add(createTool("icon/compressVertical.gif","Compress Vertical",new VStretchAction(-10)));
      if (eman!=null) eman.addClient(this);
	    new_machines(cs);
      setLeftComponent(left);
      setRightComponent(fortools);
      setDividerLocation(200);
      setBigFont(fontFlag);
      validate();
      output.addKeyListener(new KeyPress());
      output.addMouseListener(new MyMouse());
    }

	//------------------------------------------------------------------------

  class HStretchAction implements ActionListener {
     int increment;
     HStretchAction(int i) {increment = i;}
     public void actionPerformed(ActionEvent e) { 
        if (output!=null) output.stretchHorizontal(increment);
     }
  }

  class VStretchAction implements ActionListener {
     int increment;
     VStretchAction(int i) {increment = i;}
     public void actionPerformed(ActionEvent e) { 
        if (output!=null) output.stretchVertical(increment);
     }
  }
  
  class PrintAction implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() && !singleMode) return;
        int machine = list.getSelectedIndex();
        if (machine<0 || machine >=Nmach) return;
        if (singleMode) {
          output.draw(machine,sm[machine],
                    validMachine(machine,prevEvent),
                    validMachine(machine,lastEvent),lastName);
        } else {
           if (!machineToDrawSet[machine]) {
              output.draw(machine,sm[machine],
                    validMachine(machine,prevEvent),
                    validMachine(machine,lastEvent),lastName);
              machineToDrawSet[machine]=true;
           } else {
              output.clear(machine);
              machineToDrawSet[machine]=false;
           }
           list.clearSelection();
        }
    }
  }
  
  private int validMachine(int machine,int [] event) {
        if (event!=null && machine<(Nmach-hasC))
            return event[machine];
        else
            return 0;
  }

class KeyPress extends KeyAdapter {
      public void keyPressed(KeyEvent k) {
        if (output == null) return;
        int code = k.getKeyCode();
        if (code ==KeyEvent.VK_LEFT) {
           output.stretchHorizontal(-5);
        } else if (code == KeyEvent.VK_RIGHT) {
           output.stretchHorizontal(5);
        } else if (code ==KeyEvent.VK_UP) {
            output.stretchVertical(-5);
        } else if (code == KeyEvent.VK_DOWN) {
            output.stretchVertical(5);
        } else if (code == KeyEvent.VK_BACK_SPACE) {
            int m = output.clearSelected();
            if (m>=0) {
               machineToDrawSet[m]=false;
               list.repaint();
            }
        }
      }
}
 
 class MyMouse extends MouseAdapter {
    public void mouseEntered(MouseEvent e) {
       output.requestFocus();
    }
 }

/*---------LTS event broadcast action-----------------------------*/

  public void ltsAction(LTSEvent e ) {
      switch(e.kind){
      case LTSEvent.NEWSTATE:
           prevEvent = lastEvent;
           lastEvent = (int[])e.info;
           lastName  = e.name;
           output.select(Nmach-hasC,prevEvent,lastEvent,e.name);
           buttonHighlight(e.name);
          break;
      case LTSEvent.INVALID:
          prevEvent = null;
          lastEvent = null;
          new_machines(cs=(ICompositeState)e.info);
        break;
    case LTSEvent.KILL:
        break;
        default:;
        }
  }

  private void buttonHighlight(String label) {
      if (label==null && machineHasAction!=null) {
          for (int i = 0; i<machineHasAction.length; i++)
              machineHasAction[i] = false;
      } else if (machineHasAction!=null) {
          for (int i = 0; i<sm.length-hasC; i++)
             machineHasAction[i] = (!label.equals("tau") && sm[i].hasLabel(label));
      }
      list.repaint();
      return;
  }



	private void new_machines(ICompositeState cs){
     hasC = (cs!=null && cs.getComposition()!=null)?1:0;
     if (cs !=null && cs.getMachines() !=null && cs.getMachines().size()>0) { // get set of machines
         sm = new ICompactState[cs.getMachines().size()+hasC];
         Enumeration e = cs.getMachines().elements();
         for(int i=0; e.hasMoreElements(); i++)
            sm[i] = (ICompactState)e.nextElement();
         Nmach = sm.length;
         if (hasC==1)
            sm[Nmach-1] = cs.getComposition();
         machineHasAction = new boolean[Nmach];
         machineToDrawSet = new boolean[Nmach];
     } else {
         Nmach = 0;
         machineHasAction = null;
         machineToDrawSet = null;
     }
	  DefaultListModel lm = new DefaultListModel();
		for(int i=0;i<Nmach;i++) {
		    if (hasC==1 && i== (Nmach-1))
		        lm.addElement("||"+sm[i].getName());
		    else
		        lm.addElement(sm[i].getName());
		}
    list.setModel(lm);
    output.setMachines(Nmach);
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
      output.setBigFont(b);
  }
  
  public void setDrawName(boolean b){
      output.setDrawName(b);
  }
  
  public void setNewLabelFormat(boolean b){
      output.setNewLabelFormat(b);
  }
  
  public void setMode(boolean b){
      singleMode = b;
      output.setMode(b);
      list.clearSelection();
      if (Nmach>0) machineToDrawSet = new boolean[Nmach];
      list.repaint();
  }

  //--------------------------------------------------------------------
  public void removeClient() {
   if (eman!=null) eman.removeClient(this);
  }
    
  //------------------------------------------------------------------------

  class MyCellRenderer extends JLabel implements ListCellRenderer {
     public MyCellRenderer() {         
       setOpaque(true);
       setHorizontalTextPosition(SwingConstants.LEFT);    
     }
     
     public Component getListCellRendererComponent( 
                JList list, Object value, int index,         
                boolean isSelected, boolean cellHasFocus)    
    {
        setFont(fontFlag ? f4 : f3);
        setText(value.toString());
        setBackground(isSelected ? Color.blue : Color.white);
        setForeground(isSelected ? Color.white : Color.black);
        if (machineHasAction!=null && machineHasAction[index]) {
              setBackground( Color.red );
              setForeground( Color.white);
        }
        setForeground(isSelected ? Color.white : Color.black);
        setIcon(machineToDrawSet[index] && !singleMode ? drawIcon : null);
        return this;     
     } 
  }
  
  /* --------------------------------------------*/
 
 public void saveFile () {
    IDrawMachine dm = output.getDrawing();
    if (dm == null) {
      JOptionPane.showMessageDialog(this, "No LTS picture selected to save");
      return;
    }

    FileDialog fd = new FileDialog ((Frame)getTopLevelAncestor(), "Save file in:", FileDialog.SAVE);

    //  GraphicsExporter x_exporter = new GraphicsExporter(); 

    if (Nmach>0) {
        String fname = dm.getMachine().getName();
        int colon = fname.indexOf(':',0);
        if (colon>0) fname = fname.substring(0,colon);
	// x_exporter.setDefaultFilename( fname + ".png" );
    } 
	
    // x_exporter.export( dm );
   
    fd.setVisible(true);
    String file = fd.getFile();
        if (file != null)
    try {
        int i = file.indexOf('.',0);
        file = file.substring(0,i) + "."+"pct";
        FileOutputStream fout =  new FileOutputStream(fd.getDirectory()+file);
        // get picture
        ByteArrayOutputStream baos = new ByteArrayOutputStream(10000);
        java.awt.Rectangle r = new java.awt.Rectangle(0,0,dm.getSize().width,dm.getSize().height);
        Gr2PICT pict= new Gr2PICT(baos, output.getGraphics(),r);
      //  dm.fileDraw(pict);
        pict.finalize(); // make sure pict end is written
        fout.write(baos.toByteArray());
        fout.flush();
        fout.close();
        //outln("Saved in: "+ fd.getDirectory()+file);
    }
    catch (IOException e) {
      System.out.println("Error saving file: " + e);
    }
   

 }

}
