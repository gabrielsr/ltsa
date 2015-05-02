package synthesis;

import ic.doc.ltsa.common.iface.LTSOutput;

import java.util.*;

/*
class InconsistentSpecification extends Exception {
	public InconsistentSpecification() {
		super();
	}
	public InconsistentSpecification(String S) {
		super(S);
	}	
}

class BasicMSCNotFound extends Exception {
	public BasicMSCNotFound() {
		super();
	}
	public BasicMSCNotFound(String S) {
		super(S);
	}	
}

class bMSCDefinedTwice extends Exception {
	public bMSCDefinedTwice() {
		super();
	}
	public bMSCDefinedTwice(String S) {
		super(S);
	}	
}
*/
/**
A <code>Specification</code> object is a MSC specification consisting of a set of bMSCs (<code>BasicMSC</code>) 
and a relation betweeen bMSCs that models the possibility of a bMSC to be coninued by another. 
Although inconsistent specifications can be built, methods may assume consistency: 
bMSCs are consistent, all bMSC have the instances for the same set of components, Each message label is used as an output by exactly one 
component and as an input by exactly one (different) component. 

@see BasicMSC
@see Instance
*/
public class Specification {
	private HashSet Final = new HashSet();
	private HashSet Initial = new HashSet();
	private HashMap H = new HashMap();
	private HashMap Hp = new HashMap(); // the HashMap for probabilistic transitions
	private boolean prob = false;
	
	private HashMap NegScenarios = new HashMap();	//name->NegScenario													Code = "B"asic, "W"hen, "U"ntil
	
	/* 	Supposes that all bMSCs have the same 
			set of components, and that message labels 
			are conssitent
	*/

	public void addbMSC(BasicMSC A) {
		if (!H.keySet().contains(A)){
			H.put(A, new HashSet());
		    //Adds the BMSC into the Hash for probabilistic transitions
			Hp.put(A, new HashSet());
		}
	}
	
	public void setProbabilistic(boolean _prob){
		this.prob = _prob;
	}
	
	public boolean getProbabilistic(){
		return this.prob;
	}
	
	public void addNegbMSC(String name, BasicMSC When, StringSet WScope, String disallowed, BasicMSC Until, StringSet UScope) {
		NegScenario n;
		
		if (Until == null)
			n = new AbstractNegScenario(name, When, WScope, disallowed);
		else
			n = new AfterUntilNegScenario(name, When, WScope, disallowed, Until, UScope);		
		NegScenarios.put(name, n);
	}

	public void addBasicNegbMSC(String name, BasicMSC precondition, String disallowed) {
		BasicNegScenario n = new BasicNegScenario(name, precondition, disallowed);
		NegScenarios.put(name, n);
	}

	public void addRelation(BasicMSC A, BasicMSC B) {
		if (!((Set) H.get(A)).contains(B))
			((Set) H.get(A)).add(B);
	}
	
	public void addProbabilisticRelation(BasicMSC A, BasicMSC B, String probValue, boolean _prob) {
		
		this.setProbabilistic(_prob);
		if (!((Set) H.get(A)).contains(B)){
			((Set) H.get(A)).add(B);
			//we keep both HashMaps updated temporarily. the purpose is to leave only Hp.
			ProbMSC probMSC = new ProbMSC(B, probValue); 
			((Set) Hp.get(A)).add(probMSC);			
		}
	};
	
	public void addRelationInit(BasicMSC B) {
		if (!Initial.contains(B))  {
			Initial.add(B);
		}
	}
	
	public void addRelationFinal(BasicMSC A) {
		if (!Final.contains(A))  {
			Final.add(A);
		}
	}
	
	public Set getbMSCs() {
		return (H.keySet());
	}

  /*
   * The method below returns the probability value of the transitions in the HMSC for a certain BMSC b
   */
	
	public String getTransProbability(String from, BasicMSC to) throws Exception{
		String retVal ="";
		ProbMSC probMSC;
		BasicMSC bMSC, auxbMSC;
		boolean found = false;
		Iterator iProb;
		
		Iterator I = Hp.keySet().iterator();
				
		while ((I.hasNext()) && (!found)) {
			bMSC = (BasicMSC) I.next();
			if (bMSC.name.equals(from)){
					iProb = ((Set) Hp.get(bMSC)).iterator();
				while ((iProb.hasNext())) {
				
					probMSC = (ProbMSC) iProb.next();
					auxbMSC = probMSC.getbMSC();
				
					if (auxbMSC.name.equals(to.name)) {
						retVal = probMSC.getprobValue();
						found = true;
						break;
					}
				}
			}
		}
		if (found)
			return retVal;
		else 
			throw new Exception("BMSc Not Found");
	}
	
	public Set getNegbMSCs() {
		return (NegScenarios.keySet());
	}

	public NegScenario getNegbMSC(String name) {
		return ((NegScenario) NegScenarios.get(name));
	}


	public void removebMSC(BasicMSC A) {
		H.remove(A);
	}

	public void removeProbMSCs(BasicMSC A) {
		Hp.remove(A);
	}
	
	public Set getContinuationsInit() {
		return Initial;
	}
	
	public Set getContinuationsFinal() {
		return Final;
	}	
	
	public Set getContinuations(BasicMSC A) {
		return (Set) H.get(A);
	}
	
	//public Set getProbContinuations(BasicMSC A){
	//	return (Set) Hp.get(A);
	//}
	
	public BasicMSC getBMsc(String S) throws Exception {
		BasicMSC retVal = new BasicMSC();
		boolean found = false;
		
		Iterator I = H.keySet().iterator();
		while (I.hasNext()) {
			retVal = (BasicMSC) I.next();
			if (retVal.name.equals(S)) {
				found = true;
				break;
			}
		}		
		if (found)
			return retVal;
		else {
			throw new Exception("BMSc Not Found");
		}
	}
	
	
	public boolean containsBMsc(String S) {
		boolean retVal = false;
		BasicMSC aux;
		
		Iterator I = H.keySet().iterator();
		while (I.hasNext() && !retVal) {
			aux = (BasicMSC) I.next();
			retVal = aux.name.equals(S);
		}		
		
		if (!retVal)  {
			I = NegScenarios.keySet().iterator();
			while (I.hasNext() && !retVal) {
				retVal = ((String) I.next()).equals(S);
			}		
		}
		
		return retVal;
	}

	public boolean hasSamePositiveBehaviour(Specification S, LTSOutput o)  {
		try  {
		boolean equivalent = true;
		
		equivalent = (this.getbMSCs().size() == S.getbMSCs().size());
		
		Iterator I=getbMSCs().iterator();
		while (I.hasNext() && equivalent)  {
			BasicMSC b1 = (BasicMSC) I.next();
			if (S.containsBMsc(b1.name))  {
				BasicMSC b2 = S.getBMsc(b1.name);
				equivalent = b1.isTheSameAs(b2, o);
				if (equivalent) 
					equivalent = (getContinuations(b1).size() == S.getContinuations(b2).size());
					
				Iterator J = getContinuations(b1).iterator();
				while (J.hasNext() && equivalent)  {
					String name1 = ((BasicMSC) J.next()).name;
					Iterator K = S.getContinuations(b2).iterator();
					boolean found = false;
					while (K.hasNext() && !found)  {
						String name2 = ((BasicMSC) K.next()).name;
						found = name1.equals(name2);
					}
					equivalent = found;
				}			
			}				
			else
				equivalent = false;
		}
		Iterator J = getContinuationsInit().iterator();
		while (J.hasNext() && equivalent)  {
			String name1 = ((BasicMSC) J.next()).name;
			Iterator K = S.getContinuationsInit().iterator();
			boolean found = false;
			while (K.hasNext() && !found)  {
				String name2 = ((BasicMSC) K.next()).name;
				found = name1.equals(name2);
			}
			equivalent = found;
		}	
		
		return equivalent;
		}catch (Exception e) {throw new Error();}
	}

	
	
	public Set components() { //Assumes all bMSCs have the same components
		Iterator I;	
		I = H.keySet().iterator();
		if (I.hasNext()) 
			return ((BasicMSC)I.next()).components();
		else
			return new HashSet();	
	}
	
	public Map getComponentInstances(String name) throws Exception {
		HashMap S = new HashMap();
		BasicMSC b;
		Iterator I;	
		I = H.keySet().iterator();
		while (I.hasNext()) {
			b = (BasicMSC) I.next();
			try {
				S.put(b.name, b.componentInstance(name));
			} catch (ComponentInstanceNotFound e) {
				throw new Exception("Component instance '" + name + "' cannot be found in bMSC '" + b.name + "'.");
			}
		}
		return S;
	}

	
	public boolean containsConditions() throws Exception {
		boolean Contains = false;
		Iterator I;	
		I = H.keySet().iterator();
		while (I.hasNext() && !Contains) {
			BasicMSC b = (BasicMSC) I.next();
			Contains = b.containsConditions();
		}
		return Contains;
	}
	
	public void removeConditions() throws Exception {
		Iterator I;	
		I = H.keySet().iterator();
		while (I.hasNext()) {
			BasicMSC b = (BasicMSC) I.next();
			b.removeConditions();
		}
	}

	
	
	/**
	@exception ComponentInstanceNotFound
	InconsistentLabelUse
	loopBackMessage
	*/
	
	public void checkConsistency() throws Exception {
		boolean retVal = true;
		
		//All bMSCs contain the same set of components. 
		//sameComponents(); No need anymore....see addMissingComponents.
		
			
		//Every bMSC appearing in the hMSC must have been defined previously. 
		//Checked during construction....
		
   		//Every message label is used only between one pair of components with always the same one as the sender. Messages are not sent by a component to iteself
		consistentLabels();
		
   		//For every event label in every bMSC, there is a one to one correspondence between its inputs and outputs. 
		// And that lines can be drawn horizontally
		consistentEvents();
			
		//No bMSC is called after a component.
		bMSCLabels();
	}
	
	//InconsistentLabelUse
	private Map consistentLabels() throws Exception {
		Iterator I;
		HashMap M = new HashMap(); //label->(bMSC name, to:componentName,from:componentName) 
		
		
		I = getbMSCs().iterator();
		while (I.hasNext()) 
				((BasicMSC) I.next()).consistentLabels(M);				
		I = getNegbMSCs().iterator();
		while (I.hasNext())  {
				String aux = (String) I.next();
				NegScenario n = getNegbMSC(aux);
				if (n instanceof BasicNegScenario)
					((BasicNegScenario) n).precondition().consistentLabels(M);
				else if (n instanceof AbstractNegScenario)
					((AbstractNegScenario) n).precondition().consistentLabels(M);	
				else if (n instanceof AfterUntilNegScenario) {
					((AfterUntilNegScenario) n).after().consistentLabels(M);
					((AfterUntilNegScenario) n).until().consistentLabels(M);	
				} else 
					throw new Exception("Unknown negative scenario");
		}
		return M;
	}

	private void bMSCLabels() throws Exception {
		Iterator I = getbMSCs().iterator();
		if (I.hasNext())  {
			BasicMSC B = (BasicMSC) I.next();				
			Iterator J = B.components().iterator();
			while (J.hasNext()) { 
				String aux = (String) J.next();
				if (containsBMsc(aux))
					throw new Exception("Label " + aux + " is used for a bMSC and a message");
			}
		}
	}


	
	public Map getLabelMap() {
		Map M;
		try {
			M = consistentLabels();
			return M;
		}
		catch (Exception e) {
			return new HashMap();
		}
	}
		
	public StringSet alphabet()  {
		StringSet L = new StringSet(); //Ignores Negative Scenarios to avoid loop. Assumes negative scenarios alphabet is a subset...
		Iterator I;

		I = getbMSCs().iterator();
		while (I.hasNext()) 
				((BasicMSC) I.next()).addToAlphabet(L);				
		return L;		
	}
	//InconsistentEvents
	private void consistentEvents() throws Exception {
		Iterator I;

		I = getbMSCs().iterator();
		while (I.hasNext()) 
				((BasicMSC) I.next()).consistentEvents();				
	}
	

	
	
	private void sameComponents() throws ComponentInstanceNotFound {
		Iterator I;
		BasicMSC B = null;
		BasicMSC A;
		
		I = getbMSCs().iterator();
		while (I.hasNext()) {
			if (B == null)
				B = (BasicMSC) I.next();
			else {
				A = (BasicMSC) I.next();

				try {
					B.hasAllComponentsIn(A);
				}
				catch (ComponentInstanceNotFound e) {
					throw new ComponentInstanceNotFound("bMSC" + A.name + " does not contain an instance for " + e.getMessage());
				}
			
				try {
					A.hasAllComponentsIn(B);
				}
				catch (ComponentInstanceNotFound e) {
					throw new ComponentInstanceNotFound("bMSC" + B.name + " does not contain an instance for " + e.getMessage());
				}				
			}
		}
	}
		
	public void print(MyOutput Out) {
		Iterator I,J;	
		BasicMSC b,c;

		I = getbMSCs().iterator();
		while (I.hasNext()) {
			b = (BasicMSC)I.next();
			b.print(Out);
		}
		
		Out.println("hmsc;");

		I = getContinuationsInit().iterator();
		while (I.hasNext()) {
			b = (BasicMSC)I.next();
			Out.println("Init -> " + b.name + ";");
		}
		
		I = getContinuationsFinal().iterator();
		while (I.hasNext()) {
			b = (BasicMSC)I.next();
			Out.println(b.name + " -> Stop;");
		}
		
		J = getbMSCs().iterator();
		while (J.hasNext()) {
			c = (BasicMSC) J.next();
			I = getContinuations(c).iterator();
			while (I.hasNext()) {
				b = (BasicMSC) I.next();
				Out.println(c.name + " -> " +  b.name + ";");
			}
		}
		Out.println("endhmsc");
	}
	
