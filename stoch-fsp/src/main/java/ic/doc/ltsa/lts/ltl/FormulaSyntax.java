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
import ic.doc.ltsa.common.ast.IFormula;
import ic.doc.ltsa.common.iface.IActionLabels;
import ic.doc.ltsa.common.iface.IAssertDefinition;
import ic.doc.ltsa.common.iface.IFormulaFactory;
import ic.doc.ltsa.common.iface.IFormulaSyntax;
import ic.doc.ltsa.common.iface.ISymbol;
import ic.doc.ltsa.common.infra.Diagnostics;
import ic.doc.ltsa.lts.*;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Stack;

/*
* abstract syntax tree for unexpanded (i.e. includes forall ) LTL formlae.
*/

public class FormulaSyntax implements IFormulaSyntax {
	IFormulaSyntax left,right;
	ISymbol operator;
	ISymbol proposition;
	IActionLabels range;
	IActionLabels action;
	Vector parameters;  //overloaded with Stack for "when expr"
	
	private FormulaSyntax(IFormulaSyntax lt, ISymbol op, IFormulaSyntax rt, ISymbol prop, IActionLabels r, IActionLabels a, Vector v)  {
		left = lt;
		right = rt;
		operator = op;
		proposition = prop;
		range = r;
		action = a;
		parameters = v;
	}
	
	public static IFormulaSyntax make(IFormulaSyntax lt, ISymbol op, IFormulaSyntax rt)  {
		return new FormulaSyntax(lt,op,rt,null,null,null,null);
	}
	
	public static IFormulaSyntax make(ISymbol prop)  {
		return new FormulaSyntax(null,null,null,prop,null,null,null);
	}
	
	public static IFormulaSyntax make(ISymbol prop, IActionLabels r)  {
		return new FormulaSyntax(null,null,null,prop,r,null,null);
	}
	
	public static IFormulaSyntax make(ISymbol prop, Vector v)  {
		return new FormulaSyntax(null,null,null,prop,null,null,v);
	}
	
	public static IFormulaSyntax makeE(ISymbol op, Stack v)  {
		return new FormulaSyntax(null,op,null,null,null,null,v);
	}
	
	public static IFormulaSyntax make(IActionLabels a)  {
		return new FormulaSyntax(null,null,null,null,null,a,null);
	}
	
	public static IFormulaSyntax make(ISymbol op, IActionLabels r, IFormulaSyntax rt)  {
		return new FormulaSyntax(null,op,rt,null,r,null,null);
	}
	
	public IFormula expand(IFormulaFactory fac, Hashtable locals, Hashtable globals)  {
		if (proposition!=null)  {
			if (range == null)  {
				if (PredicateDefinition.definitions!=null && PredicateDefinition.definitions.containsKey(proposition.toString())) 
				  return fac.make(proposition);
				else  {
					IAssertDefinition p = (IAssertDefinition)AssertDefinition.definitions.get(proposition.toString());
					if (p==null) 
						Diagnostics.fatal ("LTL fluent or assertion not defined: "+proposition, proposition);
					if (parameters==null)
						return p.getLTLFormula().expand(fac,locals,p.getInitialParams() );
					else  {
						if (parameters.size()!=p.getParams().size())
							Diagnostics.fatal ("Actual parameters do not match formals: "+proposition, proposition);
						
						Hashtable actual_params = new Hashtable();
						Vector values = paramValues(parameters,locals,globals);
						for (int i=0; i<parameters.size(); ++i) 
						    actual_params.put(p.getParams().elementAt(i),values.elementAt(i));
						return p.getLTLFormula().expand(fac,locals,actual_params);	
					}	
				} 
			} else  {
				return fac.make(proposition,range,locals,globals);
			}
		} else if (action!=null)  {
			return fac.make(action,locals,globals);
		} else if (operator.getKind() == Symbol.RIGID)  {
			return fac.make((Stack)parameters, locals, globals);
		} else if (operator!=null && range==null)  {
			if (left==null)  {
				return fac.make(null,operator,right.expand(fac,locals,globals));
			} else  {
				return fac.make(left.expand(fac,locals,globals),operator,right.expand(fac,locals,globals));
			}
		} else if (range!=null && right!=null)  {
			range.initContext(locals,globals);
		 	IFormula f = null;
         	while(range.hasMoreNames()) {
				range.nextName();
				if (f==null) 
				  f = right.expand(fac,locals,globals);
			   else  {
			   	  if (operator.getKind() == Symbol.AND)
			        f = fac.makeAnd(f,right.expand(fac,locals,globals));
				  else
				  	f = fac.makeOr(f,right.expand(fac,locals,globals));	
			   }	
         	}
         	range.clearContext();
		 	return f;
		}
		return null;
	}
	
	private Vector paramValues(Vector paramExprs, Hashtable locals, Hashtable globals) {
        if (paramExprs==null) return null;
        Enumeration e = paramExprs.elements();
        Vector v = new Vector();
        while(e.hasMoreElements()) {
            Stack stk = (Stack)e.nextElement();
            v.addElement(Expression.getValue(stk,locals,globals));
        }
        return v;
    }

	
}

	
	
