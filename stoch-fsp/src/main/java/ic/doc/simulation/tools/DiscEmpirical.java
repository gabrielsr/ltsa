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

public class DiscEmpirical extends DistributionSampler {
  private double xs[], cs[] ;

  public DiscEmpirical( double xs[], double fs[] ) {
    Check.checkAssertion( xs.length == fs.length && fs.length > 0,
                  "Empirical distribution array error" ) ;
    this.xs = new double[ xs.length ] ;
    this.xs = xs ;
    this.cs = new double[ xs.length ] ;
    double fTotal = 0.0 ;
    for ( int i = 0 ; i < fs.length ; i++ )
      fTotal += fs[ i ] ;
    cs[ 0 ] = fs[ 0 ] / fTotal ;
    for ( int i = 1 ; i < fs.length ; i++ )
      cs[ i ] = cs[ i - 1 ] + fs[ i ] / fTotal ;
  }

  public double next() {
    double r = Math.random() ;
    int index = 0 ;
    while ( r >= cs[ index ] ) {
      index++ ;
    }
    return xs[ index ] ;
  }
  
  public boolean equals(Object o) {
	if (!this.getClass().equals(o.getClass())) return false;
	DiscEmpirical c = (DiscEmpirical)o;
	return (this.cs.equals(c.cs) && this.xs.equals(c.cs)); 
  }

}