	public void printLatex(StringBuffer buff) {
		MyOutput Out = new MyOutput(buff);
		Iterator I,J;	
		BasicMSC b,c;
		Out.println("\\documentclass{article}");
		Out.println("\\usepackage{msc}");
		Out.println("\\begin{document}");
		
		
		I = getbMSCs().iterator();
		while (I.hasNext()) {
			b = (BasicMSC)I.next();
			b.printLatex(Out);
		}
		
		Out.println("hmsc;");
		Out.println("");
		Out.println("");

		I = getContinuationsInit().iterator();
		while (I.hasNext()) {
			b = (BasicMSC)I.next();
			Out.println("Init $\\Rightarrow$ " + b.name.replace('_', '.') + ";");
			Out.println("");
			Out.println("");
		}
		
	
		J = getbMSCs().iterator();
		while (J.hasNext()) {
			c = (BasicMSC) J.next();
			I = getContinuations(c).iterator();
			while (I.hasNext()) {
				b = (BasicMSC) I.next();
				Out.println(c.name.replace('_', '.') + " $\\Rightarrow$ " +  b.name.replace('_', '.') + ";");
				Out.println("");
				Out.println("");
			}
		}
		
		I = getContinuationsFinal().iterator();
		while (I.hasNext()) {
			b = (BasicMSC)I.next();
			Out.println(b.name.replace('_', '.') + " $\\Rightarrow$ Stop;");
			Out.println("");
			Out.println("");
		}
		
		Out.println("endhmsc");
		Out.println("");
		Out.println("");
		Out.println("\\end{document}");		
	}
	
	
	public boolean identicalbMSCs(BasicMSC B1, BasicMSC B2, LTSOutput o) throws Exception {
		boolean equivalent = true;
		equivalent = B1.isTheSameAs(B2, o);
//		if (equivalent) o.outln("EQUAL");
//		o.outln("Checked for equality");
		return equivalent;		

	}
	
	
	
	
	
	
	//Replaces B1 with B2 and deletes B1 ASSUMING THAT B2 is in the specification already!
	public void replaceAndDelete(BasicMSC B1, BasicMSC B2, LTSOutput o)  {
//		o.outln("Analysing Initial set");
		if (Initial.contains(B1))  {
			addRelationInit(B2);
			Initial.remove(B1);
		}
//		o.outln("Analysing Final set");
		if (Final.contains(B1))  {
			addRelationInit(B2);
			Final.remove(B1);
		}

		Iterator I = getbMSCs().iterator();					
		while (I.hasNext())  {
			BasicMSC B = (BasicMSC) I.next();
//			o.outln("Analysing bMSCs" + B.name);
			Set S = ((Set) H.get(B));
			if (S.contains(B1))  {
				S.add(B2);
				S.remove(B1);
			}
		}
		H.remove(B1);	
	}
	
	
	public void AddScenarioMessages() {
		Iterator I;	
		
		I = getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC b;
			b = (BasicMSC) I.next();
			b.AddScenarioEvents();
		}		
	}
	public Set RemoveScenarioMessages() {
		Iterator I;	
		Set S = new HashSet();
		
		I = getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC b;
			b = (BasicMSC) I.next();
			b.RemoveScenarioEvents(S);
		}		
		return S;
	}

	public Set getScenarioMessages() {
		Iterator I;	
		Set S = new HashSet();
		
		I = getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC b;
			b = (BasicMSC) I.next();
			b.getScenarioEvents(S);
		}		
		return S;
	}


	public void RemoveLabelsNotIn(StringSet s) {
		Iterator I;	
		
		I = getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC b;
			b = (BasicMSC) I.next();
			b.RemoveLabelsNotIn(s);
		}		
	}
	
	public boolean Normalised(LTSOutput o) throws Exception {
		Iterator I =  getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC b = (BasicMSC) I.next();
			//o.outln("Analysing bMSCs" + b.name);
			Set Cont = getContinuations(b);
			//o.outln("Got the continuations");
			if (Cont.size() > 1)  {
				//o.outln("More than one continuation");
				Iterator J1 =  Cont.iterator();
				while (J1.hasNext()) {
					BasicMSC b1 = (BasicMSC) J1.next();
					//o.outln("Looking at continuation " + b1.name);
					Iterator J2 =  Cont.iterator();
					while (J2.hasNext())  {
						BasicMSC b2 = (BasicMSC) J2.next();
						//o.outln("against continuation " + b2.name);
						if (b1 != b2)  { 
							if (b1.hasCommonFirstMoves(b2,o))  {
								o.outln(b1.name + " and " + b2.name + " have common initial messages.");
								return false; 	
							}
						}
					}
				}
			}
		}			
		return true;
	}




	public void eliminateEmptyScenarios(LTSOutput o)  throws Exception {
		boolean found;
		boolean dbg = false;
		
		if (dbg) o.outln("eliminating");
		Iterator I =  getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC b = (BasicMSC) I.next();
			if (dbg) o.outln("got " + b.name);
			found = false;
			if (b.empty())  {
				found = true;
				if (dbg) o.outln("its empty!");
				Set S = getContinuations(b);
				Iterator J = getbMSCs().iterator();
				while (J.hasNext())  {
					BasicMSC c = (BasicMSC) J.next();
					if (dbg) o.outln("looking at " + c.name);
					if (getContinuations(c).contains(b))  {
						if (dbg) o.outln("adding  continuations");
						Iterator s = S.iterator();
						while (s.hasNext())
							getContinuations(c).add(s.next());	
						if (dbg) o.outln("removing continuations");
						getContinuations(c).remove(b);
					}
				}
				if (getContinuationsInit().contains(b)) {
					if (dbg) o.outln("adding from Init");
					Iterator s = S.iterator();
					while (s.hasNext())
						getContinuationsInit().add(s.next());	
					if (dbg) o.outln("removing continuations");
					getContinuationsInit().remove(b);
				}
			}
			if (found)  {
				removebMSC(b);
				I = getbMSCs().iterator();
			}
		}
	}


	private boolean DetectCycle(BasicMSC A, Set Partition, LTSOutput o) {
		Iterator I = getContinuations(A).iterator();
		boolean found = false;
		while (I.hasNext() && !found)  
			found = DetectCycleRec(A, (BasicMSC) I.next(), Partition, o);
		return found;
	}
	
	private boolean DetectCycleRec(BasicMSC A, BasicMSC B, Set Partition, LTSOutput o) {
		if (A == B) 
			return true;
		if (B.OverlapsPartition(Partition))	
			return false;
		
		Iterator I = getContinuations(B).iterator();
		boolean found = false;
		while (I.hasNext() && !found)  
			found = DetectCycleRec(A, (BasicMSC) I.next(), Partition, o);
		return found;
	}
	
	private void CheckRegularLanguage(BasicMSC A, Set Partitions, LTSOutput o) throws Exception {
		boolean dbg=false;
		if (dbg) o.outln(A.name + "can be partitionedVerticaly in a non-trivial way");
		Iterator Part= Partitions.iterator();
		boolean found=false;
		while (Part.hasNext() && !found)  {
			Set Partition = (Set)Part.next();
			if (Partition.size()>1)  {
				if (dbg) o.outln("Checking cycle for a partition");
				found = DetectCycle(A, Partition, o); 
			}
		}
		if (found)
			throw new Exception("Non regular language: Loop from bMSC " + A.name);	
	}
	
	
	private void CheckRegularLanguage(LTSOutput o) throws Exception {
		Iterator I =  getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC A = (BasicMSC) I.next();
			Set Partitions = new HashSet();	
			if (A.PartitionVerticaly(Partitions, o))	
				CheckRegularLanguage(A, Partitions, o);
		}
	}
/*			else  {
				if (dbg) o.outln(A.name + "cannot be partitionedVerticaly in a non-trivial way");
				Iterator Part= Partitions.iterator();
				boolean found=false;
				while (Part.hasNext() && !found)  {
					Set Partition = (Set)Part.next();
					if (Partition.size() < A.components().size() && Partition.size()>1)  {
						if (dbg) o.outln("Checking cycle for a partition");
						found = DetectCycle2(A, Partition, o); 
					}
				}
				if (found)
					throw new Exception("Non regular language: Loop from bMSC " + A.name);
			}
		}
	}
/*
	private boolean DetectCycle2(BasicMSC A, Set Partition, LTSOutput o) {
		Iterator I = getContinuations(A).iterator();
		boolean found = false;
		while (I.hasNext() && !found)  
			found = DetectCycle2Rec(A, (BasicMSC) I.next(), Partition, false, o);
		return found;
	}


	private boolean DetectCycle2Rec(BasicMSC A, BasicMSC B, Set Partition, boolean Flag, LTSOutput o) {
		if (A == B) 
			return Flag;
		
		HashSet Partitions = new HashSet();
		B.PartitionVerticaly(Partitions, o);	
		Iterator Part= Partitions.iterator();
		boolean found=false;
		while (Part.hasNext() && !found)  {
			Set Partition = (Set)Part.next();
			if (Partition.size()>1)  {
				if (dbg) o.outln("Checking cycle for a partition");
				found = DetectCycle(A, Partition, o); 
			}
		}
		
		
		if (B.IndependentMessage(Partition))	
			Flag = true;
		
		Iterator I = getContinuations(B).iterator();
		boolean found = false;
		while (I.hasNext() && !found)  
			found = DetectCycle2Rec(A, (BasicMSC) I.next(), Partition, Flag, o);
		return found;
	}
*/




	
	
	public void Interleave(LTSOutput o) throws Exception  {
//		o.outln("ACA1 !!!!!");
//		return;
		
		CheckRegularLanguage(o);
		Map Postponed = new HashMap();
		Map VisitedAsA = new HashMap();
		Map VisitedAsB = new HashMap();
		int id=0;
		while (FindAndApplyRules(Postponed, VisitedAsA, VisitedAsB, id++, o)) {
//			CheckRegularLanguage(o);
		}
	}
	
	private boolean FindAndApplyRules(Map Postponed, Map VisitedAsA, Map VisitedAsB, int id, LTSOutput o) throws Exception {
		boolean dbg = false;
		if (dbg) o.outln("Finding and Applying rules");
		boolean appliedRule = true;
		int loops = 0;
		
/*		if (id >= 2)  {
			o.outln("Exceeded Limit!!!!!");
			return false;
		}
*/		
		Iterator I =  getbMSCs().iterator();
		appliedRule = false;
//		while (loops++ < 100 && I.hasNext() && !appliedRule) {
		if (dbg) o.outln("Starting cylce I");	
		while (I.hasNext() && !appliedRule) {
			BasicMSC A = (BasicMSC) I.next();		
			Set Cont = getContinuations(A);
			Iterator J1 =  Cont.iterator();
			while (J1.hasNext() && !appliedRule) {
				if (dbg) o.outln("About to apply rule");
				BasicMSC B = (BasicMSC) J1.next();
				if (A != B) {
					if (PreConditionSequence(A, B, Postponed))  {
						appliedRule = ApplySequenceRules(A, B, Postponed, VisitedAsA, VisitedAsB, id, o);
					}
				}
				else  {
					appliedRule = ApplyRuleLoop(A, Postponed, VisitedAsA, VisitedAsB, id, o);
				}
				if (dbg) o.outln("Finished applying rule");						
			}
			if (dbg) o.outln("Finished cylce J1");	
		}
		if (dbg) o.outln("Finished cylce I");	
//		if (loops >=100)  {
//			o.outln("Too many Loops!");
//			return false;
//		}
		return appliedRule;
	}

	private boolean PreConditionSequence(BasicMSC A, BasicMSC B, Map Postponed)  {
		if (getContinuations(A).contains(A))   //if A has a loop on itself
			if (Postponed.keySet().contains(A))   //and the loop has not been checked
				if (!((Set) Postponed.get(A)).contains(A))
					return false;					// return false;
		if (getContinuations(B).contains(B))  		//Same for B.
				if (Postponed.keySet().contains(B))
					if (!((Set) Postponed.get(B)).contains(B))
						return false;
		return true;
	}


	private boolean ApplyRuleLoop(BasicMSC A, Map Postponed, Map VisitedAsA, Map VisitedAsB, int Id, LTSOutput o) throws Exception {	
		boolean dbg = true;
		boolean dbg2 = true;

		if (!Postponed.keySet().contains(A))
			Postponed.put(A, new HashSet());		
		if (((Set)Postponed.get(A)).contains(A))  
			return false;

		if (dbg) o.outln("");		
		if (dbg) o.outln("ApplyRuleLoop" + A.name + ", " + A.name);
		
		StringSet Postponables = new StringSet();
		
		BasicMSC A_P = (BasicMSC) A.clone();
		A_P.name = "" + A.name + "_P" + Id;
		
		BasicMSC P_A =  (BasicMSC) A.clone();
		P_A.name = "P_" + A.name + "" + Id;			
		
		BasicMSC P =  new BasicMSC();
		P.copyComponents(A);
		P.name = "P" + Id;			

		boolean found = true;
		while (found)  {
			found = false;
			Set FirstMoves = P_A.getFirstMoves(o);			
			Set LastMoves = A_P.getLastMoves(o);
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
						P_A.addMessage(C1, label, C2);
						P.addMessage(C1, label, C2);
						Postponables.add(label);
						found = true;
					}
				}
			}
		}		
		

		//If Postponables is empty rule cannot be applied
		if (Postponables.size()==0)  {
			((Set) Postponed.get(A)).add(A);	
			return true; 
		}

		//If A_ is empty dont do anything then either A has  only one message or the language is not regular
		if (A_P.getFirstMoves(o).size() == 0)  {
			if (A.getFirstMoves(o).size() == 1)  {
				MarkAsPostponed(Postponed, A, A,o);
				return true;
			}
			else  {
				throw new Exception("Non regular language: Loop in bMSC " + A.name);
			}
		}
		
		
		//Apply rule

		BasicMSC P_A_P =  (BasicMSC) P.clone();
		P_A_P.append(A_P);
		P_A_P.name = "P_" + A.name + "_P" + Id;			
		
		//if (dbg) o.outln("// Add transitions '1'");
		Iterator I =  getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC X = (BasicMSC) I.next();
			if (X!=A)  {
				if (getContinuations(X).contains(A))  {
					if (dbg) o.outln("// Adding (1)" + X.name + " -> " + A_P.name);
					addRelation(X, A_P);
				} 
			} 	
		}			 
		
		//Checking for Init->A
		if (getContinuationsInit().contains(A)) {
			if (dbg) o.outln("// Adding Init -> " + A_P.name);
			addRelationInit(A_P);
		}

		if (dbg) o.outln("// Adding bMSCs " + P_A_P.name + ", " + A_P.name + ", " + P_A.name + ", " +  P.name);
		addbMSC(P_A_P);
		addbMSC(A_P);
		addbMSC(P_A);
		addbMSC(P);


		//if (dbg) o.outln("// Add transitions '2'");
		Iterator J =  getContinuations(A).iterator();
		while (J.hasNext()) {
			BasicMSC Aux = (BasicMSC) J.next();
			if (Aux != A)  {
				if (dbg) o.outln("// Adding " + P_A.name + " -> " + Aux.name);
				addRelation (P_A, Aux);
				if (dbg) o.outln("// Adding " + P.name + " -> " + Aux.name);
				addRelation (P, Aux);
			}
		}
			
		if (dbg) o.outln("// Adding " + P.name + " -> " + A.name);
		addRelation(P, A);				
		if (dbg) o.outln("// Adding " +A.name + " -> " + A_P.name);
		addRelation(A, A_P);
		if (dbg) o.outln("// Adding " +A_P.name + " -> " + P_A.name);
		addRelation(A_P, P_A);
		if (dbg) o.outln("// Adding " +P_A.name + " -> " + A.name);
		addRelation(P_A, A);
		if (dbg) o.outln("// Adding " +A_P.name + " -> " + P_A_P.name);
		addRelation(A_P, P_A_P);
		if (dbg) o.outln("// Adding " +P_A_P.name + " -> " + P_A_P.name);
		addRelation(P_A_P, P_A_P);
		if (dbg) o.outln("// Adding " +P_A_P.name + " -> " + P.name);
		addRelation(P_A_P, P);
		if (dbg) o.outln("// Adding " +P_A_P.name + " -> " + P_A.name);
		addRelation(P_A_P, P_A);
		if (dbg) o.outln("// Adding " +P_A.name + " -> " + A_P.name);
		addRelation(P_A, A_P);

			
		if (dbg) o.outln("Updating postponed map (" + A.name + "," +  A.name +")");
		((Set) Postponed.get(A)).add(A);						
		
		if (dbg) o.outln("Updating postponed map (" + A.name + "," +  A_P.name +")");
		((Set) Postponed.get(A)).add(A_P);						

		if (dbg) o.outln("Updating postponed map (" + P.name + "," +  A.name +")");
		Postponed.put(P, new HashSet());
		((Set) Postponed.get(P)).add(A);						
		
		if (dbg) o.outln("Updating postponed map (" + P_A.name + "," +  A.name +")");
		Postponed.put(P_A, new HashSet());
		((Set) Postponed.get(P_A)).add(A);						
						
		if (dbg) o.outln("Updating postponed map (" + A_P.name + "," +  P_A.name +")");
		Postponed.put(A_P, new HashSet());
		((Set) Postponed.get(A_P)).add(P_A);						
		
		if (dbg) o.outln("Updating postponed map (" + A_P.name + "," +  P_A_P.name +")");
		((Set) Postponed.get(A_P)).add(P_A_P);						

		if (dbg) o.outln("Updating postponed map (" + P_A_P.name + "," +  P_A_P.name +")");
		Postponed.put(P_A_P, new HashSet());
		((Set) Postponed.get(P_A_P)).add(P_A_P);						

		if (dbg) o.outln("Updating postponed map (" + P_A_P.name + "," +  P.name +")");
		((Set) Postponed.get(P_A_P)).add(P);						


		if (dbg) o.outln("Updating postponed map (" + P_A_P.name + "," +  P_A.name +")");
		((Set) Postponed.get(P_A_P)).add(P_A);						

		if (dbg) o.outln("Updating postponed map (" + P_A.name + "," +  A_P.name +")");
		((Set) Postponed.get(P_A)).add(A_P);						

		if (dbg) o.outln("Register that " + A.name +" has been visited as B that the result is " + P_A.name + ".");
		Vector v = new Vector(2);
		v.add(0, Postponables);
		v.add(1, P_A);
		if (!VisitedAsB.keySet().contains(A))
			VisitedAsB.put(A, new HashSet());
		((Set) VisitedAsB.get(A)).add(v);

		if (dbg) o.outln("Register that "+A.name+" has been visited as A and that the resutl is " + A_P.name);
		v = new Vector(2);
		v.add(0, Postponables);
		v.add(1, A_P);
