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

import ic.doc.ltsa.common.infra.Relation;
import ic.doc.ltsa.common.iface.IEventState;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.simulation.sim.*;

import java.util.*;
import java.io.PrintStream;

/** This is a linked list of transitions of a CompactState object.
 *  Every transition holds the properties of the current transition
 *  and one or several pointers to other transitions.
 * 
 *  Here, transitions can be probabilistic and hold clock setting and
 *  testing actions and conditions.
 * 
 *  Transitions are always held in event order, smallest first. The list
 *  pointer points to the next transition in the list.
 *  If the system is non-deterministic, all the probabilities are 0,
 *  and the nondet pointer is used for alternative transitions with
 *  the same transition element.
 *  
 *  In this picture, stars are EventState objects, | signs are nondet links
 *  and - signs are list links.
 * 
 *  * - * - * - * - *
 *  |   |       |
 *  *   *       *
 *      |
 *      *
 *  
 *  If the system is probabilistic, all probabilities are > 0, and
 *  the nondet pointer is unused.
 * 
 *  * - * - * - *
 *    
 *  In the future, these two implementations should be separated into
 *  separate classes with a common interface. 
 *  
 * */
public class EventState implements IEventState {
	
    int event;
    int next;
    int machine;
    
    /** The probability on this transition. */
    double prob = 0;
    
	/** The condition on this transition. May be singleton or composite. */
	Condition condition;
    
	/** The action to be performed on this transition. May be singleton or composite. */
	Action action;

    IEventState list;  //used to keep list in event order, TAU first
	// nondet is ONLY used in non-deterministic systems, in probabilistic systems
	// nondet is NOT used, even if there is more than one transition with the
	// same transition label. Instead, they appear after each other in the list.
 
    IEventState nondet;//used for additional non-deterministic transitions
    IEventState path;  //used by analyser & by minimiser

    public EventState(int e, int i ) {
        event = e;
        next = i;
    }
    
	/**
	 * Creates a new event-state tuple, with the given event and
	 * follower state, condition and action.
	 * @param e The event number.
	 * @param i The follower state number.
	 * @param condition The condition on the transition, may be
	 * composite or singleton.
	 * @param action The action to be performed on the transition, may
	 * be composite or singleton.
	 */
	public EventState(int e, int i,Condition condition, Action action, double prob ) {
		this( e, i );
		this.condition = condition;
		this.action = action;
		this.prob = prob;
	}

	/** get a list of all labels used in this state */
	public static int[] getLabelArray(IEventState head) {
		IEventState p = head;
		int[] temp = new int[100];
		int pos = 0;
		while (p!=null) {
			temp[pos] = p.getEvent();
			pos++;
			p = p.getList();
		}
		int[] t = new int[pos];
		for (int i = 0; i<pos; i++)
			t[i] = temp[i];
		return t;
	}

    public Enumeration elements() {
        return new EventStateEnumerator(this);
    }
    
	/** gives a string representation of the transition at the head of
	 * the list. */
	public String toString() {
		return "--(" + condition + "," + event + "," + action + "," + prob +")->" + next;
	}

    // the following is not very OO but efficient
    // duplicates are discarded
    public static IEventState add(IEventState head, IEventState tr) {
        
        EventState xTr = (EventState)tr;
        
        // add at head
        if (head==null || tr.getEvent() < head.getEvent()) {
            tr.setList( head );
            return tr;
        }
        EventState p = (EventState)head;
        while (p.getList() !=null && p.getEvent()!= tr.getEvent() && tr.getEvent() >= p.getList().getEvent()) 
        		p=(EventState)p.getList();
        if (p.getEvent() == tr.getEvent() && p.getProb() ==0) { //add to nondet
            EventState q = p;
            do {
               if (equal(q,xTr)) return head;
				if (q.getNondet() !=null) q = (EventState)q.getNondet();
             } while (q.getNondet() != null);
            q.setNondet( tr );
        } else {
        	if (p.getProb() > 0 && tr.getProb() > 0
        	    && tr.getEvent() == p.getEvent()
        	    && tr.getNext() == p.getNext()
        	    && ( (p.getCondition() == null && xTr.getCondition()==null) ||
        	         (p.getCondition() !=null && p.getCondition().equals(xTr.getCondition()))
        	       )
        	   ) {
        		// transition can be merged
        		p.setAction( CompositeAction.add(p.getAction(),xTr.getAction()) );
        		p.setProb( p.getProb() + tr.getProb() );
        	}
        	else {
        		// add after p
	            tr.setList( p.getList() );
	            p.setList( tr );
        	}
        }
        return head;
    }
    
