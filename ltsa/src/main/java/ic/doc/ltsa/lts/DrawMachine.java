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
 
package ic.doc.ltsa.lts;

import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.IDrawMachine;
import ic.doc.ltsa.common.iface.IEventState;

import java.awt.*;
import java.util.BitSet;
import javax.swing.JPanel;

public class DrawMachine implements IDrawMachine {
    
    public static int MAXDRAWSTATES = 64; // maximum drawable size
    final static int STATESIZE = 30;
    
    Font labelFont;  //used for drawing labels
    Font nameFont;   //used for displaying names
    Font stateFont  = new Font("SansSerif",Font.BOLD,18);

      
    protected boolean displayName = false; // draw machine name
    protected boolean newLabelFormat = true; //draw new label format
    protected boolean selectedMachine    = false; // true if this machine is selected
    
    int SEPARATION;  //state separation
    int ARCINC;      //arc separation
      
    int topX =0;          //X - top corner of bounding rectangle
    int topY =0;          //Y - top corner of bounding rectangle

    int zeroX;            //X - start X for drawing states
    int zeroY;            //Y - start Y for drawing states
      
    int heightAboveCenter;   // zeroX - topX
    int nameWidth = 0;       // width of machine name
      
    Dimension size;          // the size of this machine
      
    private int errorState = 0;   //1 if refers to error state
      
    private int lastselected= -3; //last selected state
    private int selected = 0;     //current selected state
    private String lastaction;    //action to be high lit
      
    ICompactState mach = null;     //the machine to draw
    
    BitSet accepting;          // the set of states which are accepting
      
    JPanel parent;
    

    public DrawMachine(ICompactState m, JPanel p, 
                       Font fn, Font fl, 
                       boolean dn, boolean nl,
                       int separation, int arcIncrement) {
        mach = m;
        parent = p;
        nameFont = fn;
        labelFont = fl;
        displayName = dn;
        newLabelFormat = nl;
        SEPARATION =  separation;
        ARCINC = arcIncrement;
        accepting = mach.accepting();
        if (newLabelFormat) initCompactLabels();
        size = computeDimension(mach);
  	}
    
    public void setDrawName(boolean flag) {
        displayName = flag;
        size = computeDimension(mach);
    }
    
    public void setNewLabelFormat(boolean flag) {
        newLabelFormat = flag;
        if (newLabelFormat) initCompactLabels();
        size = computeDimension(mach);
    }

    
    public void setFonts(Font fn, Font fl) {
        nameFont = fn;
        labelFont = fl;
        size = computeDimension(mach);
    }
    
    public void setStretch(boolean absolute, int separation, int arcIncrement) {
      if (absolute) {
         SEPARATION = separation;
         ARCINC = arcIncrement;
      } else {
         if (SEPARATION + separation >10) SEPARATION += separation;
         if (ARCINC + arcIncrement >5 ) ARCINC += arcIncrement;
      }
      size = computeDimension(mach);
    }
    
    public void select(int last, int current, String name) {
        lastselected = last;
        selected = current;
        lastaction = name;
    }
    
    public void setPos(int x, int y) {
      topX = x;
      topY = y;
    }
    
    public boolean isSelected() {return selectedMachine;}
    public void setSelected(boolean b) {selectedMachine = b;}
    
    public Dimension getSize() {return size;}
    
    public void getRect(Rectangle r) {
        r.x = topX; 
        r.y =topY; 
        r.width = size.width; 
        r.height= size.height;
    }
    
    public ICompactState getMachine(){return mach;}
    
