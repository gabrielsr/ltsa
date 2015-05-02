package synthesis;

import ic.doc.ltsa.common.iface.LTSOutput;
import java.util.*;

//Factor out a class Trasitions?


public class ConstraintSynthesiser {

	public boolean printConstraints(MyOutput Output, Specification S, String ConstraintName, LTSOutput o) throws Exception  {
		boolean dbg = false;
		if (dbg) o.outln("PrintConstraints");
		if (S.getNegbMSCs().size() > 0)  {
			String line = "";
			boolean first = true;
			Iterator J = S.getNegbMSCs().iterator();
			while (J.hasNext()) {
				String CName = (String) J.next();
				if (dbg) o.outln("Processing " +CName);

				NegScenario n = S.getNegbMSC(CName);
				if (n instanceof AfterUntilNegScenario)
					PrintConstraint(S.alphabet(), (AfterUntilNegScenario) n, Output, o);
				else if (n instanceof AbstractNegScenario)
					PrintConstraint(S.alphabet(), (AbstractNegScenario) n, Output, o);
				else if (n instanceof BasicNegScenario)
					PrintConstraint(S.alphabet(), (BasicNegScenario) n, Output, o);

				if (first) first = false; else line = line + " || ";
				line = line + CName;
			}
			line = "||"+ConstraintName+" = (" + line + ").";
			Output.println(line);
		}
		else
			Output.println(ConstraintName+" = STOP.");
		return (S.getNegbMSCs().size() > 0);
	}



////////////////////////////////////////////////////////////////////////////////////
//
//Basic Negative Scnearios
//
////////////////////////////////////////////////////////////////////////////////////

	private void PrintConstraint(StringSet Alphabet, BasicNegScenario n, MyOutput Output, LTSOutput o) throws Exception  {
		boolean dbg = false;

		String name = n.name();
		String disallowed = n.disallowed();
		BasicMSC P = n.precondition();
		if (dbg) o.outln("Printing Constraint " + name);

		P.name = name+"_Precondition";


		if (dbg) o.outln("Building Complete Alphabet ");

		if (!P.empty())  {
			if (dbg) o.outln("Precondition is not empty.");

			//Annotations correspond to TOSEM paper


			//Rule a: Set P and Rule b.
			if (dbg) o.outln("Building Prefixes.");
			Set PTraces = P.getAllTraces(o);
			Set PPrefixes = new HashSet();
			Set Pends = new HashSet();
			Trace PInit = getPrefixes(PTraces, Pends, PPrefixes, o);

			//Rule c1.
			if (dbg) o.outln("Adding prefix extending transitions.");
			Map PTransitions = new HashMap();
			PrefixExtendingTransitions(PPrefixes, PTransitions, o);

			//Rule  a: State f
			if (dbg) o.outln("Create Final State");
			Iterator I = Pends.iterator();
			Trace Final = ((Trace) I.next()).myClone();
			Final.add("ThisIsFinal");
			PPrefixes.add(Final);


			//Rule c3 and c4.
			if (dbg) o.outln("Adding undefined transitions to Final state");
			AddUndefinedTransitions(PPrefixes, PTransitions, Alphabet, Final, o);
			if (dbg) o.outln("Removing constrained transition");
			Iterator J = Pends.iterator();
			while (J.hasNext()) {
				Trace From = (Trace) J.next();
				removeTransition(PTransitions, From, disallowed, Final, o);
			}


			printLTS(PInit, P.name, PTransitions.keySet(), PTransitions, Output, o);
			Output.println("||" + name + " = " + P.name + ".");
		}
		else   {
			o.outln("Empty Precondition is not allowed in " + name );
			throw new Error();
		}

	}

////////////////////////////////////////////////////////////////////////////////////
//
//Abstract Negative Scnearios
//
////////////////////////////////////////////////////////////////////////////////////

	private void PrintConstraint(StringSet UniversalAlphabet, AbstractNegScenario n, MyOutput Output, LTSOutput o) throws Exception  {
		boolean dbg = false;

		String name = n.name();
		String disallowed = n.disallowed();
		BasicMSC P = n.precondition();
		StringSet Alphabet = n.Alphabet();
		if (Alphabet == null) {
			Alphabet = new StringSet();
			Alphabet.addAll(UniversalAlphabet);
		}

		if (dbg) o.outln("Building Ignore");
		StringSet IgnoreAlphabet = new StringSet();
		if (!Alphabet.contains(disallowed))
			IgnoreAlphabet.add(disallowed);


		if (dbg) IgnoreAlphabet.print(o);



		if (dbg) o.outln("Printing Constraint " + name);


		P.name = name+"_Precondition";

		if (!P.empty())  {
			if (dbg) o.outln("a");

			//Rule a. and b.
			Set PTraces = P.getAllTraces(o);
			Set PPrefixes = new HashSet();
			Set Pends = new HashSet();
			Trace PInit = getPrefixes(PTraces, Pends, PPrefixes, o);

			//Rule c1, c2 (overdoes the m not equal l case
			if (dbg) o.outln("Adding prefix extending transitions.");
			Map PTransitions = new HashMap();
			PrefixExtendingTransitions(PPrefixes, PTransitions, o);

			if (dbg) o.outln("Adding undefined transitions to suffixes");
			AddUndefinedTransitionsToSuffixes(PPrefixes, PTransitions, Alphabet, o);

			if (dbg) o.outln("Adding undefined transitions as loops ");
			AddUndefinedTransitionsToSelf(PPrefixes, PTransitions, IgnoreAlphabet, o);

			//Rule c2. enforces case m equal l
			if (dbg) o.outln("Removing disallowed transitions from Ending states");
			RemoveTransitionsStartingAt_andLabelledWith_(PTransitions, Pends, disallowed, o);

			printLTS(PInit, P.name, PTransitions.keySet(), PTransitions, Output, o);
			Output.println("||" + name + " = " + P.name + ".");
		}
		else   {
			o.outln("Empty Precondition is not allowed in " + name );
			throw new Error();
		}
	}


////////////////////////////////////////////////////////////////////////////////////
//
//After Until Negative Scnearios
//
////////////////////////////////////////////////////////////////////////////////////


