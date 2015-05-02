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

/** Provides a list of extended transitions. An extended transition
 * occurs between two states with a certain event, subject to certain
 * conditions being satisfied and performing certain actions to update
 * the state of the system.
 *
 * @author Jonas Wolf, original code Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:29 $
 */
public class ProbabilisticTimedTransitionList extends MyList {

	private class PTEntry extends MyListEntry {
		Condition condition;
		Action action;
		double prob;
	
		public PTEntry( int from, byte[] to, int event, Condition cond, Action act, double prob ) {
			super( from, to, event );
			this.condition = cond;
			this.action = act;
			this.prob = prob;
		}
		
		public String toString() {
			return "<" + getFrom() + ","
			+ Util.toString( getTo() ) + ","
			+ getAction() + ","
			+ condition + ","
			+ action + "," 
			+ prob + ">";
		}
	}

	private boolean byteArrayEquals(byte[] a, byte[] b) {
		if (a.length!=b.length) return false;
		for (int i=0; i<a.length; i++) {
			if (a[i]!=b[i]) return false;
		}
		return true;
	} 

	/** Adds a transition to the list between the states specified
	 * occuring with the given event.
	 * @param from The state transitted from.
	 * @param to The state transitted to.
	 * @param event The event transitted with.
	 */
	public void add( int from, byte[] to, int event ) {
		add( from, to, event, null, null, 0 );
	}

	public void add (int from, byte[] to, int event, Condition guard, Action action) {
		add (from,to,event,guard,action,0);
	}
	
	public void add (int from, byte[] to, int event, double prob) {
		add (from,to,event,null,null,prob);
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
	public void add( int from, byte[] to, int event, Condition guard, Action action, double prob ) {
		MyListEntry e = new PTEntry( from, to, event, guard, action, prob );
		if (head == null) {
			head = tail = e;
		} else {
/*
			// do not do this anymore..
			// find the right spot for the event
			MyListEntry p = head;
			while (p.actionNo < event && p.next!=null) p = p.next;
			if (p instanceof PTEntry &&
			    p.actionNo == event &&
			    byteArrayEquals(p.toState,to) &&
			    (((PTEntry)p).condition == null || ((PTEntry)p).condition.equals(guard))  ) {
				//transitions can be combined
				if (((PTEntry)p).action==null)
					((PTEntry)p).action = action;
				else
					((PTEntry)p).action = CompositeAction.add(((PTEntry)p).action,action);
				((PTEntry)p).prob += prob;
			}
			else
			{
				//insert between p and p.next
				MyListEntry temp = p.next;
				p.next = e;
				e.next = temp;
				if (temp == null) tail = e;
			}
*/ 
			tail.setNext(e);
			tail = e;
		}
		++count;
	}

	/**
	 * Returns the {@link Condition} object that guards this
	 * transition.
	 */
	public Condition getCondition() {
		return head != null ? ((PTEntry) head).condition : null;
	}

	/**
	 * Returns the {@link Action} object to be executed with this
	 * transition.
	 */
	public Action getActionObject() {
		return head != null ? ((PTEntry) head).action : null;
	}
	
	/** Returns the probability associated with the current	transition. */
	public double getProbability() {
		return head != null ? ((PTEntry) head).prob : -1;
	}

	/**
	 * Normalises probabilities so that they will sum to one.
	 */
	public void normalise() {
		PTEntry current;
		double sum = 0;
	
		current = (PTEntry) head;
		while( current != null ) {
			sum += current.prob;
			current = (PTEntry) current.getNext();
		}
	
		current = (PTEntry) head;
		while( current != null ) {
			current.prob = current.prob / sum;
			current = (PTEntry) current.getNext();
		}
	}
	
	public ProbabilisticTimedTransitionList myclone() {
		ProbabilisticTimedTransitionList l = new ProbabilisticTimedTransitionList();
		l.head = head;
		l.tail = tail;
		return l;
	}
	
}
