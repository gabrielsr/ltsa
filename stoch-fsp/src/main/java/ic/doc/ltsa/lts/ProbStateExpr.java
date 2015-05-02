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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * Represents probabilistic state expressions, mapping to certain
 * instances of <code>LocalProcess</code> in the FSP specification. It
 * augments the choices of a normal {@link StateExpr} with a map
 * giving probabilities for each choice.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
class ProbStateExpr
    extends StateExpr {

    private Map _probs = new Hashtable();

    /** Adds each transition from <code>choices</code> to the
     * specified state machine.
     * @param from The state transitions occur from in the state machine
     * @param locals A table of local constants and variables
     * @param m The state machine to add transitions to
     */
    public void addTransition(int from, Hashtable locals, StateMachine m,boolean probabilisticSystem) {
	Enumeration e = choices.elements() ;
	while( e.hasMoreElements() ) {
	    ChoiceElement d = (ChoiceElement) e.nextElement();
	    d.addTransition( from, locals, getProbExpr( d ), m, probabilisticSystem );
	}
    }

    /**
     * Returns the probability expression for the choice element
     * indicated.
     * @param choice The choice element to obtain probability for.
     * @return The expression which evaluates the probability.
     * @throws {@link RuntimeException} If the choice element is not a
     * member of the set of choices for this state expression.
     */
    public Stack getProbExpr( ChoiceElement choice ) {
	if( !choices.contains( choice ) )
	    throw new RuntimeException( "Choice not in choices" );

	return (Stack) _probs.get( choice );
    }

    /**
     * Sets the probability expression for the choice element
     * indicated.
     * @param choice The choice element
     * @param expr The probability expression
     * @throws {@link RuntimeException} If the choice element is not a
     * member of the set of choices for this state expression.
     */
    public void setProbExpr( ChoiceElement choice, Stack expr ) {
	if( !choices.contains( choice ) )
	    throw new RuntimeException( "Choice not in choices" );

	_probs.put( choice, expr );
    }

    public String toString() {
	StringBuffer s = new StringBuffer();

	if( name != null ) {
	    // process ref
	    s.append( name.toString() );
	    if( expr != null ) {
		// add process subscripts
		Iterator i = expr.iterator();
		while( i.hasNext() ) {
		    i.next();
		    s.append( "[expr]" );
		}
	    }
	} else {
	    if( choices.size() == 1 ) {
		// deterministic process
		s.append( choices.elementAt( 0 ).toString() );
	    } else {
		// probabilistic process
		s.append( "( " );
		Iterator i = choices.iterator();
		while( i.hasNext() ) {
		    s.append( "<<probExpr>> " );
		    s.append( i.next().toString() );
		    if( i.hasNext() ) s.append( " | " );
		}
		s.append( ")" );
	    }
	}

	return s.toString();
    }
}