	private void PrintConstraint(StringSet UniversalAlphabet, AfterUntilNegScenario n, MyOutput Output, LTSOutput o) throws Exception  {
		boolean dbg = false;
		boolean printLTS = false;

		String name = n.name();
		BasicMSC A = n.after();
		String disallowed = n.disallowed();
		BasicMSC U = n.until();

		StringSet AAlphabet = n.afterAlphabet();
		if (AAlphabet == null) {
			AAlphabet = new StringSet();
			AAlphabet.addAll(UniversalAlphabet);
		}
		StringSet UAlphabet = n.untilAlphabet();
		if (UAlphabet == null) {
			UAlphabet = new StringSet();
			UAlphabet.addAll(UniversalAlphabet);
		} else {
			if (UAlphabet.contains(disallowed))  {
				o.outln("Disallowed action '" + disallowed + "' included in until condition");
				throw new Error();
			}
		}


		if (dbg) o.outln("Printing Constraint " + name);

		A.name = name+"_After";
		U.name = name+"_Until";


		if (dbg) o.outln("Building Complete Alphabet ");
		StringSet CompleteAlphabet = new StringSet();;
		CompleteAlphabet.addAll(AAlphabet);
		CompleteAlphabet.addAll(UAlphabet);
		CompleteAlphabet.add(disallowed);
		if (dbg) CompleteAlphabet.print(o);

		if (dbg) o.outln("Building AIgnore");
		StringSet AIgnoreAlphabet = new StringSet();
		AIgnoreAlphabet.addAll(CompleteAlphabet);
		AIgnoreAlphabet.removeAll(AAlphabet);
		if (dbg) AIgnoreAlphabet.print(o);

		if (dbg) o.outln("Building UIgnore ");
		StringSet UIgnoreAlphabet = new StringSet();
		UIgnoreAlphabet.addAll(CompleteAlphabet);
		UIgnoreAlphabet.removeAll(UAlphabet);
		UIgnoreAlphabet.remove(disallowed);
		if (dbg) UIgnoreAlphabet.print(o);


		if (dbg) o.outln("Removing " + disallowed + " from UAlphabet");
		if (dbg) UAlphabet.print(o);
		if (UAlphabet.contains(disallowed))  {
			UAlphabet.remove(disallowed);
			if (dbg) UAlphabet.print(o);
		}
		else
			if (dbg) o.outln("not found");



		if (!A.empty() && !U.empty())  {
			if (dbg) o.outln("a");


			//Rule a. Set P_a (but with proper prefixes too)
			Set ATraces = A.getAllTraces(o);
			Set APrefixes = new HashSet();
			Set Aends = new HashSet();
			Trace AInit = getPrefixes(ATraces, Aends, APrefixes, o);

			//Rule a. Set P_u (but with proper prefixes too)
			Set UTraces = U.getAllTraces(o);
			Set UPrefixes = new HashSet();
			Set Uends = new HashSet();
			Trace UInit = getPrefixes(UTraces, Uends, UPrefixes, o);


			//Rule	c1.
			if (dbg) o.outln("Adding prefix extending transitions.");
			Map ATransitions = new HashMap();
			PrefixExtendingTransitions(APrefixes, ATransitions, o);

			if (dbg) o.outln("Adding undefined transitions to Final state");
			AddUndefinedTransitionsToSuffixes(APrefixes, ATransitions, AAlphabet, o);

			if (dbg) o.outln("Adding undefined transitions as loops ");
			AddUndefinedTransitionsToSelf(APrefixes, ATransitions, AIgnoreAlphabet, o);


			//Rule c3. (PArtly)
			if (dbg) o.outln("Adding prefix extending transitions.");
			Map UTransitions = new HashMap();
			PrefixExtendingTransitions(UPrefixes, UTransitions, o);

			if (dbg) o.outln("Adding undefined transitions to Final state");
			AddUndefinedTransitionsToSuffixes(UPrefixes, UTransitions, UAlphabet, o);

			if (dbg) o.outln("Adding undefined transitions as loops ");
			AddUndefinedTransitionsToSelf(UPrefixes, UTransitions, UIgnoreAlphabet, o);

			if (printLTS) printLTS(AInit, "AfterLTS", ATransitions.keySet(), ATransitions, Output, o);
			if (printLTS) printLTS(UInit, "UntilLTS", UTransitions.keySet(), UTransitions, Output, o);

			//Rule a
			//Rule c2
			Map CombTxs = new HashMap();
			Pair Init = CrossProduct(APrefixes, ATransitions, UPrefixes, UTransitions, CombTxs, o);

			if (printLTS) printLTS(Init, "CrossLTS", CombTxs.keySet(), CombTxs, Output, o);



			//For all states in Until (FromA, FromU) -lbl->(ToA, ToU)
	//Rule c4.  // if ToA not in ends, ToU in ends, send to After state ToA
	//Rule c5	// if ToA in ends, ToU not in ends, send to (ToA, Init)
	//Rule c6.	// if ToA in ends, ToU in ends, lbl in AAlphabet, send to (ToA, Init)
	//Rule c7.	// if ToA in ends, ToU in ends, lbl not in AAlphabet, send to After state x, where x is maximal proper suffix of ToA

	//Rule a// Plus, if FromU in UEnds: Remove
			LinkUntilWithAfter(Aends, ATransitions, AAlphabet, UInit, Uends, UTransitions, CombTxs, o);


			//For all states in After From -> To
	//Rule c2.	//if To in ends, send to (To, Uinit)
	//Rule a	//Remove Froms in Aends
			LinkAfterWithUntil(Aends, ATransitions, UInit, CombTxs.keySet(), o);



			Map AllTransitions = CombTxs;
			AllTransitions.putAll(ATransitions);
			if (printLTS) printLTS(AInit, "WithUnreachable", AllTransitions.keySet(), AllTransitions, Output, o);
			printLTS(AInit, A.name, Reachable(AllTransitions, AInit, o), AllTransitions, Output, o);
			Output.println("||" + name + " = " + A.name + ".");

		}
		else   {
			o.outln("Empty After or Until is not allowed in " + name );
			throw new Error();
		}
	}

