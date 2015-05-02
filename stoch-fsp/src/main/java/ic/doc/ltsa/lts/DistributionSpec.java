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

import ic.doc.simulation.tools.*;

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/** Describes a distribution. Properties include the type of
 * distribution and the parameters it takes.
 *
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 * @author Thomas Ayles
 */
class DistributionSpec {
    private String distType;
    private Vector parameters;
    private boolean isLocked = false;

    /** Creates a distribution descriptor with the given type.
     * @param type The type of distribution descriptor to create, e.g. "exp"
     * @pre type != null
     */
    public DistributionSpec( String type ) {
	parameters = new Vector();
	distType = type;
    }

    /** Returns the name of the type of this distribution. */
    public String getType() { return distType; }

    /** Returns the number of parameters in the parameter list. */
    public int getParameterCount() { return parameters.size(); }

    /** Adds the given expression to the end of the parameter list.
     * @param exp The expression {@link Stack} to add.
     * @throws RuntimeException If the parameter list is locked.
     */
    public void addParameter( Stack exp )
	throws RuntimeException {
	if( !isLocked )
	    parameters.add( exp );
	else
	    throw new RuntimeException( "Attempt to modify locked DistributionDesc" );
    }

    /** Prevents further parameters from being added to this
     * description. */
    public void lockParameters() { isLocked = true; }

    /** Returns the expression for the parameter at the position in
     * the list given.
     * @param n The parameter to return, valid values are 1, 2,...
     * @return The expression stack
     * @throws ArrayIndexOutOfBoundsException If the parameter index is out
     * of range.
     */
    public Stack getParameter( int n )
	throws ArrayIndexOutOfBoundsException {
	// the call to parameters.elementAt will generate the
	// exception if n is out of range
	return (Stack) parameters.elementAt( n - 1 );
    }

    /** Returns an instance of the distribution specified. The
     * parameters for the distribution are evaluated using the locals
     * and globals given.
     * @param locals A table of local variables
     * @param globals A table of global variables
     */
    public DistributionSampler getInstance( Hashtable locals, Hashtable globals )
	throws Exception {
	return DistributionFactory.getInstance()
	    .getDistributionInstance( distType,
				      parameters,
				      locals,
				      globals );
    }

    public String toString() {
	StringBuffer str = new StringBuffer( getType() );

	if( parameters.size() > 0 ) {
	    str.append( '(' );
	    
	    for( int i = 1; i <= parameters.size(); i++ ) {
		if( i < parameters.size() ) str.append( ',' );
	    }
	    
	    str.append( ')' );
	}

	return str.toString();
    }
}
