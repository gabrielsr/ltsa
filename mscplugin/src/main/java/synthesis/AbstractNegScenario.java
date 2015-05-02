package synthesis;


class AbstractNegScenario extends NegScenario {
	private BasicMSC precondition;
	private StringSet Alphabet;
	
	AbstractNegScenario (String _name, BasicMSC _precondition, StringSet _Alphabet, String _disallowed) {
		super(_name, _disallowed);
		precondition = _precondition;
		Alphabet = _Alphabet;  //null means ALL;
		if (Alphabet != null)
			Alphabet.addAll(precondition.getAlphabet());
	}
	
	
	BasicMSC precondition() {
		return precondition;
	}
	
	StringSet Alphabet() {
		return Alphabet;
	}
}

