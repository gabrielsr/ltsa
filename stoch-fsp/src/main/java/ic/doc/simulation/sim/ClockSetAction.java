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
import ic.doc.simulation.tools.*;

/**
 * This class represents clock setting actions associated with a
 * transition. The action, when executed, will resample its associated
 * clock in the simulation state from the appropriate distribution
 * sampler.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 * @see DistributionSampler
 */
public class ClockSetAction
    extends ClockAction {

    private DistributionSampler dist;

    /**
     * Creates a new clock setting action that will samplethe
     * specified clock with a value drawn from the given distribution.
     * @param clock The identifier of the clock to set.
     * @param dist The distribution to draw samples from.
     */
    public ClockSetAction( int clock, DistributionSampler dist ) {
	super( clock );
	this.dist = dist;
    }

    public void execute( SimulationState state ) {
	state.setClock( getClockIdentifier(), dist.next() );
    }

    public String prettyPrint( StochasticAutomata m ) {
	return getClockIdentifier() + ":" + dist;
    }

    public String toString() {
	return getClockIdentifier() + "<-" + dist;
    }

    public boolean equals( Object o ) {
	return super.equals( o )
	    && ((ClockSetAction)o).dist.equals( this.dist );
    }

    public int hashCode() {
	return super.hashCode() ^ dist.hashCode();
    }
    
    public Class getDistributionType() {
    	return dist.getClass();
    }

}
