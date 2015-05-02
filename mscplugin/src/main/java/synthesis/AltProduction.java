package synthesis;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

/**
An <code>AltProduction</code> is a grammar production of the form first : P1 | ... | PN
Where P1, ...,Pn are <code>Production</code> objects. <code>alternatives<code> is the set of productions P1, ..., Pn.

Note that because of optimisations the value of Pi.first can be not equal to first. 
Pi.first is considered irrelevant in this object.

@see Production
*/

public class AltProduction {
	public String first;
	public Set alternatives;  //Productions

	public AltProduction(String f) {
		first = f;
		alternatives = new HashSet();
	}
	
	public void addAlternative(Production p) {
		alternatives.add(p);
	}
	
	public Set getAlternatives() {
		return alternatives;
	}
	
	public boolean removeDuplicateAlternatives() {
		Iterator A = alternatives.iterator();
		Iterator B;
		Production P;
		Production Q;
		boolean found, retVal = false;
		
		HashSet newAlt = new HashSet();
		while (A.hasNext()) {
			P = (Production) A.next();
			B = newAlt.iterator();
			found = false;
			while (B.hasNext() && !found) {
				Q = (Production) B.next();
				found = P.sameRightSide(Q);
			}
			if (!found) 
				newAlt.add(P);
			else
				retVal = true;
		}
		alternatives = newAlt;
		return retVal;	
	}
	
	public boolean equivalentTo(AltProduction p) {
		Iterator I = alternatives.iterator();
		Iterator J = p.alternatives.iterator();
		boolean retVal = true;
		while (I.hasNext() && retVal) 
			retVal = p.hasEquivalentAlternative((Production) I.next());
			
		while (J.hasNext() && retVal) 
			retVal = this.hasEquivalentAlternative((Production) J.next());
		
		return retVal;
	}
			
	
	private boolean hasEquivalentAlternative(Production P) {
		Iterator I = alternatives.iterator();
		boolean found = false;
		
		while (I.hasNext() && !found) 
			found = ((Production) I.next()).sameRightSide(P);
			
		return found;
	}
	
	
}

 