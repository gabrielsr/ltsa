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

package ic.doc.simulation.sim;

import ic.doc.ltsa.lts.StochasticAutomata;
import ic.doc.ltsa.lts.ProbabilisticTimedTransitionList;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class IntegrityCheck {

    private final StochasticAutomata aut;
    private final Map violations;

    public IntegrityCheck( StochasticAutomata aut ) {
	this.aut = aut;
	this.violations = new Hashtable();
	doCheck();
    }

    public boolean violationFound() {
	return violations.size() > 0;
    }

    public Map getViolations() {
	return violations;
    }

    private void doCheck() {
	// get transitions from initial state
	ProbabilisticTimedTransitionList ls = aut.getProbTimedTransitions( aut.START() );

	// check integrity
	while( !ls.isEmpty() ) {
	    checkTransition( ls );
	    ls.next();
	}
    }

    private void checkTransition( ProbabilisticTimedTransitionList tr ) {
	if( equals( tr.getTo(), aut.START() ) ) {
	    if( tr.getCondition() == null ) {
		// no conditions on transition - can be executed immediately
		addViolations( tr.getAction(), tr.getActionObject() );
	    }
	}
    }

    private void addViolations( int event, Action a ) {
	if( a instanceof MeasureAction ) {
	    String measureName = aut.getMeasureNames()
		[((MeasureAction)a).getIdentifier()];
	    String eventName = aut.getAlphabet()[event];
	    violations.put( measureName, eventName );
	} else if( a instanceof CompositeAction ) {
	    for( Iterator i=((CompositeAction) a).iterator(); i.hasNext(); ) {
		addViolations( event, (Action) i.next() );
	    }
	}
    }

    private boolean equals( byte[] x, byte[] y ) {
	// deal with cases of nulls
	if( x==null && y==null ) return true;
	if( x==null || x==null ) return false;

	// neither null, compare
	if( x.length != y.length ) return false;
	for( int i=0; i<x.length; i++ ) {
	    if( x[i] != y[i] ) return false;
	}

	// equal
	return true;
    }
}
