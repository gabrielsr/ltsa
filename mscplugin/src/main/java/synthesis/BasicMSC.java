package synthesis;

import ic.doc.ltsa.common.iface.LTSOutput;
import java.util.*;

/**
A <code>BasicMSC</code> object represents a bMSC, that is a set of component instances
with their message interaction. Although inconsistent bMSCs can be constructed, operations on
bMSCs might be assuming consistency: Each message label is used as an output by exactly one
component instance and as an input by exactly one (different) component instance.
In addition message sending and receiving is consistent. i.e. If the bMSC were drawn,
message arrows would not cross.

@exception ComponentInstanceNotFound The required instance of given component name is not in the bMSC.

@see Instance
*/
class ComponentInstanceNotFound extends Exception {
	public ComponentInstanceNotFound() {
		super();
	}
	public ComponentInstanceNotFound(String S) {
		 super(S);
	}
}

class InconsistentLabelUse extends Exception {
	public InconsistentLabelUse() {
		super();
	}
	public InconsistentLabelUse(String S) {
		super(S);
	}
}

class loopBackMessage extends Exception {
	public loopBackMessage() {
		super();
	}
	public loopBackMessage(String S) {
		super(S);
	}
}

class InconsistentEvents extends Exception {
	public InconsistentEvents() {
		super();
	}
	public InconsistentEvents(String S) {
		super(S);
	}
}





public class BasicMSC {

	static int counter = 0;
	static int limit = 0;
	static int counterlimit = 0;
	static boolean last = false;
	private HashMap instances = new HashMap();
	public String name;
	private Map SavedDependencies=null;
	private Map SavedLastDependencies=null;
	private Map SavedCanFinishBefore=null;

	public void addInstance (String componentName, Instance I) {
		instances.put (componentName, I);
	}

	public Set components () {
		return instances.keySet();
	}

	public Instance componentInstance(String name) throws ComponentInstanceNotFound {
		if (containsComponent(name))
			return ((Instance) instances.get(name));
		else
			throw new ComponentInstanceNotFound();
	}

	public String getSource(String lbl)  {
		Iterator I = components().iterator();
		boolean found = false;
		String Comp = null;

		while (I.hasNext() && !found)  {
			Instance Inst = null;
			Comp = (String) I.next();
			try  {Inst = componentInstance(Comp);}  catch (Exception e) {throw new Error("Internal Consistency Error");}
			found =  Inst.outputs(lbl);
		}
		if (!found)
			return null;
		else
			return Comp;
	}

	public String getTarget(String lbl)  {
		Iterator I = components().iterator();
		boolean found = false;
		String Comp = null;

		while (I.hasNext() && !found)  {
			Instance Inst = null;
			Comp = (String) I.next();
			try  {Inst = componentInstance(Comp);}  catch (Exception e) {throw new Error("Internal Consistency Error");}
			found =  Inst.inputs(lbl);
		}
		if (!found)
			return null;
		else
			return Comp;
	}



	public boolean containsConditions() throws Exception {
		Iterator I = components().iterator();
		boolean Contains = false;
		Instance Inst = null;

		while (I.hasNext() && !Contains)  {
			String lbl = (String) I.next();
			try  {Inst = componentInstance(lbl);}  catch (Exception e) {throw new Exception("Internal Consistency Error");}
			Contains = Inst.containsConditions();
		}
		return Contains;
	}


	public StringSet getAlphabet()  {
		Iterator I = components().iterator();
		Instance Inst = null;
		StringSet A = new StringSet();

		while (I.hasNext())  {
			String lbl = (String) I.next();
			try  {Inst = componentInstance(lbl);}  catch (Exception e) {throw new Error("Internal Consistency Error");}
			A.addAll(Inst.getAlphabet());
		}
		return A;

	}

	public boolean empty() throws Exception {
		Iterator I = components().iterator();
		Instance Inst = null;
		boolean empty = true;

		while (I.hasNext() && empty)  {
			String lbl = (String) I.next();
			try  {Inst = componentInstance(lbl);}  catch (Exception e) {throw new Exception("Internal Consistency Error");}
			empty = (Inst.size()==0);
		}
		return empty;
	}


	public StringSet getParticipatingComponents()  {
		Iterator I = components().iterator();
		Instance Inst = null;
		StringSet S = new StringSet();

		while (I.hasNext())  {
			String lbl = (String) I.next();
			try  {Inst = componentInstance(lbl);}  catch (Exception e) {throw new Error("Internal Consistency Error");}
			if (!Inst.isEmpty())
				S.add(lbl);
		}
		return S;
	}


	public void removeConditions() throws Exception {
		Iterator I = components().iterator();
		Instance Inst = null;

		while (I.hasNext())  {
			String lbl = (String) I.next();
			try  {Inst = componentInstance(lbl);}  catch (Exception e) {throw new Exception("Internal Consistency Error");}
			Inst.removeConditions();
		}
	}


	public void append(BasicMSC B) {
		Iterator I = B.components().iterator();
		Instance BInst;
		Instance Inst;
		String lbl;

		name = name + "_" + B.name;

		try {
			while (I.hasNext()) {
				lbl = (String) I.next();
				BInst = B.componentInstance(lbl);
				Inst = componentInstance(lbl);
				Inst.append(BInst);
			}
		} catch (Exception e) {}
	}

	public void appendAndMap(BasicMSC B, Map Sigma) {
		Iterator I = B.components().iterator();
		Instance BInst;
		Instance Inst;
		String lbl;

		name = name + "_" + B.name;
		System.out.println("BasicMSC.appendAndMap called!");
		try {
			while (I.hasNext()) {
				lbl = (String) I.next();
				BInst = B.componentInstance(lbl);
				Inst = componentInstance(lbl);
				Inst.appendAndMap(BInst, Sigma);
			}
		} catch (Exception e) {}
	}


	public void appendMessage(String From, String lbl, String To)  {
		Instance IFrom = getInstance(From);
		Instance ITo = getInstance(To);

		System.out.println("BasicMSC.appendMessage: From "+From+" To: "+To);
		OutputEvent eFrom = new OutputEvent(lbl);
		eFrom.setTo(To);
		IFrom.appendEvent(eFrom);

		InputEvent eTo = new InputEvent(lbl);
		eTo.setFrom(From);
		ITo.appendEvent(eTo);
	}

	public void addMessage(String From, String lbl, String To, int Id1, int Id2)  {
		Instance IFrom = getInstance(From);
		Instance ITo = getInstance(To);

		System.out.println("BasicMSC.addMessage: From "+From+" To: "+To);
		OutputEvent eFrom = new OutputEvent(lbl);
		eFrom.setTo(To);
		eFrom.Id = Id1;
		IFrom.addEvent(eFrom);


		InputEvent eTo = new InputEvent(lbl);
		eTo.setFrom(From);
		eTo.Id = Id2;
		ITo.addEvent(eTo);
	}


	public void addMessage(String From, String lbl, String To)  {
		Instance IFrom = getInstance(From);
		Instance ITo = getInstance(To);

		System.out.println("BasicMSC.addMessage: From "+From+" To: "+To);
		OutputEvent eFrom = new OutputEvent(lbl);
		eFrom.setTo(To);
		IFrom.addEvent(eFrom);

		InputEvent eTo = new InputEvent(lbl);
		eTo.setFrom(From);
		ITo.addEvent(eTo);
	}
	
	/*When adding this message, we must also add the probabilistic annotation
	 * to the messages.
	 
	 
	public void addProbabilisticMessage(String From, String lbl, String To, String Weight)  {
		Instance IFrom = getInstance(From);
		Instance ITo = getInstance(To);
		
		OutputEvent eFrom = new OutputEvent(lbl);
		eFrom.setTo(To);
		//eFrom.setWeight(Weight);
		IFrom.addEvent(eFrom);

		InputEvent eTo = new InputEvent(lbl);
		eTo.setFrom(From);
		eTo.setWeight(Weight);
		ITo.addEvent(eTo);
	}
	*/


 	public void addToAlphabet(StringSet L)  {
	 	Iterator J;
	 	String component;

	 	J = instances.keySet().iterator();
	 	while (J.hasNext()) {
	 		component = (String) J.next();
			Instance I= null;
	 		try  {I = (Instance) instances.get(component);}
	 		catch (Exception e)  {}
	 		I.addToAlphabet(L);
	 	}
 	}


	/**
	Checks if bMSC B has all the components that the actual BasicMSC has;

	@exception ComponentInstanceNotFound contains the name of the component that does not have an instance in B
	*/

	public void hasAllComponentsIn(BasicMSC B) throws ComponentInstanceNotFound {
		Iterator I = components().iterator();
		String Aux;

		while (I.hasNext()) {
			Aux = (String) I.next();
			if (!B.containsComponent(Aux))
				throw new ComponentInstanceNotFound(Aux);
		}
	}

	public boolean containsComponent(String name) {
		return (instances.containsKey(name));
	}

