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
import ic.doc.ltsa.common.infra.Relation;

import java.util.*;
import java.io.PrintStream;

// records transitions in the CompactState class

public class EventState implements IEventState {
    
    private int event;
    private int next;
    private int machine;
    private IEventState list;  //used to keep list in event order, TAU first
    private IEventState nondet;//used for additional non-deterministic transitions
    private IEventState path;  //used by analyser & by minimiser

    public EventState(int e, int i ) {
        event = e;
        next = i;
    }

    public Enumeration elements() {
        return new EventStateEnumerator(this);
    }

    // the following is not very OO but efficient
    // duplicates are discarded
    public static IEventState add(IEventState head, IEventState tr) {
        // add at head
        if (head==null || tr.getEvent() < head.getEvent() ) {
            tr.setList(head);
            return tr;
        }
        IEventState p    = head;
        while (p.getList()!=null && p.getEvent() !=tr.getEvent() && tr.getEvent() >=p.getList().getEvent() ) p=p.getList();
        if (p.getEvent() == tr.getEvent() ) { //add to nondet
            IEventState q = p;
            if (q.getNext() == tr.getNext() ) return head;
            while(q.getNondet()!=null) {
               q=q.getNondet();
               if (q.getNext() == tr.getNext() ) return head;
             }
            q.setNondet(tr);
        } else {    //add after p
            tr.setList(p.getList());
            p.setList(tr);
        }
        return head;
    }

