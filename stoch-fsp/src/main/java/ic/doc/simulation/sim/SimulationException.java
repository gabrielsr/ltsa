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

/** Represents exceptions that may occur during a simulation
 * cycle. The type of the exception is determined by the constants
 * {@link #ZENO}, {@link #STALLED} and {@link #TERMINAL_STATE}.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class SimulationException
    extends RuntimeException {

    /** Exception code for possible Zeno condition. */
    public static final int ZENO = 401;
    /** Exception code for stalled execution, ie where the clock
     * conditions guarding transitions out of the current state will
     * never be satisfied. */
    public static final int STALLED = 402;
    /** Exception code for entering a state which has no outbound
     * transitions. */
    public static final int TERMINAL_STATE = 403;
    /** Exception code for failed integrity check. */
    public static final int INTEGRITY = 404;
     

    private static final Map errorStrings;

    static {
	errorStrings = new Hashtable();
	errorStrings.put( new Integer( ZENO ),
			  "Possible Zeno condition detected" );
	errorStrings.put( new Integer( STALLED ),
			  "Stalled execution (guards will never be true)" );
	errorStrings.put( new Integer( TERMINAL_STATE ),
			  "Entered terminal state" );
	errorStrings.put( new Integer( INTEGRITY ),
			  "Integrity check failed" );
    }

    private Integer code;

    /**
     * Creates a new simulation exception with the given code.
     * @param code The code of the exception, must range over
     * constants defined in this class.
     */
    public SimulationException( int code ) {
	super();
	this.code = new Integer( code );
	if( errorStrings.get( this.code ) == null )
	    throw new IllegalArgumentException( this.code
						+ " is not a valid code" );
    }

    public String getMessage() {
	return code + ": " + errorStrings.get( code );
    }
}
