package synthesis;

import java.util.LinkedList; 
import java.util.Map; 
import java.util.ListIterator; 
import ic.doc.ltsa.common.iface.LTSOutput;


/**
A <code>Instance</code> object is a sequence of events corresponding to the input, ouputs and highlighted states 
of a component in a specific scenario.

@see Event
@see BasicMSC
*/



public class Instance {
	private LinkedList list;
	
	public Instance () {
		list = new LinkedList();
	}
		
	public void appendEvent(Event E) {
		list.add(E);
	}

	public void addEvent(Event E) {
		insertEvent(E, 0);
	}
	
	public ListIterator iterator() {
		return list.listIterator(0);
	}
		
	public int size() {
		return list.size();
	}
	
	public Event get(int i) {
		return (Event) list.get(i);
	}
	
	public void append(Instance I) {
		ListIterator J = I.iterator();
		while (J.hasNext()) {
			appendEvent((Event) J.next());
		}
	}

	public void appendAndMap(Instance I, Map Sigma) {
		ListIterator J = I.iterator();
		while (J.hasNext()) {
			Event e = (Event) J.next();
			Event NewEvent = null;
			if (e instanceof ConditionEvent) 
				NewEvent = new ConditionEvent (e.getLabel());
			else
				if (e instanceof OutputEvent) {
					OutputEvent f = new OutputEvent (e.getLabel());
					f.setTo(new String (((OutputEvent) e).getTo()));
					NewEvent = f;
				}
				else {
					InputEvent f = new InputEvent (e.getLabel());
					f.setFrom(new String (((InputEvent) e).getFrom()));
					NewEvent =  f;
				}
			appendEvent(NewEvent);
			Sigma.put(NewEvent, e);
		}
	}

	public int getFirstMessageEvent()  {
		boolean found = false;
		int i;
		for (i = 0; i<size() && !found; i++)  {
			Event e = get(i);
			found = !(e instanceof ConditionEvent);
		}
		if (found) 
			return i-1;
		else
			return -1;	
	}

	public void removeConditions()  {
		for (int i = 0; i< list.size(); i++)  {
			Event e = (Event) list.get(i);
			if (e instanceof ConditionEvent)  {
				list.remove(i);
				i--;
			}
		}
	}


	public boolean containsConditions()  {
		boolean Contains = false;
		for (int i = 0; i< list.size() && !Contains; i++)  {
			Event e = (Event) list.get(i);
			Contains = (e instanceof ConditionEvent);
		}
		return Contains;
	}

	
	public void deleteCondition(String name)  {
		for (int i = 0; i< list.size(); i++)  {
			Event e = (Event) list.get(i);
			if (e instanceof ConditionEvent)  {
				if (e.getLabel().equals(name))  {
					list.remove(i);
					i--;
				}
			}
		}
	}


	public void addToAlphabet(StringSet L)  {
		for (int i = 0; i< list.size(); i++)  {
			Event e = (Event) list.get(i);
			if (!(e instanceof ConditionEvent))  
				L.add(e.getLabel());
		}
	}
	
	
	public void insertEvent(Event E, int pos) {
		list.add(pos, E);
	}

	public void removeEvent(int pos) {
		list.remove(pos);
	}
	
	public void print(MyOutput Out) {
		ListIterator I = this.iterator();
		Event e;
		while (I.hasNext()) {
			e = (Event) I.next();
			if (e == null) 
				Out.println("////IS NULL");
			else  {
				if (e instanceof ConditionEvent) 
					Out.println("   local " + e.getLabel() + ";");
				else
					if (e instanceof OutputEvent) 
						Out.println("   out " + e.getLabel() + " to " + ((OutputEvent) e).getTo() + ";");
					else
						Out.println("   in " + e.getLabel() + " from " + ((InputEvent) e).getFrom() + ";");
			}	
		}
	}
		
		public void outPrint(MyOutput Out) {
			ListIterator I = this.iterator();
			Event e;
			while (I.hasNext()) {
				e = (Event) I.next();
				if (e == null) 
					Out.println("////IS NULL");
				else  {
					if (e instanceof ConditionEvent){
						System.out.println("Instance.outPrint(): instance of ConditionEvent");
						System.out.println("   local " + e.getLabel() + ";");
					}
					else
						if (e instanceof OutputEvent){ 
							System.out.println("Instance.outPrint(): instance of OutputEvent");
							System.out.println("   out " + e.getLabel() + " to " + ((OutputEvent) e).getTo() + ";");
						}
						else{
							System.out.println("Instance.outPrint(): instance of InputEvent");
							System.out.println("   in " + e.getLabel() + " from " + ((InputEvent) e).getFrom() + ";");

						}	
				}//1st else
			}//while
	}//outPrint


