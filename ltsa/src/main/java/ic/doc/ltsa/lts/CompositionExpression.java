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

/* -----------------------------------------------------------------------*/
class CompositeBody {
    // single process reference P
    ProcessRef  singleton;
    // list of CompositeBodies ( P || Q)
    Vector procRefs;
    // conditional if Bexp then P else Q
    Stack boolexpr;
    CompositeBody thenpart; //overloaded as body of replicator
    CompositeBody elsepart;
    // forall[i:R][j:S].
    ActionLabels range;     // used to store forall range/ranges
    // the following are only  applied to singletons & procRefs (...)
    ActionLabels prefix;     // a:
    ActionLabels accessSet;  // S::
    Vector relabelDefns;     // list of relabelling defns

    private Vector accessors = null;
    private Relation relabels = null;

    void compose(CompositionExpression c, Vector machines, Hashtable locals) {
        Vector accessors  = accessSet==null? null : accessSet.getActions(locals,c.constants);
        Relation relabels = RelabelDefn.getRelabels(relabelDefns,c.constants,locals);
        //conditional compostion
        if (boolexpr!=null) {
            if (Expression.evaluate(boolexpr,locals,c.constants)!=0)
                thenpart.compose(c,machines,locals);
            else if (elsepart !=null)
               elsepart.compose(c,machines,locals);
        } else if (range!=null) {
        //replicated composition
            range.initContext(locals,c.constants);
            while(range.hasMoreNames()) {
                range.nextName();
                thenpart.compose(c,machines,locals);
            }
            range.clearContext();
        } else {
        //singleton or list
            Vector tempMachines = getPrefixedMachines(c,locals);
            // apply accessors
            if (accessors!=null)
                for(Enumeration e = tempMachines.elements(); e.hasMoreElements();) {
                    Object o = e.nextElement();
                    if (o instanceof CompactState) {
                        CompactState mach = (CompactState)o;
                        mach.addAccess(accessors);
                    } else {
                        CompositeState cs = (CompositeState)o;
                        cs.addAccess(accessors);
                    }
                }
            // apply relabels
            if (relabels!=null)
                for(int i = 0; i<tempMachines.size(); ++i) {
                    Object o = tempMachines.elementAt(i);
                    if (o instanceof CompactState) {
                        CompactState mach = (CompactState)o;
                        mach.relabel(relabels);
                    } else {
                        CompositeState cs = (CompositeState)o;
                        CompactState mm = cs.relabel(relabels,c.output);
                        if (mm!=null) tempMachines.setElementAt(mm,i);
                    }
                }
            // add tempMachines to machines
            for(Enumeration e = tempMachines.elements(); e.hasMoreElements();) {
                    machines.addElement(e.nextElement());
            }
        }
    }

    private Vector getPrefixedMachines(CompositionExpression c, Hashtable locals) {
        if (prefix==null) {
            return getMachines(c,locals);
        } else {
            Vector pvm = new Vector();
            prefix.initContext(locals,c.constants);
            while (prefix.hasMoreNames()) {
                String px = prefix.nextName();
                Vector vm = getMachines(c,locals);
                for(Enumeration e = vm.elements(); e.hasMoreElements();) {
                    Object o = e.nextElement();
                    if (o instanceof CompactState) {
                        CompactState m = (CompactState)o;
                        m.prefixLabels(px);
                        pvm.addElement(m);
                    } else {
                        CompositeState cs = (CompositeState) o;
                        cs.prefixLabels(px);
                        pvm.addElement(cs);
                    }
                }
            }
            prefix.clearContext();
            return pvm;
         }
    }

    private Vector getMachines(CompositionExpression c, Hashtable locals) {
        Vector vm = new Vector();
        if (singleton!=null) {
            singleton.instantiate(c,vm,c.output,locals);
        } else if (procRefs!=null) {
            Enumeration e = procRefs.elements();
            while(e.hasMoreElements()){
                CompositeBody cb = (CompositeBody)e.nextElement();
                cb.compose(c,vm,locals);
            }
        }
        return vm;
    }

}

class CompositionExpression {
    Symbol name;
    CompositeBody body;
    Hashtable constants;
    Hashtable init_constants = new Hashtable();  // constant table
    Vector parameters = new Vector();       // position of names in constants
    Map processes;                    // table of process definitions
    Map compiledProcesses;            // table of compiled definitions
    Map composites;                   // table of composite definitions
    LTSOutput output;                       // a bit of a hack
    boolean priorityIsLow = true;
    LabelSet priorityActions;                // priority action set
    LabelSet alphaHidden;                     // concealments
    boolean exposeNotHide=false;
    boolean makeDeterministic = false;
    boolean makeMinimal = false;
    boolean makeProperty = false;
    boolean makeCompose = false;

