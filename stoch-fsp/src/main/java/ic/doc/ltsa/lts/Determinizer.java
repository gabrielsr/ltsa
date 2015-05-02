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

import ic.doc.ltsa.common.iface.IEventState;
import ic.doc.ltsa.common.iface.LTSOutput;

import java.util.*;

/*
* the class computes a Deterministic Finite State Automata
* from a deterministic  finite state automata
* reference "Introduction to Automata Theory, Languages and Computation"
* John e. Hopcroft & Jeffrey D. Ullman p 21-23
*
* non-deterministic transitions to ERROR state are disgarded
* (but not with Dimitra's mod)
* this treats ERROR in the same way as STOP
*/

public class Determinizer {

    final static int TAU = 0;

    CompactState machine;
    LTSOutput output;

    Vector newStates;      //list of newStates, indexed transition lists (EventState)
    Vector stateSets;      //list of sets of oldStates
    Map map;         // maps sets of oldstates (BitSet) -> new state (Integer)

    protected int addState(BitSet bs){
	    Integer ii = (Integer)map.get(bs);
	    if (ii!=null) return ii.intValue();
	    map.put(bs,new Integer(nextState));
	    stateSets.addElement(bs);
	    ++nextState;
	    return nextState-1;
	}

	int nextState;         //next new state number
    int currentState;      //current state being computed

    public Determinizer(CompactState c, LTSOutput output) {
        machine = c;
        this.output = output;
    }

    public CompactState determine() {
		
		
		//TODO: this is just the regular determinisation algorithm
		// change it to deal with probabilities
		
        output.outln("make DFA("+machine.getName()+")");
        newStates =  new Vector(machine.maxStates*2);
        stateSets  =  new Vector(machine.maxStates*2);
        map = new HashMap(machine.maxStates*2);
        nextState = 0;
        currentState = 0;
        BitSet st =  new BitSet(); st.set(0); // start state is set with state 0
        addState(st);
        while (currentState<nextState) {
            compute(currentState);
            ++currentState;
        }
        return makeNewMachine();
    }

    protected void compute(int n) {
        BitSet state = (BitSet) stateSets.elementAt(n);
        IEventState tr = null; // the set of all transitions from this state set
        IEventState newtr = null; // the new transitions from the new state
        for (int i = 0; i<state.size(); ++i) {
            if (state.get(i)) tr = EventState.union(tr,machine.getStates()[i]);
        }
        IEventState action = tr;
        while (action!=null) {   //for each action
            boolean errorState = false;
            BitSet newState = new BitSet();
			/*
            if (action.next!=Declaration.ERROR)
                newState.set(action.next);
            else
                errorState = true;
            EventState nd = action.nondet;
            while (nd!=null) {
                if(nd.next!=Declaration.ERROR) {
                    newState.set(nd.next);
                    errorState = false;
                }
                nd=nd.nondet;
            }
			*/
			// change for Dimitra
            if (action.getNext() !=Declaration.ERROR)
                newState.set(action.getNext());
            else
                errorState = true;
            IEventState nd = action.getNondet();
            while (nd!=null) {
                if(nd.getNext()!=Declaration.ERROR) {
                    newState.set(nd.getNext());
                    //errorState = false;
                } else 
				    errorState = true;
                nd=nd.getNondet();
            }
            int newStateId;
            if (errorState)
                newStateId = Declaration.ERROR;
            else
                newStateId = addState(newState);
            newtr = EventState.add(newtr,new EventState(action.getEvent(),newStateId));
            action = action.getList();
        }
        newStates.addElement(newtr);
    }

    protected CompactState makeNewMachine() {
        CompactState m = new CompactState();
        m.setName(machine.getName());
        m.alphabet = new String[machine.alphabet.length];
        for (int i=0; i<machine.alphabet.length; i++) m.alphabet[i]=machine.alphabet[i];
        m.maxStates = nextState;
        m.setStates(new EventState[m.maxStates]);
        for (int i=0;i<m.maxStates; i++) {
           m.getStates()[i] = (EventState)newStates.elementAt(i);
        }
		//compute new end state if any
		if (machine.endseq>=0) 
		{
			BitSet es =  new BitSet();
			es.set(machine.endseq);
			Integer ii = (Integer)map.get(es);
            if (ii!=null) m.endseq = ii.intValue();
		}
        output.outln("DFA("+machine.getName()+") has "+m.maxStates+" states.");
        return m;
    }


}

