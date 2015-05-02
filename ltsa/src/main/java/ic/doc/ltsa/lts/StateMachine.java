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

import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.Diagnostics;
import ic.doc.ltsa.common.infra.Relation;

import java.util.*;

class StateMachine {

    String name;
    String kludgeName;
    Hashtable alphabet = new Hashtable();
    Vector hidden;
    Relation relabels;
    Map<String,Integer> explicit_states = new Hashtable<String,Integer>();
    Hashtable constants; // a bit of a kludge, should not be here
    Counter eventLabel = new Counter(0);
    Counter stateLabel = new Counter(0);
    Vector transitions = new Vector();
    boolean isProperty = false;
    boolean isMinimal  = false;
    boolean isDeterministic = false;
    boolean exposeNotHide = false;
    Hashtable sequentialInserts;
    Hashtable preInsertsLast;
    Hashtable preInsertsMach;
    Hashtable aliases = new Hashtable();
    
    public static  LTSOutput output;

    public StateMachine(ProcessSpec spec,Vector params) {
        name = spec.getname();
        if (params!=null) {
            spec.doParams(params);
            kludgeName = name+paramString(params);
        } else
            kludgeName = name;
        make(spec);
    }

    public StateMachine(ProcessSpec spec) {
        // compute machine name
        name = spec.getname();
        kludgeName = name;
        make(spec);
    }

    private void make(ProcessSpec spec){
        constants = spec.constants;
        alphabet.put("tau",eventLabel.label());
        // compute explicit states
        spec.explicitStates(this);
        // crunch aliases
        spec.crunch(this);
        // relabel states in contiguous range from zero
        renumber();
        // compute transitions
        spec.transition(this);
        // alphabet extensions
        spec.addAlphabet(this);
        // alphabet relabels;
        spec.relabelAlphabet(this);
        // alphabet concealments
        spec.hideAlphabet(this);
        isProperty = spec.isProperty;
        isMinimal  = spec.isMinimal;
        isDeterministic = spec.isDeterministic;
        exposeNotHide = spec.exposeNotHide;
    }

    public CompactState makeCompactState() {
        CompactState c = new CompactState();
        c.setName(kludgeName);
        c.setMaxStates(stateLabel.lastLabel().intValue());
        Integer ii = (Integer)explicit_states.get("END");
        if (ii!=null) c.setEndseq(ii.intValue());
        c.setAlphabet(new String[alphabet.size()]);
        Enumeration e = alphabet.keys();
        while(e.hasMoreElements()) {
            String s = (String)e.nextElement();
            int j = ((Integer)alphabet.get(s)).intValue();
            if (s.equals("@")) s = "@"+c.getName();
            c.getAlphabet()[j] = s;
        }
        c.setStates(new EventState[c.getMaxStates()]);
        e = transitions.elements();
        while(e.hasMoreElements()) {
            Transition t = (Transition)e.nextElement();
            int  ev = ((Integer)alphabet.get(""+t.event)).intValue();
            c.getStates()[t.from] =EventState.add(c.getStates()[t.from],new EventState(ev,t.to));
        }
        if (sequentialInserts!=null)
        		c.expandSequential(sequentialInserts);
        if (relabels!=null)
            c.relabel(relabels);
        if (hidden!=null) {
            if (!exposeNotHide)
                c.conceal(hidden);
            else
                c.expose(hidden);
        }
        if (isProperty) {
            if (c.isNonDeterministic() || c.hasTau())
                Diagnostics.fatal("primitive property processes must be deterministic: "+name);
            c.makeProperty();
        }
        check_for_ERROR(c);
        c.reachable();
        if (isMinimal) {
          Minimiser me = new Minimiser(c,output);
          c = me.minimise();
        }
        if (isDeterministic) {
           Minimiser md = new Minimiser(c,output);
           c = md.trace_minimise();
        }
        return c;
    }
    
    
   // is the first state = ERROR ie P = ERROR?
   void check_for_ERROR(CompactState c) {
   	 Integer I = (Integer)explicit_states.get(name);
   	 if (I.intValue()==Declaration.ERROR) 
      {
         c.setStates(new EventState[1]);
         c.setMaxStates(1);
         c.getStates()[0] 
           = EventState.add(c.getStates()[0],
                            new EventState(Declaration.TAU,Declaration.ERROR));
       }
   }
      
