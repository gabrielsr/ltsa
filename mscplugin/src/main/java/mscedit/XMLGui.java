package mscedit;

import ic.doc.extension.LTSA;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;

import java.util.*;
import java.io.*;

//class XMLGui extends JFrame {

public class XMLGui extends JPanel {
	
	private HMSCEditorCanvas o_hmsc_canvas;
	private Specification o_spec;
	
	private JTabbedPane o_tabbed_pane;
	private JToolBar o_toolbar;
	private JPopupMenu o_popup, o_hmsc_popup;
	private JPopupMenu o_ptransition_popup;
    private JPopupMenu o_bmsctransition_popup;
	
	private List o_editors;
	
	private JButton o_pointer, o_delete, o_newmsc, o_newinst, o_newmsg, o_newtrans, o_newnegmsg, o_newdiv;
	
	private boolean o_positive = true;
	private boolean o_add_link_mode = false;
	private boolean o_add_divider_mode = false;
	private String o_link_name = ""; 
	private int o_neg_count = 0;
	private HashSet o_message_alphabet;
	private List o_instance_alphabet;
	
	private LTSA o_ltsa;
	public XMLGui() {
		
		//super( "Message Sequence Chart editor" );
		
		o_editors = new ArrayList();
		o_spec = new Specification();
		o_tabbed_pane = new JTabbedPane();
		o_hmsc_canvas = new HMSCEditorCanvas( this );
		o_spec.addHMSC( o_hmsc_canvas.getHMSC() );
		o_spec.addBMSC( new BMSC( "init" ) );
		o_hmsc_canvas.addBMSC( "init" , 50 , 30 );
		o_hmsc_canvas.rebuild();
		
		HMSCCanvasMouseListener x_mouse_listener = new HMSCCanvasMouseListener( o_hmsc_canvas );
		o_hmsc_canvas.addMouseListener( x_mouse_listener );
		o_hmsc_canvas.addMouseMotionListener( x_mouse_listener );
		
		o_toolbar = new JToolBar("MSC Editor");
		
		/*JMenuBar x_menubar = new JMenuBar();
		 
		 setJMenuBar( x_menubar );
		 */
		
		populateMenus();
		populateToolBar();
		
		o_instance_alphabet = new ArrayList();
		o_message_alphabet = new HashSet();
		
		o_hmsc_canvas.setPreferredSize( new Dimension( 500,400 ));
		setLocation( 150,150 );
		
		o_tabbed_pane.addTab( "hMSC" , null , new JScrollPane( o_hmsc_canvas ) , "High-Level Message Sequence Chart" );
		o_editors.add( o_hmsc_canvas );
		o_tabbed_pane.addChangeListener( new ChangeListener() {
			
			public void stateChanged( ChangeEvent p_ce ) {
				
				enableButtons();
			} 
		});
		/*
		 this.getContentPane().setLayout( new BorderLayout() );
		 this.getContentPane().add( o_toolbar , BorderLayout.NORTH );
		 this.getContentPane().add( o_tabbed_pane , BorderLayout.CENTER );
		 */
		
		enableButtons();
		
		setLayout( new BorderLayout() );
		
		add( o_tabbed_pane , BorderLayout.CENTER );
		add( o_toolbar , BorderLayout.SOUTH );
	}
	
	private void enableButtons() {
		
		if ( o_tabbed_pane.getSelectedIndex() == 0 ) {
			//enable hmsc relevant buttons
			o_newmsc.setEnabled( true );
			o_newtrans.setEnabled( true );
			o_newmsg.setEnabled( false );
			o_newnegmsg.setEnabled( false );
			o_newdiv.setEnabled( false );
			o_newinst.setEnabled( false );
		} else {
			//enable bmsc relevant buttons
			o_newmsc.setEnabled( true );
			o_newtrans.setEnabled( false );
			o_newmsg.setEnabled( true );
			o_newnegmsg.setEnabled( true );
			o_newdiv.setEnabled( true );
			o_newinst.setEnabled( true );
		}
	}
	
	public Specification getSpecification() {
		
		return o_spec;
	}
	
