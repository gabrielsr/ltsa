package mscedit;

import org.jdom.Element;

public class ProbabilisticTransition extends Transition {

	public ProbabilisticTransition() {
		super();
	}

	public ProbabilisticTransition(Element p_elem) {
		super(p_elem);
	}

	public void apply(Visitor v) {
		v.caseAProbabilisticTransition(this);
	}
	
	
}
