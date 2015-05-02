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

/** Represents a performance measure that counts the number of
 * occurrences of certain actions during a simulation, corresponding to
 * the syntax <code>counter FOO {a,b,c}</code>, for example.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 * @see ActionCounterAction
 */
public class ActionCounter
    extends PerformanceMeasure {
    /** A mapping from action names to the number of occurrences of
	those actions. */
    private Map actions;
    
    /**
     * Creates a new instance with the given name.
     * @param name The name of the measure being created.
     */
    public ActionCounter( String name ) {
		super( name );
		actions = new Hashtable();
    }

    /**
     * Records an occurrence of the given action.
     * @param name The name of the action to log.
     */
    public void record( String name ) {
		if( !actions.containsKey( name ) ) {
		    actions.put( name, new Integer( 1 ) );
		} else {
		    Integer i = (Integer) actions.get( name );
		    actions.put( name, new Integer( i.intValue() + 1 ) );
		}
    }

	public void transientReset() {
		// counters are not affected by a transient reset
		// it is just a normal reset
		reset();
	}

    public void reset() {
		for( Iterator i = actions.keySet().iterator(); i.hasNext(); ) {
		    actions.put( i.next(), new Integer( 0 ) );
		}
    }

    public void hardReset() {
		reset();
    }
    
    /** Returns a collection of the event names that have been
     * recorded by this measure.
     */
    public Collection getNames() {
		return actions.keySet();
    }

    /** Returns the number of occurances of a certain event recorded
     * by this measure.
     * @param name The name of the event.
     * @return The number of occurances of that event.
     */
    public int getCount( String name ) {
	Integer i = (Integer) actions.get( name );
	return i==null ? 0 : i.intValue();
    }

    public Result getResult() {
	return new ActionCounterResult( this, actions );
    }

    public String toString() {
	return "Action Counter - " + actions;
    }
}