	void populateMenus() {    // JMenuBar p_menubar ) {
		
		o_popup = new JPopupMenu("Message");
		o_hmsc_popup = new JPopupMenu("hmsc");
		o_ptransition_popup = new JPopupMenu("Transition");
        o_bmsctransition_popup = new JPopupMenu("Transition");
				
		//It is not possible to add the same menu item to a number of different menus
		//so we have to create duplicates.
		
		ActionListener xRenameListener = new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.rename(); }	
		};
		
		JMenuItem x_item = new JMenuItem( "Rename" );
		x_item.addActionListener( xRenameListener );
		o_popup.add( x_item );
		
		x_item = new JMenuItem( "Rename" );
		x_item.addActionListener( xRenameListener );
		o_hmsc_popup.add( x_item );
		
        x_item = new JMenuItem( "Rename" );
        x_item.addActionListener( xRenameListener );
        o_ptransition_popup.add( x_item );

        x_item = new JMenuItem( "Rename" );
        x_item.addActionListener( xRenameListener );
        o_bmsctransition_popup.add( x_item );

		x_item = new JMenuItem( "Reverse arrow" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.reverseLink(); }	
		});
		o_popup.add( x_item );
		
		ActionListener xDeleteListener = new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.deleteMessage(); }
		};
		
		x_item = new JMenuItem( "Delete" );
		x_item.addActionListener( xDeleteListener );
		o_popup.add( x_item );
		
		x_item = new JMenuItem( "Delete" );
		x_item.addActionListener( xDeleteListener );
		o_hmsc_popup.add( x_item );

        x_item = new JMenuItem( "Delete" );
        x_item.addActionListener( xDeleteListener );
        o_ptransition_popup.add( x_item );

        x_item = new JMenuItem( "Delete" );
        x_item.addActionListener( xDeleteListener );
        o_bmsctransition_popup.add( x_item );
		
        x_item = new JMenuItem( "Add Weight" );
        x_item.addActionListener( new GuiEventListener( this ) {
            public void actionPerformed( ActionEvent p_e ) { o_gui.addWeight(); }   
        });
        o_ptransition_popup.add( x_item );

        x_item = new JMenuItem( "Add Weight" );
        x_item.addActionListener( new GuiEventListener( this ) {
            public void actionPerformed( ActionEvent p_e ) { o_gui.addBMSCWeight(); }   
        });
        o_bmsctransition_popup.add( x_item );

		x_item = new JMenuItem( "Tidy" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.tidyChart(); }	
		});
		o_popup.add( x_item );
		
        x_item = new JMenuItem( "Duplicate Scenario");
        x_item.addActionListener( new GuiEventListener( this ) {
            public void actionPerformed( ActionEvent p_e ) { o_gui.duplicateBMSC(); }
        });
        o_popup.add( x_item );

//		x_item = new JMenuItem( "New instance" );
//		x_item.addActionListener( new GuiEventListener( this ) {
//		public void actionPerformed( ActionEvent p_e ) { o_gui.newInstance(); }
//		} );
//		x_insert_menu.add( x_item );
		
//		x_item = new JMenuItem( "New message" );
//		x_item.addActionListener( new GuiEventListener( this ) {
//		public void actionPerformed( ActionEvent p_e ) { o_gui.newMessage( true ); }
//		} );
//		x_insert_menu.add( x_item );
		
//		x_item = new JMenuItem( "New transition" );
//		x_item.addActionListener( new GuiEventListener( this ) {
//		public void actionPerformed( ActionEvent p_e ) { o_gui.newTransition(); }
//		} );
//		x_insert_menu.add( x_item );
		
//		x_item = new JMenuItem( "Large display" );
//		x_item.addActionListener( new GuiEventListener( this ) {
//		public void actionPerformed( ActionEvent p_e ) { o_gui.setBigFont( true ); }
//		});
//		x_option_menu.add( x_item );
		