	public void print(LTSOutput Out) {
		ListIterator I = this.iterator();
		Event e;
		while (I.hasNext()) {
			e = (Event) I.next();
			if (e == null) 
				Out.outln("////IS NULL");
			else  {
				if (e instanceof ConditionEvent) 
					Out.outln("   local " + e.getLabel() + ";");
				else
					if (e instanceof OutputEvent) 
						Out.outln("   out " + e.getLabel() + " to " + ((OutputEvent) e).getTo() + ";");
					else
						Out.outln("   in " + e.getLabel() + " from " + ((InputEvent) e).getFrom() + ";");
			}	
		}
	}

	
	public Object clone() {
		Instance Inst = new Instance();
		ListIterator I = this.iterator();
		Event e;
		while (I.hasNext()) {
				e = (Event) I.next();
				if (e instanceof ConditionEvent)  {
					Event ne = new ConditionEvent (e.getLabel());
					ne.Id = e.Id;
					Inst.appendEvent(ne);
					
				}
				else
					if (e instanceof OutputEvent) {
						OutputEvent f = new OutputEvent (e.getLabel());
						f.setTo(new String (((OutputEvent) e).getTo()));
						f.Id = e.Id;
						Inst.appendEvent(f);
					}
					else {
						InputEvent f = new InputEvent (e.getLabel());
						f.setFrom(new String (((InputEvent) e).getFrom()));
						f.Id = e.Id;
						Inst.appendEvent(f);
					}
		}
			
		return Inst;
	}
	
	
	public boolean isPrefixOf(Instance Inst) {
		ListIterator I, J;
		Event e1 = null, e2 = null;
		I = iterator();
		J = Inst.iterator();
		boolean isPrefix = true;
		boolean found;	
			
		while (isPrefix && I.hasNext()) {
			e1 = (Event) I.next();
			if (!(e1 instanceof ConditionEvent)) {
				found = false;
				while (!found && J.hasNext()){
					e2 = (Event) J.next();
					if (!(e2 instanceof ConditionEvent)) {
						found = true;
					}
				}
				if (found)
					isPrefix = (e2.getLabel().equals(e1.getLabel()));
				else
					isPrefix = false;
			}
		}
		return isPrefix;
	}
	
	public boolean isEmpty() {
		ListIterator I;
		Event e1;
		I = iterator();
		boolean found = false;
			
		while (!found && I.hasNext()) {
			e1 = (Event) I.next();
			found = !(e1 instanceof ConditionEvent);
		}
		return !found;
	}	
	
	
	public void deleteLast(int n)  {
		while (size() > 0 && n > 0)  {
			list.remove(size()-1);
			n--;
		}			
	}
	
	public int getIdOfLast()  {
		return ((Event)list.get(size()-1)).Id;
	}
	
	public boolean isTheSameAs(Instance I, LTSOutput o)  {
		boolean equals = true;
		boolean dbg = false;
		
		if (dbg) o.outln("Comparing instance: " + I.size() + ", " + size());
		if (I.size() != size())
			return false;
		if (dbg) o.outln("SameSize");
		ListIterator A = iterator();
		ListIterator B = I.iterator();
		while (A.hasNext() && B.hasNext() && equals)  {
			Event a = (Event) A.next();
			Event b = (Event) B.next();
			if (dbg) o.outln("Checking evets " + a.getLabel() + " and " + b.getLabel());
			equals = (a instanceof ConditionEvent && b instanceof ConditionEvent) ||
					 (a instanceof InputEvent && b instanceof InputEvent) ||
					 (a instanceof OutputEvent && b instanceof OutputEvent);
			
			
			if (equals)  
				equals = (a.getLabel().equals(b.getLabel()));				
		}
	
		return equals;	
	
	}

	public boolean outputs(String lbl)  {
		boolean found = false;
		for (int i = 0; i<size() && !found; i++)  {
			Event e = get(i);
			if (e instanceof OutputEvent) 
				found = e.getLabel().equals(lbl);
		}
		return found;	
	}

	public boolean inputs(String lbl)  {
		boolean found = false;
		for (int i = 0; i<size() && !found; i++)  {
			Event e = get(i);
			if (e instanceof InputEvent) 
				found = e.getLabel().equals(lbl);
		}
		return found;	
	}


	public StringSet getAlphabet()  {
		StringSet S = new StringSet();
		for (int i = 0; i<size(); i++)  {
			Event e = get(i);
			S.add(e.getLabel());
		}
		return S;	
	}
	
}
