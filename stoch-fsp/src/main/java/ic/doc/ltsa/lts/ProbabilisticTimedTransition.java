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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import ic.doc.simulation.sim.*;

/**
 * Represents transitions with added clock conditions and clock
 * setting actions and probability values.
 *
 * @author Jonas Wolf, original code Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:29 $
 */
public class ProbabilisticTimedTransition extends Transition {
	
	/** list of {@link ClockConditionSpec} objects */
	private Collection conditions;

	/** list of (@link ClockActionSpec} objects */
	private Collection actions;

	/** Probability value */
	double prob;

	/** Creates a new extended transition between the states
	 * specified, with the given action and without any clock setting
	 * or clock condition specifications.
	 * @param from The number of the state this transits from.
	 * @param event The event this transits with.
	 * @param to The number of the state this transits to.
	 */
	public ProbabilisticTimedTransition( int from, Symbol event, int to, double prob ) {
		this( from, event, to, null, null, null, null, prob );
	}

	/** Creates a new extended transition between the states
	 * specified, with the given action, clock setting and clock
	 * condition specifications. The clock setting and condition
	 * specifications are instantiated in the context of the local and
	 * global variables given.
	 * @param from The number of the state this transits from.
	 * @param event The event this transits with.
	 * @param to The number of the state this transits to.
	 * @param conditions A collection of {@link ClockConditionSpec}.
	 * @param actions A collection of {@link ClockActionSpec}.
	 * @param locals A map from {@link String} variable names to their
	 * {@link Value}.
	 * @param globals A map from {@link String} variable names to their
	 * {@link Value}.
	 */
	public ProbabilisticTimedTransition( int from, Symbol event, int to, Collection conditions, Collection actions, Hashtable locals, Hashtable globals, double prob ) {
		super( from, event, to );
		this.prob = prob;
		instantiateClocks( conditions, actions, locals, globals );
	}

	public ProbabilisticTimedTransition( int from, Symbol event, int to, Collection conditions, Collection actions, Hashtable locals, Hashtable globals) {
		super( from, event, to );
		this.prob = 0;
		instantiateClocks( conditions, actions, locals, globals );
	}

	public void addToMachine( CompactState c, StateMachine s ) {
		c.getStates()[from] = EventState.add( c.getStates()[from], makeEventState( s ) );
	}

	private EventState makeEventState( StateMachine s ) {
		return new EventState( s.getEventNumber( event ), to, makeCondition( s ), makeAction( s ), prob);
	}

	/** returns an {@link Action} object corresponding to the
	 * action(s) specified by this transition. Clock numbers are
	 * obtained from the {@link StateMachine} context.
	 * @param s The state machine to use as a context for clock numbers.
	 */
	private Action makeAction( StateMachine s ) {
		Action act = null;
		for( Iterator i=actions.iterator(); i.hasNext(); ) {
			ClockActionSpec spec = (ClockActionSpec) i.next();
			act = CompositeAction.add( act, spec.makeAction(s) );
		}
		return act;
	}

	/** returns a {@link Condition} object corresponding to the
	 * condition(s) placed on this transition.
	 * @param s The state machine to use as a context for clock numbers.
	 */
	private Condition makeCondition( StateMachine s ) {
		Condition cond = null;
		Iterator i;
		i = conditions.iterator();
		while( i.hasNext() ) {
			ClockConditionSpec spec = (ClockConditionSpec) i.next();
			cond = CompositeCondition.add( cond, new ClockCondition(s.getClockNumber(spec.getClockName()), !spec.isNegated() ) );
		}
		return cond;
	}


	/** instantiates clock distributions by evaluating the expressions.
	 */
	public void instantiateClocks( Collection conds,Collection acts,Hashtable locals,Hashtable globals ) {
		if( conds != null )
			conditions = conds;
		else
			conditions = new Vector();
		if( acts != null )
			actions = acts;
		else
			actions = new Vector();
		for( Iterator i=actions.iterator(); i.hasNext(); ) {
			((ClockActionSpec) i.next()).instantiate( locals, globals );
		}
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		if (prob>0) s.append( "(" + prob + ")" );
		if( conditions != null && conditions.size() > 0 ) {
			Iterator i = conditions.iterator();
			s.append( "?" );
			while( i.hasNext() ) {
				s.append( i.next().toString() );
				if( i.hasNext() ) s.append( "," );
			}
			s.append( "? " );
		}
		s.append( from );
		s.append( " " );
		s.append( event.toString() );
		s.append( " " );
		s.append( to );
		if( actions != null && actions.size() > 0 ) {
			Iterator i = actions.iterator();
			s.append( " <" );
			while( i.hasNext() ) {
				s.append( i.next().toString() );
				if( i.hasNext() ) s.append( "," );
			}
			s.append( ">" );
		}
		return s.toString();
	}
	
	public boolean isProbabilisticTransition() {
		return prob>0;
	}

	public boolean isTimedTransition() {
		return (conditions!=null && conditions.size() > 0)
		    || (actions!=null && actions.size() > 0);
	}
	
}
