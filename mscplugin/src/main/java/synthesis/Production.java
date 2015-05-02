package synthesis;


import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Comparator;
import java.util.Set;
import java.util.Iterator;



/**
A <code>Production</code> is a grammar production of the form first : e1->...en->last.
Where e1, ...,en are <code>event</code> objects. <code>sequence<code> is the list of events e1, ..., en.
The sequence can be empty.

@see AltProduction
*/
public class Production implements Comparator{
	private ArrayList sequence;

	public Production() {
		sequence = new ArrayList();
	}
	
	public void add(String s) {
			sequence.add(s);
	}

	public Object clone () {
		Production P = new Production();
		P.sequence = (ArrayList) sequence.clone();
		
		return P;
	}
	
	public int size() {
		return sequence.size();
	}
	
	public void set(int i, String s) {
		if (i<size()) 
					sequence.set(i, s);
	}
	
	public String get(int i) {
		if (i<size()) 
			return (String) sequence.get(i);
		else 
			return null;
	}
	
	public String first() {
		if (size() == 0)
			return null;
		else
			return (String) sequence.get(0);
	}
	
	public String last() {
		if (size() == 0)
			return null;
		else
			return (String) sequence.get(sequence.size()-1);
	}

	public boolean equals(Production P) {
		ListIterator I,J;
		boolean retVal = true;
		
		if(retVal)
			retVal = retVal && first().equals(P.first());
		if (retVal)
			retVal = retVal && sameRightSide(P);
		return retVal;
	}
	
	public boolean sameRightSide(Production P) {
		ListIterator I,J;
		boolean retVal = true;
		
		
		if (retVal)
			retVal = retVal && (P.size() == size());
	
		if (retVal)
				retVal = retVal && (P.size()>0);
		
		I = sequence.listIterator(1);
		J = P.sequence.listIterator(1);
		while (I.hasNext() && retVal) {
			retVal = retVal && I.next().equals(J.next());
		}		
		return retVal;
	}


public int compare(Object O1, Object O2) throws ClassCastException {
		int Aux;
		
		if (!(O1 instanceof Production) || !(O2 instanceof Production))
			throw new ClassCastException();
			
		Production P1, P2;
		P1 = (Production) O1;
		P2 = (Production) O2;
		
		ListIterator I,J;
		int retVal = 0;

		if (retVal == 0) {
			if (P1.sequence.size() < P2.sequence.size())
				Aux = P1.sequence.size();
			else
				Aux = P2.sequence.size();
				
			I = P1.sequence.listIterator();
			J = P2.sequence.listIterator();
			for (int a = 0; a < Aux && retVal == 0;a++) {
				retVal = ((String) I.next()).compareTo((String)J.next());
			}
		}	

		if (retVal == 0) 
			retVal = P1.sequence.size() - P2.sequence.size();
	
		return retVal;
	}	

	static void printProductions(String ComponentName, Set Q, MyOutput out) {//Q is a Set(Production)
		Iterator I = Q.iterator(); //Iterator(Set(Production))
		Production p; 
		ListIterator E; 
		
		out.println(ComponentName);		
		while (I.hasNext()) {
			p = (Production) I.next();
			for (int i = 0; i<p.size(); i++) {
				out.print(p.get(i));
				out.print(" -> ");
			}
			out.println("");
		}
	}
}
	

