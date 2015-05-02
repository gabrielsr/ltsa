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
import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.IEventState;
import ic.doc.ltsa.common.infra.MyList;
import ic.doc.ltsa.common.infra.Relation;
import ic.doc.ltsa.common.infra.StackCheck;

import java.util.*;
import java.io.*;

public class CompactState implements IAutomata, ICompactState {
    
    private String oName;
    private int oMaxStates;
    private String[] oAlphabet;
    private IEventState[] oStates; // each state is to a vector of <event, nextstate>
   
    private int endseq = -9999; //number of end of sequence state if any

    public  CompactState() {} // null constructor

    public CompactState(int size, String name, MyHashStack statemap, MyList transitions, String[] alphabet, int endSequence) {

        oAlphabet = alphabet;
        oName = name;
        oMaxStates = size;
        oStates = new EventState[getMaxStates()];
        
        while(!transitions.isEmpty()) {
            int fromState = (int)transitions.getFrom();
            int toState   = transitions.getTo()==null?-1:statemap.get(transitions.getTo());
            oStates[fromState]= EventState.add(oStates[fromState],new EventState(transitions.getAction(),toState));
            transitions.next();
        }
        endseq = endSequence;
    }
    
     
    public void setMaxStates(int pMaxStates) {

        oMaxStates = pMaxStates;
    }

    public int getMaxStates() {

        return oMaxStates;
    }

    public void setName(String pName) {

        oName = pName;
    }

    public String getName() {

        return oName;
    }

    public void setAlphabet(String[] alphabet) {

        this.oAlphabet = alphabet;
    }

    public String[] getAlphabet() {

        return oAlphabet;
    }

    public void setEndseq(int endseq) {

        this.endseq = endseq;
    }

    public int getEndseq() {

        return endseq;
    }

    public void reachable() {
    	  MyIntHash otn = EventState.reachable(oStates);
    	  //System.out.println("reachable states "+otn.size()+" total states "+maxStates);
    	  // always do reachable for better layout!!
    	  //if (otn.size() == maxStates) return;
    	  IEventState[] oldStates = oStates;
    	  setMaxStates(otn.size());
    	  oStates = new EventState[getMaxStates()];
    	  for (int oldi = 0; oldi<oldStates.length; ++oldi) {
    	  	int newi = otn.get(oldi);
    	  	if (newi>-2) {
    	  		oStates[newi] = EventState.renumberStates(oldStates[oldi],otn);
    	  	}
    	  }
    	  if (endseq>0) endseq = otn.get(endseq);
    }
    
    // change (a ->(tau->P|tau->Q)) to (a->P | a->Q)
    public void removeNonDetTau() { 
    	  if (!hasTau()) return;
    	  while (true) {
    	  	  boolean canRemove = false;
    	  	  for (int i = 0; i<getMaxStates(); i++)   // remove reflexive tau
            oStates[i] = EventState.remove(oStates[i],new EventState(Declaration.TAU,i));
	    	  BitSet tauOnly = new BitSet(getMaxStates());
	    	  for (int i = 1; i<getMaxStates(); ++i) {
						if (EventState.hasOnlyTauAndAccept(oStates[i],oAlphabet)) {
							   tauOnly.set(i);
							   canRemove=true;
						}
	    	  }
				if (!canRemove) return;
				for (int i = 0; i<getMaxStates(); ++i) {
					  if (!tauOnly.get(i))
					     oStates[i] = EventState.addNonDetTau(oStates[i],oStates,tauOnly);
	    	  }
	    	  int oldSize = getMaxStates();
	    	  reachable();
	    	  if (oldSize == getMaxStates()) return;
    	  }
    }
	
	public void removeDetCycles(String action)  {
		int act = eventNo(action);
		if (act >=oAlphabet.length) return;
		for (int i =0; i<oStates.length; ++i)  {
			if (!EventState.hasNonDetEvent(oStates[i],act))
				oStates[i] = EventState.remove(oStates[i],new EventState(act,i));
		}
	}
	
	//check if has only single terminal accept state
	//also if no accept states - treats as safety property so that TRUE generates a null constraint
	public boolean isSafetyOnly()  {
		int terminalAcceptStates =0;
		int acceptStates = 0;
		for (int i = 0; i<getMaxStates(); i++)  {
			if (EventState.isAccepting(oStates[i],oAlphabet)) {
			   ++acceptStates;
			   if (EventState.isTerminal(i,oStates[i]))
			   		++terminalAcceptStates;
			}
		}
		return (terminalAcceptStates==1 && acceptStates ==1) || acceptStates == 0 ;
	}
	
