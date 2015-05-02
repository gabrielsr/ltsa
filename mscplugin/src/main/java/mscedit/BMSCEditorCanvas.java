package mscedit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

class BMSCEditorCanvas extends EditorCanvas {
	
	private BMSC o_msc;
	private Vector o_instance_glyphs;
	private Vector o_message_glyphs;
	private Vector o_divider_glyphs;
	
	public BMSCEditorCanvas() {}
	
	public BMSCEditorCanvas( XMLGui p_gui , String p_name ) {
		
		o_msc = new BMSC( p_name );
		o_gui = p_gui;
		rebuild();
		setBackground( Color.white );
	}
	
	public BMSCEditorCanvas( XMLGui p_gui , BMSC p_msc ) {
		
		o_msc = p_msc;
		o_gui = p_gui;
		rebuild();
	}
	
	public Dimension getPreferredSize() {
		
		double x_scale = Glyph.getScale();
		return new Dimension( (int)(o_instance_glyphs.size() * 100 * x_scale) ,
				( (int)((o_msc.getLastTimeIndex() * 25 + 150 ) * x_scale)) );
	}
	
	public BMSC getBMSC() { return o_msc; }
	
	public void rebuild() {
		
		o_instance_glyphs = new Vector();
		o_message_glyphs = new Vector();
		o_divider_glyphs = new Vector();
		
		if ( o_msc != null ) {
			
			List x_insts = o_msc.getInstances();
			
			if ( x_insts != null ) {
				
				for( int i=0 ; i < x_insts.size() ; i++ ) {
					o_instance_glyphs.add( new InstanceGraphic( ((Instance)x_insts.get( i )).getName() , i , o_msc.getLastTimeIndex() ) ); 
				}
			} 
			
			List x_links = o_msc.getLinks();
			
			if ( x_links != null ) {
				
				for( Iterator i = x_links.iterator() ; i.hasNext() ; ) {
					Link x_link = (Link)i.next();
                    if (x_link.hasWeight())
                    {
                        double weight = x_link.getWeight();

                        if ( x_link.isSelfTransition() ) {
                            o_message_glyphs.add( new SelfTransitionGraphic( x_link.getName() , locate( x_link.getFrom() ) ,
                                    locate( x_link.getTo() ) , x_link.getTimeIndex(), weight) );
                        } else if ( x_link.isNegative() ) {
                            o_message_glyphs.add( new NegativeMessageGraphic( x_link.getName() , locate( x_link.getFrom() ) ,
                                    locate( x_link.getTo() ) , x_link.getTimeIndex(), weight ) );
                        } else {
                            o_message_glyphs.add( new MessageGraphic( x_link.getName() , locate( x_link.getFrom() ) ,
                                    locate( x_link.getTo() ) , x_link.getTimeIndex(), weight ) );
                        }
                    }
                    else
                    {
                        if ( x_link.isSelfTransition() ) {
    						o_message_glyphs.add( new SelfTransitionGraphic( x_link.getName() , locate( x_link.getFrom() ) ,
    								locate( x_link.getTo() ) , x_link.getTimeIndex() ) );
    					} else if ( x_link.isNegative() ) {
    						o_message_glyphs.add( new NegativeMessageGraphic( x_link.getName() , locate( x_link.getFrom() ) ,
    								locate( x_link.getTo() ) , x_link.getTimeIndex() ) );
    					} else {
    						o_message_glyphs.add( new MessageGraphic( x_link.getName() , locate( x_link.getFrom() ) ,
    								locate( x_link.getTo() ) , x_link.getTimeIndex() ) );
    					}
                    }
				}
			}
			
			List x_dividers = o_msc.getDividers();
			
			if ( x_dividers != null ) {
				
				for( Iterator i = x_dividers.iterator() ; i.hasNext() ; ) {
					o_divider_glyphs.add( new DividerGraphic( ((Divider)i.next()).getTimeIndex() , o_msc.getInstances().size() ) ); 
				}
			}
		} 
		
		repaint();
	}
	
