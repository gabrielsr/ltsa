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
import ic.doc.ltsa.common.iface.IAnalysisFactory;
import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.IDrawMachine;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;

import com.chatley.magicbeans.PluginManager;

public class LTSCanvas extends JPanel implements Scrollable {

    private static boolean oFontFlag = false;
    private static boolean oDisplayName = false;
    private static boolean oNewLabelFormat = true;
    Dimension oInitial = new Dimension(10,10);

    private IAnalysisFactory oAnalysisFactory;
    
    Font nameFont;
    Font labelFont;
    
    final static int SEPARATION = 80;
    final static int ARCINC = 30;
    
    protected boolean singleMode = false;
 
    IDrawMachine drawing[];
    IDrawMachine focus;
    
    protected MouseInputListener mouse;
    
    public LTSCanvas(boolean mode) {
        super();        
        setBigFont(fontFlag());
        setBackground(Color.white);
        singleMode = mode;
        if (!singleMode) {
          mouse = new MyMouse();
          this.addMouseListener(mouse);
          this.addMouseMotionListener(mouse);
        }
        
        PluginManager.getInstance().addBackDatedObserver(this);
  	}
    
    public void setMode(boolean mode) {
        if (mode == singleMode) return;
        focus = null;
        if (drawing!=null) {
            int len = drawing.length;
            drawing = new IDrawMachine[len];
        }
        singleMode = mode;
        if (!singleMode) {
          mouse = new MyMouse();
          this.addMouseListener(mouse);
          this.addMouseMotionListener(mouse);
        } else {
          if (mouse!=null) {
            this.removeMouseListener(mouse);
            this.removeMouseMotionListener(mouse);
            mouse = null;
          }
        }
        setPreferredSize(oInitial);
        revalidate();
        repaint();
    }
    
    public void setBigFont(boolean flag) {
       if (flag) {
            labelFont = new Font("Serif",Font.BOLD,14);
            nameFont  = new Font("SansSerif",Font.BOLD,18);
       } else {
            labelFont = new Font("Serif",Font.PLAIN,12);
            nameFont =  new Font("SansSerif", Font.BOLD,14);
       }
       if (drawing!=null) 
          for(int i=0; i<drawing.length; i++)
               if (drawing[i]!=null) drawing[i].setFonts(nameFont, labelFont);
       repaint();
    }
    
    public void setDrawName(boolean flag) {
        setDisplayName(flag);
        if (drawing!=null) 
          for(int i=0; i<drawing.length; i++)
               if (drawing[i]!=null) drawing[i].setDrawName(displayName());
        repaint();
    }
    
    public void setNewLabelFormat(boolean flag) {
        seyNewLabelFormat(flag);
        if (drawing!=null) 
          for(int i=0; i<drawing.length; i++)
               if (drawing[i]!=null) drawing[i].setNewLabelFormat(useNewLabelFormat());
        repaint();
    }

    public void setMachines(int n) {
      focus = null;
      if (n>0) 
         drawing = new IDrawMachine[n];
      else 
         drawing = null;
      setPreferredSize(oInitial);
      revalidate();
      repaint();
    }

    public static void setDisplayName(boolean displayName) {
		LTSCanvas.oDisplayName = displayName;
	}

	public static boolean displayName() {
		return oDisplayName;
	}

	public static void setFontFlag(boolean oFontFlag) {
		LTSCanvas.oFontFlag = oFontFlag;
	}

	public static boolean fontFlag() {
		return oFontFlag;
	}

	public void draw(int id, ICompactState m, int last, int current, String name) {
        if (m == null || id>=drawing.length) {drawing = null; repaint(); return;}
        if (drawing[id]==null) {
           drawing[id] = oAnalysisFactory.createDrawMachine
                      (m, this, nameFont, labelFont, displayName(), useNewLabelFormat(), SEPARATION,ARCINC);
        }
        if (singleMode) focus = drawing[id];
        drawing[id].select(last,current,name);
        Dimension d = drawing[id].getSize();
        Dimension e = getPreferredSize();
        setPreferredSize
          (new Dimension(Math.max(e.width,d.width),Math.max(e.height,d.height))); 
        revalidate();
        repaint();
    }
    
    public void clear(int id) {
        drawing[id] = null;
        repaint();
    }
    
    public int clearSelected() {
        if (focus == null || singleMode || drawing ==null) return -1;
        int ret;
        for(ret = 0; drawing[ret]!=focus; ++ret);
        focus = null;
        drawing[ret] = null;
        repaint();
        return ret;
    }
         
    private Rectangle rr = new Rectangle();
    
