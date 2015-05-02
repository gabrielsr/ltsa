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
import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.IFluentTrace;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.Relation;
import ic.doc.ltsa.lts.ltl.FluentTrace;

import java.util.*;

public class CompositeState implements ICompositeState {

    public static boolean reduceFlag = true;

    public String name;
    public Vector machines;      // set of CompactState from which this can be composed
    public CompactState composition; // the result of a composition;
    public Vector hidden;        // set of actions concealed in composed version
    public boolean exposeNotHide = false;  // expose rather than conceal
    public boolean priorityIsLow = true;
    public boolean makeDeterministic = false; //construct equivalent DFA if NDFA
    public boolean makeMinimal = false;
    public boolean makeCompose =  false;   //force composition if true
    public boolean isProperty = false;
    public Vector priorityLabels;  // set of actions given priority
    public CompactState alphaStop; //stop process with alphbet of the composition  
    protected Vector errorTrace = null;
    
    private FluentTrace tracer;
    
    public CompositeState() {}
    
    public CompositeState (Vector v) {
        name = "DEFAULT";
        machines = v;
    }

    public CompositeState (String s, Vector v) {
        name = s;
        machines = v;
        initAlphaStop();
    }
    
    public List getErrorTrace() {return errorTrace;}
    
    public IFluentTrace getTracer() { return tracer; }
    
    public void setErrorTrace(List ll) {
    	if (ll!=null) {
    	   errorTrace = new Vector();
         errorTrace.addAll(ll);
    	}
    }

	public void compose (LTSOutput output)  {
		compose(output,false);
	}
	
    public void compose(LTSOutput output, boolean ignoreAsterisk) {
        if(machines!=null && machines.size()>0) {
            Analyser a = new Analyser(this,output,null,ignoreAsterisk);
            composition = a.composeNoHide();
            if (makeDeterministic) {
                applyHiding();
                determinise(output);
            } else if (makeMinimal) {
                applyHiding();
                minimise(output);
            } else
                applyHiding();
        }
    }
    
    private void applyHiding(){
       if (composition==null) return;
        if (hidden!=null) {
          if (!exposeNotHide)
              composition.conceal(hidden);
          else
              composition.expose(hidden);
        }
    }

    public void analyse( LTSOutput output ) {
        analyse( output, true );
    }

    public void analyse(LTSOutput output , boolean checkDeadlocks ) {

    	if (saved!=null) { machines.remove(saved); saved =null;}
        if(composition!=null) {
            CounterExample ce = new CounterExample(this);
            ce.print(output , checkDeadlocks );
            errorTrace = ce.getErrorTrace();

        } else {
        	  Analyser a = new Analyser(this,output,null);
        	  a.analyse();
        	  setErrorTrace(a.getErrorTrace());
        }
    }

    public void checkProgress(LTSOutput output) {
    	  ProgressCheck cc;
    	  	if (saved!=null) { machines.remove(saved); saved =null;}
        if(composition!=null) {
            cc = new ProgressCheck(composition,output);
            cc.doProgressCheck();
        } else {
        	IAutomata a = new Analyser(this,output,null);
            cc = new ProgressCheck(a,output);
            cc.doProgressCheck();
        }
        errorTrace = cc.getErrorTrace();
    }
    
    private ICompactState saved = null;
    
    public void checkLTL(LTSOutput output, ICompositeState cs) {
	   ICompactState ltl_property = cs.getComposition();
	   if (name.equals("DEFAULT") && machines.size()==0) {
	   	  //debug feature for producing consituent machines
	      machines = cs.getMachines();
	      composition = (CompactState)cs.getComposition();
	   } else {
    	   if (saved!=null) machines.remove(saved);
    	   Vector saveHidden = hidden;
    	   boolean saveExposeNotHide = exposeNotHide;
    	   hidden = ltl_property.getAlphabetV();
    	   exposeNotHide = true;
	       machines.add(saved = ltl_property);
	       Analyser a = new Analyser(this,output,null);
		   if (!cs.getComposition().hasERROR()) {
		   	    // do full liveness check
		        ProgressCheck cc = new ProgressCheck(a,output,cs.getTracer() );
		        cc.doLTLCheck();
		        errorTrace = cc.getErrorTrace();
		   } else {
		   	    // do safety check
			 	a.analyse((FluentTrace)cs.getTracer());
			 	setErrorTrace(a.getErrorTrace());
		   }
	       hidden = saveHidden;
	       exposeNotHide = saveExposeNotHide;
      }
    }

