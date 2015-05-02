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

/** Represents a result of the {@link Timer} or {@link
 * PopulationCounter} performance measure types.
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $ in $RCSfile: MeasureResult.java,v $
 */
public class MeasureResult
    extends Result {

    private final double mean;
    private final double variance;
    private final double minimum;
    private final double maximum;
    private final TimeSeries ds;

    /** Creates a new instance with the specified mean, variance and
     * data series (for charting).
     * @param source The source of the result.
     * @param mean The mean.
     * @param var The variance.
     * @param min The minimum recorded value.
     * @param max The maximum recorded value.
     * @param ds The data series of the measure evolving over the
     * length of a simulation run.
     */
    MeasureResult( ChartablePerformanceMeasure source,
		   double mean, double var,
		   double min, double max,
		   TimeSeries ds ) {
		super( source );
		this.mean = mean;
		this.variance = var;
		this.minimum = min;
		this.maximum = max;
		this.ds = ds;
    }

    /** Returns the mean. */
    public double getMean() { return mean; }
    /** Returns the variance. */
    public double getVariance() { return variance; }
    /** Returns the data series of the mean of the measure against
     * simulation time. */
    public TimeSeries getSeries() { return ds; }
    /** Returns the maximum recorded value. */
    public double getMaximum() { return maximum; }
    /** Returns the minimum recorded value. */
    public double getMinimum() { return minimum; }

    public String toString() {
	return "mean=" + mean + ", var=" + variance
	    + ", min=" + minimum + ", max=" + maximum;
    }
}
