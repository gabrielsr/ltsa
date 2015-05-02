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

import java.text.DecimalFormat ;

public class Histogram extends Measure {
  private int bucket[] ;
  private double low, high, width ;
  private int n ;
  private int underflows, overflows = 0 ;

  public Histogram( double l, double h, int b ) {
    super() ;
    bucket = new int [b] ;
    low = l ;
    high = h ;
    n = b ;
    width = ( high - low ) / n ;
  }

  public void add( double x ) {
    super.add( x ) ;
    if ( x < low ) 
      underflows++ ;
    else if ( x >= high )
      overflows++ ;
    else {
      int index = (int)( ( x - low ) / width ) ;
      bucket[ index ]++ ;
    }
  }

  public int bucketContent( int i ) {
    return bucket[ i ] ;
  }

	public int underflows() { return underflows; }
	public int overflows() { return overflows; }

  public void display() {
    DecimalFormat decimal = new DecimalFormat( "000000.000" ) ;
    DecimalFormat integer = new DecimalFormat( "0000000" ) ;
    DecimalFormat general = new DecimalFormat( "######.###" ) ;
    final int maxHeight = 20 ;
    System.out.println( "\nObservations = " + super.count() + 
                        "   Mean = " + general.format( super.mean() ) +
                        "   Variance = " + general.format( super.variance() ) ) ;
    int max = 0 ;
    for ( int i = 0 ; i < n ; i++ ) 
      if ( bucket[i] > max )
        max = bucket[i] ;
    if ( max == 0 ) 
      System.out.print( "Histogram is empty\n" ) ;
    else {
      for ( int i = 0 ; i < n ; i++ ) {
        System.out.print( decimal.format( low + i * width ) + " - " +
                          decimal.format( low + ( i + 1 ) * width ) + "   " +
                          integer.format( (double) bucket[i] ) + "  |" ) ;
        String stars = "" ;
        for ( int j = 0 ; j < (double) bucket[i] / max * maxHeight ; j++ )
          stars += "*" ;
        System.out.print( stars + "\n" ) ;
      }
      System.out.print( "Underflows = " + underflows +
                        "   Overflows = " + overflows + "\n" ) ;
    }
  }    

  public void reset() {
    super.reset() ;
    underflows = overflows = 0;
    bucket = new int[bucket.length];
  }
  
  public void transientReset() {
  	// taken out for now
  	reset();
/*
  	super.transientReset();
	underflows = overflows = 0;
	bucket = new int[bucket.length];
*/
  }
  
}
