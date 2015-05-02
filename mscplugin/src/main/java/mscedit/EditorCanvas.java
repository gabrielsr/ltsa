package mscedit;

import ic.doc.extension.Exportable;
import org.jdom.*;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.Point;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.*;

abstract class EditorCanvas extends JPanel implements Exportable {
	
	protected XMLGui o_gui;
	protected Glyph o_selected;
	protected String o_old_selection;
	
	abstract void rebuild();
	abstract void addInstance( String p_name );
	abstract void addLink( String p_from , String p_to , String p_name , int p_timeindex );
	abstract void delete();
	abstract void changeLinkName( String p_from , String p_to );
	abstract void reverseSelectedLink();
	abstract void negateSelectedLink();
	abstract void tidy();
	abstract void addNegativeLink( String p_from , String p_to , String p_name , int p_timeindex );
	
	public void fileDraw( Graphics g ) {}
	
	public void swapToTab( String p_name ) { o_gui.swapToTab( p_name ); }
	
	public EditorCanvas() {}
	
	Glyph getSelection() { return o_selected; }
	
	String getSelectionName() {
		
		if ( o_selected != null ) { return o_selected.getName(); }
		return "";
	}
	
	String getSelectionId() {
		
		if ( o_selected != null ) { return o_selected.getId(); }
		return "";
	}
	
	void setSelection( Glyph p_trans ) {
		
		o_selected = p_trans;
		repaint();
	}
	
	void setOldSelection( String p_sel ) {
		
		o_old_selection = p_sel;
	}
	
	String getOldSelection() {
		
		if ( o_old_selection != null ) { return o_old_selection; }
		return "";
	}
	
	void showPopup( MouseEvent p_me ) {
		
		o_gui.showPopup( p_me , o_selected );
	}
	
	public Dimension getPreferredSize() {
		return new Dimension( 500 , 400 );
	}
}



class CanvasMouseListener extends MouseInputAdapter {
	
	private BMSCEditorCanvas o_canvas;
	private int o_clicks = 0;
	private Glyph o_moving;
	
	public CanvasMouseListener( BMSCEditorCanvas p_canvas ) {
		
		o_canvas = p_canvas;
		o_moving = null;
	}
	
	public void mouseClicked( MouseEvent p_me ) {
		
		if ( p_me.getButton() == MouseEvent.BUTTON1 ) {
			
			if ( o_canvas.inAddDividerMode() ) {
				
				o_canvas.addDivider( (int)( p_me.getY() / Glyph.getScale() ) );
				
			} else if ( o_canvas.inAddLinkMode() ) { 
				
				String x_inst_name = o_canvas.getInstanceAt( p_me.getX() , p_me.getY() );
				
				if ( x_inst_name == null ) { return; }
				
				if ( o_clicks == 0 ) {
					
					o_canvas.setOldSelection( x_inst_name );
					Graphics2D x_gr = (Graphics2D)o_canvas.getGraphics();
					double x_s = Glyph.getScale();
					x_gr.setColor( Color.red );
					x_gr.fill( new java.awt.geom.Ellipse2D.Float( (int)((( p_me.getX() / x_s  - 10 ) / 100 * 100 + 10 ) * x_s ),
							(int)((( p_me.getY() / x_s  - 50 ) / 25  * 25  + 50 ) * x_s ), 5 , 5 ) );
					
					
				} else {
					
					//if ( x_inst_name.equals( o_canvas.getOldSelection() ) ) { return; }
					if ( o_canvas.addPositive() ) { o_canvas.addLink( o_canvas.getOldSelection() , x_inst_name , (int)( p_me.getY() / Glyph.getScale())); }
					else { o_canvas.addNegativeLink( o_canvas.getOldSelection() , x_inst_name ,(int)( p_me.getY() / Glyph.getScale() ) ); }
				}
				
				o_clicks = 1 - o_clicks;
				
			} else {
				
				o_canvas.setSelection( o_canvas.getMessageAt( p_me.getX() , p_me.getY() ) );
			}
		}
		
		if ( p_me.getButton() == MouseEvent.BUTTON3 ) {
			o_canvas.showPopup( p_me );
			// right click
		}
	}   
	
