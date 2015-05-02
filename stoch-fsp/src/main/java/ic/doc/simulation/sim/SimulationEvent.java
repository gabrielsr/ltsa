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

import ic.doc.simulation.tools.*;

import ic.doc.ltsa.lts.StochasticAutomata;
import ic.doc.ltsa.lts.ProbabilisticTimedTransitionList;
import java.util.Collection;
import java.util.Iterator;


/** Represents a single event in a simulation. These events are
 * scheduled at times when the conditions for transitions out of a
 * state have been met. All transitions that can be performed at that
 * time are performed (with respect to the semantics of the system),
 * and then a further event is scheduled for the next point on the
 * time line. Should a problem occur during the processing of a
 * simulation event, a {@link SimulationException} will be thrown by
 * the {@link #call()} method.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
class SimulationEvent
    extends Event {

    private SimulationState simState;
    private StochasticAutomata automata;
    private final long zenoThreshold;

    /** Creates a new simulation event in the given simulation context
     * to be executed at the given time with respect to a certain Zeno
     * threshold. The Zeno threshold is the maximum number of
     * transitions that can be performed without time progressing
     * before throwing an exception flagging the lack of progress of
     * the simulation.
     * @param s The simulation context.
     * @param t The time to execute the event.
     * @param p The stochastic automata to simulate.
     * @param state The simulation state to affect.
     * @param zt The Zeno threshold, as maximum number of transitions
     * without time passing.
     */
    public SimulationEvent( Sim s, double t, StochasticAutomata p, SimulationState state, long zt ) {
		super( s, t );
		simState = state;
		automata = p;
		zenoThreshold = zt;
    }
	
    public void call() {
		double nextEvent = doSimulationCycle();
		if( nextEvent == Double.POSITIVE_INFINITY ) {
		    throw new SimulationException( SimulationException.STALLED );
		} else {
		    new SimulationEvent( getSim(), nextEvent, automata, simState, zenoThreshold );
		    simState.advanceClocks( nextEvent );
		}
    }

    private double doSimulationCycle() {
		// do as many transitions as possible
		long transitions = 0;
		while( doTransition() && transitions <= zenoThreshold )
		    transitions++;
		if( transitions > zenoThreshold )
		    throw new SimulationException( SimulationException.ZENO );
		
		// return time to next transition
		return getTimeToNextTransition();
    }
    
    /** does one transition if possible.
     * @return true if a transition was performed */
    private boolean doTransition() {
		boolean transitOccurred = false;
	
/*
		if( automata.isProbabilisticState( simState.getState() ) ) {
		    ProbabilisticTimedTransitionList ls
			= automata.getProbabilisticTransitions( simState.getState() );
		    if( ls.size() == 0 )
			throw new SimulationException( SimulationException.TERMINAL_STATE );
		    ls.normalise();
	
		    double sum = ls.getProbability();
		    double rand = Math.random();
		    while( rand > sum ) {
			ls.next();
			sum += ls.getProbability();
		    }
	
		    simState.setState( ls.getTo() );
		    transitOccurred = true;
		} else {
		    ProbabilisticTimedTransitionList ls
			= automata.getTimedTransitions( simState.getState() );
		    if( ls.size() == 0 )
			throw new SimulationException( SimulationException.TERMINAL_STATE );
		    ls = getAvailableTransitions( ls );
		    if( ls.size() > 0 ) {
			final double step = 1.0 / ls.size();
			double sum = step;
			double rand = Math.random();
	
			while( sum < rand ) {
			    ls.next();
			    sum += step;
			}
	
			Action action = ls.getActionObject();
			if( action != null ) action.execute( simState );
			simState.setState( ls.getTo() );
			transitOccurred = true;
		    }
		}
*/
		ProbabilisticTimedTransitionList ls	= automata.getProbTimedTransitions( simState.getState() );
		if( ls.size() == 0 )
			throw new SimulationException( SimulationException.TERMINAL_STATE );

		// throw away unreachable transitions and then normalise for remaining
		ls = getAvailableTransitions( ls );
		if (ls.size()>0) {
			boolean probabilistic = ls.getProbability()>0;
			if (probabilistic) ls.normalise();
			double step = 1.0/ls.size();

			// TODO: Is this fair on all transitions ?? They are always in the same order...
			// maybe the transitions should be shuffled before this ??

			double sum = probabilistic ? ls.getProbability() : step;
			double rand = Math.random();
			while( rand > sum ) {
				ls.next();
				sum += probabilistic ? ls.getProbability() : step;
			}
		
			Action action = ls.getActionObject();
			if( action != null ) action.execute( simState );
			simState.setState( ls.getTo() );
			transitOccurred = true;
		}
		return transitOccurred;
    }


    /** returns the amount of time until the next transition can
     * occur.
     * @return The time until the next transition, or positive
     * infinity if no further transitions can be made.
     */
    private double getTimeToNextTransition() {
		ProbabilisticTimedTransitionList transitions;
		double next = Double.POSITIVE_INFINITY;
	
		transitions = automata.getProbTimedTransitions( simState.getState() );
		while( !transitions.isEmpty() ) {
		    double t = 0;
		    if( transitions.getCondition() != null )
			t = transitions.getCondition().timeUntilTrue( simState );
		    next = (t < next)? t : next;
		    transitions.next();
		}
	
		return next;
    }

    /** returns the time interval until all of the conditions in the
     * given collection are met.
     * @param c a collection of {@link Condition} objects
     * @return The time until all conditions are met, positive
     * infinity if this set can never be met */
    private double getTimeToConditionsMet( Collection c ) {
		Iterator i;
		double max = 0;
	
		i = c.iterator();
		while( i.hasNext() ) {
		    Condition cond = (Condition) i.next();
		    double t = cond.timeUntilTrue( simState );
		    max = t > max ? t : max;
		}
	
		return max;
    }

    /** filters a transition list so that only those transitions whose
	guards are true with respect to the simulation state remain. */
    private ProbabilisticTimedTransitionList getAvailableTransitions( ProbabilisticTimedTransitionList l ) {
		ProbabilisticTimedTransitionList r = new ProbabilisticTimedTransitionList();
		
		while( !l.isEmpty() ) {
		    if( l.getCondition() == null || l.getCondition().evaluate( simState ))
				r.add( l.getFrom(), l.getTo(), l.getAction(), l.getCondition(), l.getActionObject(), l.getProbability() );
		    l.next();
		}
		
		return r;
    }
}

