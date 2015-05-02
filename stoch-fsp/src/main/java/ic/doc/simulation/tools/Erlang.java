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

public class Erlang extends DistributionSampler {
  private int k ;
  private double theta ;
  private double acc ;

  public Erlang( int k, double theta ) {
    this.k = k ;
    this.theta = theta ;
  }

  public double next() {
    acc = 1.0 ;
    for ( int i = 1 ; i <= k ; i++ )
      acc *= Math.random() ;
    return -Math.log( acc ) / ( k * theta ) ;
  }

  public static double erlang( int k, double theta ) {
      double acc = 1.0 ;
      for ( int i = 1 ; i <= k ; i++ )
        acc *= Math.random() ;
      return -Math.log( acc ) / ( k * theta ) ;
  }

    public String toString() {
	return "erlang("+k+","+theta+")";
    }
    
	public boolean equals(Object o) {
	  if (!this.getClass().equals(o.getClass())) return false;
	  Erlang c = (Erlang)o;
	  return (this.k == c.k && this.theta == c.theta && this.acc == c.acc); 
	}

}