    public void minimise(LTSOutput output) {
        if(composition!=null) {
           //change (a ->(tau->P|tau->Q)) to (a->P | a->Q) and (a->tau->P) to a->P
           if (reduceFlag) composition.removeNonDetTau();
           Minimiser e = new Minimiser(composition,output);
           composition = e.minimise();
        }
    }

    public void determinise(LTSOutput output) {
        if(composition!=null) {
           Minimiser d = new Minimiser(composition,output);
           composition = d.trace_minimise();
           if (isProperty) composition.makeProperty();
        }
    }

    public CompactState create(LTSOutput output) {
        compose(output);
        return composition;
    }

    public boolean needNotCreate() {
        return (hidden==null && priorityLabels==null
                && !makeDeterministic
                && !makeMinimal
                && !makeCompose);
    }

    /*
    * prefix all consituent machines
    */
    public void prefixLabels(String prefix) {
        name = prefix+":"+name;
        alphaStop.prefixLabels(prefix);
        for (Enumeration ee = machines.elements(); ee.hasMoreElements();) {
            CompactState mm = (CompactState)ee.nextElement();
            mm.prefixLabels(prefix);
        }
    }

    /*
    * add prefix set to all constitutent machines
    */
    public void addAccess(Vector pset) {
        int n = pset.size();
        if (n==0) return;
        String s = "{";
        Enumeration e =  pset.elements();
        int i =0;
        while (e.hasMoreElements()) {
            String prefix = (String)e.nextElement();
            s = s + prefix;
            i++;
            if (i<n) s = s+",";
        }
        //new name
        name = s+"}::"+name;
        alphaStop.addAccess(pset);
        for (Enumeration ee = machines.elements(); ee.hasMoreElements();) {
            CompactState mm = (CompactState)ee.nextElement();
            mm.addAccess(pset);
        }
    }

    /*
    * relabel all constituent machines
    * checks to see if it is safe to leave uncomposed
    * if a relabeling causes synchronization, then the composition is
    * formed before relabelling is applied
    */
    public CompactState relabel(Relation oldtonew, LTSOutput output) {
        alphaStop.relabel(oldtonew);
        if (alphaStop.relabelDuplicates() && machines.size()>1) {
            // we have to do the composition, before relabelling
            compose(output);
            composition.relabel(oldtonew);
            return composition;
        } else {
            for (Enumeration ee = machines.elements(); ee.hasMoreElements();) {
                CompactState mm = (CompactState)ee.nextElement();
                mm.relabel(oldtonew);
            }
        }
        return null;
    }

    /*
    * initialise the alphaStop process
    */
    protected void initAlphaStop() {
        alphaStop = new CompactState();
        alphaStop.setName(name);
        alphaStop.setMaxStates(1);
        alphaStop.setStates(new EventState[alphaStop.getMaxStates()]); // statespace for STOP process
        alphaStop.getStates()[0] = null;
        // now define alphabet as union of constituents
        Hashtable alpha = new Hashtable();
        for (Enumeration e = machines.elements(); e.hasMoreElements();) {
            CompactState m = (CompactState)e.nextElement();
            for (int i=1; i<m.getAlphabet().length; ++i)
                alpha.put(m.getAlphabet()[i],m.getAlphabet()[i]);
        }
        alphaStop.setAlphabet(new String[alpha.size()+1]);
        alphaStop.getAlphabet()[0] = "tau";
        int j =1;
        for (Enumeration e = alpha.keys(); e.hasMoreElements();) {
            String s  = (String)e.nextElement();
            alphaStop.getAlphabet()[j] = s;
            ++j;
        }
    }
	
	
	
	
	public void setFluentTracer(ic.doc.ltsa.lts.ltl.FluentTrace ft) {
		tracer = ft;
	}
	
	public ic.doc.ltsa.lts.ltl.FluentTrace getFluentTracer()  {
		return tracer;
	}

    public ICompactState getComposition() {

        return composition;
    }
    
    public Vector getMachines() {
        
        return machines;
    }

    public String getName() {

        return name;
    }

    public void setReduction(boolean b) {

        reduceFlag = b; 
    }

	
}
