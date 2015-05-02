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
import java.io.*;

import ic.doc.ltsa.common.infra.Relation;
import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.IEventState;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.MyList;
import ic.doc.ltsa.common.infra.StackCheck;
import ic.doc.simulation.sim.*;

/** CompactState is one individual process with all its states and
 *  transitions, and the alphabet.
 * */
public class CompactState implements ICompactState, StochasticAutomata {
	
	private String name;
	public int maxStates;
	public String[] alphabet;
	private IEventState[] states; // each state is to a vector of <event, nextstate>
	int endseq = -9999; //number of end of sequence state if any

	/** Records the names of measures referred to in this process,
	 * indexed by measure identifier number. Where no measures are
	 * used, the value should be null. Otherwise, no element should be
	 * null.
	 */
	String[] measureNames;
	/** Records the types of measures referred to in this process
	 * indexed by measure identifier number. Where no measures are
	 * used, the value should be null. Otherwise, no element should be
	 * null.
	 */
	Class[] measureTypes;

	public CompactState() {} // null constructor

	public CompactState(String name, MyHashStack statemap, MyList transitions, String[] alphabet, byte[] sequence) {
	
		this.alphabet = alphabet;
		this.name = name;
		init(statemap.size());
		while(!transitions.isEmpty()) {
			
			int fromState = (int)transitions.getFrom();
			int toState   = transitions.getTo()==null?-1:statemap.get(transitions.getTo());
			Condition condition = null;
			Action action = null;
			double prob = 0;

			// if we are dealing with an extended system, get the
			// condition and action associated with the transition
			// from the list
			if( transitions instanceof ProbabilisticTimedTransitionList ) {
				condition = ((ProbabilisticTimedTransitionList)transitions).getCondition();
				action = ((ProbabilisticTimedTransitionList)transitions).getActionObject();
				prob = ((ProbabilisticTimedTransitionList)transitions).getProbability();
			}
			states[fromState] = EventState.add(states[fromState],new EventState( transitions.getAction(),toState,condition,action,prob));
			transitions.next();
		}
		if (sequence==null)
			endseq = -99999;
		else
			endseq = statemap.get(sequence);
	}

	public void reachable() {
		
		MyIntHash otn = EventState.reachable(states);
		IEventState[] oldStates = states;
		
		//System.out.println("reachable states "+otn.size()+" total states "+maxStates);
		// always do reachable for better layout!!
		//if (otn.size() == maxStates) return;
		//Collection[] oldPTR = probTransitions;
		
		init(otn.size());
		for (int oldi = 0; oldi<oldStates.length; ++oldi) {
			int newi = otn.get(oldi);
			if (newi>-2) {
				states[newi] = EventState.renumberStates(oldStates[oldi],otn);
			}
		}
		
		if (endseq>0) endseq = otn.get(endseq);
	}
    
	// change (a ->(tau->P|tau->Q)) to (a->P | a->Q)
	public void removeNonDetTau() { 
		  if (!hasTau()) return;
		  while (true) {
			  boolean canRemove = false;
			  for (int i = 0; i<maxStates; i++)   // remove reflexive tau
			  states[i] = EventState.remove(states[i],new EventState(Declaration.TAU,i));
			  BitSet tauOnly = new BitSet(maxStates);
			  for (int i = 1; i<maxStates; ++i) {
						if (EventState.hasOnlyTauAndAccept(states[i],alphabet)) {
							   tauOnly.set(i);
							   canRemove=true;
						}
			  }
				if (!canRemove) return;
				for (int i = 0; i<maxStates; ++i) {
					  if (!tauOnly.get(i))
						 states[i] = EventState.addNonDetTau(states[i],states,tauOnly);
			  }
			  int oldSize = maxStates;
			  reachable();
			  if (oldSize == maxStates) return;
		  }
	}
	
	public void removeDetCycles(String action)  {
		int act = eventNo(action);
		if (act >=alphabet.length) return;
		for (int i =0; i<states.length; ++i)  {
			if (!EventState.hasNonDetEvent(states[i],act))
				states[i] = EventState.remove(states[i],new EventState(act,i));
		}
	}
	
	//check if has only single terminal accept state
	public boolean isSafetyOnly()  {
		int terminalAcceptStates =0;
		int acceptStates = 0;
		for (int i = 0; i<maxStates; i++)  {
			if (EventState.isAccepting(states[i],alphabet)) {
			   ++acceptStates;
			   if (EventState.isTerminal(i,states[i]))
					++terminalAcceptStates;
			}
		}
		return terminalAcceptStates==1 && acceptStates ==1;
	}
	
	//precondition - isSafetyOnly()
	//translates acceptState to ERROR state
	public void makeSafety()  {
		for (int i = 0; i<maxStates; i++)  {
			if (EventState.isAccepting(states[i],alphabet)) {
			   states[i] = new EventState(Declaration.TAU,Declaration.ERROR);
			}
		}
	}			
       	
	  //remove acceptance from states with only outgoing tau
	public void removeAcceptTau(){
	  for (int i = 1; i<maxStates; ++i) {
				if (EventState.hasOnlyTauAndAccept(states[i],alphabet)) {
					 states[i] = EventState.removeAccept(states[i]);
				}
	  }
	}
	
	public boolean hasERROR() {
		for (int i=0; i<maxStates; i++ )
			if (EventState.hasState(states[i],Declaration.ERROR))
				return true;
		return false;	
	}	

    
	public void prefixLabels(String prefix) {
		name = prefix+":"+name;
		prefixMeasureNames( prefix + ":" );
		for (int i=1; i<alphabet.length; i++) { // don't prefix tau
			String old = alphabet[i];
			alphabet[i]= prefix+"."+old;
		}
	}

	private boolean hasduplicates = false;

	public boolean relabelDuplicates() {return hasduplicates;}

