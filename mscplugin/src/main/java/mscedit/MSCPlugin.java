package mscedit;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import java.util.*;
import java.awt.event.*;
import java.awt.Component;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileOutputStream;

import synthesis.*;
import ic.doc.extension.*;

public class MSCPlugin extends LTSAPlugin implements	ic.doc.ltsa.common.infra.EventClient {

    private static MSCPlugin o_this;
    private XMLGui o_gui;
    private List o_toolbar_buttons;
    private Map o_menu_items;
    private JMenuBar o_menubar;

    private ImpliedScenarioSynthesiser o_msccompiler;

    private BMSC o_trace;
    private int o_trace_count = 0;

    public MSCPlugin() {}

    public MSCPlugin( LTSA p_ltsa ) { super( p_ltsa ); }

    public static MSCPlugin getInstance() { return o_this; }

    public void initialise() {

		o_this = this;
	 	o_gui = new XMLGui();
		o_msccompiler = new ImpliedScenarioSynthesiser( getLTSA() );
		o_toolbar_buttons = new ArrayList();
		createButtons();
		o_menubar = new JMenuBar();
		o_menu_items = buildMenuItems();
	
		o_gui.populateMSCMenus( o_menubar , getLTSA() );
    }

    public String getName() { return "MSC Editor"; }
    public boolean addAsTab() { return true; }
    public Component getComponent() { return o_gui; }

    public boolean addToolbarButtons() { return true; }

    public List getToolbarButtons() {

		return o_toolbar_buttons;
    }

    public boolean addMenusToMenuBar() { return false; }
    public boolean useOwnMenuBar() { return false; }  // was true - try using exisiting menu options
    public JMenuBar getMenuBar() { return o_menubar; }

    public boolean providesNewFile() { return true; }
    public void newFile() { o_gui.newSpec(); }

    public boolean providesOpenFile() { return true; }
    public void openFile( File p_file ) { o_gui.openFile( p_file ); }

    public boolean providesSaveFile() { return true; }
    public void saveFile( FileOutputStream p_fos ) { o_gui.saveFile( p_fos ); }

    public String getFileExtension() { return "xml"; }

    public boolean addMenuItems() { return true; }

    public Map getMenuItems() { return o_menu_items; }


    public void setBigFont(boolean p_big) {
		
		o_gui.setBigFont(p_big);
	}

