package synthesis;


class BasicNegScenario extends NegScenario{
	private BasicMSC precondition;

	
	BasicNegScenario (String _name, BasicMSC _precondition, String _disallowed) {
		super(_name, _disallowed);
		precondition = _precondition;
	}
	
	
	BasicMSC precondition() {
		return precondition;
	}
}
