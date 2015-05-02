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
 
package ic.doc.ltsa.lts.ltl;
import ic.doc.ltsa.common.infra.Diagnostics;
import ic.doc.ltsa.lts.*;
import gov.nasa.ltl.graph.*;
import java.util.*;
import java.io.*;

class Converter extends CompactState{
	
	BitSet accepting;
	Graph g;
	int iacc = 0; //one if first state is accepting
	              //in this case first state is duplicated with state 1 accepting
				  //this allows for initialisation
	
	Converter(String n,Graph g, LabelFactory lf) {
		setName(n); 
		this.g = g;
		accepting = getAcceptance();
		// disable code for initial accepting state
		//iacc = accepting.get(0) ? 1 : 0;
		setAlphabet(lf.makeAlphabet());
		makeStates(lf);
	}
	
	private void makeStates(LabelFactory lf) {
		setMaxStates(g.getNodeCount()+ iacc + 1); //add extra node for completion
		setStates(new EventState[getMaxStates()]);
		HashMap trl = lf.getTransLabels();
		addTrueNode(getMaxStates()-1,trl);
		Iterator ii = g.getNodes().iterator();
		while (ii.hasNext()) {
         addNode((gov.nasa.ltl.graph.Node)ii.next(), trl);
		}
		if (iacc==1)  {
			getStates()[0] = EventState.union(getStates()[0],getStates()[1]);
		}
		addAccepting();
		reachable();
	}
	
	private void addAccepting()  {
		for (int id = 0; id<getMaxStates()-1; id++ )  {
			if (accepting.get(id)) {
				getStates()[id+iacc] = EventState.add(getStates()[id+iacc],new EventState(getAlphabet().length-1,id+iacc));
			}
		}
	}

	
	void addNode(gov.nasa.ltl.graph.Node n, HashMap trl) {
		int id = n.getId();
		BitSet all = new BitSet(getAlphabet().length-2);
		Iterator ii = n.getOutgoingEdges().iterator(); 
		while (ii.hasNext()) {
            addEdge((Edge)ii.next(),id,trl,all);
        }
        complete(id, all);
	}
	
	void addTrueNode(int id, HashMap trl) {
		BitSet tr = (BitSet)trl.get("true");
        for (int i = 0; i<tr.size(); ++i) {
        	  if (tr.get(i)) {
        	  	  getStates()[id] = EventState.add(getStates()[id], new EventState(i+1,id));
        	  }
        }
	}
	
	void complete(int id, BitSet all) {
     for (int i = 0; i<getAlphabet().length-2; ++i) {
        	  if (!all.get(i)) {
        	  	  getStates()[id+iacc] = EventState.add(getStates()[id+iacc], new EventState(i+1,getMaxStates()-1));
        	  }
        }
	}


	void addEdge(Edge e, int id, HashMap trl, BitSet all) {
        String s;
        if(e.getGuard().equals("-"))
            s = "true";
        else
            s = e.getGuard();
        BitSet tr = (BitSet)trl.get(s);
        all.or(tr);
        for (int i = 0; i<tr.size(); ++i) {
        	  if (tr.get(i)) {
        	  	  getStates()[id+iacc] = EventState.add(getStates()[id+iacc], new EventState(i+1,e.getNext().getId()+iacc));
        	  }
        }
    }
	
	
	public  void printFSP(PrintStream printstream)
    {
        boolean flag = false;
        if(g.getInit() != null)
        {
            printstream.print(getName()+" = S" + g.getInit().getId());
        } else
        {
            printstream.print("Empty");
            flag = true;
        }
        gov.nasa.ltl.graph.Node node;
        for(Iterator iterator = g.getNodes().iterator(); iterator.hasNext(); printNode(node,printstream))
        {
            printstream.println(",");
            node = (gov.nasa.ltl.graph.Node)iterator.next();
        }

        printstream.println(".");
        
        //printstream.println("AS = "+getAcceptance());
        
        if(printstream != System.out)
            printstream.close();
    }
    
    protected  BitSet getAcceptance() {
    	    BitSet acc = new BitSet();
        int i = g.getIntAttribute("nsets");
        if (i>0) Diagnostics.fatal("More than one acceptance set");
        for(Iterator iterator1 = g.getNodes().iterator(); iterator1.hasNext();)
        {
            gov.nasa.ltl.graph.Node node1 = (gov.nasa.ltl.graph.Node)iterator1.next();
            if(node1.getBooleanAttribute("accepting"))
                acc.set(node1.getId());
        }
        return acc;
    }
  
    
    void printNode(gov.nasa.ltl.graph.Node n, PrintStream printstream) {
    	   String s = accepting.get(n.getId()) ? "@" : "";
        printstream.print("S" + n.getId()+ s + " =(");
        for(Iterator iterator = n.getOutgoingEdges().iterator(); iterator.hasNext();)
        {
            printEdge((Edge)iterator.next(),printstream);
            if(iterator.hasNext())
                printstream.print(" |");
        }

        printstream.print(")");
    }
    
    void printEdge(Edge e, PrintStream printstream) {
    	   String s1 = "";
        String s;
        if(e.getGuard().equals("-"))
            s = "true";
        else
            s = e.getGuard();
        printstream.print(s + " -> S" + e.getNext().getId());
    }


    
}


	