	void addInstance( String p_name ) {
		
		o_msc.addInstance( p_name );
	}
	
	void addLink( String p_from , String p_to , int p_timeindex ) {
		
		o_gui.addLink( p_from , p_to , ( p_timeindex - 50 ) / 25 );
	}
	
	void addNegativeLink( String p_from , String p_to , int p_timeindex ) {
		
		o_gui.addNegativeLink( p_from , p_to , ( p_timeindex - 50 ) / 25 );
	}
	
	void addDivider( int p_timeindex ) {
		
		o_msc.addDivider( ( p_timeindex - 50 ) / 25 );
		o_gui.setAddDividerMode( false );
		setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
		rebuild();
	}
	
	void moveMessage( String p_id , int p_timeindex ) {
		
		o_msc.changeTimeIndex( p_id , p_timeindex );
	}
	
	void moveDivider( int p_oldtimeindex , int p_newtimeindex ) {
		
		o_msc.moveDivider( p_oldtimeindex , p_newtimeindex );
	}
	
	// I get the feeling we should only have one of these two, and get the p_name from the gui...
	
	void addLink( String p_from , String p_to , String p_name , int p_timeindex ) {
		
		String x_from_inst;
		String x_to_inst;
		String x_name;
		
		if ( p_name.startsWith( "@@" ) ) {
			
			x_name = p_name.substring( 2 , p_name.length() );
			
		} else {
			
			if ( p_from.indexOf( ":" ) < 0 ) { x_from_inst = p_from.toLowerCase(); }
			else { x_from_inst = p_from.substring( 0 , p_from.indexOf( ":" ) ); } 
			
			if ( p_to.indexOf( ":" ) < 0 ) { x_to_inst = p_to.toLowerCase(); }
			else { x_to_inst = p_to.substring( 0 , p_to.indexOf( ":" ) ); }
			
			x_name = x_from_inst + "," + x_to_inst + "," + p_name;
		}
		o_msc.addLink( p_from , p_to , x_name , p_timeindex );
		
		o_gui.addMessageToAlphabet( x_name );
	}
	
	void addNegativeLink( String p_from , String p_to , String p_name , int p_timeindex ) {
		
		o_msc.addNegativeLink( p_from , p_to , p_name , p_timeindex );
	}
	
	void delete() {
		
		if ( o_selected == null ) {
			
			int x_sure = JOptionPane.showConfirmDialog( null, "Are you sure you want to delete this bMSC?",
					"Delete bMSC?",
					JOptionPane.YES_NO_OPTION);
			if ( x_sure == JOptionPane.YES_OPTION ) {
				
				o_gui.deleteBMSC( o_msc.getName() );
			}
			
			rebuild();
			return;
		}
		
		if ( o_selected instanceof MessageGraphic ) {
			
			String x_name = getSelectionName();
			
			o_msc.deleteLink( getSelectionId() );
			
			List x_links = o_msc.getLinks();
			boolean x_found = false;
			for( Iterator i = x_links.iterator() ; i.hasNext() ; ) {
				x_found |= ((Link)i.next()).getName().equals( x_name );
			}
			
			if ( ! x_found ) {
				o_gui.removeFromMsgAlphabet( x_name );
			}
			
		} else if ( o_selected instanceof DividerGraphic ) {
			o_msc.deleteDivider( Integer.parseInt( o_selected.getId() ) );
		} else if ( o_selected instanceof InstanceGraphic ) {
			o_msc.deleteInstance( getSelectionId() );
		}
	}
	
	void changeLinkName( String p_from , String p_to ) {
		
		o_msc.changeLinkName( p_from , p_to );
	}
	
	void reverseSelectedLink() {
		
		o_msc.reverseLink( getSelectionId() );
	}
	
	void negateSelectedLink() {
		
		o_msc.negateLink( getSelectionId() );
	}
	
