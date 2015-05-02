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

/** Represents a list of probabilistic transitions.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class ProbabilisticTransitionList
    extends MyList {

    /** Represents an entry in the list.
     */
    protected class ProbEntry
	extends MyListEntry {
	/** Creates a new entry.
	 * @param from The state this transits from.
	 * @param to The state this transits to.
	 * @param action The action this transits with (should always be tau).
	 * @param prob The probability weight of this transition.
	 */
	public ProbEntry( int from, byte[] to,
			  int action, double prob ) {
	    super( from, to, action );
	    probability = prob;
	}

	public String toString() {
	    return "<" + getFrom() + ","
		+ Util.toString( getTo() ) + ","
		+ getAction() + ","
		+ probability + ">";
	}

	/** probability weight. */
	double probability;
    }

    /** Adds a transition to the list, between the states specified
     * and occuring with action TAU (0) and probability weight 0.
     * @param from The state transitted from.
     * @param to The state transitted to.
     * @param action The action transitted with - ignored and always
     * set to 0, i.e. tau.
     */

    public void add( int from, byte[] to, int action ) {
	add( from, to, action, 0 );
    }

    /** Adds a transition to the list, between the states specified
     * and occuring with action TAU (0) and the given probability
     * weight.
     * @param from The state transitted from.
     * @param to The state transitted to.
     * @param action The action transitted with - ignored and always
     * set to 0, i.e. tau.
     * @param prob The probability weight for this transition.
     */
//  TODO: we should probably call this add not the one above to pass the probability value for the tau transition - Gena’na.
    public void add( int from, byte[] to, int action, double prob ) {
	MyListEntry e = new ProbEntry( from, to, action, prob );

	if( prob <= 0 )
	    throw new RuntimeException( "probability cannot be zero or negative!" );
	
	if (head == null) {
	    head = tail = e;
	} else {
	    tail.setNext( e );
	    tail = e;
	}
	++count;
    }


    /**
     * Normalises probabilities so that they will sum to one.
     */
    public void normalise() {
	ProbEntry current;
	double sum = 0;

	current = (ProbEntry) head;
	while( current != null ) {
	    sum += current.probability;
	    current = (ProbEntry) current.getNext();
	}

	current = (ProbEntry) head;
	while( current != null ) {
	    current.probability = current.probability / sum;
	    current = (ProbEntry) current.getNext();
	}
    }
    
    /** Returns the probability associated with the current
	transition. */
    public double getProbability() {
	return head != null ? ((ProbEntry) head).probability : -1;
    }
}
