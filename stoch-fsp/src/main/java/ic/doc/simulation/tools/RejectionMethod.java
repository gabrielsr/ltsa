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

//
// Generic code for the (acceptance-)rejection method...
//

abstract class RejectionMethod {
  private double a, b, m ;

  public RejectionMethod( double a, double b, double m ) {
    this.a = a ;
    this.b = b ;
    this.m = m ;
  }

  abstract double density( double x ) ;

  public double next() {
    double x = Uniform.uniform( a, b ) ;
    double y = Uniform.uniform( 0, m ) ;
    int nrej = 0 ;
    while ( y > density( x ) ) {
       x = Uniform.uniform( a, b ) ;
       y = Uniform.uniform( 0, m ) ;
       nrej++ ;
    }
    return x ;
  }
}

