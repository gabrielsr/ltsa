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

import ic.doc.ltsa.common.iface.IActionLabels;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.simulation.sim.*;

/** Specification for the {@link PopulationCounter} performance measure.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class PopulationCounterSpec
    extends MeasureSpec {

    /** Defines the actions that should increment the population. */
    private IActionLabels incrActions;

    /** Defines the actions that should decrement the population. */
    private IActionLabels decrActions;

    /** Creates a new specification for a population counter with the
     * given name.
     * @param name The name of the population counter.
     */
    public PopulationCounterSpec( String name ) {
	super( name, PopulationCounter.class );
    }

    /** Sets the actions whose occurance should increment
     * the population measure being maintained.
     * @param actions The actions that will increment the measure.
     */
    public void setIncrementActions( IActionLabels actions ) {
	incrActions = actions;
    }

    /** Sets the actions whose occurance should decrement the
     * population measure being maitained.
     * @param actions The actions that will increment the measure.
     */
    public void setDecrementActions( IActionLabels actions ) {
	decrActions = actions;
    }

    /** Returns the actions that increment the measure.
     */
    public IActionLabels getIncrementActions() {
	return incrActions;
    }

    /** Returns the actions that decrement the measure.
     */
    public IActionLabels getDecrementActions() {
	return decrActions;
    }

    protected void configure( CompactState c, LTSOutput output, boolean probabilisticSystem ) {
		Map alpha = new Hashtable();
		doAlpha( c, alpha );
		doTransitions( c, alpha, probabilisticSystem );
    }

    private void doAlpha( CompactState c, Map alpha ) {
	int index = 0;

	alpha.put( "tau", new Integer( index++ ) );

	incrActions.initContext( null, null );
	while( incrActions.hasMoreNames() ) {
	    String name = incrActions.nextName();
	    if( !alpha.containsKey( name ) )
		alpha.put( name, new Integer( index++ ) );
	}

	decrActions.initContext( null, null );
	while( decrActions.hasMoreNames() ) {
	    String name = decrActions.nextName();
	    if( !alpha.containsKey( name ) )
		alpha.put( name, new Integer( index++ ) );
	}

	c.alphabet = new String[index];
	for( Iterator i = alpha.keySet().iterator(); i.hasNext(); ) {
	    String name = (String) i.next();
	    c.alphabet[ ((Integer) alpha.get(name)).intValue() ] = name;
	}
    }

    private void doTransitions( CompactState c, Map alpha, boolean probabilisticSystem ) {
		c.maxStates = 1;
		c.setStates(new EventState[1]);
		// measure identifier to use is zero
	
		// initialise every action with probability 1 if extended
		incrActions.initContext( null, null );
		while( incrActions.hasMoreNames() ) {
		    c.getStates()[0] = EventState
			.add( c.getStates()[0],
			      new EventState( ((Integer)alpha
					       .get( incrActions.nextName() ))
					      .intValue(),
					      0,
					      null,
					      new PopulationIncrementAction(0),
					      probabilisticSystem ? 1 : 0
					      )
			      );
		}
		decrActions.initContext( null, null );
		while( decrActions.hasMoreNames() ) {
		    c.getStates()[0]	= EventState
			.add( c.getStates()[0],
			      new EventState( ((Integer)alpha
					       .get( decrActions.nextName() ))
					      .intValue(),
					      0,
					      null,
					      new PopulationDecrementAction(0),
						  probabilisticSystem ? 1 : 0
					      )
			      );
		}
		
		// normalise probabilities if extended
		if (probabilisticSystem) EventState.normalise(c.getStates()[0]);
    }
}