	public void relabel(Relation oldtonew) {
		hasduplicates = false;
		if (oldtonew.isRelation())
			relational_relabel(oldtonew);
		else
			functional_relabel(oldtonew);
	}

	private void relational_relabel(Relation oldtonew) {
		Vector na = new Vector();
		Relation otoni = new Relation();  // index map old to additional
		na.setSize(alphabet.length);
		int new_index = alphabet.length;
		na.setElementAt(alphabet[0], 0);
		for (int i=1; i<alphabet.length; i++) {
			int prefix_end = -1;
			Object o = oldtonew.get(alphabet[i]);
			if (o!=null) {
				if (o instanceof String) {
					na.setElementAt(o,i);
				} else { //one - to - many
					Vector v = (Vector)o;
					na.setElementAt(v.firstElement(),i);
					for (int j=1;j<v.size();++j) {
						na.addElement(v.elementAt(j));
						otoni.put(new Integer(i),new Integer(new_index));
						++new_index;
					}
				}
			} else if ((prefix_end=maximalPrefix(alphabet[i],oldtonew))>=0) { //is it prefix?
				String old_prefix = alphabet[i].substring(0,prefix_end);
				o = oldtonew.get(old_prefix);
				if (o!=null) {
					if (o instanceof String) {
						na.setElementAt(((String)o) + alphabet[i].substring(prefix_end),i);
					} else { //one - to - many
						Vector v = (Vector)o;
						na.setElementAt(((String)v.firstElement()) + alphabet[i].substring(prefix_end),i);
						for (int j=1;j<v.size();++j) {
							na.addElement(((String)v.elementAt(j)) + alphabet[i].substring(prefix_end));
							otoni.put(new Integer(i),new Integer(new_index));
							++new_index;
						}
					}
				} else {
					na.setElementAt(alphabet[i],i); //not relabelled
				}
			} else {
				na.setElementAt(alphabet[i],i); //not relabelled
			}
		}
		//install new alphabet
		String aa[] = new String[na.size()];
		na.copyInto(aa);
		alphabet = aa;
		// add transitions
		addtransitions(otoni);
		checkDuplicates();
	}

	private void functional_relabel(Hashtable oldtonew) {
	   for (int i=1; i<alphabet.length; i++) {  //don't relabel tau
			String newlabel = (String)oldtonew.get(alphabet[i]);
			if (newlabel!=null)
				 alphabet[i] = newlabel;
			else
				 alphabet[i] = prefixLabelReplace(i,oldtonew);
		}
		checkDuplicates();
	}

	private void checkDuplicates(){
		Hashtable duplicates=new Hashtable();
		for (int i=1; i<alphabet.length; i++) {
			if(duplicates.put(alphabet[i],alphabet[i])!=null) {
				hasduplicates = true;
				crunchDuplicates();
			}
		}
	}

	private void crunchDuplicates() {
		Hashtable newAlpha = new Hashtable();
		Hashtable oldtonew = new Hashtable();
		int index = 0;
		for(int i = 0; i<alphabet.length; i++) {
			if (newAlpha.containsKey(alphabet[i])) {
				oldtonew.put(new Integer(i), newAlpha.get(alphabet[i]));
			} else {
				newAlpha.put(alphabet[i],new Integer(index));
				oldtonew.put(new Integer(i), new Integer(index));
				index++;
			}
		}
		alphabet = new String[newAlpha.size()];
		Enumeration e = newAlpha.keys();
		while(e.hasMoreElements()) {
			String s = (String)e.nextElement();
			int i = ((Integer)newAlpha.get(s)).intValue();
			alphabet[i] = s;
		}
		// renumber transitions
		for (int i=0; i<states.length; i++)
			states[i] = EventState.renumberEvents(states[i],oldtonew);
			
/*
		// renumber probTransitions
		if (probTransitions != null) {
			for (int i=0; i<probTransitions.length; i++) {
				if (probTransitions[i]==null) break;
				Iterator it = probTransitions[i].iterator();
				while (it.hasNext()) {
					ProbClockTransition t = (ProbClockTransition)it.next();
					int newevent = ((Integer)oldtonew.get(new Integer(t.event))).intValue();
					t.event = newevent;
				}
			}
		}
*/
	 }
     
	 //now used only for incremental minimization
	 public Vector hide(Vector toShow) {
	   Vector toHide = new Vector();
		for(int i = 1; i<alphabet.length; i++) {
			if (!contains(alphabet[i],toShow))
				toHide.addElement(alphabet[i]);
		}
		return toHide;
	}


	// hides every event but the ones in toShow
	public void expose(Vector toShow) {
		BitSet visible = new BitSet(alphabet.length);
		for(int i=1; i<alphabet.length ; ++i) {
		   if (contains(alphabet[i],toShow)) visible.set(i);
		}
		visible.set(0);
		dohiding(visible);
	}

	public void conceal(Vector toHide) {
		BitSet visible = new BitSet(alphabet.length);
		for(int i=1; i<alphabet.length ; ++i) {
		   if (!contains(alphabet[i],toHide)) visible.set(i);
		}
		visible.set(0);
		dohiding(visible);
	}
  
	private void dohiding(BitSet visible) {
		Integer tau = new Integer(Declaration.TAU);
		Hashtable oldtonew = new Hashtable();
		Vector newAlphabetVec = new Vector();
		int index =0;
		for(int i = 0; i<alphabet.length; i++) {
			if (!visible.get(i)) {
				oldtonew.put(new Integer(i), tau);
			} else {
				newAlphabetVec.addElement(alphabet[i]);
				oldtonew.put(new Integer(i), new Integer(index));
				index++;
			}
		}
		alphabet = new String[newAlphabetVec.size()];
		newAlphabetVec.copyInto(alphabet);
		// renumber transitions
		for (int i=0; i<states.length; i++)
			states[i] = EventState.renumberEvents(states[i],oldtonew);
	 }