	//precondition - isSafetyOnly()
	//translates acceptState to ERROR state
	/*
	public void makeSafety()  {
		for (int i = 0; i<maxStates; i++)  {
			if (EventState.isAccepting(states[i],alphabet)) {
			   states[i] = new EventState(Declaration.TAU,Declaration.ERROR);
			}
		}
	}*/
	/* This version handles FALSE 13th June 2004 */
	public void makeSafety()  {
		int acceptState = -1;
		for (int i = 0; i<getMaxStates(); i++)  {
			if (EventState.isAccepting(oStates[i],oAlphabet)) {
			  acceptState = i;
			  break;
			}
		}
		if (acceptState>=0) oStates[acceptState] = EventState.removeAccept(oStates[acceptState]);
		for (int i = 0; i<getMaxStates(); i++)  {
			EventState.replaceWithError(oStates[i],acceptState);
		}
		reachable();
	}				
       	
	  //remove acceptance from states with only outgoing tau
    public void removeAcceptTau(){
      for (int i = 1; i<getMaxStates(); ++i) {
				if (EventState.hasOnlyTauAndAccept(oStates[i],oAlphabet)) {
					 oStates[i] = EventState.removeAccept(oStates[i]);
				}
      }
    }
	
	public boolean hasERROR() {
		for (int i=0; i<getMaxStates(); i++ )
            if (EventState.hasState(oStates[i],Declaration.ERROR))
				return true;
		return false;	
	}	

    
    public void prefixLabels(String prefix) {
        oName = prefix+":"+oName;
        for (int i=1; i<oAlphabet.length; i++) { // don't prefix tau
            String old = oAlphabet[i];
            oAlphabet[i]= prefix+"."+old;
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
        na.setSize(oAlphabet.length);
        int new_index = oAlphabet.length;
        na.setElementAt(oAlphabet[0], 0);
        for (int i=1; i<oAlphabet.length; i++) {
            int prefix_end = -1;
            Object o = oldtonew.get(oAlphabet[i]);
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
            } else if ((prefix_end=maximalPrefix(oAlphabet[i],oldtonew))>=0) { //is it prefix?
                String old_prefix = oAlphabet[i].substring(0,prefix_end);
                o = oldtonew.get(old_prefix);
                if (o!=null) {
                    if (o instanceof String) {
                        na.setElementAt(((String)o) + oAlphabet[i].substring(prefix_end),i);
                    } else { //one - to - many
                        Vector v = (Vector)o;
                        na.setElementAt(((String)v.firstElement()) + oAlphabet[i].substring(prefix_end),i);
                        for (int j=1;j<v.size();++j) {
                            na.addElement(((String)v.elementAt(j)) + oAlphabet[i].substring(prefix_end));
                            otoni.put(new Integer(i),new Integer(new_index));
                            ++new_index;
                        }
                    }
                } else {
                    na.setElementAt(oAlphabet[i],i); //not relabelled
                }
            } else {
                na.setElementAt(oAlphabet[i],i); //not relabelled
            }
        }
        //install new alphabet
        String aa[] = new String[na.size()];
        na.copyInto(aa);
        oAlphabet = aa;
        // add transitions
        addtransitions(otoni);
        checkDuplicates();
    }

    private void functional_relabel(Hashtable oldtonew) {
       for (int i=1; i<oAlphabet.length; i++) {  //don't relabel tau
            String newlabel = (String)oldtonew.get(oAlphabet[i]);
            if (newlabel!=null)
                 oAlphabet[i] = newlabel;
            else
                 oAlphabet[i] = prefixLabelReplace(i,oldtonew);
        }
        checkDuplicates();
    }

    private void checkDuplicates(){
        Hashtable duplicates=new Hashtable();
        for (int i=1; i<oAlphabet.length; i++) {
            if(duplicates.put(oAlphabet[i],oAlphabet[i])!=null) {
                hasduplicates = true;
                crunchDuplicates();
            }
        }
    }

