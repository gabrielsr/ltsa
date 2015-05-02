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
 * Interface used to represent transition conditions. The interface
 * provides methods to evaluate the condition with respect to a
 * certain {@link SimulationState} and also to return the amount of
 * time until the condition becomes true with respect to a {@link
 * SimulationState}. For reasons of efficiency and encapsulation, a
 * composite design pattern is used to represent multiple conditions
 * on a transition. The class {@link CompositeCondition} is provided
 * for this purpose, and the static method 
 * {@link CompositeCondition#add(Condition,Condition)} can be used to
 * form the composite of two conditions.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 *
 * @see ic.doc.ltsa.lts.EventState
 * @see ic.doc.ltsa.lts.TimedTransitionList
 */
public interface Condition {
    /**
     * Evaluates the condition with respect to the simulation state
     * given.
     * @param s The simulation state to use.
     * @return true if the condition is currently met, false otherwise.
     */
    public boolean evaluate( SimulationState s );

    /**
     * Returns the time until the condition becomes true with respect
     * to the given simulation state.
     * @param s The simulation state to use.
     * @return The time until the condition becomes true, or positive
     * infinity if the condition will never become true with the
     * current state.
     */
    public double timeUntilTrue( SimulationState s );

    /**
     * Adds an offset to the identifier of the clocks, if any,
     * referred to by this condition.
     * @param offset The offset to add.
     */
    public void addClockOffset( int offset );

    /**
     * Returns the highest clock identifier referred to by this
     * condition. If no clock is referred to, -1 is returned.
     * @return The highest clock identifier, or -1 if no clocks used.
     */
    public int getMaxClockIdentifier();

    /**
     * Clones the condition.
     */
    public Condition cloneCondition();

    /**
     * Produces a string representation of the condition, mapping
     * identifiers to string values using the given state machine.
     * @param m State machine for mappings.
     */
    public String prettyPrint( StochasticAutomata m );
    
}
