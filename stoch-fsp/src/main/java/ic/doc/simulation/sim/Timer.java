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
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * <p>Encapsulates a <code>timer</code> measurement device. A timer is
 * used to measure the time period between occurrences of certain
 * actions corresponding to calling of {@link #start(int)} and {@link
 * #stop(int)}. Timers are held as part of the {@link
 * SimulationState}, and updated by the execution of {@link
 * TimerAction} in measurement processes.
 *
 * <p>Each time that {@link #start(int)} is called, the time of calling
 * is placed in a queue. Each call to {@link #stop(int)} results in
 * the time at the front of the queue being removed, and the
 * difference between that time and the current time being recorded as
 * a sample point in an internally maintained measure. The timer can
 * maintain multiple queues identified by the argument to {@link
 * #start(int)} and {@link #stop(int)}, and so can handle the accurate
 * recording of scenarios that are not necessarily FIFO. After
 * simulation has finished, the {@link Measure} can be obtained to
 * observe the results.
 *
 * <p>A histogram can be maintained within the measure to record the
 * distribution of times. This is achieved by calling the {@link
 * #setHistogramRange(double,double,int)} method. When this is the
 * case, the internally maintained measure is an instance of {@link
 * Histogram}, and the result returned by {@link #getResult()} will be
 * an instance of {@link HistogramResult}, as opposed to {@link
 * MeasureResult}.
 * 
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 * @see TimerAction
 * @see ic.doc.ltsa.lts.TimerSpec
 */
public class Timer
    extends ChartablePerformanceMeasure {

    /** The set of queues being maintained, indexed by queue number. */
    private LinkedList[] samples;
    private Measure measure;
    private double minimum = Double.POSITIVE_INFINITY;
    private double maximum = 0;
    private int lastAvCount = 0;
    private double lastAvTime = 0;
    private double lastAvValue = 0;

    /**
     * Creates a new timer measure with the given name. The simulation
     * context passed is used to ascertain the time at which
     * {@link #start(int)} and {@link #stop(int)} actions occur.
     * @param name The name of this measure.
     * @param sim The simulation context.
     */
    public Timer( String name, Sim sim ) {
		super( name, sim );
    }

    protected void initMeasure() {
		if( makeHistogram() ) {
		    measure = new Histogram( histogramLow(), histogramHigh(), histogramBuckets() );
		} else {
		    measure = new Measure();
		}
    }

    /** Gets the queue with the given number, creating it if necessary.
     * @param queue The queue to fetch.
     * @return The queue.
     */
    private LinkedList getQueue( int queue ) {
		if( samples == null ) samples = new LinkedList[queue+1];
		if( queue >= samples.length ) {
		    LinkedList[] tmp = new LinkedList[queue+1];
		    System.arraycopy( samples, 0, tmp, 0, samples.length );
		    samples = tmp;
		}
		if( samples[queue] == null ) samples[queue] = new LinkedList();
		return samples[queue];
    }

    /**
     * Starts a timer.
     */
    public void start( int queue ) {
		getQueue(queue).add( new Double( sim.now() ) );
    }

    /**
     * Stops a timer and records the sample point in the measure.
     */
    public void stop( int queue ) {
		try {
		    Double t = (Double) getQueue(queue).removeFirst();
		    double sample = sim.now() - t.doubleValue();
		    if( sample > maximum ) maximum = sample;
		    if( sample < minimum ) minimum = sample;
		    measure.add( sample );
		}
		catch( NoSuchElementException e ) {
		}
    }

	public void transientReset() {
		// disabled for now
		reset();
/*
		measure.transientReset();
		minimum = Double.POSITIVE_INFINITY;
		maximum = 0;
		lastAvTime = sim.now();
		lastAvValue = 0;
*/
	}

    public void reset() {
		measure.reset();
		lastAvTime = sim.now();
		lastAvValue = 0;
    }

    public void hardReset() {
		super.hardReset();
		samples = null;
		minimum = Double.POSITIVE_INFINITY;
		maximum = 0;
		reset();
    }

    /**
     * Returns the measure that this timer has been maintaining.
     */
    public Measure getMeasure() {
		return measure;
    }

    public Result getResult() {
    	// fetch "real" mean and variance, taking into account transient reset
    	double mean = measure._mean();
    	double variance = measure._variance();
    	TimeSeries timeSeries = getTimeSeries();
		if( measure instanceof Histogram ) {
		    Number[] buckets = new Number[histogramBuckets()];
		    for( int i=0; i<buckets.length; i++ ) {
				buckets[i] = new Integer( ((Histogram) measure).bucketContent(i) );
		    }
		    return new HistogramResult( this,
						mean,
						variance,
						minimum,
						maximum,
						timeSeries,
						histogramLow(),
						histogramHigh(),
						buckets,
						((Histogram) measure).underflows(),
						((Histogram) measure).overflows() );
		} else {
		    return new MeasureResult( this, 
					      mean, 
					      variance,
					      minimum,
					      maximum,
					      timeSeries );
		}
    }
    
    public void addSample() {
		if( isMovingAverage() ) {
		    final double now = sim.now();
		    final double mean = measure.mean();
		    
		    if( now <= lastAvTime )
				addSample( measure.mean() );
		    else {
				final double instAvg = (mean*now - lastAvValue*lastAvTime) / (now-lastAvTime);
				addSample( instAvg );
		    }
		    lastAvTime = now;
		    lastAvValue = mean;
		} else {
		    addSample( measure.mean() );
		}
    }

    public String toString() {
		return "timer " + getName() + ": mean = " + measure.mean()
		    + ", variance = " + measure.variance()
		    + ", minimum = " + minimum
		    + ", maximum = " + maximum;
    }
}