	public void mouseDragged( MouseEvent p_me ) {
		
		if ( o_moving == null ) {
			
			o_moving = o_canvas.getMessageAt( p_me.getX() , p_me.getY() );
			
		} else {
			o_moving.moveTo( (int)( p_me.getY() / Glyph.getScale() ) );
			o_canvas.repaint();
		}
	}
	
	public void mouseReleased( MouseEvent p_me ) {
		
		if ( o_moving != null ) {
			o_moving.moveTo(  p_me.getY() ) ;
			if ( o_moving instanceof DividerGraphic ) {
				o_canvas.moveDivider( Integer.parseInt( o_moving.getId() ) , ( (int)( p_me.getY() / Glyph.getScale() ) - 50 ) / 25 );
			} else {
				o_canvas.moveMessage( o_moving.getId() , ( (int)( p_me.getY() / Glyph.getScale() ) - 50 ) / 25 );
			}
			
			o_moving = null;
			o_canvas.rebuild();
		}
	}
}

class HMSCEditorCanvas extends EditorCanvas {
	
	private HMSC o_msc; 
	private Vector o_bmsc_glyphs;
	private Vector o_transition_glyphs;
	
	public HMSCEditorCanvas() {}
	
	public HMSCEditorCanvas( XMLGui p_gui ) {
		
		o_gui = p_gui;
		o_msc = new HMSC();
		rebuild();
	}
	
	public HMSCEditorCanvas( XMLGui p_gui , HMSC p_hmsc ) {
		
		o_gui = p_gui;
		o_msc = p_hmsc;
		rebuild();
	}
	
	public Dimension getPreferredSize() {
		
		// this should be better. But can we work out where the bounds of the glyphs are,
		// given that the co-ords are private to the Glyph objects?
		
		double x_scale = Glyph.getScale();
		
		if ( o_bmsc_glyphs == null )  { return new Dimension( 500, 400 ); }
		
		if ( o_bmsc_glyphs.size() < 5 ) { return new Dimension( (int)( x_scale * 500 ), (int)( x_scale * 400 ) ); }
		if ( o_bmsc_glyphs.size() < 12 ) { return new Dimension( (int)( x_scale * 750 ), (int)( x_scale * 600 ) ); }
		
		return new Dimension( 4000 , 4000 );
	}
	
	public HMSC getHMSC() { return o_msc; }
	
	
	public void fileDraw( Graphics g ) {
		paintComponent( g );
	}
	
	void changeLinkName( String p_to , String p_from ) {} 
	void reverseSelectedLink() {}
	void negateSelectedLink() {}
	
	public void rebuild() {
		
		o_bmsc_glyphs = new Vector();
		o_transition_glyphs = new Vector();
		
		if ( o_msc != null ) {
			
			List x_bmscs = o_msc.getBMSCs();
			if ( x_bmscs != null ) {
				
				for( Iterator i = x_bmscs.iterator() ; i.hasNext() ; ) {
					
					Element x_bmsc = (Element)i.next();
					o_bmsc_glyphs.add( new BMSCGraphic( x_bmsc.getAttribute("name").getValue() , 
							Integer.parseInt( x_bmsc.getAttribute("x").getValue() ) ,
							Integer.parseInt( x_bmsc.getAttribute("y").getValue() ) ) ); 
				}
			} 
			
			List x_transitions = o_msc.getTransitions();
			if ( x_transitions != null ) {
				
				for( Iterator i = x_transitions.iterator() ; i.hasNext() ; ) {
					
					Transition x_trans = (Transition)i.next();
					String x_to = x_trans.getTo();
					String x_from = x_trans.getFrom();
					
					if ( o_msc.isWeighted() ) {
						
						String x_weight = String.valueOf( x_trans.getWeight() );
						o_transition_glyphs.add( new WeightedTransitionGraphic( x_from , x_to , locate( x_from ) , locate( x_to ) , x_weight ));
			
					} else {
						o_transition_glyphs.add( new TransitionGraphic( x_from , x_to , locate( x_from ) , locate( x_to ) ) );
					}
				}
			} 
		}
		
		repaint();
	}
	
