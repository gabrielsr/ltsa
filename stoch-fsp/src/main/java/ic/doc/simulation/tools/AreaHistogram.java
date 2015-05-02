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

public class AreaHistogram extends SystemMeasure {
  private double bucket[] ;
  private double low, high, width ;
  private int n ;
  private int underflows, overflows = 0 ;
  private Sim sim;

  public AreaHistogram( Sim s, double l, double h, int b ) {
    super( s ) ;
    sim = s;
    bucket = new double [b] ;
    low = l ;
    high = h ;
    n = b ;
    width = ( high - low ) / n ;
  }

  public void update( double t ) {
    double current = super.currentValue() ;
    double lastChange = super.timeLastChanged() ;
    if ( current < low ) 
      underflows++ ;
    else if ( current >= high )
      overflows++ ;
    else {
      int index = (int)( ( current - low ) / width ) ;
      bucket[ index ] += sim.now() - lastChange ;
    }
    super.update( t ) ;
  }

  public double bucketContent( int i ) {
    return bucket[ i ] ;
  }
    
    public int underflows() { return underflows; }
    public int overflows() { return overflows; }

  public void display() {
    DecimalFormat decimal = new DecimalFormat( "000000.00" ) ;
    DecimalFormat area    = new DecimalFormat( "0000000.00" ) ;
    DecimalFormat general = new DecimalFormat( "######.##" ) ;
    final int maxHeight = 20 ;
    System.out.println( "\nNo. of state changes = " + super.count() + 
                        "   Mean = " + general.format( super.mean() ) +
                        "   Variance = " + general.format( super.variance() ) ) ;
    double max = 0 ;
    for ( int i = 0 ; i < n ; i++ ) 
      if ( bucket[i] > max )
        max = bucket[i] ;
    if ( max == 0 ) 
      System.out.print( "Histogram is empty\n" ) ;
    else {
      for ( int i = 0 ; i < n ; i++ ) {
        System.out.print( decimal.format( i * width ) + " - " +
                          decimal.format( ( i + 1 ) * width ) + "   " +
                          area.format( (double) bucket[i] ) + "  |" ) ;
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
  }

}
