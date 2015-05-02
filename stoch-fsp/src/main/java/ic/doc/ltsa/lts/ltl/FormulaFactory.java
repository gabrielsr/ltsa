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
import ic.doc.ltsa.common.ast.*;
import ic.doc.ltsa.common.iface.IActionLabels;
import ic.doc.ltsa.common.iface.IFormulaFactory;
import ic.doc.ltsa.common.iface.ISymbol;
import ic.doc.ltsa.common.infra.Diagnostics;
import ic.doc.ltsa.lts.*;

import java.util.*;

/*
* factory for LTL formlae
*/

public class FormulaFactory implements IFormulaFactory {
	
	NotVisitor nv;
	int id;
	Map subf;   //stores subformula to ensure uniqueness
	SortedSet props; //stores the set of propositions
	IFormula formula;	
	private Hashtable actionPredicates;
	private boolean hasNext = false;
	
	
	public FormulaFactory() {
		 nv = new NotVisitor(this);
		 subf = new HashMap();
		 props = new TreeSet();
		 id = 1;
		 actionPredicates = null;
	}
	
	boolean nextInFormula()  { return hasNext;}
	
	public void setFormula(IFormula f) {   // generate the negation for verification
		formula = makeNot(f);
	}
	
	public IFormula getFormula() {
		return formula;
	}
	
	public IFormula make(ISymbol sym) {
		  return unique(new Proposition(sym));
	}
	
	public IFormula make(ISymbol sym, IActionLabels range, Hashtable locals, Hashtable globals)  {
         range.initContext(locals,globals);
		 IFormula f = null;
         while(range.hasMoreNames()) {
         	String s = range.nextName();
			Symbol newSym = new Symbol((Symbol)sym,sym+"."+s);
			if (f==null) 
				f = make(newSym);
			else
			    f = makeOr(f,make(newSym));	
         }
         range.clearContext();
		 return f;
	}
	
	public IFormula make(Stack expr, Hashtable locals, Hashtable globals)  {
		if (Expression.evaluate(expr,locals,globals)>0) 
			return True.make();
		else
			return False.make();
	}	
	
	
	public IFormula make(IActionLabels act, Hashtable locals, Hashtable globals)  {
		if(actionPredicates==null) actionPredicates = new Hashtable();
		Vector av = act.getActions(locals,globals);
		String name = (new Alphabet(av)).toString();
		if (!actionPredicates.containsKey(name))
			actionPredicates.put(name, av);
		return unique(new Proposition(new Symbol(Symbol.UPPERIDENT,name)));
	}
	
	public SortedSet getProps(){
		  return props;
	}
	
	public IFormula make(IFormula left, ISymbol pOp, IFormula right) {
	    Symbol op = (Symbol)pOp;
		switch (op.kind) {
			case Symbol.PLING:       return makeNot(right);
			case Symbol.NEXTTIME:    return makeNext(right);
			case Symbol.EVENTUALLY:  return makeEventually(right);
			case Symbol.ALWAYS:      return makeAlways(right);
			case Symbol.AND:         return makeAnd(left,right);
			case Symbol.OR:          return makeOr(left,right);
			case Symbol.ARROW:       return makeImplies(left, right);
			case Symbol.UNTIL:       return makeUntil(left,right);
			case Symbol.WEAKUNTIL:   return makeWeakUntil(left,right);
			case Symbol.EQUIVALENT:  return makeEquivalent(left,right);
			default:
				Diagnostics.fatal ("Unexpected operator in LTL expression: "+op, op);
		} 
		return null;
	}
	
	public IFormula makeAnd(IFormula left, IFormula right) {
		 if (left == right) return left; // P/\P
		 if (left == False.make() || right == False.make()) return False.make(); //P/\false
		 if (left == True.make()) return right;
		 if (right == True.make()) return left;
		 if (left == makeNot(right)) return False.make(); //contradiction
		 if ( (left instanceof Next) && (right instanceof Next)) // X a && X b --> X(a && b)
		 	return makeNext(makeAnd( ((Next)left).getNext(),((Next)right).getNext()));
		 if (left.compareTo(right)<0)
         return unique(new And(left,right));
      else 
         return unique(new And(right,left));
	}
	
	public IFormula makeOr(IFormula left, IFormula right) {
	 if (left == right) return left; //P\/P
	 if (left == True.make() || right == True.make()) return True.make(); //P\/true
	 if (left == False.make()) return right;
	 if (right == False.make()) return left;
	 if (left == makeNot(right)) return True.make(); //tautology
  	 if (left.compareTo(right)<0)
       return unique(new Or(left,right));
    else 
       return unique(new Or(right,left));
	}
	
	IFormula makeUntil(IFormula left, IFormula right) {
		 if (right==False.make()) return False.make();  // P U false = false
		 if ( (left instanceof Next) && (right instanceof Next)) // X a U X b --> X(a U b)
		 	return makeNext(makeUntil(((Next)left).getNext(),((Next)right).getNext()));
      return unique(new Until(left,right));
	}
	
	IFormula makeWeakUntil(IFormula left, IFormula right) {
		//return makeOr(makeAlways(left),makeUntil(left,right));
		return makeRelease(right,makeOr(left,right));
	}
	
	IFormula makeRelease(IFormula left, IFormula right) {
      return unique(new Release(left,right));
	}
	
