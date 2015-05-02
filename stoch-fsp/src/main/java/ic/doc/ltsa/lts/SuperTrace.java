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
import ic.doc.ltsa.common.iface.ISuperTrace;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.MyList;

import java.util.*;


/*
*  implements Holtzmann's SuperTrace Partial Search
*/

public class SuperTrace implements ISuperTrace {
	
	BitSet table;
	MyStack stack;
	int nbits;
	IAutomata mach;
	LTSOutput output;
	LinkedList errorTrace;
	private static int DEPTHBOUND = 100000;
	private static int HASHSIZE = 8000;  //Kilobytes
	
	public SuperTrace() {}
	
	public void setDepthBound(int depth) {
		   DEPTHBOUND = depth;
	}
	
	public int getDepthBound() {return DEPTHBOUND;}
	
	public void setHashSize(int size) {
		   HASHSIZE = size;
	}
	
	public int getHashSize() {return HASHSIZE;}

	
	private final static int SUCCESS = 0;
  private final static int DEADLOCK = 1;
  private final static int ERROR    =2;
	
	public SuperTrace(IAutomata mach, LTSOutput output) {
		this.mach = mach;
		this.output = output;
		nbits = HASHSIZE * 1024 * 8;
		table = new BitSet(nbits);
		stack = new MyStack();
		analyse();
	}
	
	public void analyse() {
        output.outln("Analysing using Supertrace (Depth bound "+DEPTHBOUND
                     +" Hashtable size "+HASHSIZE+"K )...");
        System.gc();        //garbage collect before start
        long start =System.currentTimeMillis();
        int ret = search();
        long finish = System.currentTimeMillis();
        outStatistics(stack.depth,nstate,nTrans);
        if (ret==DEADLOCK) {
           output.outln("Trace to DEADLOCK: (in superstrace)");
           errorTrace = computeTrace(false);
           if (errorTrace.size()<=100)
           			printPath(errorTrace);
           else
                output.outln("Trace length "+errorTrace.size()+", replay using Check/Run");
        } else if (ret==ERROR) {
           output.outln("Trace to property violation in "+mach.getViolatedProperty()+":");
           errorTrace = computeTrace(true);
           if (errorTrace.size()<=100)
           			printPath(errorTrace);
           else
                output.outln("Trace length "+errorTrace.size()+", replay using Check/Run");
        } else {
           output.outln("No deadlocks/errors");
        }
        output.outln("Analysed using Supertrace in: "+(finish-start) + "ms");
     }

	
	private int hashOne(byte[] value) {
		return StateCodec.hash(value);
	}
	
	private int hashTwo(byte[] val) {
		long value = StateCodec.hashLong(val);
		value += 1325656567898L;
		int h = (int)((value ^ (value >>> 32)));
	  return h & Integer.MAX_VALUE;
	}
	
	private void put(byte[] key) {
		 table.set(hashOne(key)%nbits);
		 table.set(hashTwo(key)%nbits);
	}
	
	private boolean contains(byte[] key) {
		return table.get(hashOne(key)%nbits) 
			     && table.get(hashTwo(key)%nbits);
	}
	
	int nstate = 0; //nstates analysed
	int nTrans = 0; //ntransitions examined
	
	private int search() {
		  byte[] start = mach.START();
		  MyHash onStack = null;
		  if (mach.isPartialOrder()) {
		  	   onStack = new MyHash(DEPTHBOUND+1);
		  	   mach.setStackChecker(onStack);
		  }
		  stack.push(start);
		  put(start);
		  while (!stack.empty()) {
		  	if (stack.marked()) {
		  		 if (onStack!=null) onStack.remove(stack.peek());
		  		 stack.pop();
		  	} else {
		  		++nstate;
		  		if (nstate%10000 ==0) 
    	  	   	  outStatistics(stack.getDepth(),nstate,nTrans);
    	    	byte[] currentState = stack.peek();
		  	  stack.mark();
		  	  if (onStack!=null) onStack.put(currentState);
		  	  MyList transitions = mach.getTransitions(currentState);
		  		if (transitions.isEmpty() && !mach.END(currentState)) return DEADLOCK;
    	  	while (!transitions.isEmpty()) {
  	  	  	  ++nTrans;
              if (transitions.getTo() == null) {
                  return ERROR;
              } else {
              	  if (stack.getDepth()<DEPTHBOUND) {
	              	  if (!contains(transitions.getTo())) {
	              	  	stack.push(transitions.getTo());
	              	  	put(transitions.getTo());
	              	  }
              	  }
              }
              transitions.next();
    	  	}
		  	}
		  }
		  return SUCCESS;
	}
	
 public List getErrorTrace() {return errorTrace;}
	
 private void outStatistics(int depth, int states, int transitions) { 	   
 	   output.out("-- Depth: "+depth+" States: "+states+" Transitions: "+transitions);
 	   Runtime r = Runtime.getRuntime(); 	   
     output.outln(" Memory used: "+(r.totalMemory()-r.freeMemory())/1000+"K");
 }
 
 private void printPath(LinkedList v) {
        Iterator t = v.iterator();
        while (t.hasNext())
            output.outln("\t"+(String)t.next());
 }

 private LinkedList computeTrace(boolean error) {
 	   mach.disablePartialOrder();
 	   LinkedList trace = new LinkedList();
 	   //if last state was error
 	   if (error) {
	 	   while (!stack.marked()) stack.pop();
	 	   trace.addFirst(findAction(stack.peek(),null));
 	   }
 	   byte[] to = stack.pop();
 	   while (!stack.empty()) {
 	   	  if (!stack.marked()) {
 	   	  	stack.pop();
 	   	  } else {
 	   	  	trace.addFirst(findAction(stack.peek(),to));
 	   	  	to = stack.pop();
 	   	  }
 	   }
 	   return trace;
 }   	
 	   	
 private String findAction(byte[] from, byte[] to) {
 	   MyList t = mach.getTransitions(from);
 	   while (!t.isEmpty()) {
 	   	  if (StateCodec.equals(t.getTo(),to)){
 	   	  	 return (mach.getAlphabet())[t.getAction()];
 	   	  }
 	   	  t.next();
 	   }
 	   return "ACTION NOT FOUND";
 }

public void setOutput(LTSOutput window) {

    output = window;
    
}

public void setAnalyser(IAutomata a) {

    mach = a;
}

 	   

	
}
              	  
		  		
		  		
		
	
	
	
	

	
	

	
	
