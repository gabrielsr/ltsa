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

/** Minimiser takes a CompactState and creates a "low-fat" version of it.
 *  The behaviour of the minimised CompactState will be exactly the same
 *  as that of the original, with at most as many states and transitions
 *  as the original (hopefully less).
 * */
public class Minimiser {

    final static int TAU = 0;

    BitSet [] E;  // array of |states| x |states| bits
    BitSet [] A;  // array of |states| x |actions| bits
    IEventState [] T;  // tau adjacency lists - stores  reflexive transitive closure
    CompactState machine;
    LTSOutput output;

    public Minimiser(CompactState c, LTSOutput output) {
        machine = c;
        this.output = output;
    }

    // initialise T with transitive closure of tau in machine
    private void initTau() {
        T = new IEventState[machine.getStates().length];
        for (int i = 0; i<T.length; i++) {
            T[i] = EventState.reachableTau(machine.getStates(),i);
        }
    }

    // G=>G' using T
    private CompactState machTau(CompactState m) {
        // do T* a pass

        for (int i=0; i<m.getStates().length; i++)
            m.getStates()[i] = EventState.tauAdd(m.getStates()[i],T);
        for (int i=0; i<m.getStates().length; i++) {
            m.getStates()[i] = EventState.union(m.getStates()[i],T[i]);
            m.getStates()[i] = EventState.actionAdd(m.getStates()[i],m.getStates());
        }
        for (int i=0; i<m.getStates().length; i++)
            m.getStates()[i] = EventState.add(m.getStates()[i],new EventState(Declaration.TAU,i));
        output.out(".");
        return m;
    }

    private CompactState removeTau(CompactState m) {
        for (int i=0; i<m.getStates().length; i++)
            m.getStates()[i] = EventState.removeTau(m.getStates()[i]);
        return m;
    }

    //first step in initialisation is set up E
    private void initialise() {
        // initialise A such that A[i,a] is true if transition a from state i
        A = new BitSet[machine.maxStates];
        for (int i = 0; i<A.length; i++) {
            A[i] = new BitSet(machine.alphabet.length);
            EventState.setActions(machine.getStates()[i],A[i]);
        }
        E = new BitSet[machine.maxStates];
        for (int i=0; i<E.length; i++)
            E[i] = new BitSet(E.length);
        // set E[i,j] if A[i] = A[j] ie same set of transitions
        for (int i=0; i<E.length; i++) {
            E[i].set(i);
            for(int j=0; j<i; j++)
                if (A[i].equals(A[j])){ E[i].set(j); E[j].set(i); }
        }
        output.out(".");
    }

    private void dominimise() {
        boolean more=true;;
        while (more) {
            output.out(".");
            more = false;
            for (int i=0; i<E.length; i++) {
                Thread.yield();
                for(int j=0; j<i; j++)
                    if (E[i].get(j)){
                        boolean b = is_equivalent(i,j) && is_equivalent(j,i);
                        if (!b) {
                            more = true;
                            E[i].clear(j);
                            E[j].clear(i);
                        }
                      }
            }
        }
    }

    /*
    * minimise using observational equivalence
    */

    public CompactState minimise() {
         output.out(machine.getName()+" minimising");
         long start =System.currentTimeMillis();
         CompactState saved = machine.myclone();
         /* distinguish  end state from STOP with self transition using special label */
         if (machine.endseq>=0) {
            int es = machine.endseq;
            machine.getStates()[es] 
             = EventState.add(machine.getStates()[es],new EventState(machine.alphabet.length,es));
         }
         if (machine.hasTau()) {
             initTau();
             machine = machTau(machine);
             T=null; // release storage
         }
        initialise();
        dominimise();
        machine = saved;
        CompactState c = makeNewMachine();
        long finish =System.currentTimeMillis();
        output.outln("");
        output.outln("Minimised States: "+c.maxStates+" in "+(finish-start)+"ms");
        return  c;
    }

