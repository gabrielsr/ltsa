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

public class Normal extends DistributionSampler {
  private static final double twoPI = 2 * Math.PI ;

  private double mu, sigma, r1, r2, k ;
  private boolean mustRedo = false ;

  public Normal( double mu, double sigma ) {
    this.mu = mu ;
    this.sigma = sigma ;
  }

  public double next() {
    mustRedo = !mustRedo ;
    if ( mustRedo ) {
      r1 = Math.random() ;
      r2 = Math.random() ;
      k = Math.sqrt( -2 * Math.log( r1 ) ) ;
      return k * Math.cos( twoPI * r2 ) * sigma + mu ;
    }
    else
      return k * Math.sin( twoPI * r2 ) * sigma + mu ;
  }

    public String toString() {
	return "normal("+mu+","+sigma+")";
    }

  public static double normal(  double m, double s ) {
    double r1 = Math.random() ;
    double r2 = Math.random() ;
    double k  = Math.sqrt( -2 * Math.log( r1 ) ) ;
    return k * Math.cos( twoPI * r2 ) * s + m ;
  }
  
  public boolean equals(Object o) {
	if (!this.getClass().equals(o.getClass())) return false;
	Normal c = (Normal)o;
	return (this.mu == c.mu && this.sigma == c.sigma); 
  }

}