	//For all states in Until (FromA, FromU) -lbl->(ToA, ToU)
		// if ToA in ends, ToU in ends, lbl not in AAlphabet, send to After state x, where x is maximal proper suffix of ToA
		// if ToA in ends, ToU in ends, lbl in AAlphabet, send to (ToA, Init)
		// if ToA not in ends, ToU in ends, send to After state ToA
		// if ToA in ends, ToU not in ends, send to (ToA, Init)
	// Plus, if FromU in UEnds: Remove
	private void LinkUntilWithAfter(Set Aends, Map ATransitions, Set AAlphabet, Trace UInit, Set Uends, Map UTransitions, Map CombTxs, LTSOutput o) throws Exception {
		boolean dbg = false;
		if (dbg) o.outln("LinkUntilWithAfter");
		Set ToAdd = new HashSet();
		Set ToRemove = new HashSet();
		Iterator I = CombTxs.keySet().iterator();
		while (I.hasNext()) {
			Pair CombFrom = (Pair) I.next();
			if (dbg) o.outln("CombFrom: A:" + CombFrom.After.getString() + " B:" + CombFrom.Until.getString());
			if (Uends.contains(CombFrom.Until)) {
				ToRemove.add(CombFrom);
				if (dbg) o.outln("Removed");
			} else {
				Iterator J = ((Set) CombTxs.get(CombFrom)).iterator();
				while (J.hasNext()) {
					Vector v = (Vector) J.next();
					Pair CombTo = (Pair) v.get(0);
					String lbl = (String) v.get(1);
					Vector w = new Vector();
					w.add(0, CombFrom);
					w.add(1, lbl);

					if (dbg) o.outln("lbl " + lbl);
					if (dbg) o.outln("CombTo: A:" + CombTo.After.getString() + " B:" + CombTo.Until.getString());

					if (Aends.contains(CombTo.After) && Uends.contains(CombTo.Until)) {
						if (dbg) o.outln("CombTo.A is END!! and CombTo.B is END!!");
						if (!AAlphabet.contains(lbl)) {
							if (dbg) o.outln("Send CombFrom to state in After which is maximal proper suffix of CombTo.A");
							Trace BestTo = getMaximalSuffixOf(CombTo.After, ATransitions.keySet(), o);
							if (dbg) o.outln("BestTo: A:" + BestTo.getString());
							w.add(2, BestTo);
							ToAdd.add(w);
						} else {
							if (dbg) o.outln("Force CombTo.B to reset: Send it to Init");
							Pair auxP = new Pair();
							auxP.After = CombTo.After;
							auxP.Until = UInit;
							w.add(2, getPair(auxP, CombTxs.keySet(), o));
							ToAdd.add(w);
						}
					} else if (!Aends.contains(CombTo.After) && Uends.contains(CombTo.Until)) {
						if (dbg) o.outln("CombTo.A is not End!! and CombTo.B is END!!");
						if (dbg) o.outln("Take CombFrom to state CombTo.A in A");
						w.add(2, CombTo.After);
						ToAdd.add(w);
					}  else if (Aends.contains(CombTo.After) && !Uends.contains(CombTo.Until)) {
						if (dbg) o.outln("CombTo.A is END!! and CombTo.B is not END!!");
						if (dbg) o.outln("Force CombTo.B to reset: Send it to Init");
						Pair auxP = new Pair();
						auxP.After = CombTo.After;
						auxP.Until = UInit;
						w.add(2, getPair(auxP, CombTxs.keySet(), o));
						ToAdd.add(w);

					} else {
						if (dbg) o.outln("CombTo.A is not END!! and CombTo.B is not END!!");
					}
				}
			}
		}

		Iterator J = ToAdd.iterator();
		while (J.hasNext()) {
			Vector v = (Vector) J.next();
			addTransitions(CombTxs, (Pair) v.get(0), (String) v.get(1), v.get(2), "Override", o);
		}

		J = ToRemove.iterator();
		while (J.hasNext())
			CombTxs.remove(J.next());

	}



