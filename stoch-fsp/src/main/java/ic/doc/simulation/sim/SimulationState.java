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

import java.util.Collection;

/**
 * Encapsulates the state of a simulation, including the current
 * location (state number) and the current values of clocks.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 * @see Condition
 * @see Action
 */
public interface SimulationState {
    /**
     * Returns the number of clocks maintained in this simulation
     * state.
     * @return The number of clocks in the simulation.
     */
    public int numClocks();

    /**
     * Returns the current value of a clock in the simulation
     * state. The value of the clock indicates the number of time
     * units until the clock expires. If the number is zero or
     * negative, the clock has already expired.
     * @param c The identifier of the clock value to return.
     * @return The number of time units until the clock expires.
     */
    public double getClock( int c );

    /**
     * Sets the number of time units until a clock expires in the
     * current simulation state. The value should be zero or positive.
     * @param c The clock identifier of the clock whose value should
     * be set.
     * @param v The time until the clock expires.
     * @pre c >= 0 && c < numClocks()
     */
    public void setClock( int c, double v );

    /**
     * Causes the given clock to cease running.
     * @param c The clock to hold.
     */
    public void holdClock( int c );

    /**
     * Causes the given clock to resume running.
     * @param c The clock to resume.
     */
    public void resumeClock( int c );

    /**
     * Returns true iff the given clock is running.
     */
    public boolean clockRunning( int c );

    /**
     * Gets the current byte encoded state.
     * @return The current byte encoded state.
     */
    public byte[] getState();

    /**
     * Sets the current byte encoded state.
     * @param s The new state.
     */
    public void setState( byte[] s );

    /**
     * Advances all running clocks in the current simulation state by
     * the amount of time specified. The value of clocks that are held
     * is not affected.
     * @param v The number of time units to advance clocks by.
     * @pre v >= 0
     */
    public void advanceClocks( double v );

    /**
     * Returns the performance measure with the given identifier.
     */
    public PerformanceMeasure getMeasure( int identifier );

    /**
     * Returns a collection of all the {@link PerformanceMeasure}
     * being maintained in the simulation.
     * @return A collection of {@link PerformanceMeasure}
     * objects.
     */
    public Collection getPerformanceMeasures();
}