    private void crunchDuplicates() {
        Hashtable newAlpha = new Hashtable();
        Hashtable oldtonew   = new Hashtable();
        int index =0;
        for(int i = 0; i<oAlphabet.length; i++) {
            if (newAlpha.containsKey(oAlphabet[i])) {
                oldtonew.put(new Integer(i), newAlpha.get(oAlphabet[i]));
            } else {
                newAlpha.put(oAlphabet[i],new Integer(index));
                oldtonew.put(new Integer(i), new Integer(index));
                index++;
            }
        }
        oAlphabet = new String[newAlpha.size()];
        Enumeration e = newAlpha.keys();
        while(e.hasMoreElements()) {
            String s = (String)e.nextElement();
            int i = ((Integer)newAlpha.get(s)).intValue();
            oAlphabet[i] = s;
        }
         // renumber transitions
        for (int i=0; i<oStates.length; i++)
            oStates[i] = EventState.renumberEvents(oStates[i],oldtonew);
     }
     
     //now used only for incremental minimization
     public Vector hide(Vector toShow) {
       Vector toHide = new Vector();
        for(int i = 1; i<oAlphabet.length; i++) {
            if (!contains(oAlphabet[i],toShow))
                toHide.addElement(oAlphabet[i]);
        }
        return toHide;
    }


    // hides every event but the ones in toShow
    public void expose(Vector toShow) {
        BitSet visible = new BitSet(oAlphabet.length);
        for(int i=1; i<oAlphabet.length ; ++i) {
           if (contains(oAlphabet[i],toShow)) visible.set(i);
        }
        visible.set(0);
        dohiding(visible);
    }

    public void conceal(Vector toHide) {
        BitSet visible = new BitSet(oAlphabet.length);
        for(int i=1; i<oAlphabet.length ; ++i) {
           if (!contains(oAlphabet[i],toHide)) visible.set(i);
        }
        visible.set(0);
        dohiding(visible);
    }
  
