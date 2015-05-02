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
import java.util.Iterator;

/** Represents a result from the {@link ActionCounter} performance
 * measure.
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $ in $RCSfile: ActionCounterResult.java,v $
 */
public class ActionCounterResult
    extends Result {

    private final Map count;

    /** Creates a new result containing the event counts in the given
     * map.
     * @param source The source of the result.
     * @param m A map from {@link String} event names to {@link
     * Integer} counts.
     */
    ActionCounterResult( ActionCounter source, Map m ) {
	super( source );
	count = new Hashtable();
	for( Iterator i = m.keySet().iterator();
	     i.hasNext(); ) {
	    String event = (String) i.next(); 
	    count.put( event, m.get( event ) );
	}
    }

    /** Returns the set of event names recorded in this result.
     */
    public Collection getNames() {
	return count.keySet();
    }

    /** Returns the number of occurances of a given event in this
     * result.
     * @param name The name of the event.
     * @return The number of occurances.
     */
    public int getCount( String name ) {
	Integer i = (Integer) count.get( name );
	return i==null ? 0 : i.intValue();
    }

    public String toString() {
	return count.toString();
    }
}