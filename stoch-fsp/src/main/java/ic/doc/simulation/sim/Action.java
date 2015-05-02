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
 * This interface is used to abstract actions associated with
 * transitions. The interface provides one method, {@link #execute},
 * to execute the action appropriately updating some simulation
 * state. For reasons of efficiency and encapsulation, a composite
 * design pattern is used to represent multiple actions. For this
 * reason, the {@link CompositeAction} class is provided. The static
 * method {@link CompositeAction#add(Action,Action)} can be used to
 * form the composition of two actions.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 * 
 * @see ic.doc.ltsa.lts.EventState
 * @see ic.doc.ltsa.lts.TimedTransitionList
 * @see ic.doc.ltsa.sim.Simulation
 * @see ic.doc.ltsa.sim.SimulationState
 */
public interface Action {

    /**
     * Executes the action, updating the simulation state accordingly.
     * @param s The simulation state to update.
     */
    public void execute( SimulationState s );

    /**
     * Offsets the identifiers of clocks and measures in this action
     * by the amount specified.
     * @param clockOffset The amount to offset clock identifiers by.
     * @param measureOffset The amount to offset measure identifiers by.
     */
    public void applyOffsets( int clockOffset, int measureOffset );

    /**
     * Returns the highest clock identifier referred to by this
     * action. If no clock is referred to, -1 is returned.
     * @return The highest clock identifier, or -1 if no clocks used.
     */
    public int getMaxClockIdentifier();

    /**
     * Clones the action.
     */
    public Action cloneAction();

    /**
     * Produces a string representation of the action, using the given
     * state machine to map identifiers to names.
     * @param m The machine to use for mappings
     */
    public String prettyPrint( StochasticAutomata m );
    
}
