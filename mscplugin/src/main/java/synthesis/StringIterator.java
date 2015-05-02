package synthesis;

import java.util.Iterator;

class StringIterator implements Iterator {
	private Iterator I;
	
	public StringIterator(Iterator It) {
		I = It;
	}
	
	public boolean hasNext() {
		return I.hasNext();
	}
	
	public String nextString() {
		return ((String) I.next());
	}
	
	public Object next() {
		return ((String) I.next());
	}
	
	public void remove() {
		I.remove();
	}
}