	public void copyComponents(BasicMSC B) {
		Iterator I = B.components().iterator();
		while (I.hasNext()) {
			String lbl = (String) I.next();
			addInstance(lbl, new Instance());
		}
	}


	public Object clone() {
		BasicMSC B = new BasicMSC();
		B.name = name;
		String lbl;
		Iterator I = components().iterator();
		//System.out.println("Cloning bMSC");
		while (I.hasNext()) {
			lbl = (String) I.next();
			try {
				//System.out.println("About to clone Instance");
				B.addInstance(lbl, (Instance) componentInstance(lbl).clone());
			} catch (Exception e) {		System.out.println("Error");}
		}
		return B;
	}

	public Object cloneAndMap(Map Sigma) {
		BasicMSC B = new BasicMSC();
		B.name = name;
		String lbl;
		Iterator I = components().iterator();
		//System.out.println("Cloning bMSC");
		while (I.hasNext()) {
			lbl = (String) I.next();
			try {
				//System.out.println("About to clone Instance");
				Instance OldInstance = componentInstance(lbl);
				Instance NewInstance = (Instance) OldInstance.clone();
				B.addInstance(lbl, NewInstance);
				for (int i = 0; i < OldInstance.size(); i++)
					Sigma.put(NewInstance.get(i), OldInstance.get(i));
			} catch (Exception e) {		System.out.println("Error");}
		}
		return B;
	}

	/** @exception InconsistentLabelUse
	loopBackMessage
	*/
	public void consistentLabels(HashMap M) throws  Exception {
		//M = label->(bMSC name, to:componentName,from:componentName)
		Iterator I = components().iterator();
		ListIterator E;
		Iterator K;
		Event e;
		Instance i;
		String iname, key=null;
		boolean found;
		Vector truple;

		while (I.hasNext()) {														//For all instances
				iname = (String) I.next();
				try{i = componentInstance(iname);} catch (ComponentInstanceNotFound ex) {throw new Exception("Internal Consistency Error:" + ex.getMessage());}
				E = i.iterator();

				while (E.hasNext()) {												//For events
					e = (Event) E.next();
					if (!(e instanceof ConditionEvent)) {			//that are message input or outputs
						K = M.keySet().iterator();
						found = false;
						while (K.hasNext() && !found) { 				//search for the event in M
							key = (String) K.next();
							found = e.getLabel().equals(key);
						}
						if (!found) {														//if not found, add to M
							truple = new Vector(3);
							truple.add(0,this.name);
							if (e instanceof InputEvent) {
								truple.add(1,iname);
								truple.add(2,((InputEvent)e).getFrom());
							}
							else {
								truple.add(1, ((OutputEvent)e).getTo());
								truple.add(2, iname);
							}
							M.put(e.getLabel(), truple);
							if (truple.get(1).equals(truple.get(2)))
									throw new loopBackMessage("Label " + e.getLabel() + " used as input and output in instance of component " + truple.get(2) + " in bMSC " + this.name);

						//	System.out.println("Adding: " + e.getLabel() + " is defined in bMSC " + ((String) truple.get(0)) + " to be message to " + ((String) truple.get(1)) + " from  " + ((String) truple.get(2)));
						}
						else {																	//if found check consistency
							truple = (Vector) M.get(key);
							if (e instanceof InputEvent) {
							//	if (truple.get(1) == null) {
							//		truple.set(1,iname);
							//		if (!((InputEvent)e).From.equals(iname))
							//			throw new InconsistentLabelUse("Label " + key + " is declared to be received from component " + ((InputEvent)e).From + " but is being received from component " + iname + " in bMSC " + this.name);
							//		System.out.println("Completing: " + e.getLabel() + " to be message to " + ((String) truple.get(1)));
							//		if (truple.get(1).equals(truple.get(2)))
							//			if (this.name.equals(truple.get(0)))
							//				throw new loopBackMessage("Label " + key + " used as input and output in instance of component " + truple.get(1) + " in bMSC " + this.name);
							//			else
							//				throw new loopBackMessage("Label " + key + " used as input and output in instances of component " + truple.get(1) + " in bMSCs " + this.name + " and " + truple.get(0));
							//	}
							//	else {
									if (!truple.get(1).equals(iname)) {
										if (this.name.equals(truple.get(0)))
											throw new InconsistentLabelUse("Label " + key + " used inconsistently in bMSC " + this.name);
										else
											throw new InconsistentLabelUse("Label " + key + " used inconsistently in bMSCs " + this.name + " and " + truple.get(0));
									}
									if (!((InputEvent)e).getFrom().equals(truple.get(2)))
										throw new InconsistentLabelUse("Label " + key + " is declared to be received from component " + ((InputEvent)e).getFrom() + " but is being received from component " + truple.get(2) + " in bMSC " + this.name);
							//	}
							}
							else {
							//	if (truple.get(2) == null) {
							//		truple.set(2,iname);
							//		if (!((OutputEvent)e).To.equals(iname))
							//			throw new InconsistentLabelUse("Label " + key + " is declared to be sent to component " + ((OutputEvent)e).To + " but is being sent to component " + iname + " in bMSC " + this.name);
								//	System.out.println("Completing: " + e.getLabel() + " to be message from " + ((String) truple.get(2)));
							//		if (truple.get(1).equals(truple.get(2)))
							//			if (this.name.equals(truple.get(0)))
							//				throw new loopBackMessage("Label " + key + " used as input and output in instance of component " + truple.get(1) + " in bMSC " + this.name);
							//			else
							//				throw new loopBackMessage("Label " + key + " used as input and output in instances of component " + truple.get(1) + " in bMSCs " + this.name + " and " + truple.get(0));
							//	}
							//	else {
									if (!truple.get(2).equals(iname)) {
										if (this.name.equals(truple.get(0)))
											throw new InconsistentLabelUse("Label " + key + " used inconsistently in bMSC " + this.name);
										else
											throw new InconsistentLabelUse("Label " + key + " used inconsistently in bMSCs " + this.name + " and " + truple.get(0));
									}
									if (!((OutputEvent)e).getTo().equals(truple.get(1)))
										throw new InconsistentLabelUse("Label " + key + " is declared to be sent to component " + ((OutputEvent)e).getTo() + " but is being sent to component " + truple.get(1) + " in bMSC " + this.name);
							//	}
							}
						}
					}
				}
		}
	}

	//InconsistentEvents
	public void consistentEvents() throws Exception {
	//	HashMap tgt = new HashMap(); //output event->input event
	//	HashMap invtgt = new HashMap();//input event->output event

		int size = components().size();
		Vector Instances = new Vector(size); //vector(ListIterator)
		Vector enabledEvents = new Vector(size); //vector(events)
		int index, index2 = 0;
		Iterator I;
		Instance Aux;
		ListIterator i;
		int InstancesFinished = 0;
		boolean foundMatch;
		Event e1 = null;
		Event e2 = null;
		String iname;


	//	System.out.println("bMSC " + name);

		//Create Listiterators for instances
		I = components().iterator();
		index = 0;
		while (I.hasNext()) {
				iname = (String) I.next();
				try{Aux = componentInstance(iname);} catch (ComponentInstanceNotFound ex) {throw new Exception("Internal Consistency Error:" + ex.getMessage());}
				Instances.add(index++, Aux.iterator());
		}

		//Create enabledEvents
		for (index = 0; index<size; index++) {
			i = (ListIterator) Instances.get(index);
			e1 = null;
			while (i.hasNext() && e1 == null) {
				e1 = (Event) i.next();
				if (e1 instanceof ConditionEvent)
					e1 = null;
			}
			enabledEvents.add(index, e1);
			if (e1 == null)
				InstancesFinished++;
		}

	//	printEnabled(enabledEvents, size);

		while (InstancesFinished < size) {							//While there are instances to process
			foundMatch = false;
			for (index = 0; index<size && !foundMatch;index++) {				//search for a pair of matching events
				e1 = (Event) enabledEvents.get(index);
				if (e1 != null) {
					for (index2 = index + 1; index2<size && !foundMatch;index2++) {
						e2 = (Event) enabledEvents.get(index2);
						if (e2 != null) {
							foundMatch = (e1.getLabel().equals(e2.getLabel()));
					//		if (foundMatch) System.out.println("Found Match for " + e2.getLabel());
						}
					}
				}
			}

			if (foundMatch) {
		/*	if (e1 instanceof OutputEvent) {
					tgt.put(e1, e2);
					invtgt.put(e2, e1);
				}
				else {
					tgt.put(e2, e1);
					invtgt.put(e1, e2);
				}*/


				e1 = null;
				i = (ListIterator) Instances.get(index-1);
				while (i.hasNext() && e1 == null) {
					e1 = (Event) i.next();
					if (e1 instanceof ConditionEvent)
						e1 = null;
				}
				enabledEvents.set(index-1, e1);
				if (e1 == null)
					InstancesFinished++;

				e1 = null;
				i = (ListIterator) Instances.get(index2-1);
				while (i.hasNext() && e1 == null) {
					e1 = (Event) i.next();
					if (e1 instanceof ConditionEvent)
						e1 = null;
				}
				enabledEvents.set(index2-1, e1);
				if (e1 == null)
					InstancesFinished++;

	//			printEnabled(enabledEvents, size);
			}
			else {
				String Message ="";
				for (index = 0; index <size; index++) {
					if (((Event) enabledEvents.get(index)) != null)
						Message = Message + ((Event) enabledEvents.get(index)).getLabel();
					else
						Message = Message + "-";

					if (index + 1 < size)
							Message = Message + ", ";
				}
				throw new InconsistentEvents("Inconsistent events in bMSC " + name + ". Impossible to pair one of the following event labels " + Message );
			}
		}
	}



