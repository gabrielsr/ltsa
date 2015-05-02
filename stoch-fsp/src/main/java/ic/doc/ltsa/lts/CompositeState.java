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
import ic.doc.ltsa.common.iface.IAutomata;
import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.IFluentTrace;
import ic.doc.ltsa.common.iface.LTSOutput;

import java.util.*;

/** A CompositeState is a collection of CompactStates which have been
 *  chosen to be composed.
 * */

public class CompositeState implements ICompositeState {
	
	public static boolean minimiseFlag = false;
	public static boolean inc_min_Flag = false;
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
    
    private ic.doc.ltsa.lts.ltl.FluentTrace tracer;
    
    protected Vector errorTrace = null;
    
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
    
    public void setErrorTrace(List ll) {
    	if (ll!=null) {
    	   errorTrace = new Vector();
         errorTrace.addAll(ll);
    	}
    }


    public void compose(LTSOutput pOutput, boolean pIgnoreAsterisk) {
		
        if ( machines!=null && machines.size() >0 ) {

		    Analyser a = new StochasticAnalyser( this , pOutput , null );         //, x_use_prob , ignoreAsterisk );
	
	        composition = a.composeNoHide();
	        if (makeDeterministic) {
	            applyHiding();
	            determinise(pOutput);
	        } else if (makeMinimal && !incrementalMinimization(pOutput)) {
				 applyHiding();
				 //minimise(pOutput);
				 minimise(pOutput);
	        } else {
	            applyHiding();
			}
		}

    }
    
