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

import ic.doc.ltsa.common.infra.MyList;
import ic.doc.ltsa.common.infra.MyListEntry;
import ic.doc.simulation.sim.*;

//import java.util.Collection;
//import java.util.Vector;

/** Provides a list of extended transitions. An extended transition
 * occurs between two states with a certain event, subject to certain
 * conditions being satisfied and performing certain actions to update
 * the state of the system.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:29 $
 */
public class TimedTransitionList
    extends MyList {

    private class TEntry
	extends MyListEntry {
	Condition condition;
	Action action;

	public TEntry( int from, byte[] to, int event,
		       Condition cond, Action act ) {
	    super( from, to, event );
	    this.condition = cond;
	    this.action = act;
	}

	public String toString() {
	    return "<" + getFrom() + ","
		+ Util.toString( getTo() ) + ","
		+ getAction() + ","
		+ condition + ","
		+ action + ">";
	}
    }

    /** Adds a transition to the list between the states specified
     * occuring with the given event.
     * @param from The state transitted from.
     * @param to The state transitted to.
     * @param event The event transitted with.
     */
    public void add( int from, byte[] to, int event ) {
	add( from, to, event, null, null );
    }

    /** Adds a transition to the list between the states specified
     * occuring with the given event, subject to conditions and
     * performing actions.
     * @param from The state transitted from.
     * @param to The state transitted to.
     * @param event The event transitted with.
     * @param guard The condition this transition is subject to.
     * @param action The action that will be performed with this
     * transition.
     */
    public void add( int from, byte[] to, int event,
		     Condition guard, Action action ) {
	MyListEntry e = new TEntry( from, to, event, guard, action );
	
	if (head == null) {
	    head = tail = e;
	} else {
	    tail.setNext( e );
	    tail = e;
	}
	++count;
    }


    /**
     * Returns the {@link Condition} object that guards this
     * transition.
     */
    public Condition getCondition() {
	return head != null ? ((TEntry) head).condition : null;
    }

    /**
     * Returns the {@link Action} object to be executed with this
     * transition.
     */
    public Action getActionObject() {
	return head != null ? ((TEntry) head).action : null;
    }
}