	void moveBMSC( String p_name , int p_x , int p_y ) {
		
		o_msc.moveBMSC( p_name , p_x , p_y );
	}
	
	private Point locate( String p_name ) {
		
		for( Iterator i = o_bmsc_glyphs.iterator() ; i.hasNext() ; ) {
			
			BMSCGraphic x_bgr = (BMSCGraphic)i.next();
			if ( x_bgr.getName().equals( p_name ) ) {
				
				return x_bgr.getCentre();
			}
		}
		
		return null;
	}
	
	public void addBMSC( String p_name , int p_x , int p_y ) {
		
		o_msc.addBMSC( p_name , p_x , p_y );
	}
	
	void addTransition( String p_from , String p_to ) {
		
		o_msc.addTransition( p_from , p_to );
		setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
		rebuild();
	}
	
	void addInstance( String p_name ) {}
	void addLink( String p_from , String p_to , String p_name , int p_timeindex ) { }
	void addNegativeLink( String p_from , String p_to , String p_name , int p_timeindex ) { }
	
	void delete() {
		
		if ( o_selected != null && o_selected instanceof TransitionGraphic ) {
			
			o_msc.deleteTransition( ((TransitionGraphic)o_selected).getFrom() , ((TransitionGraphic)o_selected).getTo() );
			
		} else if ( o_selected != null && o_selected instanceof BMSCGraphic ) {
			
			o_gui.deleteBMSC( ((BMSCGraphic)o_selected).getName() );
		}
		
		rebuild();
	}
	
	void deleteBMSC( String p_name ) {
		o_msc.deleteBMSC( p_name );
	}
	
	void tidy(){ /*it'd be cool if this did something useful, but I can't think how atm*/ }
	
	boolean inAddLinkMode() { return o_gui.inAddLinkMode(); }
	void setAddLinkMode( boolean p_addlink ) { o_gui.setAddLinkMode( p_addlink ); }
	
	String getBMSCAt( int p_x , int p_y ) {
		
		BMSCGraphic x_gr = getBMSCGraphicAt( p_x , p_y );
		if ( x_gr != null ) {
			return x_gr.getName(); 
		}
		
		return null;
	}
	
	BMSCGraphic getBMSCGraphicAt( int p_x , int p_y ) {
		
		for ( Iterator i = o_bmsc_glyphs.iterator() ; i.hasNext() ; ) {
			
			BMSCGraphic x_bmsc_gr = (BMSCGraphic)i.next();
			
			if ( x_bmsc_gr.contains( p_x , p_y ) ) {
				return x_bmsc_gr;
			}
		}
		
		return null;
	}
	
	TransitionGraphic getTransitionAt( int p_x , int p_y ) {
		
		for ( Iterator i = o_transition_glyphs.iterator() ; i.hasNext() ; ) {
			
			TransitionGraphic x_trans_gr = (TransitionGraphic)i.next();
			
			if ( x_trans_gr.contains( p_x , p_y ) ) {
				return x_trans_gr;
			}
		}
		
		return null;
	}
	