	static boolean contains(String action, Vector v) {
		Enumeration e = v.elements();
		while(e.hasMoreElements()) {
			String s = (String)e.nextElement();
			if(s.equals(action) || isPrefix(s,action)) return true;
		}
		return false;
	}

	// make every state have transitions to ERROR state
	// for actions not already declared from that state
	// properties can terminate in any state,however, we set no end state

/*
	private boolean prop = false;
	
	public boolean isProperty() {
		return prop;
	}	
*/

	public void makeProperty() {
/*
		endseq = -9999;
		prop = true;
*/
		for (int i=0; i<maxStates; i++ )
			states[i] = EventState.addTransToError(states[i],alphabet.length);
	}

	public boolean isNonDeterministic() {
		for (int i=0; i<maxStates; i++ )
			if (EventState.hasNonDet(states[i])) return true;
		return false;
	}

	//output LTS in aldebaran format
	public void printAUT(PrintStream out) {
		out.print("des(0,"+ntransitions()+","+maxStates+")\n");
		for (int i=0; i<states.length; i++)
			EventState.printAUT(states[i],i,alphabet,out);
	}
	
	// output LTS in PRISM format
	public void printPrism(PrintStream out) {
		// this uses an error machine to signify reaching an error state
		// which is easier for testing formulas.
		out.println("//This code was automatically generated by LTSA.");
		out.println("probabilistic");
		out.println("module "+name);
		out.println();
		out.println("\ts : [0.."+maxStates+"] init 0;");
		out.println("\terror : [0..1] init 0;");
		out.println();

		int error = maxStates;
		for (int i = 0; i<maxStates; i++ ) {
			IEventState current = EventState.transpose(states[i]);
			while (current != null) {
				out.print("\t[] s="+i+" -> ");
				IEventState p = current;
				while (p!=null) {
					//[] s=0 -> 0.5 : s'=1 + 0.5 : s'=2;
					out.print(p.getProb()+" : ");
					if (p.getNext()>=0)
						out.print("s'="+ p.getNext());
					else
						out.print("s'="+ error +" & error'=1");
					p = p.getNondet();
					out.print( (p != null) ? " + " : ";\n");
				}
				current = current.getList();
			}
		}
		out.println();
		out.println("endmodule");
/*

 PRISM notation:
 
		probabilistic

		module die
	
			// local state
			s : [0..7] init 0;
			// value of the die
			d : [0..6] init 0;
	
			[] s=0 -> 0.5 : s'=1 + 0.5 : s'=2;
			[] s=1 -> 0.5 : s'=3 + 0.5 : s'=4;
			[] s=2 -> 0.5 : s'=5 + 0.5 : s'=6;
			[] s=3 -> 0.5 : s'=1 + 0.5 : s'=7 & d'=1;
			[] s=4 -> 0.5 : s'=7 & d'=2 + 0.5 : s'=7 & d'=3;
			[] s=5 -> 0.5 : s'=7 & d'=4 + 0.5 : s'=7 & d'=5;
			[] s=6 -> 0.5 : s'=2 + 0.5 : s'=7 & d'=6;
			[] s=7 -> s'=7;
	
		endmodule
*/
	}
	
	public void printTransitions(LTSOutput output, int MAXPRINT) {
	    printTransitions( output , MAXPRINT , true );
	}

    public void printTransitions( LTSOutput output , int MAXPRINT , boolean summary ) {

	int linecount = 0;

	if ( summary ) {
	    //print name
	    output.outln("Process:");
	    output.outln("\t"+name);
	    // print number of states
	    output.outln("States:");
	    output.outln("\t"+maxStates);
	    output.outln("Transitions:");
	}

	output.outln("\t"+name+ " = Q0,");
	for (int i = 0; i<maxStates; i++ ) {
	    output.out("\tQ"+i+"\t= ");
	    EventState current = EventState.transpose(states[i]);
	    if (current == null) {
		if (i==endseq)
		    output.out("END");
		else
		    output.out("STOP");
		if (i<maxStates-1) 
		    output.outln(","); 
		else 
		    output.outln(".");  
	    }
	    else {
		output.out("(");
		while (current != null) {
		    if (current.getProb()==0) {
			linecount++;
			if (linecount>MAXPRINT) {
			    output.outln("EXCEEDED MAXPRINT SETTING");
			    return;
			}
			String[] events = EventState.eventsToNext(current,alphabet);
			Alphabet a = new Alphabet(events);
			output.out(" "+a);
			output.out(" -> ");
			if (current.getNext()<0) 
			    output.out("ERROR"); 
			else 
			    output.out("Q"+current.getNext());
		    }
		    else {
			EventState p = current;
			while (p!=null) {
			    linecount++;
			    if (linecount>MAXPRINT) {
				output.outln("EXCEEDED MAXPRINT SETTING");
				return;
			    }
			    output.out(" ("+p.getProb()+") ");
			    if (p.getCondition()!=null) {
				String s = p.getCondition().prettyPrint(this);
				if (!s.equals("")) output.out("?"+s+"? ");
			    }
			    output.out(alphabet[p.getEvent()]);
			    if (p.getAction()!=null) {
				String s = p.getAction().prettyPrint(this);
				if (!s.equals("")) output.out(" <"+s+">");
			    }
			    output.out(" -> ");
			    if (p.getNext()<0) 
				output.out("ERROR"); 
			    else 
				output.out("Q"+p.getNext());
			    p = (EventState)p.getNondet();
			    if (p!=null) {
				output.out("\n\t\t  |");
			    }

			}
		    }
		    current = (EventState)current.getList();
		    if (current==null) {
			if (i<maxStates-1)
			    output.outln("),"); 
			else 
			    output.outln(").");
		    }
		    else {
			output.out("\n\t\t  |");
		    }
		}
	    }
	}
    }