	/*private void printEnabled(Vector V, int s) {
		for (int i = 0; i<s; i++) {
			if (((Event) V.get(i)) != null)
				System.out.print(((Event) V.get(i)).getLabel() + ", ");
			else
				System.out.print("End, ");
		}
		System.out.println("");
	}
	*/

	public void print (MyOutput Out) {
		Iterator J;
		String component;

		Out.println("msc " + name + ";");
		J = instances.keySet().iterator();
		while (J.hasNext()) {
			component = (String) J.next();
			Out.println("inst "+ component + ";");
			try  {((Instance) instances.get(component)).print(Out);}
			catch (Exception e)  {Out.println("Error in instance.print");}
			Out.println("endinst");
		}
		Out.println("endmsc ");
	}


	public void print (LTSOutput Out) {
		Iterator J;
		String component;

		Out.outln("msc " + name + ";");
		J = instances.keySet().iterator();
		while (J.hasNext()) {
			component = (String) J.next();
			Out.outln("inst "+ component + ";");
			try  {((Instance) instances.get(component)).print(Out);}
			catch (Exception e)  {Out.outln("Error in instance.print");}
			Out.outln("endinst");
		}
		Out.outln("endmsc ");
	}


	public boolean isPrefixOf(Set bMSCs) {  //A is a prefix of B iif there is an instance of A that is prefix of an instance in B.
		Iterator J, K;
		String component;
		Instance I,I2;
		BasicMSC B;
		boolean retVal = false;
		boolean isPrefix;

		//System.out.println("Analysing if " + name + " is prefix of ...");
		K = bMSCs.iterator();
		while (K.hasNext() && !retVal) {
			B = (BasicMSC) K.next();
			if (B != this) {
				//System.out.println("... of " + B.name);
				J = instances.keySet().iterator();
				isPrefix = false;
				while (J.hasNext() && !isPrefix) {
					component = (String) J.next();
					//System.out.println("Checking instance " + component);
					I = (Instance) instances.get(component);
					I2 = B.getInstance(component);
					isPrefix = I.isPrefixOf(I2);
					if (isPrefix)
						System.out.println(component + ":" + name + " is a prefix of " + component + ":" + B.name);
				}
				if (isPrefix) {
					//System.out.println(name + " is a prefix of " + B.name);
					retVal = true;
				}
			}
		}
		return retVal;
	}


	public StringMap BuildPrefixRelation(Set bMSCs) {  //Map (CompName-> ("P", "E", "N"))
		Iterator J, K;
		String component;
		Instance I,I2;
		BasicMSC B;
		boolean isPrefix;
		StringMap H = new StringMap();

		//System.out.println("Component " + name);
		J = instances.keySet().iterator();
		while (J.hasNext()) {
			component = (String) J.next();
			I = (Instance) instances.get(component);
			if (I.isEmpty()) {
				H.put(component, "E");
				//System.out.println("Instance " + component + ": E");
			}
			else {
				K = bMSCs.iterator();
				isPrefix = false;
				while (K.hasNext() && !isPrefix) {
					B = (BasicMSC) K.next();
					if (B != this) {
						I2 = B.getInstance(component);
						if (I.isPrefixOf(I2)) {
							isPrefix = true;
							H.put(component, "P");
							//System.out.println("Instance " + component + ": P");
						}
					}
				}
				if (!isPrefix)
					H.put(component, "N");
					//System.out.println("Instance " + component + ": N");
			}
		}
		return H;
	}


	public Instance getInstance(String Component) {
		Iterator I = instances.keySet().iterator();
		String lbl;
		//System.out.println("z");
		while (I.hasNext()) {
			lbl = (String) I.next();
			//System.out.println(Component + " seeks for " + lbl);
			if (lbl.equals(Component))
				return (Instance) instances.get(lbl);
		}
		return null;
	}

	public void printLatex (MyOutput Out) {
		Iterator J;
		String component;

		Out.println("\\begin{msc}{" + name.replace('_', '.') + "}");
		J = instances.keySet().iterator();
		while (J.hasNext()) {
			component = (String) J.next();
			Out.println("\\declinst{"+ component.replace('_', '.') + "}{}{" + component.replace('_', '.') + "}");
		}
		printLatexInstances(Out);

		Out.println("\\end{msc}");
		Out.println("");Out.println("");
		Out.println("");Out.println("");
	}


	public void printLatexInstances(MyOutput Out) {

		int size = components().size();
		Vector Instances = new Vector(size); //vector(ListIterator)
		Vector InstancesNames = new Vector(size);
		HashSet Level;
		Vector enabledEvents = new Vector(size); //vector(events)
		int index, index2 = 0;
		Iterator I;
		Instance Aux = new Instance();
		ListIterator i;
		int InstancesFinished = 0;
		boolean foundMatch;
		Event e1 = null;
		Event e2 = null;
		String iname;


		//Create Listiterators for instances
		I = components().iterator();
		index = 0;
		while (I.hasNext()) {
				iname = (String) I.next();
				try{Aux = componentInstance(iname);} catch (ComponentInstanceNotFound ex) {}
				InstancesNames.add(index, iname);
				Instances.add(index++, Aux.iterator());
		}

		//Create enabledEvents
		for (index = 0; index<size; index++) {
			i = (ListIterator) Instances.get(index);
			e1 = null;
			while (i.hasNext() && e1 == null) {
				e1 = (Event) i.next();
				if (e1 instanceof ConditionEvent)
					e1 = null;
			}
			enabledEvents.add(index, e1);
			if (e1 == null)
				InstancesFinished++;
		}

		Level = new HashSet();
		while (InstancesFinished < size) {							//While there are instances to process
			foundMatch = false;
			for (index = 0; index<size && !foundMatch;index++) {				//search for a pair of matching events
				e1 = (Event) enabledEvents.get(index);
				if (e1 != null) {
					for (index2 = index + 1; index2<size && !foundMatch;index2++) {
						e2 = (Event) enabledEvents.get(index2);
						if (e2 != null) {
							foundMatch = (e1.getLabel().equals(e2.getLabel()));
							if (foundMatch)   {
								String from, to;
								if (e2 instanceof OutputEvent)  {
									to = ((OutputEvent) e2).getTo();
									from = ((InputEvent)e1).getFrom();
								}
								else  {
									to = ((OutputEvent)e1).getTo();
									from = ((InputEvent)e2).getFrom();
								}
/*								if (Level.contains(InstancesNames.get(index)) ||
											Level.contains(InstancesNames.get(index2)))  {
									Out.println("\\nextlevel");
									Level.clear();
								}
								Level.add(InstancesNames.get(index));
								Level.add(InstancesNames.get(index2));*/
								Out.println("\\mess{" + e2.getLabel().replace('_', '.')+"}{"+from.replace('_', '.')+"}{"+to.replace('_', '.')+"}");
								Out.println("\\nextlevel");
							}
						}
					}
				}
			}

			e1 = null;
			i = (ListIterator) Instances.get(index-1);
			while (i.hasNext() && e1 == null) {
				e1 = (Event) i.next();
				if (e1 instanceof ConditionEvent)  {
/*					if (Level.contains(InstancesNames.get(index - 1)))  {
						Out.println("\\nextlevel");
						Level.clear();
					}
					Level.add(InstancesNames.get(index-1));*/
					Out.println("\\condition{"+e1.getLabel().replace('_', '.')+"}{"+((String) InstancesNames.get(index-1)).replace('_', '.')+"}");
					Out.println("\\nextlevel[2]");
					e1 = null;
				}
			}
			enabledEvents.set(index-1, e1);
			if (e1 == null)
				InstancesFinished++;

			e1 = null;
			i = (ListIterator) Instances.get(index2-1);
			while (i.hasNext() && e1 == null) {
				e1 = (Event) i.next();
				if (e1 instanceof ConditionEvent)  {
					/*if (Level.contains(InstancesNames.get(index2 - 1)))  {
						Out.println("\\nextlevel");
						Level.clear();
					}
					Level.add(InstancesNames.get(index2-1));*/
					Out.println("\\condition{"+e1.getLabel().replace('_', '.')+"}{"+((String)InstancesNames.get(index2-1)).replace('_', '.')+"}");
					Out.println("\\nextlevel[2]");
					e1 = null;
				}
			}
			enabledEvents.set(index2-1, e1);
			if (e1 == null)
				InstancesFinished++;
		}
	}