	//For all states in After From -> To
		//if To in ends, send to (To, Uinit)
	//Remove Froms in Aends
	private void LinkAfterWithUntil(Set Aends, Map ATransitions, Trace UInit, Set CombStates, LTSOutput o) throws Exception {
		boolean dbg = false;
		if (dbg) o.outln("LinkAfterWithUntil");
		Set ToAdd = new HashSet();
		Set ToRemove = new HashSet();
		Iterator I = ATransitions.keySet().iterator();
		while (I.hasNext()) {
			Trace From = (Trace) I.next();
			if (dbg) o.outln("From: "+ From.getString()+ "------------------");

			if (Aends.contains(From))
				ToRemove.add(From);
			else {
				Iterator J = ((Set) ATransitions.get(From)).iterator();
				while (J.hasNext()) {
					Vector v = (Vector) J.next();
					Trace To = (Trace) v.get(0);
					String lbl = (String) v.get(1);

					if (dbg) o.outln("To: "+ To.getString());
					if (dbg) o.outln("Lbl: "+ lbl);

					if (Aends.contains(To)) {
						if (dbg) o.outln("To is an ending trace");
						Pair auxP = new Pair();
						auxP.After = To;
						auxP.Until = UInit;
						Vector w = new Vector();
						w.add(0, From);
						w.add(1, lbl);
						w.add(2, getPair(auxP, CombStates, o));
						ToAdd.add(w);
					}
					else
						if (dbg) o.outln("To is not an ending trace");
				}
			}
		}
		Iterator J = ToAdd.iterator();
		while (J.hasNext()) {
			Vector v = (Vector) J.next();
			addTransitions(ATransitions, (Trace) v.get(0), (String) v.get(1), (Pair) v.get(2), "Override", o);
		}
		J = ToRemove.iterator();
		while (J.hasNext())
			ATransitions.remove(J.next());

	}



/*





						From_lbl.add(lbl);

						if (dbg) o.outln("Search for pair in CombStates that is suffix");
						Pair BestTo = GetCombStateWithFixedUntilAndAfterIsAProperPrefixOf(From_lbl, ATransitions.keySet(), UInit, CombStates, o);
						if (BestTo == null) {
							throw new Exception("Didn't find suffix!!");
						}
						else	 {
							if (dbg) {o.outln("Found Suffix:"); BestTo.After.print(o);}
							Vector w = new Vector();
							w.add(0, From);
							w.add(1, lbl);
							w.add(2, BestTo);
							ToAdd.add(w);
						}


*/
	private Pair GetCombStateWithFixedUntilAndAfterIsAProperPrefixOf(Trace After, Set AStates, Trace Until, Set CombStates, LTSOutput o) {
		boolean dbg = false;
		if (dbg) o.outln("Search for pair in CombStates that has B =" + Until.getString() );
		if (dbg) o.outln("           A is maximal proper suffix og =" + After.getString() );


		Pair Candidate = new Pair();

		Candidate.After = getMaximalSuffixOf(After, AStates, o);
		Candidate.Until = Until;

		return getPair(Candidate, CombStates, o);
	}

	private Trace getMaximalSuffixOf(Trace T, Set States, LTSOutput o) {
		boolean dbg = false;
		if (dbg) o.outln("Looking for maximal suffix of " + T.getString() + " in set of " + States.size() + " States");
		Iterator K = States.iterator();
		Trace Best = null;
		int BestSize = -1;
		while (K.hasNext()) {
			Trace Candidate = (Trace) K.next();
			if (dbg) o.outln("We have a candidate " + Candidate.getString());
			if (T.size() > Candidate.size())  {
				if (dbg) o.outln("Could be a proper suffix");
				if (Candidate.size() > BestSize) {
					if (dbg) o.outln("And could be a longer suffix than previous");
					if (Candidate.equals(T.subtrace(T.size()-(Candidate.size())))) {
						if (dbg) o.outln("Equal!");
							Best = Candidate;
							BestSize = Candidate.size();
					}
					else
						if (dbg) o.outln("NotEqual!");
				} else
					if (dbg) o.outln("Can't improve previous suffix");
			} else
				if (dbg) o.outln("Can't be PROPER suffix");
		}
		return Best;
	}



	private Pair CrossProduct(Set AStates, Map ATxs, Set UStates, Map UTxs, Map Transitions, LTSOutput o)  {
		boolean dbg = false;		boolean dbg2 = false;

		if (dbg) o.outln("Get Initial states of When and Until Constraints");
		Trace AInit = getInit(AStates);
		Trace UInit = getInit(UStates);
		Pair InitialPair = null;

		if (dbg) o.outln("Initialise recursion");
		HashSet Rest = new HashSet();
		Iterator I = AStates.iterator();

		while (I.hasNext()) {
			Pair aux = new Pair();
			aux.After = (Trace) I.next();
			aux.Until = UInit;
			Rest.add(aux);

			if (aux.After == AInit)
				InitialPair = aux;
		}

		if (dbg2) o.outln("Building cross product");
		while (Rest.size()!=0)  {
			Pair p = null;
			if (dbg2) o.outln("Get a state to process");
			{
				Iterator Iaux=Rest.iterator();
				p=(Pair) Iaux.next();
			}
			Transitions.put(p, new HashSet());
			Trace FromU = p.Until;
			if (dbg2) o.outln("U in state " + FromU.getString());
			Trace FromA = p.After;
			if (dbg2) o.outln("A is in state " + FromA.getString());

			if (dbg) o.outln("Get possible moves from A State...");
			Iterator J = ((Set)ATxs.get(FromA)).iterator();
			while (J.hasNext())  {
				Vector t = (Vector) J.next();
				String lbl = (String) t.get(1);
				Trace ToA = (Trace) t.get(0);
				if (dbg2) o.outln("Got ToA " + ToA.getString());

				if (dbg2) o.outln("Get move for " + lbl + " from U state");
				Iterator K = ((Set)UTxs.get(FromU)).iterator();
				boolean found = false;
				Trace ToU=null;
				while (K.hasNext() && !found)  {
					Vector v = (Vector) K.next();
					found = lbl.equals((String) v.get(1));
					ToU = (Trace) v.get(0);
				}

				if (!found) {
					if (dbg2) o.outln("U state has not move for label " + lbl);
					if (dbg2) o.outln("It should be the disallowed, thus no transition should be created");
				}
				else {
					if (dbg2) o.outln("Found ToU " + ToU.getString());

					if (AStates.contains(ToA) && UStates.contains(ToU)) {
						if (dbg2) o.outln("New state: ToA: " + ToA.getString() + " ToU: " + ToU.getString());
						Pair np = new Pair(ToA, ToU);

						if (dbg) o.outln("getting equiv object if new state has already been reached...");
						Pair Existing = getPair(np, Transitions.keySet(), o);

						if (Existing == null) {
							Existing = getPair(np, Rest, o);
							if (Existing == null) {
								if (dbg2) o.outln("Its a new node...");
								Rest.add(np);
							} else {
								np = Existing;
							}
						}
						else {
							if (dbg2) o.outln("Its an old node...");
							np = Existing;
						}

						if (dbg2) o.outln("adding transition...");
						Vector v = new Vector();
						v.add(0, np);
						v.add(1, lbl);
						((Set) Transitions.get(p)).add(v);
					} else
						if (dbg) o.outln("Pair does not belong to the corss product");

				}
			}
			if (dbg) o.outln("removing processed state");
			Rest.remove(p);
			if (dbg) o.outln("removed");
		}
		if (dbg) o.outln("Finished");
		return InitialPair;
	}