//		v.add(2, null);
		if (!VisitedAsA.keySet().contains(A))
			VisitedAsA.put(A, new HashSet());
		((Set) VisitedAsA.get(A)).add(v);
				
		if (dbg2) o.outln("AppliedLoopRule (" + Id +") to " + A.name);

		return true;
		
	}
	private void MarkAsPostponed(Map Postponed, BasicMSC A, BasicMSC B, LTSOutput o)  {
		boolean dbg = true;
		if (dbg) o.outln("Marked as postponed ("+A.name+" -> " + B.name + ")");
		if (!Postponed.keySet().contains(A))
			Postponed.put(A, new HashSet());		
		((Set)Postponed.get(A)).add(B);
	}
	
	private boolean HasBeenPostponed(Map Postponed, BasicMSC A, BasicMSC B)  {
		if (!Postponed.keySet().contains(A)) 
			return false;
		else	
			return ((Set)Postponed.get(A)).contains(B);
	}
	
	
	
	private boolean ApplySequenceRules(BasicMSC A, BasicMSC B, Map Postponed, Map VisitedAsA, Map VisitedAsB, int Id, LTSOutput o) throws Exception {
		boolean dbg = false;
		
		if (dbg) o.outln("1");
		if (HasBeenPostponed(Postponed, A, B))
			return false;
		if (dbg) o.outln("2");
		
		if (getContinuations(B).contains(A))
			return ApplyRuleSequenceAndBack(A, B, Postponed, VisitedAsA, VisitedAsB, Id, o);
		else	
			return ApplyRuleSequence(A, B, Postponed, VisitedAsA, VisitedAsB, Id, o); 
	}	
	
