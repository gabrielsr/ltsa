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

/**
 * This class encapsulates options for a simulation run.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class SimulationOptions {

    private double runLength;
    private double transientReset;
    private long zenoThreshold;
    private int samplePoints;
    private boolean movAvg;
    private int movAvgPeriod;

    /** Creates a new set of simulation options with default values.
     */
    public SimulationOptions() {
	runLength = 1000;
	zenoThreshold = 1000;
	samplePoints = 100;
	transientReset = 0;
	movAvg = false;
	movAvgPeriod = 5;
    }
    
    /** Sets the run length of the simulation. The run length
     * determines the number of simulated time units that the
     * simulation will continue for.
     * @param l The number of time units to run the simulation for.
     */
    public void setRunLength( double l ) {
	runLength = l;
    }

    /** Returns the run length of the simulation.
     */
    public double getRunLength() {
	return runLength;
    }

    /** Sets the time at which measures will be reset to eliminate the
     * effect of warm-up transients.
     * @param t The time of the reset, in simulated time units from
     * the beginning of the simulation.
     */
    public void setTransientResetTime( double t ) {
	transientReset = t;
    }

    /** Returns the time of the transient reset event.
     */
    public double getTransientResetTime() {
	return transientReset;
    }

    /** Sets the Zeno threshold to use during simulation. The Zeno
     * threshold is the maximum number of transitions that may be
     * performed at any point in the simulation time before a Zeon
     * condition is flagged. A Zeno condition indicates that the
     * simulation is not progressing in terms of time.
     * @param t The maximum number of transitions to be performed at
     * any point in simulation time.
     */
    public void setZenoThreshold( long t ) {
	if( t > 0 ) {
	    zenoThreshold = t;
	} else {
	    throw new IllegalArgumentException( "Zeno threshold must be positive" );
	}
    }

    /** Returns the Zeno threshold being used.
     */
    public long getZenoThreshold() {
	return zenoThreshold;
    }

    public void setNumberOfSamplePoints( int n ) {
	samplePoints = n;
    }

    public int getNumberOfSamplePoints() {
	return samplePoints;
    }

    public void setMovingAverage( boolean movAvg ) { this.movAvg = movAvg; }

    public boolean isMovingAverage() { return movAvg; }

    public void setMovingAveragePeriod( int period ) { movAvgPeriod = period; }

    public int getMovingAveragePeriod() { return movAvgPeriod; }

    public String toString() {
	StringBuffer s = new StringBuffer();
	s.append( "run length: " );
	s.append( runLength );
	s.append( " transient reset: " );
	s.append( transientReset );
	return s.toString();
    }
}
