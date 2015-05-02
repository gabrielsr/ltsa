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
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.*;
import ic.doc.extension.IAnimator;

/**
* Window to run the simple animator
*/
public class AnimWindow extends JFrame {

    public static boolean fontFlag = false;

    JTextArea output;
    IAnimator animator;
    JCheckBox [] choices;
    Font f1;
    BitSet actions;
    Color priority;
    BitSet pactions;
    String [] modelAlphabet;
    JButton step, run;
    public boolean autoRun  = false;
    private final static int STEPLIMIT = 64;
    protected boolean traceMode = false;

    public AnimWindow(IAnimator animator, RunMenu r, boolean auto, boolean traceM) {
        autoRun = auto;
        traceMode = traceM;
        //setFont
        if (fontFlag) {
            f1 = new Font("SansSerif",Font.BOLD,16); //should be bold - bug
        } else {
            f1 = new Font("SansSerif",Font.BOLD,12);
        }
        //parameters
        this.animator=animator;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
        setBackground(Color.white);
        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
        // trace output
        output = new JTextArea("",15,15);
				output.setEditable(false);
				output.setFont(f1);
				output.setBackground(Color.white);
        output.setBorder(new EmptyBorder(0,5,0,0));
				JScrollPane outp = new JScrollPane
			                    (output,
			             	        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			             	        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
			             	       );
      getContentPane().add(outp);
      if (r==null)
          actions = animator.initialise(null);
      else
          actions = animator.initialise(r.getAlphabet() );
      if (traceMode) 
         setTitle("Replay Animator");
      else 
         setTitle("Animator");
      // buttons
      Box buttons;
      step = new JButton("Step");
      step.setFont(f1);
      step.addActionListener(new AnimAction(traceMode?-3:-1));
      run  = new JButton("Run");
      run.addActionListener(new AnimAction(traceMode?-4:-2));
      run.setFont(f1);
      buttons = Box.createHorizontalBox();
      buttons.add(run);
      buttons.add(step);
      //controls
      String alphabet[] = animator.getMenuNames();
      modelAlphabet = animator.getAllNames();
      if (animator.getPriority())
          priority = Color.cyan;
      else
          priority = Color.pink;
      pactions = animator.getPriorityActions();
      choices = new JCheckBox[alphabet.length];
      Box p = Box.createVerticalBox();
      for(int i = 1; i<alphabet.length; i++){
          p.add(choices[i] = new JCheckBox(alphabet[i],null,actions.get(i)));
          choices[i].setFont(f1);
          choices[i].addActionListener(new AnimAction(i));
          if (traceMode) choices[i].setEnabled(false);
          if (pactions!=null && pactions.get(i)) 
              choices[i].setBackground(priority);
      }
      p.add(Box.createHorizontalStrut(10));
      boolean enable = (animator.nonMenuChoice() || traceMode);
      step.setEnabled(enable);
      run.setEnabled(enable);
      if (empty(actions) && !enable) outln("STOP");
      JScrollPane boxes = new JScrollPane
                            (p,
                               JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                               JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
                              );
      boxes.setBorder(new EmptyBorder(0,0,0,0));
      Box side = Box.createVerticalBox();
      side.add(  buttons);
      side.add(boxes);
       getContentPane().add(side);
      p.setBackground(Color.white);
      validate();
      if (autoRun) dostep(-2);
    }

    private void dostep(int choice) {
       if (animator.isError()) return;
       if (choice==-1) {
            actions = animator.singleStep();
            outAction();
       } else if (choice==-2) {
            actions = multiStep(null);
       } else if (choice==-3) {
            actions = animator.traceStep();
            outAction();
       } else if (choice==-4) {
            actions = multiTraceStep(null);
       } else if (!choices[choice].isSelected()) {  //must have been toggled from true

	   actions = animator.menuStep(choice);
            outAction();
       }
       if (actions==null) return;
       if (autoRun && !traceMode) actions = multiStep(actions);
       for(int j =1; j<choices.length; j++){
            choices[j].setSelected(actions.get(j));
       }
       if (!traceMode) {
         boolean enable = animator.nonMenuChoice();
         step.setEnabled(enable);
         run.setEnabled(enable);
         if (empty(actions) && !enable && !animator.isError()) {
              if (animator.isEnd()) outln("END"); else outln("STOP");
         }
       } else {
         boolean enable = animator.traceChoice();
         step.setEnabled(enable);
         run.setEnabled(enable);
         if (!enable && !animator.isError()) {
              if (empty(actions)) {
                  if (animator.isEnd()) outln("END"); else outln("STOP");
               } else {
                  outln("DIVERGED FROM TRACE");
               }
         }
       }
       repaint();
    }

    private BitSet multiStep(BitSet b) {
        int step = 0;
        while(animator.nonMenuChoice()){
            b = animator.singleStep();
            outAction();
            if (++step>STEPLIMIT) {
                outln("LOOP");
                return b;
            }
        }
        return b;
    }
    
    private BitSet multiTraceStep(BitSet b) {
        while(animator.traceChoice()){
            b = animator.traceStep();
            outAction();
        }
        return b;
    }




//-----MuCSPOutput-------------------------------------------------------------------

	public void out ( String str ) {
		output.append(str);
	}

	public void outln ( String str ) {
		output.append(str+"\n");
	}

	public void clearOutput () {
		output.setText("");
	}
//------------------------------------------------------------------------

    private boolean empty(BitSet b) {
        for (int i = 0; i<b.size();++i)
            if (b.get(i)) return false;
        return true;
    }

    private void outAction() {
        outln(" "+modelAlphabet[animator.actionChosen()]);
        if (animator.isError()) outln("ERROR");
    }
    
    class AnimAction implements ActionListener {
    	int choice;
    	AnimAction(int id){choice = id;}
    	public void actionPerformed(ActionEvent e) { dostep(choice);}
    }
    

}
