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

/** Abstractly represents actions that update some kind of measure.
 *
 * @author Thomas Ayles.
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public abstract class MeasureAction
    implements Action {

    private int identifier;

    /** Creates a new measure action to act on the measure with the
     * given identifier.
     * @param identifier The identifier of the measure this action
     * will update.
     */
    public MeasureAction( int identifier ) {
	this.identifier = identifier;
    }

    /** Returns the identifier of the measure this action updates.
     * @return The identifier of the measure to update.
     */
    public final int getIdentifier() { return identifier; }

    public final void applyOffsets( int clockOffset, int measureOffset ) {
	identifier += measureOffset;
    }
    public final int getMaxClockIdentifier() { return -1; }

    /**
     * Returns the name of the measure that this action affects.
     * @return The name, or the identifier number if lookup is unsuccessful.
     */
    protected final String getMeasureName( StochasticAutomata m ) {
	if( m.getMeasureNames() != null
	    && m.getMeasureNames().length > identifier
	    && m.getMeasureNames()[identifier] != null )
	    return m.getMeasureNames()[identifier];
	else
	    return Integer.toString( identifier );
    }
    
	// these do not appear in the action list any more
    public final String prettyPrint(StochasticAutomata m) {
    	return "";
    }
    
}
