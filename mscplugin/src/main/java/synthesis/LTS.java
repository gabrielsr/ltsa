
package synthesis;
import ic.doc.ltsa.common.iface.LTSOutput;
import java.util.*;


class InvalidNumberOfStates extends Exception {
	public InvalidNumberOfStates () {
		super();
	}
	public InvalidNumberOfStates (String S) {
		super(S);
	}	
}

class StateDoesNotExist extends Exception {
	public StateDoesNotExist () {
		super();
	}
	public StateDoesNotExist (String S) {
		super(S);
	}	
}



public class LTS {
	private HashMap delta;  //integer->set{Vector(String, integer)}
	private Vector Inputs; //integer
	private int max;
	private Integer actual;
	private int totalTransitions;
	
	public LTS() {
		max = 100;
		delta = new HashMap();
		totalTransitions = 0;
	}

	
	public void print(MyOutput Out) {
		Iterator I = delta.keySet().iterator();
		Integer from;
		while (I.hasNext()) {
			from = (Integer) I.next();
			Iterator J = ((Set)delta.get(from)).iterator();
			while (J.hasNext()) {
				Vector v = (Vector)J.next();
				Out.println(from.toString() + "--" + ((String)v.get(0)) + "->" + ((Integer)v.get(1)).toString() );
			}
		}
	}
	
	public void print(LTSOutput Out) {
		Iterator I = delta.keySet().iterator();
		Integer from;
		while (I.hasNext()) {
			from = (Integer) I.next();
			Iterator J = ((Set)delta.get(from)).iterator();
			while (J.hasNext()) {
				Vector v = (Vector)J.next();
				Out.outln(from.toString() + "--" + ((String)v.get(0)) + "->" + ((Integer)v.get(1)).toString() );
			}
		}
	}

	public LTS(int s) {
		Integer Aux;
		max = s;
		delta = new HashMap();
		Inputs = new Vector(s);
		actual = new Integer(0);
		totalTransitions = 0;
		delta.put(actual, new HashSet());
		Inputs.add(0, new Integer(0));
		
		for (int i = 1; i<max; i++) {
			Aux = new Integer(i);
			delta.put(Aux, new HashSet());
			Inputs.add(i, new Integer(0));
		}
	}
		
	public Integer getState(int i) {
			return getKey(i);
	}
	
	private Integer getKey(int i) {
		boolean found = false;
		Integer retVal =null;
		Iterator I = delta.keySet().iterator();
		
		while (I.hasNext() && !found) {
			retVal = (Integer) I.next();
			found = (retVal.intValue() == i);
		}
		if (found)
			return retVal;
		else
			return null;
	}
			
	public void addTransition(int from, String lbl, int to)  {
		Integer i, j;
		int Aux;
		HashSet s;
		Vector v;
		i = getKey(from);
		j = getKey(to);
			v = new Vector(3);
			v.add(0, lbl);
			v.add(1, j);
			v.add(2, i); //just to eliminate possible repeated values shouldn't be used.
			s = (HashSet) delta.get(i);
			s.add(v);
			Aux = ((Integer) Inputs.get(to)).intValue(); 
			Inputs.set(to, new Integer(Aux + 1));
			totalTransitions++;
	}
	
	public int numberOfTransitions() {
		return totalTransitions;
	}
	public void reset() {
			actual = getKey(0);
	}
	
	public boolean step(String lbl) {
		Iterator I = ((Set)delta.get(actual)).iterator();
		boolean found = false;
		Vector v = null;
		
		while (I.hasNext() && !found) {
			v = (Vector) I.next();
			found = (((String) v.get(0)).equals(lbl));
		}
		if (found) {
			actual = (Integer) v.get(1);
			return true;
		}
		else
			return false;
	}
	