	//Assumes that the bMSC is consistent.
	public BasicMSC FindCutAndSplit(LTSOutput o, boolean l) throws Exception {
		last = l;
		int size = components().size();
		Vector Instances = new Vector(size); //vector(ListIterator)
		Vector enabledEvents = new Vector(size); //vector(events)
		Vector Names = new Vector(size);//vector(string)
		Vector Pos = new Vector(size);
		int index = 0;

		int InstancesFinished = 0;

//		o.outln("Cutting bMSC " + name);
		BasicMSC B=null;


		//Fill vectors: Names, Pos, Instances
		Iterator I = components().iterator();
		index = 0;
		while (I.hasNext()) {
				Instance Aux;
				String iname  = (String) I.next();
				try{Aux = componentInstance(iname);} catch (ComponentInstanceNotFound ex) {throw new Exception("Internal Consistency Error:" + ex.getMessage());}
				Names.add(index, iname);
				Pos.add(index, new Integer(0));
				Instances.add(index++, Aux);
		}


		//Create enabledEvents and update InstancesFinished
		for (index = 0; index<size; index++) {
			Instance i = (Instance) Instances.get(index);
			int p = ((Integer)Pos.get(index)).intValue();
			Event e;

			if (p < i.size())  {
				e = (Event) i.get(p);
				Pos.set(index, new Integer(p+1));
			}
			else
				e = null;

			enabledEvents.add(index, e);
			if (e == null)
				InstancesFinished++;
		}

		if (FindNextCut(Instances, enabledEvents, Names, Pos, o))  {
			B = new BasicMSC();
			B.name = this.name + "_P";
			Split(B, Instances, Names, Pos, enabledEvents, o);
		}
		return B;
	}

	private boolean FindNextCut(Vector Instances, Vector enabledEvents, Vector Names, Vector Pos, LTSOutput o )  throws Exception {
		int index = 0;
		int size = components().size();
		boolean Finished = false;
		boolean retVal = false;
		boolean FirstTime = true;
		boolean dbg = false;

		while (!Finished)  {
			if (dbg) o.outln("About to move forward");
			if (MoveForwardUntilBlock(Instances, enabledEvents, Pos, o) || !FirstTime)  {
				FirstTime = false;
				if (dbg) o.outln("Cut, end state, locked");
				for (index = 0; index<size && enabledEvents.get(index)!=null;index++);
				if (index<size)  {
					if (dbg) o.outln("Some instance is at its end.");
					Finished = true;
					retVal = false;
				}
				else  {
					if (dbg) o.outln("Cut, locked");
					boolean foundCut = true;
					Event e=null;
					for (index = 0; index<size && foundCut;index++) {				//search for a pair of matching events
						e = (Event) enabledEvents.get(index);
						if (e != null)
							foundCut = (e instanceof ConditionEvent);
						else
							foundCut = false;
					}
					if (!foundCut) {
						if (dbg) o.outln("locked");

						if (e == null)  {
							if (dbg) o.outln(Names.get(index) + " is at the end. No cut is possible");
							Finished = true;
							retVal = false;
						}
						else  {
							index--;
							boolean readyToUnblock = false;
							Event e2 = null;
							int index2=0;
							while (!readyToUnblock)  {
								if (e2 != null)  {
									index = index2;
									e = e2;
								}
								if (dbg) o.outln(Names.get(index) + " has event " + e.getLabel() + " enabled");

								//Search for other instance
								String name;
								if (e instanceof OutputEvent)
									name =((OutputEvent) e).getTo();
								else
									name = ((InputEvent) e).getFrom();
								index2 = 0;
								while (!Names.get(index2).equals(name) && index2<size)
									index2++;
								if (index2==size)
									throw new InconsistentEvents("Inconsistency!");
								if (dbg) o.outln("Event needs instance " + Names.get(index2) + " unblocked");

								e2 = (Event) enabledEvents.get(index2);
								if (e2 != null)  {
									readyToUnblock = (e2 instanceof ConditionEvent);
								}
								else
									throw new InconsistentEvents("Inconsistency1!");
							}
							if (dbg) o.outln("Unblocking instance " + index2);
							if (!MoveForward(Instances, Pos, enabledEvents, index2, o))
								throw new InconsistentEvents("Inconsistency4!");
						}
					}// Finished "locked" case
					else  {
						if (dbg) o.outln("Cut!");
						Finished = true;
						retVal = true;
					}// Finished "cut" case.
				}// Finished "cut", "locked" cases.
			} //Finished "Cut, end state, locked"
			else  {
				if (dbg) o.outln("Initial State is a cut");
				FirstTime = false;
				boolean foundOneCut = false;
				Vector pairs = GetAllPairsThatCanBeUnblocked(Instances, Pos, enabledEvents, o);
				for (index = 0; index * 2 < pairs.size(); index++)  {
					Vector c_enabledEvents = (Vector) enabledEvents.clone();
					Vector c_Pos = (Vector) Pos.clone();
					if (!MoveForward(Instances, c_Pos, c_enabledEvents, ((Integer)pairs.get(2*index)).intValue(), o)
					 || !MoveForward(Instances, c_Pos, c_enabledEvents, ((Integer)pairs.get(2*index + 1)).intValue(), o))
						throw new InconsistentEvents("Inconsistency4!");


					if (FindNextCut(Instances, c_enabledEvents, Names, c_Pos, o))  {
						foundOneCut = true;
						MoveForward(Instances, Pos, enabledEvents, ((Integer)pairs.get(2*index)).intValue(), o);
						MoveForward(Instances, Pos, enabledEvents, ((Integer)pairs.get(2*index + 1)).intValue(), o);
					}
				}
				if (!foundOneCut)  {
					Finished = true;
					retVal = false;
				}
			}
		} //While(finished)
		return retVal;
	}


	//Assumes all instances are
	private Vector GetAllPairsThatCanBeUnblocked(Vector Instances, Vector Pos, Vector enabledEvents, LTSOutput o)  {
		Vector pairs = new Vector();
		int p;
		Event e1, e2;
		Instance i;
		int pos=0;

		for (int index = 0; index<components().size(); index++) {
			p = ((Integer)Pos.get(index)).intValue();
		 	i = (Instance) Instances.get(index);
			if (p<i.size()) {
				e1 = (Event) i.get(p);
				if (! (e1 instanceof ConditionEvent))  {
					for (int index2 = index+1; index2<components().size(); index2++) {
						p = ((Integer)Pos.get(index2)).intValue();
						i = (Instance) Instances.get(index2);
						if (p<i.size()) {
							e2 = (Event) i.get(p);
							if (!(e2 instanceof ConditionEvent))  {
								if (e1.getLabel().equals(e2.getLabel()))  {
									pairs.add(pos++, new Integer(index));
									pairs.add(pos++, new Integer(index2));
									//o.outln("Pair to unblock: " + index + "," + index2 + ". Label:" + e1.getLabel());
								}
							}
						}
					}
				}
			}
		}
		return pairs;
	}






	public boolean MoveForwardUntilBlock(Vector Instances, Vector enabledEvents, Vector Pos, LTSOutput o) throws Exception {
		Vector TripleResult;
		int index, index2;
		boolean foundMatch = true;
		boolean moved = false;


		//o.outln("Moving forward...");
		int Counter = 0;
		while (foundMatch) {
			TripleResult = FindMatch(components().size(), enabledEvents, o);
			index = ((Integer)TripleResult.get(0)).intValue();
			index2 = ((Integer)TripleResult.get(1)).intValue();
			foundMatch = (((Integer)TripleResult.get(2)).intValue() == 1);

			if (foundMatch) {
				moved = true;
				MoveForward(Instances, Pos, enabledEvents, index, o);
				MoveForward(Instances, Pos, enabledEvents, index2, o);
			}
		}
		//o.outln("Finished moving forward");
		return moved;
	}


	//returns (int index, int index2, int FoundMatch)
	private Vector FindMatch(int size, Vector enabledEvents, LTSOutput o)  {
		int index=0, index2=0;
		boolean dbg = false;
		//o.outln("FindMatch.");

		boolean foundMatch = false;
		for (index = 0; index<size && !foundMatch;index++) {				//search for a pair of matching events
			Event e1 = (Event) enabledEvents.get(index);
			if (e1 != null) {
				if (!(e1 instanceof ConditionEvent))  {
					for (index2 = index + 1; index2<size && !foundMatch;index2++) {
						Event e2 = (Event) enabledEvents.get(index2);
						if (e2 != null) {
							if (!(e2 instanceof ConditionEvent))  {
								foundMatch = (e1.getLabel().equals(e2.getLabel()));
								if (dbg) if (foundMatch) o.outln("Found match: " + (index) +" and " + (index2) + " on label " + e1.getLabel());
							}
						}
					}
				}
			}
		}
		Vector retVal = new Vector();
		retVal.add(0, new Integer(index-1));
		retVal.add(1, new Integer(index2-1));
		if (foundMatch)  {
			retVal.add(2, new Integer(1));
		}
		else
			retVal.add(2, new Integer(0));
		return retVal;
	}