    private static boolean equal (EventState a, EventState b) {
		return
			a.getEvent() == b.getEvent() &&
			a.getNext() == b.getNext() &&
			(     (a.getCondition() == null && b.getCondition() == null)
			   || (a.getCondition() !=null && a.getCondition().equals(b.getCondition()))
			) &&
			(    (a.getAction() == null && b.getAction() == null)
			  || (a.getAction() !=null && a.getAction().equals(b.getCondition()))
			)
			;
    }

    public static IEventState remove(IEventState head, IEventState tr) {
        
        EventState xTr = (EventState)tr;
        EventState xHead = (EventState)head;
        
        //remove from head
        if (head==null) return head;
        // now check whether conditions and actions are the same..
        if (equal(xHead,xTr)) {
            if (head.getNondet()==null)
                return head.getList();
            else {
                head.getNondet().setList( head.getList() );
                return head.getNondet();
            }
        }
        IEventState p = head;
        IEventState plag = head;
        while(p!=null) {
            EventState q = (EventState)p;
            IEventState qlag = p;
            while (q!=null) {
                if ( equal(q,xTr) ) {
                    if(p==q) { //remove from head of nondet
                        if(p.getNondet()==null) {
                           plag.setList( p.getList() );
                           return head;
                        } else {
                           p.getNondet().setList( p.getList() );
                           plag.setList( p.getNondet() );
                           return head;
                        }
                    } else {
                        qlag.setNondet( q.getNondet() );
                        return head;
                    }
                }
                qlag = q;
                q=(EventState)q.getNondet();
            }
            plag=p;
            p=p.getList();
        }
        return head;
    }
    