	public boolean step(String lbl, Integer i) {
		Iterator I = ((Set)delta.get(actual)).iterator();
		boolean found = false;
		Vector v = null;
		
		while (I.hasNext() && !found) {
			v = (Vector) I.next();
			found = (((String) v.get(0)).equals(lbl)  && ((Integer) v.get(1)) == i);
		}
		if (found) {
			actual = (Integer) v.get(1);
			return true;
		}
		else
			return false;
	}
	
	
	public void step(Vector v) {
			actual = (Integer) v.get(1);
	}
	
	public boolean run (Vector trace) {
		return run_oc(trace, 0);
	}
	
	private boolean run_oc(Vector trace, int pos) {
		Iterator i = enabledTransitions().iterator();
		Integer RememberActual = actual;
		boolean moved;
		
		if (pos == trace.size())
			moved = true;
		else {
			moved = false;
			while (i.hasNext() && !moved) {
				if (RememberActual != actual) {
					setActual(RememberActual);
				}
				moved = step((String) trace.get(pos), (Integer)((Vector)i.next()).get(1));
				if (moved) {
					moved = moved && run_oc(trace, pos+1);
				}
			}
		}
		return moved;
	}
	
	public Set getStates() {
		return delta.keySet();
	}
	
	
	public void setActual(Integer a) {
		actual = a;
	}	
	
	private Integer getActual() {
		return actual;
	}	
	
	public int getActualState() {
		return getActual().intValue();
	}	

	public Set enabledTransitions() {
		return (Set) delta.get(actual);
	}
	public int numberOfInputs(int s) {
		return ((Integer) Inputs.get(s)).intValue();
	}
	public int numberOfOutputs(int s) {
		try {
			return ((Set) delta.get(getKey(s))).size();
		} catch (Exception e) {return 0;}
	}
	

	//Returns path extended until they repeat node.
	public Map pathsWithNoMoreThanNCycles(int MAX, LTSOutput o) {  //Set(Vector(String, int))
		reset();
		return pathsWithNoMoreThanNCycles(actual, MAX, o);
	}

	public Map pathsWithNoMoreThanNCycles(Integer node, int MAX, LTSOutput o) {  
		boolean dbg = true;
		HashMap Paths = new HashMap();
		
		if (dbg) o.outln("Generating paths without cycles");
		Paths.put(new Vector(), "");
		while (extendPathsWithNoMoreThanNCycles(Paths, node, MAX, o)) {
			if (dbg) o.outln("Extended paths");
		}
		
		if (dbg)  {
			//Print Paths
			Iterator I = Paths.keySet().iterator();
			while (I.hasNext())  { 
				Vector Path = (Vector) I.next();
				String line="Path:";
				for (int i = 0; i<Path.size();i++)
					line = line + (String)((Vector)Path.get(i)).get(0) + ", ";
				if (((String)Paths.get(Path)).equals("Loop"))
					line = line + ": LOOP";
				else
					line = line + ": Deadlock";
				o.outln(line);
			}
		}
		
		return Paths;
	}

	
	