	public CompactState myclone() {
		CompactState m = new CompactState();
		m.name = name;
		if( measureNames != null ) {
			m.measureNames = new String[measureNames.length];
			m.measureTypes = new Class[measureTypes.length];
			System.arraycopy( measureNames, 0,
					  m.measureNames, 0,
					  measureNames.length );
			System.arraycopy( measureTypes, 0,
					  m.measureTypes, 0,
					  measureTypes.length );
		} else {
			m.measureNames = null; m.measureTypes = null;
		}
		m.endseq = endseq;
		//m.prop = prop;
		m.alphabet = new String[alphabet.length];
		for (int i=0; i<alphabet.length; i++) m.alphabet[i]=alphabet[i];
		m.init( maxStates );
		for (int i=0;i<maxStates; i++) {
			m.states[i] = EventState.union(m.states[i],states[i]);
/*
			//copy probabilistic transitions if any
			if(probTransitions != null)
				m.probTransitions[i] = probTransitions[i];
*/
		}
		return m;
	}

	public int ntransitions() {
		int count = 0;
		for (int i=0; i<states.length; i++)
			count += EventState.count(states[i]);
		return count;
	}

	public boolean hasTau() {
		for (int i = 0; i<states.length; ++i) {
			if (EventState.hasTau(states[i])) return true;
		}
		return false;
	}


	/* ------------------------------------------------------------*/
	private String prefixLabelReplace(int i, Hashtable oldtonew) {
		int prefix_end = maximalPrefix(alphabet[i],oldtonew);
		if (prefix_end<0) return alphabet[i];
		String old_prefix = alphabet[i].substring(0,prefix_end);
		String new_prefix = (String)oldtonew.get(old_prefix);
		if (new_prefix==null) return alphabet[i];
		return new_prefix + alphabet[i].substring(prefix_end);
	}

	private int maximalPrefix(String s, Hashtable oldtonew) {
		int prefix_end = s.lastIndexOf('.');
		if (prefix_end<0) return prefix_end;
		if (oldtonew.containsKey(s.substring(0,prefix_end)))
			return prefix_end;
		else
			return maximalPrefix(s.substring(0,prefix_end),oldtonew);
	}

	static private boolean isPrefix(String prefix, String s) {
		int prefix_end = s.lastIndexOf('.');
		if (prefix_end<0) return false;
		if (prefix.equals(s.substring(0,prefix_end)))
			return true;
		else
			return isPrefix(prefix,s.substring(0,prefix_end));
	}

	/* ------------------------------------------------------------*/

	public boolean isErrorTrace(Vector trace) {
		boolean hasError = false;
		for (int i=0; i<maxStates && !hasError; i++ )
			if (EventState.hasState(states[i],Declaration.ERROR))
				hasError=true;
		if (!hasError) return false;
		return isTrace(trace); //,0,0);
	}

    //
    // -JMC
    //

      private boolean isTrace(Vector trace) {

	  BitSet currentStateSet = new BitSet(maxStates);

	  // Set the initial state as reachable
	  currentStateSet.set(0);

	  // Initially, the error state cannot be reached
	  boolean errorStateReached = false;

	  // Loop over the trace, one event at a time
	  for(int i = 0; i < trace.size(); i++) {

	      errorStateReached = false;

	      String eventName = (String)trace.elementAt(i);
	      int eventNumber  = eventNo(eventName);

	      // If the event is tau or in our alphabet, compute all the
	      // states that are reachable in one step on that event from
	      // the states in currentStateSet
	      BitSet nextStateSet = new BitSet(this.maxStates);

	      if(eventNumber < alphabet.length) {

		  // Loop over the states in currentStateSet
		  for(int currentState = currentStateSet.nextSetBit(0);
		      currentState >= 0;
		      currentState = currentStateSet.nextSetBit(currentState+1)) {
		      // Look at all things reachable in one step from
		      // currentState
		      int nextStates[] = EventState.nextState(this.states[currentState], eventNumber);
            
		      if(nextStates != null) {

			  for (int j = 0; j < nextStates.length; j++) {
			      int nextState = nextStates[j];
			      
			      // If nextState is the error state, note this,
			      // otherwise, add the state to the nextStateSet
			      if(nextState == Declaration.ERROR) {
				  errorStateReached = true;
			      } else {
				  nextStateSet.set(nextState);
			      }
			  } // end for j=0..nextStates.length
		      } // end for loop over currentStateSet
		  } // end if(nextStates != null)
	      } // end if(eventNumber < alphabet.length)
	      
	      // Now that nextStateSet has been computed, what happens to it
	      // depends on what the event is.  If it is tau, then this
	      // CompactState may or may not transition into those state, so
	      // union currentStateSet and nextStateSet.  If it is not tau
	      // and in our alphabet, this CompactState must make a
	      // transition, so replace currentStateSet by nextStateSet.
	      
	      if(eventNumber == Declaration.TAU) {
		  currentStateSet.or(nextStateSet);
	      } else if(eventNumber < alphabet.length) {
		  currentStateSet = nextStateSet;
	      }
	  } // end for loop over trace
	  
	  return(errorStateReached);
      }
    

    
    // 
    // -JNM
    //
    // 	private boolean isTrace(Vector v,int index, int start) {
    // 		if (index<v.size()) {
    // 			String ename = (String) v.elementAt(index);
    // 			int eno = eventNo(ename);
    // 			if (eno<alphabet.length) {   // this event is in the alphabet
    // 				if (EventState.hasEvent(states[start],eno)) {
    // 					int n[] = EventState.nextState(states[start],eno);
    // 					for (int i=0; i<n.length; ++i) // try each nondet path
    // 						if (isTrace(v,index+1,n[i])) return true;
    // 					return false;
    // 				} else if (eno!=Declaration.TAU)  // ignore taus
    // 					return false;
    // 			}
    // 			return isTrace(v,index+1,start);
    // 		} else
    // 			return (start == Declaration.ERROR);
    // 	}

	private int eventNo(String ename) {
		int i = 0;
		while (i<alphabet.length && !ename.equals(alphabet[i])) i++;
		return i;
	}