    public void stretchHorizontal(int d){
        if (focus!=null) {
          focus.setStretch(false,d,0);
          focus.getRect(rr);
          Dimension p = getPreferredSize();
          setPreferredSize
               (new Dimension(Math.max(p.width,rr.x + rr.width),
                              Math.max(p.height,rr.y + rr.height))); 
          revalidate();
          repaint();
        } 
    }
        
    public void stretchVertical(int d){
        if (focus!=null) {
          focus.setStretch(false,0,d);
          focus.getRect(rr);
          Dimension p = getPreferredSize();
          setPreferredSize
               (new Dimension(Math.max(p.width,rr.x + rr.width),
                              Math.max(p.height,rr.y + rr.height))); 
          revalidate();
          repaint();
        } 
    }

    public void select(int n, int last[], int current[], String name) {
      if (drawing == null) return;
      for (int i=0; i<n; i++) {
        if (drawing[i]!=null) {
           int ls = last!=null? last[i]:0;
           int cs = current!=null?current[i]:0;
           drawing[i].select(ls,cs,name);
        }
      }
      repaint();    
    }
    
    public IDrawMachine getDrawing() {return focus;}

    public void paintComponent(Graphics g){
      super.paintComponent(g);
	    Graphics2D g2d = (Graphics2D)g;
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	        	               RenderingHints.VALUE_ANTIALIAS_ON);
	    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	        	               RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
	    g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
	        	               RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
      if (drawing!=null && !singleMode) {          
           for(int i=0; i<drawing.length; i++)
               if (drawing[i]!=null) {
                 if (drawing[i]!=focus || focus == null) drawing[i].draw(g);  
               }
      }  
      if (focus!=null) focus.draw(g);
   }

/*-------------Mouse handler ---------------------------*/

    class MyMouse extends MouseInputAdapter {
    	  Point start = null;
    	  Rectangle r = new Rectangle();  //avoid unnecessary garbage
        	  
    	  public void mousePressed(MouseEvent e) {
    	  	 if (drawing!=null) {
             if (focus!=null) {
               focus.setSelected(false);
               focus = null;
               repaint();
             }
             for(int i=0; i<drawing.length; i++) 
               if (drawing[i]!=null) {
                  drawing[i].getRect(r);
    	  	 	      if (r.contains(e.getPoint())) {
                     focus = drawing[i];
    	  	 	  	      focus.setSelected(true);
    	  	 	  	      start = e.getPoint();
    	  	 	  	      repaint();
                     return;
                 }
               }
           }
        }
    	  
    	  public void mouseDragged(MouseEvent e) {
    	  	 if (focus!=null) {
    	  	 	  focus.getRect(r);
    	  	 	  Point current = e.getPoint();
    	  	 	  if (start!=null) {
    	  	 	  	  double xoff = current.getX() - start.getX();
    	  	 	  	  int newX = (int)(r.x + xoff);
    	  	 	  	  double yoff = current.getY() - start.getY();
    	  	 	  	  int newY = (int)(r.y + yoff);
    	  	 	  	  focus.setPos(newX>0?newX:0,newY>0?newY:0);
    	  	 	  	  start = current;
    	  	 	  	  repaint();
    	  	 	  }
    	  	 }
    	  }

    	  public void mouseReleased(MouseEvent e) {
    	  	 start = null;
    	  	 if (focus!=null) {
    	  	 	  focus.getRect(r);
    	  	 	  if (!r.contains(e.getPoint())) {
                 focus.setSelected(false);
                 focus = null;
    	  	 	  	  repaint();
    	  	 	  } else {
                Dimension p = getPreferredSize();
                setPreferredSize
                    (new Dimension(Math.max(p.width,r.x + r.width),Math.max(p.height,r.y + r.height))); 
                  revalidate();
    	  	 	  }
    	  	 }
    	  }
    }

/*-------------Scrollable Implementation ---------------*/
  private int maxUnitIncrement = 1;
  
  public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();    
  }
  
  public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction) {
     return maxUnitIncrement;
  }
 
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {

        if (orientation == SwingConstants.HORIZONTAL)
            return visibleRect.width - SEPARATION;
        else
            return visibleRect.height - ARCINC;
    }

    public boolean getScrollableTracksViewportWidth() {

        return false;
    }

    public boolean getScrollableTracksViewportHeight() {

        return false;
    }

    public void setMaxUnitIncrement(int pixels) {

        maxUnitIncrement = pixels;
    }

    /*--------------------------------------------*/
    public boolean isFocusTraversable() {

        return true;
    }

    public void pluginAdded(IAnalysisFactory pAnalFact) {

        oAnalysisFactory = pAnalFact;
    }

	public static void seyNewLabelFormat(boolean newLabelFormat) {
		LTSCanvas.oNewLabelFormat = newLabelFormat;
	}

	public static boolean useNewLabelFormat() {
		return oNewLabelFormat;
	}
 }