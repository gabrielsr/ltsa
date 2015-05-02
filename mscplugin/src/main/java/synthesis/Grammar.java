package synthesis;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

/**
A <code>Grammar</code> object is grammar determined by a set of productions <code>AltProductions<code> and with 
the initial non-terminal Init. Basically it knows how to perform some cleaning up operations that 
preserve the generated language. All clean-up methods return true if the grammar was actually modified.


@see AltProductions
*/
class Grammar {
	private Set AP; //set(AltProductions)
	private String InitialSymbol;
	
	
	public Grammar(Set S, String Init) {
		AP = S;
		InitialSymbol=Init;
	}
	
	public Set getAltProductions() {
		return AP;
	}
	
	/**
	Removes productions that correspond to unreachable non-terminals in the grammar. 
	Returns true if the grammar was actually modified.
	*/
	public boolean removeUnreachableNonTerminals() {
		boolean retVal = false;
		Set Reachable = new HashSet();
		AltProduction ap;
		Iterator P = AP.iterator();
		Iterator A;
		String Searched;
		boolean found;
		
		while (P.hasNext())  {
			A = ((AltProduction) P.next()).alternatives.iterator();
			while (A.hasNext()) 
				Reachable.add(((Production)A.next()).last());
		}
		P = AP.iterator();
		while (P.hasNext())  {
			ap = (AltProduction) P.next();
			A = Reachable.iterator();
			found = false;
			while (A.hasNext() && !found) 
				found = ap.first.equals(A.next());
			if (!found && !ap.first.equals(InitialSymbol))  {
				P.remove(); //removes ap from AP
				retVal = true;
			}
		}
		return retVal;
	}	

	
	/**
	Modifies the grammar in order to remove productions of the form A: B, that is productions with only one alternative in 
	which the only element is a non-terminal.

	Returns true if the grammar was actually modified.
	*/

	public boolean removeTrivialProductions() {
		HashMap TrivialMap = new HashMap(); //trivialnonterminal -> nonterminal to be used to replace it.
		Iterator P = AP.iterator();
		AltProduction ap;
		Production p;
		String s;
		boolean retVal = false;
		
		while (P.hasNext())  {
			ap = (AltProduction) P.next();
			//System.out.println("Analysing " + ap.first);
			if (ap.alternatives.size() == 1)  {
				p = (Production) ap.alternatives.iterator().next();
				//System.out.println("It has only one alternative: " + p.last);
				if (p.size()<=2)  {
				//	System.out.println("And its sequence is empty!");
					retVal = true;
					if (!ap.first.equals(p.last())) {
					//	System.out.println("Replacing " + ap.first +" with " + p.last);
						if (ap.first.equals(InitialSymbol)) {
							renameNonTerminal(ap.first, p.last());
						//	System.out.println("Replacing " + p.last + " with Init");
							renameNonTerminal(p.last(), InitialSymbol);		
						}
						else
							renameNonTerminal(ap.first, p.last());
					}
					P.remove(); //Remove ap from AP
					//System.out.println("Removed");
				}
			}
		}
		return retVal;
	}
	
/**
	Removes alternatives of the form A: A, that is alternatives in
	which the only element is the same non-terminal. It assumes that there are no trivial productions.<p>
	For example given A: a->b->C | A | a->A | C, the production is reduced to A: a->b->C | a->A | C.

	Returns true if the grammar was actually modified.
	
	@see removeTrivialProductions
	*/

	public boolean removeRecursiveAlternatives() {	//eliminate recursive alternatives
		Iterator P = AP.iterator();
		Iterator A;
		Production p;
		AltProduction ap;
		boolean retVal = false;
			
		while (P.hasNext())  {
			ap = (AltProduction) P.next();
			A = ap.alternatives.iterator();
			while (A.hasNext())  {
				p = (Production) A.next();
				if (p.size() <= 2)  {
					if (ap.first.equals(p.last()))  {
						retVal = true;
						A.remove(); //remove p from ap.alternatives
					}
				}
			}
		}
		return retVal;
	}
		

	/**
	Removes alternatives of the form A: B, that is alternatives in
	which the only element is the same non-terminal. It assumes that there are no trivial productions.<p>
	For example given A: a->b->C | A | a->A | C, the production is reduced to A: a->b->C | a->A | C.

	Returns true if the grammar was actually modified.
	
	@see removeTrivialProductions
	
	*/		
	public boolean replaceTrivialAlternatives() { //replace alternatives A->B with all alternatives of B
		Iterator P = AP.iterator();
		Iterator A;
		Iterator P2, A2;
		Production p;
		AltProduction ap, ap2;
		String s;
		boolean retVal = false;
		boolean cont;
		
		while (P.hasNext())  {
			ap = (AltProduction) P.next();
			A = ap.alternatives.iterator();
			cont = A.hasNext();
			while (cont)  {
				p = (Production) A.next();
				if (p.size() <= 2)  {
					cont = false;
					s = p.last();
					A.remove(); //removes p from ap.alternatives.
					retVal = true;
					P2 = AP.iterator();
					while (P2.hasNext()) 	 {
						ap2 = (AltProduction) P2.next();
						if (ap2.first.equals(p.last()))  {
							A2 = ap2.alternatives.iterator();
							while (A2.hasNext()) 							
								ap.alternatives.add(A2.next());
							A = ap.alternatives.iterator();
						}
					}
				}
				else
					cont = A.hasNext();
			}
		}
		return retVal;
	}	
	
	/**
	Removes duplicate alternatives. For example given A: a->b->C | a->A | a->A | C, 
	the production is reduced to A: a->b->C | a->A | C.

	Returns true if the grammar was actually modified.
	
	*/		
	public boolean removeDuplicateAlternatives() { 
		Iterator P = AP.iterator();
		AltProduction ap;
		boolean retVal, aux;		
		retVal = false;
		while (P.hasNext())  {
			ap = (AltProduction) P.next();
			aux = ap.removeDuplicateAlternatives();
			retVal = retVal || aux;
		}
		return retVal;
	}
	
	/**
	Modifies grammar to remove equivalent productions. For example if the grammar contains
	A: a->b->C | a->A | C and C: a->b->C | C | a->A, one can be eliminated.

	Returns true if the grammar was actually modified.
	
	*/		
	
	public boolean removeEquivalentProductions() { 
		Iterator P = AP.iterator();
		Iterator Q;
		Set Aux = new HashSet();
		AltProduction ap, ap2;
		boolean retVal, aux;		
		retVal = false;
		while (P.hasNext())  {
			ap = (AltProduction) P.next();
			Q = AP.iterator();
			while (Q.hasNext()) {
				ap2 = (AltProduction) Q.next();
				if (ap != ap2 && !Aux.contains(ap) && !ap2.first.equals(InitialSymbol)) {
					if (ap.equivalentTo(ap2)) {
						renameNonTerminal(ap2.first, ap.first);	
						Aux.add(ap2);
					}
				}
			}
		}
		Q = Aux.iterator();
		while (Q.hasNext()) {
			AP.remove(Q.next());
			retVal = true;
		}
		return retVal;
	}
	
	private void renameNonTerminal(String a, String b) {
		Iterator P = AP.iterator();
		Iterator A;
		Production p;
		AltProduction ap;
			
		while (P.hasNext())  {
			ap = (AltProduction) P.next();
			A = ap.alternatives.iterator();
			if (ap.first.equals(a))
				ap.first=b;
			while (A.hasNext())  {
				p = (Production)A.next();
				if (p.last().equals(a))
					p.set(p.size()-1, b);
			}
		}
	}
	
	
}