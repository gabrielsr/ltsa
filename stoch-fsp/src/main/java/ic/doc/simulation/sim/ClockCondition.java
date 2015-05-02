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

/**
 * This class represents clock conditions that may guard
 * transitions. The condition refers to a particular clock and
 * sense. The sense of the condition determines whether the clock must
 * have expired or not - if the sense is true, the clock must have
 * expired.
 * 
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class ClockCondition
    implements Condition, Cloneable {
    
    private int clock;
    private boolean sense;
    
    /**
     * Creates a new clock condition referring to the specified clock
     * in the given sense. The sense determines whether the clock must
     * have or must have not expired in order for the condition to be
     * met.
     * @param clock The identifier of the clock that this condition
     * refers to.
     * @param sense The sense of the condition - if <code>true</code>
     * the clock must have expired (have a value less than zero) for
     * the condition to be met.
     */
    public ClockCondition( int clock, boolean sense ) {
	this.clock = clock;
	this.sense = sense;
    }

    public boolean evaluate( SimulationState s ) {
	return sense ? s.getClock( clock ) <= 0
	    : s.getClock( clock ) > 0;
    }

    public double timeUntilTrue( SimulationState s ) {
	if( sense )
	    return s.clockRunning(clock) ?
		s.getClock( clock ) :
		Double.POSITIVE_INFINITY;
	else
	    return s.getClock( clock ) > 0 ?
		0 :
		Double.POSITIVE_INFINITY;
    }

    public void addClockOffset( int offset ) {
	clock += offset;
    }

    public int getMaxClockIdentifier() { return clock; }

    /**
     * Returns the identifier of the clock that this condition refers to.
     * @return The identifier of the clock this condition refers to.
     */
    public int getClockIdentifier() {
	return clock;
    }

    public String toString() {
	return ( sense ? "" : "!" ) + clock;
    }

    public boolean equals( Object o ) {
	try {
	    ClockCondition c = (ClockCondition) o;
	    return c.getClass().equals( this.getClass() )
		&& c.clock == this.clock
		&& c.sense == this.sense;
	} catch( ClassCastException e ) {
	    return false;
	}
    }

    public int hashCode() {
	return getClass().hashCode() ^ clock ^ (sense?0xFF:0x00);
    }

    public Condition cloneCondition() {
	try {
	    return (Condition) super.clone();
	} catch( CloneNotSupportedException e ) {
	    throw new RuntimeException( e.getMessage() );
	}
    }

    public String prettyPrint( StochasticAutomata m ) {
	return (sense?"":"!") + clock;
    }
}