	private Set Reachable(Map Transitions, Object Init, LTSOutput o) throws Exception {
		boolean dbg = false;

		if (dbg) o.outln("Initialise recursion");
		HashSet Rest = new HashSet();
		Rest.add(Init);

		HashSet Reachables = new HashSet();

		while (Rest.size()!=0)  {
			Object p = null;
			if (dbg) o.outln("Get a state to process");
			{
				Iterator I=Rest.iterator();
				p= I.next();
			}

			if (!Transitions.keySet().contains(p)) {
				if (p instanceof Trace)
					throw new Exception("Found Deadlock state!" + ((Trace)p).getString());
				else if (p instanceof Pair)
					throw new Exception("Found Deadlock state! a:" + (((Pair)p).After).getString() + " b:" + (((Pair)p).Until).getString() );
				else
					throw new Exception("Found Deadlock state! And cant figure out the type!");
			}

			Iterator I = ((Set)Transitions.get(p)).iterator();
			while (I.hasNext())  {
				Vector t = (Vector) I.next();
				String lbl = (String) t.get(1);
				Object To = t.get(0);
				if (!Reachables.contains(To))
					Rest.add(To);
			}
			Reachables.add(p);
			Rest.remove(p);
		}
		return Reachables;
	}


	private Trace getInit(Set Nodes)  {
		Trace Init = null;
		for (Iterator I = Nodes.iterator(); I.hasNext() && Init==null;) {
			Trace t = (Trace) I.next();
			if (t.size()==0)
				Init = t;
		}
		return Init;
	}

////////////////////////////////////////////////////////////////////////////////////
//
//Operations on Transition
//
////////////////////////////////////////////////////////////////////////////////////
	//Adds transitions (x-l->y) if x.l=y
	//Map: Prefix->setof{(Prefix, lbl)}
	private void PrefixExtendingTransitions(Set Prefixes, Map Transitions, LTSOutput o) throws Exception  {
		boolean dbg = false;
		if (dbg) o.outln("PrefixExtendingTransitions");


		Iterator I = Prefixes.iterator();
		while (I.hasNext())  {
			Trace From = (Trace) I.next();
			if (dbg) {o.outln("Analysing From state:"); From.print(o);}

			Iterator J = Prefixes.iterator();
			while (J.hasNext())  {
				Trace To  = (Trace) J.next();
				if (dbg) {o.outln("Looking at possible To state:"); To.print(o);}
				if ((To.size() == From.size()+1) && From.isPrefixOf(To)) {
					String lbl = To.get(To.size()-1);
					if (dbg) o.outln("This pair requires a transition labelled: "+ lbl);
					addTransitions(Transitions, From, lbl, To, "Determinisitic", o);
				}
			}
		}
	}


	//Adds transitions (x-l->T) to all states x with no enabled transition labelled l
	//Map: Prefix->setof{(Prefix, lbl)}
	private void AddUndefinedTransitions(Set States, Map Transitions, StringSet Alphabet, Trace T, LTSOutput o) throws Exception  {
		boolean dbg = false;
		if (dbg) o.outln("AddUndefinedTransitions");

		Iterator I = States.iterator();
		while (I.hasNext())  {
			Trace From = (Trace) I.next();
			if (dbg) {o.outln("Analysing From state:"); From.print(o);}

			Iterator J = Alphabet.iterator();
			while (J.hasNext())  {
				String lbl = (String) J.next();
				if (dbg) o.outln("Looking at label :"+lbl);
				if (!existsTransition(Transitions, From, lbl, o)) {
					if (dbg) o.outln("Found undefined label");
					addTransitions(Transitions, From, lbl, T, "Determinisitic", o);
				}
			}
		}
	}


