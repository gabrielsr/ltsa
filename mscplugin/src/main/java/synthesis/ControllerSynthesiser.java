package synthesis;

import java.io.*;
import java.util.*;
import ic.doc.ltsa.common.iface.LTSOutput;

class PathNotFound extends Exception {
	public PathNotFound () {
		super();
	}
	public PathNotFound (String S) {
		super(S);
	}
}

class UncoveredLTS extends Exception {
	public UncoveredLTS () {
		super();
	}
	public UncoveredLTS (String S) {
		super(S);
	}
}


public class  ControllerSynthesiser {
	private static final String SYSNAME = "ImpliedScenarioProperty";
	private static final String SYSVERSION = "v3.0";
	private boolean Error = false;




	public int synthesise(Specification S, MyOutput OutStream, String ControllerName, LTSOutput o) throws Exception {

		return BuildFSP(S, OutStream, ControllerName, o);
		//return BuildProbabilisticFSP(S, OutStream, ControllerName, o);	
	}


//------------------------------------------------------------------------------------------------------
	private int BuildFSP(Specification S, MyOutput Out, String ControllerName, LTSOutput o)  throws Exception {
		boolean dbg = false;
		boolean dbg2 = false;
		Set Nodes = new HashSet();
		Map Transitions = new HashMap();
		Map Components = new HashMap();

		Iterator ItComponents = S.components().iterator();
		int CompId = 0;
		while (ItComponents.hasNext())  {
			String CompName = (String) ItComponents.next();
			Components.put(new Integer(CompId), CompName);
			if (dbg2) o.outln(CompId + "-" + CompName);
			CompId++;
		}



		Node Init = BuildLTS(Nodes, Transitions, S, Components, o);
		if (dbg) o.outln("Number of Nodes:" + Nodes.size());
		int Size = Nodes.size();
		//Create Ids for Nodes
		Map NodeIds= new HashMap();
		Iterator ItNodes = Nodes.iterator();
		int a = 1;
		while (ItNodes.hasNext())  {
			Node Aux = (Node) ItNodes.next();
			//NodeIds.put(Aux, new Integer(Aux.Id));
			NodeIds.put(Aux, new Integer(a));
			a++;
		}

		Out.println("Coordinator = N" + NodeIds.get(Init) + ",");
		ItNodes = Transitions.keySet().iterator();
		while (ItNodes.hasNext())  {
			Node N = (Node) ItNodes.next();

			Iterator ItTransitions = ((Set) Transitions.get(N)).iterator();
			if (!ItTransitions.hasNext())   {
				Out.print("N" + NodeIds.get(N) + " = STOP");
			}
			else  {
				Out.print("N" + NodeIds.get(N) + " = (");
				while (ItTransitions.hasNext())  {
					Vector t = (Vector) ItTransitions.next();
					FSPLabel l = new FSPLabel();
					l.setComponentLabel((String) Components.get(t.get(0)));
					Out.print("s_" + l.getLabel() + "_" + ((BasicMSC) t.get(1)).name + " -> N" + NodeIds.get((Node) t.get(2)));
					if (ItTransitions.hasNext())
						Out.print("|");
				}
				Out.print(")");
			}
			if (ItNodes.hasNext())
				Out.println(",");
			else
				Out.println(".");
		}
		return Size;
	}

