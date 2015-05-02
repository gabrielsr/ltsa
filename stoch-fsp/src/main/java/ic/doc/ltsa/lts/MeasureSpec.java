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

import ic.doc.ltsa.common.iface.LTSOutput;

import java.util.Vector;

/** Abstractly specifies a measurement process during the parsing and
 * compilation process.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:29 $
 */
public abstract class MeasureSpec
    implements Compilable {

    private String name;
    private Class type;

    /**
     * Creates a new measurement specification with the given name.
     * @param name The name of the measure.
     * @param type The type of measure being created.
     */
    public MeasureSpec( String name, Class type ) {
		this.name = name;
		this.type = type;
    }

    public final CompactState makeCompactState( LTSOutput output, Vector params, boolean probabilisticSystem ) {
		return makeCompactState( output, probabilisticSystem );
    }

    public final CompactState makeCompactState( LTSOutput output, boolean probabilisticSystem ) {
		CompactState c = new CompactState();
	
		c.setName(name);
		c.init( 1 );
	
		c.measureTypes = new Class[1];
		c.measureNames = new String[1];
		c.measureTypes[0] = type;
		c.measureNames[0] = name;
	
		// allow subclasses to add transitions and alphabet.
		configure( c, output, probabilisticSystem );
		output.outln( "Compiled: " + c.getName() );
	
		return c;
    }

    /**
     * Subclasses should implement this method to configure the state
     * machine appropriately. The state machine will have been
     * initialised with the correct name, state space, measure name
     * array and measure type array. Subclasses should add the correct
     * alphabet and transitions to the machine. The identifier of the
     * measure to update is 0.
     * @param c The state machine to configure.
     * @param out Output to use during the process, if required.
     */
    protected abstract void configure( CompactState c, LTSOutput out, boolean probabilisticSystem );


    public final int getNumberOfParameters() {
	return 0;
    }

    public final String getName() { return name; }
}
