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

/**
 * Represents an action that effects some clock in the simulation state.
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public abstract class ClockAction
    implements Action, Cloneable {

    private int identifier;

    /**
     * Creates a clock action that will act on the specified clock.
     * @param identifier The identifier of the clock to act on.
     */
    public ClockAction( int identifier ) {
	this.identifier = identifier;
    }

    public final int getMaxClockIdentifier() { return identifier; }

    /**
     * Returns the identifier of the clock that this condition refers to.
     * @return The identifier of the clock this condition refers to.
     */
    public final int getClockIdentifier() { return identifier; }

    public void applyOffsets( int clockOffset, int measureOffset ) {
	identifier += clockOffset;
    }

    public Action cloneAction() {
	try {
	    return (Action) clone();
	} catch( CloneNotSupportedException e ) {
	    throw new RuntimeException( e.getMessage() );
	}
    }

    public boolean equals( Object o ) {
	return o != null
	    && this.getClass().equals( o.getClass() )
	    && ((ClockAction)o).identifier == this.identifier;
    }

    public int hashCode() {
	return identifier ^ getClass().hashCode();
    }
    
    public abstract String prettyPrint(StochasticAutomata m);

}
