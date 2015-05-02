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

/** This is an action that will update a population counter during
 * simulation.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public abstract class PopulationCounterAction
    extends MeasureAction
    implements Cloneable {
    
    /** Creates a new action to update the population counter with the
     * given identifier.
     * @param identifier The identifier of the population counter this
     * action will update.
     */
    public PopulationCounterAction( int identifier ) {
		super( identifier );
    }

    public final void execute( SimulationState state ) {
		doAction( (PopulationCounter) state.getMeasure( getIdentifier() ) );
    }

    public Action cloneAction() {
		try {
		    return (Action) super.clone();
		} catch( CloneNotSupportedException e ) {
		    throw new RuntimeException( e.getMessage() );
		}
    }
    
    /** Updates the corresponding performance measure.
     * @param counter The performance measure to update.
     */
    protected abstract void doAction( PopulationCounter counter );

}
