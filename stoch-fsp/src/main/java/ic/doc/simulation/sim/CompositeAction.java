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

import ic.doc.ltsa.lts.StochasticAutomata;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents a composite of one or more {@link Action} objects. The
 * static method {@link #add(Action,Action)} is provided in order to
 * combine two {@link Action}s into a single composite.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.2 $ $Date: 2005/05/12 11:28:40 $
 */
public class CompositeAction
    implements Action {

    /** holds the composite actions (all singletons). */
    private ArrayList actions;

    /**
     * Combines the two actions given as arguments and returns the
     * result. If both arguments are null, null is returned. If one
     * argument is a singleton and the other null, the original
     * singleton object is returned. Otherwise a new composite action
     * is returned containing all actions in the arguments.
     */
	public static Action add( Action a, Action b ) {
		if( a==null && b==null ) return null;
		if( a!=null && b==null ) {
			if( a instanceof CompositeAction ) return new CompositeAction( a );
			else return a;
		}
		if( a==null && b!=null ) {
			if( b instanceof CompositeAction ) return new CompositeAction( b );
			else return b;
		}
		
		// neither a nor b is null, so what follows is safe.
		CompositeAction c = new CompositeAction( a );
		c.add( b );
		return c;
	}

    /**
     * Creates a new composite action, containing the action specified
     * (which may be a singleton or a composite). This constructor can
     * be used to create a shallow clone of another CompositeAction
     * object. A deep clone can be created using {@link #cloneAction()}.
     * @param a The initial action to be contained in the composite.
     */
    public CompositeAction( Action a ) {
	actions = new ArrayList();
	add( a );
    }

    public void execute( SimulationState state ) {
	// execute all the contained actions
	final int size = actions.size(); // optimise for speed
	for( int i=0; i<size; i++ ) ((Action)actions.get(i)).execute( state );
    }

    public void applyOffsets( int clockOffset, int measureOffset ) {
	for( Iterator i=actions.iterator(); i.hasNext(); ) {
	    ((Action)i.next()).applyOffsets( clockOffset, measureOffset );
	}
    }

    public int getMaxClockIdentifier() {
	int max = -1;
	for( Iterator i=actions.iterator(); i.hasNext(); ) {
	    int c = ((Action)i.next()).getMaxClockIdentifier();
	    if( c > max ) max = c;
	}
	return max;
    }

    public Action cloneAction() {
	Iterator i = actions.iterator();
	CompositeAction r
	    = new CompositeAction( ((Action)i.next()).cloneAction() );
	while( i.hasNext() ) r.add( ((Action)i.next()).cloneAction() );
	return r;
    }

    /**
     * Adds the given {@link Action} to the composite. If the argument
     * is a composite, then its constituents are added as singletons
     * for efficiency.
     * @param a The action to add to the composite.
     */
    public void add( Action a ) {
	if( a == null ) throw new NullPointerException( "null arg to add" );
	if( a instanceof CompositeAction ) {
	    for( Iterator i=((CompositeAction)a).iterator(); i.hasNext(); ) {
		actions.add( i.next() );
	    }
	} else {
	    actions.add( a );
	}
    }

    /**
     * Returns the number of singleton actions contained within this
     * composite.
     * @return The number of singletons contained within.
     */
    public int size() {
	return actions.size();
    }

    /**
     * Returns an iterator over all the actions contained in this
     * composite.
     * @return An iterator over {@link Action} objects.
     */
    public Iterator iterator() {
	return actions.iterator();
    }

    public boolean equals( Object o ) {
	// test for set equality

	if( o instanceof CompositeAction ) {
	    CompositeAction c = (CompositeAction) o;
	    if( size() != c.size() ) return false;
	    if( !c.actions.containsAll( actions ) ) return false;
	    return true;
	} else {
	    return false;
	}
    }

    public int hashCode() {
	int hash = 0;
	for( Iterator i=actions.iterator(); i.hasNext(); )
	    hash = hash ^ i.next().hashCode();
	return hash;
    }

    public String toString() {
		return actions.toString();
    }

    public String prettyPrint( StochasticAutomata m ) {
		StringBuffer str = new StringBuffer();
		boolean empty = true;
		for( Iterator i=actions.iterator(); i.hasNext(); ) {
			String s = ((Action) i.next()).prettyPrint(m);
		    if (!s.equals("")) {
				if (!empty) str.append(',');
		    	str.append(s);
		    	empty = false;
		    }
		}
		return str.toString();
    }

/*    
	public CompositeAction(Action a, Action b) {
	}
*/

}