	public void pathCovers(HashSet Paths) {  //Set(Vector(String, int))
		HashSet Covered = new HashSet();
		//HashSet Paths = new HashSet();
		Vector initialTrace = new Vector();
		Paths.add(initialTrace);
		while (extendCoverTraces(Covered, Paths));
		//return Paths;
	}

	
	private Set ExtendPrefix(Set Paths, Set Prefixes) {
		Iterator I, J;
		Vector trace, LastStep, v;
		Integer Last;
		Set Covered;
		boolean Extended, TriedEverything;

		//System.out.println("Extending prefixes....");
		I = Paths.iterator();
		while (I.hasNext()) {
			trace = (Vector) I.next();
			if (trace.size() == 0) {
				reset();
				Last = actual;
			}
			else {
				LastStep = (Vector) trace.get(trace.size()-1);
				Last = (Integer) LastStep.get(1);
			}
			
			Covered = new HashSet();
			TriedEverything=false;
			while (!TriedEverything && Prefixes.contains(Last)) {
				J = ((Set)delta.get(Last)).iterator();
				Extended = false;
				while (J.hasNext() && !Extended) {
					v = (Vector) J.next();
					if (!Covered.contains(v)) {
						Covered.add(v);
						Extended = true;
						trace.add(trace.size(), v);			
					}			
				}
				if (!J.hasNext()) {
					TriedEverything = true;
				}
					
				LastStep = (Vector) trace.get(trace.size()-1);
				Last = (Integer) LastStep.get(1);
			}
		}
		return Paths;
	}
	
	
	
	
	private boolean extendCoverTraces(Set Covered, Set Paths) {
		Iterator I, J;
		boolean extended, found = false;
		Vector v = null, v2;
		Vector trace; 
		Integer Last;
		Vector Copy = null, Copy2;
		Vector LastStep;
		extended = false;
		
		I = Paths.iterator();
		while (I.hasNext() && !extended) {
			trace = (Vector) I.next();
			if (trace.size() == 0) {
				reset();
				Last = actual;
			}
			else {
				LastStep = (Vector) trace.get(trace.size()-1);
				Last = (Integer) LastStep.get(1);
			}
			J = ((Set)delta.get(Last)).iterator();
			while (J.hasNext()) {
				v = (Vector) J.next();
				if (!Covered.contains(v)) {
					extended = true;
					Covered.add(v);
					if (Copy == null) {
						Copy = (Vector) trace.clone();
						trace.add(trace.size(), v);		
					}
					else {
						Copy2 = (Vector) Copy.clone();
						Copy2.add(Copy2.size(), v);		
						Paths.add(Copy2);
					}	
				}
			}
		}
		return extended;
	}


	private boolean extendPathsWithNoMoreThanNCycles(Map Paths, Integer initialNode, int MAX, LTSOutput o) {
		//Cantidad de veces que esta permitido que unbMSCS aparezac == MAX+1
		boolean extended = false;
		boolean dbg = false;
		
		Iterator I = Paths.keySet().iterator();
		while (I.hasNext() && !extended) {
			Vector trace = (Vector) I.next();			
			if (dbg) o.outln("Got a trace out of " + Paths.keySet().size());
			if (!((String) Paths.get(trace)).equals("Loop")) {
				if (dbg) o.outln("Not a loop");
				Vector LastStep;
				Integer Last;
				Vector Original = (Vector) trace.clone();
				
				//Set Last to last bMSC in trace
				if (trace.size() == 0) {
					Last = initialNode;
				}
				else {
					LastStep = (Vector) trace.get(trace.size()-1);
					Last = (Integer) LastStep.get(1);
				}
				if (dbg) o.outln("Last node is:" + Last.intValue());
				
				//Iterate over continuations of Last.
				Iterator J = ((Set)delta.get(Last)).iterator();
				while (J.hasNext()) {
					Vector v = (Vector) J.next();				
					if (dbg) o.outln("Got a transition:" +(String) v.get(0) );
								
					if (extended)  {
						if (dbg) o.outln("Copying trace");
						trace = (Vector) Original.clone();
						//Paths.put(trace, "");
					}
					else 
						extended = true;
					Paths.remove(trace);
					trace.add(trace.size(), v);
					if (dbg) o.outln("Added to trace");
					//Check if path is finished (last node is repeated)
					Integer node = (Integer) v.get(1);
					int found;
					if (node.intValue() == initialNode.intValue())  found = 1; else found = 0;
					for (int pos=0; pos < trace.size() && found != MAX+1; pos++)  {
						if (node == ((Vector)trace.get(pos)).get(1))
							found++;
					}
					if (found == MAX+1)  {
						if (dbg) o.outln("Its a loop! Looped at " + node.intValue());
						Paths.put(trace, "Loop");
						if (dbg) o.outln("Trace is clasified as " + (String) Paths.get(trace));
					}
					else 
						Paths.put(trace, "");
				} 
			}
		}
		return extended;
	}