    private void dohiding(BitSet visible) {
        Integer tau = new Integer(Declaration.TAU);
        Hashtable oldtonew = new Hashtable();
        Vector newAlphabetVec = new Vector();
        int index =0;
        for(int i = 0; i<oAlphabet.length; i++) {
            if (!visible.get(i)) {
                oldtonew.put(new Integer(i), tau);
            } else {
                newAlphabetVec.addElement(oAlphabet[i]);
                oldtonew.put(new Integer(i), new Integer(index));
                index++;
            }
        }
        oAlphabet = new String[newAlphabetVec.size()];
        newAlphabetVec.copyInto(oAlphabet);
        // renumber transitions
        for (int i=0; i<oStates.length; i++)
            oStates[i] = EventState.renumberEvents(oStates[i],oldtonew);
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
	
	private boolean prop = false;
	
	public boolean isProperty() {
		return prop;
	}	
	
    public void makeProperty() {
		endseq = -9999;
		prop = true;
        for (int i=0; i<getMaxStates(); i++ )
            oStates[i] = EventState.addTransToError(oStates[i],oAlphabet.length);
    }
	
    public void unMakeProperty() {
    	endseq = -9999;
    	prop = false;
         for (int i=0; i<getMaxStates(); i++ )
             oStates[i] = EventState.removeTransToError(oStates[i]);
     }
	

    public boolean isNonDeterministic() {
        for (int i=0; i<getMaxStates(); i++ )
            if (EventState.hasNonDet(oStates[i])) return true;
        return false;
    }

    //output LTS in aldebaran format
    public void printAUT(PrintStream out) {    
      out.print("des(0,"+ntransitions()+","+getMaxStates()+")\n");
      for (int i=0; i<oStates.length; i++)
          EventState.printAUT(oStates[i],i,oAlphabet,out);
    }

    public CompactState myclone() {
        CompactState m = new CompactState();
        m.oName = oName;
        m.endseq = endseq;
		m.prop = prop;
        m.oAlphabet = new String[oAlphabet.length];
        for (int i=0; i<oAlphabet.length; i++) m.oAlphabet[i]=oAlphabet[i];
        m.setMaxStates(oMaxStates);
        m.oStates = new EventState[getMaxStates()];
        for (int i=0;i<getMaxStates(); i++)
            m.oStates[i] = EventState.union(m.oStates[i],oStates[i]);
        return m;
    }

    public int ntransitions() {
        int count = 0;
        for (int i=0; i<oStates.length; i++)
            count += EventState.count(oStates[i]);
        return count;
    }

    public boolean hasTau() {
        for (int i = 0; i<oStates.length; ++i) {
            if (EventState.hasTau(oStates[i])) return true;
        }
        return false;
    }


    /* ------------------------------------------------------------*/
    private String prefixLabelReplace(int i, Hashtable oldtonew) {
        int prefix_end = maximalPrefix(oAlphabet[i],oldtonew);
        if (prefix_end<0) return oAlphabet[i];
        String old_prefix = oAlphabet[i].substring(0,prefix_end);
        String new_prefix = (String)oldtonew.get(old_prefix);
        if (new_prefix==null) return oAlphabet[i];
        return new_prefix + oAlphabet[i].substring(prefix_end);
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
        for (int i=0; i<getMaxStates() && !hasError; i++ )
            if (EventState.hasState(oStates[i],Declaration.ERROR))
                hasError=true;
        if (!hasError) return false;
        return isTrace(trace,0,0);
    }

    private boolean isTrace(Vector v,int index, int start) {
        if (index<v.size()) {
            String ename = (String) v.elementAt(index);
            int eno = eventNo(ename);
            if (eno<oAlphabet.length) {   // this event is in the alphabet
                if (EventState.hasEvent(oStates[start],eno)) {
                    int n[] = EventState.nextState(oStates[start],eno);
                    for (int i=0; i<n.length; ++i) // try each nondet path
                        if (isTrace(v,index+1,n[i])) return true;
                    return false;
                } else if (eno!=Declaration.TAU)  // ignore taus
                    return false;
            }
            return isTrace(v,index+1,start);
        } else
            return (start == Declaration.ERROR);
    }

    private int eventNo(String ename) {
        int i = 0;
        while (i<oAlphabet.length && !ename.equals(oAlphabet[i])) i++;
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
        oName = s+"}::"+oName;
        //new alphabet
        int alphaN = oAlphabet.length - 1;
        oAlphabet = new String[(alphaN*n) +1];
        oAlphabet[0] = "tau";
        for (int j = 0; j<n ; j++) {
            for (int k = 1; k<machs[j].oAlphabet.length; k++) {
                oAlphabet[alphaN*j+k] = machs[j].oAlphabet[k];
            }
        }
        //additional transitions
        for(int j = 1; j<n; j++) {
            for(int k = 0; k<getMaxStates(); k++) {
                EventState.offsetEvents(machs[j].oStates[k],alphaN*j);
                oStates[k] = EventState.union(oStates[k],machs[j].oStates[k]);
            }
        }
    }

  /* ---------------------------------------------------------------*/

    private void addtransitions(Relation oni) {
        for (int i=0; i<oStates.length; i++) {
            IEventState ns = EventState.newTransitions(oStates[i],oni);
            if (ns!=null)
                oStates[i] = EventState.union(oStates[i],ns);
        }
    }

  /* ---------------------------------------------------------------*/

    public boolean hasLabel(String label) {
        for (int i = 0; i<oAlphabet.length ; ++i)
            if (label.equals(oAlphabet[i])) return true;
        return false;
    }
    
    public boolean usesLabel(String label) {
        if (!hasLabel(label)) return false;
        int en = eventNo(label);
        for (int i = 0; i<oStates.length; ++i) {
            if (EventState.hasEvent(oStates[i],en)) return true;
        }
        return false;
    }
    
  /* ---------------------------------------------------------------*/

    public boolean isSequential() {
        return endseq >=0;
    }
    
    public boolean isEnd() {
        return getMaxStates() == 1 && endseq == 0;
    }
    
  /*----------------------------------------------------------------*/
  
   public static CompactState sequentialCompose(Vector seqs) {
   		if (seqs==null) return null;
   		if (seqs.size()==0) return null;
   		if (seqs.size()==1) return (CompactState)seqs.elementAt(0);
   		CompactState machines[] = new CompactState[seqs.size()];
   		machines = (CompactState[])seqs.toArray(machines);
   		CompactState newMachine =  new CompactState();
   		newMachine.oAlphabet = sharedAlphabet(machines);
   		newMachine.setMaxStates(seqSize(machines));
   		newMachine.oStates = new EventState[newMachine.getMaxStates()];
   		int offset = 0;
   		for (int i=0; i<machines.length; i++ ) {
   			boolean last = (i==(machines.length-1));
   			copyOffset(offset,newMachine.oStates,machines[i],last);
   			if (last) 	newMachine.endseq = machines[i].endseq+offset;	
   			offset +=machines[i].oStates.length;
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
   		oAlphabet = sharedAlphabet(machines);
   		//copy inserted machines
   		for (int i=1; i<machines.length; ++i) {
        int offset = insertAt[i];
   			for (int j = 0; j<machines[i].oStates.length; ++j) {
   				oStates[offset+j] = machines[i].oStates[j];
   			}
   		}
   }


  /*
  *   compute size of sequential composite
  */
  private static int seqSize(CompactState[] sm) {
  	 int length = 0;
  	 for (int i=0; i<sm.length; i++ ) 
  	 	    length+=sm[i].oStates.length;
  	 return length;
  }
  
  private static void copyOffset(int offset, IEventState[] dest, CompactState m, boolean last ) {
  	 for(int i = 0; i<m.oStates.length; i++) {
  	 	  if (!last)
  	 	    dest[i+offset] = EventState.offsetSeq(offset,m.endseq,m.getMaxStates()+offset,m.oStates[i]);
  	 	  else
  	 	  	dest[i+offset] = EventState.offsetSeq(offset,m.endseq,m.endseq+offset,m.oStates[i]);
  	 }
  }
  	 	    
  public void offsetSeq(int offset, int finish) {
     for (int i=0; i<oStates.length; i++) {
         EventState.offsetSeq(offset,endseq,finish,oStates[i]);
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
          for (int j = 0; j < sm[i].oAlphabet.length; j++) {
              if (!actionMap.containsKey(sm[i].oAlphabet[j])) {
                  actionMap.put(sm[i].oAlphabet[j],newLabel.label());
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
          for(int j=0; j<sm[i].getMaxStates();j++) {
              IEventState p = sm[i].oStates[j];
              while(p!=null) {
                  IEventState tr = p;
                  tr.setEvent(((Integer)actionMap.get(sm[i].oAlphabet[tr.getEvent()])).intValue());
                  while (tr.getNondet()!=null) {
                      tr.getNondet().setEvent(tr.getEvent());
                      tr = tr.getNondet();
                  }
                  p=p.getList();
              }
          }
      }
      return actionName;
          
	}
	
	/** implementation of Automata interface **/
	
	private byte[] encode(int state) {
		 byte[] code = new byte[4];
		 for(int i=0; i<4; ++i) {
		  	   code[i] |= (byte)state;
		  	   state = state >>>8;
		  }
		  return code;
	}
				
  private int decode( byte[] code){
  	  	 int x =0;
		 for(int i=3; i>=0; --i) {
		  	   x |= (int)(code[i])& 0xFF;
		  	   if (i>0) x = x << 8;
		  }
		  return x;

  }
  
	//public String[] getAlphabet() {return alphabet;}
	
	public Vector getAlphabetV() {
		  Vector v = new Vector(oAlphabet.length-1);
		  for (int i=1; i<oAlphabet.length; ++i)
		  		v.add(oAlphabet[i]);
		  return v;
	}
	
	public MyList getTransitions(byte[] fromState) {
		MyList tr = new MyList();
		int state;
		if (fromState == null)
			state = Declaration.ERROR;
	  else
	     state = decode(fromState);
		if (state<0 ||state>=getMaxStates()) return tr;
		if (oStates[(int)state]!=null)
		for(Enumeration e = oStates[state].elements(); e.hasMoreElements();) {
                EventState t = (EventState)e.nextElement();
                tr.add(state,encode(t.getNext()),t.getEvent());
		}
		return tr;
	}
	
	public String getViolatedProperty() {return null;}

	//returns shortest trace to  state (vector of Strings)
	public Vector getTraceToState(byte[] from, byte[] to){
		EventState trace = new EventState(0,0);
    int result = EventState.search(trace,oStates,decode(from),decode(to),-123456);
    return EventState.getPath(trace.getPath(),oAlphabet);
	}

//return the number of the END state
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
	
	public void setPartialOrderReduction( boolean pPOR ) {}
	
	public void setPreserveObsEquiv( boolean pObEq ) {}

	
	/*-------------------------------------------------------------*/
	// is state accepting
	public boolean isAccepting(int n) {
		  if (n<0 || n>=getMaxStates()) return false;
		  return EventState.isAccepting(oStates[n],oAlphabet);
	}
	
	public BitSet accepting() {
		  BitSet b = new BitSet();
		  for (int i = 0; i<getMaxStates(); ++i) 
		  	   if (isAccepting(i)) b.set(i);
		  	return b;
	}

    public void setStates(EventState[] states) {

        oStates = states;
    }

    public IEventState[] getStates() {

        return oStates;
    }

	public void printPrism(PrintStream myOutput) {
		
		// This is not implemented in the standard backend - only in the probabilistic backend
	}

}
