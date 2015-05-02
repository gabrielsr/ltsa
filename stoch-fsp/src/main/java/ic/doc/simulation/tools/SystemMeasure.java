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

public class SystemMeasure {
  private double lastChange = 0.0 ;
  private int m, n = 0 ;
  private double moments[] = new double[ 100 ] ;
  private double current = 0.0 ;
  private double resetTime = 0.0 ;
  private Time time;

  public SystemMeasure() {}

  public SystemMeasure( Sim context ) {
    time = context.time;
    m = 2 ;
  }

  public SystemMeasure( Sim context, int noMoments ) {
    time = context.time;
    if ( m > 99 )
      m = 99 ;
    else if ( m < 0 )
      m = 2 ;
    else
      m = noMoments ;
  }
      
  public void update( double x ) {
    for ( int i = 1 ; i <= m ; i++ )
      moments[ i ] += ( Math.pow( current, (double) i ) ) * 
                      ( time.time - lastChange ) ;
    current = x ;
    lastChange = time.time ;
    n++ ;
  } 

  public double count() {
    return n ;
  }

  public double currentValue() {
    return current ;
  }

  public double timeLastChanged() {
    return lastChange ;
  }
      
  public double mean() {
    return (moments[1] + current*(time.time-lastChange)) / ( time.time - resetTime ) ;
  }

  public double variance() {
    double mean = this.mean() ;
    return (moments[2] + (Math.pow(current,2)*(time.time-lastChange)))/ ( time.time - resetTime ) - mean * mean ;
  }

  public double moment( int n ) {
    return moments[ n ] ;
  }

  public void reset() {
    resetTime = time.time ;
    n = 0 ;
    for ( int i = 1 ; i <= m ; i++ )
      moments[ i ] = 0.0 ;
  }
  
  // jew01: added this to support smooth transient reset

  private double[] _moment = null; 
  private int _n = 0;
  private double _time = 0;
  private boolean transientReset = false;
  
  public void transientReset() {
  	// disabled for now
  	reset();
/*
	if (transientReset) throw new RuntimeException("Second transient reset occurred in SystemMeasure!");
	// keep everything intact, but remember the values at this instant
	// so that the values can be correctly measured
	_time = time.time;
	_moment = new double[m+1];
	for (int i=1; i<=m; i++) _moment[i] = moments[i];
	_n = n;
*/
  }
  
  public double _count() {
	if (!transientReset) return count();
	return (n - _n);
  }

  public double _mean() {
	if (!transientReset) return mean();
	return (moments[1] + current*(time.time-lastChange) - _moment[1]) / ( time.time - _time ) ;
  }
  
  public double _variance() {
	if (!transientReset) return variance();
	double mean = this._mean() ;
	return (moments[2] + (Math.pow(current,2)*(time.time-lastChange)) - _moment[2]) / ( time.time - _time ) - mean * mean ;
  }

  
}