/*	
	private boolean ApplyRuleSequence(BasicMSC A, BasicMSC B, Map Postponed, Map VisitedAsA, Map VisitedAsB, int Id, LTSOutput o) throws Exception {
		boolean dbg = true;

		if (dbg) o.outln("");
		if (dbg) o.outln("ApplyRuleSequence " + A.name + ", " + B.name);
		
		//Set Prev_P_A_P = new HashSet();
		//Iterator It_Prev_P_A_P;
		//Set Prev_P_B_P = new HashSet();
		//Iterator It_Prev_P_B_P;
				
				
		if (dbg) o.outln("Creating bMSCs A-P, P+B and P");				
		BasicMSC A_P = (BasicMSC) A.clone();
		A_P.name = "" + A.name + "_" + Id + "P";

		BasicMSC P = new BasicMSC();
		P.copyComponents(A);
		P.name = "P" + Id;

		BasicMSC P_B =  (BasicMSC) B.clone();
		P_B.name = "P" + Id + "_" + B.name;			

		if (dbg) o.outln("Moving messages from A-P to P+B");				
		boolean found = true;
		while (found)  {
			found = false;
			Set FirstMoves = P_B.getFirstMoves(o);
			Set LastMoves = A_P.getLastMoves(o);
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
						Vector Ids = A_P.getIdOfLastMessage(C1, label, C2);
						int Id1 = ((Integer)Ids.get(0)).intValue();
						int Id2 = ((Integer)Ids.get(1)).intValue();
						if (!B.hasEventId(Id1) && !B.hasEventId(Id2))   {
							if (dbg) o.outln("Found postponable action "+ label);
							A_P.removeLastMessage(C1, label, C2);
							P_B.addMessage(C1, label, C2, Id1, Id2);
							P.addMessage(C1, label, C2, Id1, Id2);
							found = true;
						}
					}
				}
			}
		}		
		
		if (P.empty())  {
			if (dbg) o.outln("No postponable messages");
			MarkAsPostponed(Postponed, A, B, o);	
			return true; 
		}


		if (dbg) o.outln("// Check if A has ever been used as A with exactly P. If so use the previous A-P");
		if (!VisitedAsA.keySet().contains(A))
			VisitedAsA.put(A, new HashSet());
		Iterator VA = ((Set) VisitedAsA.get(A)).iterator();
		while (VA.hasNext())  {
			Vector aux = (Vector) VA.next();
			//if (Postponables.hasSameElements((StringSet) aux.get(0)))  {
			if (P.isTheSameAs((BasicMSC) aux.get(0),o))  {
				A_P = (BasicMSC) aux.get(1);
				if (dbg) o.outln("Found that " + A.name + " has been used as A with P, so now using " + A_P.name + " as A_P");
				if (aux.size() == 3)  {
					Prev_P_A_P.add(aux.get(2));
					if (dbg) o.outln(" PREV In addition will remember to link Prev_P_A_P=" + ((BasicMSC)aux.get(2)).name + " to link with anything A_P is linked with"); 
				}
			}
		}

		if (dbg) o.outln("// Check if B has ever been used as B with exactly P. If so use the previous P_B and and dont add from and to P_B");
		if (!VisitedAsB.keySet().contains(B))
			VisitedAsB.put(B, new HashSet());
		Iterator V = ((Set) VisitedAsB.get(B)).iterator();
		boolean Circular = false;
		while (V.hasNext())  {
			Vector aux = (Vector) V.next();
			//if (Postponables.hasSameElements((StringSet) aux.get(0)))  {
			if (P.isTheSameAs((BasicMSC) aux.get(0),o))  {
				Circular = true;
				P_B = (BasicMSC) aux.get(1);
				if (dbg) o.outln("Found that " + B.name + " has been visited as B with exactly P, so now using " + P_B.name + " as previous P_B");
				if (aux.size() == 3)  {
					Prev_P_B_P.add(aux.get(2));
					if (dbg) o.outln(" PREV In addition will remember to link Prev_P_B_P=" + ((BasicMSC)aux.get(2)).name + " FROM anything that is liked to P_B"); 
				}
			}
		}

// If A-P is empty don't use it.
//		if (A_P.getFirstMoves(o).size() == 0)  {
//			A_P = P_B;
//		}				
		
		if (dbg) o.outln("Add transitions '3'");
		Iterator I =  getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC X = (BasicMSC) I.next();
			if (X != B && X != A && X != A_P && X != P_B)  { //&& X != P
				if (getContinuations(X).contains(A))  {
					if (dbg) o.outln("// Adding " + X.name + " -> " + A_P.name);
					addRelation(X, A_P);
					if (HasBeenPostponed(Postponed, X, A))
						MarkAsPostponed(Postponed, X, A_P, o);
				} 
			} 	
		}			

		//Checking for Init->A
		if (getContinuationsInit().contains(A))  {
			if (dbg) o.outln("// Adding Init -> " + A_P.name);
			addRelationInit(A_P);
		}


		if (dbg) o.outln("// Adding bMSC " + A_P.name);
		addbMSC(A_P);
//		if (!Circular)  {
//			if (A_P != P_B)   {
				if (dbg) o.outln("// Adding bMSCs " + P_B.name);
				addbMSC(P_B);
//			}
//		}

		if (dbg) o.outln("Add transitions '1'");
		if (getContinuations(A).contains(A))  {
			if (dbg) o.outln("// Add relation " + A.name + " -> " + A_P.name);
			addRelation (A, A_P);
			MarkAsPostponed(Postponed,A,A_P,o);  // No hace flata hace A->A-P porque ya A->A fue hecho...
			
		}

		if (dbg) o.outln("Add transitions '2'");
		if (!Circular)  { //Si es circular entonces ya fueron agregadas estas continuaciones.
			if (getContinuations(B).contains(B))  {
				if (dbg) o.outln("// Add relation " + P_B.name + " -> " + B.name);
				addRelation(P_B, B);
				//MarkAsPostponed(Postponed, P_B, B, o); 
			}
			
			if (dbg) o.outln("// Add transitions '6'");
			Iterator J =  getContinuations(B).iterator();
			while (J.hasNext()) {
				BasicMSC Aux = (BasicMSC) J.next();
				if (Aux != B && Aux!=A && Aux != A_P && Aux != P_B)  { //  && Aux != P
					if (dbg) o.outln("// Add relation " + P_B.name + " -> " + Aux.name);
					addRelation (P_B, Aux);			
					if (HasBeenPostponed(Postponed, B, Aux))
						MarkAsPostponed(Postponed, P_B, Aux, o);
				}
			}
		}


		if (Circular)  {
			if (VisitedAsA.keySet().contains(P_B)) {
				if (dbg) o.outln(P_B.name + " has been visited as A...so will add A_P to P_B-X and Prev_P_A_P to P_B-X");
				Iterator VV = ((Set) VisitedAsA.get(P_B)).iterator();
				while (VV.hasNext())  {
					Vector aux = (Vector) VV.next();
					if (dbg) o.outln("Adding relation " + A_P.name + " to " + ((BasicMSC)aux.get(1)).name);

					addRelation(A_P, (BasicMSC) aux.get(1));
					MarkAsPostponed(Postponed, A_P, (BasicMSC) aux.get(1),o);

					It_Prev_P_A_P = Prev_P_A_P.iterator();
					while (It_Prev_P_A_P.hasNext())  {
						BasicMSC PAP = (BasicMSC) It_Prev_P_A_P.next();
						if (dbg) o.outln("PREV Adding relation " + PAP.name + " to " + ((BasicMSC)aux.get(1)).name);
						addRelation(PAP, (BasicMSC) aux.get(1));
						MarkAsPostponed(Postponed, PAP, (BasicMSC) aux.get(1),o);
					}
				}
			}
			else
				if (dbg) o.outln(P_B.name + " has not been visited as A ");
		}
	
		if (Circular && A == P_B)  { 
			if (dbg) o.outln("Because Circular and A=P_B, adding relation " + A_P.name + " to " + A_P.name);
			addRelation(A_P, A_P);
			throw new Exception("Case that should never happen");
		}

		if (dbg) o.outln("Adding relation " + A_P.name + " to " + P_B.name);
		addRelation(A_P, P_B);
		MarkAsPostponed(Postponed, A_P, P_B,o);

		It_Prev_P_B_P = Prev_P_B_P.iterator();
		while (It_Prev_P_B_P.hasNext())  {
			BasicMSC PBP = (BasicMSC) It_Prev_P_B_P.next();
			if (dbg) o.outln("Adding PREV relation " + A_P.name + " to " + PBP.name);
			addRelation(A_P, PBP);
			MarkAsPostponed(Postponed, A_P, PBP, o);
		} 
		


		It_Prev_P_A_P = Prev_P_A_P.iterator();
		while (It_Prev_P_A_P.hasNext())  {
			BasicMSC PAP = (BasicMSC) It_Prev_P_A_P.next();
			if (dbg) o.outln("Adding PREV relation " + PAP.name + " to " + P_B.name);
			addRelation(PAP, P_B);
			MarkAsPostponed(Postponed, PAP, P_B, o);
			
			It_Prev_P_B_P = Prev_P_B_P.iterator();
			while (It_Prev_P_B_P.hasNext())  {
				BasicMSC PBP = (BasicMSC) It_Prev_P_B_P.next();
				if (dbg) o.outln("Adding PREV relation " + PAP.name + " to " + PBP.name);
				addRelation(PAP, PBP);
				MarkAsPostponed(Postponed, PAP, PBP, o);
			}			
		} 
		
			
		MarkAsPostponed(Postponed, A, B,o);
			

		//Register that B has been extended with P and that the result is P_B.
		if (!Circular)  {
			if (dbg) o.outln("Register that " + B.name +" has been visited as B that the result is " + P_B.name + ".");
			Vector v = new Vector(2);
			v.add(0, P);
			v.add(1, P_B);
			((Set) VisitedAsB.get(B)).add(v);
		}
		// Register that A has been extended 
		if (dbg) o.outln("Register that " + A.name +" has been visited as A that the result is " + A_P.name + ".");
		Vector v = new Vector(2);
		v.add(0, P);
		v.add(1, A_P);
		
		if (!VisitedAsA.keySet().contains(A))
			VisitedAsA.put(A, new HashSet());
		((Set) VisitedAsA.get(A)).add(v);
		
		if (dbg2) o.outln("AppliedRuleSequence (" + Id +") " + A.name + ", " + B.name);
			
		if (P_B.getPostponables(P_B,o).isTheSameAs(P, o)) {
			if (dbg) o.outln("Independent!");
			MarkAsPostponed(Postponed, P_B, B, o);
		
			BasicMSC BClone = (BasicMSC) B.clone();
			BClone.name = B.name+"clone"+Id;
			addbMSC(BClone);
			addbMSC(P);
			addRelation(A_P, BClone);
			MarkAsPostponed(Postponed, A_P, BClone, o);
			It_Prev_P_A_P = Prev_P_A_P.iterator();
			while (It_Prev_P_A_P.hasNext())  {
				BasicMSC PAP = (BasicMSC) It_Prev_P_A_P.next();
				if (dbg) o.outln("PREV ! ");
				addRelation(PAP, BClone);
				MarkAsPostponed(Postponed, PAP, BClone, o);
			}
			
			addRelation(BClone, BClone);			
			MarkAsPostponed(Postponed, BClone, BClone, o);
			addRelation(BClone, P);
			MarkAsPostponed(Postponed, BClone, P, o);
			addRelation(P, B);
			MarkAsPostponed(Postponed, P, B, o);


			if (dbg) o.outln("// Add transitions '6'");
			Iterator J =  getContinuations(B).iterator();
			while (J.hasNext()) {
				BasicMSC Aux = (BasicMSC) J.next();
				if (Aux != B && Aux!=A && Aux != A_P && Aux != P_B && Aux != P && Aux != BClone)  {
					if (dbg) o.outln("// Add relation " + P.name + " -> " + Aux.name);
					addRelation (P, Aux);
				}
			}
		}
		else
			if (dbg) o.outln("Not Independent!");
			
			
			
		return true;
	}


	private boolean ApplyRuleSequenceAndBack(BasicMSC A, BasicMSC B, Map Postponed, Map VisitedAsA, Map VisitedAsB, int Id, LTSOutput o) throws Exception {
		boolean dbg = true;
		boolean dbg2 = true;
		if (dbg) o.outln("");
		if (dbg) o.outln("ApplyRuleSequenceAndBack " + A.name + ", " + B.name);
		boolean CircularA = false, CircularB = false;
		
		StringSet PostponablesA = new StringSet();

		Set Prev_P_A_P = new HashSet();
		Set Prev_P_B_P = new HashSet();
		Iterator It_Prev_P_A_P;
		Iterator It_Prev_P_B_P;
		

		Set PrevB_P_A_P = new HashSet();
		Set PrevB_P_B_P = new HashSet();
		Iterator It_PrevB_P_A_P;
		Iterator It_PrevB_P_B_P;


		BasicMSC A_P = (BasicMSC) A.clone();
		A_P.name = "" + A.name + "_P" + Id;

		BasicMSC P_B =  (BasicMSC) B.clone();
		P_B.name = "P_" + B.name + "" + Id;			

		BasicMSC P_B_P =  new BasicMSC();
		P_B_P.copyComponents(B);
		
		BasicMSC PA =  new BasicMSC();
		PA.copyComponents(A);

		{  // Find postponables from A to B
			boolean found = true;
			while (found)  {
				found = false;
				Set FirstMoves = P_B.getFirstMoves(o);
				Set LastMoves = A_P.getLastMoves(o);
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
							if (dbg) o.outln("1-Found postponableA action "+ label);
							Vector Ids = A_P.getIdOfLastMessage(C1, label, C2);
							int Id1 = ((Integer)Ids.get(0)).intValue();
							int Id2 = ((Integer)Ids.get(1)).intValue();
							if (!B.hasEventId(Id1) && !B.hasEventId(Id2))   {
								A_P.removeLastMessage(C1, label, C2);
								P_B.addMessage(C1, label, C2, Id1, Id2);
								P_B_P.addMessage(C1, label, C2, Id1, Id2);
								PA.addMessage(C1, label, C2, Id1, Id2);
								PostponablesA.add(label);
								found = true;
							}
						}
					}
				}
			}				
		}

		StringSet PostponablesB = new StringSet();

		BasicMSC B_P = (BasicMSC) B.clone();
		B_P.name = "" + B.name + "_P" + Id;

		BasicMSC P_A =  (BasicMSC) A.clone();
		P_A.name = "P_" + A.name + "" + Id;			

		BasicMSC P_A_P =  new BasicMSC();
		P_A_P.copyComponents(A);

		BasicMSC PB =  new BasicMSC();
		PB.copyComponents(B);
		
		{   //Find postponables from B to A
			boolean found = true;
			while (found)  {
				found = false;
				Set FirstMoves = P_A.getFirstMoves(o);
				Set LastMoves = B_P.getLastMoves(o);
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
							if (dbg) o.outln("2-Found postponableB action "+ label);
							Vector Ids = B_P.getIdOfLastMessage(C1, label, C2);
							int Id1 = ((Integer)Ids.get(0)).intValue();
							int Id2 = ((Integer)Ids.get(1)).intValue();
							if (!A.hasEventId(Id1) && !A.hasEventId(Id2))   {
								B_P.removeLastMessage(C1, label, C2);
								P_A.addMessage(C1, label, C2, Id1, Id2);
								P_A_P.addMessage(C1, label, C2, Id1, Id2);
								PB.addMessage(C1, label, C2, Id1, Id2);
								PostponablesB.add(label);
								found = true;
							}
						}
					}
				}
			}		
		}
		
		//if (dbg) o.outln("//If Postponables is empty rule cannot be applied");
		if (PostponablesB.size()==0 && PostponablesA.size()==0)  {
			MarkAsPostponed(Postponed, A, B,o);	
			MarkAsPostponed(Postponed, B, A,o);	
			return true; 
		}
		
		//if (dbg) o.outln("// Check if A has ever been used as A with exactly P. If so use the previous A-P");
		if (!VisitedAsA.keySet().contains(A))
			VisitedAsA.put(A, new HashSet());
		Iterator VA = ((Set) VisitedAsA.get(A)).iterator();
		while (VA.hasNext())  {
			Vector aux = (Vector) VA.next();
			//if (Postponables.hasSameElements((StringSet) aux.get(0)))  {
			if (PA.isTheSameAs((BasicMSC) aux.get(0),o))  {
				A_P = (BasicMSC) aux.get(1);
				if (dbg) o.outln("Found that " + A.name + " has been used as A with P, so using " + A_P.name + " as A_P. Size("+aux.size()+")");
				if (aux.size() == 3)  {
					Prev_P_A_P.add(aux.get(2));
					if (dbg) o.outln("PREV remembering Prev_P_A_P=" + ((BasicMSC) aux.get(2)).name + " in order to link with all things A_P is linked with");
				}
			}
		}
		
		//if (dbg) o.outln("// Check if B has ever been used as A with exactly P. If so use the previous B-P");
		if (!VisitedAsA.keySet().contains(B))
			VisitedAsA.put(B, new HashSet());
		Iterator VB = ((Set) VisitedAsA.get(B)).iterator();
		while (VB.hasNext())  {
			Vector aux = (Vector) VB.next();
			//if (Postponables.hasSameElements((StringSet) aux.get(0)))  {
			if (PB.isTheSameAs((BasicMSC) aux.get(0),o))  {
				B_P = (BasicMSC) aux.get(1);
				if (dbg) o.outln("Found that " + B.name + " has been used as A with exactly P, so using " + B_P.name + " as B_P. Size("+aux.size()+")");
				if (aux.size() == 3)  {
					Prev_P_B_P.add(aux.get(2));
					if (dbg) o.outln("PREV remembering Prev_P_B_P=" + ((BasicMSC) aux.get(2)).name + " in order to link with all the things B_P is linked with");
				} 
			}
		}
		
		
		if (PostponablesA.size()!=0)  {
			if (dbg) o.outln("3-PostponablesA is not empty");
			// Check if B has ever used as B  with exactly P. If so use the previous P_B and and dont add from and to P_B
			if (!VisitedAsB.keySet().contains(B))
				VisitedAsB.put(B, new HashSet());
			Iterator V = ((Set) VisitedAsB.get(B)).iterator();
			CircularB = false;
			while (V.hasNext() && !CircularB)  {
				Vector aux = (Vector) V.next();
				if (PA.isTheSameAs((BasicMSC) aux.get(0),o))  {
					if (dbg) o.outln("4-Circular!!");
					P_B = (BasicMSC) aux.get(1);
					CircularB = true;
					if (dbg) o.outln("Found that " + B.name + " has been visited as B with exactly P, so now using " + P_B.name + " as previous P_B");
					if (aux.size() == 3)  {
						PrevB_P_B_P.add(aux.get(2));
						if (dbg) o.outln(" PREV In addition will remember to link PrevB_P_B_P=" + ((BasicMSC)aux.get(2)).name + " FROM anything that is liked to P_B"); 
					}			
				}
			}
			
			if (dbg) o.outln("5-Add transitions '3'");
			Iterator I =  getbMSCs().iterator();
			while (I.hasNext()) {
				BasicMSC X = (BasicMSC) I.next();
				if (X != B && X != A && X != A_P && X != B_P && X != P_B && X != P_A)  {
					if (getContinuations(X).contains(A))  {
						if (dbg) o.outln("6-Adding " + X.name + " -> " + A_P.name);
						addRelation(X, A_P);
						if (HasBeenPostponed(Postponed, X, A))
							MarkAsPostponed(Postponed, X, A_P, o);						
					} 
				} 	
			}			
	
			//Checking for Init->A
			if (getContinuationsInit().contains(A))  {
				if (dbg) o.outln("7-Adding Init -> " + A_P.name);
				addRelationInit(A_P);
			}
			
			if (dbg) o.outln("8-Adding bMSC " + A_P.name);
			addbMSC(A_P);
			if (!CircularB)  {
	//			if (A_P != P_B)   {
					if (dbg) o.outln("9-Adding bMSC " + P_B.name);
					addbMSC(P_B);
	//			}
			}

			if (dbg) o.outln("10-Add relation " + B.name + " -> " + A_P.name);
			addRelation (B, A_P);
			MarkAsPostponed(Postponed,B, A_P,o);

			//if (dbg) o.outln("Add transitions '1'");
			if (getContinuations(A).contains(A))  {
				if (dbg) o.outln("11-Add relation " + A.name + " -> " + A_P.name);
				addRelation (A, A_P);
				MarkAsPostponed(Postponed, A, A_P,o);
			}

			if (getContinuations(B).contains(B))  {
				if (dbg) o.outln("12-Add relation " + P_B.name + " -> " + B.name);
				addRelation(P_B, B);
				//MarkAsPostponed(Postponed, P_B, B,o);
				
			}
			if (dbg) o.outln("13-Add relation " + P_B.name + " -> " + A.name);
			addRelation (P_B, A);
			if (!CircularB)
				MarkAsPostponed(Postponed, P_B, A,o);		
			
			if (!CircularB)  {
				if (dbg) o.outln("14-Add transitions '6'");
				Iterator J =  getContinuations(B).iterator();
				while (J.hasNext()) {
					BasicMSC Aux = (BasicMSC) J.next();
					if (Aux != B && Aux!=A && Aux != A_P && Aux != B_P && Aux != P_B && Aux != P_A)  {
						if (dbg) o.outln("15-Add relation " + P_B.name + " -> " + Aux.name);
						addRelation (P_B, Aux);
						if (HasBeenPostponed(Postponed, B, Aux))  
							MarkAsPostponed(Postponed, P_B, Aux, o);
					}
				}
			}

			if (CircularB)  {
				if (VisitedAsA.keySet().contains(P_B)) {
					Iterator VV = ((Set) VisitedAsA.get(P_B)).iterator();
					while (VV.hasNext())  {
						Vector aux = (Vector) VV.next();
						//if (PostponablesA.hasSameElements((StringSet) aux.get(0)))  {
						//if (PA.isTheSameAs((BasicMSC) aux.get(0),o))  {
							if (dbg) o.outln("16-Adding relation " + A_P.name + " to " + ((BasicMSC)aux.get(1)).name);
							addRelation(A_P, (BasicMSC) aux.get(1));
							MarkAsPostponed(Postponed, A_P, (BasicMSC) aux.get(1),o);
							
							//Why dont I add PAP -> Aux.get(1)?
							It_Prev_P_A_P = Prev_P_A_P.iterator();
							while (It_Prev_P_A_P.hasNext())  {
								BasicMSC PAP = (BasicMSC) It_Prev_P_A_P.next();
								if (dbg) o.outln("16,5-Adding PREV relation " + PAP.name + " to " + ((BasicMSC) aux.get(1)).name);
								addRelation(PAP, (BasicMSC) aux.get(1));
								MarkAsPostponed(Postponed, PAP, (BasicMSC) aux.get(1), o);	
							}

							
						//}
					}
				}
			}

			if (dbg) o.outln("17-Adding relation " + A_P.name + " to " + P_B.name);
			addRelation(A_P, P_B);	
			
			It_PrevB_P_B_P = PrevB_P_B_P.iterator();
			while (It_PrevB_P_B_P.hasNext())  {
				BasicMSC PBP = (BasicMSC) It_PrevB_P_B_P.next();
				if (dbg) o.outln("Adding PREV relation " + A_P.name + " to " + PBP.name);
				addRelation(A_P, PBP);
				MarkAsPostponed(Postponed, A_P, PBP, o);
			} 
		
			
			MarkAsPostponed(Postponed, A_P, P_B, o);
			It_Prev_P_A_P = Prev_P_A_P.iterator();
			while (It_Prev_P_A_P.hasNext())  {
				BasicMSC PAP = (BasicMSC) It_Prev_P_A_P.next();
				if (dbg) o.outln("17,5-Adding PREV relation " + PAP.name + " to " + P_B.name);
				addRelation(PAP, P_B);
				MarkAsPostponed(Postponed, PAP, P_B, o);	
		
				It_PrevB_P_B_P = PrevB_P_B_P.iterator();
				while (It_PrevB_P_B_P.hasNext())  {
					BasicMSC PBP = (BasicMSC) It_PrevB_P_B_P.next();
					if (dbg) o.outln("Adding PREV relation " + PAP.name + " to " + PBP.name);
					addRelation(PAP, PBP);
					MarkAsPostponed(Postponed, PAP, PBP, o);
				} 
			}
			
			
			if (dbg) o.outln("18-Adding relation " + P_B.name + " to " + A_P.name);
			addRelation(P_B, A_P);
			MarkAsPostponed(Postponed, P_B, A_P, o);


		}


		if (PostponablesB.size()!=0)  {
			if (dbg) o.outln("21-PostponablesB is not empty");		
			// Check if A has ever been used as B with exactly P. If so use the previous P_A and and dont add from and to P_A
			if (!VisitedAsB.keySet().contains(A))
				VisitedAsB.put(A, new HashSet());
			Iterator V = ((Set) VisitedAsB.get(A)).iterator();
			CircularA = false;
			while (V.hasNext() && !CircularA)  {
				Vector aux = (Vector) V.next();
				//if (PostponablesB.hasSameElements((StringSet) aux.get(0)))  {
				if (PB.isTheSameAs((BasicMSC) aux.get(0),o))  {
					P_A = (BasicMSC) aux.get(1);
					CircularA = true;
					if (dbg) o.outln("22-Circular!!");
					
					if (dbg) o.outln("Found that " + A.name + " has been visited as B with exactly P, so now using " + P_A.name + " as previous P_A");
					if (aux.size() == 3)  {
						PrevB_P_A_P.add(aux.get(2));
						if (dbg) o.outln(" PREV In addition will remember to link PrevB_P_A_P=" + ((BasicMSC)aux.get(2)).name + " FROM anything that is liked to P_A"); 
					}
				}
			}

			//if (dbg) o.outln("Add transitions '5'");
			Iterator I =  getbMSCs().iterator();
			while (I.hasNext()) {
				BasicMSC X = (BasicMSC) I.next();
				if (X != B && X != A && X != A_P && X != B_P && X != P_B && X != P_A)  {					if (getContinuations(X).contains(B))  {
						if (dbg) o.outln("23-Adding " + X.name + " -> " + B_P.name);
						addRelation(X, B_P);
						if (HasBeenPostponed(Postponed, X, B))
							MarkAsPostponed(Postponed, X, B_P, o);						
					} 
				} 	
			}			
	
			//Checking for Init->B
			if (getContinuationsInit().contains(B))  {
				if (dbg) o.outln("24-Adding Init -> " + B_P.name);
				addRelationInit(B_P);
			}

			if (dbg) o.outln("25-Adding bMSC " + B_P.name);
			addbMSC(B_P);

			if (!CircularA)  {
	//			if (A_P != P_B)   {
					if (dbg) o.outln("26-Adding bMSC " + P_A.name);
					addbMSC(P_A);
	//			}
			}

			if (dbg) o.outln("27- Add relation " + A.name + " -> " + B_P.name);
			addRelation (A, B_P);
			MarkAsPostponed(Postponed, A, B_P,o);
	
			//if (dbg) o.outln("Add transitions '2'");
			if (getContinuations(B).contains(B))  {
				if (dbg) o.outln("28- Add relation " + B.name + " -> " + B_P.name);
				addRelation (B, B_P);
				MarkAsPostponed(Postponed, B, B_P,o);
			}		

			//if (dbg) o.outln("Add transitions '2'");
			if (getContinuations(A).contains(A))  {
				if (dbg) o.outln("29- Add relation " + P_A.name + " -> " + A.name);
				addRelation(P_A, A);
				//MarkAsPostponed(Postponed, P_A, A,o);		
			}
			if (dbg) o.outln("30- Add relation " + P_A.name + " -> " + B.name);
			addRelation (P_A, B);
			if (!CircularA)
				MarkAsPostponed(Postponed, P_A, B,o);		

			if (!CircularA)  {
				if (dbg) o.outln("31- Add transitions '4'");
				Iterator J =  getContinuations(A).iterator();
				while (J.hasNext()) {
					BasicMSC Aux = (BasicMSC) J.next();
					if (Aux != B && Aux!=A && Aux != A_P && Aux != B_P && Aux != P_B && Aux != P_A)  {
						if (dbg) o.outln("32- Add relation " + P_A.name + " -> " + Aux.name);
						addRelation (P_A, Aux);
					//	if (HasBeenPostponed(Postponed, A, Aux))  {
					//		MarkAsPostponed(Postponed, P_A, Aux, o);
					}
				}			
			}
			
			
			if (CircularA)  {
				if (VisitedAsA.keySet().contains(P_A)) {
					Iterator VV = ((Set) VisitedAsA.get(P_A)).iterator();
					while (VV.hasNext())  {
						Vector aux = (Vector) VV.next();
						//if (PostponablesB.hasSameElements((StringSet) aux.get(0)))  {
						//if (PB.isTheSameAs((BasicMSC) aux.get(0),o))  {
							if (dbg) o.outln("33-Adding relation " + B_P.name + " to " + ((BasicMSC)aux.get(1)).name);
							addRelation(B_P, (BasicMSC) aux.get(1));
							MarkAsPostponed(Postponed, B_P, (BasicMSC) aux.get(1),o);
							
							
							//Why dont I add PBP to aux.get?
							It_Prev_P_B_P = Prev_P_B_P.iterator();
							while (It_Prev_P_B_P.hasNext())  {
								BasicMSC PBP = (BasicMSC) It_Prev_P_B_P.next();
								if (dbg) o.outln("33,5-Adding PREV relation " + PBP.name + " to " + ((BasicMSC) aux.get(1)).name);
								addRelation(PBP, (BasicMSC) aux.get(1));
								MarkAsPostponed(Postponed, PBP, (BasicMSC) aux.get(1), o);
							}
						//}
					}
				}
			}
			
			if (dbg) o.outln("34-Adding relation " + B_P.name + " to " + P_A.name);
			addRelation(B_P, P_A);
			MarkAsPostponed(Postponed, B_P, P_A, o);
			
			It_PrevB_P_A_P = PrevB_P_A_P.iterator();
			while (It_PrevB_P_A_P.hasNext())  {
				BasicMSC PAP = (BasicMSC) It_PrevB_P_A_P.next();
				if (dbg) o.outln("Adding PREV relation " + B_P.name + " to " + PAP.name);
				addRelation(B_P, PAP);
				MarkAsPostponed(Postponed, B_P, PAP, o);
			} 
				

			It_Prev_P_B_P = Prev_P_B_P.iterator();
			while (It_Prev_P_B_P.hasNext())  {
				BasicMSC PBP = (BasicMSC) It_Prev_P_B_P.next();
				if (dbg) o.outln("34-Adding PREV relation " + PBP.name + " to " + P_A.name);
				addRelation(PBP, P_A);
				MarkAsPostponed(Postponed, PBP, P_A, o);
			
				It_PrevB_P_A_P = PrevB_P_A_P.iterator();
				while (It_PrevB_P_A_P.hasNext())  {
					BasicMSC PAP = (BasicMSC) It_PrevB_P_A_P.next();
					if (dbg) o.outln("Adding PREV relation " + PBP.name + " to " + PAP.name);
					addRelation(PBP, PAP);
					MarkAsPostponed(Postponed, PBP, PAP, o);
				} 
			}

			if (dbg) o.outln("35-Adding relation " + P_A.name + " to " + B_P.name);
			addRelation(P_A, B_P);
			MarkAsPostponed(Postponed, P_A, B_P, o);

		}
		
		
		//Register that A has been extended with P and that the result is P_A.
		if (dbg) o.outln("36-Register that " + A.name +" has been visited as B that the result is " + P_A.name + ".");
		Vector v = new Vector(2);
		v.add(0, PB);
		v.add(1, P_A);
		if (PostponablesB.size()!=0 && PostponablesA.size()!=0)  {
			v.ensureCapacity(3);
			v.add(2, P_A_P);
			if (dbg) o.outln("Adding PREV not null !!! Name :" + P_A_P.name);
		}
		if (!VisitedAsB.keySet().contains(A))
			VisitedAsB.put(A, new HashSet());
		((Set) VisitedAsB.get(A)).add(v);



		//Register that B has been extended with P and that the result is P_B.
		if (dbg) o.outln("19-Register that " + B.name +" has been visited as B that the result is " + P_B.name + ".");
		v = new Vector(2);
		v.add(0, PA);
		v.add(1, P_B);
		if (PostponablesB.size()!=0 && PostponablesA.size()!=0)  {
			v.ensureCapacity(3);
			v.add(2, P_B_P);
			if (dbg) o.outln("Adding PREV not null !!! Name :" + P_B_P.name);
		}
		if (!VisitedAsB.keySet().contains(B))
			VisitedAsB.put(B, new HashSet());
		((Set) VisitedAsB.get(B)).add(v);			

			
		// Register that B has been extended 
		if (dbg) o.outln("37-Register that " + B.name +" has been visited as A that the result is " + B_P.name + ".");
		v = new Vector(2);
		v.add(0, PB);
		v.add(1, B_P);
		if (PostponablesB.size()!=0 && PostponablesA.size()!=0)  {
			v.ensureCapacity(3);
			v.add(2, P_B_P);
			if (dbg && P_B_P == null) o.outln("Adding PREV null !!!");
			if (dbg) o.outln("Adding PREV not null !!! Name :" + P_B_P.name);
		}
		if (!VisitedAsA.keySet().contains(B))
			VisitedAsA.put(B, new HashSet());
		((Set) VisitedAsA.get(B)).add(v);

		
		// Register that A has been extended 
		if (dbg) o.outln("20-Register that " + A.name +" has been visited as A that the result is " + A_P.name + ".");
		v = new Vector(2);
		//v.add(0, PostponablesA);
		v.add(0, PA);
		v.add(1, A_P);
		if (PostponablesB.size()!=0 && PostponablesA.size()!=0)  {
			v.ensureCapacity(3);
			v.add(2, P_A_P);
			if (dbg && P_A_P == null) o.outln("Adding PREV null !!!");
			if (dbg) o.outln("Adding PREV not null !!! Name: " + P_A_P.name);			
		}
		if (!VisitedAsA.keySet().contains(A))
			VisitedAsA.put(A, new HashSet());
		((Set) VisitedAsA.get(A)).add(v);				
			




		// If A-P is empty don't use it.
		// if (A_P.getFirstMoves(o).size() == 0)  {
		// A_P = P_B;
		// }				
		


		if (PostponablesB.size()!=0 && PostponablesA.size()!=0)  {
		
			P_A_P.append(A_P);
			P_A_P.name = "P_" + A.name + "_P" + Id;			

			P_B_P.append(B_P);
			P_B_P.name = "P_" + B.name + "_P" + Id;			
			
		
			if (dbg) o.outln("38- Adding bMSC " + P_A_P.name);
			addbMSC(P_A_P);
			if (dbg) o.outln("39- Adding bMSC " + P_B_P.name);
			addbMSC(P_B_P);
			
			
			if (dbg) o.outln("40- Add relation " + A_P.name + " -> " + P_B_P.name);
			addRelation (A_P, P_B_P);
			MarkAsPostponed(Postponed, A_P, P_B_P,o);	
			
			It_Prev_P_A_P = Prev_P_A_P.iterator();
			while(It_Prev_P_A_P.hasNext())  {
				BasicMSC PAP= (BasicMSC) It_Prev_P_A_P.next();
				if (dbg) o.outln("40- Add PREV relation " + PAP.name + " -> " + P_B_P.name);
				addRelation (PAP, P_B_P);
				MarkAsPostponed(Postponed, PAP, P_B_P,o);		
				
				Iterator It_Cont_PAP = getContinuations(PAP).iterator();
				while (It_Cont_PAP.hasNext()) {
					BasicMSC Cont_PAP = (BasicMSC) It_Cont_PAP.next();
					if (dbg) o.outln("40,5- Add PREV relation " + P_A_P.name + " -> " + Cont_PAP.name);
					addRelation(P_A_P, Cont_PAP);
					MarkAsPostponed(Postponed, P_A_P, Cont_PAP, o);		
				}
				
			}
			
			
			if (dbg) o.outln("41- Add relation " + B_P.name + " -> " + P_A_P.name);
			addRelation (B_P, P_A_P);
			MarkAsPostponed(Postponed, B_P, P_A_P,o);		
			
			It_Prev_P_B_P = Prev_P_B_P.iterator();			
			while (It_Prev_P_B_P.hasNext())  {
				BasicMSC PBP = (BasicMSC) It_Prev_P_B_P.next();
				if (dbg) o.outln("41- Add PREV relation " + PBP.name + " -> " + P_A_P.name);
				addRelation (PBP, P_A_P);
				MarkAsPostponed(Postponed, PBP, P_A_P,o);		
				
				Iterator It_Cont_PBP = getContinuations(PBP).iterator();
				while (It_Cont_PBP.hasNext()) {
					BasicMSC Cont_PBP = (BasicMSC) It_Cont_PBP.next();
					if (dbg) o.outln("41,5- Add PREV relation " + P_B_P.name + " -> " + Cont_PBP.name);			
					addRelation(P_B_P, Cont_PBP);
					MarkAsPostponed(Postponed, P_B_P, Cont_PBP, o);		
				}
				
			}
			
			if (dbg) o.outln("42-Adding relation " + P_A_P.name + " to " + P_B_P.name);
			addRelation(P_A_P, P_B_P);
			if (dbg) o.outln("43-Adding relation " + P_A_P.name + " to " + P_B.name);
			addRelation(P_A_P, P_B);
			It_PrevB_P_B_P = PrevB_P_B_P.iterator();
			while (It_PrevB_P_B_P.hasNext())  {
				BasicMSC PBP = (BasicMSC) It_PrevB_P_B_P.next();
				if (dbg) o.outln("Adding PREV relation " + P_A_P.name + " to " + PBP.name);
				addRelation(P_A_P, PBP);
				MarkAsPostponed(Postponed, P_A_P, PBP, o);
			} 
	
			
			
	
			if (dbg) o.outln("44-Adding relation " + P_B_P.name + " to " + P_A_P.name);
			addRelation(P_B_P, P_A_P);
			if (dbg) o.outln("45-Adding relation " + P_B_P.name + " to " + P_A.name);
			addRelation(P_B_P, P_A);
			It_PrevB_P_A_P = PrevB_P_A_P.iterator();
			while (It_PrevB_P_A_P.hasNext())  {
				BasicMSC PAP = (BasicMSC) It_PrevB_P_A_P.next();
				if (dbg) o.outln("Adding PREV relation " + P_B_P.name + " to " + PAP.name);
				addRelation(P_B_P, PAP);
				MarkAsPostponed(Postponed, P_B_P, PAP, o);
			} 
	
	
			MarkAsPostponed(Postponed, P_A_P, P_B_P,o);
			MarkAsPostponed(Postponed, P_A_P, P_B,o);
			
			MarkAsPostponed(Postponed, P_B_P, P_A_P,o);
			MarkAsPostponed(Postponed, P_B_P, P_A,o);			
		}


		if (CircularA && B == P_A)  { 
			if (dbg) o.outln("46-Adding relation " + B_P.name + " to " + B_P.name);
			addRelation(B_P, B_P);
		}

		if (CircularB && A == P_B)  { 
			if (dbg) o.outln("47-Adding relation " + A_P.name + " to " + A_P.name);
			addRelation(A_P, A_P);
		}

		MarkAsPostponed(Postponed, A, B,o);
			
		MarkAsPostponed(Postponed, B, A,o);

		if (dbg2) o.outln("48-AppliedRuleSequenceAndBack (" + Id +") " + A.name + ", " + B.name);

		return true;
	}

*/
// OLDVersionsOf RuleSequence


	private boolean ApplyRuleSequence(BasicMSC A, BasicMSC B, Map Postponed, Map VisitedAsA, Map VisitedAsB, int Id, LTSOutput o) throws Exception {
		boolean dbg = true;
		boolean dbg2 = true;
		if (dbg) o.outln("");
		if (dbg) o.outln("ApplyRuleSequence " + A.name + ", " + B.name);
		
		StringSet Postponables = new StringSet();
		
		Set Prev_P_A_P = new HashSet();
		Iterator It_Prev_P_A_P;
		Set Prev_P_B_P = new HashSet();
		Iterator It_Prev_P_B_P;
				
		BasicMSC A_P = (BasicMSC) A.clone();
		A_P.name = "" + A.name + "_P" + Id;

		BasicMSC P = new BasicMSC();
		P.copyComponents(A);
		P.name = "P" + Id;

		BasicMSC P_B =  (BasicMSC) B.clone();
		P_B.name = "P_" + B.name + "" + Id;			

		boolean found = true;
		while (found)  {
			found = false;
			Set FirstMoves = P_B.getFirstMoves(o);
			Set LastMoves = A_P.getLastMoves(o);
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
						Vector Ids = A_P.getIdOfLastMessage(C1, label, C2);
						int Id1 = ((Integer)Ids.get(0)).intValue();
						int Id2 = ((Integer)Ids.get(1)).intValue();
						if (!B.hasEventId(Id1) && !B.hasEventId(Id2))   {
							if (dbg) o.outln("Found postponable action "+ label);
							A_P.removeLastMessage(C1, label, C2);
							P_B.addMessage(C1, label, C2, Id1, Id2);
							P.addMessage(C1, label, C2, Id1, Id2);
//							Postponables.add(label);
							found = true;
						}
					}
				}
			}
		}		
		
		//if (dbg) o.outln("//If Postponables is empty rule cannot be applied");
		if (P.empty())  {
			MarkAsPostponed(Postponed, A, B, o);	
			return true; 
		}


		//if (dbg) o.outln("// Check if A has ever been used as A with exactly P. If so use the previous A-P");
		if (!VisitedAsA.keySet().contains(A))
			VisitedAsA.put(A, new HashSet());
		Iterator VA = ((Set) VisitedAsA.get(A)).iterator();
		while (VA.hasNext())  {
			Vector aux = (Vector) VA.next();
			//if (Postponables.hasSameElements((StringSet) aux.get(0)))  {
			if (P.isTheSameAs((BasicMSC) aux.get(0),o))  {
				A_P = (BasicMSC) aux.get(1);
				if (dbg) o.outln("Found that " + A.name + " has been used as A with P, so now using " + A_P.name + " as A_P");
				if (aux.size() == 3)  {
					Prev_P_A_P.add(aux.get(2));
					if (dbg) o.outln(" PREV In addition will remember to link Prev_P_A_P=" + ((BasicMSC)aux.get(2)).name + " to link with anything A_P is linked with"); 
				}
			}
		}

		//if (dbg) o.outln("// Check if B has ever been used as B with exactly P. If so use the previous P_B and and dont add from and to P_B");
		if (!VisitedAsB.keySet().contains(B))
			VisitedAsB.put(B, new HashSet());
		Iterator V = ((Set) VisitedAsB.get(B)).iterator();
		boolean Circular = false;
		while (V.hasNext())  {
			Vector aux = (Vector) V.next();
			//if (Postponables.hasSameElements((StringSet) aux.get(0)))  {
			if (P.isTheSameAs((BasicMSC) aux.get(0),o))  {
				Circular = true;
				P_B = (BasicMSC) aux.get(1);
				if (dbg) o.outln("Found that " + B.name + " has been visited as B with exactly P, so now using " + P_B.name + " as previous P_B");
				if (aux.size() == 3)  {
					Prev_P_B_P.add(aux.get(2));
					if (dbg) o.outln(" PREV In addition will remember to link Prev_P_B_P=" + ((BasicMSC)aux.get(2)).name + " FROM anything that is liked to P_B"); 
				}
			}
		}