    public void compose (LTSOutput output)  {

		compose(output,false);
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

	boolean incrementalMinimization(LTSOutput output) {
	  if (!inc_min_Flag) return false;
	  if (hidden == null) return false;
	  if (composition==null) return false;
	  if (composition.maxStates<100) return false;
	  Vector toHide = (exposeNotHide)?composition.hide(hidden):hidden;
	  if (toHide.size()<=1) return false;
	  output.outln("***** Doing incremental minimization *****");
	  long start =System.currentTimeMillis();
	  CompactState target = composition;
	  Enumeration e = toHide.elements();
	  while (e.hasMoreElements()) {
		  Vector hideSome = new Vector(10);
		  boolean domin = false;
		  while(!domin && e.hasMoreElements()) {
			  String label = (String)e.nextElement();
			  //output.outln(label);
			  hideSome.addElement(label);
			  if (target.maxStates<=100)  
				  domin = false;
			  else
				  domin = target.usesLabel(label);
		  }
		  target.conceal(hideSome);
		  // change minimiser to stochastic minimiser
		  Minimiser mi = new Minimiser(target,output);
		  target = mi.minimise();
	  }
	  composition = target;
	  long finish =System.currentTimeMillis();
	  output.outln("Incremental Minimization took: "+(finish-start)+"ms");
	  return true;
	}

	public void analyse(LTSOutput output) {
		analyse( output, true );
	}

    public void analyse(LTSOutput output , boolean checkDeadlocks ) {
    	if (saved!=null) { machines.remove(saved); saved =null;}
        if(composition!=null) {
            CounterExample ce = new CounterExample(this);
            ce.print(output , checkDeadlocks );
            errorTrace = ce.getErrorTrace();
        } else {
        	// rudimentary hack, change this!!
		//	Analyser a = nAnalyser.getInstance(this,output,null,((CompactState)machines.firstElement()).getStates()[0].getProb() > 0);
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
    
    private CompactState saved = null;
    
//     public void checkLTL(LTSOutput output, CompositeState cs) {
//     	   CompactState ltl_property = cs.composition;
//     	   if (name.equals("DEFAULT") && machines.size()==0) {
//     	      machines = cs.machines;
//     	      composition = cs.composition;
//     	   } else {
// 	    	   if (saved!=null) machines.remove(saved);
// 	    	   Vector saveHidden = hidden;
// 	    	   boolean saveExposeNotHide = exposeNotHide;
// 	    	   hidden = ltl_property.getAlphabetV();
// 	    	   exposeNotHide = true;
// 	        machines.add(saved = ltl_property);
// 	        Automata a = new Analyser(this,output,null);
// 	        ProgressCheck cc = new ProgressCheck(a,output);
// 	        cc.doLTLCheck();
// 	        errorTrace = cc.getErrorTrace();
// 	        hidden = saveHidden;
// 	        exposeNotHide = saveExposeNotHide;
//     	   }
//     }
    
    public void checkLTL(LTSOutput output, CompositeState cs) {
	   CompactState ltl_property = cs.composition;
	   if (name.equals("DEFAULT") && machines.size()==0) {
	   	  //debug feature for producing consituent machines
	      machines = cs.machines;
	      composition = cs.composition;
	   } else {
    	   if (saved!=null) machines.remove(saved);
    	   Vector saveHidden = hidden;
    	   boolean saveExposeNotHide = exposeNotHide;
    	   hidden = ltl_property.getAlphabetV();
    	   exposeNotHide = true;
	       machines.add(saved = ltl_property);
	       Analyser a = new Analyser(this,output,null);
		   if (!cs.composition.hasERROR()) {
		   	    // do full liveness check
		        ProgressCheck cc = new ProgressCheck(a,output,cs.tracer);
		        cc.doLTLCheck();
		        errorTrace = cc.getErrorTrace();
		   } else {
		   	    // do safety check
			 	a.analyse(cs.tracer);
			 	setErrorTrace(a.getErrorTrace());
		   }
	       hidden = saveHidden;
	       exposeNotHide = saveExposeNotHide;
      }
    }

    public void minimise(LTSOutput output) {
		
        if(composition!=null) {
           //change (a ->(tau->P|tau->Q)) to (a->P | a->Q) and (a->tau->P) to a->P
           //if (reduceFlag) composition.removeNonDetTau();
           Minimiser e = new StochasticMinimiser(composition,output);
		   composition = e.minimise();
        }
		        
//           if (composition.getStates()[0].getProb() > 0)
//           	e = new StochasticMinimiser(composition,output);
//           else
//           	e = new Minimiser(composition,output);
//           composition = e.minimise();

    }

    public void determinise(LTSOutput output) {
        if(composition!=null) {
        	//TODO find all functions that call determinise n change to stocMin
        	// changes made here
           TauRemover.removeTau(composition,output);
           Minimiser d = new StochasticMinimiser(composition,output);
           composition = d.trace_minimise();
           if (isProperty) 
        	   		composition.makeProperty();
        }
    }

    public CompactState create(LTSOutput output) {
        compose(output);
		if (minimiseFlag &&	!(makeDeterministic || makeMinimal)) 
			minimise(output);
        return composition;
    }

    public boolean needNotCreate() {
        return (!minimiseFlag && hidden==null && priorityLabels==null
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
        alphaStop.maxStates = 1;
        alphaStop.setStates(new EventState[alphaStop.maxStates]); // statespace for STOP process
        alphaStop.getStates()[0] = null;
        // now define alphabet as union of constituents
        Hashtable alpha = new Hashtable();
        for (Enumeration e = machines.elements(); e.hasMoreElements();) {
            CompactState m = (CompactState)e.nextElement();
            for (int i=1; i<m.alphabet.length; ++i)
                alpha.put(m.alphabet[i],m.alphabet[i]);
        }
        alphaStop.alphabet = new String[alpha.size()+1];
        alphaStop.alphabet[0] = "tau";
        int j =1;
        for (Enumeration e = alpha.keys(); e.hasMoreElements();) {
            String s  = (String)e.nextElement();
            alphaStop.alphabet[j] = s;
            ++j;
        }
    }

   
    
    public void setFluentTracer(ic.doc.ltsa.lts.ltl.FluentTrace ft) {
        tracer = ft;
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

    public void checkLTL(LTSOutput arg0, ICompositeState arg1) {

        // not implemented        
    }

    public IFluentTrace getTracer() {
        
        return tracer;
    }

    public void setReduction(boolean red) {

        reduceFlag = red;
        
    }
}
