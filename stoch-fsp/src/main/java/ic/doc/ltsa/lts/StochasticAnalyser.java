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

import java.util.*;

import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.IEventState;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.simulation.sim.*;

/** Used for composing extended FSP processes.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.2 $ $Date: 2005/05/12 11:28:40 $
 */
public class StochasticAnalyser
    extends Analyser
    implements StochasticAutomata {

    private final CompactState[] machines;
    private final StateCodec coder;

    /** names of measures in the composition. */
    private String[] measureNames;
    /** types of measures in the composition */
    private Class[] measureTypes;

    /** the highest clock identifier used by any machine */
    private int maxClockIdentifier = 0;
    private int Nmach;

    public StochasticAnalyser() { //not sure about assigning null to final field....
        machines = null; coder = null; 
    }
    
    /** Creates a new analyser for the given composite.
     * @param cs The composite to analyse.
     * @param output The output to use during analysis.
     */
    public StochasticAnalyser( ICompositeState cs, LTSOutput output, ic.doc.ltsa.common.infra.EventManager eman ) {
		super( cs, output, eman );
		machines = getMachines();
		Nmach = machines.length;
		setUpSharedClocksAndMeasures();
		coder = getCodec( machines );	
		disablePartialOrder();
    }

    /**
     * Sets up the shared clock and measure structures of the
     * composition.
     */
    private void setUpSharedClocksAndMeasures() {
		int nMeasures = 0;
	
		// determine size of shared measure arrays
		for( int i=0; i<machines.length; i++ ) {
		    if( machines[i].measureNames != null )
			nMeasures += machines[i].measureNames.length;
		}
		if( nMeasures > 0 ) {
		    measureNames = new String[nMeasures];
		    measureTypes = new Class[nMeasures];
		}
	
		// set up offsets
		int clOffset = 0;
		int mOffset = 0;
		for( int i=0; i<machines.length; i++ ) {
		    // find the number of clocks and measures in the state machine
		    int nClocks = machines[i].getMaxClockIdentifier() + 1;
		    int nM = (machines[i].measureNames!=null)?machines[i].measureNames.length:0;
	
		    // set up shared measure structure
		    for( int j=0; j<nM; j++ ) {
				measureNames[mOffset+j] = machines[i].measureNames[j];
				measureTypes[mOffset+j] = machines[i].measureTypes[j];
		    }
		    // array reference, so these machines will also get measures from
		    // later machines.
		    machines[i].measureNames = measureNames;
		    machines[i].measureTypes = measureTypes;
	
		    machines[i].applyOffsets( clOffset, mOffset );
		    clOffset += nClocks;
		    mOffset += nM;
		}
		maxClockIdentifier = clOffset;
    }

	private static class Trans {
        int[] to;
        int event;
        Condition condition = null;
        Action action = null;

        public String toString() {

            return "<" + Util.toString(to) + ", " + event + ", " + condition + ", " + action + ">";
        }

        public void setCondition(Condition pAction) {

            condition = pAction;

        }

        public void setAction(Action pAction) {

            action = pAction;
        }
    }

	private static class ProbTrans extends Trans {
	double prob = 0;
	
	public String toString() {
		return "<" + Util.toString( to ) + ", " + event + ", "
		+ condition + ", " + action + ", " + prob + ">";
	}
	}

	public ProbabilisticTimedTransitionList getProbTimedTransitions( byte[] s ) {
		int[] state = coder.decode( s );
		ProbabilisticTimedTransitionList ls = new ProbabilisticTimedTransitionList();
		Collection eligible = myGetProbabilisticTransitions( state );
		if (eligible==null) return ls;

		for( Iterator i = eligible.iterator(); i.hasNext(); ) {
			ProbTrans t = (ProbTrans) i.next();
			ls.add( 0, coder.encode( t.to ), t.event, t.condition, t.action, t.prob);
		}
	
		return ls;
	}

/*
    List eligibleTransitions( int[] state ) {
	// compute probabilistic transitions
	byte[] encodedState = coder.encode( state );
	if( isProbabilisticState( encodedState ) ) {
	    List ls = new Vector();
	    ProbabilisticTimedTransitionList pt = getProbabilisticTransitions( encodedState );
	    while( !pt.empty() ) {
		int[] to = coder.decode( pt.getTo() );
		to[machines.length] = 0;
		ls.add( to );
		pt.next();
	    }
	    return ls;
	} else {
	    return super.eligibleTransitions( state );
	}
    }
*/

    /** Mirrors the behaviour of {@link #eligibleTransitions}. however, instead
     * of returning a collection of int[] elements describing the next state,
     * it returns a collection of {@link Trans} objects which also contain
     * information about the clock conditions and actions associated with each
     * transition.
     * @param state The state to determine eligible transitions from
     * @return a collection of {@link Trans} objects describing the
     * available transitions.  */
    private Collection _eligibleTransitions( int[] state ) {
        int[] acts = myclone(actionCount);
        IEventState[] trs = new EventState[actionCount.length];
        int nsucc = 0; //count of feasible successor transitions
        int highs = 0; //eligible high priority actions

        for( int i=0; i<Nmach; i++ ) { // for each machine...
            IEventState p = machines[i].getStates()[state[i]];
            while( p != null ) { // for each transition...
                IEventState tr = p;
                tr.setPath( trs[tr.getEvent()] );
                trs[tr.getEvent()] = tr;
                acts[tr.getEvent()]--;
                if (tr.getEvent() !=0 && acts[tr.getEvent()]==0) {
				    nsucc ++; //ignoring tau, this transition is possible
				    if( highAction != null && highAction.get( tr.getEvent() ) ) ++highs;
                }
                p = p.getList(); // next transitions
            }
        }

        if( nsucc==0 && trs[0]==null )
	    return null; //DEADLOCK - no successor states

        int actionNo = 1;
        Collection<Trans> transitions = new ArrayList<Trans>(8);

        // we include tau if it is high priority or its low and there
        // are no high priority transitions
        if( trs[0] != null ) {
	    boolean highTau = (highAction!=null && highAction.get(0));
	    if( highTau || highs==0 )
            	_computeTauTransitions( trs[0], state, transitions );
            if( highTau ) ++highs;
        }

        while( nsucc >0 ) { // do this loop once per successor state
            nsucc--;

            // find number of action
            while( acts[actionNo] > 0 ) actionNo++;

            // now compute the state for this action if not excluded tock
            if( highs > 0 && !highAction.get( actionNo ) )
                ;// doo nothing
            else {
                IEventState tr = trs[actionNo];
                boolean nonDeterministic = false;

		// test for non determinism
                while( tr != null && !nonDeterministic ) {
                    if( tr.getNondet() != null ) nonDeterministic = true;
                    tr = tr.getPath();
                }

                tr = trs[actionNo];
                if( !nonDeterministic ) {
		    Trans t = new Trans();
		    t.to = myclone(state);
                    t.to[Nmach] = t.event = actionNo;

                    while( tr != null ) {
			// compute state changes and union in
			// conditions/actions over all sub-transitions
                        t.to[tr.getMachine()] = tr.getNext();
			t.condition
			    = CompositeCondition.add(t.condition,((EventState)tr).getCondition());
			t.action = CompositeAction.add(t.action,((EventState)tr).getAction());
                        tr = tr.getPath();
                    }

                    transitions.add( t );
                } else
		    _computeNonDetTransitions( tr, state, transitions );
            }
            ++acts[actionNo];
        }

        return transitions;
    }

    private void _computeTauTransitions(IEventState first, int[] state, Collection<Trans> transitions) {

        IEventState down = first;
        while (down != null) {
            EventState across = (EventState)down;
            while (across != null) {
                Trans t = new Trans();
                t.to = myclone(state);
                t.to[across.getMachine()] = across.getNext();
                t.to[Nmach] = t.event = 0; //tau
                t.setCondition(across.getCondition());
                t.setAction(across.getAction());

                transitions.add(t);

                across = (EventState)across.getNondet();
            }
            down = down.getPath();
        }
    }

    private void _computeNonDetTransitions(IEventState first, int[] state, Collection<Trans> v) {

        IEventState tr = first;
        while (tr != null) {
            Trans t = new Trans();
            t.to = myclone(state);
            t.to[tr.getMachine()] = tr.getNext();
            if (first.getPath() != null)
                _computeNonDetTransitions(first.getPath(), t.to, v);
            else {
                t.to[Nmach] = t.event = first.getEvent();
                t.setCondition(((EventState) first).getCondition());
                t.setAction(((EventState) first).getAction());

                v.add(t);
            }
            tr = tr.getNondet();
        }
    }

    private static final int[] myclone( int[] x ) {
		if( x==null ) return null;
		else {
		    int[] r = new int[x.length];
		    System.arraycopy( x, 0, r, 0, x.length );
		    return r;
		}
    }


    /** Returns the state codec used by this analyser.
     */
    public StateCodec getCodec() {
		return coder;
    }

    private StateCodec getCodec( CompactState[] m ) {
		int[] bases;
	
		bases = new int[ m.length ];
		for( int i=0; i<m.length; i++ ) {
		    bases[i] = m[i].maxStates;
		}
	
		return new StateCodec( bases );
    }

    public boolean isProbabilisticState( byte[] s ) {
    	// now, this class only handles probabilistic systems
    	return true;
/*
		boolean p = false;
		int[] state = coder.decode( s );
	
		for( int i=0; !p && i<machines.length; i++ ) {
		    p = p || machines[i].isProbabilisticState( CompactState.encode( state[i] ) );
		}
	
		return p;
*/
    }

	private void unsetPaths(IEventState p) {
		while (p!=null) {
			IEventState tmp = p;
			p = p.getPath();
			tmp.setPath( null );
		}
	}

	/** checks if a machine is ONLY a measure. This is hard-coded and not
	 *  very good, find a better replacement for it. */
	private static boolean isOnlyMeasure(CompactState c) {
		if (c.measureNames==null) return false;
		String s = c.getName();
		for (int i=0; i<c.measureNames.length; i++)
			if (c.measureNames[i].equals(s)) return true;
		return false;
	}
	
	/** adds all (event,action) entries present in the measure c into Map m
	 *  pre: c must be a measure, ht != null */
	private static void addEntries(Map<Integer,Action> m,CompactState c) {
		IEventState p = c.getStates()[0];
		while (p!=null) {
			Integer key = new Integer(p.getEvent());
			Action old = (Action)m.get(key);
			if (old!=null) {
				Action a = CompositeAction.add(old,((EventState)p).getAction());
				m.put(key,a);
			}
			else
				m.put(key,((EventState)p).getAction());
			p = p.getList();
		}
	}

	/** 
	 * @author jew01 - Jonas Wolf
	 * This mirrors the behavior of _eligibleTransitions, however, it takes
	 * probabilities into account. It uses rules for parallel composition
	 * originally set forth by d'Argenio et al in the paper "On Generative
	 * Parallel Composition", but extending the rules to an arbitrary
	 * number of processes, instead of just two. It gives an equal share
	 * of the scheduler to each one of the participating processes.
	 * */

	public Collection myGetProbabilisticTransitions(int[] state) {
		
		// all measures are ignored throughout composition, and
		// the measure actions are reinserted into the resulting
		// composition.
		Map<Integer,Action> eventToAction = new HashMap<Integer,Action>();
		
		boolean[] ignore = new boolean[Nmach];
		
		for (int i=0; i<Nmach; i++) {
			ignore[i] = isOnlyMeasure(machines[i]);
			if (ignore[i]) addEntries(eventToAction,machines[i]);
		}

		// will hold the proposed transitions for each machine in this state
		IEventState[] copy = new EventState[Nmach];
		IEventState[] intents = new EventState[Nmach];
		
		boolean any = false;
		
		for (int i=0; i<Nmach; i++) {
			intents[i] = machines[i].getStates()[state[i]];
			// keep a copy for reset
			copy[i] = intents[i];
			if (!any && intents[i]!=null) any=true;
		}
		
		if (!any) return null;
		
		boolean[] enabled = new boolean[alphabet.size()];
		
		for (int i=0; i<enabled.length; i++) enabled[i] = false;
		
		// check priority actions & get possible transition elements
		for (int i=0; i<Nmach; i++) {

			IEventState p = intents[i];
			while (p!=null) {
				enabled[p.getEvent()] = true;
				p = p.getList();
			}
		}
		
		// now, enabled[i] is true iff any participating process has an outgoing
		// transition with event i from this state 
		
		// number of high priority actions
		int highs = 0;
		
		// now find out the possibilities
		for (int i=0; i<enabled.length; i++) {
			if (enabled[i] && highAction != null && highAction.get(i))
				highs++;
		}
		
		// we include event i if it is high priority or its low and there
		// are no high priority transitions
		for (int i=0; i<enabled.length; i++) {
			enabled[i] = enabled[i] && (highs==0 || (highAction!=null && highAction.get(i))); 
		}
	
		// now, enabled is true iff any participating process has an outgoing
		// transition with event i from this state AND scheduling 
		// priority does not restrict this from happening

		// checked will tell us if we have checked machine i
		boolean[] checked = new boolean[Nmach];
		for (int i=0; i<Nmach; i++) checked[i] = false;
		
		// sets will hold the sets of processes which share a transition
		IEventState[] sets = new EventState[actionCount.length];
		
		// will hold finished transitions
		Collection<ProbTrans> transitions = new ArrayList<ProbTrans>(8);
		
		// will hold the overall sum of probabilities for normalisation
		double sum = 0;
		
		// while we have not reached the end
		boolean done = false;
		while (!done) { 
			// reset values
			double prob = 1;
			int numsets = 0;
			for (int i=0; i<Nmach; i++) checked[i] = false;
			// go through all of the labels and see if the transition is possible
			for (int machine = 0; machine < Nmach; machine++) {
				
				// if already checked this transition or no transitions, get next 
				if (ignore[machine] || checked[machine] || intents[machine]==null) continue;
				int label = intents[machine].getEvent();
				
				// if element is not enabled, get next
				if (!enabled[label]) continue;
				// initialise next set
				sets[numsets] = intents[machine];
				// check if all processes with that label in their alphabet are intending
				// to take action on it
				// tau does not need to synchronise
				boolean block = (label == 0);
				
				for (int i=0; !block && i<Nmach; i++) {
					// don't compare with yourself
					if (ignore[i] || machine==i) continue;
					// check if label is in the alphabet and machine is blocking
					if (machines[i].hasLabel(actionName[label])) {
						if (intents[i]==null || intents[i].getEvent() != label)
							block = true;
						else {
							// add to end of current set
							checked[i] = true;
							IEventState p = sets[numsets];
							while (p.getPath() != null) p = p.getPath();
							p.setPath( intents[i] );
						}
					}
				}
				if (!block || label==0)
					// this set can transit
					numsets++;
				else {
					// factor in probability
					prob *= sets[numsets].getProb();
					// unset all paths
					unsetPaths(sets[numsets]);
					sets[numsets] = null;
				}
			}
			
			// now, build one transition per sets of processes
			// all of them will have the same probability

			Collection<ProbTrans> temp = new ArrayList<ProbTrans>();
			for (int set=0; set<numsets; set++) {
				
				EventState tr = (EventState)sets[set];
				ProbTrans t = new ProbTrans();
				// calculate successor state
				t.to = myclone(state);
				t.to[Nmach] = t.event = tr.getEvent();
				while (tr != null) {
					// compute state changes and union in
					// conditions/actions over all sub-transitions
					t.to[tr.getMachine()] = tr.getNext();
					t.condition = CompositeCondition.add(t.condition,tr.getCondition());
					t.action = CompositeAction.add(t.action,tr.getAction());
					prob *= tr.getProb();
					tr = (EventState)tr.getPath();
				}
				
				// found final transition for this event
				temp.add(t);
				// unset all paths
				unsetPaths(sets[set]);
				sets[set] = null;
			}
			
			if (numsets>0) {
				// factor in amount of transitions
				prob /= numsets;
				// all transitions in this set will receive the same probability
				sum += temp.size()*prob;
			}
			
			// now get all transitions, add probability and add to real list
			Iterator i = temp.iterator();
			while (i.hasNext()) {
				
				boolean merge=false;
				ProbTrans pt = (ProbTrans)i.next();
				pt.prob = prob;
				// check if transition is mergeable with one from the list
				Iterator old = transitions.iterator();
				while (!merge && old.hasNext()) {
					ProbTrans t = (ProbTrans)old.next();
					// check if transitions are compatible, i.e the same
					if ( t.event==pt.event && equal(t.to,pt.to) &&
					     ( (t.action==null && pt.action==null) || (t.action!=null && t.action.equals(pt.action))) &&
					     ( (t.condition==null && pt.condition==null) || (t.condition!=null && t.condition.equals(pt.condition)))
					   )
					{
						// merge t into pt
						t.prob += pt.prob;
						merge = true;
					}
				}
				// found fresh transition, add
				if (!merge)	transitions.add(pt);
			}
			// free some space
			temp = null;
			
			// update encoding
			int pos = Nmach-1;
			boolean ok = false;
			boolean update = false;
			while (!ok) {
				// find next process with transitions
				while (pos>=0 && intents[pos]==null) pos--;
				if (pos<0) {
					// the cycle is complete, if we have not updated, we are done
					ok = true;
					if (!update) done = true;
				}
				else {
					// now have spot where we have to update
					intents[pos] = intents[pos].getList();
					if (intents[pos]!=null) {
						update = true;
						ok = true;
					}
					else {
						// now have to go left one step and set next
						// reset current position
						intents[pos] = copy[pos];
						pos--;
					}
				}
			}
		}
		
		// put measure actions back and
		// normalise probabilities with determined factor
		Iterator i = transitions.iterator();
		while (i.hasNext()) {
			ProbTrans t = (ProbTrans)i.next();
			Action a = (Action)eventToAction.get(new Integer(t.event));
			if (a!=null) t.action = CompositeAction.add(t.action,a);
			// only normalise if probabilities not 0
			if (sum>0) t.prob /= sum;
		}
		return transitions;
	}

    private boolean equal (int[] x, int[] y) {
		boolean equal = (x.length == y.length);
		for (int i=0; equal && i<x.length; i++) {
			equal = (x[i] == y[i]);
		}
		return equal;
    }

    private int[] copy(int[] x) {
		int[] tmp = new int[x.length];
		for (int i=0; i<x.length; i++) tmp[i]=x[i];
		return tmp;
    }

    private String transitionListToString( List ls ) {
		StringBuffer s = new StringBuffer();
		Iterator i = ls.iterator();
	
		s.append( '{' );
		while( i.hasNext() ) {
		    s.append( transitionToString( (int[]) i.next() ) );
		    if( i.hasNext() ) s.append( ',' );
		}
		s.append( '}' );
	
		return s.toString();
    }

    private String transitionToString( int[] tr ) {
		StringBuffer s = new StringBuffer();
	
		s.append( '<' );
		s.append( '[' );
		for( int i=0; i+1 < tr.length; i++ ) {
		    s.append( tr[i] );
		    if( i+2 < tr.length ) s.append( ',' );
		}
		s.append( ']' );
		s.append( ',' );
		s.append( tr[tr.length-1] );
		s.append( '>' );
	
		return s.toString();
    }

    public int getMaxClockIdentifier() {
		return maxClockIdentifier;
    }

    public String[] getMeasureNames() { return measureNames; }
    public Class[] getMeasureTypes() { return measureTypes; }

    /** Composes the composite process into a single state machine,
     * respecting conditions and actions on transitions and
     * probabilistic transitions. Does not perform deadlock checking,
     * or partial order reduction.
     */
    public CompactState compose() { return compose( true ); }
    public CompactState composeNoHide() { return compose( false ); }

    private CompactState compose( final boolean doHiding ) {
		
        message("Composing...");
        long start = System.currentTimeMillis();

		// transition relations
		ProbabilisticTimedTransitionList pttr = new ProbabilisticTimedTransitionList();
	
		// maintains the stack of states to visit, plus mappings to new
		// state numbers.
		MyHashStack visited = new MyHashStack( 100001 );
	
		int stateCount = 0;
	
		// start in the initial state
		visited.pushPut( coder.zero() );
		
		while(!visited.empty()) {
		    if(visited.marked())
		    	visited.pop();
		    else {
				int[] state = coder.decode(visited.peek());
				visited.mark( stateCount );
				
				// now every state is always probabilistic
				Collection transitions = myGetProbabilisticTransitions( state );
				if( transitions != null ) {
					for(Iterator i=transitions.iterator(); i.hasNext();) {
						ProbTrans t = (ProbTrans) i.next();
						byte[] code = coder.encode( t.to );
						pttr.add( stateCount, code, t.event, t.condition, t.action, t.prob );
						if(code!=null && !visited.containsKey(code))
							visited.pushPut( code );
					}
				}
				stateCount++;
		    }
		}

		CompactState c = new CompactState( getCompositeState().name,
						   visited,
						   pttr,
						   getAlphabet(),
						   endseq() );
		c.measureNames = machines[0].measureNames;
		c.measureTypes = machines[0].measureTypes;
	
		// do hiding
		if( doHiding ) {
		    CompositeState cs = getCompositeState();
		    if( cs.hidden!=null ) {
				if( !cs.exposeNotHide ){
				    c.conceal( cs.hidden );
				} else{
				    c.expose( cs.hidden );
				}
		    }
		}
        outStatistics(stateCount, pttr.size());
        message("Composed in " + (System.currentTimeMillis()-start) + "ms");  
        
        //remove the prefix from the transitions to ERROR starting with error_
        //ErrorManager e = new ErrorManager();
        //c = e.mergeErrors(c);


        
		return c;
    }

    private int translateState( int[] state ) {
		int r = 0;
	
		for( int i=0; i<machines.length; i++ ) {
		    int base = 1;
		    for( int j=i+1; j<machines.length; j++ ) {
				base *= machines[j].maxStates;
		    }
		    
		    r += state[i] * base;
		}
	
		return r;
    }

    private class StateIterator
	implements Iterator {

	private int nMachines;
	private int[] currentState;
	private int[] maxStates;
	private boolean hasNext;

	StateIterator() {
	    nMachines = machines.length;
	    
	    currentState = new int[nMachines];
	    for( int i=0; i<nMachines; i++ )
		currentState[i] = 0;

	    maxStates = new int[nMachines];
	    for( int i=0; i<nMachines; i++ )
		maxStates[i] = machines[i].maxStates;

	    hasNext = true;
	}

	public boolean hasNext() {
	    return hasNext;
	}

	public Object next() {
	    if( !hasNext )
		throw new NoSuchElementException();
	    
	    // copy current state to result
	    int[] r = new int[currentState.length];
	    for( int i=0; i<currentState.length; i++ )
		r[i] = currentState[i];

	    // increment state
	    hasNext = incrementState();

	    return r;
	}

	/** increments the state, returning true if there is another
	 * element to follow.
	 */
	private boolean incrementState() {
	    currentState[nMachines-1]++;

	    int i = nMachines - 1;
	    boolean rolling = true;
	    while( i >= 0 && rolling ) {
		if( currentState[i] == maxStates[i] ) {
		    currentState[i] = 0;
		    if( i > 0 ) currentState[i-1]++;
		} else {
		    rolling = false;
		}
		i--;
	    }

	    return !rolling;
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }
    
    public List<List<?>> getActions() {
    	List<List<?>> v = new ArrayList<List<?>>();
    	for (int i=0; i<Nmach; i++)
    		v.add(machines[i].getActions());
    	return v;
    }
    
}