	private boolean MoveForward(Vector Instances, Vector Pos, Vector enabledEvents, int index, LTSOutput o) {
		boolean dbg = false;
		if (dbg) o.outln("Moving Forward. Index = " + index + ". Instaces.size = " + Instances.size());
		Event e1 = null;
		Instance i = (Instance) Instances.get(index);
		int p = ((Integer)Pos.get(index)).intValue();

		if (p < i.size())  {
			e1 = (Event) i.get(p);
			Pos.set(index, new Integer(p+1));
		}
		else
			e1 = null;

		enabledEvents.set(index, e1);
		return (e1 != null);
	}



	private void Split(BasicMSC B, Vector Instances, Vector Names, Vector Pos, Vector enabledEvents, LTSOutput o)  throws Exception {
		boolean NonEmptySplit = false;
		for (int inst = 0; inst <components().size(); inst++)  {
			//o.outln("Looking at instance " + inst + " - " + (String) Names.get(inst));
			Instance NI = new Instance();
			B.addInstance((String) Names.get(inst), NI);
			Instance i = (Instance) Instances.get(inst);
			int p = ((Integer)Pos.get(inst)).intValue();

			if (enabledEvents.get(inst) != null)  {
				NI.appendEvent((Event) enabledEvents.get(inst));
			//	o.outln("deleted enabled event");
			}
			//o.outln("About to start deleteing from pos " + p);
			int tobedeleted = 0;
			while (p<i.size())  {
				tobedeleted ++;
				NonEmptySplit = true;
				NI.appendEvent(i.get(p));
				p++;
			}
			Instance Aux2;
			try{Aux2 = componentInstance((String) Names.get(inst));} catch (ComponentInstanceNotFound ex) {throw new Exception("Internal Consistency Error:" + ex.getMessage());}
			Aux2.deleteLast(tobedeleted);
		} //end for
	}



 public boolean TrivialCut()  throws Exception {
 	boolean trivial = true;
 	Iterator I = components().iterator();
	while (I.hasNext() && trivial)  {
		Instance Aux;
		String iname  = (String) I.next();
		try{Aux = componentInstance(iname);} catch (ComponentInstanceNotFound ex) {throw new Exception("Internal Consistency Error:" + ex.getMessage());}
		trivial = false;
		if (Aux.size() == 1)
			if (Aux.get(0) instanceof ConditionEvent)
				trivial = true;
	}
	return trivial;
 }



	public boolean isTheSameAs(BasicMSC B, LTSOutput o) throws Exception {
		Instance I1 = null, I2 = null;
		boolean equal = true, dbg=false;
		Iterator I = components().iterator();
		while (I.hasNext() && equal) {
				String n = (String) I.next();
				if (dbg) o.outln("Checking component " + n);
				try{I1 = componentInstance(n); I2 = B.componentInstance(n); } catch (ComponentInstanceNotFound ex) {throw new Exception("Internal Consistency Error:" + ex.getMessage());}
				if (dbg) if (I1 == null) o.outln(name + " does not have an Instance for that componment!");
				if (dbg) if (I2 == null) o.outln(B.name + "does not have an Instance for that componment!");
				equal = I1.isTheSameAs(I2,o);
		}
		return equal;
	}


	public void AddScenarioEvents() 	{
 		Iterator I = components().iterator();

		while (I.hasNext()) {
			String compname = (String) I.next();
			Instance Inst = (Instance) instances.get(compname);
			//int pos = Inst.getFirstMessageEvent();
			//if (pos >= 0)  {
				FSPLabel l = new FSPLabel();
				try {l.setComponentLabel(compname);} catch (Exception e) {}
				String oEvent = "s_" + l.getLabel() + "_" + name;
				OutputEvent c = new OutputEvent(oEvent);
				//System.out.println("BasicMSC.AddScenarioEvents: "+oEvent);
				c.setTo("Environment");
				Inst.insertEvent(c, 0);
			//}
		}
	}

	public void RemoveScenarioEvents(Set S) 	{
		Iterator I = components().iterator();
		while (I.hasNext()) {
			Instance Inst = (Instance) instances.get((String) I.next());
			for (int i = 0; i< Inst.size(); i++)  {
				Event e = Inst.get(i);
				if (e instanceof OutputEvent) {
					//o.outln("Output event " + e.getLabel() + " to " + ((OutputEvent)e).getTo());
					if (((OutputEvent)e).getTo().equals("Environment"))  {
						//o.outln("Environment checked! First Letter is " + e.getLabel().substring(0,2));
						if (e.getLabel().substring(0,2).equals("s_"))  {
							//o.outln("Removed");
							S.add(e.getLabel());
							Inst.removeEvent(i);
							i--;
						}
					}
				}
			}
		}
	}


	public void getScenarioEvents(Set S) 	{
		Iterator I = components().iterator();
		while (I.hasNext()) {
			Instance Inst = (Instance) instances.get((String) I.next());
			for (int i = 0; i< Inst.size(); i++)  {
				Event e = Inst.get(i);
				if (e instanceof OutputEvent) {
					//o.outln("Output event " + e.getLabel() + " to " + ((OutputEvent)e).getTo());
					if (((OutputEvent)e).getTo().equals("Environment"))  {
						//o.outln("Environment checked! First Letter is " + e.getLabel().substring(0,2));
						if (e.getLabel().substring(0,2).equals("s_"))  {
							//o.outln("Removed");
							String sLabel = e.getLabel();
							//System.out.println("BasicMSC.getScenarioEvents: "+sLabel);
							S.add(sLabel);
						}
					}
				}
			}
		}
	}



	public void RemoveLabelsNotIn(StringSet s) 	{
		Iterator I = components().iterator();
		while (I.hasNext()) {
			Instance Inst = (Instance) instances.get((String) I.next());
			for (int i = 0; i< Inst.size(); i++)  {
				Event e = Inst.get(i);
				if (e instanceof ConditionEvent) {
					if (!s.contains(e.getLabel()))  {
						Inst.removeEvent(i);
						i--;
					}
				}
			}
		}
	}


	public String showSequence() {
		String Out = "";
		int size = components().size();
		Vector Instances = new Vector(size); //vector(ListIterator)
		Vector InstancesNames = new Vector(size);
		HashSet Level;
		Vector enabledEvents = new Vector(size); //vector(events)
		int index, index2 = 0;
		Iterator I;
		Instance Aux = new Instance();
		ListIterator i;
		int InstancesFinished = 0;
		boolean foundMatch;
		Event e1 = null;
		Event e2 = null;
		String iname;


		//Create Listiterators for instances
		I = components().iterator();
		index = 0;
		while (I.hasNext()) {
				iname = (String) I.next();
				try{Aux = componentInstance(iname);} catch (ComponentInstanceNotFound ex) {}
				InstancesNames.add(index, iname);
				Instances.add(index++, Aux.iterator());
		}

		//Create enabledEvents
		for (index = 0; index<size; index++) {
			i = (ListIterator) Instances.get(index);
			e1 = null;
			while (i.hasNext() && e1 == null) {
				e1 = (Event) i.next();
				if (e1 instanceof ConditionEvent)
					e1 = null;
			}
			enabledEvents.add(index, e1);
			if (e1 == null)
				InstancesFinished++;
		}

		Level = new HashSet();
		while (InstancesFinished < size) {							//While there are instances to process
			foundMatch = false;
			for (index = 0; index<size && !foundMatch;index++) {				//search for a pair of matching events
				e1 = (Event) enabledEvents.get(index);
				if (e1 != null) {
					for (index2 = index + 1; index2<size && !foundMatch;index2++) {
						e2 = (Event) enabledEvents.get(index2);
						if (e2 != null) {
							foundMatch = (e1.getLabel().equals(e2.getLabel()));
							if (foundMatch)   {
								String from, to;
								if (e2 instanceof OutputEvent)  {
									to = ((OutputEvent) e2).getTo();
									from = ((InputEvent)e1).getFrom();
								}
								else  {
									to = ((OutputEvent)e1).getTo();
									from = ((InputEvent)e2).getFrom();
								}
								Out= Out + e2.getLabel() + ", ";
							}
						}
					}
				}
			}

			e1 = null;
			i = (ListIterator) Instances.get(index-1);
			while (i.hasNext() && e1 == null) {
				e1 = (Event) i.next();
				if (e1 instanceof ConditionEvent)  {
					//Out.println("\\condition{"+e1.getLabel().replace('_', '.')+"}{"+((String) InstancesNames.get(index-1)).replace('_', '.')+"}");
					//Out.println("\\nextlevel[2]");
					e1 = null;
				}
			}
			enabledEvents.set(index-1, e1);
			if (e1 == null)
				InstancesFinished++;

			e1 = null;
			i = (ListIterator) Instances.get(index2-1);
			while (i.hasNext() && e1 == null) {
				e1 = (Event) i.next();
				if (e1 instanceof ConditionEvent)  {
					//Out.println("\\condition{"+e1.getLabel().replace('_', '.')+"}{"+((String)InstancesNames.get(index2-1)).replace('_', '.')+"}");
					//Out.println("\\nextlevel[2]");
					e1 = null;
				}
			}
			enabledEvents.set(index2-1, e1);
			if (e1 == null)
				InstancesFinished++;
		}
		return Out;
	}