    protected Dimension computeDimension(ICompactState m) {
        // now compute size of drawing
        // work out length & height of name
        int nameHeight = 0;
        if (displayName) {
          Graphics g = parent.getGraphics();
          if (g!=null) {
            g.setFont(nameFont);
            FontMetrics fm = g.getFontMetrics();
            nameWidth = fm.stringWidth(mach.getName());
            nameHeight = fm.getHeight();
          } else {
            nameWidth = SEPARATION; //kludge should never happen
          }
        } else
          nameWidth = 0;
        //if tooo big return
          if (m.getMaxStates()>MAXDRAWSTATES) return new Dimension(220+nameWidth,50);
        // first compute length of largest end label if any
        String largestEndLabel = null;
        if (!newLabelFormat) {
          IEventState p = m.getStates()[m.getMaxStates()-1];
          while (p!=null) {
              IEventState tr = p;
              while(tr!=null) {
                  if (tr.getNext()==(m.getMaxStates()-1)) {
                    if (largestEndLabel == null) {
                      largestEndLabel = m.getAlphabet()[tr.getEvent()];
                    } else {
                      String s = m.getAlphabet()[tr.getEvent()];
                      if (s.length()>largestEndLabel.length()) largestEndLabel = s;
                    }
                  }
                  tr=tr.getNondet();
              }
              p=p.getList();
          }
        } else 
           largestEndLabel = labels[m.getMaxStates()][m.getMaxStates()];
        int endWidth = 10;
        if (largestEndLabel != null) {
          Graphics g = parent.getGraphics();
          if (g!=null) {
            g.setFont(labelFont);
            FontMetrics fm = g.getFontMetrics();
            endWidth = fm.stringWidth(largestEndLabel);
            endWidth += SEPARATION/3;
          } else {
            endWidth = SEPARATION; // kludge should never happen
          }
        }
        // check if machine has an error state
        errorState=0;
        for (int i=0; i<m.getMaxStates(); i++ )
            if (EventState.hasState(m.getStates()[i],Declaration.ERROR))
                {errorState=1;}
        // now compute maximum forward and backward arcs
        int maxFwd = 0;
        int maxFwdLabels = 0;
        int maxBwd = 0;
        int maxBwdLabels = 0;
        for (int i=0; i<m.getMaxStates(); i++ ) {
                int[] ntrans =  new int[m.getMaxStates()+1];
                int fwdToState = 0;
                int bwdToState = 0;
                boolean fwd = false;
                boolean bwd = false;
                IEventState p = m.getStates()[i];
                while (p!=null) {
                    IEventState tr = p;
                    while(tr!=null) {
                        ntrans[tr.getNext()+1]++;
                        int diff = tr.getNext() - i;
                        if (diff>maxFwd || (diff==maxFwd && ntrans[tr.getNext()+1] > maxFwdLabels)) 
                          {maxFwd = diff; fwdToState = tr.getNext()+1; fwd = true; }
                        if (diff<maxBwd || (diff==maxBwd && ntrans[tr.getNext()+1] > maxBwdLabels))  
                          {maxBwd = diff; bwdToState = tr.getNext()+1; bwd = true; }                
                        tr=tr.getNondet();
                    }
                    p=p.getList();
                }
                if (fwd) maxFwdLabels = newLabelFormat?1:ntrans[fwdToState];
                if (bwd) maxBwdLabels = newLabelFormat?1:ntrans[bwdToState];
            }
        if (m.getMaxStates()==1) maxFwdLabels =0;
        int fheight = 10;
        Graphics g = parent.getGraphics();
        if (g!=null) {
            g.setFont(labelFont);
            FontMetrics fm = g.getFontMetrics();
            fheight = fm.getHeight();
        }
        heightAboveCenter = (maxFwd!=0)? (ARCINC*maxFwd)/2:(STATESIZE/2 + nameHeight);
        heightAboveCenter = heightAboveCenter + maxFwdLabels*fheight + 10;
        int heightBelowCenter = (maxBwd!=0)? ARCINC*Math.abs(maxBwd)/2:STATESIZE/2;
        heightBelowCenter = heightBelowCenter +maxBwdLabels * fheight +10;
        int pwidth  = errorState==0 
                        ?10+nameWidth+STATESIZE+endWidth+(m.getMaxStates()-1)*SEPARATION
                        : 10 +STATESIZE+endWidth+m.getMaxStates()*SEPARATION;
        int pheight = heightAboveCenter + heightBelowCenter;
        return new Dimension(pwidth,pheight);
    }
    
