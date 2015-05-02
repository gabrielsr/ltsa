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

package ic.doc.simulation.tools; 

public class Measure {
  private int moments, n = 0 ;
  private double moment[] = new double[ 100 ] ;

  public Measure() {
    moments = 2 ;
  }
 
  public Measure( int m ) {
    if ( moments > 99 )
      moments = 99 ;
    else if ( moments < 0 ) 
      moments = 2 ;
    else
      moments = m ;
  }

  public void add( double x ) {
    for ( int i = 1 ; i <= moments ; i++ )
      moment[ i ] += Math.pow( x, (double) i ) ;
    n += 1 ;
  }

  public double mean() {
   	return moment[1] / n ;
  }

  public int count() {
    return n ;
  }

  public double variance() {
    double mean = this.mean() ;
    return ( moment[2] - n * mean * mean ) / ( n - 1 ) ;
  }

  public double moment( int n ) {
    return moment[ n ] ;
  }

  public void reset() {
    n = 0 ;
    for ( int i = 1 ; i <= moments ; i++ )
      moment[ i ] = 0.0 ;
  }
  
  // jew01: added this to support smooth transient reset
  // but it is disabled at the moment

  private double[] _moment = null; 
  private int _n = 0;
  private boolean transientReset = false;
  
  public void transientReset() {
  	// disabled for now
  	reset();
/*  	
  	if (transientReset) throw new RuntimeException("Second transient reset occurred in measure!");
  	// keep everything intact, but remember the values at this instant
  	// so that the values can be correctly measured
  	_moment = new double[moments+1]; 
  	for (int i=1; i<=moments; i++) _moment[i] = moment[i];
  	_n = n;
*/
  }
  
  public int _count() {
	if (!transientReset) return count();
  	return (n - _n);
  }

  public double _mean() {
	if (!transientReset) return mean();
  	return (moment[1] - _moment[1]) / _count();
  }
  
  public double _variance() {
	if (!transientReset) return variance();
	double mean = this._mean() ;
	return ( (moment[2] - _moment[2]) - _count() * mean * mean ) / ( _count() - 1 ) ;
  }

}
