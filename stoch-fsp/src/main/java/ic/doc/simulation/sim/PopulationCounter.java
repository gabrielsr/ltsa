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

/** Represents a performance measure that records the population of
 * part of a system over the period of a simulation, corresponding to
 * the syntax <code>counter FOO < a, b ></code>, where <code>a</code>
 * is the set of increment actions, and <code>b</code> is the set of
 * decrement actions.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class PopulationCounter
    extends ChartablePerformanceMeasure {
    private SystemMeasure measure;
    private long population;
    private long minimum;
    private long maximum;
    private double lastAvTime = 0;
    private double lastAvValue = 0;

    /** Creates a new measure with the given name using the given
     * simulation context.
     * @param name The name of the measure
     * @param sim The simulation context
     */
    public PopulationCounter( String name, Sim sim ) {
		super( name, sim );
		minimum = maximum = population = 0;
    }

    protected void initMeasure() {
		if( makeHistogram() )
		    measure = new AreaHistogram( sim,
						 histogramLow(), histogramHigh(),
						 histogramBuckets() );
		else
		    measure = new SystemMeasure( sim );
    }

    /** Decrements the measure.
     */
    public void increment() {
		measure.update( ++population );
		if( population > maximum ) maximum = population;
		else if( population < minimum ) minimum = population;
    }

    /** Decrements the population.
     */
    public void decrement() {
		measure.update( --population );
		if( population > maximum ) maximum = population;
		else if( population < minimum ) minimum = population;
    }

	public void transientReset() {
		// disabled for now
		reset();
/*
		measure.transientReset();
		//measure.update( population );
		lastAvTime = sim.now();
		lastAvValue = population;
*/
	}

    public void reset() {
		measure.reset();
		measure.update( population );
		lastAvTime = sim.now();
		lastAvValue = population;
    }

    public void hardReset() {
		super.hardReset();
		minimum = maximum = population = 0;
		initMeasure();
		lastAvTime = lastAvValue = 0;
    }

    /** Returns the underlying system measure being maintained.
     */
    public SystemMeasure getMeasure() {
		return measure;
    }

    public Result getResult() {
		if( measure instanceof AreaHistogram ) {
		    Number[] bucket = new Number[histogramBuckets()];
		    for( int i=0; i<histogramBuckets(); i++ ) {
			bucket[i] = new Double(((AreaHistogram) measure)
					       .bucketContent(i));
		    }
		    return new HistogramResult( this,
						measure.mean(),
						measure.variance(),
						minimum,
						maximum,
						getTimeSeries(),
						histogramLow(),
						histogramHigh(),
						bucket,
						((AreaHistogram) measure).underflows(),
						((AreaHistogram) measure).overflows() );
		} else {
		    return new MeasureResult( this,
					      measure.mean(),
					      measure.variance(),
					      minimum,
					      maximum,
					      getTimeSeries() );
		}
    }

    public void addSample() {
		if( isMovingAverage() ) {
		    final double now = sim.now();
		    final double mean = measure.mean();
		    
		    if( now <= lastAvTime )
			addSample( measure.mean() );
		    else {
			final double instAvg = (mean*now - lastAvValue*lastAvTime)
			    / (now-lastAvTime);
			addSample( instAvg );
		    }
		    lastAvTime = now;
		    lastAvValue = mean;
		} else {
		    addSample( measure.mean() );
		}
    }

    public String toString() {
		return "Population measure " + getName()
		    + ": mean " + measure.mean()
		    + ", variance " + measure.variance()
		    + ", minimum " + minimum
		    + ", maximum " + maximum;
    }
}
