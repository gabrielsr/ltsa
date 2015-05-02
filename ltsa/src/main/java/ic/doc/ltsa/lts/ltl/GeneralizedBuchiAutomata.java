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
import java.util.*;

import ic.doc.ltsa.common.ast.IFormula;
import ic.doc.ltsa.common.iface.IFormulaFactory;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.Diagnostics;
import gov.nasa.ltl.graph.Edge;
import gov.nasa.ltl.graph.Graph;


public class GeneralizedBuchiAutomata {
	
	List nodes;
	IFormula formula;
	IFormulaFactory fac;
	List untils;
	int maxId = -1;
	Node[] equivClasses;
	State[] states;
	int naccept;
	String name;
	LabelFactory labelFac;
	
	public GeneralizedBuchiAutomata(String n, IFormulaFactory f, Vector alphaExt) {
		fac = f;
		name = n;
		formula = f.getFormula();
		nodes = new ArrayList();
		labelFac = new LabelFactory(name, fac, alphaExt);
	}
	
	public void translate() {
		Node.setAut(this);
		Node.setFactory(fac);
		Transition.setLabelFactory(labelFac);
		naccept = fac.processUntils(formula,untils=new ArrayList());
		Node first  = new Node (formula);
		nodes = first.expand(nodes);
		states = makeStates();
	}
	
	public LabelFactory getLabelFactory() {
		 return labelFac;
	}
	
	public void printNodes(LTSOutput out) {
		/*
		 * Iterator i = nodes.iterator(); while(i.hasNext()) { Node n =
		 * (Node)i.next(); n.printNode(out); }
		 */
		// print states
		for (int ii = 0; ii<states.length; ii++) {
			if (states[ii]!=null && ii==states[ii].getId()){
				states[ii].print(out,naccept);
			}
		}
	}
	
	public int indexEquivalence(Node n) {
    int i;
    for(i = 0; i < maxId; i++) {
        if(equivClasses[i] == null)
            break;
        if(equivClasses[i].next.equals(n.next))
            return equivClasses[i].id;
    }
    if(i == maxId)
        Diagnostics.fatal("size of equivalence classes array was incorrect");
    equivClasses[i] = n;
    return equivClasses[i].id;
  }

    public State[] makeStates() {
      State astate[] = new State[maxId];
      equivClasses = new Node[maxId];
      Iterator i = nodes.iterator();
      while(i.hasNext()){
          Node node = (Node)i.next();
          node.equivId = indexEquivalence(node);
          node.makeTransitions(astate);
      }
      return astate;
    }
										
   int newId() { return ++maxId;}
   
    Graph Gmake()
    {
        Graph graph = new Graph();
        graph.setStringAttribute("type", "gba");
        graph.setStringAttribute("ac", "edges");
        if(states == null)
            return graph;
        int i = maxId;
        gov.nasa.ltl.graph.Node anode[] = new gov.nasa.ltl.graph.Node[i];
        for(int j = 0; j < i; j++)
            if(states[j] != null && j == states[j].getId())
            {
                anode[j] = new gov.nasa.ltl.graph.Node(graph);
                anode[j].setStringAttribute("label", "S" + states[j].getId());
            }

        for(int k = 0; k < i; k++)
            if(states[k] != null && k == states[k].getId())
                states[k].Gmake(anode, anode[k],naccept);

        if(naccept == 0)
            graph.setIntAttribute("nsets", 1);
        else
            graph.setIntAttribute("nsets", naccept);
        return graph;
    }

}

class State implements Comparable {
	private List<Transition> transitions;
	private int stateId;
	
	State(List<Transition> t, int id) {
		transitions = t;
		stateId = id;
	}
	
	State() {
		this(new LinkedList<Transition>(), -1);
	}
	
	State(int id) {
		this(new LinkedList<Transition>(), id);
	}
	
	void setId(int id) {stateId = id;}
  int getId() {return stateId;}
	
  public int compareTo(Object obj) {
        return this != obj ? 1 : 0;
  }

  public void add(Transition t) {
      transitions.add(t);
  }
  
  void print(LTSOutput out, int nacc) {
  	  out.outln("STATE "+stateId);
  	  Iterator i = transitions.iterator();
  	  while(i.hasNext())
  	  	   ((Transition)i.next()).print(out,nacc);
  }
  
  void Gmake(gov.nasa.ltl.graph.Node anode[], gov.nasa.ltl.graph.Node node, int nacc)
    {
        ListIterator listiterator = transitions.listIterator(0);
        boolean flag = true;
        Transition transition;
        for(; listiterator.hasNext(); transition.Gmake(anode, node, nacc))
            transition = (Transition)listiterator.next();

    }


}

class Transition {
	 SortedSet propositions;
   int pointsTo;
   BitSet accepting;
   boolean safe_acc;
   
   static LabelFactory lf;
   
   static void setLabelFactory(LabelFactory f) {lf = f;}

  Transition(SortedSet p, int i, BitSet acc, boolean sa) {
      propositions = p;
      pointsTo = i;
      accepting = new BitSet();
      accepting.or(acc);
      safe_acc = sa;
    }

  int goesTo() {
     return pointsTo;
  }
  
  BitSet computeAccepting(int nacc) {
  	   BitSet b = new BitSet(nacc);
  	   for (int i = 0; i<nacc; ++i) 
  	   	if (!accepting.get(i)) b.set(i);
  	   return b;
  }
  
  void print(LTSOutput out, int nacc) {
  	  if (propositions.isEmpty()) 
  	  	   out.out("LABEL True");
  	  	else
  	      Node.printFormulaSet(out,"LABEL",propositions);
  	  out.out(" T0 "+goesTo());
  	  if (nacc>0)
  	  	   out.outln(" Acc "+computeAccepting(nacc));
  	  	else if (safe_acc)
  	  		 out.outln(" Acc {0}");
  	  else
  	      out.outln("");
  }
  
  
  
   void Gmake(gov.nasa.ltl.graph.Node anode[], gov.nasa.ltl.graph.Node node, int nacc)
    {
        String s = "-";
        String s1 = "-";
        if(!propositions.isEmpty())
        {
            s = lf.makeLabel(propositions);
        } 
        Edge edge = new Edge(node, anode[pointsTo], s, s1);
        if(nacc == 0)
        {
 // if(safe_acc)
                edge.setBooleanAttribute("acc0", true);
        } else
        {
            for(int i = 0; i < nacc; i++)
                if(!accepting.get(i))
                    edge.setBooleanAttribute("acc" + i, true);

        }
    }


    
}


  	   
	
