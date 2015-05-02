package synthesis;

import java.util.Vector;
import ic.doc.ltsa.common.iface.LTSOutput;

public class Trace {
	private Vector Events;
	private int size;
	
	public Trace() {
		size = 0;
		Events = new Vector();
	}

	public void add(String lbl)  {
		Events.add(size, lbl);
		size++;
	}
	
	public boolean equals(Object o)  {
		if (o instanceof Trace) {
			Trace t = (Trace) o;
			boolean equals = (t.size() == this.size());
			
			for (int i = 0; i<size && equals; i++) {
				equals = t.get(i).equals(this.get(i));
			}
			
			return equals;
		}
		else
			return false;
	}
	
	public String getString()  {
		String S = "";		
		for (int i = 0; i<size; i++) {
			S = S + get(i);
		}
		
		return S;
	}


	public void print(LTSOutput o)  {
		for (int i = 0; i<size; i++) {
			o.out(get(i));
			if (i<size-1)
				o.out(",");
		}
		o.outln(".");
	}


	public String get(int c)  {
		return ((String) Events.get(c));
	}
	
	public int size()  {
		return size;
	}	


	public Trace subtrace(int from, int to)  {
		Trace t = new Trace();
		for (int i=from; i<=to; i++) 
			t.add(get(i));
		return t;	
	}
	
	public Trace subtrace(int from)  {
		return this.subtrace(from, this.size()-1);
	}
	
	public Trace myClone()  { 
		Trace t = new Trace();
		t.Events = (Vector) this.Events.clone();
		t.size = this.size();
		return t;
	}

	public boolean isPrefixOf (Trace t)  {
		//checlks if this is a prefix of t
		if (this.size()>t.size())
			return false;
		else	
			return t.subtrace(0, this.size()-1).equals(this);
	}	
	
	public Trace project(StringSet S)  {
		Trace projection = new Trace();
		for (int i = 0; i<size; i++) {
			if (S.contains(this.get(i)))
				projection.add(this.get(i));
		}
		return projection;
		
	}
}
