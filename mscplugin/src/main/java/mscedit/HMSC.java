package mscedit;

import org.jdom.*;

import java.util.*;

public class HMSC {
	
	private Element o_root;
	
	public HMSC() {
		
		o_root = new Element("hmsc");
	}
	
	public HMSC( Element p_root ) {
		
		o_root = p_root;
	}
	
	public HMSC addTransition( String p_from , String p_to ) {
		
		Element x_transition = new Element("transition");
		x_transition.addContent( new Element("from").setText( p_from ) );
		x_transition.addContent( new Element("to").setText( p_to ) );
		
		o_root.addContent( x_transition );
		return this;	  
	}
	
	public List getTransitions() {
		
		List x_transitions = new ArrayList();
		List x_elems = o_root.getChildren("transition");
		
		for ( Iterator i = x_elems.iterator() ; i.hasNext() ; ) {
			
			Element x_elem = (Element)i.next(); 
			
			if ( isWeighted() ) {
				
				x_transitions.add( new ProbabilisticTransition( x_elem ) );
				
			} else {
			
				x_transitions.add( new Transition( x_elem ) );
			}
		}
		
		return x_transitions;
	}
	
	
	public HMSC deleteTransition( String p_from , String p_to ) {
		
		List x_transitions =  o_root.getChildren("transition");
		
		for ( Iterator i = x_transitions.iterator() ; i.hasNext() ; ) {
			
			Transition x_trans = new Transition( (Element)i.next() );
			if ( x_trans.getFrom().equals( p_from ) &&
					x_trans.getTo().equals( p_to ) ) { 
				i.remove();
				break;
			}
		}
		
		return this;
	}
	
	public HMSC setTransitionWeight( String p_from , String p_to , String p_weight ) {
		
		List x_transitions = getTransitions();
		
		Transition x_trans;
		
		for ( Iterator i = x_transitions.iterator() ; i.hasNext() ; ) {
			
			x_trans = (Transition)i.next();
			if ( x_trans.getFrom().equals( p_from ) && x_trans.getTo().equals( p_to ) ) { 
			
				x_trans.setWeight( p_weight ); 
			}
		}
		
		o_root.setAttribute( "weighted" , "true" );
			
		return this;
	}
	
	public Element asXML() {
		
		return o_root;
	}
	
	public void apply( Visitor v ) {
		
		v.caseAHMSC( this );
	}
	
	public void reverseTransition( String p_from , String p_to ) {
		
		List x_transitions = getTransitions();
		
		for ( Iterator i = x_transitions.iterator() ; i.hasNext() ; ) {
			
			Transition x_trans = (Transition)i.next();
			if ( x_trans.getFrom().equals( p_from ) &&
					x_trans.getTo().equals( p_to ) ) {
				
				x_trans.setFrom( p_to );
				x_trans.setTo( p_from );
				break;
			}
		}
	}
	
	public HMSC addBMSC( String p_name , int p_x , int p_y ) {
		
		Element x_bmsc = new Element( "bmsc" );
		x_bmsc.setAttribute( "name" , p_name );
		x_bmsc.setAttribute( "x" , String.valueOf( p_x ) );
		x_bmsc.setAttribute( "y" , String.valueOf( p_y ) );
		o_root.addContent( x_bmsc );
		return this;
	}
	
	public List getBMSCs() {
		
		return o_root.getChildren( "bmsc" );
	}
	
	public void moveBMSC( String p_name , int p_x , int p_y ) {
		
		List x_bmscs = getBMSCs();
		for ( Iterator i = x_bmscs.iterator() ; i.hasNext() ; ) {
			
			Element x_bmsc = (Element)i.next();
			
			if ( x_bmsc.getAttribute("name").getValue().equals( p_name ) ) {
				
				x_bmsc.setAttribute( "x" , String.valueOf( p_x ) );
				x_bmsc.setAttribute( "y" , String.valueOf( p_y ) );
				break;
			}
		}
	}
	
	public void renameBMSC( String p_oldname , String p_newname ) {
		
		List x_bmscs = getBMSCs();
		for ( Iterator i = x_bmscs.iterator() ; i.hasNext() ; ) {
			
			Element x_bmsc = (Element)i.next();
			
			if ( x_bmsc.getAttribute("name").getValue().equals( p_oldname ) ) {
				
				x_bmsc.setAttribute( "name" , p_newname );
				break;
			}
		}
		
		List x_transitions = getTransitions();
		
		for ( Iterator i = x_transitions.iterator() ; i.hasNext() ; ) {
			
			Transition x_trans = (Transition)i.next();
			if ( x_trans.getFrom().equals( p_oldname ) ) {
				x_trans.setFrom( p_newname );
			}
			
			if ( x_trans.getTo().equals( p_oldname ) ) {
				x_trans.setTo( p_newname );
			}
		}
		
	}
	
	public void deleteBMSC( String p_name ) {
		
		if ( p_name.equals( "init" ) ) { return; }
		
		//remove the BMSC
		
		List x_bmscs = getBMSCs();
		for ( Iterator i = x_bmscs.iterator() ; i.hasNext() ; ) {
			
			Element x_bmsc = (Element)i.next();
			
			if ( x_bmsc.getAttribute("name").getValue().equals( p_name ) ) {
				
				i.remove();
			}
		}
		
		//remove all transitions to or from that BMSC
		
		List x_transitions =  o_root.getChildren("transition");
		
		for ( Iterator i = x_transitions.iterator() ; i.hasNext() ; ) {
			
			Transition x_trans = new Transition( (Element)i.next() );
			if ( x_trans.getFrom().equals( p_name ) ||
					x_trans.getTo().equals( p_name ) ) { 
				i.remove();
			}
		}
		
	}

	public boolean isWeighted() {
		
		Attribute x_weighted = o_root.getAttribute("weighted");
		
		try {
			return x_weighted != null && x_weighted.getBooleanValue();
		} catch (DataConversionException e) {
			return false;
		}		
	}
}
