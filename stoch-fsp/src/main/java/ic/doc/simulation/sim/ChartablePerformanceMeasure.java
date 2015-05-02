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

/**
 * Represents those performance measure for which charting is an
 * appropriate action. The class internally maintains a data series
 * that is updated by calling the {@link #addSample()} method on the
 * object. This method will typically be called from some {@link
 * ProgressListener} on the simulation at regular time intervals.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public abstract class ChartablePerformanceMeasure
    extends PerformanceMeasure {

    private TimeSeries data;
    protected final Sim sim;
    private boolean recordMovingAverage = false;
    private boolean makeHistogram = false;
    private int histBuckets;
    private double histHigh;
    private double histLow;

    /** Creates a new performance measure with the given identifier in
     * the specified simulation context. Calls {@link #initMeasure()}
     * to initialise the internal measure.
     * @param identifier The identifier of the measure.
     * @param sim The simulation context, ie, the {@link Sim} object
     * that is being used to perform the simulation.
     */
    public ChartablePerformanceMeasure( String name, Sim sim ) {
	super( name );
	this.data = new TimeSeries();
	this.sim = sim;
	initMeasure();
    }

    /** Returns the simulation context within which this measure is
     * maintained.
     */
    public final Sim getSimulationContext() {
	return sim;
    }

    /** Determines whether this measure will produce a moving average
     * or a running average.
     * @param movAvg true iff this measure is to produce a moving
     * average.
     */
    public final void setMovingAverage( boolean movAvg ) {
	recordMovingAverage = movAvg;
    }

    /** Returns true iff this measure will record a moving average
     * over time.
     */
    public final boolean isMovingAverage() {
	return recordMovingAverage;
    }

    /** Adds a sample to the internally maintained data series.
     */
    public abstract void addSample();

    /** Returns the data series that has been maintained.
     */
    public final TimeSeries getTimeSeries() {
		return data;
    }

    /** Specifies that the measure should produce a histogram with the
     * given parameters.
     * @param low The lower range of the histogram.
     * @param high The upper range of the histogram.
     * @param buckets The number of buckets to maintain.
     */
    public final void setHistogramRange( double low, double high, int buckets ) {
		this.makeHistogram = true;
		this.histBuckets = buckets;
		this.histLow = low;
		this.histHigh = high;
		initMeasure();
    }

    /** Returns true iff the measure should produce a histogram. */
    protected final boolean makeHistogram() { return makeHistogram; }
    /** Returns the lower bound of the histogram. */
    protected final double histogramLow() { return histLow; }
    /** Returns the upper bound of the histogram. */
    protected final double histogramHigh() { return histHigh; }
    /** Returns the number of buckets in the histogram. */
    protected final int histogramBuckets() { return histBuckets; }

    /** Subclasses must implement this to initialise their internally
     * maintained measure appropriately.
     */
    protected abstract void initMeasure();

    public void hardReset() {
		data = new TimeSeries();
    }
    
    /** Subclasses should call this method from the {@link
     * #addSample()} method in order to add a data point to the
     * series. The data point will be added using the current time as
     * the x value.
     */
    protected final void addSample( double y ) {
		if( !Double.isNaN( y ) && !Double.isInfinite( y ) )
		    data.add( sim.now(), y );
		else
		    // have to add something to ensure number of sample points
		    data.add( sim.now(), 0 );
    }
}
