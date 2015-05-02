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

import ic.doc.ltsa.common.iface.IAssertDefinition;
import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.IFormulaFactory;
import ic.doc.ltsa.common.iface.IFormulaSyntax;
import ic.doc.ltsa.common.iface.ISymbol;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.Diagnostics;
import ic.doc.ltsa.lts.*;

import java.util.*;
import java.io.*;
import gov.nasa.ltl.graph.*;

/* -----------------------------------------------------------------------*/

public class AssertDefinition implements IAssertDefinition {
    
    private ISymbol         name;
    private IFormulaFactory fac;
	private IFormulaSyntax  ltl_formula;
    private CompositeState cached;
	private LabelSet       alphaExtension;
	private Hashtable      init_params;   // initial parameter values name,value
	private Vector         params;        // list of parameter names

    static Hashtable definitions;
	static Hashtable constraints;
    
	public AssertDefinition(){}
	
    private AssertDefinition(ISymbol n, IFormulaSyntax f, LabelSet ls, Hashtable ip, Vector p){
    	  name = n;
    	  ltl_formula = f;
    	  cached = null;
		  alphaExtension = ls;
		  init_params = ip;
		  params = p;
    }
    
    public static void put(ISymbol n, IFormulaSyntax f, LabelSet ls, Hashtable ip, Vector p, boolean isConstraint) {
    	    if(definitions==null) definitions = new Hashtable();
			if(constraints==null) constraints = new Hashtable();
			if (!isConstraint)  {
    	       if(definitions.put(n.toString(),new AssertDefinition(n,f,ls, ip, p))!=null) 
                 Diagnostics.fatal ("duplicate LTL property definition: "+n, n);
    	    } else  {
			   if(constraints.put(n.toString(),new AssertDefinition(n,f,ls, ip, p))!=null) 
                 Diagnostics.fatal ("duplicate LTL constraint definition: "+n, n);
            } 
    }
	    
    public static void init(){
    	  definitions = null;
		  constraints = null;
    }
    
    public String[] getNames() {
        if (definitions==null) return null;
        int n = definitions.size();
        String na[];
        if (n==0) return null; else na = new String[n];
        Enumeration e = definitions.keys();
        int i = 0;
        while (e.hasMoreElements())
            na[i++] = (String)e.nextElement();
        return na;
    }
	
	public static void compileAll(LTSOutput output)  {
		compileAll(definitions, output);
		compileAll(constraints, output);
	} 
	
	private static void compileAll(Hashtable definitions, LTSOutput output)  {
		if (definitions == null) return;
		Enumeration e = definitions.keys();
        while (e.hasMoreElements())  {
             String name = (String)e.nextElement();
			 AssertDefinition p = (AssertDefinition)definitions.get(name);
			 p.fac = new FormulaFactory();
			 p.fac.setFormula(p.ltl_formula.expand(p.fac,new Hashtable(),p.init_params));
        }
    }
	
    public ICompositeState compile(LTSOutput output, String asserted){
		return compile(definitions,output,asserted);
    }
	
	public static void compileConstraints(LTSOutput output, Hashtable compiled)  {
		if (constraints==null) return;
		Enumeration e = constraints.keys();
		while (e.hasMoreElements())  {
		     String name = (String)e.nextElement();
			 CompactState cm = compileConstraint(output,name);
			 compiled.put(cm.getName(),cm);
		}
	}
	
