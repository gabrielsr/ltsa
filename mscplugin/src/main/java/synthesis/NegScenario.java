package synthesis;

class NegScenario {
	
	String name;
	String disallowed;
	
	NegScenario (String _name, String _disallowed) {
		name = _name;
		disallowed = _disallowed;
	}

	NegScenario () {};
		
	String disallowed() {
		return disallowed;
	}
	
	String name() {
		return name;
	}
}
