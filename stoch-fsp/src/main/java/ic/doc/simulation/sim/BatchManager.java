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

import java.util.Vector;
import java.util.Collection;
import java.util.Iterator;

/** This class coordinates the execution of multiple simulation runs
 * in a single batch. A single {@link Simulation} is passed to the
 * constructor. This is subsequently executed multiple times,
 * resetting the simulation between each run. A {@link BatchListener}
 * can register itself to be informed of the progress of the batch.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class BatchManager {
    final private int runs;
    final private Simulation sim;

    private Collection lstnrs;
    private ResultSet[] results;

    /** Creates a new batch manager to perform a certain number of
     * runs on the given simulation.
     * @param s The simulation to execute.
     * @param runs The numbers of simulation runs to coordinate.
     */
    public BatchManager( Simulation s, int runs ) {
		this.runs = runs;
		this.sim = s;
		lstnrs = new Vector();
    }

    /** Executes the batch of simulation runs.
     */
    public void run() throws SimulationAbortionException {
		results = new ResultSet[runs];
	
		sim.prepare();

		for( int i=0; i<runs; i++ ) {
		    sim.reset();
		    for( Iterator l = lstnrs.iterator();
			 l.hasNext(); ) {
			((BatchListener) l.next()).runStarting( i );
		    }

			// garbage collect before start
			System.gc();

		    sim.run();
		    results[i] = sim.getResultSet();
		    for( Iterator l = lstnrs.iterator();
			 l.hasNext(); ) {
			((BatchListener) l.next()).runFinished( i );
		    }
		}
    }

    /** Adds a listener to be informed of progress through the batch.
     * @param l The listener.
     */
    public void addBatchListener( BatchListener l ) {
	lstnrs.add( l );
    }

    /** Returns the results from the batch after the batch has completed.
     * @return The results, indexed by the run number.
     */
    public ResultSet[] getResults() {
	return results;
    }
}