	void tidy() {
		
		o_msc.tidy();
	}
	
	boolean inAddLinkMode() { return o_gui.inAddLinkMode(); }
	
	boolean inAddDividerMode() { return o_gui.inAddDividerMode(); }
	
	boolean addPositive() { return o_gui.addPositive(); }
	
	String getInstanceAt( int p_x , int p_y ) {
		
		for ( Iterator i = o_instance_glyphs.iterator() ; i.hasNext() ; ) {
			
			InstanceGraphic x_inst_gr = (InstanceGraphic)i.next();
			
			if ( x_inst_gr.contains( p_x , p_y ) ) {
				return x_inst_gr.getName();
			}
		}
		
		return null;
	}
	
	Glyph getMessageAt( int p_x , int p_y ) {
		
		for ( Iterator i = o_message_glyphs.iterator() ; i.hasNext() ; ) {
			
			MessageGraphic x_mess_gr = (MessageGraphic)i.next();
			
			if ( x_mess_gr.contains( p_x , p_y ) ) {
				return x_mess_gr;
			}
		}
		
		for ( Iterator i = o_divider_glyphs.iterator() ; i.hasNext() ; ) {
			
			DividerGraphic x_div_gr = (DividerGraphic)i.next();
			
			if ( x_div_gr.contains( p_x , p_y ) ) {
				return x_div_gr;
			}
		}
		
		for ( Iterator i = o_instance_glyphs.iterator() ; i.hasNext() ; ) {
			
			InstanceGraphic x_inst_gr = (InstanceGraphic)i.next();
			
			if ( x_inst_gr.contains( p_x , p_y ) ) {
				return x_inst_gr;
			}
		}
		
		return null;
	}
	
	private int locate( String p_name ) {
		
		for( int i=0 ; i < o_instance_glyphs.size() ; i++ ) {
			if ( ((InstanceGraphic)o_instance_glyphs.get( i )).getName().equals( p_name ) ) { return i; } 
		}
		
		return -1;
	}
	
	public void fileDraw( Graphics g ) {
		paintComponent( g );
	}
	
	
	public void paintComponent( Graphics g ) {
		
		Graphics2D x_g2d = (Graphics2D)g;
		
		//Make background white
		x_g2d.setColor(Color.white);
		x_g2d.fillRect(0, 0, getSize().width -1, getSize().height -1);
		x_g2d.setColor(Color.black);
		
		for( Iterator i = o_instance_glyphs.iterator() ; i.hasNext() ; ) {
			Glyph x_glyph = (Glyph)i.next();
			if ( x_glyph == o_selected ) {
				g.setColor( Color.red );
				x_glyph.draw( g );
				g.setColor( Color.black );
			} else {
				x_glyph.draw( g );
			}
		}
		
		for( Iterator i = o_message_glyphs.iterator() ; i.hasNext() ; ) {
			Glyph x_glyph = (Glyph)i.next();
			if ( x_glyph == o_selected ) {
				g.setColor( Color.red );
				x_glyph.draw( g );
				g.setColor( Color.black );
			} else {
				x_glyph.draw( g );
			}
		}
		
		for( Iterator i = o_divider_glyphs.iterator() ; i.hasNext() ; ) {
			Glyph x_glyph = (Glyph)i.next();
			if ( x_glyph == o_selected ) {
				g.setColor( Color.red );
				x_glyph.draw( g );
				g.setColor( Color.black );
			} else {
				x_glyph.draw( g );
			}
			
			/* Glyph x_glyph = (Glyph)i.next();
			 x_glyph.draw( g );
			 */
		}
	}
    
    public void addWeight(String p_weight) {
        try {
            MessageGraphic g = ((MessageGraphic)o_selected);
            g.o_weight = Double.parseDouble(p_weight);
            String x_id = g.getId();
            o_msc.setLinkWeight(x_id, g.o_weight);
            rebuild();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

}
