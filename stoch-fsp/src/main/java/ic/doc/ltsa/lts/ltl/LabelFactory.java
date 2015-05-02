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

import ic.doc.ltsa.common.iface.IFormulaFactory;
import ic.doc.ltsa.lts.*;

public class LabelFactory {
	
	SortedSet allprops;
	IFormulaFactory fac;
	Vector alphaX;
	String name;
	HashMap tr;              //transition sets
	BitSet[] ps;             //proposition sets
	BitSet[] nps;            //not proposition sets
	Vector propProcs;          //process per proposition
	SortedSet allActions;    //application alphabet of all propositions
	
	public LabelFactory(String n, IFormulaFactory f, Vector alphaExtension) {
		allprops = f.getProps();
		fac = f;
		name =n;
		alphaX = alphaExtension;
		tr = new HashMap();
		initPropSets();
	  compileProps();
	}
	
	HashMap getTransLabels() {
		return tr;
	}
	
	Vector getPrefix() {
		Vector v = new Vector();
		Formula f = (Formula)allprops.first();
		v.add("_"+f);
		return v;
	}
	
	String makeLabel(SortedSet props) {
		StringBuffer sb = new StringBuffer();
 		Iterator ii = allprops.iterator();
		boolean isMore = false;
		BitSet labels = new BitSet();
		int m = 0;
		while(ii.hasNext()){
		    Formula f = (Formula)ii.next();
		    if (props.contains(f)) {
		    	   if (isMore) {
		    	   	sb.append("&");
		    	   	labels.and(ps[m]);
		    	   } else {
		    	   	labels.or(ps[m]);
		    	   	isMore = true;
		    	   }
		    	   sb.append(f);
		    } else if (props.contains(fac.makeNot(f))) {
		      	if (isMore) {
		    	   	sb.append("&");
		    	   	labels.and(nps[m]);
		    	   } else {
		    	   	labels.or(nps[m]);
		    	   	isMore = true;
		    	   }
		    	   sb.append("!"+f);
		    } 
		    ++m;
		}
		String s =  sb.toString();
		tr.put(s,labels);
		return s;
	}
	
	public String[] makeAlphabet() {
		return makeAlphabet(null,null,null);
	}
	
  private String[] makeAlphabet(PredicateDefinition p, BitSet tt, BitSet ft) {
  	  int extra = 0;
  	  if (p==null) {
  	  	    extra = 1; //accept label
  	  } else {
  	  	    extra = p.trueActions.size() + p.falseActions.size();
  	  }
		int len = (1<<allprops.size())+1+extra; //labels + tau + extra
		String alpha[] = new String[len];
		for(int i=0; i<len-extra; i++) {
			StringBuffer sb = new StringBuffer();
			Iterator ii = allprops.iterator();
			boolean isMore = false;
			int m =0;
			while(ii.hasNext()){
				Formula f = (Formula)ii.next();
				if (isMore) sb.append(".");
			  isMore = true;
				sb.append("_"+f+"."+(i>>m)%2);
				++m;
			}
			alpha[i+1] = sb.toString();
		}
		alpha[0] = "tau";
		if (p==null) {
		   alpha[len-1]="@"+name;
		} else {
			 int pos = len-extra;
			 Iterator ii = p.falseActions.iterator();
			 while (ii.hasNext()) {
			 	alpha[pos] = (String)ii.next();
			 	ft.set(pos);
			 	++pos;
			 }
			 ii = p.trueActions.iterator();
			 while (ii.hasNext()) {
			 	alpha[pos] = (String)ii.next();
			 	tt.set(pos);
			 	++pos;
			 }
		}
		return alpha;
	}
	
	void initPropSets() {
		  int len = allprops.size();
		  ps = new BitSet[len];
		  nps = new BitSet[len];
		  BitSet trueSet = new BitSet(1<<len);
		  for(int m = 0; m<len; ++m) {
		  	   ps[m] = new BitSet(1<<len);
		  	   nps[m] = new BitSet(1<<len);
		  }
		  	for(int i = 0; i<(1<<len); ++i) {
		  		trueSet.set(i);
		  		for (int m = 0; m<len ; ++m)
		  			if ( ((i>>m)%2) ==1) {
		  				ps[m].set(i);
		  			} else {
		  				nps[m].set(i);
		  			}
		  	}
		  	tr.put("true",trueSet);
	}
	
	private List fluents = new ArrayList();
	
	public PredicateDefinition[]  getFluents()  {
		if (fluents.size() == 0) return null;
		PredicateDefinition[] pds = new PredicateDefinition[fluents.size()];
		for(int i=0; i<pds.length; ++i)
			pds[i] = (PredicateDefinition)fluents.get(i);
	    return pds;
	}
	
