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

public class Geometric extends DistributionSampler {
  private double p;
  private double q ;

  public Geometric( double p ) {
    this.p = p;
    q = Math.log( 1 - p ) ;
  }

  public double next() {
    return (int) ( Math.log( Math.random() ) / q ) ;
  }

  public static double geometric( double p ) {
    return (int) ( Math.log( Math.random() ) / Math.log( 1 - p ) ) ;
  }

    public String toString() {
	return "geometric("+p+")";
    }
    
	public boolean equals(Object o) {
	  if (!this.getClass().equals(o.getClass())) return false;
	  Geometric c = (Geometric)o;
	  return (this.p == c.p); 
	}
    
}