//		p_menubar.add( x_file_menu );
//		p_menubar.add( x_edit_menu );
//		p_menubar.add( x_insert_menu );
//		p_menubar.add( x_option_menu );
	} 
	
    protected void addWeight() {
        String x_weight = JOptionPane.showInputDialog( null , "Enter a weight for the transition:" , "Set weight" , JOptionPane.QUESTION_MESSAGE );
        o_hmsc_canvas.addWeight( x_weight );
    }

    protected void addBMSCWeight() {
        String x_weight = JOptionPane.showInputDialog( null , "Enter a weight for the transition:" , "Set weight" , JOptionPane.QUESTION_MESSAGE );
        try {
            // TODO this is a terrible hack, get rid of it!
            Object o = o_tabbed_pane.getSelectedComponent();
            JScrollPane s = (JScrollPane)o;
            for (int i=0; i<s.getComponentCount(); i++) {
                Component c = s.getComponent(i);
                if (c instanceof JViewport) {
                    JViewport v = (JViewport)c;
                    for (int j=0; j<v.getComponentCount(); j++) {
                        Component cc = v.getComponent(j);
                        if (cc instanceof BMSCEditorCanvas) {
                            // we found it
                            BMSCEditorCanvas bmsc_canvas = (BMSCEditorCanvas)cc;
                            bmsc_canvas.addWeight( x_weight );
                            return;
                        }
                    }
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

 	void setBigFont( boolean p_set ) {
		
		if ( p_set ) {
			Glyph.setFont( new Font("Terminal",Font.BOLD,15 ) );
			Glyph.setScale( 1.5 );
		} else {
			Glyph.setFont( new Font("Terminal",Font.PLAIN,11) );
			Glyph.setScale( 1.0 );
		}
		
		
		for ( Iterator i = o_editors.iterator() ; i.hasNext() ; ) {
			
			EditorCanvas x_canvas = (EditorCanvas)i.next();
			x_canvas.rebuild();
		}
	}
	
	void showToolBar( boolean p_show ) {
		
		if (p_show) { 
			add( BorderLayout.SOUTH , o_toolbar );
		} else {
			remove( o_toolbar );
		}
	}
	
	void newMSC() {
		
		String x_name = JOptionPane.showInputDialog( null , "Enter a name for the MSC:" , "New MSC" , JOptionPane.QUESTION_MESSAGE );
		
		if ( x_name != null && ! x_name.trim().equals("") ) {
			
			if ( existsMSCCalled( x_name ) || x_name.trim().equals("init") ) {
				
				JOptionPane.showMessageDialog( null , "MSC called " + x_name + " already exists." , "Duplicate name" , JOptionPane.ERROR_MESSAGE );
				return;
			}
			
			x_name = capitalise( x_name );
			
			BMSCEditorCanvas x_canvas = new BMSCEditorCanvas( this , x_name );
			CanvasMouseListener x_mouse_listener = new CanvasMouseListener( x_canvas );
			x_canvas.addMouseListener( x_mouse_listener  );
			x_canvas.addMouseMotionListener( x_mouse_listener );
			
			o_tabbed_pane.addTab( x_name , null , new JScrollPane( x_canvas ) , "Basic Message Sequence Chart" );
			o_editors.add( x_canvas );
			o_tabbed_pane.setSelectedIndex( o_tabbed_pane.indexOfTab( x_name ) );
			
			//initialise new scenario with all current instances.
			for ( Iterator i = o_instance_alphabet.iterator() ; i.hasNext() ; ) {
				x_canvas.addInstance( (String)i.next() );
			}
			
			o_spec.addBMSC( x_canvas.getBMSC() );
			
			x_canvas.rebuild();
			
			o_hmsc_canvas.addBMSC( x_name , ( o_tabbed_pane.getTabCount() - 2 ) % 4 * 120 + 50 , ( o_tabbed_pane.getTabCount() - 2 ) / 4 * 100 + 130 );
			o_hmsc_canvas.rebuild();
		}
	}
	
	public void addBMSC( BMSC p_bmsc ) {
		
		BMSCEditorCanvas x_canvas = new BMSCEditorCanvas( this , p_bmsc );
		CanvasMouseListener x_mouse_listener = new CanvasMouseListener( x_canvas );
		x_canvas.addMouseListener( x_mouse_listener  );
		x_canvas.addMouseMotionListener( x_mouse_listener );
		
		o_tabbed_pane.addTab( p_bmsc.getName() , null , new JScrollPane( x_canvas ) , "Implied Scenario" );
		o_editors.add( x_canvas );
		o_tabbed_pane.setSelectedIndex( o_tabbed_pane.indexOfTab( p_bmsc.getName() ) );
		o_spec.addBMSC( x_canvas.getBMSC() );
		
		x_canvas.rebuild();
		
	}
	
	public void addBMSCtoHMSC( BMSC p_bmsc ) {
		
		o_hmsc_canvas.addBMSC( p_bmsc.getName() , ( o_tabbed_pane.getTabCount() - 2 ) % 4 * 120 + 50 , ( o_tabbed_pane.getTabCount() - 2 ) / 4 * 100 + 130 );
		o_hmsc_canvas.rebuild();    
		
	}
	
	File fileChooser( String p_name ) {
		
		JFileChooser x_filechooser = new JFileChooser( new File( o_ltsa.getCurrentDirectory() ) );
		ExampleFileFilter x_filter = new ExampleFileFilter();
		x_filter.addExtension("xml");
		x_filter.setDescription("XML files");
		x_filechooser.addChoosableFileFilter(x_filter);
		x_filechooser.setApproveButtonText( p_name );
		x_filechooser.setDialogTitle( p_name );
		
		int x_rv = x_filechooser.showOpenDialog(this);
		
		if ( x_rv == JFileChooser.APPROVE_OPTION) {
			
			o_ltsa.setCurrentDirectory( x_filechooser.getCurrentDirectory().getAbsolutePath() );
			
			return x_filechooser.getSelectedFile();
		}
		
		return null;
	}
	
	
	void newSpec() {
		
		o_spec = new Specification();
		o_tabbed_pane.removeAll();
		o_editors.clear();
		o_hmsc_canvas = new HMSCEditorCanvas( this );
		o_spec.addHMSC( o_hmsc_canvas.getHMSC() );
		o_spec.addBMSC( new BMSC( "init" ) );
		o_hmsc_canvas.addBMSC( "init" , 50 , 30 );
		o_hmsc_canvas.rebuild();
		
		HMSCCanvasMouseListener x_mouse_listener = new HMSCCanvasMouseListener( o_hmsc_canvas );
		o_hmsc_canvas.addMouseListener( x_mouse_listener );
		o_hmsc_canvas.addMouseMotionListener( x_mouse_listener );
		o_tabbed_pane.addTab( "hMSC" , null , new JScrollPane( o_hmsc_canvas ) , "High-Level Message Sequence Chart" );
		o_editors.add( o_hmsc_canvas );
		enableButtons();
		
		o_neg_count = 0;
		o_message_alphabet = new HashSet();
		o_instance_alphabet = new ArrayList();
	}
	
	void openFile( File p_file ) {
		
		//	File x_file = fileChooser("Open");
		
		if ( p_file != null ) {
			
			o_spec = new Specification( XMLUtil.loadFromFile( p_file ) );
			o_tabbed_pane.removeAll();
			o_editors.clear();
			
			HMSC x_hmsc = o_spec.getHMSC();
			o_hmsc_canvas = new HMSCEditorCanvas( this , x_hmsc );
			
			HMSCCanvasMouseListener x_mouse_listener = new HMSCCanvasMouseListener( o_hmsc_canvas );
			o_hmsc_canvas.addMouseListener( x_mouse_listener );
			o_hmsc_canvas.addMouseMotionListener( x_mouse_listener );
			o_tabbed_pane.addTab( "hMSC" , null , new JScrollPane( o_hmsc_canvas ) , "High-Level Message Sequence Chart" );
			o_editors.add( o_hmsc_canvas );
			
			o_neg_count = 0;
			
			List x_bmscs = o_spec.getBMSCs();
			
			o_instance_alphabet = new ArrayList();
			o_message_alphabet = new HashSet();
			
			for( Iterator i = x_bmscs.iterator() ; i.hasNext() ; ) {
				
				BMSC x_bmsc = (BMSC)i.next();
				
				//don't create an editor for the init bmsc
				if ( x_bmsc.getName().equals( "init" ) ) continue;
				
				if ( x_bmsc.isNegative() ) { o_neg_count++; }
				
				BMSCEditorCanvas x_editor = new BMSCEditorCanvas( this , x_bmsc );
				CanvasMouseListener x_listener = new CanvasMouseListener( x_editor );
				x_editor.addMouseListener( x_listener );
				x_editor.addMouseMotionListener( x_listener );
				o_tabbed_pane.addTab( x_bmsc.getName() , null , new JScrollPane( x_editor ) , "Basic Message Sequence Chart" );
				o_editors.add( x_editor );
				
				List x_instances = x_bmsc.getInstances();
				for( Iterator j = x_instances.iterator() ; j.hasNext() ; ) {
					String x_name = ((Instance)j.next()).getName();
					if( !  o_instance_alphabet.contains( x_name ) ) {
						o_instance_alphabet.add( x_name );
					}
				}
				
				List x_links = x_bmsc.getLinks();
				for( Iterator j = x_links.iterator() ; j.hasNext() ; ) {
					o_message_alphabet.add( ((Link)j.next()).getName());
				}
			}
		}
		
		o_hmsc_canvas.rebuild();
		updateTitleBar();
	}
	
	void saveFile() {
		
		File x_file = fileChooser("Save");
		
		if ( x_file != null ) {
			
			XMLUtil.saveToFile( o_spec.asXML() , x_file );
		}
	}
	
	void saveFile( FileOutputStream p_fos ) {
		
		if ( p_fos != null ) {
			
			XMLUtil.saveToFile( o_spec.asXML() , p_fos );
		}
	}
	
	void newInstance() {
		
		if ( o_tabbed_pane.getTabCount() == 0 ) { newMSC(); }
		
		Object[] x_possibilities = new Object[o_instance_alphabet.toArray().length + 1];
		System.arraycopy( o_instance_alphabet.toArray() , 0 , x_possibilities , 1 , o_instance_alphabet.toArray().length );
		Arrays.sort( x_possibilities  , 1 , x_possibilities.length );
		x_possibilities[0] = "New...";
		
		String x_name = (String)JOptionPane.showInputDialog( null , "Select instance name:" , "New Instance" , JOptionPane.QUESTION_MESSAGE ,null , x_possibilities , "New..." );
		
		if ( x_name != null && x_name.equals("New...") ) {
			x_name = (String)JOptionPane.showInputDialog( null , "Enter instance name:" , "New Instance" , JOptionPane.QUESTION_MESSAGE );
		}
		
		if ( x_name != null && ! x_name.trim().equals("") && x_name.indexOf( ":" ) != 0 ) {
			
			BMSCEditorCanvas x_current = (BMSCEditorCanvas)getCurrentEditor();
			
			for ( Iterator i = x_current.getBMSC().getInstances().iterator() ; i.hasNext() ; ) {
				if ( ((Instance)i.next()).getName().equals( x_name ) ) {
					JOptionPane.showMessageDialog( null , "Instance called " + x_name + " already exists." , "Duplicate name" , JOptionPane.ERROR_MESSAGE );
					return;
				}
			}
			
			x_name = capitalise( x_name );
			
			if ( ! o_instance_alphabet.contains( x_name ) ) {
				o_instance_alphabet.add( x_name );
			}
			
			x_current.addInstance( x_name );
			x_current.rebuild();
		}
	}
	
	void newMessage( boolean p_positive ) {
		
		if ( o_tabbed_pane.getSelectedIndex() > 0 ) {
			
			Object[] x_possibilities = new Object[o_message_alphabet.toArray().length + 1];
			System.arraycopy( o_message_alphabet.toArray() , 0 , x_possibilities , 1 , o_message_alphabet.toArray().length );
			Arrays.sort( x_possibilities , 1 , x_possibilities.length );
			x_possibilities[0] = "New...";
			
			/*
			 JOptionPane pane = new JOptionPane( "Select link name:" , JOptionPane.QUESTION_MESSAGE , 0  , null , x_possibilities , "New..." );
			 JDialog dialog = new JDialog();
			 dialog.setContentPane( pane ); //.createDialog( this , "New Link" );
			 
			 dialog.show();
			 Object selectedValue = pane.getValue();
			 
			 System.out.println("new code");
			 
			 String x_name = null;
			 if ( selectedValue instanceof String ) { x_name = (String)selectedValue; }
			 */
			
			String x_name = (String)JOptionPane.showInputDialog( null , "Select link name:" , "New Link" , JOptionPane.QUESTION_MESSAGE ,null , x_possibilities , "New..." );
			
			if ( x_name != null && x_name.equals("New...") ) {
				x_name = (String)JOptionPane.showInputDialog( null , "Enter link name:" , "New Link" , JOptionPane.QUESTION_MESSAGE );
			} 
			
			
			if ( x_name != null && ! x_name.trim().equals("") ) {
				
				x_name = smallCaps( x_name );
				
				//if we know about this message add it automatically
				if ( o_message_alphabet.contains( x_name ) ) {
					
					List x_all_links = getSpecification().getAllLinks();
					String x_from = "";
					String x_to = "";
					for( Iterator i = x_all_links.iterator() ; i.hasNext() ; ) {
						Link x_l = (Link)i.next();
						if ( x_l.getName().equals( x_name ) ) {
							x_from = x_l.getFrom();
							x_to = x_l.getTo();
							break;
						}
					}
					
					BMSCEditorCanvas x_current = (BMSCEditorCanvas)getCurrentEditor();
					int x_ti = x_current.getBMSC().getLastTimeIndex() + 1;
					x_current.addLink( x_from , x_to , "@@" + x_name , x_ti );
					x_current.rebuild();
					return;
				}
				
				//	o_message_alphabet.add( x_name );
				
				o_positive = p_positive;
				o_add_link_mode = true;
				o_link_name = x_name;
				
			}
			
			setCursor( new Cursor(Cursor.CROSSHAIR_CURSOR) );
		}
	}
	
	public void addMessageToAlphabet( String p_name ) {
		
		o_message_alphabet.add( p_name );
	}
	
	void newTransition() {
		
		if ( o_tabbed_pane.getSelectedIndex() == 0 ) {
			
			o_add_link_mode = true;
			setCursor( new Cursor(Cursor.CROSSHAIR_CURSOR) );
		}
	}
	
	void newDivider() {
		
		o_add_link_mode = false;
		o_add_divider_mode = true;
		
		setCursor( new Cursor(Cursor.CROSSHAIR_CURSOR) );
	}
	
	void deleteMessage() {
		
		EditorCanvas x_current = getCurrentEditor();
		x_current.delete();
		x_current.rebuild();
	}
	
	void deleteBMSC( String p_name ) {
		
		if ( p_name.equals( "init" ) ) { return; }
		
		getSpecification().deleteBMSC( p_name );
		
		HMSCEditorCanvas x_hmsc_edit = (HMSCEditorCanvas)o_editors.get( 0 );
		x_hmsc_edit.deleteBMSC( p_name );
		x_hmsc_edit.rebuild();
		
		if ( o_tabbed_pane.indexOfTab( p_name ) > -1 ) {
			o_editors.remove( o_tabbed_pane.indexOfTab( p_name ) );
			o_tabbed_pane.removeTabAt( o_tabbed_pane.indexOfTab( p_name ) );
		}
	}
	
	void rename() {
		
		EditorCanvas x_current = getCurrentEditor();
		Glyph x_selection = x_current.getSelection();
		
		if ( x_selection instanceof InstanceGraphic ) {
			
			String x_name = JOptionPane.showInputDialog( null , "Enter new name:" , 
					"Rename instance globally" , JOptionPane.QUESTION_MESSAGE );	
			renameInstance( x_current.getSelectionId() , x_name );
			
		} else if ( x_selection instanceof BMSCGraphic ) {
			
			String x_oldname = x_selection.getName();
			
			if ( x_oldname.equals( "init" ) ) { return; } //can't rename init
			
			String x_newname = JOptionPane.showInputDialog( null , "Enter new name:" , 
					"Rename bMSC" , JOptionPane.QUESTION_MESSAGE );	
			
			if ( x_newname == null || x_newname.trim().equals("") ) return;
			
			if ( existsMSCCalled( x_newname ) || x_newname.trim().equals("init") ) {
				
				JOptionPane.showMessageDialog( null , "MSC called " + x_newname + " already exists." , 
						"Duplicate name" , JOptionPane.ERROR_MESSAGE );
				return;
			}
			
			x_newname = capitalise( x_newname );
			
			HMSC x_hmsc = o_spec.getHMSC();
			x_hmsc.renameBMSC( x_oldname , x_newname );
			o_spec.renameBMSC( x_oldname , x_newname );
			
			renameTab( x_oldname , x_newname );
			x_current.rebuild();
			
		} else if ( x_selection instanceof MessageGraphic ) {
			
			String x_name = JOptionPane.showInputDialog( null , "Enter new name:" , 
					"Rename Message" , JOptionPane.QUESTION_MESSAGE );	
			x_current.changeLinkName( x_current.getSelectionId() , x_name );
			x_current.rebuild();
		}
		
	}
	
	void reverseLink() {
		
		EditorCanvas x_current = getCurrentEditor();
		x_current.reverseSelectedLink();
		x_current.rebuild();
	}
	
	void negateMessage() {
		
		EditorCanvas x_current = getCurrentEditor();
		x_current.negateSelectedLink();
		x_current.rebuild();
	}
	
	void tidyChart() {
		
		EditorCanvas x_current = getCurrentEditor();
		x_current.tidy();
		x_current.rebuild();
	}
	
	private String prefix( String p_str ) {
		return p_str.substring( 0 , p_str.indexOf( ":" ) );
	}
	
	void renameInstance( String p_oldname , String p_newname ) {
		
		List x_bmscs = o_spec.getBMSCs();
		
		for ( Iterator i = x_bmscs.iterator() ; i.hasNext() ; ) {
			
			BMSC x_bmsc = (BMSC)i.next();
			List x_instances = x_bmsc.getInstances();
			
			for ( Iterator j = x_instances.iterator() ; j.hasNext() ; ) {
				
				Instance x_inst = (Instance)j.next();
				if ( x_inst.getName().equals( p_oldname ) ) {
					x_inst.setName( p_newname );
				}
			}
			
			List x_msgs = x_bmsc.getLinks();
			
			for ( Iterator j = x_msgs.iterator() ; j.hasNext() ; ) {
				
				Link x_link = (Link)j.next();
				String x_name = x_link.getName();
				
				o_message_alphabet.remove( x_name );
				
				if ( x_link.getTo().equals( p_oldname ) ) {
					
					x_bmsc.deleteLink( x_link.getId() );
					x_link.setTo( p_newname );
					x_link.setName( x_name.replaceAll( "," + prefix(p_oldname) + "," , "," + prefix(p_newname) + "," ) );
					x_bmsc.addLink( x_link );
				}
				
				if ( x_link.getFrom().equals( p_oldname ) ) {
					
					x_bmsc.deleteLink( x_link.getId() );
					x_link.setFrom( p_newname );
					x_link.setName( x_name.replaceAll( prefix(p_oldname) + "," , prefix(p_newname) + "," ) );
					x_bmsc.addLink( x_link );
				}
				
				o_message_alphabet.add( x_link.getName() );
			}
		}
		
		for ( Iterator i = o_editors.iterator() ; i.hasNext() ; ) {
			
			EditorCanvas x_canvas = (EditorCanvas)i.next();
			x_canvas.rebuild();
		}
	}
	
	void duplicateBMSC() {
		
		EditorCanvas x_current = getCurrentEditor();
		
		if ( x_current instanceof BMSCEditorCanvas ) {
			
			// cast it
			BMSCEditorCanvas x_canvas = (BMSCEditorCanvas)x_current;
			BMSC x_bmsc = x_canvas.getBMSC();
			
			// get new name
			String x_name = JOptionPane.showInputDialog( null , "Name for new scenario:" , "Duplicate Scenario" , JOptionPane.QUESTION_MESSAGE );	
			
			BMSC x_copy = x_bmsc.duplicate(); 
			// make sure to do a deep copy, or editing one will affect the copy
			
			x_copy.setName( x_name );
			addBMSC( x_copy );
		}
	}
	
	void removeFromMsgAlphabet( String p_name ) {
		
		o_message_alphabet.remove( p_name );
	}
	
	//test code >>>
	/*
	 void testVisitor() {
	 
	 TestVisitor v = new TestVisitor();
	 o_spec.apply( v );
	 }
	 */
	
	//<<<<
	
	public boolean inAddLinkMode() { return o_add_link_mode; }
	
	public boolean inAddDividerMode() { return o_add_divider_mode; }
	
	public void setAddDividerMode( boolean p_set ) { o_add_divider_mode = p_set; }
	
	public boolean addPositive() { return o_positive; }
	
	public void setAddLinkMode( boolean p_addlink ) { o_add_link_mode = p_addlink; }
	
	public String getLinkName() {
		
		return o_link_name;
	}
	
	public List getInstanceAlphabet() { return o_instance_alphabet; }
	
	public void addLink( String p_from , String p_to , int p_timeindex ) {
		
		EditorCanvas x_current = getCurrentEditor();
		x_current.addLink( p_from , p_to , o_link_name, p_timeindex );
		o_add_link_mode = false;
		setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
		x_current.rebuild();
	}
	
	public void addNegativeLink( String p_from , String p_to , int p_timeindex ) {
		
		EditorCanvas x_current = getCurrentEditor();
		x_current.addNegativeLink( p_from , p_to , o_link_name, p_timeindex );
		o_add_link_mode = false;
		o_positive = true;
		x_current.rebuild();
	}
	
	int getNegCount() { return o_neg_count; }
	
	void setNegCount( int p_count ) { o_neg_count = p_count; }
	
	void updateTitleBar() {
		
		//setTitle( "Message Sequence Chart editor" );
	}
	
	public void redrawCurrentCanvas() {
		
		EditorCanvas x_current = getCurrentEditor();
		x_current.rebuild();
	}
	
	public void showPopup( MouseEvent p_me , Glyph p_selection ) {
		
		if ( p_me.getComponent() == o_hmsc_canvas ) {
			
			if ( p_selection instanceof BMSCGraphic ) {
				o_hmsc_popup.show( p_me.getComponent() , p_me.getX() , p_me.getY() );
			}
			
			if ( p_selection instanceof TransitionGraphic ) {
				o_ptransition_popup.show( p_me.getComponent() , p_me.getX() , p_me.getY() );
			}
            
        } else if (p_selection instanceof MessageGraphic) {
            o_bmsctransition_popup.show( p_me.getComponent() , p_me.getX() , p_me.getY() );
		} else {
			o_popup.show( p_me.getComponent() , p_me.getX() , p_me.getY() );
		}
	}
	
	private void populateToolBar() {
		
		o_pointer = new JButton( new ImageIcon( this.getClass().getResource( "/mscedit/icon/pointer.gif" ) ) );
		o_pointer.setToolTipText("Select");
		o_pointer.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) {
				o_add_link_mode = false;
				o_add_divider_mode = false;
				EditorCanvas x_current = getCurrentEditor();
				x_current.setSelection(null);
				x_current.setOldSelection(null);
				x_current.rebuild();
				setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
			}
		} );
		o_toolbar.add( o_pointer );
		
		o_delete = new JButton( new ImageIcon( this.getClass().getResource( "/mscedit/icon/delete.gif" ) ) );
		o_delete.setToolTipText("Delete");
		o_delete.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) {
				o_gui.deleteMessage(); 
			}
		});
		o_toolbar.add( o_delete );
		
		o_newmsc = new JButton( new ImageIcon( this.getClass().getResource( "/mscedit/icon/newmsc.gif" ) ) );
		o_newmsc.setToolTipText("New bMSC");
		o_newmsc.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.newMSC(); }
		} );
		o_toolbar.add( o_newmsc );
		
		o_newtrans = new JButton( new ImageIcon( this.getClass().getResource( "/mscedit/icon/newlink.gif" ) ) );
		o_newtrans.setToolTipText("New transition");
		o_newtrans.addActionListener( new GuiEventListener( this ) { 
			public void actionPerformed( ActionEvent p_e ) { o_gui.newTransition(); } 
		} );
		o_toolbar.add( o_newtrans );
		
		o_newinst = new JButton( new ImageIcon( this.getClass().getResource( "/mscedit/icon/newinst.gif" ) ) );
		o_newinst.setToolTipText("New instance");
		o_newinst.addActionListener( new GuiEventListener( this ) { 
			public void actionPerformed( ActionEvent p_e ) { o_gui.newInstance(); } 
		} );
		o_toolbar.add( o_newinst );
		
		o_newmsg = new JButton( new ImageIcon( this.getClass().getResource( "/mscedit/icon/newmsg.gif" ) ) );
		o_newmsg.setToolTipText("New message");
		o_newmsg.addActionListener( new GuiEventListener( this ) { 
			public void actionPerformed( ActionEvent p_e ) { o_gui.newMessage( true ); } 
		} );
		o_toolbar.add( o_newmsg );
		
		o_newnegmsg = new JButton( new ImageIcon( this.getClass().getResource( "/mscedit/icon/negmsg.gif" ) ) );
		o_newnegmsg.setToolTipText("New negative message");
		o_newnegmsg.addActionListener( new GuiEventListener( this ) { 
			public void actionPerformed( ActionEvent p_e ) { o_gui.newMessage( false ); } 
		} );
		o_toolbar.add( o_newnegmsg );
		
		o_newdiv = new JButton( new ImageIcon( this.getClass().getResource( "/mscedit/icon/div.gif" ) ) );
		o_newdiv.setToolTipText("New divider");
		o_newdiv.addActionListener( new GuiEventListener( this ) { 
			public void actionPerformed( ActionEvent p_e ) { o_gui.newDivider(); } 
		} );
		o_toolbar.add( o_newdiv ); 
	}
	
	
	
	// > rbc
	
	public void populateMSCMenus( JMenuBar p_menubar , LTSA p_ltsa ) {
		
		o_ltsa = p_ltsa;
		
		JMenu x_file_menu = new JMenu("File");
		JMenu x_edit_menu = new JMenu("Edit");
		JMenu x_insert_menu = new JMenu("Insert");
		JMenu x_option_menu = new JMenu("Options");
		
		JMenuItem x_item = new JMenuItem( "New..." );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.newSpec(); }
		} );
		x_file_menu.add( x_item );
		