    /*
    * generate minimized trace equivalent deterministic automata
    */
    public CompactState trace_minimise() {
        boolean must_minimize = false;
        //convert to trace equivalent NFA without tau
        if (machine.hasTau()) {
            must_minimize = true;
            output.out("Eliminating tau");
            initTau();
            machine = machTau(machine);
            machine = removeTau(machine);
            T=null; // release storage
        }
        //convert NFA to DFA
        if (must_minimize || machine.isNonDeterministic()) {
           must_minimize = true;
           Determinizer d = new Determinizer(machine,output);
           machine = d.determine();
        }
     
        // now minimise
        if (must_minimize)
            return minimise();
        else
            return machine;
    }

    private boolean is_equivalent(int i, int j) {
       IEventState p = machine.getStates()[i];
       while(p!=null) {
            IEventState tr = p;
            while (tr!=null) {
                if (!findSuccessor(j,tr)) return false;
                tr=tr.getNondet();
            }
            p=p.getList();
        }
        return true;
    }

    private boolean findSuccessor(int j,IEventState tr) {
        IEventState p = machine.getStates()[j];  //find event
        while(p.getEvent()!=tr.getEvent()) p=p.getList();
        while (p!=null) {
            if (tr.getNext()<0) {
                if (p.getNext()<0) return true;
            } else {
                if (p.getNext() >= 0) {
                    if (E[tr.getNext()].get(p.getNext()))return true;
                }
            }
            p=p.getNondet();
        }
        return false;
    }

    private CompactState makeNewMachine() {
        Hashtable oldtonew = new Hashtable();
        Hashtable newtoold = new Hashtable();
        Counter newSt = new Counter(0);
        for (int i=0; i<E.length; i++) {
                Integer oldIndex = new Integer(i);
                Integer newIndex = (Integer)oldtonew.get(oldIndex);
                if (newIndex==null) {
                    oldtonew.put(oldIndex,newIndex=newSt.label());
                    newtoold.put(newIndex,oldIndex);
                }
            for(int j=0; j<E.length; j++) {
                if (E[i].get(j)) oldtonew.put(new Integer(j),newIndex);
            }
        }
        CompactState m = new CompactState();
        m.setName(machine.getName());
        m.maxStates = newtoold.size();
        m.alphabet = machine.alphabet;
        m.setStates(new EventState[m.maxStates]);
        if (machine.endseq<0) 
          m.endseq = machine.endseq;
        else {
          m.endseq = ((Integer)oldtonew.get(new Integer(machine.endseq))).intValue();
          /* remove marking transition */
          m.getStates()[m.endseq] 
             = EventState.remove(m.getStates()[m.endseq],new EventState(m.alphabet.length,m.endseq));
        }
          
        for (int i = 0; i<machine.maxStates; i++) {
            int newi = ((Integer)oldtonew.get(new Integer(i))).intValue();
            IEventState tmp = EventState.renumberStates(machine.getStates()[i],oldtonew);
            m.getStates()[newi] = EventState.union(m.getStates()[newi],tmp);
        }

        for (int i = 0; i<m.maxStates; i++)   // remove reflexive tau
            m.getStates()[i] = EventState.remove(m.getStates()[i],new EventState(Declaration.TAU,i));
        return m;
    }


    public void print(LTSOutput output) {
        privPrint(output,E);
    }

    private void privPrint(LTSOutput output, BitSet[] E) {
        if (E.length>20) return;
        char [] buf = new char[E.length*2];
        for(int i=0; i<E.length*2; i++) buf[i]=' ';
        output.outln("E:");
        output.out("       ");
        for(int i=0; i<E.length; i++) output.out(" "+i);
        output.outln("");
        for(int i=0; i<E.length; i++){
            output.out("State "+i+" ");
            for(int j=0;j<E.length;j++)
                if(E[i].get(j)) buf[j*2]='1'; else buf[j*2]=' ';
            output.outln(new String(buf));
        }
    }

}