	//Adds transitions (x-l->y) to all states x with no enabled transition labelled l where y is the largest suffix of x.l in States BUT y is not equal x.l
	//Map: Prefix->setof{(Prefix, lbl)}
	private void AddUndefinedTransitionsToSuffixes(Set States, Map Transitions, StringSet Alphabet, LTSOutput o) throws Exception  {
		boolean dbg = false;
		if (dbg) o.outln("AddUndefinedTransitionsToSuffixes");

		Iterator I = States.iterator();
		while (I.hasNext())  {
			Trace From = (Trace) I.next();
			if (dbg) {o.outln("Analysing From state:"); From.print(o);}

			Iterator J = Alphabet.iterator();
			while (J.hasNext())  {
				String lbl = (String) J.next();
				if (dbg) o.outln("Looking at label :"+lbl);

				if (!existsTransition(Transitions, From, lbl, o)) {
					Trace From_lbl = From.myClone();
					From_lbl.add(lbl);
					if (dbg) {o.outln("From_lbl:"); From_lbl.print(o);}

					Trace BestTo = null; int BestToSize = -1;
					Iterator K = States.iterator();
					while (K.hasNext())  {
						Trace To = (Trace) K.next();
						if (dbg) {o.outln("Analysing To state:"); To.print(o);}
						if (dbg) {o.outln("From_lbl size = " +From_lbl.size() + ", To size =" +To.size());}
						if (From_lbl.size() > To.size())  {
							if (To.size() > BestToSize) {
								if (dbg) {o.outln("Comparing with:"); (From_lbl.subtrace(From_lbl.size()-(To.size()))).print(o);}
								if (To.equals(From_lbl.subtrace(From_lbl.size()-(To.size())))) {
									if (dbg) o.outln("Equal!");
									BestTo = To;
									BestToSize = To.size();
								}
								else
									if (dbg) o.outln("NotEqual!");
							}
						}
					}
					if (BestTo == null) {
						//throw new Exception("Didn't find suffix!!");
						if (dbg) o.outln("No Suffix:");
					}
					else	 {
						if (dbg) {o.outln("Found Suffix:"); BestTo.print(o);}
						addTransitions(Transitions, From, lbl, BestTo, "Determinisitic", o);
					}
				}
			}
		}
	}





	//Adds transitions (x-l->x) to all states x with no enabled transition labelled l
	//Map: Prefix->setof{(Prefix, lbl)}
	private void AddUndefinedTransitionsToSelf(Set States, Map Transitions, StringSet Alphabet, LTSOutput o) throws Exception  {
		boolean dbg = false;
		if (dbg) o.outln("AddUndefinedTransitionsToSelf");

		Iterator I = States.iterator();
		while (I.hasNext())  {
			Trace From = (Trace) I.next();
			if (dbg) {o.outln("Analysing From state:"); From.print(o);}

			Iterator J = Alphabet.iterator();
			while (J.hasNext())  {
				String lbl = (String) J.next();
				if (dbg) o.outln("Looking at label :"+lbl);
				if (!existsTransition(Transitions, From, lbl, o)) {
					if (dbg) o.outln("Found undefined label");
					addTransitions(Transitions, From, lbl, From, "Determinisitic", o);
				}
			}
		}
	}






	private boolean existsTransition(Map Transitions, Trace From, String lbl, LTSOutput o) {
		boolean dbg = false;
		if (!Transitions.keySet().contains(From))
			return false;
		else {
			Set Tx = ((Set) Transitions.get(From));
			if (dbg) o.outln("Check if there is a transition From---" + lbl + "--->");
			boolean found = false;
			Iterator I = Tx.iterator();
			Vector vAux = null;
			while (I.hasNext() && !found)  {
				vAux = (Vector) I.next();
				if (dbg) o.outln("Comparing with -" +(String) vAux.get(1)+"-");
				found = (lbl.equals((String) vAux.get(1)));
			}
			return found;
		}
	}


	private boolean removeTransition(Map Transitions, Trace From, String lbl, Trace To, LTSOutput o) {
		boolean dbg = false;
		if (!Transitions.keySet().contains(From))
			return false;
		Set Tx = ((Set) Transitions.get(From));

		if (dbg) o.outln("Check if there is a transition From---" + lbl + "--->");
		boolean found = false;
		Iterator I = Tx.iterator();
		Vector vAux = null;
		while (I.hasNext())  {
			vAux = (Vector) I.next();
			if (dbg) o.outln("Comparing with " +(String) vAux.get(1));
			if((lbl.equals((String) vAux.get(1)))) {
				if (To.equals((Trace) vAux.get(0))) {
					Tx.remove(vAux);
					I = Tx.iterator();
					found=true;
				}
			}
		}
		if (found) {
			return true;
		}
		else
			return false;
	}

	private boolean removeTransition(Map Transitions, Trace From, String lbl, LTSOutput o) {
		boolean dbg = false;
		if (!Transitions.keySet().contains(From))
			return false;
		Set Tx = ((Set) Transitions.get(From));

		if (dbg) o.outln("Check if there is a transition From---" + lbl + "--->");
		boolean found = false;
		Iterator I = Tx.iterator();
		Vector vAux = null;
		while (I.hasNext())  {
			vAux = (Vector) I.next();
			if (dbg) o.outln("Comparing with " +(String) vAux.get(1));
			if((lbl.equals((String) vAux.get(1)))) {
					Tx.remove(vAux);
					I = Tx.iterator();
					found =true;
			}
		}
		if (found) {

			return true;
		}
		else
			return false;
	}

	private void RemoveTransitionsStartingAt_andLabelledWith_(Map Transitions, Set ends, String disallowed, LTSOutput o) {
		Iterator I = ends.iterator();
		while (I.hasNext()) {
			Trace From = (Trace) I.next();
			removeTransition(Transitions, From, disallowed, o);
		}
	}

