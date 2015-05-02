package ic.doc.ltsa.lts;
import ic.doc.ltsa.common.iface.LTSOutput;

public class CompactTransition {
	
	int label;
	int fromState;
	int toState;
	double weight;

	//-------------------------------------------------------------
	// constructors
	
	public CompactTransition(int fromState, int label, int toState, double weight) {
		this.label = label;
		this.fromState = fromState;
		this.toState = toState;
		this.weight = weight;
	}

	//--------------------------------------------------------------
	// predicate methods

	public boolean equals(CompactTransition transition) {
		return fromState == transition.fromState
			   && label == transition.label
		       && toState == transition.toState
		       && weight == transition.weight;
	}
	
	//--------------------------------------------------------------
	// temporary methods
	public void print(LTSOutput output) {
		output.outln(fromState+" "+label+" "+toState+" "+weight);
	}

	
}