	protected void compileProps() {
		propProcs = new Vector();
		allActions = new TreeSet();
		//Pass 1 PredicateDefinition processes
		Iterator ii = allprops.iterator();
		int m = 0;
		while (ii.hasNext()) {
			Proposition f = (Proposition)ii.next();
			PredicateDefinition p = PredicateDefinition.get(f.toString());
			if (p!=null)  {
				fluents.add(p);
			   allActions.addAll(p.trueActions);
		       allActions.addAll(p.falseActions);
		       propProcs.add(makePropProcess(p,m));
			} else if (fac.getActionPredicates() !=null && fac.getActionPredicates().containsKey(f.toString()))  {
				// only add to alphabet this pass
				Vector vl = (Vector)fac.getActionPredicates().get(f.toString());
				allActions.addAll(vl);
			} else		
				Diagnostics.fatal("Proposition "+f+" not found",f.sym);
		   ++m;
		}
	    if (alphaX!=null) allActions.addAll(alphaX);
		//Pass 2 Action Predicate processes
		ii = allprops.iterator();
		m = 0;
		while (ii.hasNext()) {
			Proposition f = (Proposition)ii.next();
			PredicateDefinition p = PredicateDefinition.get(f.toString());
			if (p!=null)  {
			   // do nothing this pass
			} else if (fac.getActionPredicates() !=null && fac.getActionPredicates().containsKey(f.toString()))  {
				Vector trueActions = (Vector)fac.getActionPredicates().get(f.toString());
				Vector falseActions = new Vector();
				falseActions.addAll(allActions);
				falseActions.removeAll(trueActions);
				p = new PredicateDefinition(new Symbol(Symbol.UPPERIDENT,f.toString()),trueActions,falseActions);
				CompactState cs = makePropProcess(p,m);
				propProcs.add(cs);
			} else		
				Diagnostics.fatal("Proposition "+f+" not found",f.sym);
		   ++m;
		}
		// make sync process
		propProcs.add(makeSyncProcess());
	}
	
	CompactState makePropProcess(PredicateDefinition p, int m) {
		CompactState cs = new CompactState();
		cs.setName(p.name.toString());
		cs.maxStates = 2;
		cs.setStates(new EventState[cs.maxStates]);
		BitSet trueTrans = new BitSet();
		BitSet falseTrans = new BitSet();
		cs.alphabet = makeAlphabet( p, trueTrans, falseTrans);
		int falseS = p.initial?1:0;
		int trueS  = p.initial?0:1;
		for (int i=0; i<trueTrans.size(); ++i)
			if (trueTrans.get(i))
				 cs.getStates()[falseS] = EventState.add(cs.getStates()[falseS], new EventState(i,trueS));
	  for (int i=0; i<falseTrans.size(); ++i)
			if (falseTrans.get(i))
				 cs.getStates()[trueS] = EventState.add(cs.getStates()[trueS], new EventState(i,falseS));
	  for (int i=0; i<falseTrans.size(); ++i)
	  	   if (falseTrans.get(i))
				 cs.getStates()[falseS] = EventState.add(cs.getStates()[falseS], new EventState(i,falseS)); 
	  for (int i=0; i<trueTrans.size(); ++i)
			if (trueTrans.get(i))
				 cs.getStates()[trueS] = EventState.add(cs.getStates()[trueS], new EventState(i,trueS));
	  for (int i=0; i<nps[m].size(); ++i)
	  	   if (nps[m].get(i))
				 cs.getStates()[falseS] = EventState.add(cs.getStates()[falseS], new EventState(i+1,falseS)); 
	  for (int i=0; i<ps[m].size(); ++i)
			if (ps[m].get(i))
				 cs.getStates()[trueS] = EventState.add(cs.getStates()[trueS], new EventState(i+1,trueS));
		return cs;
	}	
	
		  	   
	CompactState makeSyncProcess() {
		CompactState cs = new CompactState();
		cs.setName("SYNC");
		cs.maxStates = 2;
		cs.setStates(new EventState[cs.maxStates]);
		String [] propA = makeAlphabet(); 
		String [] appA  = new String[allActions.size()];
		int ind =0;
		for (Iterator ii=allActions.iterator(); ii.hasNext(); appA[ind++] = (String)ii.next());
		cs.alphabet = new String[propA.length-1+appA.length];
		cs.alphabet[0] = "tau";
		for (int i = 1; i<(propA.length-1); ++i) {
			 cs.alphabet[i] = propA[i];
			 cs.getStates()[1] = EventState.add(cs.getStates()[1], new EventState(i,0));
		}
		for (int i = 0; i<appA.length; ++i) {
			 cs.alphabet[i+propA.length-1] = appA[i];
			 cs.getStates()[0] = EventState.add(cs.getStates()[0], new EventState(i+propA.length-1,1));
		}
	  return cs; 
	}

}