// If A-P is empty don't use it.
//		if (A_P.getFirstMoves(o).size() == 0)  {
//			A_P = P_B;
//		}				
		
		if (dbg) o.outln("Add transitions '3'");
		Iterator I =  getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC X = (BasicMSC) I.next();
			if (X != B && X != A && X != A_P && X != P_B)  { //&& X != P
				if (getContinuations(X).contains(A))  {
					if (dbg) o.outln("// Adding " + X.name + " -> " + A_P.name);
					addRelation(X, A_P);
					if (HasBeenPostponed(Postponed, X, A))
						MarkAsPostponed(Postponed, X, A_P, o);
				} 
			} 	
		}			

		//Checking for Init->A
		if (getContinuationsInit().contains(A))  {
			if (dbg) o.outln("// Adding Init -> " + A_P.name);
			addRelationInit(A_P);
		}


		if (dbg) o.outln("// Adding bMSC " + A_P.name);
		addbMSC(A_P);
//		if (!Circular)  {
//			if (A_P != P_B)   {
				if (dbg) o.outln("// Adding bMSCs " + P_B.name);
				addbMSC(P_B);
//			}
//		}

		if (dbg) o.outln("Add transitions '1'");
		if (getContinuations(A).contains(A))  {
			if (dbg) o.outln("// Add relation " + A.name + " -> " + A_P.name);
			addRelation (A, A_P);
			MarkAsPostponed(Postponed,A,A_P,o);  // No hace flata hace A->A-P porque ya A->A fue hecho...
			
		}

		if (dbg) o.outln("Add transitions '2'");
		if (!Circular)  { //Si es circular entonces ya fueron agregadas estas continuaciones.
			if (getContinuations(B).contains(B))  {
				if (dbg) o.outln("// Add relation " + P_B.name + " -> " + B.name);
				addRelation(P_B, B);
				//MarkAsPostponed(Postponed, P_B, B, o); 
			}
			
			if (dbg) o.outln("// Add transitions '6'");
			Iterator J =  getContinuations(B).iterator();
			while (J.hasNext()) {
				BasicMSC Aux = (BasicMSC) J.next();
				if (Aux != B && Aux!=A && Aux != A_P && Aux != P_B)  { //  && Aux != P
					if (dbg) o.outln("// Add relation " + P_B.name + " -> " + Aux.name);
					addRelation (P_B, Aux);			
					if (HasBeenPostponed(Postponed, B, Aux))
						MarkAsPostponed(Postponed, P_B, Aux, o);
				}
			}
		}


		if (Circular)  {
			if (VisitedAsA.keySet().contains(P_B)) {
				if (dbg) o.outln(P_B.name + " has been visited as A...so will add A_P to P_B-X and Prev_P_A_P to P_B-X");
				Iterator VV = ((Set) VisitedAsA.get(P_B)).iterator();
				while (VV.hasNext())  {
					Vector aux = (Vector) VV.next();
					if (dbg) o.outln("Adding relation " + A_P.name + " to " + ((BasicMSC)aux.get(1)).name);

					addRelation(A_P, (BasicMSC) aux.get(1));
					MarkAsPostponed(Postponed, A_P, (BasicMSC) aux.get(1),o);

					It_Prev_P_A_P = Prev_P_A_P.iterator();
					while (It_Prev_P_A_P.hasNext())  {
						BasicMSC PAP = (BasicMSC) It_Prev_P_A_P.next();
						if (dbg) o.outln("PREV Adding relation " + PAP.name + " to " + ((BasicMSC)aux.get(1)).name);
						addRelation(PAP, (BasicMSC) aux.get(1));
						MarkAsPostponed(Postponed, PAP, (BasicMSC) aux.get(1),o);
					}
				}
			}
			else
				if (dbg) o.outln(P_B.name + " has not been visited as A ");
		}
	
		if (Circular && A == P_B)  { 
			if (dbg) o.outln("Because Circular and A=P_B, adding relation " + A_P.name + " to " + A_P.name);
			addRelation(A_P, A_P);
			throw new Exception("Case that should never happen");
		}

		if (dbg) o.outln("Adding relation " + A_P.name + " to " + P_B.name);
		addRelation(A_P, P_B);
		MarkAsPostponed(Postponed, A_P, P_B,o);

		It_Prev_P_B_P = Prev_P_B_P.iterator();
		while (It_Prev_P_B_P.hasNext())  {
			BasicMSC PBP = (BasicMSC) It_Prev_P_B_P.next();
			if (dbg) o.outln("Adding PREV relation " + A_P.name + " to " + PBP.name);
			addRelation(A_P, PBP);
			MarkAsPostponed(Postponed, A_P, PBP, o);
		} 
		


		It_Prev_P_A_P = Prev_P_A_P.iterator();
		while (It_Prev_P_A_P.hasNext())  {
			BasicMSC PAP = (BasicMSC) It_Prev_P_A_P.next();
			if (dbg) o.outln("Adding PREV relation " + PAP.name + " to " + P_B.name);
			addRelation(PAP, P_B);
			MarkAsPostponed(Postponed, PAP, P_B, o);
			
			It_Prev_P_B_P = Prev_P_B_P.iterator();
			while (It_Prev_P_B_P.hasNext())  {
				BasicMSC PBP = (BasicMSC) It_Prev_P_B_P.next();
				if (dbg) o.outln("Adding PREV relation " + PAP.name + " to " + PBP.name);
				addRelation(PAP, PBP);
				MarkAsPostponed(Postponed, PAP, PBP, o);
			}			
		} 
		
			
		MarkAsPostponed(Postponed, A, B,o);
			

		//Register that B has been extended with P and that the result is P_B.
		if (!Circular)  {
			if (dbg) o.outln("Register that " + B.name +" has been visited as B that the result is " + P_B.name + ".");
			Vector v = new Vector(2);
			v.add(0, P);
			v.add(1, P_B);
			((Set) VisitedAsB.get(B)).add(v);
		}
		// Register that A has been extended 
		if (dbg) o.outln("Register that " + A.name +" has been visited as A that the result is " + A_P.name + ".");
		Vector v = new Vector(2);
		v.add(0, P);
		v.add(1, A_P);
		
		if (!VisitedAsA.keySet().contains(A))
			VisitedAsA.put(A, new HashSet());
		((Set) VisitedAsA.get(A)).add(v);
		
		if (dbg2) o.outln("AppliedRuleSequence (" + Id +") " + A.name + ", " + B.name);
			
		if (P_B.getPostponables(P_B,o).isTheSameAs(P, o)) {
			if (dbg) o.outln("Independent!");
			MarkAsPostponed(Postponed, P_B, B, o);
		
			BasicMSC BClone = (BasicMSC) B.clone();
			BClone.name = B.name+"clone"+Id;
			addbMSC(BClone);
			addbMSC(P);
			addRelation(A_P, BClone);
			MarkAsPostponed(Postponed, A_P, BClone, o);
			It_Prev_P_A_P = Prev_P_A_P.iterator();
			while (It_Prev_P_A_P.hasNext())  {
				BasicMSC PAP = (BasicMSC) It_Prev_P_A_P.next();
				if (dbg) o.outln("PREV ! ");
				addRelation(PAP, BClone);
				MarkAsPostponed(Postponed, PAP, BClone, o);
			}
			
			addRelation(BClone, BClone);			
			MarkAsPostponed(Postponed, BClone, BClone, o);
			addRelation(BClone, P);
			MarkAsPostponed(Postponed, BClone, P, o);
			addRelation(P, B);
			MarkAsPostponed(Postponed, P, B, o);


			if (dbg) o.outln("// Add transitions '6'");
			Iterator J =  getContinuations(B).iterator();
			while (J.hasNext()) {
				BasicMSC Aux = (BasicMSC) J.next();
				if (Aux != B && Aux!=A && Aux != A_P && Aux != P_B && Aux != P && Aux != BClone)  {
					if (dbg) o.outln("// Add relation " + P.name + " -> " + Aux.name);
					addRelation (P, Aux);
				}
			}
		}
		else
			if (dbg) o.outln("Not Independent!");
			
			
			
		return true;
	}


	private boolean ApplyRuleSequenceAndBack(BasicMSC A, BasicMSC B, Map Postponed, Map VisitedAsA, Map VisitedAsB, int Id, LTSOutput o) throws Exception {
		boolean dbg = true;
		boolean dbg2 = true;
		if (dbg) o.outln("");
		if (dbg) o.outln("ApplyRuleSequenceAndBack " + A.name + ", " + B.name);
		boolean CircularA = false, CircularB = false;
		
		StringSet PostponablesA = new StringSet();

		Set Prev_P_A_P = new HashSet();
		Set Prev_P_B_P = new HashSet();
		Iterator It_Prev_P_A_P;
		Iterator It_Prev_P_B_P;
		

		Set PrevB_P_A_P = new HashSet();
		Set PrevB_P_B_P = new HashSet();
		Iterator It_PrevB_P_A_P;
		Iterator It_PrevB_P_B_P;


		BasicMSC A_P = (BasicMSC) A.clone();
		A_P.name = "" + A.name + "_P" + Id;

		BasicMSC P_B =  (BasicMSC) B.clone();
		P_B.name = "P_" + B.name + "" + Id;			

		BasicMSC P_B_P =  new BasicMSC();
		P_B_P.copyComponents(B);
		
		BasicMSC PA =  new BasicMSC();
		PA.copyComponents(A);

		{  // Find postponables from A to B
			boolean found = true;
			while (found)  {
				found = false;
				Set FirstMoves = P_B.getFirstMoves(o);
				Set LastMoves = A_P.getLastMoves(o);
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
							if (dbg) o.outln("1-Found postponableA action "+ label);
							Vector Ids = A_P.getIdOfLastMessage(C1, label, C2);
							int Id1 = ((Integer)Ids.get(0)).intValue();
							int Id2 = ((Integer)Ids.get(1)).intValue();
							if (!B.hasEventId(Id1) && !B.hasEventId(Id2))   {
								A_P.removeLastMessage(C1, label, C2);
								P_B.addMessage(C1, label, C2, Id1, Id2);
								P_B_P.addMessage(C1, label, C2, Id1, Id2);
								PA.addMessage(C1, label, C2, Id1, Id2);
								PostponablesA.add(label);
								found = true;
							}
						}
					}
				}
			}				
		}

		StringSet PostponablesB = new StringSet();

		BasicMSC B_P = (BasicMSC) B.clone();
		B_P.name = "" + B.name + "_P" + Id;

		BasicMSC P_A =  (BasicMSC) A.clone();
		P_A.name = "P_" + A.name + "" + Id;			

		BasicMSC P_A_P =  new BasicMSC();
		P_A_P.copyComponents(A);

		BasicMSC PB =  new BasicMSC();
		PB.copyComponents(B);
		
		{   //Find postponables from B to A
			boolean found = true;
			while (found)  {
				found = false;
				Set FirstMoves = P_A.getFirstMoves(o);
				Set LastMoves = B_P.getLastMoves(o);
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
							if (dbg) o.outln("2-Found postponableB action "+ label);
							Vector Ids = B_P.getIdOfLastMessage(C1, label, C2);
							int Id1 = ((Integer)Ids.get(0)).intValue();
							int Id2 = ((Integer)Ids.get(1)).intValue();
							if (!A.hasEventId(Id1) && !A.hasEventId(Id2))   {
								B_P.removeLastMessage(C1, label, C2);
								P_A.addMessage(C1, label, C2, Id1, Id2);
								P_A_P.addMessage(C1, label, C2, Id1, Id2);
								PB.addMessage(C1, label, C2, Id1, Id2);
								PostponablesB.add(label);
								found = true;
							}
						}
					}
				}
			}		
		}
		
		//if (dbg) o.outln("//If Postponables is empty rule cannot be applied");
		if (PostponablesB.size()==0 && PostponablesA.size()==0)  {
			MarkAsPostponed(Postponed, A, B,o);	
			MarkAsPostponed(Postponed, B, A,o);	
			return true; 
		}
		
		//if (dbg) o.outln("// Check if A has ever been used as A with exactly P. If so use the previous A-P");
		if (!VisitedAsA.keySet().contains(A))
			VisitedAsA.put(A, new HashSet());
		Iterator VA = ((Set) VisitedAsA.get(A)).iterator();
		while (VA.hasNext())  {
			Vector aux = (Vector) VA.next();
			//if (Postponables.hasSameElements((StringSet) aux.get(0)))  {
			if (PA.isTheSameAs((BasicMSC) aux.get(0),o))  {
				A_P = (BasicMSC) aux.get(1);
				if (dbg) o.outln("Found that " + A.name + " has been used as A with P, so using " + A_P.name + " as A_P. Size("+aux.size()+")");
				if (aux.size() == 3)  {
					Prev_P_A_P.add(aux.get(2));
					if (dbg) o.outln("PREV remembering Prev_P_A_P=" + ((BasicMSC) aux.get(2)).name + " in order to link with all things A_P is linked with");
				}
			}
		}
		
		//if (dbg) o.outln("// Check if B has ever been used as A with exactly P. If so use the previous B-P");
		if (!VisitedAsA.keySet().contains(B))
			VisitedAsA.put(B, new HashSet());
		Iterator VB = ((Set) VisitedAsA.get(B)).iterator();
		while (VB.hasNext())  {
			Vector aux = (Vector) VB.next();
			//if (Postponables.hasSameElements((StringSet) aux.get(0)))  {
			if (PB.isTheSameAs((BasicMSC) aux.get(0),o))  {
				B_P = (BasicMSC) aux.get(1);
				if (dbg) o.outln("Found that " + B.name + " has been used as A with exactly P, so using " + B_P.name + " as B_P. Size("+aux.size()+")");
				if (aux.size() == 3)  {
					Prev_P_B_P.add(aux.get(2));
					if (dbg) o.outln("PREV remembering Prev_P_B_P=" + ((BasicMSC) aux.get(2)).name + " in order to link with all the things B_P is linked with");
				} 
			}
		}
		
		
		if (PostponablesA.size()!=0)  {
			if (dbg) o.outln("3-PostponablesA is not empty");
			// Check if B has ever used as B  with exactly P. If so use the previous P_B and and dont add from and to P_B
			if (!VisitedAsB.keySet().contains(B))
				VisitedAsB.put(B, new HashSet());
			Iterator V = ((Set) VisitedAsB.get(B)).iterator();
			CircularB = false;
			while (V.hasNext() && !CircularB)  {
				Vector aux = (Vector) V.next();
				if (PA.isTheSameAs((BasicMSC) aux.get(0),o))  {
					if (dbg) o.outln("4-Circular!!");
					P_B = (BasicMSC) aux.get(1);
					CircularB = true;
					if (dbg) o.outln("Found that " + B.name + " has been visited as B with exactly P, so now using " + P_B.name + " as previous P_B");
					if (aux.size() == 3)  {
						PrevB_P_B_P.add(aux.get(2));
						if (dbg) o.outln(" PREV In addition will remember to link PrevB_P_B_P=" + ((BasicMSC)aux.get(2)).name + " FROM anything that is liked to P_B"); 
					}			
				}
			}
			
			if (dbg) o.outln("5-Add transitions '3'");
			Iterator I =  getbMSCs().iterator();
			while (I.hasNext()) {
				BasicMSC X = (BasicMSC) I.next();
				if (X != B && X != A && X != A_P && X != B_P && X != P_B && X != P_A)  {
					if (getContinuations(X).contains(A))  {
						if (dbg) o.outln("6-Adding " + X.name + " -> " + A_P.name);
						addRelation(X, A_P);
						if (HasBeenPostponed(Postponed, X, A))
							MarkAsPostponed(Postponed, X, A_P, o);						
					} 
				} 	
			}			
	
			//Checking for Init->A
			if (getContinuationsInit().contains(A))  {
				if (dbg) o.outln("7-Adding Init -> " + A_P.name);
				addRelationInit(A_P);
			}
			
			if (dbg) o.outln("8-Adding bMSC " + A_P.name);
			addbMSC(A_P);
			if (!CircularB)  {
	//			if (A_P != P_B)   {
					if (dbg) o.outln("9-Adding bMSC " + P_B.name);
					addbMSC(P_B);
	//			}
			}

			if (dbg) o.outln("10-Add relation " + B.name + " -> " + A_P.name);
			addRelation (B, A_P);
			MarkAsPostponed(Postponed,B, A_P,o);

			//if (dbg) o.outln("Add transitions '1'");
			if (getContinuations(A).contains(A))  {
				if (dbg) o.outln("11-Add relation " + A.name + " -> " + A_P.name);
				addRelation (A, A_P);
				MarkAsPostponed(Postponed, A, A_P,o);
			}

			if (getContinuations(B).contains(B))  {
				if (dbg) o.outln("12-Add relation " + P_B.name + " -> " + B.name);
				addRelation(P_B, B);
				//MarkAsPostponed(Postponed, P_B, B,o);
				
			}
			if (dbg) o.outln("13-Add relation " + P_B.name + " -> " + A.name);
			addRelation (P_B, A);
			if (!CircularB)
				MarkAsPostponed(Postponed, P_B, A,o);		
			
			if (!CircularB)  {
				if (dbg) o.outln("14-Add transitions '6'");
				Iterator J =  getContinuations(B).iterator();
				while (J.hasNext()) {
					BasicMSC Aux = (BasicMSC) J.next();
					if (Aux != B && Aux!=A && Aux != A_P && Aux != B_P && Aux != P_B && Aux != P_A)  {
						if (dbg) o.outln("15-Add relation " + P_B.name + " -> " + Aux.name);
						addRelation (P_B, Aux);
						if (HasBeenPostponed(Postponed, B, Aux))  
							MarkAsPostponed(Postponed, P_B, Aux, o);
					}
				}
			}

			if (CircularB)  {
				if (VisitedAsA.keySet().contains(P_B)) {
					Iterator VV = ((Set) VisitedAsA.get(P_B)).iterator();
					while (VV.hasNext())  {
						Vector aux = (Vector) VV.next();
						//if (PostponablesA.hasSameElements((StringSet) aux.get(0)))  {
						//if (PA.isTheSameAs((BasicMSC) aux.get(0),o))  {
							if (dbg) o.outln("16-Adding relation " + A_P.name + " to " + ((BasicMSC)aux.get(1)).name);
							addRelation(A_P, (BasicMSC) aux.get(1));
							MarkAsPostponed(Postponed, A_P, (BasicMSC) aux.get(1),o);
							
							//Why dont I add PAP -> Aux.get(1)?
							It_Prev_P_A_P = Prev_P_A_P.iterator();
							while (It_Prev_P_A_P.hasNext())  {
								BasicMSC PAP = (BasicMSC) It_Prev_P_A_P.next();
								if (dbg) o.outln("16,5-Adding PREV relation " + PAP.name + " to " + ((BasicMSC) aux.get(1)).name);
								addRelation(PAP, (BasicMSC) aux.get(1));
								MarkAsPostponed(Postponed, PAP, (BasicMSC) aux.get(1), o);	
							}

							
						//}
					}
				}
			}

			if (dbg) o.outln("17-Adding relation " + A_P.name + " to " + P_B.name);
			addRelation(A_P, P_B);	
			
			It_PrevB_P_B_P = PrevB_P_B_P.iterator();
			while (It_PrevB_P_B_P.hasNext())  {
				BasicMSC PBP = (BasicMSC) It_PrevB_P_B_P.next();
				if (dbg) o.outln("Adding PREV relation " + A_P.name + " to " + PBP.name);
				addRelation(A_P, PBP);
				MarkAsPostponed(Postponed, A_P, PBP, o);
			} 
		
			
			MarkAsPostponed(Postponed, A_P, P_B, o);
			It_Prev_P_A_P = Prev_P_A_P.iterator();
			while (It_Prev_P_A_P.hasNext())  {
				BasicMSC PAP = (BasicMSC) It_Prev_P_A_P.next();
				if (dbg) o.outln("17,5-Adding PREV relation " + PAP.name + " to " + P_B.name);
				addRelation(PAP, P_B);
				MarkAsPostponed(Postponed, PAP, P_B, o);	
		
				It_PrevB_P_B_P = PrevB_P_B_P.iterator();
				while (It_PrevB_P_B_P.hasNext())  {
					BasicMSC PBP = (BasicMSC) It_PrevB_P_B_P.next();
					if (dbg) o.outln("Adding PREV relation " + PAP.name + " to " + PBP.name);
					addRelation(PAP, PBP);
					MarkAsPostponed(Postponed, PAP, PBP, o);
				} 
			}
			
			
			if (dbg) o.outln("18-Adding relation " + P_B.name + " to " + A_P.name);
			addRelation(P_B, A_P);
			MarkAsPostponed(Postponed, P_B, A_P, o);


		}


		if (PostponablesB.size()!=0)  {
			if (dbg) o.outln("21-PostponablesB is not empty");		
			// Check if A has ever been used as B with exactly P. If so use the previous P_A and and dont add from and to P_A
			if (!VisitedAsB.keySet().contains(A))
				VisitedAsB.put(A, new HashSet());
			Iterator V = ((Set) VisitedAsB.get(A)).iterator();
			CircularA = false;
			while (V.hasNext() && !CircularA)  {
				Vector aux = (Vector) V.next();
				//if (PostponablesB.hasSameElements((StringSet) aux.get(0)))  {
				if (PB.isTheSameAs((BasicMSC) aux.get(0),o))  {
					P_A = (BasicMSC) aux.get(1);
					CircularA = true;
					if (dbg) o.outln("22-Circular!!");
					
					if (dbg) o.outln("Found that " + A.name + " has been visited as B with exactly P, so now using " + P_A.name + " as previous P_A");
					if (aux.size() == 3)  {
						PrevB_P_A_P.add(aux.get(2));
						if (dbg) o.outln(" PREV In addition will remember to link PrevB_P_A_P=" + ((BasicMSC)aux.get(2)).name + " FROM anything that is liked to P_A"); 
					}
				}
			}

			//if (dbg) o.outln("Add transitions '5'");
			Iterator I =  getbMSCs().iterator();
			while (I.hasNext()) {
				BasicMSC X = (BasicMSC) I.next();
				if (X != B && X != A && X != A_P && X != B_P && X != P_B && X != P_A)  {					if (getContinuations(X).contains(B))  {
						if (dbg) o.outln("23-Adding " + X.name + " -> " + B_P.name);
						addRelation(X, B_P);
						if (HasBeenPostponed(Postponed, X, B))
							MarkAsPostponed(Postponed, X, B_P, o);						
					} 
				} 	
			}			
	
			//Checking for Init->B
			if (getContinuationsInit().contains(B))  {
				if (dbg) o.outln("24-Adding Init -> " + B_P.name);
				addRelationInit(B_P);
			}

			if (dbg) o.outln("25-Adding bMSC " + B_P.name);
			addbMSC(B_P);

			if (!CircularA)  {
	//			if (A_P != P_B)   {
					if (dbg) o.outln("26-Adding bMSC " + P_A.name);
					addbMSC(P_A);
	//			}
			}

			if (dbg) o.outln("27- Add relation " + A.name + " -> " + B_P.name);
			addRelation (A, B_P);
			MarkAsPostponed(Postponed, A, B_P,o);
	
			//if (dbg) o.outln("Add transitions '2'");
			if (getContinuations(B).contains(B))  {
				if (dbg) o.outln("28- Add relation " + B.name + " -> " + B_P.name);
				addRelation (B, B_P);
				MarkAsPostponed(Postponed, B, B_P,o);
			}		

			//if (dbg) o.outln("Add transitions '2'");
			if (getContinuations(A).contains(A))  {
				if (dbg) o.outln("29- Add relation " + P_A.name + " -> " + A.name);
				addRelation(P_A, A);
				//MarkAsPostponed(Postponed, P_A, A,o);		
			}
			if (dbg) o.outln("30- Add relation " + P_A.name + " -> " + B.name);
			addRelation (P_A, B);
			if (!CircularA)
				MarkAsPostponed(Postponed, P_A, B,o);		

			if (!CircularA)  {
				if (dbg) o.outln("31- Add transitions '4'");
				Iterator J =  getContinuations(A).iterator();
				while (J.hasNext()) {
					BasicMSC Aux = (BasicMSC) J.next();
					if (Aux != B && Aux!=A && Aux != A_P && Aux != B_P && Aux != P_B && Aux != P_A)  {
						if (dbg) o.outln("32- Add relation " + P_A.name + " -> " + Aux.name);
						addRelation (P_A, Aux);
					//	if (HasBeenPostponed(Postponed, A, Aux))  {
					//		MarkAsPostponed(Postponed, P_A, Aux, o);
					}
				}			
			}
			
			
			if (CircularA)  {
				if (VisitedAsA.keySet().contains(P_A)) {
					Iterator VV = ((Set) VisitedAsA.get(P_A)).iterator();
					while (VV.hasNext())  {
						Vector aux = (Vector) VV.next();
						//if (PostponablesB.hasSameElements((StringSet) aux.get(0)))  {
						//if (PB.isTheSameAs((BasicMSC) aux.get(0),o))  {
							if (dbg) o.outln("33-Adding relation " + B_P.name + " to " + ((BasicMSC)aux.get(1)).name);
							addRelation(B_P, (BasicMSC) aux.get(1));
							MarkAsPostponed(Postponed, B_P, (BasicMSC) aux.get(1),o);
							
							
							//Why dont I add PBP to aux.get?
							It_Prev_P_B_P = Prev_P_B_P.iterator();
							while (It_Prev_P_B_P.hasNext())  {
								BasicMSC PBP = (BasicMSC) It_Prev_P_B_P.next();
								if (dbg) o.outln("33,5-Adding PREV relation " + PBP.name + " to " + ((BasicMSC) aux.get(1)).name);
								addRelation(PBP, (BasicMSC) aux.get(1));
								MarkAsPostponed(Postponed, PBP, (BasicMSC) aux.get(1), o);
							}
						//}
					}
				}
			}
			
			if (dbg) o.outln("34-Adding relation " + B_P.name + " to " + P_A.name);
			addRelation(B_P, P_A);
			MarkAsPostponed(Postponed, B_P, P_A, o);
			
			It_PrevB_P_A_P = PrevB_P_A_P.iterator();
			while (It_PrevB_P_A_P.hasNext())  {
				BasicMSC PAP = (BasicMSC) It_PrevB_P_A_P.next();
				if (dbg) o.outln("Adding PREV relation " + B_P.name + " to " + PAP.name);
				addRelation(B_P, PAP);
				MarkAsPostponed(Postponed, B_P, PAP, o);
			} 
				

			It_Prev_P_B_P = Prev_P_B_P.iterator();
			while (It_Prev_P_B_P.hasNext())  {
				BasicMSC PBP = (BasicMSC) It_Prev_P_B_P.next();
				if (dbg) o.outln("34-Adding PREV relation " + PBP.name + " to " + P_A.name);
				addRelation(PBP, P_A);
				MarkAsPostponed(Postponed, PBP, P_A, o);
			
				It_PrevB_P_A_P = PrevB_P_A_P.iterator();
				while (It_PrevB_P_A_P.hasNext())  {
					BasicMSC PAP = (BasicMSC) It_PrevB_P_A_P.next();
					if (dbg) o.outln("Adding PREV relation " + PBP.name + " to " + PAP.name);
					addRelation(PBP, PAP);
					MarkAsPostponed(Postponed, PBP, PAP, o);
				} 
			}

			if (dbg) o.outln("35-Adding relation " + P_A.name + " to " + B_P.name);
			addRelation(P_A, B_P);
			MarkAsPostponed(Postponed, P_A, B_P, o);

		}
		
		
		//Register that A has been extended with P and that the result is P_A.
		if (dbg) o.outln("36-Register that " + A.name +" has been visited as B that the result is " + P_A.name + ".");
		Vector v = new Vector(2);
		v.add(0, PB);
		v.add(1, P_A);
		if (PostponablesB.size()!=0 && PostponablesA.size()!=0)  {
			v.ensureCapacity(3);
			v.add(2, P_A_P);
			if (dbg) o.outln("Adding PREV not null !!! Name :" + P_A_P.name);
		}
		if (!VisitedAsB.keySet().contains(A))
			VisitedAsB.put(A, new HashSet());
		((Set) VisitedAsB.get(A)).add(v);



		//Register that B has been extended with P and that the result is P_B.
		if (dbg) o.outln("19-Register that " + B.name +" has been visited as B that the result is " + P_B.name + ".");
		v = new Vector(2);
		v.add(0, PA);
		v.add(1, P_B);
		if (PostponablesB.size()!=0 && PostponablesA.size()!=0)  {
			v.ensureCapacity(3);
			v.add(2, P_B_P);
			if (dbg) o.outln("Adding PREV not null !!! Name :" + P_B_P.name);
		}
		if (!VisitedAsB.keySet().contains(B))
			VisitedAsB.put(B, new HashSet());
		((Set) VisitedAsB.get(B)).add(v);			

			
		// Register that B has been extended 
		if (dbg) o.outln("37-Register that " + B.name +" has been visited as A that the result is " + B_P.name + ".");
		v = new Vector(2);
		v.add(0, PB);
		v.add(1, B_P);
		if (PostponablesB.size()!=0 && PostponablesA.size()!=0)  {
			v.ensureCapacity(3);
			v.add(2, P_B_P);
			if (dbg && P_B_P == null) o.outln("Adding PREV null !!!");
			if (dbg) o.outln("Adding PREV not null !!! Name :" + P_B_P.name);
		}
		if (!VisitedAsA.keySet().contains(B))
			VisitedAsA.put(B, new HashSet());
		((Set) VisitedAsA.get(B)).add(v);

		
		// Register that A has been extended 
		if (dbg) o.outln("20-Register that " + A.name +" has been visited as A that the result is " + A_P.name + ".");
		v = new Vector(2);
		//v.add(0, PostponablesA);
		v.add(0, PA);
		v.add(1, A_P);
		if (PostponablesB.size()!=0 && PostponablesA.size()!=0)  {
			v.ensureCapacity(3);
			v.add(2, P_A_P);
			if (dbg && P_A_P == null) o.outln("Adding PREV null !!!");
			if (dbg) o.outln("Adding PREV not null !!! Name: " + P_A_P.name);			
		}
		if (!VisitedAsA.keySet().contains(A))
			VisitedAsA.put(A, new HashSet());
		((Set) VisitedAsA.get(A)).add(v);				
			




		// If A-P is empty don't use it.
		// if (A_P.getFirstMoves(o).size() == 0)  {
		// A_P = P_B;
		// }				
		


		if (PostponablesB.size()!=0 && PostponablesA.size()!=0)  {
		
			P_A_P.append(A_P);
			P_A_P.name = "P_" + A.name + "_P" + Id;			

			P_B_P.append(B_P);
			P_B_P.name = "P_" + B.name + "_P" + Id;			
			
		
			if (dbg) o.outln("38- Adding bMSC " + P_A_P.name);
			addbMSC(P_A_P);
			if (dbg) o.outln("39- Adding bMSC " + P_B_P.name);
			addbMSC(P_B_P);
			
			
			if (dbg) o.outln("40- Add relation " + A_P.name + " -> " + P_B_P.name);
			addRelation (A_P, P_B_P);
			MarkAsPostponed(Postponed, A_P, P_B_P,o);	
			
			It_Prev_P_A_P = Prev_P_A_P.iterator();
			while(It_Prev_P_A_P.hasNext())  {
				BasicMSC PAP= (BasicMSC) It_Prev_P_A_P.next();
				if (dbg) o.outln("40- Add PREV relation " + PAP.name + " -> " + P_B_P.name);
				addRelation (PAP, P_B_P);
				MarkAsPostponed(Postponed, PAP, P_B_P,o);		
				
				Iterator It_Cont_PAP = getContinuations(PAP).iterator();
				while (It_Cont_PAP.hasNext()) {
					BasicMSC Cont_PAP = (BasicMSC) It_Cont_PAP.next();
					if (dbg) o.outln("40,5- Add PREV relation " + P_A_P.name + " -> " + Cont_PAP.name);
					addRelation(P_A_P, Cont_PAP);
					MarkAsPostponed(Postponed, P_A_P, Cont_PAP, o);		
				}
				
			}
			
			
			if (dbg) o.outln("41- Add relation " + B_P.name + " -> " + P_A_P.name);
			addRelation (B_P, P_A_P);
			MarkAsPostponed(Postponed, B_P, P_A_P,o);		
			
			It_Prev_P_B_P = Prev_P_B_P.iterator();			
			while (It_Prev_P_B_P.hasNext())  {
				BasicMSC PBP = (BasicMSC) It_Prev_P_B_P.next();
				if (dbg) o.outln("41- Add PREV relation " + PBP.name + " -> " + P_A_P.name);
				addRelation (PBP, P_A_P);
				MarkAsPostponed(Postponed, PBP, P_A_P,o);		
				
				Iterator It_Cont_PBP = getContinuations(PBP).iterator();
				while (It_Cont_PBP.hasNext()) {
					BasicMSC Cont_PBP = (BasicMSC) It_Cont_PBP.next();
					if (dbg) o.outln("41,5- Add PREV relation " + P_B_P.name + " -> " + Cont_PBP.name);			
					addRelation(P_B_P, Cont_PBP);
					MarkAsPostponed(Postponed, P_B_P, Cont_PBP, o);		
				}
				
			}
			
			if (dbg) o.outln("42-Adding relation " + P_A_P.name + " to " + P_B_P.name);
			addRelation(P_A_P, P_B_P);
			if (dbg) o.outln("43-Adding relation " + P_A_P.name + " to " + P_B.name);
			addRelation(P_A_P, P_B);
			It_PrevB_P_B_P = PrevB_P_B_P.iterator();
			while (It_PrevB_P_B_P.hasNext())  {
				BasicMSC PBP = (BasicMSC) It_PrevB_P_B_P.next();
				if (dbg) o.outln("Adding PREV relation " + P_A_P.name + " to " + PBP.name);
				addRelation(P_A_P, PBP);
				MarkAsPostponed(Postponed, P_A_P, PBP, o);
			} 
	
			
			
	
			if (dbg) o.outln("44-Adding relation " + P_B_P.name + " to " + P_A_P.name);
			addRelation(P_B_P, P_A_P);
			if (dbg) o.outln("45-Adding relation " + P_B_P.name + " to " + P_A.name);
			addRelation(P_B_P, P_A);
			It_PrevB_P_A_P = PrevB_P_A_P.iterator();
			while (It_PrevB_P_A_P.hasNext())  {
				BasicMSC PAP = (BasicMSC) It_PrevB_P_A_P.next();
				if (dbg) o.outln("Adding PREV relation " + P_B_P.name + " to " + PAP.name);
				addRelation(P_B_P, PAP);
				MarkAsPostponed(Postponed, P_B_P, PAP, o);
			} 
	
	
			MarkAsPostponed(Postponed, P_A_P, P_B_P,o);
			MarkAsPostponed(Postponed, P_A_P, P_B,o);
			
			MarkAsPostponed(Postponed, P_B_P, P_A_P,o);
			MarkAsPostponed(Postponed, P_B_P, P_A,o);			
		}


		if (CircularA && B == P_A)  { 
			if (dbg) o.outln("46-Adding relation " + B_P.name + " to " + B_P.name);
			addRelation(B_P, B_P);
		}

		if (CircularB && A == P_B)  { 
			if (dbg) o.outln("47-Adding relation " + A_P.name + " to " + A_P.name);
			addRelation(A_P, A_P);
		}

		MarkAsPostponed(Postponed, A, B,o);
			
		MarkAsPostponed(Postponed, B, A,o);

		if (dbg2) o.outln("48-AppliedRuleSequenceAndBack (" + Id +") " + A.name + ", " + B.name);

		return true;
	}


	public BasicMSC createbMSC(Vector trace, LTSOutput o)  {
		boolean dbg = false;
		BasicMSC b = new BasicMSC();
		
		System.out.println("Specification.createbMSC: method called");
		if (dbg) o.outln("createBMSC...");
		b.copyComponents((BasicMSC) getbMSCs().iterator().next());
		if (dbg) o.outln("copied components");
		Vector inv = new Vector();
		int pos = 0;
		for (Enumeration e = trace.elements() ; e.hasMoreElements() ;) {
			inv.add(pos,(String) e.nextElement());
			pos++;
		}
		
		for (int i = pos-1; i>=0; i--)  {
			if (dbg) o.outln("got element");
        	String lbl = (String) inv.get(i);
			b.addMessage(getSource(lbl, o), lbl, getTarget(lbl, o));
			if (dbg) o.outln("added");
		}
		if (dbg) o.outln("Finished trace");
		return b;
	}
	
	public String getSource(String lbl, LTSOutput o)  {
		Iterator I = getbMSCs().iterator();	
		BasicMSC b = null;
		String source = null;
		boolean found = false;
		
		while (I.hasNext() && !found)  {
			b = (BasicMSC) I.next();
			source = b.getSource(lbl);
			found =  (source != null);
		}
		if (!found)  {
			o.outln("No component that can output " + lbl); 
			throw new Error("No component that can output " + lbl); 
		}
		return source;
	}

	public String getTarget(String lbl, LTSOutput o)  {
		Iterator I = getbMSCs().iterator();	
		BasicMSC b = null;
		String target = null;
		boolean found = false;
		
		while (I.hasNext() && !found)  {
			b = (BasicMSC) I.next();
			target = b.getTarget(lbl);
			found =  (target != null);
		}
		if (!found)  {
			o.outln("No component that can input " + lbl); 
			throw new Error("No component that can input " + lbl); 
		}
		return target;
	}



	public void addMissingComponents() {
		Iterator I;

		// first get all component names.
		StringSet names = new StringSet();
		I = getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC B = (BasicMSC) I.next();
			Iterator J = B.components().iterator();
			while (J.hasNext()) {
				String name = (String) J.next();
				names.add(name);
			}
		}
		
		I = getbMSCs().iterator();
		while (I.hasNext()) {
			BasicMSC B = (BasicMSC) I.next();
			Iterator J = names.iterator();
			while (J.hasNext()) {
				String name = (String) J.next();
				if (B.getInstance(name) == null) 
					B.addInstance(name, new Instance());
			}
		}
	}
	
	public void print(LTSOutput Out) {
		Iterator I,J;	
		BasicMSC b,c;

		I = getbMSCs().iterator();
		while (I.hasNext()) {
			b = (BasicMSC)I.next();
			b.print(Out);
		}
		
		Out.outln("hmsc;");

		I = getContinuationsInit().iterator();
		while (I.hasNext()) {
			b = (BasicMSC)I.next();
			Out.outln("Init -> " + b.name + ";");
		}
		
		I = getContinuationsFinal().iterator();
		while (I.hasNext()) {
			b = (BasicMSC)I.next();
			Out.outln(b.name + " -> Stop;");
		}
		
		J = getbMSCs().iterator();
		while (J.hasNext()) {
			c = (BasicMSC) J.next();
			I = getContinuations(c).iterator();
			while (I.hasNext()) {
				b = (BasicMSC) I.next();
				Out.outln(c.name + " -> " +  b.name + ";");
			}
		}
		Out.outln("endhmsc");
	}	



/*
 *  This is an auxiliary class to obtain the transition probability from one BMSC to another.
 *  Look at method addProbabilitiscRelation to further understanding
 */

class ProbMSC{
		
		BasicMSC b;
		String probValue;
		
		ProbMSC(BasicMSC _b, String _probValue){
			this.b = _b;
			this.probValue = _probValue;
		}
		
		public BasicMSC getbMSC(){
			return this.b;
		}
		
		public String getprobValue(){
			return this.probValue;
		}
		
	}
	
}


