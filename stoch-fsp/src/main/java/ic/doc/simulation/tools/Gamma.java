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

// Only for integer values of beta, so Gamma( b, t ) is the same as
// Erlang( b, t ) - the code is for demonstration purposes only,
// showing the application of the rejection method and reinforcing
// the tutorial exercise on distribution sampling

public class Gamma extends DistributionSampler {
  private double m, b, theta, betatheta ;
  private int beta ;
  private int fact[] ;
  private GammaSampler gammaSampler ;
  private final double epsilon = 0.0000001 ; 

  public Gamma( int beta, double theta ) {
    this.theta = theta ;
    this.beta = beta ;
    betatheta = beta * theta ;
    fact = new int[ beta ] ;
    int f = 1 ;
    for ( int i = 0 ; i < beta ; i++ ) {
      fact[ i ] = f ;
      f *= ( i + 1 ) ;
    }
    double y = 0.0 ;
    m = f( ( beta - 1 ) / betatheta ) ;
    double x = 1.0, xold = 2.0 ;
    while ( Math.abs( x - xold ) / x > epsilon ) {
      xold = x ;
	  x = xold - ( bigF( xold ) - 0.99999 ) / f( xold ) ; 
    }
    b = x ;
    gammaSampler = new GammaSampler() ;
  }

    public String toString() {
	return "gamma("+beta+","+theta+")";
    }

  double bigF( double x ) {
    double acc = 0.0 ;
    for ( int i = 0 ; i < beta-1 ; i++ )
      acc += Math.pow( betatheta * x, (float) i ) / fact[ i ] ;
    return 1 - acc * Math.exp( -betatheta * x ) ;
  }

  double f( double x ) {
    return betatheta * Math.pow( betatheta * x, (float) beta - 1 ) *
           Math.exp( -betatheta * x ) / fact[ beta - 1 ] ;
  }

  public double next() {
    return gammaSampler.next() ;
  }

  class GammaSampler extends RejectionMethod {
    public GammaSampler() {
      super( 0, b, m ) ;
    }
    double density( double x ) {
      return f( x ) ;
    }
  }

  public static double gamma( double theta, int beta ) {
    Check.checkAssertion( false, "Static method for gamma sampling not available\n" +
                         "Use Gamma class instead" ) ;
    return 0.0 ;
  }
  
  public boolean equals(Object o) {
	if (!this.getClass().equals(o.getClass())) return false;
	Gamma c = (Gamma)o;
	return (this.theta == c.theta && this.beta == c.beta); 
  }

}
