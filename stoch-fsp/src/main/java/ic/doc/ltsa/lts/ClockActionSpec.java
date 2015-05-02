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

package ic.doc.ltsa.lts;

import java.util.Hashtable;

import ic.doc.simulation.sim.*;
import ic.doc.simulation.tools.*;

/**
 * Represents an abstract clock action specification.
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 * @see ClockAction
 */
abstract class ClockActionSpec {
    private static long clockCount = 0;

    private final String name;

    /** Accessor for obtaining anonymous clock names (wierd concept...).
     * @return The next unique anonymous clock name of form 'anon<n>'
     */
    public static String getAnonymousClockName() {
	return new String( "anon<" + clockCount++ + ">" );
    }

    /** Creates a new clock action specification to act on the cock
     * with the given name.
     */
    public ClockActionSpec( String name ) {
	this.name = name;
    }

    /** Returns the name of the clock this specification refers to. */
    public String getClockName() { return name; }

    /** subclasses must implement to perform context specific
     * instantiation, eg, distribution instantiation.
     */
    public abstract void instantiate( Hashtable locals, Hashtable globals );

    /** Produces an action corresponding to the spec, using the given
     * state machine as a context for producing clock identifiers.
     */
    public abstract ClockAction makeAction( StateMachine s );
}

/**
 * Describes a clock setting action.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $ in $RCSfile: ClockActionSpec.java,v $
 * @see ClockSetAction
 */
class ClockSettingSpec
    extends ClockActionSpec {
    private final DistributionSpec distSpec;
    private DistributionSampler sampler;

    /** Creates a clock setting descriptor with the given clock name
     * and distribution.
     * @param name The clock name
     * @param dist The distribution descriptor
     */
    public ClockSettingSpec( String name, DistributionSpec dist ) {
	super( name );
	this.distSpec = dist;
    }

    public void instantiate( Hashtable locals, Hashtable globals ) {
		try {
		    sampler = distSpec.getInstance( locals, globals );
		} catch( Exception e ) {
		    //e.printStackTrace();
		    //throw new RuntimeException( e.toString() );
		    throw new LTSException(e.getMessage());
		}
    }

    public ClockAction makeAction( StateMachine s ) {
	if( sampler == null )
	    throw new NullPointerException( "Sampler not instantiated" );
	return new ClockSetAction( s.getClockNumber(getClockName()), sampler );
    }

    public String toString() { return getClockName() + ":" + distSpec; }
}

/**
 * Describes a clock holding action.
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $ in $RCSfile: ClockActionSpec.java,v $
 * @see ClockHoldAction
 */
class ClockHoldSpec
    extends ClockActionSpec {

    /** Creates a specification for a clock holding action to act on
     * the clock given.
     * @param name The name of the clock to hold.
     */
    public ClockHoldSpec( String name ) {
	super( name );
    }

    public void instantiate( Hashtable l, Hashtable g ) {}
    
    public ClockAction makeAction( StateMachine s ) {
	return new ClockHoldAction( s.getClockNumber( getClockName() ) );
    }

    public String toString() { return getClockName() + ":hold"; }
}

/**
 * Specifies a clock resume action.
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $ in $RCSfile: ClockActionSpec.java,v $
 * @see ClockResumeAction
 */
class ClockResumeSpec
    extends ClockActionSpec {

    /** Creates a specification for a clock resume action to act on
     * the clock with the given name.
     * @param name The name of the clock to act on.
     */
    public ClockResumeSpec( String name ) {
	super( name );
    }

    public void instantiate( Hashtable l, Hashtable g ) {}
    
    public ClockAction makeAction( StateMachine s ) {
	return new ClockResumeAction( s.getClockNumber( getClockName() ) );
    }

    public String toString() { return getClockName() + ":resume"; }
}