    public static void printAUT(IEventState head, int from, String[] alpha, PrintStream output) {
        IEventState p =head;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                output.print("("+from+","+ alpha[q.getEvent()] + "," + q.getNext() +")\n");
                q=q.getNondet();
            }
            p=p.getList();
        }
    }

    public static int count(IEventState head) {
        IEventState p =head;
        int n =0;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                n++;
                q=q.getNondet();
            }
            p=p.getList();
        }
        return n;
    }

    public static boolean hasState(IEventState head, int next) {
        IEventState p =head;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                if (q.getNext() == next) return true;
                q=q.getNondet();
            }
            p=p.getList();
        }
        return false;
    }
  
    public static IEventState offsetSeq(int off, int seq, int max, IEventState head) {
        IEventState p =head;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
            	  if (q.getNext()>=0) {
            	  	if (q.getNext()==seq) 
            	  		  q.setNext( max );
            	  	else
            	  		  q.setNext( q.getNext() + off );
            	  }
                q=q.getNondet();
            }
            p=p.getList();
        }
        return head;
    }


    public static int toState(IEventState head, int next) {
        IEventState p =head;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                if (q.getNext() == next) return q.getEvent();
                q=q.getNondet();
            }
            p=p.getList();
        }
        return -1;
    }

    public static int countStates(IEventState head, int next) {
        IEventState p = head;
        int result = 0;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                if (q.getNext() == next) result++;
                q=q.getNondet();
            }
            p=p.getList();
        }
        return result;
    }

   public static boolean hasEvent(IEventState head, int event) {
        IEventState p =head;
        while(p!=null) {
             if (p.getEvent() == event) return true;
             p=p.getList();
        }
        return false;
    }
    
    public static boolean isAccepting(IEventState head, String[] alphabet) {
        IEventState p =head;
        while(p!=null) {
             if (alphabet[p.getEvent()].charAt(0)=='@') return true;
             p=p.getList();
        }
        return false;
    }

    
    public static IEventState firstCompState(IEventState head, int event, int[] state) {
    	   IEventState p =head;
        while(p!=null) {
             if (p.getEvent() == event) {
             	  state[p.getMachine()] = p.getNext();
             	  return p.getNondet();
             }
             p=p.getList();
        }
        return null;
    }
    
    public static IEventState moreCompState(IEventState head, int[] state) {
    	   state[head.getMachine()] = head.getNext();
    	   return head.getNondet();
    }

    public static boolean hasTau(IEventState head) {
        if (head==null) return false;
        return (head.getEvent() == Declaration.TAU);
    }
    
    public static boolean hasOnlyTau(IEventState head) {
        if (head==null) return false;
        return (head.getEvent() == Declaration.TAU && head.getList() == null);
    }
    
    public static boolean hasOnlyTauAndAccept(IEventState head, String[] alphabet) {
        if (head==null) return false;
        if (head.getEvent() != Declaration.TAU) return false;
        if (head.getList() == null) return true;
        if (alphabet[head.getList().getEvent()].charAt(0)!='@') return false;
        return (head.getList().getList() == null);
    }
    
    //precondition is "hasOnlyTauAndAccept"
    public static IEventState removeAccept(IEventState head) {
    	   head.setList( null );
    	   return head;
    }
    	   
    public static IEventState addNonDetTau(IEventState head, IEventState states[], BitSet tauOnly) {
    	   IEventState p =head;
    	   IEventState toAdd = null;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                if (q.getNext() >0 && tauOnly.get(q.getNext())) {
                	 int nextS[] =  nextState(states[q.getNext()],Declaration.TAU);
                	 q.setNext( nextS[0] );  //replace transition to next 
                	 for (int i=1; i<nextS.length; ++i) {
                	 	   toAdd = add(toAdd,new EventState(q.getEvent(),nextS[i]));
                	 }              	
                }
                q=q.getNondet();
            }
            p=p.getList();
        }
        if (toAdd == null)
        	return head;
        else
         return union(head,toAdd);
    }


    public static boolean hasNonDet(IEventState head) {
        IEventState p =head;
        while(p!=null) {
             if (p.getNondet() != null) return true;
             p=p.getList();
        }
        return false;
    }
    
    public static int[] localEnabled(IEventState head) {
    	    IEventState p = head;
    	    int n = 0;
    	    while(p!=null) {++n; p=p.getList();}
    	    if (n==0) return null;
    	    int[] a = new int[n];
    	    p = head; n=0;
    	    while(p!=null) {a[n++]=p.getEvent(); p=p.getList();}
    	    return a;
    }
			  
    public static void hasEvents(IEventState head, BitSet actions) {
        IEventState p =head;
        while(p!=null) {
             actions.set(p.getEvent());
             p=p.getList();
        }
    }

    public static int[] nextState( IEventState head, int event) {
        IEventState p = head;
        while(p!=null) {
            if (p.getEvent() == event) {
                IEventState q = p; int size=0;
                while(q!=null) {q=q.getNondet(); ++size;}
                q = p;
                int n[] = new int[size];
                for (int i=0; i<n.length; ++i) {n[i]=q.getNext(); q=q.getNondet();}
                return n;
            }
            p=p.getList();
        }
        return null;
    }

    public static IEventState renumberEvents(IEventState head, Hashtable oldtonew) {
        IEventState p =head;
        IEventState newhead =null;
        while(p!=null) {
            EventState q = (EventState)p;
            while (q!=null) {
                int event = ((Integer)oldtonew.get(new Integer(q.getEvent()))).intValue();
                newhead = add(newhead,new EventState(event,q.getNext(),q.getCondition(),q.getAction(),q.getProb()));
                q = (EventState)q.getNondet();
            }
            p=p.getList();
        }
        return newhead;
    }

    public static IEventState newTransitions(IEventState head, Relation oldtonew) {
        IEventState p = head;
        IEventState newhead =null;
        while(p!=null) {
            EventState q = (EventState)p;
            while (q!=null) {
                Object o = oldtonew.get(new Integer(q.getEvent()));
                if (o!=null) {
                    if (o instanceof Integer) {
                    	// one to two, so probability of both halves
                    	q.setProb( q.getProb() / 2 );
                    	newhead = add(newhead,new EventState(((Integer)o).intValue(),q.getNext(),q.getCondition(),q.getAction(),q.getProb()));
                    } else {
                        Vector v = (Vector)o;
                        // split probability between new transitions equally
                        double newprob = q.getProb()/v.size();
                        for (Enumeration e = v.elements();e.hasMoreElements();) {
                            newhead = add(newhead,new EventState(((Integer)e.nextElement()).intValue(),q.getNext(),q.getCondition(),q.getAction(),newprob));
                        }
                    }
                }
                q=(EventState)q.getNondet();
            }
            p=p.getList();
        }
        return newhead;
    }


    public static IEventState offsetEvents(IEventState head, int offset) {
        IEventState p = head;
        IEventState newhead = null;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                q.setEvent( q.getEvent() ==0 ? 0 : q.getEvent() + offset );
                q=q.getNondet();
            }
            p=p.getList();
        }
        return newhead;
    }


    public static IEventState renumberStates(IEventState head, Hashtable oldtonew) {
        IEventState p =head;
        IEventState newhead =null;
        while(p!=null) {
            EventState q = (EventState)p;
            while (q!=null) {
                int next = q.getNext()<0?Declaration.ERROR:((Integer)oldtonew.get(new Integer(q.getNext()))).intValue();
                newhead = add(newhead,new EventState(q.getEvent(),next,q.getCondition(),q.getAction(),q.getProb()));
                q=(EventState)q.getNondet();
            }
            p=p.getList();
        }
        return newhead;
    }
    
    public static IEventState renumberStates(IEventState head, MyIntHash oldtonew) {
        IEventState p =head;
        IEventState newhead =null;
        while(p!=null) {
            EventState q = (EventState)p;
            while (q!=null) {
                int next = q.getNext()<0?Declaration.ERROR:oldtonew.get(q.getNext());
                newhead = add(newhead,new EventState(q.getEvent(),next,q.getCondition(),q.getAction(),q.getProb()));
                q=(EventState)q.getNondet();
            }
            p=p.getList();
        }
        return newhead;
    }


    public static IEventState addTransToError(IEventState head,int last) {
        IEventState p =head;
        IEventState newhead = null;
        if (p!=null && p.getEvent() == Declaration.TAU) p = p.getList(); //skip tau
        int index =1;
        while(p!=null) {
            if (index<p.getEvent() ) {
                for (int i= index; i<p.getEvent(); i++)
                    newhead = add(newhead,new EventState(i,Declaration.ERROR));

            }
            index=p.getEvent()+1;
            EventState q = (EventState)p;
            while (q!=null) {
                newhead = add(newhead,new EventState(q.getEvent(),q.getNext(),q.getCondition(),q.getAction(),q.getProb()));
                q=(EventState)q.getNondet();
            }
            p=p.getList();
        }
        for (int i= index; i<last; i++)
                    newhead = add(newhead,new EventState(i,Declaration.ERROR));
        return newhead;
    }

    //remove tau actions
    public static IEventState removeTau(IEventState head) {
        if (head==null) return head;
        if (!(head.getEvent() == Declaration.TAU && head.getProb()==0)) return head;
        return head.getList();
    }

    //add states reachable by next from events
    public static IEventState tauAdd(IEventState head, IEventState[] T) {
        IEventState p = head;
        IEventState added = null;
        if (p!=null && p.getEvent() == Declaration.TAU) p = p.getList(); //skip tau
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                if (q.getNext() !=Declaration.ERROR) {
                    IEventState t = T[q.getNext()];
                    while(t!=null) {
                        added = push(added,new EventState(p.getEvent(),t.getNext()));
                        t=t.getNondet();
                    }
                }
                q = q.getNondet();
            }
            p=p.getList();
        }
        while (added!=null) {
            head = add(head,added);
            added = pop(added);
        }
        return head;
    }

    public static void setActions(IEventState head, BitSet b) {
        IEventState p =head;
        while(p!=null) {
            b.set(p.getEvent());
            p=p.getList();
        }
    }

    // add actions reachable by tau
    public static IEventState actionAdd(IEventState head, IEventState[] states) {
        if (head == null || head.getEvent()!=Declaration.TAU) return head; //no tau
        IEventState tau = head;
        while (tau != null) {
            if (tau.getNext()!=Declaration.ERROR) head = union(head,states[tau.getNext()]);
            tau=tau.getNondet();
        }
        return head;
    }

    // to = to U from
    public static IEventState union(IEventState to, IEventState from){
        IEventState res = to;
        IEventState p =from;
        while(p!=null) {
            EventState q = (EventState)p;
            while(q!=null) {
				Condition condition = null;
				Action action = null;
				if( q.getCondition() != null )
					condition = q.getCondition().cloneCondition();
				if( q.getAction() != null )
					action = q.getAction().cloneAction();
                res = add(res,new EventState(q.getEvent(),q.getNext(),condition,action,q.getProb()));
                q=(EventState)q.getNondet();
            }
            p =  p.getList();
        }
        return res;
    }
    
	public static EventState transpose(IEventState from) {
		if (from==null) return (EventState)from;
		if (from.getProb() == 0)
			return transposeNonDet(from);
		else
			return transposeProb(from);
	}

    //normally, EventState lists are sorted by event with
    //the nondet list containing lists of different next states
    // for the same event
    // transpose creates a new list sorted by next
	private static EventState transposeNonDet(IEventState from) {
		IEventState res = null;
		IEventState p = from;
		while(p!=null) {
			EventState q = (EventState)p;
			while(q!=null) {
				res = add(res,new EventState(q.getNext(),q.getEvent(),q.getCondition(),q.getAction(),q.getProb())); //swap event & next
				q=(EventState)q.getNondet();
			}
			p = p.getList();
		}
		// now walk through the list a swap event & next back again
		p =res;
		while(p!=null) {
			IEventState q = p;
			while (q!=null) {
				int n = q.getNext(); q.setNext( q.getEvent() ); q.setEvent( n );
				q=q.getNondet();
			}
			p=p.getList();
		}
		return (EventState)res;
	}

	/** does the same as transpose, but with a probabilistic system
	 *  In a probabilistic system, the nondet pointer is not used,
	 *  instead you can have several transitions with the same event
	 *  appearing in the list, ordered by the event number.
	 * */
	private static EventState transposeProb(IEventState from) {
		IEventState temp = null;
		EventState p = (EventState)from;
		while (p!=null) {
			// chuan: some modification here 
			EventState q = p;
			while(q!=null) {			
				temp = add(temp,new EventState(q.getNext(),q.getEvent(),q.getCondition(),q.getAction(),q.getProb())); //swap event & next
				q=(EventState)q.getNondet();
			}
			p = (EventState)p.getList();
		}
		// now gather all common next's
		IEventState res = new EventState(temp.getNext(),temp.getEvent(),((EventState)temp).getCondition(),((EventState)temp).getAction(),temp.getProb());
		IEventState pos = res;
		temp = temp.getList();
		boolean gotnext = true;
		while (temp != null) {
			p = (EventState)pos;
			EventState q = p;
			// now we search for as many consecutive transitions as possible
			while (temp!=null && temp.getEvent() == p.getNext()) {
				q.setNondet( new EventState(temp.getNext(),temp.getEvent(),((EventState)temp).getCondition(),((EventState)temp).getAction(),temp.getProb()) );
				q = (EventState)q.getNondet();
				temp = temp.getList();
				gotnext = true;
			}
			// nothing happened, get next
			if (!gotnext) temp = temp.getList();
			gotnext = false;
			if (temp!=null) {
				// initialise next target state
				 pos.setList( new EventState(temp.getNext(),temp.getEvent(),((EventState)temp).getCondition(),((EventState)temp).getAction(),temp.getProb()));
				 pos = pos.getList();
				 temp = temp.getList();
				gotnext = true;
			}
		}
		return (EventState)res;
	}
    
    // only applicable to a transposed list
    // returns set of event names to next state
    public static String[] eventsToNext(IEventState from, String[] alphabet) {
         IEventState q = from;
         int size=0;
         while(q!=null) {q=q.getNondet(); ++size;}
         q = from;
         String s[] = new String[size];
         for (int i=0; i<s.length; ++i) {
              s[i]=alphabet[q.getEvent()]; 
              q=q.getNondet();
         }
         return s;
    }
    
    // only applicable to a transposed list
    // returns set of event names to next state
    // omit accepting label
    public static String[] eventsToNextNoAccept(IEventState from, String[] alphabet) {
         IEventState q = from;
         int size=0;
         while(q!=null) {
         	  if (alphabet[q.getEvent()].charAt(0)!='@') ++size; 
         	  q=q.getNondet();
         }
         q = from;
         String s[] = new String[size];
         for (int i=0; i<s.length; ++i) {
              if (alphabet[q.getEvent()].charAt(0)!='@')
              	  s[i]=alphabet[q.getEvent()]; 
              else
                  --i;
              q=q.getNondet();
         }
         return s;
    }


    /* --------------------------------------------------------------*/
    // Stack using path
    /* --------------------------------------------------------------*/

    private static IEventState push(IEventState head, IEventState es) {
        if (head==null)
            es.setPath( es );
        else
            es.setPath( head );
        return head = es;
    }


    private static boolean inStack(IEventState es) {
        return (es.getPath() !=null);
    }

    private static IEventState pop(IEventState head) {
        if (head==null) return head;
        IEventState es = head;
        head = es.getPath();
        es.setPath( null );
        if (head==es)
            return null;
        else
            return head;
    }

    /*-------------------------------------------------------------*/
    //compute all states reachable from state k
    /*-------------------------------------------------------------*/

    public static IEventState reachableTau(IEventState[] states, int k) {
        IEventState head = states[k];
        if (head==null || head.getEvent() !=Declaration.TAU)
            return null;
        BitSet visited = new BitSet(states.length);
        visited.set(k);
        IEventState stack=null;
        while (head!=null) {
            stack = push(stack,head);
            head = head.getNondet();
        }
        while(stack!=null) {
            int j = stack.getNext();
            head = add(head,new EventState(Declaration.TAU,j));
            stack = pop(stack);
            if (j!=Declaration.ERROR) {
                visited.set(j);
                IEventState t = states[j];
                if (t!=null && t.getEvent() == Declaration.TAU && t.getProb() == 0) {
                    while (t!=null) {
	                    if (!inStack(t)) {
	                        if(t.getNext() <0 || !visited.get(t.getNext()))
	                        	stack = push(stack,t);
	                    }
	                    t=t.getNondet();
                    }
                }
            }
        }
        return head;
    }

    /* --------------------------------------------------------------*/
    // Queue using path
    /* --------------------------------------------------------------*/

    private static IEventState addtail(IEventState tail, IEventState es) {
        es.setPath( null );
        if (tail!=null) tail.setPath( es );
        return es;
    }


    private static IEventState removehead(IEventState head) {
        if (head==null) return head;
        IEventState es = head;
        head = es.getPath();
        return head;
    }

		/*----------------------------------------------------------------*/
		/*   depth first Search to return set of reachable states
		/*----------------------------------------------------------------*/
		
		public static MyIntHash reachable(IEventState[] states) {
			  int ns = 0; //newstate
			  MyIntHash visited = new MyIntHash(states.length);
			  IEventState stack = null;
			  stack = push(stack, new EventState(0,0));
			  while(stack!=null) {
			  	 int v = stack.getNext();
			  	 stack = pop(stack);
			  	 if (!visited.containsKey(v)) {
				  	 visited.put(v,ns++); 
		         IEventState p =states[v];
		         while(p!=null) {
		            IEventState q = p;
		            while (q!=null) {
		                if (q.getNext() >=0 && !visited.containsKey(q.getNext())) stack = push(stack,q);
		                q=q.getNondet();
		            }
		            p=p.getList();
		         }
			  	 }
	      }
        return visited;
    }

   /*-------------------------------------------------------------*/
   //breadth first search of states from 0, return trace to deadlock/error
   /*-------------------------------------------------------------*/


    public static int search(IEventState trace, IEventState[] states, int fromState, int findState, int ignoreState) {

	return search( trace, states, fromState, findState, ignoreState, true );
    }


    public static int search(IEventState trace, IEventState[] states, int fromState, int findState, int ignoreState, boolean checkDeadlocks ) {
        IEventState zero = new EventState(0,fromState);
        IEventState head = zero;
        IEventState tail = zero;
        int res = Declaration.SUCCESS;
        int id = 0;
        IEventState val[] = new EventState[states.length+1]; //shift by 1 so ERROR is 0
        while(head!=null) {  
            int k = head.getNext();
            val[k+1] = head;  //the event that got us here
            if (k<0 || k==findState) {res = Declaration.ERROR; break;} //ERROR
            IEventState t = states[k];
            if (checkDeadlocks && t==null && k!=ignoreState){res = Declaration.STOP; break;}; //DEADLOCK
            while(t!=null) {
                IEventState q = t;
                while (q!=null) {
                    if (val[q.getNext()+1]==null){  // not visited or in queue
                        q.setMachine( k );         //backward pointer to source state
                        tail = addtail(tail,q);
                        val[q.getNext()+1]=zero;
                    }
                     q=q.getNondet();
                }
                t=t.getList();
            }
            head = removehead(head);
        }
        if (head==null) return res;
        IEventState stack = null;
        IEventState ts = head;
        while (ts.getNext() != fromState) {
            stack = push(stack,ts);
            ts = val[ts.getMachine()+1];
        }
        trace.setPath(stack);
        return res;
    }

   /*-------------------------------------------------------------*/
   //print a path of EventStates
   /*-------------------------------------------------------------*/
    public static void printPath(IEventState head, String[] alpha, LTSOutput output) {
        IEventState q =head;
        while(q!=null) {
                output.outln("\t"+ alpha[q.getEvent()]);
                q=pop(q);
        }
    }

    public static Vector getPath(IEventState head, String[] alpha) {
        IEventState q =head;
        Vector v = new Vector();
        while (q!=null) {
            v.addElement(alpha[q.getEvent()]);
            q=pop(q);
        }
        return v;
    }

    public static boolean isTerminal(int state, IEventState head) {

        IEventState p = head;
        while (p != null) {
            IEventState q = p;
            while (q != null) {
                if (q.getNext() != state)
                    return false;
                q = q.getNondet();
            }
            p = p.getList();
        }
        return true;
    }

    public static boolean hasNonDetEvent(IEventState head, int event) {

	IEventState p =head;
	while(p!=null ) {
	    if (p.getEvent() == event && p.getNondet() != null) return true;
	    p=p.getList();
	}

	return false;
    }
    
    public static IEventState normalise(IEventState head) {
    	// if not probabilistic, no need to normalise
    	if (head == null || head.getProb() == 0) return head;
		IEventState p = head;
		double sum = 0;
		while (p != null) {
			sum += p.getProb();
			p = p.getList();
		}
		p = head;
		while (p != null) {
			p.setProb( p.getProb() / sum );
			p = p.getList();
		}
    	return head;
    }

    public int getEvent() {

        return event;
    }

    public int getNext() {

        return next;
    }

    public void setList(IEventState pList ) {

        list = pList;
    }

    public IEventState getList() {

        return list;
    }

    public IEventState getNondet() {

        return nondet;
    }

    public void setNondet(IEventState pNonDet) {
        
        nondet = pNonDet;
    }

    public void setNext(int pNext) {

        next = pNext;
    }

    public int getMachine() {

        return machine;
    }

    public void setEvent(int pEvent) {

        event = pEvent;
    }

    public IEventState getPath() {

        return path;
    }

    public void setPath(IEventState pPath) {

        path = pPath;
    }

    public void setMachine(int pMach) {

        machine = pMach;
    }
    
    public double getProb() {
        
        return prob;
    }

    public void setProb(double pProb) {

        prob = pProb;
    }

    public Action getAction() {

        return action;
    }

    public Condition getCondition() {

        return condition;
    }

    public void setAction(Action pAction) {

        action = pAction;
        
    }

    public void setCondition(Condition pCond ) {

        condition = pCond;
    }
    

}


final
class EventStateEnumerator implements Enumeration {
   IEventState es;
   IEventState list;

    EventStateEnumerator(IEventState es) {
	    this.es = es;
	    if (es!=null) list = es.getList();
    }

    public boolean hasMoreElements() {
	    return es!=null;
    }

    public Object nextElement() {
        if (es!=null) {
		    IEventState temp = es;
		    if (es.getNondet() != null)
		        es = es.getNondet();
		    else {
		        es =list;
		        if (es!=null) list = list.getList();
		    }
		    return temp;
	    }
	throw new NoSuchElementException("EventStateEnumerator");
    }

}