	public static CompactState compileConstraint(LTSOutput output, Symbol name, String refname, Vector pvalues)  {
        if (constraints==null) return null;
		AssertDefinition p = (AssertDefinition)constraints.get(name.toString());	
		if (p==null) return null;
		p.cached = null;
		p.fac = new FormulaFactory();
		if (pvalues!=null)  {
			if (pvalues.size()!=p.params.size())
				Diagnostics.fatal ("Actual parameters do not match formals: "+name, name);					
	    		Hashtable actual_params = new Hashtable();
	    		for (int i=0; i<pvalues.size(); ++i) 
					actual_params.put(p.params.elementAt(i),pvalues.elementAt(i));
				p.fac.setFormula(p.ltl_formula.expand(p.fac,new Hashtable(),actual_params));
		} else  {
			p.fac.setFormula(p.ltl_formula.expand(p.fac,new Hashtable(),p.init_params));
		}
		CompositeState cs = compile(constraints, output, name.toString());
		if (cs==null) return null;
		if (!cs.isProperty)  {
		    Diagnostics.fatal ("LTL constraint must be safety: "+p.name, p.name);
		}
		cs.composition.unMakeProperty();
		cs.composition.setName(refname);
		return cs.composition;
	}

			
	
	public static CompactState compileConstraint(LTSOutput output, String constraint)  {
		CompositeState cs = compile(constraints, output, constraint);
		if (cs==null) return null;
		if (!cs.isProperty)  {
			AssertDefinition p = (AssertDefinition)constraints.get(constraint);
		    Diagnostics.fatal ("LTL constraint must be safety: "+p.name, p.name);
		}
		cs.composition.unMakeProperty();
		return cs.composition;
	}
		
	

    private static CompositeState compile(Hashtable definitions, LTSOutput output, String asserted){
        
        if (definitions==null || asserted == null) return null;
    	AssertDefinition p = (AssertDefinition)definitions.get(asserted);
    	if (p==null) return null;
    	if (p.cached!=null) return p.cached;
        output.outln("Formula !"+p.name.toString()+" = "+p.fac.getFormula());
		Vector alpha = p.alphaExtension!=null ? p.alphaExtension.getActions(null) : null;
	    if (alpha==null) alpha = new Vector();
		alpha.add("*");
        GeneralizedBuchiAutomata gba = new GeneralizedBuchiAutomata(p.name.toString(),p.fac, alpha);
        gba.translate();
        //gba.printNodes(output);
        Graph g = gba.Gmake();
		output.outln("GBA " + g.getNodeCount() + " states " + g.getEdgeCount() + " transitions");
        g = SuperSetReduction.reduce(g);
		//output.outln("SSR " + g.getNodeCount() + " states " + g.getEdgeCount() + " transitions");
        Graph g1 = Degeneralize.degeneralize(g);
        //output.outln("DEG " + g1.getNodeCount() + " states " + g1.getEdgeCount() + " transitions");
        g1 = SCCReduction.reduce(g1);
        //output.outln("SCC " + g1.getNodeCount() + " states " + g1.getEdgeCount() + " transitions");
        g1 = Simplify.simplify(g1);
		g1 = SFSReduction.reduce(g1);
        //output.outln("SFS " + g1.getNodeCount() + " states " + g1.getEdgeCount() + " transitions");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Converter c = new Converter(p.name.toString(),g1,gba.getLabelFactory());
        output.outln("Buchi automata:");
        c.printFSP(new PrintStream(baos));
        output.out(baos.toString());
        Vector procs = gba.getLabelFactory().propProcs;
        procs.add(c);
        CompositeState cs = new CompositeState(c.getName(),procs);
        cs.hidden = gba.getLabelFactory().getPrefix();
		cs.setFluentTracer(new FluentTrace(gba.getLabelFactory().getFluents()));
        cs.compose(output,true);
        cs.composition.removeNonDetTau();
		output.outln("After Tau elimination = "+cs.composition.getMaxStates()+" state");
        Minimiser e = new Minimiser(cs.composition,output);
        cs.composition = e.minimise();
		if (cs.composition.isSafetyOnly())  {
			cs.composition.makeSafety();
			cs.determinise(output);
		    //ErrorManager e = new ErrorManager();
		    //cs.composition = e.mergeErrors(cs.composition);

			cs.isProperty = true;
		}
		cs.composition.removeDetCycles("*");
        p.cached = cs;
        return cs;
    }
    
    public Vector getParams() { return params; }
    public Hashtable getInitialParams() { return init_params; }
    public IFormulaSyntax getLTLFormula() { return ltl_formula; }
}