	//Returns set of Vectors with strings in them.
	public Set getAllTraces(LTSOutput o) throws Exception {
		boolean dbg = false;
		if (dbg) o.outln("Getting traces");
		HashSet Traces = new HashSet();
		int size = components().size();
		Vector Instances = new Vector(size); //vector(ListIterator)
		Vector enabledEvents = new Vector(size); //vector(events)
		Vector Names = new Vector(size);//vector(string)
		Vector Pos = new Vector(size);
		int index = 0;

		int InstancesFinished = 0;

		//Fill vectors: Names, Pos, Instances
		Iterator I = components().iterator();
		index = 0;
		while (I.hasNext()) {
				Instance Aux;
				String iname  = (String) I.next();
				try{Aux = componentInstance(iname);} catch (ComponentInstanceNotFound ex) {throw new Exception("Internal Consistency Error:" + ex.getMessage());}
				Names.add(index, iname);
				Pos.add(index, new Integer(0));
				Instances.add(index++, Aux);
		}


		//Create enabledEvents and update InstancesFinished
		for (index = 0; index<size; index++) {
			Instance i = (Instance) Instances.get(index);
			int p = ((Integer)Pos.get(index)).intValue();
			Event e;

			if (p < i.size())  {
				e = (Event) i.get(p);
				Pos.set(index, new Integer(p+1));
			}
			else
				e = null;

			enabledEvents.add(index, e);
			if (e == null)
				InstancesFinished++;
		}

		Trace t = new Trace();
		HashMap Count = new HashMap();
		getTrace(Traces, t, Instances, enabledEvents, Names, Pos, o);
		return Traces;
	}

	private boolean getTrace(Set Traces, Trace t, Vector Instances, Vector enabledEvents, Vector Names, Vector Pos, LTSOutput o )  throws Exception {
		int index = 0;
		int size = components().size();
		boolean Finished = false;
		boolean retVal = false;
		boolean FirstTime = true;
		boolean dbg = false;

		if (dbg) o.outln("Printing Trace");
		Vector pairs = GetAllPairsThatCanBeMoved(size, enabledEvents, o);
		if (pairs.size() == 0)
			Traces.add(t);
		else  {
			for (index = 0; index * 2 < pairs.size(); index++)  {
				Vector c_enabledEvents = (Vector) enabledEvents.clone();
				Vector c_Pos = (Vector) Pos.clone();
				Event e1 = (Event)c_enabledEvents.get(((Integer)pairs.get(2*index)).intValue());
				Event e2 = (Event)c_enabledEvents.get(((Integer)pairs.get(2*index + 1)).intValue());
				if (dbg) o.outln("About to move forward Indexes " + ((Integer)pairs.get(2*index)).intValue() + ", " + ((Integer)pairs.get(2*index + 1)).intValue() + ". Label " + e1.getLabel() + e2.getLabel());
				MoveForward(Instances, c_Pos, c_enabledEvents, ((Integer)pairs.get(2*index)).intValue(), o);
				MoveForward(Instances, c_Pos, c_enabledEvents, ((Integer)pairs.get(2*index + 1)).intValue(), o);
				if (dbg) o.outln("Moved forward...");

				Trace TraceCopy = (Trace)t.myClone();
				if (dbg) o.outln("cloned...");

				if (e1 instanceof OutputEvent)
					TraceCopy.add(e1.getLabel());
				else
					TraceCopy.add(e2.getLabel());
				if (dbg) o.outln("recursive call...");
				getTrace(Traces, TraceCopy, Instances, c_enabledEvents, Names, c_Pos, o);
			}
		}
		return retVal;
	}


	private Vector GetAllPairsThatCanBeMoved(int size, Vector enabledEvents, LTSOutput o)  {
		int index=0, index2=0,pos =0;
		Vector pairs = new Vector();
		boolean dbg = false;
		//o.outln("FindMatch.");

		for (index = 0; index<size ;index++) {				//search for a pair of matching events
			Event e1 = (Event) enabledEvents.get(index);
			if (e1 != null) {
				if (!(e1 instanceof ConditionEvent))  {
					for (index2 = index + 1; index2<size ;index2++) {
						Event e2 = (Event) enabledEvents.get(index2);
						if (e2 != null) {
							if (!(e2 instanceof ConditionEvent))  {
								if (e1.getLabel().equals(e2.getLabel())) {
									if (dbg) o.outln("Indexes " + index + ", " + index2 + ". Label " + e1.getLabel());
									pairs.add(pos++, new Integer(index));
									pairs.add(pos++, new Integer(index2));
								}
							}
						}
					}
				}
			}
		}
		return pairs;
	}


	public Set getFirstMoves(LTSOutput o) throws Exception {
		boolean dbg = false;
		StringSet Labels = new StringSet();
		HashSet Moves = new HashSet();
		int size = components().size();
		Vector Instances = new Vector(size); //vector(ListIterator)
		Vector enabledEvents = new Vector(size); //vector(events)
		Vector Names = new Vector(size);//vector(string)
		Vector Pos = new Vector(size);
		int index = 0;

		int InstancesFinished = 0;

		//Fill vectors: Names, Pos, Instances
		Iterator I = components().iterator();
		index = 0;
		while (I.hasNext()) {
				Instance Aux;
				String iname  = (String) I.next();
				try{Aux = componentInstance(iname);} catch (ComponentInstanceNotFound ex) {throw new Exception("Internal Consistency Error:" + ex.getMessage());}
				Names.add(index, iname);
				Pos.add(index, new Integer(0));
				Instances.add(index++, Aux);
		}


		//Create enabledEvents and update InstancesFinished
		for (index = 0; index<size; index++) {
			Instance i = (Instance) Instances.get(index);
			int p = ((Integer)Pos.get(index)).intValue();
			Event e;

			if (p < i.size())  {
				e = (Event) i.get(p);
				Pos.set(index, new Integer(p+1));
			}
			else
				e = null;

			enabledEvents.add(index, e);
			if (e == null)
				InstancesFinished++;
		}

		Vector pairs = GetAllPairsThatCanBeMoved(size, enabledEvents, o);
		for ( index = 0; index * 2 < pairs.size(); index++)  {
			Integer FirstInt = (Integer)pairs.get(2*index);
			Integer SecondInt = (Integer)pairs.get(2*index+1);
			Event e = (Event) enabledEvents.get(FirstInt.intValue());
			String label = e.getLabel();
			if (dbg) o.outln("Adding vector " + FirstInt.intValue()+ ", " + label + ", " + SecondInt.intValue());
			if (!Labels.contains(label))  {
				Labels.add(label);
				Vector v = new Vector(3);
				if (e instanceof OutputEvent)  {
					v.add(0, (String) Names.get(FirstInt.intValue()));
					v.add(1, label);
					v.add(2, (String) Names.get(SecondInt.intValue()));
				}
				else {
					v.add(0, (String) Names.get(SecondInt.intValue()));
					v.add(1, label);
					v.add(2, (String) Names.get(FirstInt.intValue()));
				}
				Moves.add(v);
			}
		}
		return Moves;
	}



	public boolean hasCommonFirstMoves(BasicMSC b, LTSOutput o) throws Exception {
		Set S1 = getFirstMoves(o);
		Set S2 = b.getFirstMoves(o);

		Iterator I1 = S1.iterator();
		while (I1.hasNext())  {
			Vector v1 = (Vector) I1.next();
			String s1 = (String) v1.get(1);
			//o.outln("Looking at label " + s1);
			Iterator I2 = S2.iterator();
			while (I2.hasNext())  {
				Vector v2 = (Vector) I2.next();
				String s2 = (String) v2.get(1);
				//o.outln("compared to " + s2);
				if (s1.equals(s2))  {
					o.outln("Common intial message found: " + s1);
					return true;
				}
			}
		}
		return false;
	}