	IFormula makeImplies(IFormula left, IFormula right) {
		  return makeOr(makeNot(left),right);
	}
	
	IFormula makeEquivalent(IFormula left, IFormula right) {
		  return makeAnd(makeImplies(left,right),makeImplies(right,left));
	}
	
	IFormula makeEventually(IFormula right) {
		  return makeUntil(True.make(),right);
	}
	
	IFormula makeAlways(IFormula right) {
		  return makeRelease(False.make(),right);
	}
	
	public IFormula makeNot(IFormula right) {
		 return right.accept(nv);
	}
	
	IFormula makeNot(Proposition p) {
		 return unique(new Not(p));
	}
	
	IFormula makeNext(IFormula right) {
		hasNext = true;
		return unique(new Next(right));
	}
	
	public int processUntils(IFormula f, List untils) { 
		f.accept(new UntilVisitor(this,untils));
		return untils.size();
	}
	
	public boolean specialCaseV(IFormula f, Set s) {
      IFormula ff = makeRelease(False.make(), f);
      return s.contains(ff);
  }
    
  public boolean syntaxImplied(IFormula f, Set one, Set two) {
  	  if (f==null) return true;
    if (f instanceof True) return true;
    if (one.contains(f))   return true;
    if (f.isLiteral()) return false;
    IFormula a = f.getSub1();
    IFormula b = f.getSub2();
    IFormula c = ((f instanceof Until) || (f instanceof Release)) ? f : null;
    boolean bf = syntaxImplied(b,one,two);
    boolean af = syntaxImplied(a,one,two);
    boolean cf;
    if(c != null) {
       if(two != null)
            cf = two.contains(c);
        else
            cf = false;
    } else
        cf = true;
    if ((f instanceof Until) || (f instanceof Or))
    	  return bf || af && cf;
    if (f instanceof Release) 
    	  return af && bf || af && cf;
    	if (f instanceof And) 
    	  return af && bf; 
    if (f instanceof Next) {
    	  if(a != null){
         if(two != null)
            return two.contains(a);
          else
            return false;
       } else {
         return true;
       }
    }
    return false;
  }
	
  private int newId(){return ++id;}
  
	private IFormula unique(IFormula f) {
     String s = f.toString();
     if (subf.containsKey(s))
         return (IFormula)subf.get(s);
     else {
     	  f.setId(newId());
         subf.put(s, f);
         if (f instanceof Proposition) props.add(f);
         return f;
     }
  }

    public void setActionPredicates(Hashtable actionPredicates) {

        this.actionPredicates = actionPredicates;
    }

    public Hashtable getActionPredicates() {

        return actionPredicates;
    } 
  
}

/*
* Not visitor pushes negation inside operators to get negative normal form
*/

class NotVisitor implements IVisitor {
    
	private FormulaFactory fac;
	NotVisitor(FormulaFactory f){fac = f;}
	
	public IFormula visit(ITrue t) 
	  {return False.make();}
	public IFormula visit(IFalse f)
	  {return True.make();}
	public IFormula visit(IProposition p)
	  {return fac.makeNot(p);}
	public IFormula visit(INot n)
	  {return n.getNext();}
	public IFormula visit(INext n)
	  {return fac.makeNext(fac.makeNot(n.getNext()));}
	public IFormula visit(IAnd a)
	  {return fac.makeOr(fac.makeNot(a.getLeft()), fac.makeNot(a.getRight()));}
	public IFormula visit(IOr o)
	  {return fac.makeAnd(fac.makeNot(o.getLeft()), fac.makeNot(o.getRight()));}
	public IFormula visit(IUntil u)
	  {return fac.makeRelease(fac.makeNot(u.getLeft()), fac.makeNot(u.getRight()));}
	public IFormula visit(IRelease r)
	  {return fac.makeUntil(fac.makeNot(r.getLeft()), fac.makeNot(r.getRight()));}
}

/*
* Untils visitor computes the untils indexes
*/

class UntilVisitor implements IVisitor {
	private FormulaFactory fac;
	private List ll;
	UntilVisitor(FormulaFactory f, List l){fac = f; ll = l;}
	
	public IFormula visit(ITrue t) 
	  {return t;}
	public IFormula visit(IFalse f)
	  {return f;}
	public IFormula visit(IProposition p)
	  {return p;}
	public IFormula visit(INot n) {
		n.getNext().accept(this); 
	  return n;
	}
	public IFormula visit(INext n) {
		n.getNext().accept(this); 
	  return n;
	}
	public IFormula visit(IAnd a) {
	  a.getLeft().accept(this); 
	  a.getRight().accept(this); 
	  return a;
	}
	public IFormula visit(IOr o) {
	  o.getLeft().accept(this); 
	  o.getRight().accept(this); 
	  return o;
	}
	public IFormula visit(IUntil u) {
		if (!u.visited()) {
			u.setVisited();
			ll.add(u);
			u.setUI(ll.size()-1);
			u.getRight().setRofUI(ll.size()-1);
		    u.getLeft().accept(this); 
		    u.getRight().accept(this); 
		}
	  return u;
	}
	public IFormula visit(IRelease r) {
	  r.getLeft().accept(this); 
	  r.getRight().accept(this); 
	  return r;
	}
}
