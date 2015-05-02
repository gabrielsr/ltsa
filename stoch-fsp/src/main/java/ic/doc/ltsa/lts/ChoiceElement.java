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

import ic.doc.ltsa.common.iface.IActionLabels;

import java.util.*;

/** encapsulates one possible choice of a process (p) ?c? a <c:dist(E)> -> P 
 * */

class ChoiceElement extends Declaration {
    Stack guard;
    IActionLabels action;
    StateExpr stateExpr;
    
	/** Probability for this transition, null if deterministic */
	Stack prob;
	/** Clock conditions guarding this transition */
	Collection clockConditions;
	/** Clock setting actions associated with this transition */
	Collection clockActions;

/*
	private void add( int from, Hashtable locals, StateMachine m, ActionLabels action ) {
		add( from, locals, m, action, null );
	}
*/

	/** Adds this transition to the state machine. A transition will
	 * be added for each label in the set for this element. Adds this
	 * transition's action labels to the state machine's alphabet.
	 * @param from 'From' state in m
	 * @param locals locals
	 * @param m state machine to modify
	 * @param s The action label
	 */
	private void add( int from, Hashtable locals, StateMachine m, IActionLabels action, Stack probExpr,boolean probabilisticSystem ) {
		action.initContext(locals, m.constants);
		while (action.hasMoreNames()) {
			String s = action.nextName();
			Symbol e = new Symbol(Symbol.IDENTIFIER, s);
			if(!m.alphabet.containsKey(s))
				m.alphabet.put(s,m.eventLabel.label());
			stateExpr.endTransition( from, e, locals, m, clockConditions, clockActions, probExpr, probabilisticSystem );
		}
		action.clearContext();
	}

    private void add(int from, Hashtable locals, StateMachine m, String s,boolean probabilisticSystem) {
        Symbol e = new Symbol(Symbol.IDENTIFIER, s);
        if(!m.alphabet.containsKey(s))
            m.alphabet.put(s,m.eventLabel.label());
        stateExpr.endTransition(from, e, locals, m, clockConditions, clockActions,probabilisticSystem);
    }

	/** Adds the transition represented by this object to the state
	 * machine given. The addition is conditional on any guard
	 * evaluating to true in the context of the locals given.
	 *
	 * @param from The state this transition goes from in the state
	 * machine
	 * @param locals Local vars and consts, for evaluation
	 * @param m The state machine to add transition to
	 */
	public void addTransition(int from, Hashtable locals, StateMachine m,boolean probabilisticSystem){
		if (guard==null || Expression.evaluate(guard,locals,m.constants)!=0) {
			if (action!=null) {
				add(from,locals,m,action,prob,probabilisticSystem);
			}
		}
	}

	public void addTransition( int from, Hashtable locals, Stack probExpr, StateMachine m,boolean probabilisticSystem ) {
		if( guard==null
		|| Expression.evaluate( guard, locals, m.constants)!= 0 ) {
			if( action != null ) {
				add( from, locals, m, action, probExpr, probabilisticSystem );
			}
		}
	}

    public ChoiceElement myclone() {
    	   ChoiceElement ce = new ChoiceElement();
       ce.guard = guard;
       if (action!=null)
       	 ce.action = ((ActionLabels)action).myclone();
       if (stateExpr!=null) 
       	 ce.stateExpr = stateExpr.myclone();
       return ce;
    }
    
	/** Converts the choice element back into its syntactic form.
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();

		// add probability, if exists
		if (prob != null) s.append("("+prob.toString() +") ");	

		//if( guard != null ) s.append( "when [expr] " );
		if( guard != null ) s.append("when "+guard.toString()+" ");
		if( clockConditions != null && clockConditions.size() > 0 ) {
			s.append( "?" );
			Iterator i = clockConditions.iterator();
			while( i.hasNext() ) {
			s.append( i.next().toString() );
			if( i.hasNext() ) s.append( "," );
			}
			s.append( "? " );
		}
		s.append( action==null?"null":action.toString() );
		if( clockActions != null && clockActions.size() > 0 ) {
			s.append( " <" );
			Iterator i = clockActions.iterator();
			while( i.hasNext() ) {
			s.append( i.next().toString() );
			if( i.hasNext() ) s.append( "," );
			}
			s.append( ">" );
		}
		s.append( " -> " );
		s.append( stateExpr==null?"null":stateExpr.toString() );
	
		return s.toString();
	}
	
	/** associates a probability with this transition */
	public void setProbExpr(Stack expr) {
		prob = expr;
	}

}