	public Set getLastMoves(LTSOutput o) throws Exception {
		boolean dbg = false;
		StringSet Labels = new StringSet();
		HashSet Moves = new HashSet();
		int size = components().size();
		Vector Instances = new Vector(size); //vector(ListIterator)
		Vector enabledEvents = new Vector(size); //vector(events)
		Vector Names = new Vector(size);//vector(string)
		Vector Pos = new Vector(size);
		int index = 0;

		int InstancesFinished = 0;

		if (dbg) o.outln("Fill vectors: Names, Pos, Instances");
		Iterator I = components().iterator();
		index = 0;
		while (I.hasNext()) {
				Instance Aux;
				String iname  = (String) I.next();
				try{Aux = componentInstance(iname);} catch (ComponentInstanceNotFound ex) {throw new Exception("Internal Consistency Error:" + ex.getMessage());}
				Names.add(index, iname);
				Pos.add(index, new Integer(Aux.size()-1));
				Instances.add(index++, Aux);
		}


		if (dbg) o.outln("Create enabledEvents and update InstancesFinished");
		for (index = 0; index<size; index++) {
			Instance i = (Instance) Instances.get(index);
			int p = ((Integer)Pos.get(index)).intValue();
			Event e;

			if (p >= 0)  {
				e = (Event) i.get(p);
				Pos.set(index, new Integer(p-1));
			}
			else
				e = null;

			enabledEvents.add(index, e);
			if (e == null)
				InstancesFinished++;
		}

		if (dbg) o.outln("GetAllPairsThatCanBeMoved");
		Vector pairs = GetAllPairsThatCanBeMoved(size, enabledEvents, o);
		for ( index = 0; index * 2 < pairs.size(); index++)  {
			Integer FirstInt = (Integer)pairs.get(2*index);
			Integer SecondInt = (Integer)pairs.get(2*index+1);
			Event e = (Event) enabledEvents.get(FirstInt.intValue());
			String label = e.getLabel();
			if (dbg) o.outln("Adding vector " + FirstInt.intValue()+ ", " + label + ", " + SecondInt.intValue());
			if (!Labels.contains(label))  {
				Labels.add(label);
				Vector v = new Vector(3);
				if (e instanceof OutputEvent)  {
					v.add(0, (String) Names.get(FirstInt.intValue()));
					v.add(1, label);
					v.add(2, (String) Names.get(SecondInt.intValue()));
				}
				else {
					v.add(0, (String) Names.get(SecondInt.intValue()));
					v.add(1, label);
					v.add(2, (String) Names.get(FirstInt.intValue()));
				}
				Moves.add(v);
			}
		}
		return Moves;
	}

	public boolean hasPostponableMessage(BasicMSC B, LTSOutput o) throws Exception {
		boolean dbg = false;
		if (dbg) o.outln("hasPostponableMessages?");
		Set LastMoves = getLastMoves(o);
		if (dbg) o.outln("Got Last moves:" + LastMoves.size());
		Set FirstMoves = B.getFirstMoves(o);
		if (dbg) o.outln("Got First moves:" + FirstMoves.size());
		Iterator LM = LastMoves.iterator();
		boolean found = false;
		while (LM.hasNext() && !found)  {
			Vector move = (Vector) LM.next();
			String C1 = (String) move.get(0);
			String label = (String) move.get(1);
			String C2 = (String) move.get(2);
			Iterator FM = FirstMoves.iterator();
			if (dbg) o.outln("Got " + label + ", comparing");
			while (FM.hasNext() && !found)  {
				Vector m = (Vector) FM.next();
				if (dbg) o.outln("Got " + (String) m.get(1));
				if (!C1.equals((String) m.get(0)) && !C1.equals((String)m.get(2)) && !C2.equals((String)m.get(0)) && !C2.equals((String)m.get(2)))  {
					found = true;
				}
			}
		}
		return found;
	}

	public Vector getIdOfLastMessage(String C1, String label, String C2) {
		Instance I1 = getInstance(C1);
		Instance I2 = getInstance(C2);
		Vector v = new Vector(2);

		v.add(0, new Integer(I1.getIdOfLast()));
		v.add(1, new Integer(I2.getIdOfLast()));
		return v;
	}



	public void removeLastMessage(String C1, String label, String C2) {
		Instance I1 = getInstance(C1);
		Instance I2 = getInstance(C2);

		I1.deleteLast(1);
		I2.deleteLast(1);
	}

	public boolean PartitionVerticaly(Set Partitions, LTSOutput o)  {
		boolean dbg = false;
		Set NotProcessed = new HashSet();

		Iterator Aux  = instances.keySet().iterator();
		while (Aux.hasNext()) NotProcessed.add((String) Aux.next());

		while (NotProcessed.size()!=0)  {
			Iterator J = NotProcessed.iterator();
			String Comp = (String) J.next();
			StringSet Partition = new StringSet();
			Partitions.add(Partition);
			Partition.add(Comp);
			NotProcessed.remove(Comp);
			if (dbg) o.outln("New Partition, added " + Comp);
			boolean added = true;
			while (added)  {
				added = false;
				Iterator I = Partition.iterator();
				while (I.hasNext() && !added)  {
					Instance Inst = getInstance((String) I.next());
					for (int i=0; i<Inst.size(); i++)  {
						String SAux = ((MessageEvent) Inst.get(i)).getToFrom();
						if (!Partition.contains(SAux))  {
							added = true;
							Partition.add(SAux);
							NotProcessed.remove(SAux);
							if (dbg) o.outln("added " + SAux);
						}
					}
				}
			}
		}
		int NonTrivialPartitions = 0;
		Iterator J = Partitions.iterator();
		while (J.hasNext())  {
			if (((Set) J.next()).size() > 1)
				NonTrivialPartitions++;
		}
		return (NonTrivialPartitions > 1);
	}

	boolean OverlapsPartition(Set Partition)  {
		Iterator I = Partition.iterator();
		while (I.hasNext()) {
			String instName = (String) I.next();
			Instance Inst = getInstance(instName);
			if (Inst.size()>0)
				return true;
		}
		return false;
	}

	//returns true if the bMSC has a message that is independent of the partition.
	boolean IndependentMessage(Set Partition)  {
		Iterator I = instances.keySet().iterator();
		while (I.hasNext()) {
			String instName = (String) I.next();
			if (!Partition.contains(instName)) {
				Instance Inst = getInstance(instName);
				for (int i=0;i<Inst.size();i++)  {
					String SAux = ((MessageEvent) Inst.get(i)).getToFrom();
					if (!Partition.contains(SAux))
						return true;
				}
			}
		}
		return false;
	}

	public boolean IsIndependent(BasicMSC B, LTSOutput o)  {
		o.outln("C1");
		StringSet C1 = getActiveComponents(o);
		o.outln("C2");
		StringSet C2 = B.getActiveComponents(o);
		return (C1.intersection(C2).size() == 0);
	}

	public StringSet getActiveComponents(LTSOutput o)  {
		StringSet RetVal = new StringSet();
		Iterator I = instances.keySet().iterator();
		while (I.hasNext()) {
			String instName = (String) I.next();
			if (getInstance(instName).size()>0)  {
				o.outln(instName);
				RetVal.add(instName);
			}
		}
		return RetVal;
	}

	BasicMSC getPostponables(BasicMSC B, LTSOutput o) throws Exception {
		boolean dbg = false;
		BasicMSC P_B = (BasicMSC) B.clone();
		BasicMSC A_P = (BasicMSC) clone();
		BasicMSC P = new BasicMSC();
		P.copyComponents(B);


		boolean found = true;
		while (found)  {
			found = false;
			Set LastMoves = A_P.getLastMoves(o);
			Set FirstMoves = P_B.getFirstMoves(o);
			Iterator LM = LastMoves.iterator();

			while (LM.hasNext() && !found)  {
				Vector move = (Vector) LM.next();
				String C1 = (String) move.get(0);
				String label = (String) move.get(1);
				String C2 = (String) move.get(2);
				Iterator FM = FirstMoves.iterator();
				while (FM.hasNext() && !found)  {
					Vector m = (Vector) FM.next();
					if (!C1.equals((String)m.get(0)) && !C1.equals((String)m.get(2)) && !C2.equals((String)m.get(0)) && !C2.equals((String)m.get(2)))  {
						if (dbg) o.outln("Found postponable action "+ label);
						A_P.removeLastMessage(C1, label, C2);
						P_B.addMessage(C1, label, C2);
						P.addMessage(C1, label, C2);
						found = true;
					}
				}
			}
		}
		return P;
	}



	public boolean hasEventId(int Id) 	{
		Iterator I = components().iterator();
		while (I.hasNext()) {
			Instance Inst = (Instance) instances.get((String) I.next());
			for (int i = 0; i< Inst.size(); i++)  {
				Event e = Inst.get(i);
				if (e.Id == Id)
					return true;
			}
		}
		return false;
	}