	public void paintComponent( Graphics g ) {
		
		Graphics2D x_g2d = (Graphics2D)g;
		
		//Make background white
		x_g2d.setColor(Color.white);
		x_g2d.fillRect(0, 0, getSize().width -1, getSize().height -1);
		x_g2d.setColor(Color.black);
		
		for( Iterator i = o_bmsc_glyphs.iterator() ; i.hasNext() ; ) {
			
			/*((Glyph)i.next()).draw( g );*/
			
			Glyph x_glyph = (Glyph)i.next();
			if ( x_glyph == o_selected ) {
				
				g.setColor( Color.red );
				x_glyph.draw( g );
				g.setColor( Color.black );
			} else {
				x_glyph.draw( g );
			}
			
		}
		
		for( Iterator i = o_transition_glyphs.iterator() ; i.hasNext() ; ) {
			Glyph x_glyph = (Glyph)i.next();
			if ( x_glyph == o_selected ) {
				g.setColor( Color.red );
				x_glyph.draw( g );
				g.setColor( Color.black );
			} else {
				x_glyph.draw( g );
			}
		}
	}

	public void addWeight(String p_weight) {
		
		String x_from = ((TransitionGraphic)o_selected).getFrom();
		String x_to = ((TransitionGraphic)o_selected).getTo();
		
		o_msc.setTransitionWeight( x_from , x_to , p_weight );
		
		rebuild();
	}
}



class HMSCCanvasMouseListener extends MouseInputAdapter {
	
	private HMSCEditorCanvas o_canvas;
	private int o_clicks = 0;
	private BMSCGraphic o_moving;
	
	public HMSCCanvasMouseListener( HMSCEditorCanvas p_canvas ) {
		
		o_canvas = p_canvas;
		o_moving = null;
	}
	
	public void mouseClicked( MouseEvent p_me ) {
		
		if ( p_me.getButton() == MouseEvent.BUTTON1 ) {
			
			if ( o_canvas.inAddLinkMode() ) { 
				
				String x_bmsc_name = o_canvas.getBMSCAt( p_me.getX() , p_me.getY() );
				
				if ( x_bmsc_name == null ) { return; }
				
				if ( o_clicks == 0 ) {
					
					o_canvas.setOldSelection( x_bmsc_name );
					o_canvas.setSelection( o_canvas.getBMSCGraphicAt( p_me.getX() , p_me.getY() ) );
					
				} else {
					
					o_canvas.addTransition( o_canvas.getOldSelection() , x_bmsc_name );
					o_canvas.setAddLinkMode( false );
				}
				
				o_clicks = 1 - o_clicks;
				
			} else {
				
				if ( o_canvas.getTransitionAt( p_me.getX() , p_me.getY() ) != null ) {
					o_canvas.setSelection( o_canvas.getTransitionAt( p_me.getX() , p_me.getY() ) );
				} else {
					o_canvas.setSelection( o_canvas.getBMSCGraphicAt( p_me.getX() , p_me.getY() ) );
				}
			}
		}
		
		if ( p_me.getClickCount() > 1 ) {
			
			//double-click
			
			String x_bmsc_name = o_canvas.getBMSCAt( p_me.getX() , p_me.getY() );
			o_canvas.setSelection(null);
			o_canvas.swapToTab( x_bmsc_name );
		}
		
		if ( p_me.getButton() == MouseEvent.BUTTON3 ) {
			o_canvas.showPopup( p_me );
			// right click detected
		}
	}
	
	public void mouseDragged( MouseEvent p_me ) {
		
		if ( o_moving == null ) {
			
			o_moving = o_canvas.getBMSCGraphicAt( p_me.getX() , p_me.getY() );
			
		} else {
			o_moving.moveTo( p_me.getX() , p_me.getY() );
			o_canvas.repaint();
		}
	}
	
	public void mouseReleased( MouseEvent p_me ) {
		
		if ( o_moving != null ) {
			o_moving.moveTo( p_me.getX() , p_me.getY() ) ;
			
			double x_s = Glyph.getScale();
			
			o_canvas.moveBMSC( o_moving.getName() , (int)( p_me.getX() / x_s ) / 120 * 120 + 50 , (int)( p_me.getY() / x_s ) / 100 * 100 + 30 );
			o_moving = null;
			o_canvas.rebuild();
		}
	}
}
