package mscedit;

import java.util.*;

public class Visitor {
	
	public void inASpecification( Specification p_node ) {}
	
	public void caseASpecification( Specification p_node ) {
		
		inASpecification( p_node );
		
		p_node.getHMSC().apply( this );
		
		for ( Iterator i = p_node.getPositiveBMSCs().iterator() ; i.hasNext() ; ) {
			
			((BMSC)i.next()).apply( this );
		}
		
		for ( Iterator i = p_node.getNegativeBMSCs().iterator() ; i.hasNext() ; ) {
			
			((BMSC)i.next()).apply( this );
		}
		
		outASpecification( p_node );
	}
	
	public void outASpecification( Specification p_node ) {}
	
	
	public void inAHMSC( HMSC p_node ) {}
	
	public void caseAHMSC( HMSC p_node ) {
		
		inAHMSC( p_node );
		
		for ( Iterator i = p_node.getTransitions().iterator() ; i.hasNext() ; ) {
			
			((Transition)i.next()).apply( this );
		}
		
		outAHMSC( p_node );
	}
	
	public void outAHMSC( HMSC p_node ) {}
	
	
	public void inABMSC( BMSC p_node ) {}
	
	public void caseABMSC( BMSC p_node ) {
		
		inABMSC( p_node );
		
		for ( Iterator i = p_node.getInstances().iterator() ; i.hasNext() ; ) {
			
			((Instance)i.next()).apply( this );
		}
		
		outABMSC( p_node );
	}
	
	public void outABMSC( BMSC p_node ) {}
	
	
	public void inATransition( Transition p_node ) {}
	
	public void caseATransition( Transition p_node ) {
		
		inATransition( p_node );
		outATransition( p_node );
	}
	
	public void outATransition( Transition p_node ) {}
	
	
	public void inAInstance( Instance p_node ) {}
	
	public void caseAInstance( Instance p_node ) {
		
		inAInstance( p_node );
		
		for ( Iterator i = p_node.getInputsAndOutputs().iterator() ; i.hasNext() ; ) {
			
			((InputOrOutput)i.next()).apply( this );
		}
		
		outAInstance( p_node );
	}
	
	public void outAInstance( Instance p_node ) {}
	
	
	public void inAInput( Input p_node ) {}
	
	public void caseAInput( Input p_node ) {
		
		inAInput( p_node );
		outAInput( p_node );
	}
	
	public void outAInput( Input p_node ) {}
	
	
	public void inAOutput( Output p_node ) {}
	
	public void caseAOutput( Output p_node ) {
		
		inAOutput( p_node );
		outAOutput( p_node );
	}
		
	public void outAOutput( Output p_node ) {}
		
	public void inANegativeBMSC( BMSC p_node ) {}
	
	public void caseANegativeBMSC( BMSC p_node ) {
		
		inANegativeBMSC( p_node );
		
		inABMSC( p_node );
		
		for ( Iterator i = p_node.getInstances().iterator() ; i.hasNext() ; ) {
			
			((Instance)i.next()).apply( this );
		}
		
		outABMSC( p_node );
		
		String x_neg_msg = p_node.getNegativeLink();
		
		outANegativeBMSC( p_node );
	}
	
	public void outANegativeBMSC( BMSC p_node ) {}
	
	public void inADivider( Divider p_node ) {}
	
	public void caseADivider( Divider p_node ) {
		
		inADivider( p_node );
		outADivider( p_node );
	}
	
	public void outADivider( Divider p_node ) {}

	public void caseAProbabilisticTransition(ProbabilisticTransition p_node) {
	
		//System.out.println("*** caseAProbabilisticTransition ***");
		inAProbabilisticTransition( p_node );
		outAProbabilisticTransition( p_node );
	}

	public void outAProbabilisticTransition(ProbabilisticTransition p_node) {}

	public void inAProbabilisticTransition(ProbabilisticTransition p_node) {}	
}

class TestVisitor extends Visitor {
	
	private void p( String x ) { System.out.println( x ); }
	
	public void inAHMSC( HMSC p_node ) { p( "HMSC" ); }
	
	public void inATransition( Transition p_node ) { p( "Transition from " + p_node.getFrom() + " to " + p_node.getTo() ); }
	
	public void inAProbabilisticTransition( ProbabilisticTransition p_node ) { p( "Weighted transition from " + p_node.getFrom() + " to " + p_node.getTo() + "with weight " + p_node.getWeight() ); }
	
	public void inABMSC( BMSC p_node ) { p( "BMSC : " + p_node.getName() ); }
	
	public void inAInstance( Instance p_node ) { p( "Instance : " + p_node.getName() ); }
	
	public void inAInput( Input p_node ) { p( "Input : " + p_node.getName() + " at " + p_node.getTimeIndex() ); }
	
	public void inAOutput( Output p_node ) { p( "Output : " + p_node.getName() + " at " + p_node.getTimeIndex() ); } 
	
	public void inANegativeBMSC( BMSC p_node ) { p( "NegativeBMSC : " + p_node.getName() ); }
	public void outANegativeBMSC( BMSC p_node ) { p( "Negative label : " + p_node.getNegativeLink() ); }
	
}
