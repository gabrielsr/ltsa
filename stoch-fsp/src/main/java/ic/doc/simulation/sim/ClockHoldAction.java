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
 * An action that causes a clock to be held in the simulation
 * state. When a clock is held, its value is not reduced when clocks
 * are advanced.
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 * @see SimulationState#holdClock(int)
 * @see ClockResumeAction
 */
public class ClockHoldAction
    extends ClockAction {

    /**
     * Creates an action that will hold the given clock.
     * @param clock The identifier of the clock to hold.
     */
    public ClockHoldAction( int clock ) {
	super( clock );
    }

    public void execute( SimulationState state ) {
	state.holdClock( getClockIdentifier() );
    }

    public String prettyPrint( StochasticAutomata m ) { return toString(); }
    public String toString() { return getClockIdentifier()+":hold"; }

}