    public static IEventState remove(IEventState head, IEventState tr) {
        //remove from head
        if (head==null) return head;
        if (head.getEvent() == tr.getEvent() && head.getNext() == tr.getNext() ) {
            if (head.getNondet()==null)
                return head.getList();
            else {
                head.getNondet().setList(head.getList());
                return head.getNondet();
            }
        }
        IEventState p =head;
        IEventState plag = head;
        while(p!=null) {
            IEventState q = p;
            IEventState qlag = p;
            while (q!=null) {
                if (q.getEvent() == tr.getEvent()&& q.getNext() == tr.getNext() ) {
                    if(p==q) { //remove from head of nondet
                        if(p.getNondet()==null) {
                           plag.setList(p.getList());
                           return head;
                        } else {
                           p.getNondet().setList(p.getList());
                           plag.setList(p.getNondet());
                           return head;
                        }
                    } else {
                        qlag.setNondet(q.getNondet());
                        return head;
                    }
                }
                qlag = q;
                q=q.getNondet();
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
	
	public static void replaceWithError(IEventState head,int sinkState)  {
		IEventState p =head;
	    while(p!=null) {
	        IEventState q = p;
	        while (q!=null) {
	            if (q.getNext() == sinkState) q.setNext( Declaration.ERROR );
	            q=q.getNondet();
	        }
	        p=p.getList();
	    }
	}
  
    public static IEventState offsetSeq(int off, int seq, int max, IEventState head) {
        IEventState p =head;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
            	  if (q.getNext() >= 0) {
            	  	if (q.getNext() == seq) 
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
        IEventState p =head;
        int result = 0;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                if (q.getNext()  == next) result++;
                q=q.getNondet();
            }
            p=p.getList();
        }
        return result;
    }

   public static boolean hasEvent(IEventState head, int event) {
        IEventState p =head;
        while(p!=null) {
             if (p.getEvent()== event) return true;
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

    public static boolean isTerminal(int state, IEventState head) {
       IEventState p =head;
        while(p!=null) {
          IEventState q = p;
          while (q!=null) {
              if (q.getNext()  != state) return false;
              q=q.getNondet();
          }
          p=p.getList();
      }
      return true;
      }

    
    public static IEventState firstCompState(IEventState head, int event, int[] state) {
    	   IEventState p =head;
        while(p!=null) {
             if (p.getEvent()== event) {
             	  state[p.getMachine()] = p.getNext() ;
             	  return p.getNondet();
             }
             p=p.getList();
        }
        return null;
    }
    
    public static IEventState moreCompState(IEventState head, int[] state) {
    	   state[head.getMachine()] = head.getNext() ;
    	   return head.getNondet();
    }

    public static boolean hasTau(IEventState head) {
        if (head==null) return false;
        return (head.getEvent()== Declaration.TAU);
    }
    
    public static boolean hasOnlyTau(IEventState head) {
        if (head==null) return false;
        return (head.getEvent()== Declaration.TAU && head.getList() == null);
    }
    
    public static boolean hasOnlyTauAndAccept(IEventState head, String[] alphabet) {
        if (head==null) return false;
        if (head.getEvent()!= Declaration.TAU) return false;
        if (head.getList() == null) return true;
        if (alphabet[head.getList().getEvent()].charAt(0)!='@') return false;
        return (head.getList().getList() == null);
    }
    
    //precondition is "hasOnlyTauAndAccept"
    public static IEventState removeAccept(IEventState head) {
    	   head.setList(null);
    	   return head;
    }
    	   
    public static IEventState addNonDetTau(IEventState head, IEventState states[], BitSet tauOnly) {
    	   IEventState p =head;
    	   IEventState toAdd = null;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                if (q.getNext() >0 && tauOnly.get(q.getNext() )) {
                	 int nextS[] =  nextState(states[q.getNext() ],Declaration.TAU);
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
	
	public static boolean hasNonDetEvent(IEventState head, int event) {
        IEventState p =head;
        while(p!=null ) {
             if (p.getEvent()== event && p.getNondet() != null) return true;
             p=p.getList();
        }
        return false;
    }
    
    public static int[] localEnabled(IEventState head) {
    	    IEventState p =head;
    	    int n = 0;
    	    while(p!=null) {++n; p=p.getList();}
    	    if (n==0) return null;
    	    int[] a = new int[n];
    	    p = head; n=0;
    	    while(p!=null) {a[n++]=p.getEvent() ; p=p.getList();}
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
            if (p.getEvent()== event) {
                IEventState q = p; int size=0;
                while(q!=null) {q=q.getNondet(); ++size;}
                q = p;
                int n[] = new int[size];
                for (int i=0; i<n.length; ++i) {n[i]=q.getNext() ; q=q.getNondet();}
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
            IEventState q = p;
            while (q!=null) {
                int event = ((Integer)oldtonew.get(new Integer(q.getEvent()))).intValue();
                newhead = add(newhead,new EventState(event,q.getNext() ));
                q=q.getNondet();
            }
            p=p.getList();
        }
        return newhead;
    }

    public static IEventState newTransitions(IEventState head, Relation oldtonew) {
        IEventState p =head;
        IEventState newhead =null;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                Object o = oldtonew.get(new Integer(q.getEvent() ));
                if (o!=null) {
                    if (o instanceof Integer) {
                         newhead = add(newhead,new EventState(((Integer)o).intValue(),q.getNext() ));
                    } else {
                        Vector v = (Vector)o;
                        for (Enumeration e = v.elements();e.hasMoreElements();) {
                            newhead = add(newhead,new EventState(((Integer)e.nextElement()).intValue(),q.getNext() ));
                        }
                    }
                }
                q=q.getNondet();
            }
            p=p.getList();
        }
        return newhead;
    }


    public static IEventState offsetEvents(IEventState head, int offset) {
        IEventState p =head;
        IEventState newhead =null;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                q.setEvent( q.getEvent() ==0 ? 0 : q.getEvent()+offset );
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
            IEventState q = p;
            while (q!=null) {
                int next = q.getNext() <0?Declaration.ERROR:((Integer)oldtonew.get(new Integer(q.getNext() ))).intValue();
                newhead = add(newhead,new EventState(q.getEvent(),next));
                q=q.getNondet();
            }
            p=p.getList();
        }
        return newhead;
    }
    
    public static IEventState renumberStates(IEventState head, MyIntHash oldtonew) {
        IEventState p =head;
        IEventState newhead =null;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                int next = q.getNext() <0?Declaration.ERROR:oldtonew.get(q.getNext() );
                newhead = add(newhead,new EventState(q.getEvent(),next));
                q=q.getNondet();
            }
            p=p.getList();
        }
        return newhead;
    }


    public static IEventState addTransToError(IEventState head,int last) {
        IEventState p =head;
        IEventState newhead = null;
        if (p!=null && p.getEvent()==Declaration.TAU) p = p.getList(); //skip tau
        int index =1;
        while(p!=null) {
            if (index<p.getEvent()) {
                for (int i= index; i<p.getEvent(); i++)
                    newhead = add(newhead,new EventState(i,Declaration.ERROR));

            }
            index=p.getEvent() + 1;
            IEventState q = p;
            while (q!=null) {
                newhead = add(newhead,new EventState(q.getEvent(),q.getNext() ));
                q=q.getNondet();
            }
            p=p.getList();
        }
        for (int i= index; i<last; i++)
                    newhead = add(newhead,new EventState(i,Declaration.ERROR));
        return newhead;
    }
	
	//prcondition - no non-deterministic transitions
	public static IEventState removeTransToError(IEventState head)  {
	  IEventState p =head;
	  IEventState newHead = null;
	  while(p!=null) {
	       if (p.getNext()  != Declaration.ERROR) 
		       newHead = add(newHead, new EventState(p.getEvent(),p.getNext() ));
	       p=p.getList();
	  }
	  return newHead;
	}
	
    //remove tau actions
    public static IEventState removeTau(IEventState head) {
        if (head==null) return head;
        if (head.getEvent()!= Declaration.TAU) return head;
        return head.getList();
    }

    //add states reachable by next from events
    public static IEventState tauAdd(IEventState head, IEventState[] T) {
        IEventState p =head;
        IEventState added = null;
        if (p!=null && p.getEvent()==Declaration.TAU) p =p.getList(); //skip tau
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                if (q.getNext() !=Declaration.ERROR) {
                    IEventState t = T[q.getNext() ];
                    while(t!=null) {
                        added = push(added,new EventState(p.getEvent(),t.getNext() ));
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
            if (tau.getNext() !=Declaration.ERROR) head = union(head,states[tau.getNext() ]);
            tau=tau.getNondet();
        }
        return head;
    }

    // to = to U from
    public static IEventState union(IEventState to, IEventState from){
        IEventState res = to;
        IEventState p =from;
        while(p!=null) {
            IEventState q = p;
            while(q!=null) {
                res = add(res,new EventState(q.getEvent(),q.getNext() ));
                q=q.getNondet();
            }
            p =  p.getList();
        }
        return res;
    }
    
    //normally, IEventState lists are sorted by event with
    //the nondet list containing lists of different next states
    // for the same event
    // transpose creates a new list sorted by next
    public static IEventState transpose(IEventState from) {
        IEventState res = null;
        IEventState p =from;
        while(p!=null) {
            IEventState q = p;
            while(q!=null) {
                res = add(res,new EventState(q.getNext() ,q.getEvent())); //swap event & next
                q=q.getNondet();
            }
            p =  p.getList();
        }
        // now walk through the list a swap event & next back again
        p =res;
        while(p!=null) {
            IEventState q = p;
            while (q!=null) {
                int n = q.getNext() ; q.setNext( q.getEvent() ); q.setEvent(n);
                q=q.getNondet();
            }
            p=p.getList();
        }
        return res;
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
            es.setPath(es);
        else
            es.setPath(head);
        return head = es;
    }


    private static boolean inStack(IEventState es) {
        return (es.getPath()!=null);
    }

    private static IEventState pop(IEventState head) {
        if (head==null) return head;
        IEventState es = head;
        head = es.getPath();
        es.setPath(null);
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
        if (head==null || head.getEvent()!=Declaration.TAU)
            return null;
        BitSet visited = new BitSet(states.length);
        visited.set(k);
        IEventState stack=null;
        while (head!=null) {
                stack = push(stack,head);
                head = head.getNondet();
        }
        while(stack!=null) {
            int j = stack.getNext() ;
            head = add(head,new EventState(Declaration.TAU,j));
            stack = pop(stack);
            if (j!=Declaration.ERROR) {
                visited.set(j);
                IEventState t = states[j];
                if (t!=null && t.getEvent()==Declaration.TAU)
                    while (t!=null) {
                        if (!inStack(t)) {
                            if(t.getNext() <0 || !visited.get(t.getNext() ))
                                    stack = push(stack,t);
                        }
                         t=t.getNondet();
                    }
            }
        }
        return head;
    }

    /* --------------------------------------------------------------*/
    // Queue using path
    /* --------------------------------------------------------------*/

    private static IEventState addtail(IEventState tail, IEventState es) {
        es.setPath(null);
        if (tail!=null) tail.setPath(es);
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
			  	 int v = stack.getNext() ;
			  	 stack = pop(stack);
			  	 if (!visited.containsKey(v)) {
				  	 visited.put(v,ns++); 
		         IEventState p =states[v];
		         while(p!=null) {
		            IEventState q = p;
		            while (q!=null) {
		                if (q.getNext() >=0 && !visited.containsKey(q.getNext() )) stack = push(stack,q);
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

    public static int search(IEventState trace, IEventState[] states, int fromState, int findState, int ignoreState , boolean checkDeadlocks ) {

        IEventState zero = new EventState(0,fromState);
        IEventState head = zero;
        IEventState tail = zero;
        int res = Declaration.SUCCESS;
        int id = 0;
        IEventState val[] = new EventState[states.length+1]; //shift by 1 so ERROR is 0
        while(head!=null) {
            int k = head.getNext() ;
            val[k+1] = head;  //the event that got us here
            if (k<0 || k==findState) {res = Declaration.ERROR; break;} //ERROR
            IEventState t = states[k];
            if (checkDeadlocks && t==null && k!=ignoreState){res = Declaration.STOP; break;}; //DEADLOCK
            while(t!=null) {
                IEventState q = t;
                while (q!=null) {
                    if (val[q.getNext() +1]==null){  // not visited or in queue
                        q.setMachine( k );         //backward pointer to source state
                        tail = addtail(tail,q);
                        val[q.getNext() +1]=zero;
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
        while (ts.getNext() !=fromState) {
            stack = push(stack,ts);
            ts = val[ts.getMachine()+1];
        }
        trace.setPath(stack);
        return res;
    }

   /*-------------------------------------------------------------*/
   //print a path of IEventStates
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

    public void setEvent(int event) {

        this.event = event;
    }

    public int getEvent() {

        return event;
    }

    public void setNext(int next) {

        this.next = next;
    }

    public int getNext() {

        return next;
    }

    public void setMachine(int machine) {

        this.machine = machine;
    }

    public int getMachine() {

        return machine;
    }

    public void setList(IEventState list) {

        this.list = list;
    }

    public IEventState getList() {

        return list;
    }

    public void setNondet(IEventState nondet) {

        this.nondet = nondet;
    }

    public IEventState getNondet() {

        return nondet;
    }

    public void setPath(IEventState path) {
        this.path = path;
    }

    public IEventState getPath() {
        return path;
    }

    public double getProb() {
        return 0;
    }

    public void setProb(double p) {
        
    }
}


final class EventStateEnumerator implements Enumeration {
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
		    if (es.getNondet()!=null)
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