	public Map getDependencies(LTSOutput o)  {
		if (SavedDependencies != null)
			return SavedDependencies;
		boolean dbg = false;
		int size = components().size();
		Vector Instances = new Vector(size); //vector(ListIterator)
		Vector Dependencies = new Vector(size);
		Vector InstanceNames = new Vector(size);
		Vector enabledEvents = new Vector(size); //vector(events)
		int index, index2 = 0;
		Iterator I;
		Instance Aux;
		ListIterator i;
		int InstancesFinished = 0;
		boolean foundMatch;
		Event e1 = null;
		Event e2 = null;
		String iname;


		if (dbg) o.outln("//Create Listiterators for instances");
		I = components().iterator();
		index = 0;
		while (I.hasNext()) {
				iname = (String) I.next();
				try{Aux = componentInstance(iname);} catch (ComponentInstanceNotFound ex) {throw new Error("Internal Consistency Error:" + ex.getMessage());}
				InstanceNames.add(index, iname);
				StringSet StSetAux = new StringSet();
				StSetAux.add(iname);
				Dependencies.add(index, StSetAux);
				Instances.add(index++, Aux.iterator());

		}

		if (dbg) o.outln("//Create enabledEvents");
		for (index = 0; index<size; index++) {
			i = (ListIterator) Instances.get(index);
			e1 = null;
			while (i.hasNext() && e1 == null) {
				e1 = (Event) i.next();
				if (e1 instanceof ConditionEvent)
					e1 = null;
			}
			enabledEvents.add(index, e1);
			if (e1 == null)
				InstancesFinished++;
		}

		while (InstancesFinished < size) {							//While there are instances to process
			foundMatch = false;
			for (index = 0; index<size && !foundMatch;index++) {				//search for a pair of matching events
				e1 = (Event) enabledEvents.get(index);
				if (e1 != null) {
					for (index2 = index + 1; index2<size && !foundMatch;index2++) {
						e2 = (Event) enabledEvents.get(index2);
						if (e2 != null) {
							foundMatch = (e1.getLabel().equals(e2.getLabel()));
						}
					}
				}
			}

			if (foundMatch) {
				if (dbg) o.outln("1");
				((Set)Dependencies.get(index-1)).add(InstanceNames.get(index2-1));
				((Set)Dependencies.get(index2-1)).add(InstanceNames.get(index-1));
				if (dbg) o.outln("2");
				((Set)Dependencies.get(index-1)).addAll((Set) Dependencies.get(index2-1));
				((Set)Dependencies.get(index2-1)).addAll((Set) Dependencies.get(index-1));
				if (dbg) o.outln("3");
				e1 = null;
				i = (ListIterator) Instances.get(index-1);
				while (i.hasNext() && e1 == null) {
					e1 = (Event) i.next();
					if (e1 instanceof ConditionEvent)
						e1 = null;
				}
				enabledEvents.set(index-1, e1);
				if (e1 == null)
					InstancesFinished++;

				e1 = null;
				i = (ListIterator) Instances.get(index2-1);
				while (i.hasNext() && e1 == null) {
					e1 = (Event) i.next();
					if (e1 instanceof ConditionEvent)
						e1 = null;
				}
				enabledEvents.set(index2-1, e1);
				if (e1 == null)
					InstancesFinished++;


			}
			else {
				String Message ="";
				for (index = 0; index <size; index++) {
					if (((Event) enabledEvents.get(index)) != null)
						Message = Message + ((Event) enabledEvents.get(index)).getLabel();
					else
						Message = Message + "-";

					if (index + 1 < size)
							Message = Message + ", ";
				}
				throw new Error("Inconsistent events in bMSC " + name + ". Impossible to pair one of the following event labels " + Message );
			}
		}
		if (dbg) o.outln("5");
		Map ret = new HashMap();
		for (int c = 0; c <size; c++)
			ret.put(InstanceNames.get(c), Dependencies.get(c));
		SavedDependencies = ret;
		return SavedDependencies;

	}


	//given c it returns those c' for which c canfinish before c'
	public Map getCanFinishBefore(LTSOutput o)  {
		boolean dbg = false;
		boolean dbg2 = false;
		if (dbg) o.outln("//getCanfinishBefore");
		if (SavedCanFinishBefore != null)
			return SavedCanFinishBefore;

		int size = components().size();
		Vector Dependencies = new Vector(size);
		Vector InstanceNames = new Vector(size);

		if (dbg) o.outln("//Create Listiterators for instances");
		Iterator I = components().iterator();
		int index = 0;
		while (I.hasNext()) {
				String iname = (String) I.next();
				InstanceNames.add(index, iname);
				StringSet StSetAux = new StringSet();
				Dependencies.add(index++, StSetAux);
		}

		for (int c = 0; c<size; c++)  {

			String DependsOn = (String) InstanceNames.get(c);
			StringSet Visited = (StringSet) Dependencies.get(c);


			boolean Cont = true;
			while (Cont)  {
				Visited.add(DependsOn);
				DependsOn = getLastDependency(DependsOn, o);
				if (DependsOn == null)
					Cont = false;
				else
					if (Visited.contains(DependsOn))
						Cont = false;
			}
			if (Visited.size() == 1)
				Visited.addAll(components());
		}




		Map ret = new HashMap();
		for (int c = 0; c <size; c++)  {
			ret.put(InstanceNames.get(c), Dependencies.get(c));
			if (dbg2) o.outln("Canfinishbefore for " +  InstanceNames.get(c) + " in " + name);
			if (dbg2) ((StringSet)Dependencies.get(c)).print(o);
		}
		SavedCanFinishBefore = ret;
		return SavedCanFinishBefore;

	}



	//if c == getLastDependency(c') then c canfinishbefore c'
	public String getLastDependency(String Component, LTSOutput o)  {
		boolean dbg = false;
		boolean dbg2 = false;
		if (dbg2) o.outln("Get Last Dependency for " + Component + " in " + name);
		if (SavedLastDependencies != null)
			return ((String) SavedLastDependencies.get(Component));




		int size = components().size();
		int ReturnComponent=-1;
		Vector Instances = new Vector(size); //vector(ListIterator)
		Vector Dependencies = new Vector(size);
		Vector InstanceNames = new Vector(size);
		Vector enabledEvents = new Vector(size); //vector(events)
		int index, index2 = 0;
		Iterator I;
		Instance Aux;
		ListIterator i;
		int InstancesFinished = 0;
		boolean foundMatch;
		Event e1 = null;
		Event e2 = null;
		String iname;


		if (dbg) o.outln("//Create Listiterators for instances");
		I = components().iterator();
		index = 0;
		while (I.hasNext()) {
				iname = (String) I.next();
				try{Aux = componentInstance(iname);} catch (ComponentInstanceNotFound ex) {throw new Error("Internal Consistency Error:" + ex.getMessage());}
				InstanceNames.add(index, iname);
				Dependencies.add(index, null);
				if (Component.equals(iname))
					ReturnComponent = index;
				Instances.add(index++, Aux.iterator());
		}

		if (dbg) o.outln("//Create enabledEvents");
		for (index = 0; index<size; index++) {
			i = (ListIterator) Instances.get(index);
			e1 = null;
			while (i.hasNext() && e1 == null) {
				e1 = (Event) i.next();
				if (e1 instanceof ConditionEvent)
					e1 = null;
			}
			enabledEvents.add(index, e1);
			if (e1 == null)
				InstancesFinished++;
		}

		while (InstancesFinished < size) {							//While there are instances to process
			foundMatch = false;
			for (index = 0; index<size && !foundMatch;index++) {				//search for a pair of matching events
				e1 = (Event) enabledEvents.get(index);
				if (e1 != null) {
					for (index2 = index + 1; index2<size && !foundMatch;index2++) {
						e2 = (Event) enabledEvents.get(index2);
						if (e2 != null) {
							foundMatch = (e1.getLabel().equals(e2.getLabel()));
						}
					}
				}
			}

			if (foundMatch) {
				Dependencies.set(index-1, (String) InstanceNames.get(index2-1));
				Dependencies.set(index2-1, (String) InstanceNames.get(index-1));


				e1 = null;
				i = (ListIterator) Instances.get(index-1);
				while (i.hasNext() && e1 == null) {
					e1 = (Event) i.next();
					if (e1 instanceof ConditionEvent)
						e1 = null;
				}
				enabledEvents.set(index-1, e1);
				if (e1 == null)
					InstancesFinished++;

				e1 = null;
				i = (ListIterator) Instances.get(index2-1);
				while (i.hasNext() && e1 == null) {
					e1 = (Event) i.next();
					if (e1 instanceof ConditionEvent)
						e1 = null;
				}
				enabledEvents.set(index2-1, e1);
				if (e1 == null)
					InstancesFinished++;


			}
			else {
				String Message ="";
				for (index = 0; index <size; index++) {
					if (((Event) enabledEvents.get(index)) != null)
						Message = Message + ((Event) enabledEvents.get(index)).getLabel();
					else
						Message = Message + "-";

					if (index + 1 < size)
							Message = Message + ", ";
				}
				throw new Error("Inconsistent events in bMSC " + name + ". Impossible to pair one of the following event labels " + Message );
			}
		}
		if (dbg) o.outln("5");

		Map ret = new HashMap();
		for (int c = 0; c <size; c++)   {
			ret.put(InstanceNames.get(c), Dependencies.get(c));
			if (dbg2) o.outln(InstanceNames.get(c) + " depends on " + Dependencies.get(c));
		}
		SavedLastDependencies = ret;

		return ((String) SavedLastDependencies.get(Component));

	}

}





