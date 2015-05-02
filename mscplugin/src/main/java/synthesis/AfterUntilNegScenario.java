package synthesis;


class AfterUntilNegScenario extends NegScenario {
	private BasicMSC after;
	private BasicMSC until;
	private StringSet afterAlphabet;
	private StringSet untilAlphabet;	
	
	AfterUntilNegScenario (String _name, BasicMSC _after, StringSet _afterAlphabet, String _disallowed, BasicMSC _until, StringSet _untilAlphabet) {
		super(_name, _disallowed);
		after = _after;
		until = _until;
		afterAlphabet = _afterAlphabet; // null means ALL;
		untilAlphabet = _untilAlphabet;
		if (afterAlphabet != null)
			afterAlphabet.addAll(after.getAlphabet()); 
		if (untilAlphabet != null)
			untilAlphabet.addAll(until.getAlphabet());
	}
	
	BasicMSC after() {
		return after;
	}
	
	BasicMSC until() {
		return until;
	}
	
	StringSet afterAlphabet() {
		return afterAlphabet;
	}

	StringSet untilAlphabet() {
		return untilAlphabet;
	}
}