	private void addTransitions(Map Transitions, Object From, String lbl, Object To, String Mode, LTSOutput o) throws Exception {
		//Modes are "Override", "Determinisitic"
		boolean dbg = false;
		if (!Transitions.keySet().contains(From))
			Transitions.put(From, new HashSet());
		Set Tx = ((Set) Transitions.get(From));

		if (dbg) o.outln("Check if there is a transition From---" + lbl + "--->");
		boolean found = false;
		Iterator I = Tx.iterator();
		Vector vAux = null;
		while (I.hasNext() && !found)  {
			vAux = (Vector) I.next();
			if (dbg) o.outln("Comparing with " +(String) vAux.get(1));
			found = (lbl.equals((String) vAux.get(1)));
		}

		//Prepare transition to be added
		Vector v = new Vector();
		v.add(0, To);
		v.add(1, lbl);


		//Compare result of search with mode.
		if (Mode == "Determinisitic")  {
			if (found)
				throw new Exception("Mode is determinisitic but there is a transition already!");
			else {
				if (dbg) o.outln("Add new transition");
				Tx.add(v);
			}
		} else if (Mode == "Override") {
			if (found) {
				if (dbg) o.outln("Delete existing and add current");
				Tx.remove(vAux);
				Tx.add(v);
			} else {
				if (dbg) o.outln("Add new transition");
				Tx.add(v);
			}
		} else
			throw new Exception("Unknown mode!");
	}



////////////////////////////////////////////////////////////////////////////////////
//
//Prefixes
//
////////////////////////////////////////////////////////////////////////////////////


	private Trace getPrefixes(Set Traces, Set ends, Set Prefexis, LTSOutput o)  {
		boolean dbg = false;
		if (dbg) o.outln("getPrefixes");
		Iterator I = Traces.iterator();
		while (I.hasNext())  {
			if (dbg) o.outln("gettingTrace");
			Trace t = (Trace) I.next();
			if (dbg) o.outln("gotTrace");
			addPrefexis(Prefexis, t, ends, o);
		}
		Trace Init = new Trace();
		Prefexis.add(Init);
		return Init;
	}

	private void addPrefexis(Set P, Trace t, Set ends, LTSOutput o) {
		boolean dbg = false;
		if (dbg) o.outln("addPrefexis");
		Trace Init = null;
		boolean found = false;
		for (int i=t.size()-1;i>=0 && !found;i--)  {
			Trace subtrace = t.subtrace(0, i);
			Iterator I = P.iterator();
			while (I.hasNext() && !found)  {
				Trace aux = (Trace) I.next();
				found = subtrace.equals(aux);
			}
			if (!found)
				P.add(subtrace);
			if (i==t.size()-1)
				ends.add(subtrace);
		}
	}



////////////////////////////////////////////////////////////////////////////////////
//
//Others
//
////////////////////////////////////////////////////////////////////////////////////



	private void printLTS(Object Init, String Name, Set States, Map Transitions, MyOutput Output, LTSOutput o) throws Exception  {
		boolean dbg = false;
		boolean printmapping = true;

		Map Ids = new HashMap();
		if (dbg) o.outln("BuildMapping"); {
			if (printmapping) Output.println("/*");
			Iterator I = States.iterator();
			int Id = 1;
			while (I.hasNext())  {
				Object t = I.next();
				if (t != Init)  {
					Ids.put(t, new Integer(Id));
					if (printmapping) {
						if (t instanceof Trace)
							Output.println(Id + "->" + ((Trace)t).getString());
						else if (t instanceof Pair)
							Output.println(Id + "-> (A:" + ((Pair)t).After.getString() + ", B:" + ((Pair)t).Until.getString() + ")");
					}
					Id++;
				}
				else  {
					Ids.put(t, new Integer(0));
					if (printmapping) {
						if (t instanceof Trace)
							Output.println("0->" + ((Trace)t).getString());
						else if (t instanceof Pair)
							Output.println("0-> (A:" + ((Pair)t).After.getString() + ", B:" + ((Pair)t).Until.getString() + ")");
					}
				}
			}
			if (printmapping) Output.println("*/");
		}

		if (dbg) o.outln("Initial declaration for When");
		Output.println("" + Name + " = N0,");

		if (dbg) o.outln("Other declarations for When ");
		Iterator I = States.iterator();
		while (I.hasNext())  {
			Object From = I.next();
			Integer FromId = (Integer) Ids.get(From);
			Output.print("N" + FromId + "=");
			Iterator J = ((Set)Transitions.get(From)).iterator();
			boolean first = true;
			boolean end = false;
			FSPLabel l = new FSPLabel();
			while (J.hasNext())  {
				Vector v = (Vector) J.next();
				String lbl = (String) v.get(1);
				Object To = v.get(0);
				Integer ToId = (Integer) Ids.get(To);
				if (ToId == null)
					o.outln("Node not found:");

				if (first)  {Output.print("(");  first = false; } else Output.print(" | ");
				l.setMessageLabel(lbl, null, null);
				Output.print(l.getLabel() + " -> N" + ToId);
			}
			if (!end)
				Output.print(")");
			if (I.hasNext())
				Output.println(",");
			else
				Output.println(".");
		}
		if (dbg) o.outln("Finished");
	}








	private Trace getLoop(Map Tx, LTSOutput o)  {
		Trace Loop = null;
		for (Iterator I = Tx.keySet().iterator(); I.hasNext() && Loop==null;) {
			Trace t = (Trace) I.next();
			if (isEnd(t, Tx, o))  {
				Set Traces = (Set) Tx.get(t);
				Iterator J = Traces.iterator();
				Vector v = (Vector) J.next();
				Loop = (Trace) v.get(2);
			}
		}
		return Loop;
	}
	private boolean isEnd(Trace T, Map Txs, LTSOutput o)  {
		boolean dbg = false;
		if (dbg) o.outln("In isEnd");
		Iterator I = ((Set)Txs.get(T)).iterator();
		if (I.hasNext())  {
			Vector t = (Vector) I.next();
			String lbl = (String) t.get(1);
			if (dbg) o.outln("Out isEnd");
			return (lbl.equals("END"));
		}
		if (dbg) o.outln("Out isEnd");
		return false;

	}


