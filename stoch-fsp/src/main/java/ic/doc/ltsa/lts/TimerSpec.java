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

import java.util.Map;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import ic.doc.ltsa.common.iface.IActionLabels;
import ic.doc.ltsa.common.iface.IEventState;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.simulation.sim.*;

/**
 * Instances of this class specify a timer measure in a
 * model. <code>TimerSpec</code> objects are created at parse time and
 * later compiled into measurement processes. Each timer will have one
 * measurement process, which has only one state. Transitions can
 * occur from this state to this state, executing either start actions
 * or stop actions depending on the event causing the transition.
 *
 * When a start action occurs, the current simulation time is placed
 * in a FIFO queue. When a stop action occurs, a time is taken form
 * the front of this queue. The difference between these times is then
 * inserted into the measure. Additionally, multiple queues of such
 * times can be kept. This allows the times for distinct entities
 * (such as different customers identified by a parameterised action)
 * to be stored, allowing all queueing disciplines to be recorded
 * correctly at higher moments of the measure.
 *
 * The measurement process can be obtained through the {@link
 * Compilable} interface.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 * @see CompactState
 * @see Timer
 * @see TimerAction 
 */
public class TimerSpec
    extends MeasureSpec {

    /** A collection of {@link TimerSpec.StartStopPair} describing the
     * pairs of start and stop actions for each queue to be
     * maintained.
     */
    private Collection startStopPairs;

    private static class StartStopPair {
        StartStopPair(IActionLabels start, IActionLabels stop) {

            this.start = start;
            this.stop = stop;
        }

        StartStopPair(IActionLabels start, IActionLabels stop, IActionLabels range) {

            this.start = start;
            this.stop = stop;
            this.range = range;
        }

        IActionLabels start, stop, range;
    }

    /**
     * Creates a new timer specification with the given name.
     * @param name The name of the timer measure.
     */
    public TimerSpec( String name ) {
	super( name, Timer.class );
	startStopPairs = new Vector();
    }

    protected void configure( CompactState c, LTSOutput output, boolean probabilisticSystem ) {
	Map alpha = new Hashtable();

	doAlpha( c, alpha );
	doTransitions( c, alpha, probabilisticSystem );
	crunchNonDet( c );
    }

    private void doAlpha( CompactState c, Map alpha ) {
	int index = 0;

	alpha.put( "tau", new Integer( index++ ) );

	for( Iterator pairs=startStopPairs.iterator(); pairs.hasNext(); ) {
	    StartStopPair pair = (StartStopPair) pairs.next();
	    
	    if( pair.range != null ) {
		Hashtable locals = new Hashtable();
		pair.range.initContext( locals, null );

		while( pair.range.hasMoreNames() ) {
		    pair.range.nextName();
		    index = addPairAlpha( pair.start, pair.stop,
					  locals, index, alpha );
		}
		pair.range.clearContext();
	    } else {
		index = addPairAlpha(pair.start,pair.stop,null,index,alpha);
	    }
	}

	c.alphabet = new String[index];
	for( Iterator i = alpha.keySet().iterator(); i.hasNext(); ) {
	    String name = (String) i.next();
	    c.alphabet[ ((Integer) alpha.get(name)).intValue() ] = name;
	}
    }

    private int addPairAlpha( IActionLabels start, IActionLabels stop,
			      Hashtable locals, int index, Map alpha ) {
	start.initContext( locals, null );
	stop.initContext( locals, null );
	
	while( start.hasMoreNames() ) {
	    String name = start.nextName();
	    if( !alpha.containsKey( name ) ) {
		alpha.put( name, new Integer( index++ ) );
	    }
	}
	
	while( stop.hasMoreNames() ) {
	    String name = stop.nextName();
	    if( !alpha.containsKey( name ) ) {
		alpha.put( name, new Integer( index++ ) );
	    }
	}
	
	start.clearContext();
	stop.clearContext();

	return index;
    }

    private void doTransitions( CompactState c, Map alpha, boolean probabilisticSystem ) {
		int queueNum = 0;
		for( Iterator pairs = startStopPairs.iterator(); pairs.hasNext(); ) {
		    StartStopPair pair = (StartStopPair) pairs.next();
		    if( pair.range != null ) {
				Hashtable locals = new Hashtable();
				pair.range.initContext( locals, null );
				while( pair.range.hasMoreNames() ) {
				    pair.range.nextName();
				    addPairTrans( c, pair.start, pair.stop, locals, queueNum++, alpha, probabilisticSystem );
				}
				pair.range.clearContext();
		    } else {
				addPairTrans( c, pair.start, pair.stop, null, queueNum++, alpha, probabilisticSystem);
		    }
		}
		// normalise probabilities
		if (probabilisticSystem) EventState.normalise(c.getStates()[0]);
    }

    private void addPairTrans( CompactState c,
			       IActionLabels start, IActionLabels stop,
			       Hashtable locals, int queue, Map alpha, boolean probabilisticSystem ) {
		Action startAction, stopAction;
		    
		Set startEvents = new HashSet();
		Set stopEvents = new HashSet();
		    
		// initialise action vectors
		// the measure identifier is 0
		startAction = new TimerAction( 0, TimerAction.START, queue );
		stopAction = new TimerAction( 0, TimerAction.STOP, queue );
		    
		start.initContext( locals, null );
		stop.initContext( locals, null );
	
		// load up start events into set
		while( start.hasMoreNames() )
		    startEvents.add( (Integer) alpha.get(start.nextName()) );
		    
		// load up stop events into set
		while( stop.hasMoreNames() )
		    stopEvents.add( (Integer) alpha.get(stop.nextName()) );
		    
		// now, for all events, add a transition
		for( Iterator i = alpha.values().iterator(); i.hasNext(); ) {
		    Integer event = (Integer) i.next(); //event number
	
		    if( startEvents.contains( event ) )
			c.getStates()[0] = EventState.add( c.getStates()[0],
						      new EventState( event.intValue(),0,null,startAction,probabilisticSystem ? 1 : 0 ) );
		    if( stopEvents.contains( event ) )
			c.getStates()[0] = EventState.add( c.getStates()[0],
						      new EventState( event.intValue(),0,null,stopAction,probabilisticSystem ? 1 : 0 ) );
		}
		    
		start.clearContext();
		stop.clearContext();
    }

    /** Ensures that no non-deterministic states exist in the process
     * by combining all transitions with a common event into a single
     * transition containing the union of all actions, ordered so that
     * stop actions occur before start actions.
     */
    private void crunchNonDet( final CompactState c ) {
	IEventState old = c.getStates()[0];
	IEventState previous = null;

	while( old != null ) {
	    EventState tr = crunchNonDet( old );
	    if( previous == null ) c.getStates()[0] = tr;
	    else previous.setList( tr );
	    previous = tr;
	    old = old.getList();
	}
    }

    private EventState crunchNonDet(final IEventState head) {

        IEventState tr = head;
        // never any conditions in measurement process
        Action act = null;

        while (tr != null) {
            act = CompositeAction.add(act, ((EventState) tr).getAction());
            tr = tr.getNondet();
        }

        act = reorderActions(act);

        return new EventState(head.getEvent(), head.getNext(), null, act, head.getProb());
    }

    private Action reorderActions( Action act ) {
	if( !(act instanceof CompositeAction) ) return act;
	else {
	    CompositeAction acts = (CompositeAction) act;
	    Collection start = new Vector(), stop = new Vector();
	    for( Iterator i=acts.iterator(); i.hasNext(); ) {
		TimerAction ta = (TimerAction) i.next();
		if( ta.getType() == TimerAction.START ) start.add( ta );
		else stop.add( ta );
	    }
	    Action r = null;
	    for( Iterator i=stop.iterator(); i.hasNext(); )
		r = CompositeAction.add( r, (Action) i.next() );
	    for( Iterator i=start.iterator(); i.hasNext(); )
		r = CompositeAction.add( r, (Action) i.next() );
	    
	    return r;
	}
    }

    /**
     * Adds a pair of action sets to act as the start and stop actions
     * for a queue. If this method is called multiple times, then a
     * set of such pairs is created, with each pair using its own
     * queue to store samples (as described in the class summary).
     * @param start Action labels that will start a timer.
     * @param stop Action labels that will stop a timer.
     */
    public void addStartStopPair(IActionLabels start, IActionLabels stop) {

        startStopPairs.add(new StartStopPair(start, stop));
    }

    /**
     * Adds a range of pairs of action sets to act as a group of start
     * and stop actions for multiple queues. When instantiating the
     * state machine corresponding to this specification, a pair of
     * start and stop actions will be added for each value of the
     * range given, with each pairing placing samples in a different
     * queue.
     *
     * For example, the syntax
     * <code>timer FOO { forall[i:1..10] <in[i],out[i]> }</code> will
     * produce a set of pairs <code>in[0],out[0]</code>,
     * <code>in[1],out[1]</code>...<code>in[10],out[10]</code>, each
     * of which will add and remove samples from its own queue. This
     * application could be used to correctly track the time spent in
     * part of a system by customers identified by the actions
     * <code>in[x], out[x]</code>.
     *
     * @param start The start action labels.
     * @param stop The stop action labels.
     * @param range The range action labels.
     */
    public void addStartStopRange(IActionLabels start, IActionLabels stop, IActionLabels range) {

        startStopPairs.add(new StartStopPair(start, stop, range));
    }
}
