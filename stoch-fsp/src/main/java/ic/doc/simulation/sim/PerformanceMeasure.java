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

//import ic.doc.simulation.tools.Sim;

/**
 * Basic class for encapsulating a performance measure on a simulation
 * run.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public abstract class PerformanceMeasure {
    private String name;

    /** Creates a new performance measure with the given name.
     * @param name The name of the performance measure.
     */
    public PerformanceMeasure( String name ) {
		this.name = name;
    }

    /** Returns the name of the performance measure. */
    public final String getName() {
		return name;
    }

    /** Returns the result for this performance measure. Typically,
     * this method will be called at the end of a simulation run. */
    public abstract Result getResult();

    /** Resets the internally maintained statistics.
     */
    public abstract void reset();
    
    /** jew01: Resets the internally maintained statistics,
     *  leaving certain things intact to be able to restart
     *  measurements after a transient reset 
     * */
    public abstract void transientReset();

    /** Resets the entire state of the measure.
     */
    public abstract void hardReset();
}
