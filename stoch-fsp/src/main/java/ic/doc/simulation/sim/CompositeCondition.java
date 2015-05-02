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
 * This class represents a composite of one or more conditions, for
 * example, multiple clock conditions on a single transitions. It
 * implements the {@link Condition} interface in order to provide
 * transparent access to both compound and singleton conditions. The
 * static method {@link #add(Condition,Condition)} can be used to form
 * the composite of two conditions.
 */
public class CompositeCondition
    implements Condition {

    /**
     * Combines the two conditions given as arguments and returns the
     * result. If both arguments are null, null is returned. If one
     * argument is a singleton and the other null, the original
     * singleton object is returned. Otherwise a new composite condition
     * is returned containing all conditions in the arguments.
     */
    public static Condition add( Condition a, Condition b ) {
	if( a==null && b==null ) return null;
	if( a!=null && b==null ) {
	    if( a instanceof CompositeCondition ) return new CompositeCondition( a );
	    else return a;
	}
	if( a==null && b!=null ) {
	    if( b instanceof CompositeCondition ) return new CompositeCondition( b );
	    else return b;
	}

	// neither a nor b is null, so what follows is safe.
	CompositeCondition c = new CompositeCondition( a );
	c.add( b );
	return c;
    }

    /** Contains the constituent conditions. Each condition should be
     * a singleton condition for efficiency. */
    private ArrayList conditions;

    /**
     * Creates a composite condition containing the condition given.
     * @param c The initial condition for the composite to contain.
     */
    public CompositeCondition( Condition c ) {
	conditions = new ArrayList();
	add( c );
    }

    public boolean evaluate( SimulationState state ) {
	// a composite condition is true only when all its constituent
	// conditions are true

	boolean r = true;
	int i=0;
	final int size = conditions.size(); //optimise for speed

	// only bother to continue evaluation if there is some chance
	// of the composite condition being met.
	while( r && i<size ) {
	    r = r && ((Condition) conditions.get(i)).evaluate( state );
	}

	return r;
    }

    public double timeUntilTrue( SimulationState state ) {
	// the time until a composite condition becomes true is the
	// greatest of the times until its constituents become true.
	double t = 0;
	final int size = conditions.size(); //optimise for speed

	for( int i=0; i<size; i++ ) {
	    double tc = ((Condition) conditions.get( i ))
		.timeUntilTrue( state );
	    if( tc > t ) t = tc;
	}

	return t;
    }

    public void addClockOffset( int offset ) {
	for( Iterator i=conditions.iterator(); i.hasNext(); ) {
	    ((Condition)i.next()).addClockOffset( offset );
	}
    }

    public int getMaxClockIdentifier() {
	int max = -1;
	for( Iterator i=conditions.iterator(); i.hasNext(); ) {
	    int c = ((Condition)i.next()).getMaxClockIdentifier();
	    if( c > max ) max = c;
	}
	return max;
    }

    public Condition cloneCondition() {
	// performs a deep clone of the composite condition.
	CompositeCondition r;
	Iterator i = conditions.iterator();
	r = new CompositeCondition( ((Condition)i.next()).cloneCondition() );
	while( i.hasNext() ) r.add( ((Condition)i.next()).cloneCondition() );
	return r;
    }

    public boolean equals( Object o ) {
	// test set equality - order unimportant

	if( o instanceof CompositeCondition ) {
	    CompositeCondition c = (CompositeCondition) o;
	    if( size() != c.size() ) return false;
	    if( !c.conditions.containsAll( conditions ) ) return false;
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Adds a condition (composite or otherwise) to this composite
     * condition. When a composite condition is added, it's components
     * are added as seperate entities, thus avoiding a hierarchy of
     * composite conditions.
     * @param c The condition to add.
     */
    public void add( Condition c ) {
	if( c == null ) throw new NullPointerException( "null arg to add" );

	// for efficiency, do not allow a hierarchy of compound
	// conditions to be created. instead flatten into a single list.
	if( c instanceof CompositeCondition ) {
	    for( Iterator i = ((CompositeCondition)c).iterator();
		 i.hasNext(); ) {
		conditions.add( i.next() );
	    }
	} else {
	    // singleton condition
	    conditions.add( c );
	}
    }

    /**
     * Returns the total number of singleton conditions contained
     * within this compound condition.
     * @return The number of contained conditions.
     */
    public int size() {
	return conditions.size();
    }

    /**
     * Returns an iterator over the singleton conditions contained
     * within this compound condition.
     * @return An iterator over {@link Condition} objects.
     */
    public Iterator iterator() {
	return conditions.iterator();
    }

    public String toString() {
	return conditions.toString();
    }

    public String prettyPrint( StochasticAutomata m ) {
	StringBuffer str = new StringBuffer();
	for( Iterator i=conditions.iterator(); i.hasNext(); ) {
	    str.append( ((Condition) i.next()).prettyPrint( m ) );
	    if( i.hasNext() ) str.append( ',' );
	}
	return str.toString();
    }
    
    }