//		x_item = new JMenuItem( "Open..." );
//		x_item.addActionListener( new GuiEventListener( this ) {
//		public void actionPerformed( ActionEvent p_e ) { o_gui.openFile(); }
//		} );
//		x_file_menu.add( x_item );
		
		x_item = new JMenuItem( "Save as..." );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.saveFile(); }
		} );
		x_file_menu.add( x_item );
		
		x_item = new JMenuItem( "Export graphics...");
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) {
				
				EditorCanvas x_current = getCurrentEditor();
				o_ltsa.exportGraphic( x_current ); } 
		} );
		x_file_menu.add( x_item );
		
		x_item = new JMenuItem( "Rename" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.rename(); }	
		});
		x_edit_menu.add( x_item );
		
		x_item = new JMenuItem( "Reverse arrow" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.reverseLink(); }	
		});
		x_edit_menu.add( x_item );
		
		x_item = new JMenuItem( "Delete" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.deleteMessage(); }
		});
		x_edit_menu.add( x_item );
		
		x_item = new JMenuItem( "Negate message" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.negateMessage(); }
		});
		x_edit_menu.add( x_item );
		
		
		x_item = new JMenuItem( "Tidy" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.tidyChart(); }	
		});
		x_edit_menu.add( x_item );
		
		x_item = new JMenuItem( "New bMSC" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.newMSC(); }
		} );
		x_insert_menu.add( x_item );
		
		x_item = new JMenuItem( "New instance" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.newInstance(); }
		} );
		x_insert_menu.add( x_item );
		
		x_item = new JMenuItem( "New transition" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.newTransition(); }
		} );
		x_insert_menu.add( x_item );
		
		
		x_item = new JMenuItem( "New message" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.newMessage( true ); }
		} );
		x_insert_menu.add( x_item );
		
		x_item = new JMenuItem ("New negative message");
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.newMessage( false ); }
		} );
		x_insert_menu.add( x_item );
		
		x_item = new JMenuItem ("New divider");
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.newDivider(); }
		} );
		x_insert_menu.add( x_item );
		
		x_item = new JCheckBoxMenuItem( "Large display" );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.setBigFont( ((JCheckBoxMenuItem)p_e.getSource()).isSelected() ); }
		});
		x_option_menu.add( x_item );
		
		x_item = new JCheckBoxMenuItem( "Show toolbar" );
		x_item.setSelected( true );
		x_item.addActionListener( new GuiEventListener( this ) {
			public void actionPerformed( ActionEvent p_e ) { o_gui.showToolBar( ((JCheckBoxMenuItem)p_e.getSource()).isSelected() ); }
		});
		x_option_menu.add( x_item );
		
		p_menubar.add( x_file_menu );
		p_menubar.add( x_edit_menu );
		p_menubar.add( x_insert_menu );
		p_menubar.add( x_option_menu );
	} 
	
	public void renameTab( String p_from , String p_to ) {
		
		o_tabbed_pane.setTitleAt( o_tabbed_pane.indexOfTab( p_from ) , p_to );
	}
	
	public void swapToTab( String p_name ) {
		
		if ( o_tabbed_pane.indexOfTab( p_name ) > -1 ) {
			o_tabbed_pane.setSelectedIndex( o_tabbed_pane.indexOfTab( p_name ) );
		}
	}
	
	public void rebuildTab( String p_name ) {
		
		EditorCanvas x_tab = (EditorCanvas)o_editors.get( o_tabbed_pane.indexOfTab( p_name ) );
		x_tab.rebuild();
	}
	
	EditorCanvas getCurrentEditor() {
		
		EditorCanvas x_current = (EditorCanvas)o_editors.get( o_tabbed_pane.getSelectedIndex() );
		return x_current;
	}
	
	/*
	 public void saveGraphic() {
	 
	 EditorCanvas x_current = (EditorCanvas)o_tabbed_pane.getSelectedComponent();
	 new SVGExporter().export( x_current );  
	 }
	 */
	
	private boolean existsMSCCalled( String p_name ) {
		
		return ( o_tabbed_pane.indexOfTab( p_name ) > -1 ) || ( o_tabbed_pane.indexOfTab( capitalise( p_name ) ) > -1 );
	}
	
	private String capitalise( String p_str ) {
		
		StringBuffer x_str_buf = removeSpaces( p_str );
		int c = x_str_buf.indexOf( ":" );
		
		if ( c < 0 ) { 
			
			x_str_buf.setCharAt( 0 , Character.toUpperCase( x_str_buf.charAt( 0 ) ) );
			
		} else {
			
			x_str_buf = x_str_buf.replace( 0 , c ,  x_str_buf.substring( 0 , c ).toLowerCase() );
			x_str_buf.setCharAt( c + 1 , Character.toUpperCase( x_str_buf.charAt( c + 1 ) ) );
		}
		
		return  x_str_buf.toString();
	}
	
	private String smallCaps( String p_str ) {
		
		StringBuffer x_str_buf = removeSpaces( p_str );
		x_str_buf.setCharAt( 0 , Character.toLowerCase( x_str_buf.charAt( 0 ) ) );
		return  x_str_buf.toString();
	}
	
	private StringBuffer removeSpaces( String p_str ) {
		
		String x_str = p_str.replace( '.' , '_' );
		
		StringBuffer x_str_buf = new StringBuffer( x_str );
		
		for ( int i = 0 ; i < x_str_buf.length() ; i++ ) {
			if ( x_str_buf.charAt( i ) == ' ' ) {
				x_str_buf.setCharAt( i + 1 , Character.toUpperCase( x_str_buf.charAt( i + 1 ) ) );
			}
		}
		
		for ( int i = 0 ; i < x_str_buf.length() ; i++ ) {
			while ( x_str_buf.charAt( i ) == ' ' ) {
				x_str_buf.deleteCharAt( i );
			}
		}
		
		return x_str_buf;
	}
}

abstract class GuiEventListener implements ActionListener {
	
	protected XMLGui o_gui;
	
	public GuiEventListener( XMLGui p_gui ) { o_gui = p_gui; }
	
	public abstract void actionPerformed( ActionEvent p_e );
}