    CompositeState compose(Vector actuals) {
        Vector machines = new Vector(); // list of instantiated machines
        Hashtable locals = new Hashtable();
        constants = (Hashtable)init_constants.clone();
        Vector references;              // list of parsed process references
        if (actuals!=null) doParams(actuals);
        body.compose(this,machines,locals);
        Vector flatmachines = new Vector();
        for (Enumeration e = machines.elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof CompactState)
                flatmachines.addElement(o);
            else {
                CompositeState cs = (CompositeState)o;
                for (Enumeration ee = cs.machines.elements(); ee.hasMoreElements();) {
                    flatmachines.addElement(ee.nextElement());
                }
            }
        }
        String refname = (actuals==null)?name.toString() : name.toString()+StateMachine.paramString(actuals);
        CompositeState c = new CompositeState(refname,flatmachines);
        c.priorityIsLow = priorityIsLow;
        c.priorityLabels = computeAlphabet(priorityActions);
        c.hidden = computeAlphabet(alphaHidden);
        c.exposeNotHide = exposeNotHide;
        c.makeDeterministic = makeDeterministic;
        c.makeMinimal = makeMinimal;
        c.makeCompose = makeCompose;
        if (makeProperty) {c.makeDeterministic = true; c.isProperty = true;}
        return c;
    }

    private void doParams(Vector actuals) {
        Enumeration a = actuals.elements();
        Enumeration f = parameters.elements();
        while(a.hasMoreElements() && f.hasMoreElements())
            constants.put(f.nextElement(),a.nextElement());
    }

    private Vector computeAlphabet(LabelSet a) {
      if (a ==null) return null;
      return a.getActions(constants);
    }

}


class ProcessRef {
    Symbol name;
    Vector actualParams;                   // Vector of expressions stacks

    public void instantiate(CompositionExpression c, Vector machines, LTSOutput output, Hashtable locals) {
        //compute parameters
        Vector actuals = paramValues(locals,c);
        String refname = (actuals==null)? name.toString() : name.toString() + StateMachine.paramString(actuals);
        // have we already compiled it?
        CompactState mach = (CompactState)c.compiledProcesses.get(refname);
        if(mach!=null) {
            machines.addElement(mach.myclone());
            return;
        }
        // we have not got one so first see if its a process
        ProcessSpec p = (ProcessSpec)c.processes.get(name.toString());
        if (p!=null) {
            if (actualParams!=null) {  //check that parameter arity is correct
                if (actualParams.size()!=p.parameters.size())
                    Diagnostics.fatal ("actuals do not match formal parameters", name);
            }
            if (!p.imported()) {
	            StateMachine one = new StateMachine(p,actuals);
	            mach = one.makeCompactState();
            } else {
            	mach = new AutCompactState(p.name, p.importFile);
            }
            machines.addElement(mach.myclone());      // pass back clone
            c.compiledProcesses.put(mach.getName(),mach);  // add to compiled processes
            if (!p.imported()) 
            	c.output.outln("Compiled: "+mach.getName());
            else
              c.output.outln("Imported: "+mach.getName());
            return;
        }
		// it could be a constraint
		mach = ic.doc.ltsa.lts.ltl.AssertDefinition.compileConstraint(output,name,refname,actuals);
		if (mach!=null)  {
		    machines.addElement(mach.myclone());      // pass back clone
            c.compiledProcesses.put(mach.getName(),mach);  // add to compiled processes
			return;
		}
        // it must be a composition
        CompositionExpression ce = (CompositionExpression)c.composites.get(name.toString());
        if (ce==null) Diagnostics.fatal ("definition not found- "+name, name);
        if (actualParams!=null) {  //check that parameter arity is correct
             if (actualParams.size()!=ce.parameters.size())
             Diagnostics.fatal ("actuals do not match formal parameters", name);
        }
        CompositeState cs;
        if (ce==c) {
           Hashtable save = (Hashtable)c.constants.clone();
           cs = ce.compose(actuals);
           c.constants=save;
        } else
           cs = ce.compose(actuals);
        // dont compose if not necessary, maintain as a list of machines
        if (cs.needNotCreate()) {
            for(Enumeration e = cs.machines.elements();e.hasMoreElements();) {
                mach = (CompactState)e.nextElement();
                mach.setName(cs.name+"."+mach.getName());
            }
            machines.addElement(cs);  //flatten later if correct
         } else {
            mach = cs.create(output);
            c.compiledProcesses.put(mach.getName(),mach);  // add to compiled processes
            c.output.outln("Compiled: "+mach.getName());
            machines.addElement(mach.myclone()); //pass back clone
        }
    }

    private Vector paramValues(Hashtable locals, CompositionExpression c) {
        if (actualParams==null) return null;
        Enumeration e = actualParams.elements();
        Vector v = new Vector();
        while(e.hasMoreElements()) {
            Stack stk = (Stack)e.nextElement();
            v.addElement(Expression.getValue(stk,locals,c.constants));
        }
        return v;
    }

}
