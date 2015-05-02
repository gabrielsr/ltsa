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

import ic.doc.ltsa.common.iface.IAutomata;

import java.util.List;

/** Interface representing a stochastic automaton (actually a subset
 * of stochastic timed automaton). This is the semantic representation
 * of extended FSP systems.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.2 $ $Date: 2005/05/12 11:28:40 $
 */
public interface StochasticAutomata
    extends IAutomata {

    /** Reveals whether a state is probabilistic. A state is
     * probabilistic if there are probabilistic transitions out of
     * that state.
     * @param state The state to check.
     * @return true iff the state is probabilistic.
     */
    public boolean isProbabilisticState( byte[] state );


	public ProbabilisticTimedTransitionList getProbTimedTransitions(byte[] state);

    /** Returns the probabilistic transitions from a state.
     * @param state The state.
     */
//    public ProbabilisticTimedTransitionList getProbabilisticTransitions( byte[] state );

    /** Returns the non-probabilistic transitions from a state. This
     * method effectively replaces {@link Automata#getTransitions(byte[])}.
     * @param state The state.
     */
//    public ProbabilisticTimedTransitionList getTimedTransitions( byte[] state );

    /** Returns the highest clock identifier used in the automaton.
     * @return The highest clock identifier, or -1 if no clocks are
     * used.
     */
    public int getMaxClockIdentifier();

    /** Returns the names of all measures used in the automaton.
     * @return Names of measures used, indexed by identifier, null
     * return if no measures are used.
     */
    public String[] getMeasureNames();

    /** Returns the types of all measures used in the automaton.
     * @return Types of measures used, indexed by identifier, null
     * return if no measures are used.
     */
    public Class[] getMeasureTypes();
    
    public List<List<?>> getActions();
    
}
