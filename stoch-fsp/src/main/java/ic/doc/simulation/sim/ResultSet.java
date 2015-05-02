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

package ic.doc.simulation.sim;

import java.util.Map;
import java.util.Hashtable;
import java.util.Collection;

/** Represents a set of results from a single simulation run. This
 * essentially consists of a map from the measure's name to the
 * result.
 * @see Result
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $ in $RCSfile: ResultSet.java,v $
 */
public class ResultSet {
    private Map results;

    public ResultSet() {
	results = new Hashtable();
    }

    public void addResult( String m, Result r ) {
	results.put( m, r );
    }

    public Collection getMeasureNames() {
	return results.keySet();
    }

    public Result getResult( String measure ) {
	return (Result) results.get( measure );
    }

    public String toString() {
	return results.toString();
    }
}