	private Pair getPair(Pair p, Set S, LTSOutput o)  {
		boolean dbg = false;
		if (dbg) o.outln("In pairIn");
		Iterator J = S.iterator();
		boolean found = false;
		while (J.hasNext())  {
			Pair np = (Pair) J.next();
			if (np.After == p.After && np.Until == p.Until)
				return np;
		}
		if (dbg) o.outln("out pairIn");
		return null;

	}










}



class Pair  {
	public Trace After;
	public Trace Until;

	Pair()  {
		After = null;
		Until = null;
	}


	Pair(Trace a, Trace b)  {
		After  = a;
		Until = b;
	}

	public String getString ()  {
		String S = "After:";
		if (After==null)
			S = S + "null";
		else
			S = S + this.After.getString();

		S = S + " Until:";
		if (Until==null)
			S = S + "null";
		else
			S = S + this.Until.getString();


		return S;
	}

	public boolean isAfterState()  {
		return (Until == null);
	}

	public boolean isUntilState()  {
		return (After != null);
	}

	public boolean myEquals(Pair p)  {
		return this.After.equals(p.After) && this.Until.equals(p.Until);
	}

	public boolean equals(Object o) {
		if (o instanceof Pair)
			return myEquals((Pair) o);
		else
			return false;

	}
}

/*
						if (dbg) o.outln("Take CombFrom to a state in A which is max suffix of FromA.lbl.");
							Trace From_lbl = CombFrom.After.myClone();
							if (AAlphabet.contains(lbl)) {
								if (dbg) o.outln("Extending From.A with lbl");
								From_lbl.add(lbl);
							}

							//Search for state in ATransitions.keySet that is suffix
							Object BestTo = null; int BestToSize = -1;
							Iterator K = ATransitions.keySet().iterator();
							while (K.hasNext()) {
								Trace To = (Trace) K.next();
								if (dbg) o.outln("To " + To.getString());
								if (From_lbl.size() >= To.size())  {
									if (dbg) o.outln("Size is right");
									if (To.size() > BestToSize) {
										if (dbg) o.outln("Could improve Best");
										if (To.equals(From_lbl.subtrace(From_lbl.size()-(To.size())))) {
											if (dbg) o.outln("Equal!");
											BestTo = To;
											BestToSize = To.size();
										}
										else
											if (dbg) o.outln("NotEqual!");
									}
								}
							}
							if (BestTo == null) {
								throw new Exception("Didn't find suffix!!");
								//if (dbg) o.outln("No Suffix:");
							}
							else	 {
								if (dbg) o.outln("Found Suffix:" + ((Trace) BestTo).getString());
								Vector w = new Vector();
								w.add(0, CombFrom);
								w.add(1, lbl);
								w.add(2, BestTo);
								ToAdd.add(w);
							}

*/

		/*				if (dbg) o.outln("CombTo.B is not END. FromA-lbl->AEnds and not FromU-lbl->UEnds, take them to a State in Until with max suffix of FromA.lbl, and ToU.");
						if (Aends.contains(CombTo.After)) {
							if (dbg) o.outln("CombTo.A is END: Search for state in CombStates that is suffix");
							Trace From_lbl = CombFrom.After.myClone();
							if (AAlphabet.contains(lbl))
								From_lbl.add(lbl);

							Pair BestTo = null; int BestToSize = -1;
							Iterator K = CombTxs.keySet().iterator();
							while (K.hasNext()) {
								Pair NewCombTo = (Pair) K.next();
								if (dbg) o.outln("NewCombTo: A:" + NewCombTo.After.getString() + " B:" + NewCombTo.Until.getString());
								if (NewCombTo.Until == CombTo.Until) {
									if (dbg) o.outln("NewCombTo.U = OldCombTo.U");
									if (From_lbl.size() > NewCombTo.After.size())  {
										if (dbg) o.outln("NewCombTo.A is short enough to be a proper prefix");
										if (NewCombTo.After.size() > BestToSize) {
											if (dbg) o.outln("NewCombTo.A is long enough to better previous");
											if (NewCombTo.After.equals(From_lbl.subtrace(From_lbl.size()-(NewCombTo.After.size())))) {
												if (dbg) o.outln("Equal!");
												BestTo = NewCombTo;
												BestToSize = NewCombTo.After.size();
											}
											else
												if (dbg) o.outln("NotEqual!");
										}
									}
								}
							}
							if (BestTo == null) {
								throw new Exception("Didn't find suffix!!");
								//if (dbg) o.outln("No Suffix:");
							}
							else	 {
								if (dbg) {o.outln("Found Suffix:"); BestTo.After.print(o);}
								Vector w = new Vector();
								w.add(0, CombFrom);
								w.add(1, lbl);
								w.add(2, BestTo);
								ToAdd.add(w);
							}
						}
					}	*/
// DONT DO: FromA-lbl->AEnds and not FromU-lbl->UEnds, take them to a State in Until with max suffix of FromA.lbl, and ToU.


/*
						if (dbg) o.outln("CombTo.A is END!! and CombTo.B is END!!");
						if (dbg) o.outln("Send CombFrom to state in Comb where U: empty, A: maximal proper suffix of CombTo.A");
						if (dbg) o.outln("UInit: " + UInit.getString());
						Pair BestTo = GetCombStateWithFixedUntilAndAfterIsAProperPrefixOf(CombTo.After, ATransitions.keySet(), UInit, CombTxs.keySet(), o);
						if (dbg) o.outln("BestTo: A:" + BestTo.After.getString() + " B:" + BestTo.Until.getString());
						w.add(2, BestTo);
						ToAdd.add(w);

*/