	public boolean ExtendPath(Vector trace, StringSet Accept, LTSOutput o)  {
		Vector LastStep; // Last step of trace
		LastStep = (Vector) trace.get(trace.size()-1);
		return ocExtendPath(trace, Accept, ((Integer) LastStep.get(1)).intValue(), 0, o);
	}


	private boolean ocExtendPath(Vector trace, StringSet Accept, int StartingState, int Count, LTSOutput o)  {
		Iterator J;
		Vector v = null;
		Integer Last; // Last state of trace
		Vector LastStep; // Last step of trace
		boolean found = false;
		Count++;
		if (Count > 10)
			o.outln("Supero!!");
		LastStep = (Vector) trace.get(trace.size()-1);
		Last = (Integer) LastStep.get(1);
		o.outln("Started in " + StartingState + ". Extending from state " + Last);
		//First try to extend in one step to an acceptable state.
		J = ((Set)delta.get(Last)).iterator();
		while (J.hasNext() && !found) {
			v = (Vector) J.next();
			if (Accept.contains((String) v.get(0)))  {
				trace.add(trace.size(), v);		
				found = true;
			}
		}
		if (!found)  {
			o.outln("Starting to backtrack");
			//backtrack
			int Size = trace.size();
			J = ((Set)delta.get(Last)).iterator();
			while (J.hasNext() && !found) {
				v = (Vector) J.next();
				if (!Accept.contains((String) v.get(0)) && ((Integer) v.get(1)).intValue() == Last.intValue())  
					o.outln("Loop that doesn't change anything");
				else  {	
					if (((Integer) v.get(1)).intValue() == StartingState)  
						o.outln("Back to the beginning");
					else {
						trace.add(trace.size(), v);		
						found = ocExtendPath(trace, Accept, StartingState, Count, o);
						if (!found)
							trace.setSize(Size); 
					}
				}	
			}
		}
		return found;
	}


	
	private void printTrace(Vector A)	{
		int j;
		System.out.println("New Trace");
		for (j=0; j<A.size(); j++) {
			System.out.println ("lbl : " + ((String)((Vector) A.get(j)).get(0)) + " State " + ((Integer)((Vector) A.get(j)).get(1)).intValue());
		}
	}	
	
	
	
	public boolean getPath(Vector Path, Instance I, int Pos, HashSet Covered, LTSOutput o) {
		HashSet Copy;
		Vector Aux;
		Iterator ICopy;
		Set TryLater;
		Iterator Transitions;
		Vector t;
		String lbl;
		Event next;
		Integer InitialState;
		boolean found;
		
		
		
		if (Pos < I.size()) {
			next = I.get(Pos);
			if (!(next instanceof ConditionEvent)) {
				found = false;
				Copy = (HashSet) Covered.clone();
				TryLater = new HashSet();
				InitialState = getActual();
				for (int j = 1; j <= 2; j++) {
					if (j == 1) {
						Transitions = enabledTransitions().iterator();
					}
					else
						Transitions = TryLater.iterator();
					while (Transitions.hasNext() && !found) {	
						t = (Vector) Transitions.next();
						lbl = (String) t.get(0);
						if (lbl.equals(next.getLabel())) {
							if (j == 1 && Covered.contains(t))
								TryLater.add(t);
							else {
								step(t);
								Covered.add(t);
								//o.outln("Added (" + (String) t.get(0) + ", " + ((Integer) t.get(1)).toString()+ ") HashCode - " + t.hashCode());
								found = getPath(Path, I, Pos+1, Covered, o);
								if (found) {
									Path.insertElementAt(t, 0);
								}
								else {
									setActual(InitialState);
									Covered.clear();
									Covered.addAll(Copy);
								}
							}
						}
					}  //end while
				} //end for
			} //end if
			else {
				found = getPath(Path, I, Pos+1, Covered, o);
			}
		}
		else
			found = true;
		return found;
	}

	
	
}
	
	
	
	
	
	
