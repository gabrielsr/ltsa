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
 * Represents a set of results from a single simulation run for a
 * {@link Timer} when a histogram is being maintained.
 */
public class HistogramResult
    extends MeasureResult {

    private final double low;
    private final double high;
    private final Number[] bucket;
    private final int underflows;
    private final int overflows;
    
    /**
     * Creates a new result with the values given.
     * @param source The source of the result.
     * @param mean The mean.
     * @param var The variance.
     * @param min The minimum sample.
     * @param max The maximum sample.
     * @param ds The data series of samples throughout simulation.
     * @param low The lower limit of the histogram.
     * @param high The upper limit of the histogram.
     * @param bucket The counts of samples falling in each bucket,
     * indexed by bucket number starting from the lower limit of the
     * histogram.
     * @param underflows The number of histogram underflows.
     * @param overflows The number of histogram overflows.
     */
    public HistogramResult( ChartablePerformanceMeasure source,
			    double mean, double var, double min, double max,
			    TimeSeries ds,
			    double low, double high, Number[] bucket,
			    int underflows, int overflows ) {
	super( source, mean, var, min, max, ds );
	this.low = low; this.high=high; this.bucket=bucket;
	this.underflows = underflows; this.overflows = overflows;
    }

    public double getHistogramLow() { return low; }
    public double getHistogramHigh() { return high; }
    public int getBucketCount() { return bucket.length; }
    public Number getBucketContent( int n ) { return bucket[n]; }
    public int getUnderflows() { return underflows; }
    public int getOverflows() { return overflows; }
}
