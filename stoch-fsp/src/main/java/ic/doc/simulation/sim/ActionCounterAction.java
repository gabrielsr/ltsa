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

/**
 * Represents an action that updates an {@link ActionCounter}
 * performance measure.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class ActionCounterAction
    extends MeasureAction
    implements Cloneable {

    private String action;

    /**
     * Creates a new action which will log the occurrence of the
     * specified action in the given measure.
     * @param identifier The identifier of the measure to update.
     * @param action The name of the action to record.
     */
    public ActionCounterAction( int identifier, String action ) {
	super( identifier );
	this.action = action;
    }

    public void execute( SimulationState state ) {
	((ActionCounter) state.getMeasure( getIdentifier() )).record( action );
    }

    public boolean equals( Object o ) {
	if( o != null && o.getClass().equals(this.getClass()) ) {
	    ActionCounterAction a = (ActionCounterAction) o;
	    return a.getIdentifier() == this.getIdentifier()
		&& a.action == this.action;
	} else {
	    return false;
	}
    }
	
    public int hashCode() {
	return getClass().hashCode() ^ getIdentifier() ^ action.hashCode();
    }

    public Action cloneAction() {
	try {
	    return (Action) super.clone();
	} catch( CloneNotSupportedException e ) {
	    throw new RuntimeException( e.getMessage() );
	}
    }
    
/*
    public String prettyPrint( StochasticAutomata m ) {
	return getMeasureName(m) + "." + action;
    }
*/

    public String toString() {
	return getIdentifier() + ".record(" + action + ")";
    }

}