	/* ---------------------------------------------------------------*/

	/* addAcess extends the alphabet by creating a new copy of the alphabet
	   for each prefix string in pset. Each transition is replicated acording to
	   the number of prefixes and renumbered with the new action number.
	*/

	public void addAccess(Vector pset) {
		int n = pset.size();
		if (n==0) return;
		String s = "{";
		CompactState machs[] = new CompactState[n];
		Enumeration e =  pset.elements();
		int i =0;
		while (e.hasMoreElements()) {
			String prefix = (String)e.nextElement();
			s = s + prefix;
			machs[i] = myclone();
			machs[i].prefixLabels(prefix);
			i++;
			if (i<n) s = s+",";
		}
		//new name
		name = s+"}::"+name;
		//new alphabet
		int alphaN = alphabet.length - 1;
		alphabet = new String[(alphaN*n) +1];
		alphabet[0] = "tau";
		for (int j = 0; j<n ; j++) {
			for (int k = 1; k<machs[j].alphabet.length; k++) {
				alphabet[alphaN*j+k] = machs[j].alphabet[k];
			}
		}
		//additional transitions
		for(int j = 1; j<n; j++) {
			for(int k = 0; k<maxStates; k++) {
				EventState.offsetEvents(machs[j].states[k],alphaN*j);
				states[k] = EventState.union(states[k],machs[j].states[k]);
			}
		}
		prefixMeasureNames( s+"}::" );
	}
	
	/** Prefixes all the names in {@link #measureNames} with the
	 * prefix given, so all n in {@link #measureNames} become
	 * prefix+n.
	 * @param prefix The prefix to add.
	 */
	public void prefixMeasureNames( String prefix ) {
		if( measureNames != null ) {
			for( int i=0; i<measureNames.length; i++ ) {
				measureNames[i] = prefix + measureNames[i];
			}
		}
	}


  /* ---------------------------------------------------------------*/

	private void addtransitions(Relation oni) {
		for (int i=0; i<states.length; i++) {
			IEventState ns = EventState.newTransitions(states[i],oni);
			if (ns!=null)
				states[i] = EventState.union(states[i],ns);
		}
	}

  /* ---------------------------------------------------------------*/

	public boolean hasLabel(String label) {
		for (int i = 0; i<alphabet.length ; ++i)
			if (label.equals(alphabet[i])) return true;
		return false;
	}
    
	public boolean usesLabel(String label) {
		if (!hasLabel(label)) return false;
		int en = eventNo(label);
		for (int i = 0; i<states.length; ++i) {
			if (EventState.hasEvent(states[i],en)) return true;
		}
		return false;
	}
    
  /* ---------------------------------------------------------------*/

	public boolean isSequential() {
		return endseq >=0;
	}
    
	public boolean isEnd() {
		return maxStates == 1 && endseq == 0;
	}
    
  /*----------------------------------------------------------------*/
  
   public static CompactState sequentialCompose(Vector seqs) {
		if (seqs==null) return null;
		if (seqs.size()==0) return null;
		if (seqs.size()==1) return (CompactState)seqs.elementAt(0);
		CompactState machines[] = new CompactState[seqs.size()];
		machines = (CompactState[])seqs.toArray(machines);
		CompactState newMachine =  new CompactState();
		newMachine.alphabet = sharedAlphabet(machines);
		newMachine.maxStates = seqSize(machines);
		newMachine.states = new EventState[newMachine.maxStates];
		int offset = 0;
		for (int i=0; i<machines.length; i++ ) {
			boolean last = (i==(machines.length-1));
			copyOffset(offset,newMachine.states,machines[i],last);
			if (last) 	newMachine.endseq = machines[i].endseq+offset;	
			offset +=machines[i].states.length;
		}			 				
	  return newMachine;
   }
   
   /*----------------------------------------------------------------*/
  
   public void expandSequential(Hashtable inserts) {
	  int ninserts = inserts.size();
	  CompactState machines[] = new CompactState[ninserts+1];
	  int insertAt[] = new int[ninserts+1];
	  machines[0] = this;
	  int index = 1;
	  Enumeration e = inserts.keys();
	  while(e.hasMoreElements()) {
		  Integer ii = (Integer)e.nextElement();
		  CompactState m = (CompactState) inserts.get(ii);
		  machines[index] = m;
		  insertAt[index] = ii.intValue();
		  ++index;
	  }
	  /*
	  System.out.println("Offsets ");
	  for (int i=0; i<machines.length; i++) {
		  machines[i].printAUT(System.out);
		  System.out.println("endseq "+machines[i].endseq);
	  }
	  */
		//newalphabet
		alphabet = sharedAlphabet(machines);
		//copy inserted machines
		for (int i=1; i<machines.length; ++i) {
		int offset = insertAt[i];
			for (int j = 0; j<machines[i].states.length; ++j) {
				states[offset+j] = machines[i].states[j];
			}
		}
   }


  /*
  *   compute size of sequential composite
  */
  private static int seqSize(CompactState[] sm) {
	 int length = 0;
	 for (int i=0; i<sm.length; i++ ) 
			length+=sm[i].states.length;
	 return length;
  }
  
  private static void copyOffset(int offset, IEventState[] dest, CompactState m, boolean last ) {
	 for(int i = 0; i<m.states.length; i++) {
		  if (!last)
			dest[i+offset] = EventState.offsetSeq(offset,m.endseq,m.maxStates+offset,m.states[i]);
		  else
			dest[i+offset] = EventState.offsetSeq(offset,m.endseq,m.endseq+offset,m.states[i]);
	 }
  }
  	 	    
  public void offsetSeq(int offset, int finish) {
	 for (int i=0; i<states.length; i++) {
		 EventState.offsetSeq(offset,endseq,finish,states[i]);
	 }
  }

