package synthesis;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.Iterator;

class StringMap implements Map{
	private HashMap H;
	
	public StringMap() {
		H = new HashMap();
	}
	
	public void clear() {
		H.clear();
	}
	
	public Object clone() {
		Iterator I;
		StringMap C = new StringMap();
		String k;
		
		I = H.keySet().iterator();
		while (I.hasNext()) {
			k = (String) I.next();
			C.put(k, get(k));
		}		
		return C;
	}
	
	public boolean containsValue(Object value) {
		return H.containsValue(value);
	}
	
	public Object put(Object key, Object value)  {
		return H.put(key, value);
	}
  
	public Set entrySet() {
		return H.entrySet();
	}
	
	public boolean equals(Object o) {
		return H.equals(o);
	}
		
	public int hashCode() {
		return H.hashCode();
	}
		
	public Object remove(Object Key) {
		boolean found;
		Iterator I;
		String lbl;
		String key = (String) Key;
		
		if (H.containsKey(key))
			return H.remove(key);
		else {
			I = H.keySet().iterator();

			while (I.hasNext()) {
				lbl = (String) I.next();
				if (lbl.equals(key));
					return H.remove(lbl);
			}
		}
		return null;
	}
	
	public boolean containsKey(Object Key) { 
		boolean found;
		Iterator I;
		String key = (String) Key;
		
		if (H.containsKey(key))
			return true;
		else {
			I = H.keySet().iterator();
			found = false;
			while (I.hasNext() && !found) {
				found = I.next().equals(key);
			}
			return found;
		}
	}
	         
  public Object get(Object Key) {
  	boolean found;
  	String s;
		Iterator I;
		String key = (String) Key;

		if (H.containsKey(key))
			return H.get(key);
		else {
			I = H.keySet().iterator();
			found = false;
			while (I.hasNext() && !found) {
				s = (String) I.next();
				if (s.equals(key))
					return H.get(s);
			}
			return null;
		}  	
  }

  public boolean isEmpty() {
  	return H.isEmpty();
  }
   
  public Set keySet() {
  	return H.keySet();
  }
  
  public StringSet keyStringSet() {
  	return new StringSet(H.keySet());
  }

  public int size() {
  	return H.size();
  }
   
  public void putAll(Map t) {
  	H.putAll(t);
  }
  
  public Collection values() {
  	return H.values();
  }
  
}