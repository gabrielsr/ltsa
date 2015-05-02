package synthesis;

/**
Events can be Input, Output or Conditions and all have labels.
*/

abstract class Event {
	private String label;
	public int Id;
	static int LastId = 0;
	
	public Event (String l) {
		setLabel(l);
		Id = LastId++;
	}
	
	public void setLabel (String l) {
			label = l;
	}
	
	public String getLabel () {
			return label;
	}
}

class ConditionEvent extends Event {
	
	public ConditionEvent(String l) {
		super(l);
	}
}

class MessageEvent extends Event {
	private String To_From;
	private String Weight="";
	
	public MessageEvent(String l) {
		super(l);
	}
	
	public String getToFrom() {
		return To_From;
	}

	public void setToFrom(String s) {
		To_From = s;
	}

	/*
	 * Adding weights for the purpose of weighted messages -- Genaina
	 */
	public String getWeight() {
		return Weight;
	}
	
	public void setWeight(String _Weight){
		Weight = _Weight;
	}
	
}

class OutputEvent extends MessageEvent {
	
	private String Weight="";

	public OutputEvent(String l) {
		super(l);
	}
	
	public String getTo() {
		return super.getToFrom();
	}

	public void setTo(String s) {
		super.setToFrom(s);
	}

}

class InputEvent extends MessageEvent {
	
	public InputEvent(String l) {
		super(l);
	}

	public String getFrom() {
		return super.getToFrom();
	}

	public void setFrom(String s) {
		super.setToFrom(s);
	}
	
}