	/* 
	* create shared alphabet for machines & renumber acording to that alphabet
	*/
	private static String [] sharedAlphabet(CompactState[] sm) {
		  // set up shared alphabet structure
	  Counter newLabel    = new Counter(0);
	  Hashtable actionMap = new Hashtable();
	  for (int i=0; i<sm.length; i++ ) {
		  for (int j = 0; j < sm[i].alphabet.length; j++) {
			  if (!actionMap.containsKey(sm[i].alphabet[j])) {
				  actionMap.put(sm[i].alphabet[j],newLabel.label());
			  } 
		  }
	  }
	  // copy into alphabet array
	  String [] actionName = new String[actionMap.size()];
	  Enumeration e = actionMap.keys();
	  while (e.hasMoreElements()) {
		  String s = (String)e.nextElement();
		  int index =((Integer)actionMap.get(s)).intValue();
		  actionName[index] =s;
	  }
	  // renumber all transitions with new action numbers
	  for (int i=0; i<sm.length; i++ ) {
		  for(int j=0; j<sm[i].maxStates;j++) {
			  IEventState p = sm[i].states[j];
			  while(p!=null) {
				  IEventState tr = p;
				  tr.setEvent(((Integer)actionMap.get(sm[i].alphabet[tr.getEvent()])).intValue() );
				  while (tr.getNondet()!=null) {
					  tr.getNondet().setEvent( tr.getEvent() );
					  tr = tr.getNondet();
				  }
				  p=p.getList();
			  }
		  }
	  }
	  return actionName;
          
	}
	
	/** implementation of StochasticAutomata interface **/
	
	public static byte[] encode(int state) {
		 byte[] code = new byte[4];
		 for(int i=0; i<4; ++i) {
			   code[i] |= (byte)state;
			   state = state >>>8;
		  }
		  return code;
	}
				
  public static int decode( byte[] code){
		 int x =0;
		 for(int i=3; i>=0; --i) {
			   x |= (int)(code[i])& 0xFF;
			   if (i>0) x = x << 8;
		  }
		  return x;

  }
  
	public String[] getAlphabet() {return alphabet;}
	
	public Vector getAlphabetV() {
		  Vector v = new Vector(alphabet.length-1);
		  for (int i=1; i<alphabet.length; ++i)
				v.add(alphabet[i]);
		  return v;
	}
	
	//TODO: check if it's in this method, on the add part, we need to pass the weights - Gena’na.
	public MyList getTransitions(byte[] fromState) {
		MyList tr = new MyList();
		int state;
		if (fromState == null)
			state = Declaration.ERROR;
	  else
		 state = decode(fromState);
		if (state<0 ||state>=maxStates) return tr;
		if (states[(int)state]!=null)
		for(Enumeration e = states[state].elements(); e.hasMoreElements();) {
				IEventState t = (IEventState)e.nextElement();
				tr.add(state,encode(t.getNext()),t.getEvent());
		}
		return tr;
	}
	
	public String getViolatedProperty() {return null;}

	//returns shortest trace to  state (vector of Strings)
	public Vector getTraceToState(byte[] from, byte[] to){
		IEventState trace = new EventState(0,0);
	int result = EventState.search(trace,states,decode(from),decode(to),-123456);
	return EventState.getPath(trace.getPath(),alphabet);
	}

	public boolean isEND(byte[] state) {
	   return decode(state) == endseq;
  	}

	//	return the number of the END state  	
  	public boolean END(byte[] state) {
	   return decode(state) == endseq;
  	}
	
	//return whether or not state is accepting
	public boolean isAccepting(byte[] state)  {
		return isAccepting(decode(state));
	}
	
	//return the number of the START state
	public byte[] START() {
		 return encode(0);
	}

  //set the Stack Checker for partial order reduction
	public void setStackChecker(StackCheck s){} // null operation

  //returns true if partial order reduction
	public boolean isPartialOrder(){return false;}
	
	//diable partial order
	public void disablePartialOrder() {}
	
	//enable partial order
	public void enablePartialOrder() {}

	/*-------------------------------------------------------------*/
	// is state accepting
	public boolean isAccepting(int n) {
		  if (n<0 || n>=maxStates) return false;
		  return EventState.isAccepting(states[n],alphabet);
	}
	
	public BitSet accepting() {
		  BitSet b = new BitSet();
		  for (int i = 0; i<maxStates; ++i) 
			   if (isAccepting(i)) b.set(i);
			return b;
	}

	/*--------------------------------------------------------------
	 *-- Stochastic Extensions                                    --
	 *--------------------------------------------------------------*/

	/** gives a string representation of the compact state machine */
	public String toString() {
		return "CompactState: " + name + '\n'
			+ "alpha: " + alphabetToString() + '\n'
			+ "measures: " + measuresToString() + '\n'
			+ "transitions:\n" + transitionsToString();
	}

	private String alphabetToString() {
		StringBuffer s = new StringBuffer();
	
		if( alphabet != null ) {
			for( int i=0; i<alphabet.length; i++ ) {
				s.append( i );
				s.append( ':' );
				s.append( alphabet[i] );
				if( i<alphabet.length-1 ) s.append( ", " );
			}
		}
	
		return s.toString();
	}

	private String measuresToString() {
		StringBuffer s = new StringBuffer();
	
		if( measureNames != null ) {
			for( int i=0; i<measureNames.length; i++ ) {
				s.append( i );
				s.append( ':' );
				s.append( measureNames[i] );
				s.append( '(' );
				s.append( measureTypes[i].toString() );
				s.append( ')' );
				if( i<measureNames.length-1 ) s.append( ", " );
			}
		}
	
		return s.toString();
	}

