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
 * <p>A class for representing a time series, ie, a collection of
 * time-value pairs, optimised for speed and storage. The class
 * maintains values internally using arrays. The initial size of the
 * array can be given when creating the object. When the capacity is
 * exceeded, new storage is allocated. All operations occur in
 * constant time, except {@link #add(double,double)}, which typically
 * occurs in constant time, but will take linear time to increase the
 * capacity. Capacity is increased to twice the current size when it
 * is exceeded.
 *
 * <p>Elements can be added to the series using {@link
 * #add(double,double)}. The series can be accessed as an
 * enumeration. The method {@link #rewind()} returns the marker to the
 * first element. The methods {@link #getTime()} and {@link
 * #getValue()} return the time and value at the current mark, and
 * {@link #next()} advances the mark to the next element. The series is
 * not stored in a time-ordered fashion - elements are stored in the
 * order in which they are added.
 */
public class TimeSeries {
    private double[] times;
    private double[] values;
    private int current = 0;
    private int size = 0;

    /**
     * Creates a time series with the default initial capacity of 2.
     */
	public TimeSeries() {
		this( 2 );
    }

    /**
     * Creates a time series with the specified initial capacity.
     * @param initialCapacity The initial capacity of the series.
     */
    public TimeSeries( int initialCapacity ) {
		times = new double[initialCapacity];
		values = new double[initialCapacity];
    }

    /**
     * Adds a time-value pair to the series.
     * @param time The time of the sample.
     * @param value The value of the sample.
     */
    public void add( double time, double value ) {
		if( size >= times.length ) {
		    // capacity will be exceeded, reallocate
		    double[] nT = new double[2*times.length];
		    double[] nV = new double[2*values.length];
		    System.arraycopy( times, 0, nT, 0, times.length );
		    System.arraycopy( values, 0, nV, 0, values.length );
		    times = nT; values = nV;
		}
		times[size] = time; values[size++] = value;
    }

    /**
     * Returns the number of time-value pairs in the series.
     */
    public int size() { return size; }

    /**
     * Returns the pointer into the series to the first element.
     */
    public void rewind() { current = 0; }

    /**
     * Returns true iff the series has more values beyond the current
     * pointer position.
     */
    public boolean hasMoreValues() { return current < size; }

    /**
     * Moves the pointer to the next time-value pair in the series.
     */
    public void next() { current++; }

    /**
     * Moves the pointer to the previous time-value pair in the series.
     */
    public void previous() { current--; }

    /**
     * Sets the pointer to the specified position in the series.
     * @param p The position to place the pointer, from 0.
     */
    public void setPosition( int p ) {
	if( p<0 || p >=size )
	    throw new IllegalArgumentException( p + " out of range" );
	current = p;
    }

    /**
     * Returns the time part of the time-value pair at the pointer.
     */
    public double getTime() { return times[current]; }

    /**
     * Returns the value part of the time-value pair at the pointer.
     */
    public double getValue() { return values[current]; }
}