	private void createButtons() {

		ImageIcon x_icon = new ImageIcon( this.getClass().getResource( "/mscedit/icon/msc.gif" ) );
	
		LTSAButton x_button = new LTSAButton( x_icon , "Compile MSC" , new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
			    compilemsc();
			}
		    });
	
		o_toolbar_buttons.add( x_button );
		
		x_icon = new ImageIcon( this.getClass().getResource( "/mscedit/icon/mscsafe.gif" ) );
		
		x_button = new LTSAButton( x_icon , "Safety check MSC" , new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
			    safetyCheckMSC();
			}
		    });
	
		o_toolbar_buttons.add( x_button );
    }

    private void compilemsc() { compilemsc( true ); }

    private void compilemsc( boolean p_showoutput ) {
	
		LTSA x_ltsa = getLTSA();
	
		x_ltsa.getUndoManager().discardAllEdits();
		//	x_ltsa.getInput().getDocument().removeUndoableEditListener(undoHandler);
		x_ltsa.invalidateState();
		x_ltsa.clearOutput();
	
		Specification x_spec = o_gui.getSpecification();
	
		if ( x_spec == null ) { System.err.println("Specification return by gui is null"); }
		else {
	
		   // x_spec.apply( new TestVisitor() );
	
		    String x_FSP_spec = o_msccompiler.run( x_spec );
	
		    if ( x_FSP_spec == null || x_FSP_spec.trim().equals("") ) { System.out.println("empty string returned from msc compiler"); }
	
			if ( x_FSP_spec != null ) {
		
			    //x_ltsa.clearInternalBuffer();
			    //x_ltsa.appendInternalBuffer( x_FSP_spec );
			    x_ltsa.getInputPane().setText( x_FSP_spec );
			    if ( p_showoutput ) { x_ltsa.showOutput(); }
			    x_ltsa.parse();
			    x_ltsa.setTargetChoice("ImpliedScenarioCheck");
			    //   input.getDocument().addUndoableEditListener(undoHandler);
			    x_ltsa.updateDoState();
			}
		}
    }

    
    private void safetyCheckMSC() {
	
		compilemsc( false );
	
		LTSA x_ltsa = getLTSA();
		
		x_ltsa.setTargetChoice("ImpliedScenarioCheck");
        x_ltsa.compileNoClear();
	
        Vector x_trace;
	
        if ( ! x_ltsa.isCurrentStateNull() ) {
	    
		    if ( ! x_ltsa.isCurrentStateComposed() ) {
			
				x_ltsa.composeCurrentState();
				x_ltsa.analyseCurrentState();
			       	
				x_trace = x_ltsa.getCurrentStateErrorTrace();
				
				if ( x_trace != null )  {
				    
				    //trace is a vector of String where the last element is the first message of the trace
				    BMSC x_imp_scen = createImpliedScenarioBMSC(x_trace);
		
				    o_gui.addBMSC( x_imp_scen );
				    
				    x_ltsa.swapto("MSC Editor");
				    
				    int x_pos_neg = classifyScenario();
		
				    if ( x_pos_neg < 0 /* negative */ ) {
					
						o_gui.setNegCount( o_gui.getNegCount() + 1 );
						x_imp_scen.setName( "NegScen" + o_gui.getNegCount() );
						x_imp_scen.negateLastMessage();
						o_gui.renameTab( "ImpScen" , "NegScen" + o_gui.getNegCount() );
						o_gui.redrawCurrentCanvas();
		
				    } else if ( x_pos_neg > 0 /* positive */ ) {
					
					//add it to the hmsc
					
						String x_new_name = JOptionPane.showInputDialog( null , "Give the scenario a name:" , "Name scenario" , JOptionPane.QUESTION_MESSAGE);
						
						x_imp_scen.setName( x_new_name );
						o_gui.renameTab( "ImpScen" , x_new_name );
						o_gui.addBMSCtoHMSC( x_imp_scen );
		
				    } else {
		
						// discard this implied scenario
						o_gui.deleteBMSC( x_imp_scen.getName() );
				    }
				    
				} else {
				    
				    JOptionPane.showMessageDialog( null , "No Implied Scenarios" );
				    x_ltsa.outln( "NO IMPLIED SCENARIOS");
				}
		    }
			
		    x_ltsa.postCurrentState();
		}
    }
	
    private BMSC createImpliedScenarioBMSC( List p_imp_scen_msgs ) {
		
		List x_all_links = o_gui.getSpecification().getAllLinks();
	
		BMSC x_imp_scen = new BMSC( "ImpScen" );
		
		//initialise new scenario with all current instances.
		for ( Iterator i = o_gui.getInstanceAlphabet().iterator() ; i.hasNext() ; ) {
		    x_imp_scen.addInstance( (String)i.next() );
		}
		
		int x_ti = 1;
		
		for ( Iterator i = p_imp_scen_msgs.iterator() ; i.hasNext() ; ) {
		    
		    String x_name = (String)i.next();
		    x_name = x_name.replace( '.' , ',' );
	
		    for ( Iterator j = x_all_links.iterator() ; j.hasNext() ; ) {
			
				Link x_link = (Link)j.next();
				
				if ( x_name.equals( x_link.getName() ) ) {
				    
				    x_imp_scen.addLink( x_link.getFrom() , x_link.getTo() , x_name , x_ti );
				    x_ti++;
				    break;
				}
		    }
		}
	
		return x_imp_scen;
    }
    
    private int classifyScenario() {
	
		Object[] options = {"Positive", "Negative" , "Ignore" };
		int n = JOptionPane.showOptionDialog( null, "Classify this scenario as positive or negative - or ignore it?",
						     "Implied Scenario",
						     JOptionPane.YES_NO_CANCEL_OPTION,
						     JOptionPane.QUESTION_MESSAGE,
						     null,     //don't use a custom Icon
						     options,  //the titles of buttons
						     options[0]); //default button title
		if ( n == 0 ) { return 1; }
		else if ( n == 1 ) { return -1; }
		else { return 0; }
    }    

    private Map buildMenuItems() {

		Map x_map = new HashMap();
	
		JMenuItem x_item = new JCheckBoxMenuItem( "MSC Editor: Large display" );
		x_item.addActionListener( new ActionListener() {
			
			public void actionPerformed( ActionEvent p_e ) { 
			    o_gui.setBigFont( ((JCheckBoxMenuItem)p_e.getSource()).isSelected() ); 
			}
		    });
	
		x_map.put( x_item , "Options" );
	
		x_item = new JCheckBoxMenuItem( "MSC Editor: Show toolbar" );
		x_item.setSelected( true );
		x_item.addActionListener( new ActionListener() {
	
			public void actionPerformed( ActionEvent p_e ) {
			    o_gui.showToolBar( ((JCheckBoxMenuItem)p_e.getSource()).isSelected() ); }
		    });
	
		x_map.put( x_item , "Options" );
	
//		JMenuItem x_testitem = new JMenuItem( "test" );
//		x_item.addActionListener( new ActionListener() {
//			
//			public void actionPerformed( ActionEvent p_e ) { 
//			    getLTSA().analyseCurrentStateNoDeadlockCheck();
//			}
//		    });
//	
//		x_map.put( x_testitem , "Options" );
	
		return x_map;
    }

    public String getFSPforComponent( String p_name , Map p_mapping ) {

		return o_msccompiler.getFSPforComponent( p_name , o_gui.getSpecification() , getLTSA() , p_mapping );
    }

    public String getSpecAsXML() {

		return new org.jdom.output.XMLOutputter().outputString( o_gui.getSpecification().asXML() );
    }

    public Set getMessageLabels( String p_component ) {

		return o_msccompiler.getMessageLabels( p_component , o_gui.getSpecification() , getLTSA() );
    }

    /**
     * Implementation (empty) of the lts.EventClient interface 
     **/
    public void ltsAction( ic.doc.ltsa.common.infra.LTSEvent p_event ) {

		try {
	
		if ( p_event.name != null ) {
	
		    if ( o_trace == null ) { newTrace(); }
	
		    List x_all_links = o_gui.getSpecification().getAllLinks();
		    
		    String x_name = p_event.name;
	
		    //rbc - 28/12/03
		    //the following fix works for tracing web animations, but not sure it will work in
		    //all cases, particularly with Darwin stuff that has lots of dots...
	
		    String x_dots2commas = x_name.replace( '.' , ',' );
	
		    for ( Iterator j = x_all_links.iterator() ; j.hasNext() ; ) {
			
				Link x_link = (Link)j.next();
		
				if ( x_dots2commas.equals( x_link.getName() ) ) {
				    
				    o_trace.addLink( x_link.getFrom() , x_link.getTo() , x_link.getName() , o_trace_count );
				    if ( x_link.isSelfTransition() ) {
						o_trace_count += 3;
				    } else { 
						o_trace_count += 1;
				    }
				    o_gui.rebuildTab( "Trace" );
				    break;
				}
			}
	
		} else if ( p_event.kind == ic.doc.ltsa.common.infra.LTSEvent.NEWSTATE ) {
	
		    newTrace();
		}
		
		} catch ( Exception e ) { e.printStackTrace(); }
	}

    private void newTrace() {

		o_trace = new BMSC("Trace");
		o_trace_count = 1;
	
		//initialise new scenario with all current instances.
		for ( Iterator i = o_gui.getInstanceAlphabet().iterator() ; i.hasNext() ; ) {
		    o_trace.addInstance( (String)i.next() );
		}
	
		o_gui.deleteBMSC( "Trace" );
		o_gui.addBMSC( o_trace );
    }
    
//    public void pluginAdded( CoreMediator p_core ) { 
//        System.out.println("****************** connecting core");
//        setLTSA(p_core.getCore()); 
//    }
}   