	private String transitionsToString() {
		StringBuffer s = new StringBuffer();
	
		// for each state
		for( int i=0; i<maxStates; i++ ) {
			s.append( i );
			s.append( ':' );
/*
			if( probTransitions != null && probTransitions[i] != null ) {
				for( Iterator e = probTransitions[i].iterator(); e.hasNext(); ) {
					s.append( e.next() );
					if( e.hasNext() )
					s.append( ',' );
				}
			} else
*/
			if( states != null && states[i] != null ) {
				Enumeration e = states[i].elements();
				while( e.hasMoreElements() ) {
					s.append( e.nextElement() );
					if( e.hasMoreElements() )
					s.append( ',' );
				}
			}
			s.append( '\n' );
		}
	
		return s.toString();
	}


	class Transition {
		int from;
		int to;
		int event;
		public String toString() {
			return "<" + from + "--" + event + "->" + to + ">";
		}
		/** Renumbers states in the transition using the given map.
		 * @param map The map from old state numbers to new state
		 * numbers.
		 */
		public void renumberStates( MyIntHash map ) {
			from = map.get( from );
			to = map.get( to );
		}
	}
	
	class ProbClockTransition extends Transition {

		double probability;
		
		/** A collection of {@link ic.doc.ltsa.sim.Condition}
			objects representing the conditions on this transition. */
		Condition condition;
		
		/** A collection of {@link ic.doc.ltsa.sim.Action}
			objects representing the actions on this transition. */
		Action action;

		public String toString() {
			return "<" + from + "--(" + condition + "," + event + "," + probability + ","  
			+ action + ")->" + to + ">";
		}

	}
    
	/** collection of all probabilistic transitions ({@link
	ProbTransition}, indexed by state number. */
	//protected Collection[] probTransitions;

	/** intialises the normal and probabilistic transition relations
	 * based on the maximum number of states. {@link #maxStates},
	 * {@link #states} and {@link #probTransitions} are appropriately
	 * set.
	 * @param s The number of states.
	 */
	void init( int s ) {
		maxStates = s;
		states = new IEventState[s];
	}

/*
	void addProbTransition( int from, int to, int event, double prob ) {
		addProbTransition(from,to,event,prob,null,null);
	}
*/

	/** adds a probabilistic transition to this automata.
	@param from the index of the from state
	@param to the index of the to state
	@param event the index of the event
	@param prob the relative probability of this transition
	*/
/*
	void addProbTransition( int from, int to, int event, Condition c, Action a, double prob ) {
		ProbClockTransition t = new ProbClockTransition();

		if( probTransitions[from] == null )
			probTransitions[from] = new Vector();
	
		t.from = from;
		t.to = to;
		t.event = event;
		t.condition = c;
		t.action = a;
		t.probability = prob;
		probTransitions[from].add( t );
	}
*/

	// StochasticAutomata implementation
	public boolean isProbabilisticState( byte[] state ) {
		return isProbState(decode(state));
/*
		return probTransitions != null 
		    && probTransitions[s] != null
		    && probTransitions[s].size() > 0;
*/
	}

	public ProbabilisticTimedTransitionList getProbTimedTransitions( byte[] s ) {
		int state = decode( s );
		EventState pTrans;
		ProbabilisticTimedTransitionList ls = new ProbabilisticTimedTransitionList();
		pTrans = (EventState)states[state];
		while (pTrans != null) {
			ls.add( state, encode( pTrans.getNext() ), pTrans.getEvent(), pTrans.getCondition(), pTrans.getAction(), pTrans.getProb() );
			pTrans = (EventState)pTrans.getList();
		}
		return ls;
	}

/*
	public ProbabilisticTimedTransitionList getProbabilisticTransitions( byte[] s ) {
		int state = decode( s );
		EventState pTrans;
		ProbabilisticTimedTransitionList ls = new ProbabilisticTimedTransitionList();
		pTrans = states[state];
		while (pTrans != null) {
			ls.add( state, encode( pTrans.next ), pTrans.event, pTrans.condition, pTrans.action, pTrans.prob );
			pTrans = pTrans.list;
		}
*/
/*
		pTrans = probTransitions[state];
		if( pTrans != null ) {
			Iterator i = pTrans.iterator();
			while( i.hasNext() ) {
				ProbClockTransition t = (ProbClockTransition) i.next();
				ls.add( state, encode( t.to ), t.event, t.condition, t.action, t.probability );
			}
			ls.normalise();
		}
*/	
/*
		return ls;
	}
*/
/*
	public ProbabilisticTimedTransitionList getTimedTransitions( byte[] s ) {
		int state = decode( s );
		Enumeration tr;
		ProbabilisticTimedTransitionList ls = new ProbabilisticTimedTransitionList();
	
		tr = states[state].elements();
		while( tr.hasMoreElements() ) {
			EventState t = (EventState) tr.nextElement();
			ls.add( state, encode( t.next ), t.event,
				t.condition, t.action );
		}
	
		return ls;
	}
*/
	/**
	 * Adds an offset to all clock identifiers, and another offset to
	 * all measure identifiers. used when composing multiple compact
	 * state machines together.
	 * @param clockOffset The offset to add to all clock ids.
	 * @param measureOffset The offset to add to all measure ids.
	 */
	public void applyOffsets( int clockOffset, int measureOffset ) {
		// basically, walk over the transition list and apply the
		// offset to each condition and action
	
		// for efficiency
		if( !(clockOffset==0 && measureOffset==0) ) {
			for( int i=0; i<states.length; i++ ) {
				if( states[i] != null ) {
					for( Enumeration e = states[i].elements();e.hasMoreElements(); ) {
						EventState t = (EventState) e.nextElement();
						applyOffsets( t, clockOffset, measureOffset );
					}
				}
			}
		}
	}

	/** Offsets the clocks and measures for a single transition. */
	private void applyOffsets( EventState trans, int cloffset, int moffset ) {
	    
		if( trans.getCondition() != null )
			trans.getCondition().addClockOffset( cloffset );
		if( trans.getAction() != null )
			trans.getAction().applyOffsets( cloffset, moffset );
	}

