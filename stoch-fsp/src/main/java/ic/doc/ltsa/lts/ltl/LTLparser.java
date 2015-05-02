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
import ic.doc.ltsa.lts.*;

public class LTLparser {
	
	private Lex lex;
	private FormulaFactory fac;
	private Symbol current;
	
	public LTLparser(Lex l) {
		lex = l;
		fac = new FormulaFactory();
	}
		
  public FormulaFactory parse() {
  	   current = modify(lex.current());
  	   if (current==null) next_symbol();
  	   fac.setFormula(ltl_unary());
  	   return fac;
  }
  
  private Symbol next_symbol () {
    	return (current = modify(lex.next_symbol()));
  }

  private void push_symbol () {
     lex.push_symbol();
  }

  private void current_is (int kind, String errorMsg) {
     if (current.kind != kind)
        Diagnostics.fatal(errorMsg,current);
  }
  
  // do not want X and U to be keywords outside of LTL expressions
  private Symbol modify(Symbol s) {
  	  if (s.kind!=Symbol.UPPERIDENT) return s;
  	  if (s.toString().equals("X")) {
  	  	   Symbol nx = new Symbol(s);
  	  	   nx.kind = Symbol.NEXTTIME;
  	  	   return nx;
  	  }
  	  if (s.toString().equals("U")) {
  	  		Symbol ut = new Symbol(s);
  	  		ut.kind = Symbol.UNTIL;
  	  		return ut;
  	  	}
  	  	return s;
  	 }

// _______________________________________________________________________________________
// LINEAR TEMPORAL LOGIC EXPRESSION

private IFormula ltl_unary() {   // !,<>,[]
	  Symbol op = current;
	  Formula f;
	  switch (current.kind) {
	   case Symbol.PLING:
	   case Symbol.NEXTTIME:
       case Symbol.EVENTUALLY:
       case Symbol.ALWAYS:
		    next_symbol ();
    		return fac.make(null,op,ltl_unary());
       case Symbol.UPPERIDENT:
    		next_symbol();
			if (!PredicateDefinition.contains(op))
				Diagnostics.fatal("proposition not defined "+op,op);
    	    return  fac.make(op);
    	case Symbol.LROUND:
    	  next_symbol ();
    		IFormula right = ltl_or ();
    		current_is (Symbol.RROUND, ") expected to end LTL expression");
    		next_symbol();
    		return right;
    	default:
    		Diagnostics.fatal ("syntax error in LTL expression",current);
    	}
    	return null;
  }
  

// _______________________________________________________________________________________
// LTL_AND

private IFormula ltl_and () {	// &
	IFormula left = ltl_unary();
	while (current.kind == Symbol.AND) {
     Symbol op = current;
		next_symbol ();
		IFormula right = ltl_unary ();
	  left = fac.make(left,op,right);
	}
	return left;
}

// _______________________________________________________________________________________
// LOGICAL_OR

private IFormula ltl_or () {	// |
	IFormula left = ltl_binary ();
	while (current.kind == Symbol.OR) {
		Symbol op = current;
		next_symbol ();
		IFormula right = ltl_binary ();
	  left = fac.make(left,op,right);
	}
	return left;
}

// _______________________________________________________________________________________
// LTS_BINARY

private IFormula ltl_binary () {	// until, ->
	IFormula left = ltl_and ();
	if (current.kind == Symbol.UNTIL || current.kind == Symbol.ARROW || current.kind == Symbol.EQUIVALENT) {
		Symbol op = current;
		next_symbol ();
		IFormula right = ltl_and ();
		left = fac.make(left,op,right);
	}
	return left;
}

}
