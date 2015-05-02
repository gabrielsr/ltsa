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

import java.util.Map;
import java.util.Hashtable;
import java.util.Iterator;

import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.simulation.sim.*;

/** Represents the specification of an {@link ActionCounter}
 * performance measure.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class ActionCounterSpec
    extends MeasureSpec {

    /** Defines the actions whose occurrence should be recorded. */
    private ActionLabels actions;

    /** Creates a new specification with the given measure name.
     * @name The name for the action counter measure.
     */
    public ActionCounterSpec( String name ) {
	super( name, ActionCounter.class );
    }

    /** Sets the set of actions that this measure should record.
     * @param actions The sets of actions to record.
     */
    public void setActions( ActionLabels actions ) {
	this.actions = actions;
    }

    /** Returns the set of actions that this measure will record.
     */
    public ActionLabels getActions() {
	return actions;
    }

    protected void configure( CompactState c, LTSOutput output, boolean probabilisticSystem ) {
		Map alpha = new Hashtable();
		doAlpha( c, alpha );
		doTransitions( c, alpha, probabilisticSystem );
    }

    private void doAlpha( CompactState c, Map alpha ) {
	int index = 0;

	alpha.put( "tau", new Integer( index++ ) );

	actions.initContext( null, null );
	while( actions.hasMoreNames() ) {
	    String name = actions.nextName();
	    if( !alpha.containsKey( name ) )
		alpha.put( name, new Integer( index++ ) );
	}
	
	c.alphabet = new String[index];
	for( Iterator i = alpha.keySet().iterator(); i.hasNext(); ) {
	    String name = (String) i.next();
	    c.alphabet[((Integer) alpha.get( name )).intValue()] = name;
	}
    }

    private void doTransitions( CompactState c, Map alpha, boolean probabilisticSystem ) {
		// initialise the state machine with one state
		c.maxStates = 1;
		c.setStates(new EventState[1]);
	
		actions.initContext( null, null );
		while( actions.hasMoreNames() ) {
		    String action = actions.nextName();
		    // measure identifier is zero.
		    c.getStates()[0] = EventState.add( c.getStates()[0], new EventState( ((Integer)alpha.get(action)).intValue(),0,null,new ActionCounterAction(0,action),probabilisticSystem ? 1 : 0));
		}
    }
}