	public int getMaxClockIdentifier() {
	// basically, walk over the transition list and get the maximum from
	// each transition

	int max = -1;

	for( int i=0; i<states.length; i++ ) {
		if( states[i] != null ) {
			for( Enumeration e = states[i].elements(); e.hasMoreElements(); ) {
				IEventState t = (IEventState) e.nextElement();
				int localMax = getMaxClockIdentifier( t );
				max = (localMax > max) ? localMax : max;
			}
		}
	}
	return max;
	}

	/** Returns the maximum clock identifier found in an individual
	transition. */
	private int getMaxClockIdentifier( IEventState trans ) {
	
	    EventState xTrans = (EventState)trans;	    
	    
	    int max = -1;
	
		if( xTrans.getCondition() != null ) {
			int m = xTrans.getCondition().getMaxClockIdentifier();
			if( m > max ) max = m;
		}
	
		if( xTrans.getAction() != null ) {
			int m = xTrans.getAction().getMaxClockIdentifier();
			if( m > max ) max = m;
		}
	
		return max;
	}

	public String[] getMeasureNames() { return measureNames; }
	public Class[] getMeasureTypes() { return measureTypes; }
	
	public boolean isProbState(int state) {
		IEventState e = states[state];
		while (e != null) {
			if (e.getProb()>0) return true;
			e = e.getList();
		}
		return false;
	}
	
	public Vector getActions() {
		Vector v = new Vector();
		for (int i = 0; i<states.length; i++) {
			IEventState p = states[i];
			while (p!=null) {
				v.add(((EventState)p).getAction());
				p = p.getList();
			}
		}
		return v;
	}

    public void setName(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }

    public int getMaxStates() {

        return maxStates;
    }

    public void setStates(IEventState[] states) {

        this.states = states;
    }

    public IEventState[] getStates() {

        return states;
    }

    public int getEndseq() {

        return endseq;
    }


    public void setPartialOrderReduction(boolean arg0) {

        // TODO Auto-generated method stub
        
    }

    public void setPreserveObsEquiv(boolean arg0) {

    }

    public void unMakeProperty() {
     
        System.err.println( "unmakeProperty() non implemented in CompactState ");
    }
    /*
    // return true if this compact state and mach has the same traces
    // with the same probabilities for all traces with length not greater than depth
    public boolean isTraceEquivalent(CompactState mach, int depth) {
    	
    	// a stack of event iterators 
    	// iterators keep track of events yet to be considered 
    	Stack< Iterator<Integer> > eventStack = new Stack< Iterator<Integer> >();
    	// a stack of maps relating states to the probability of the traces to it
    	Stack< HashMap<Integer,Double> > mapStack1 = new Stack< HashMap<Integer,Double> >();
    	Stack<Integer> mapStack2 = new Stack< HashMap<Integer,Double> >();
    	
    	// depth of recursion
    	int level = 1;
    	
    	//-----------------------------------------------------
    	// initialisation of the stack of sets of events
    	
    	// store the union of events of this compact state and mach
    	Set<Integer> initEvent = new HashSet<Integer>();
    	
    	// for all transitions in start state of this compact state
    	for(Enumeration e = states[0].elements(); e.hasMoreElements();) {
			IEventState st = (IEventState)e.nextElement();
			initEvent.add( st.getEvent() );
    	}
    	// for all transitions in start state of mach
    	for(Enumeration e = mach.getStates()[0].elements(); e.hasMoreElements();) {
			IEventState st = (IEventState)e.nextElement();
			initEvent.add( st.getEvent() );
    	}
    	// saves the iterator on the stack
    	eventStack.push( initEvent.iterator() );
    	
    	//------------------------------------------------------
    	// initialisation of the stack of maps
    	
    	HashMap<Integer,Double> initMap = new HashMap<Integer,Double>();
    	initMap.put(new Integer(0), new Double(1));
    	mapStack.push(initMap);
    	
    	//------------------------------------------------------
    	// depth-first traversal
    	
    	while( !mapStack.isEmpty() ) {
    		
    		// if all events of this map are considered
    		if( !eventStack.peek().hasNext() ) {
    			eventStack.pop(); // remove event iterator
    			mapStack.pop();   // remove map
    			level--;
    			continue;
    		}
    		
    		// else get the next event to be considered
    		int event = eventStack.peek().next();
    			
    		HashMap<Integer,Double> map = mapStack.peek();
    		HashMap<Integer,Double> newMap = new HashMap<Integer,Double>();
    		
    		// for all states in the current map
    		for( Map.Entry<Integer,Double> me : map.entrySet() ) {
    			 // get the probability of the state
    			double prob = me.getValue();
    	        
    			// for all transitions of the state
    			for(Enumeration e = states[me.getKey()].elements(); e.hasMoreElements();) {
					IEventState st = (IEventState)e.nextElement();

					// if the transition has "event"
    				if (st.getEvent() == event) {
    					// get the next state and probability of this event
    					int nextState = st.getNext();
    					Double newProb = newMap.get(nextState);
    					
    					if( level < depth ) {
    						// if the new state has not been stored
    						if( newProb == null)
    							newMap.put( st.getNext(), prob*st.getProb() );
    						else 
    							newMap.put( st.getNext(), newProb + prob*st.getProb() );
    					}
    				}
    	        }
    		}
    			
    		if( level < depth ){
    			Set<Integer> newEvents = new HashSet<Integer>();
    			// for all new states
    			for( Integer i : newMap.keySet() ) {
    				// for all transitions in the state
    		    	for(Enumeration e = states[i].elements(); e.hasMoreElements();) {
    					IEventState st = (IEventState)e.nextElement();
    					// add it to the set of new events
    					newEvents.add( st.getEvent() );
    		    	}
    			}
    			
    			// store the information for next level of recursion
    			eventStack.push( newEvents.iterator() );
    			mapStack.push(newMap);
    			level++;
    		}
    	}    	
    	return true;
    }
*/
}