    public void fileDraw(Graphics g) {
        int saveX = topX;
        int saveY = topY;
        boolean sm = selectedMachine;
        topX =0; topY =0; selectedMachine = false;
        draw(g);
        topX = saveX;
        topY = saveY;
        selectedMachine = sm;
    }

    public void draw(Graphics g) {
        //draw transitions
        ICompactState m = mach;
        if (m==null) return;
        if (selectedMachine) {
          g.setColor(Color.white);
          g.fillRect(topX,topY,size.width,size.height);
        }
        int aw = 0; //width allowed for name
        if (displayName && errorState==0) aw =nameWidth;
        zeroX = topX+ 10 +errorState*SEPARATION+aw;
        zeroY = topY + heightAboveCenter-STATESIZE/2;
        if (m.getMaxStates()>MAXDRAWSTATES) {
            g.setColor(Color.black);
            g.setFont(nameFont);
            g.drawString(m.getName()+" -- too many states: "+m.getMaxStates(), topX, topY+20);
        } else {
             // display name
            g.setFont(nameFont);
            FontMetrics fm = g.getFontMetrics();
	        int nw = fm.stringWidth(m.getName());
	        g.setColor(Color.black);
	        if(displayName) g.drawString(m.getName(),zeroX-nw,zeroY-5);
            //draw transitions - lines
            for (int i=0; i<m.getMaxStates(); i++ ) {
                int [] ntrans = new int[m.getMaxStates()+1]; //count transtions between 2 states
                IEventState p = m.getStates()[i];
                while (p!=null) {
                    IEventState tr = p;
                    String  event = m.getAlphabet()[tr.getEvent()];
                    if (event.charAt(0)!='@')
                    while(tr!=null) {
                        ntrans[tr.getNext()+1]++;
                        drawTransition(g,i,tr.getNext(),event,ntrans[tr.getNext()+1],
                                       i==lastselected && tr.getNext()==selected,false);
                        tr=tr.getNondet();
                    }
                    p=p.getList();
                }
            }
            //draw transitions - text
            for (int i=0; i<m.getMaxStates(); i++ ) {
                int [] ntrans = new int[m.getMaxStates()+1]; //count transtions between 2 states
                IEventState p = m.getStates()[i];
                while (p!=null) {
                    IEventState tr = p;
                    String  event = m.getAlphabet()[tr.getEvent()];
                    if (event.charAt(0)!='@')
                    while(tr!=null) {
                        ntrans[tr.getNext()+1]++;
                        if (!newLabelFormat) {
                          drawTransition(g,i,tr.getNext(),event,ntrans[tr.getNext()+1],
                                         i==lastselected && tr.getNext()==selected,true);
                        } else {
                          if (ntrans[tr.getNext()+1]==1)
                            drawTransition(g,i,tr.getNext(),labels[i+1][tr.getNext()+1],ntrans[tr.getNext()+1],
                                         i==lastselected && tr.getNext()==selected,true);

                        }
                        tr=tr.getNondet();
                    }
                    p=p.getList();
                }
            }

            for (int i=-errorState; i<m.getMaxStates(); i++ )
                drawState(g,i,i==selected);
        }
        if (selectedMachine) {
        	g.setColor(Color.gray);
        	g.drawRect(topX,topY,size.width,size.height);
        }
    }



    private void drawState(Graphics g,int id, boolean highlight) {
        int x = zeroX + id*SEPARATION;
        int y = zeroY;
        if (highlight)
            g.setColor(Color.red);
        else
             g.setColor(Color.cyan);
        if (id>=0 && accepting.get(id))
           g.fillArc(x-3,y-3,STATESIZE+6,STATESIZE+6,0,360);
        else
           g.fillArc(x,y,STATESIZE,STATESIZE,0,360);
        g.setColor(Color.black);
        g.setFont(stateFont);
        if (id>=0 && accepting.get(id))
        	  g.drawArc(x-3,y-3,STATESIZE+6,STATESIZE+6,0,360);
        g.drawArc(x,y,STATESIZE,STATESIZE,0,360);
        FontMetrics fm = g.getFontMetrics();
        String sid = (id==mach.getEndseq()) ?"E":""+id;
        int px = x + STATESIZE/2 - fm.stringWidth(sid)/2;
        int py = y + STATESIZE/2 + fm.getHeight()/3;
        g.drawString(sid, px, py);
    }