   void addSequential(Integer state, CompactState mach) {
   	   if (sequentialInserts==null) sequentialInserts = new Hashtable();
   	   sequentialInserts.put(state,mach);
   }
   
   void preAddSequential(Integer start, Integer end, CompactState mach) {
   	   if (preInsertsLast==null) preInsertsLast = new Hashtable();
   	   if (preInsertsMach==null) preInsertsMach = new Hashtable();
   	   preInsertsLast.put(start,end);
   	   preInsertsMach.put(start,mach);
   }
     
   private void insertSequential(int[] map) {
   		 if (preInsertsMach==null) return;
       Enumeration e = preInsertsMach.keys();
       while(e.hasMoreElements()) {
         Integer start = (Integer)e.nextElement();
         CompactState mach = (CompactState)preInsertsMach.get(start);
         Integer end = (Integer)preInsertsLast.get(start);
         Integer newStart = new Integer(map[start.intValue()]);
         mach.offsetSeq(
           newStart.intValue(),
           end.intValue()>=0 ? map[end.intValue()] : end.intValue()
         );
         addSequential(newStart,mach);
       }
   }
   
   private Integer number(Integer alias, Counter newLabel) {
       if (preInsertsMach==null) 
         return newLabel.label(); 
       CompactState mach = (CompactState)preInsertsMach.get(alias);
       if (mach == null) 
         return newLabel.label();
       return newLabel.interval(mach.getMaxStates());
   }
   
   private void crunch(int index, int[] map) {
       int newi = map[index];
       while (newi>=0 && newi != map[newi])
            newi = map[newi];
       map[index] = newi;
   }
          
   private void renumber() { //relabel states
        int map[] =new int[stateLabel.lastLabel().intValue()];
        for (int i = 0; i<map.length; ++i) 
             map[i]=i;
        //apply alias
        Enumeration e = aliases.keys();
        while (e.hasMoreElements()) {
          Integer targ  =  (Integer)e.nextElement();
          Integer alias =  (Integer)aliases.get(targ);
          map[targ.intValue()] = alias.intValue();
        }
        //crunch aliases
        for (int i = 0; i<map.length; ++i)
            crunch(i,map); 
        //renumber
        Counter newLabel = new Counter(0);
        Map<Integer,Integer> oldnew = new HashMap<Integer,Integer>();
        for (int i = 0; i<map.length; ++i) {
            Integer alias = new Integer(map[i]);
            if (!oldnew.containsKey(alias)) {
               Integer newi = map[i]>=0?number(alias,newLabel):new Integer(-1);
               oldnew.put(alias, newi);
               map[i] = newi.intValue();
            } else {
              Integer newi = (Integer)oldnew.get(alias);
              map[i] = newi.intValue();
            }
        }
        // create offset insert sequential processes
        insertSequential(map);
        // renumber state/local process lookip table
        
        for( Iterator<String> i = explicit_states.keySet().iterator() ; i.hasNext() ; ) {
			
            String s = i.next();
            Integer ii = (Integer)explicit_states.get(s);
            if (ii.intValue()>=0)
              explicit_states.put(s, new Integer(map[ii.intValue()]));
        }
        stateLabel = newLabel;
    }

    public void print(LTSOutput output) {
       // print name
       output.outln("PROCESS: "+name);
       // print alphabet
       output.outln("ALPHABET:");
       Enumeration e = alphabet.keys() ;
       while (e.hasMoreElements()) {
            String s = (String) e.nextElement();
            output.outln("\t"+alphabet.get(s)+"\t"+s);
       }
       // print states
       output.outln("EXPLICIT STATES:");
       
	   for( Iterator<String> i = explicit_states.keySet().iterator() ; i.hasNext() ; ) {
		   
            String s = i.next();
            output.outln("\t"+explicit_states.get(s)+"\t"+s);
       }
	   
       // print transitions
       output.outln("TRANSITIONS:");
       e = transitions.elements() ;
       while (e.hasMoreElements()) {
            Transition t = (Transition) e.nextElement();
            output.outln("\t"+t);
       }
    }

    static String paramString(Vector v) {
			int max = v.size() - 1;
			StringBuffer buf = new StringBuffer();
			Enumeration e = v.elements();
			buf.append("(");
			for (int i = 0 ; i <= max ; i++) {
			    String s = e.nextElement().toString();
			    buf.append(s);
			    if (i < max) {
				buf.append(",");
			    }
			}
			buf.append(")");
			return buf.toString();
    }

}