	private Node BuildLTS(Set Nodes, Map Transitions, Specification S, Map Components, LTSOutput o) throws Exception {
		boolean dbg = false;
		boolean dbg2 = false;
		boolean benchmark = false;
		FileOutputStream fout=null;
		PrintStream BenchOut=null;

		if (benchmark) {
			fout = new FileOutputStream("benchmark.csv");
			BenchOut = new PrintStream(fout);
		}

		Map Remaining = new HashMap();
		int NodeId = 0;

		BasicMSC BInit = null;
		BasicMSC Aux = null;
			BInit = new BasicMSC();
			BInit.name = "BInit";
			Iterator ItComps = S.components().iterator();
			while (ItComps.hasNext())  {
				BInit.addInstance((String) ItComps.next(), new Instance());
			}
			Aux = BInit;

		if (dbg) o.outln("Configure Init");
		Node Init = new Node(S.components().size(), Aux);
		Init.Id = NodeId++;


		Nodes.add(Init);
		int MaxHistoryLength = 0;
		MaxHistoryLength = addToRemaining(Remaining, Init, MaxHistoryLength);

		Node N = getNextFromRemaining(Remaining, MaxHistoryLength);

		while (N!=null)  {
//			if (Nodes.size()>30000)  {
//				o.outln("DEMASIADOS!!!!");
//				throw new Error("DEMASIADOS!!");
//			}
			int numConts = 0;
			if (dbg) o.outln("Nodes = " + Nodes.size() + " Remaining = " + Remaining.size() + " Nodes in Transitions = " + Transitions.keySet().size());
			if (dbg2) o.outln("Processing node:" + N.Id);
			for (int c=0; c<S.components().size(); c++)  {
				if (dbg) o.outln("Looking at component " +  Components.get(new Integer(c)));

				Set NewNodes = new HashSet();
				if (!N.isFirst(c))  {
					if (dbg) o.outln("No pica en punta");
					Node M = N.Clone();
					M.Id = NodeId++;
					if (M.Move(c, Components, BenchOut, o))  { 		//Checks if valid move too.
						Node ShortM = M.eliminateLoop(o);
						if (ShortM !=null)   {
							M = ShortM;
						}

						Node aux = AlreadyExists(Nodes, M, Components, o);
						if (aux ==null)
							NewNodes.add(M);
						else
							NewNodes.add(aux);
					}
					//else
						//AlreadyExists(Nodes, M, Components, o);
				}
				else  {
					if (dbg) o.outln("Looking at continuations");
					BasicMSC b = N.getLocation(c);
					Set Continuations;
					if (b==BInit)
						Continuations = S.getContinuationsInit();
					else
						Continuations = S.getContinuations(b);

					Iterator ItContinuations = Continuations.iterator();
					while (ItContinuations.hasNext())  {
						BasicMSC NextB = (BasicMSC) ItContinuations.next();
						if (dbg) o.outln("Cloning");
						Node M = N.Clone();
						M.Id = NodeId++;
						if (dbg) o.outln("Moving");
						if (M.Move(c, NextB, Components,BenchOut, o))  {	 //Checks if valid move too.
							Node ShortM = M.eliminateLoop(o);
							if (ShortM !=null)   {
								M = ShortM;
							}

							Node aux = AlreadyExists(Nodes, M, Components, o);
							if (aux == null)
								NewNodes.add(M);
							else
								NewNodes.add(aux);
						}
						//else
							//AlreadyExists(Nodes, M, Components, o);
						if (dbg) o.outln("Moved");
					}
				}
				if (dbg) o.outln("Size of NewNodes = " + NewNodes.size());

				Iterator ItNewNodes = NewNodes.iterator();
				while (ItNewNodes.hasNext()) {
					Node M = (Node) ItNewNodes.next();

					if (!Nodes.contains(M))  {
						Nodes.add(M);
						if (dbg) M.print(o);
						MaxHistoryLength = addToRemaining(Remaining, M, MaxHistoryLength);
					}
					numConts++;
					AddTransition (Transitions, N, M, c, M.getLocation(c), o);
				}
			}
			//if (numConts == 0)   {
			//	if (dbg2) o.outln("Deadlock state");
			//	if (dbg2) N.print(o);
			//	if (dbg2) return Init;
			//}
			RemoveFromRemaining(N, Remaining, MaxHistoryLength);
			N = getNextFromRemaining(Remaining, MaxHistoryLength);
		}
		if (benchmark) {
			BenchOut.close();
			fout.close();
		}
		return Init;
	}
    
	private Node AlreadyExists(Set Nodes, Node ShortM, Map Components, LTSOutput o)  {
		boolean dbg = false;//M.relevant();
		if (dbg) o.outln("");
		if (dbg) o.outln("!!!Looking for replacement of :" + ShortM.Id);
		if (dbg) ShortM.print(o);
/*		Node ShortM = M.eliminateLoop(o);
		if (ShortM ==null)   {
			if (dbg) o.outln(",false");
			ShortM = M;
		}
		else  {
			if (dbg) o.outln(",true");
			if (dbg) o.outln("eliminated loop:");
			if (dbg) ShortM.print(o);
		}
*/
		Iterator I = Nodes.iterator();
		while (I.hasNext())  {
			Node N = (Node) I.next();
			if (ShortM.Equals(N))
				return N;
		}

/*		if (ShortM != M)  {
			o.outln("Fatal Error: Node with loop but with no equivalent!");
			M.print(o);
			ShortM.print(o);
			//throw new Error();
		}
*/
		return null;
	}


	private void RemoveFromRemaining(Node N, Map Remaining, int MaxHistoryLength)  {
		for (int i = 0; i <=MaxHistoryLength;i++)  {
			Integer IntI = new Integer(i);
			if (Remaining.keySet().contains(IntI))  {
				Set RemSet = (Set) Remaining.get(IntI);
				if (RemSet.contains(N))  {
					RemSet.remove(N);
					return;
				}
			}
		}
		return;
	}

	private int addToRemaining(Map Remaining, Node N, int MaxHistoryLength)  {
		int Size = N.SizeOfDestiny();
		Integer IntSize = new Integer(Size);
		Set RemSet = null;
		if (Remaining.keySet().contains(IntSize))
			RemSet = (Set) Remaining.get(IntSize);
		else  {
			RemSet = new HashSet();
			if (MaxHistoryLength < Size)
				MaxHistoryLength = Size;
			Remaining.put(IntSize, RemSet);
		}
		RemSet.add(N);

		return MaxHistoryLength;

	}

	private Node getNextFromRemaining(Map Remaining, int MaxHistoryLength)  {
		for (int i = 0; i <=MaxHistoryLength;i++)  {
			Integer IntI = new Integer(i);
			if (Remaining.keySet().contains(IntI))  {
				Set RemSet = (Set) Remaining.get(IntI);
				Iterator It = RemSet.iterator();
				if (It.hasNext())
					return (Node) It.next();
			}
		}
		return null;
	}










	private void AddTransition (Map T, Node N, Node M, int c, BasicMSC b, LTSOutput o)  {
		if (!T.keySet().contains(N))
			T.put(N, new HashSet());
		if (!T.keySet().contains(M))
				T.put(M, new HashSet());

		Set S = (Set) T.get(N);
		Vector v = new Vector(4);

		v.add(0,new Integer(c));
		v.add(1,b);
		v.add(2,M);
		S.add(v);

		//if (dbg) o.outln("Transition " + v.get(0) + " to " + v.get(1));
	}

















}