    private void drawTransition(Graphics g,int from, int to, String s, int n, boolean highlight, boolean dotext) {
        if (highlight)
            g.setColor(Color.red);
        else
            g.setColor(Color.black);
        int sign = (to<=from)?-1:1;
        int start= (to<from)?to:from;
        int x = zeroX + start*SEPARATION + STATESIZE/2;
        int w = (to!=from)? (SEPARATION*Math.abs(from-to)): SEPARATION/3;
        int h = (to!=from)? (ARCINC*Math.abs(from-to)):STATESIZE-5;
        int y = zeroY - (h - STATESIZE)/2;
        if (n==1 && !dotext){  //only draw arc for first transition to->from
            if (from !=to) {
                g.drawArc(x,y,w,h,0,180*sign);
                if (sign>0)
                   drawArrow(g,x+w/2,y,arrowForward);
                else
                   drawArrow(g,x+w/2,y+h-1,arrowBackward);
            } else {
                g.drawArc(x,y,w,h,0,360);
                drawArrow(g,x+w,y+h/2,arrowDown);
            }
        }
        if (!dotext) return;
        n +=1;   // shift up to permit arrow
        g.setFont(labelFont);
        FontMetrics fm = g.getFontMetrics();
        int drop = fm.getMaxAscent()/3;
        int px = x +w/2 - fm.stringWidth(s)/2;
        if (to==from) px = x +w+2;
        int py =(sign>0)?y+drop:(y + h+drop);
        if (to==from) py = y+h/2+drop;
        if (n>1) {
              py = py - (n-1)*fm.getHeight()*sign;
        }
        g.setColor(Color.white);
        g.fillRect(px,py-fm.getMaxAscent(),fm.stringWidth(s),fm.getHeight());
        if (highlight && ((lastaction!=null && lastaction.equals(s)) || newLabelFormat))
            g.setColor(Color.red);
        else
            g.setColor(Color.black);

        g.drawString(s, px, py);
    }


    private int arrowX[] = new int[3];
    private int arrowY[] = new int[3];

    private static int arrowForward = 1;
    private static int arrowBackward = 2;
    private static int arrowDown = 3;

    private void drawArrow(Graphics g, int x, int y, int direction) {
        if (direction == arrowForward) {
            arrowX[0] = x-5; arrowY[0] = y-5;
            arrowX[1] = x+5; arrowY[1] = y;
            arrowX[2] = x-5; arrowY[2] = y+5;
        }  else if (direction == arrowBackward){
            arrowX[0] = x+5; arrowY[0] = y-5;
            arrowX[1] = x-5; arrowY[1] = y;
            arrowX[2] = x+5; arrowY[2] = y+5;
        }  else if (direction == arrowDown){
            arrowX[0] = x-5; arrowY[0] = y-5;
            arrowX[1] = x+5; arrowY[1] = y-5;
            arrowX[2] = x; arrowY[2] = y+5;
        }
        g.fillPolygon(arrowX,arrowY,3);
    }
    
    String [][] labels;  // from -- to
    
    private void initCompactLabels () {
      if (mach == null) return;
      if (mach.getMaxStates()>MAXDRAWSTATES) return;
      labels =  new String[mach.getMaxStates()+1][mach.getMaxStates()+1];
      for (int i=0; i<mach.getMaxStates(); i++ ) {
          IEventState current = EventState.transpose(mach.getStates()[i]);
          while (current != null) {
            String[] events = EventState.eventsToNextNoAccept(current,mach.getAlphabet());
            Alphabet a = new Alphabet(events);
            labels[i+1][current.getNext()+1] = a.toString();
            current = current.getList();
          }
      }
    }
      
}