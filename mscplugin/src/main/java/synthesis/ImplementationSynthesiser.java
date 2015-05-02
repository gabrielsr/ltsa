package synthesis;

import ic.doc.ltsa.common.iface.LTSOutput;
import java.util.*;



public class ImplementationSynthesiser {

	static final String SYSNAME = "Implementation Synthesiser";
	static final String SYSVERSION = "v1.4";
	static final String TAU = "internalAction";


	//---------------------------------------------------------------------
	public void synthesise(Specification S, MyOutput Output, String ComponentSuffix, String SystemName, LTSOutput o) throws Exception {
		//Set P;
		String name;
		String parallelComposition = "||" + SystemName +" = (";
		boolean first = true;
		//StringRelation CR, R;
		Iterator Components = S.components().iterator();
		//o.outln("O");
		FSPLabel l = new FSPLabel();
		try {
			while (Components.hasNext()) {
				name = (String) Components.next();
				if (!first)
					parallelComposition = parallelComposition + " || ";
				else
					first = false;
				l.setComponentLabel(name);
				parallelComposition = parallelComposition + l.getLabel() + ComponentSuffix;
				printComponentInstance(S, name, ComponentSuffix, Output, o);
			}
			parallelComposition = parallelComposition + ").";
			//System.out.println("ImplementationSynthesiser.synthesise: parallelComposition = "+parallelComposition);
			Output.println(parallelComposition);
			Output.println("");

		} catch (Exception e) {
			o.outln(e.toString());
			throw new Exception("Inconsistent Spec");
		}
	}


	public void printComponentClass(Specification S, String className, String ComponentSuffix, MyOutput Output, LTSOutput o, Map PortNames) throws Exception  {

		String Composition = "";
		FSPLabel Fl = new FSPLabel();
		Fl.setComponentLabel(className);

		StringBuffer LocalBuffer = new StringBuffer();
		MyOutput LocalOutput = new MyOutput(LocalBuffer);

		StringBuffer HMSCBuffer = new StringBuffer();
		MyOutput HMSCOutput = new MyOutput(HMSCBuffer);

		boolean found = false;
		Iterator Components = S.components().iterator();
		if (Components.hasNext()) {


			HMSCOutput.println( Fl.getLabel() + "_ND = InitialNode,");

			while (Components.hasNext()) {
				String currName= (String) Components.next();
				String currClassName = new String("");
				String currInstanceName = new String("");

				int ColonPosition = currName.indexOf(":");

				if (ColonPosition > 0) {
					currInstanceName = currName.substring(0, ColonPosition);
					currClassName = currName.substring(ColonPosition+1);
				}
				else {
					currInstanceName = new String(currName);
					currClassName = new String(currName);
				}


				if (currClassName.equals(className)) {
					if (Composition.length()!=0)
						Composition = Composition + " | ";
					
					if (!S.getProbabilistic())
						Composition = Composition + "internalAction -> " + printComponent(S, currName, ComponentSuffix, false, LocalOutput, HMSCOutput, o, PortNames, currInstanceName);
					else
						Composition = Composition + "internalAction -> " + printProbabilisticComponent(S, currName, ComponentSuffix, false, LocalOutput, HMSCOutput, o, PortNames, currInstanceName);
					found = true;
				}
			}

			HMSCOutput.println("InitialNode = (" + Composition + ").");

			HMSCOutput.append(LocalBuffer);

			HMSCOutput.println("deterministic ||" + Fl.getLabel() + ComponentSuffix + " = " + Fl.getLabel() + "_ND" + "\\{internalAction}.");
			//HMSCOutput.println(" ||" + Fl.getLabel() + ComponentSuffix + "_notDet = " + Fl.getLabel() + ComponentSuffix+".");

			if (found)
				Output.append(HMSCBuffer);

		}
	}


	public void printComponentInstance(Specification S, String name, String ComponentSuffix, MyOutput Output, LTSOutput o) throws Exception  {

		String Init ="";
		if (!S.getProbabilistic())
			Init = printComponent(S, name, ComponentSuffix, true, Output, Output, o, null, null);
		else
			Init = printProbabilisticComponent(S, name, ComponentSuffix, true, Output, Output, o, null, null);
		
		FSPLabel Fl = new FSPLabel();
		Fl.setComponentLabel(name);
		Output.println( "deterministic ||" + Fl.getLabel() + ComponentSuffix + " = " + Init + "\\{internalAction}.");
		//Output.println( "||" + Fl.getLabel() + ComponentSuffix + "_nonDet = " + Fl.getLabel() + ComponentSuffix + ".");

	}

	private String printComponent(Specification S, String name, String ComponentSuffix, boolean Instance, MyOutput LocalOutput, MyOutput Output, LTSOutput o, Map PortNames, String instanceName) throws Exception  {
		boolean first;
		boolean hasFinal = false;
		boolean hasTau= false;
		boolean dbg = false;

		String ClassInitialNode = null;

		if (!Instance)
			ClassInitialNode = "InitialNode";


		int TOTALTABS = 6;
		String TABS = "\t\t\t\t\t\t";
		int TABLENGTH = 4;

		if (dbg) o.outln("// String -> Instances: bMSC name and corresponding component instance in bMSC.");
		Map Instances;
		Instances = S.getComponentInstances(name);

		FSPLabel l = new FSPLabel();
		if (dbg) o.outln("//Process header");
			l.setComponentLabel(name);
			LocalOutput.println("////Component  " + l.getLabel());


		if (dbg) o.outln("//Start local processes.");
			Iterator I = Instances.keySet().iterator();
			while (I.hasNext()) {
				String bMSC = (String) I.next();
				Instance Inst = (Instance) Instances.get(bMSC);

				l.setComponentLabel(name);
				String cOutput = l.getLabel() + "_" + bMSC + " = ";
				LocalOutput.print(cOutput);
				//System.out.println("ImplementationSynthesiser.printComponent: cOutput => "+cOutput);
				if (Inst.size() != 0)  {
					LocalOutput.print("(");
					for (int p = 0; p<Inst.size();p++)  {
						Event e = (Event) Inst.get(p);
						//o.outln("setMessageLabel: " + e.getLabel() + " , " + instanceName);
						l.setMessageLabel(e.getLabel(), PortNames, instanceName);
						cOutput = l.getLabel();
						LocalOutput.print(cOutput);
						//System.out.println("ImplementationSynthesiser.printComponent: l.getLabel() =>  "+cOutput);
						LocalOutput.print(" -> ");
					}
					LocalOutput.println("END).");
				} else {
					LocalOutput.println("END.");
				}
			}
		if (dbg) o.outln("//End local processes.");
		int max = 0;
		if (dbg) o.outln("//Start HMSC.");
			//Create Ids for bMSCs
			Map bMSC_To_Ids= new HashMap();
			Vector Ids_To_bMSCs = new Vector();
			Ids_To_bMSCs.setSize(S.getbMSCs().size()+1);

			Set Inits = new HashSet();
			Iterator J = S.getbMSCs().iterator();
//			int a = 1;
//			boolean foundInit = false;
			int Id =0;
			while (J.hasNext())  {
				BasicMSC b = (BasicMSC) J.next();

//				if (S.getContinuationsInit().contains(b)) {
//					Id = 0;
//					if (foundInit)
//						throw new Exception("Two initial nodes!");
//					else
//						foundInit = true;
//				} else {
//					Id=a;
//					a++;
//				}
				bMSC_To_Ids.put(b, new Integer(Id));
				Ids_To_bMSCs.set(Id, b);
				if (S.getContinuationsInit().contains(b)) {
					Inits.add(new Integer(Id));
				}
				Id++;
//				max = a;
			}

//			Output.print("HMSC_"+name + " = N0");
			boolean firstInit = true;
			l.setComponentLabel(name);
			Output.print("HMSC_"+l.getLabel()+ " = (");
			J = Inits.iterator();
			while (J.hasNext()) {
				if (!firstInit)
					Output.print(" | ");
				else
					firstInit = false;
				String internalTrans = TAU + " -> "+ l.getLabel() + "_N" + J.next().toString();
			    //System.out.println("ImplementationSynthesiser.printComponent: Internal TAU => "+internalTrans);
				Output.print(internalTrans);
			}

			Output.print(")");

			for (int i=0;i<Id;i++) {
				String bName = ((BasicMSC) Ids_To_bMSCs.get(i)).name;

				Output.println(",");

				l.setComponentLabel(name);
				Output.print(l.getLabel() + "_N" + i + " = " + l.getLabel() + "_" + bName + ";"+ l.getLabel() + "_N" + i + "_Adj");
			}

			for (int i=0;i<Id;i++) {
				Output.println(",");
				Output.print(l.getLabel() +"_N" + i + "_Adj = (");

				Set Conts = S.getContinuations((BasicMSC) Ids_To_bMSCs.get(i));
				if (Conts.size() == 0)
					Output.print("endAction -> END)");
				else  {
					Iterator K = Conts.iterator();
					first=true;
					while (K.hasNext()) {
						if (first)
							first = false;
						else
							Output.print(" | ");
						Integer num = (Integer) bMSC_To_Ids.get((BasicMSC) K.next());
						if (num.intValue() != 0 || Instance){
							String internalTrans = TAU + " -> "+  l.getLabel() +"_N" + num;
						    //System.out.println("ImplementationSynthesiser.printComponent: Internal TAU => "+internalTrans);
							Output.print(internalTrans);
						}
						else
							Output.print(TAU + " -> " + ClassInitialNode);

					}
					Output.print(")");
				}
			}

			if (Instance)
				Output.println(".");
			else
				Output.println(",");

			l.setComponentLabel(name);
			return "HMSC_" + l.getLabel();
	}


	//TODO: This is the method we should add the probability values -- Gena’na.
	private String printProbabilisticComponent(Specification S, String name, String ComponentSuffix, boolean Instance, MyOutput LocalOutput, MyOutput Output, LTSOutput o, Map PortNames, String instanceName) throws Exception  {
		boolean first;
		boolean hasFinal = false;
		boolean hasTau= false;
		boolean dbg = false;

		String ClassInitialNode = null;

		if (!Instance)
			ClassInitialNode = "InitialNode";


		int TOTALTABS = 6;
		String TABS = "\t\t\t\t\t\t";
		int TABLENGTH = 4;

		if (dbg) o.outln("// String -> Instances: bMSC name and corresponding component instance in bMSC.");
		Map Instances;
		Instances = S.getComponentInstances(name);

		FSPLabel l = new FSPLabel();
		if (dbg) o.outln("//Process header");
			l.setComponentLabel(name);
			LocalOutput.println("////Component  " + l.getLabel());
         
		//Alternative substitution for Java 1.5 to Iterate over Instances
		//	for(Map.Entry i : Instances.entrySet())
		//		Instance inst = i.getValue();
			
		if (dbg) o.outln("//Start local processes.");
			Iterator I = Instances.keySet().iterator();
			while (I.hasNext()) 
			{
				String bMSC = (String) I.next();
				Instance Inst = (Instance) Instances.get(bMSC);
				// debugging
				//System.out.print("ImplementationSynthesiser.printProbabilisticComponent ->  ");
				//Inst.outPrint(LocalOutput);

				l.setComponentLabel(name);
//				start building FSP for the component related to the bMSC
				String compLabel = l.getLabel() + "_" + bMSC;
				String cOutput = compLabel + " = "+ compLabel + "_Q0,";
				LocalOutput.println(cOutput);
				if (Inst.size() != 0)  {
					//LocalOutput.print("(");
					
					// In here, we should add the probability values to the component msg and to the correspondent error -> ERROR transition.
					// For example: 
					//Comp2_Initial = Comp2_InitialQ0, 
					//Comp2_InitialQ0 = (s_Comp2_Initial -> Comp2_InitialQ1), 
					//Comp2_InitialQ1 = ( (0.7) comp1.comp2.msg1 -> Comp2_InitialQ2 
					//                  | (0.3) comp1.comp2.msg1 -> error -> ERROR),
					//Comp2_InitialQ2 = (comp2.comp3.msgToComp3 -> Comp2_InitialQ3),
					//Comp2_InitialQ3 = (comp2.comp1.msgToComp1 -> END).

					for (int p = 0; p<Inst.size();p++) {
						Event e = (Event) Inst.get(p);
						//o.outln("setMessageLabel: " + e.getLabel() + " , " + instanceName);
						//System.out.print("ImplementationSynthesiser.printProbabilisticComponent: e.getLabel() = "+ e.getLabel());
						//System.out.println(" instanceName = "+instanceName);
						
						l.setMessageLabel(e.getLabel(), PortNames, instanceName);
						
						String weightOutput = "";
						String errorTrans = "";
						if (((e instanceof OutputEvent)) || ((e instanceof InputEvent))){
							//System.out.println("ImplementationSynthesiser.printProbabilisticComponent: e.getWeight = "+((MessageEvent) e).getWeight());
						    weightOutput = ((MessageEvent)e).getWeight();
							if (!(weightOutput.equals(""))){
								double aux = 1 - Double.parseDouble(weightOutput);
						    		if (aux > 0.0)
						    			//errorTrans = TABS+"| ("+aux+") "+l.getLabel()+" -> error_"+compLabel+" -> ERROR";
						    			errorTrans = TABS+"| ("+aux+") error_"+l.getLabel()+" -> ERROR";
						    		weightOutput = " ("+((MessageEvent)e).getWeight()+") ";
						    		//System.out.println("ImplementationSynthesiser.printProbabilisticComponent: e.getWeight = "+weightOutput);
						    }
						}
	
						int next = p+1;
						cOutput = compLabel + "_Q"+ p + " = (" + weightOutput + l.getLabel()+" -> "+compLabel+"_Q"+next;
						if (!errorTrans.equals(""))
							cOutput = cOutput+"\n"+errorTrans;
						cOutput = cOutput + "),";
						LocalOutput.println(cOutput);
						// if in the last element of the Component Events
						if (p == Inst.size()-1)
							LocalOutput.print(compLabel+"_Q"+next+" = ");
						//cOutput = l.getLabel();
						//LocalOutput.print(cOutput);
						//System.out.println("ImplementationSynthesiser.printProbabilisticComponent: l.getLabel() =>  "+cOutput);
						//LocalOutput.print(" -> ");
					}
					//LocalOutput.println(" END.");
				}// else {
				LocalOutput.println(" END.");
				//}
			}
		if (dbg) o.outln("//End local processes.");
		//int max = 0;
		if (dbg) o.outln("//Start HMSC.");
			//Create Ids for bMSCs
			Map bMSC_To_Ids= new HashMap(); //The bMSC to which the transitions goes
			Vector Ids_To_bMSCs = new Vector(); // The bMSC from which the transition starts
			Ids_To_bMSCs.setSize(S.getbMSCs().size()+1);

			//Vector Ids_To_probBMSCs = new Vector();

			
			Set Inits = new HashSet();
			Iterator J = S.getbMSCs().iterator();
			
//			int a = 1;
//			boolean foundInit = false;
			int Id =0;
			
			// populate the HashMap bMSC_To_Ids and the Vector Ids_To_bMSCs
			while (J.hasNext())  {
				BasicMSC b = (BasicMSC) J.next();
				
//				if (S.getContinuationsInit().contains(b)) {
//					Id = 0;
//					if (foundInit)
//						throw new Exception("Two initial nodes!");
//					else
//						foundInit = true;
//				} else {
//					Id=a;
//					a++;
//				}
				bMSC_To_Ids.put(b, new Integer(Id));
				Ids_To_bMSCs.set(Id, b);
				//if the BMSC b is a initial BMSC, add to Inits
				if (S.getContinuationsInit().contains(b)) {
					Inits.add(new Integer(Id));
				}
				Id++;
//				max = a;
			}

//			Output.print("HMSC_"+name + " = N0");
			boolean firstInit = true;

			//start building FSP for the component related to the HMSC
			l.setComponentLabel(name);
			Output.print("HMSC_"+l.getLabel()+ " = (");
			//Iterate over from initial transitions to the bMSC
			J = Inits.iterator();
			while (J.hasNext()) {
				if (!firstInit)
					Output.print(" | ");
				else
					firstInit = false;
				
				//Here we get the transition probability for the corresponding label
				Integer IntId = ((Integer)J.next());
				//Get Transitions from Init to current BMSC
				BasicMSC b = (BasicMSC)Ids_To_bMSCs.get(IntId.intValue());
				//Obtain transition probability from Init to current BMSC
				String transVal = S.getTransProbability("Init", b);
				String sLabel = l.getLabel();
				String sId = IntId.toString();
				//Print the FSP transition with weight annotated in the transition
				String internalTrans = " ("+transVal+") " + TAU + " -> "+ sLabel + "_N" + sId;
				Output.print(internalTrans);
			}

			Output.print(")");
			
			//Build what ends up as the WCoordAct of the actions for the component in two loops:
			
			// The first loop to get the constructions of bMSCs for the component, for instance:
			//Actuator_N0 = Actuator_End;Actuator_N0_Adj,
			//Actuator_N1 = Actuator_Register;Actuator_N1_Adj,
			//Actuator_N2 = Actuator_Analysis;Actuator_N2_Adj,
			//Actuator_N3 = Actuator_Terminate;Actuator_N3_Adj,
			//Actuator_N4 = Actuator_Initialise;Actuator_N4_Adj,
			for (int i=0;i<Id;i++) {
				String bName = ((BasicMSC) Ids_To_bMSCs.get(i)).name;

				Output.println(",");

				l.setComponentLabel(name);
				Output.print(l.getLabel() + "_N" + i + " = " + l.getLabel() + "_" + bName + ";"+ l.getLabel() + "_N" + i + "_Adj");
			}
			
			// The second loop to get the internal actions between scenarios (the transitions and their probabilites)
			//Actuator_N1_Adj = ( (0.7) internalAction -> Actuator_N1 |  (0.2) internalAction -> Actuator_N2 |  (0.1) internalAction -> Actuator_N3),
			//Actuator_N2_Adj = ( (1.0) internalAction -> Actuator_N1),
			//Actuator_N3_Adj = ( (0.5) internalAction -> Actuator_N0 |  (0.5) internalAction -> Actuator_N4),
			//Actuator_N4_Adj = ( (1.0) internalAction -> Actuator_N1).
			for (int i=0;i<Id;i++) {
				Output.println(",");
				Output.print(l.getLabel() +"_N" + i + "_Adj = (");
				
				BasicMSC bMSCfrom = (BasicMSC) Ids_To_bMSCs.get(i);
				Set Conts = S.getContinuations(bMSCfrom);
				
				if (Conts.size() == 0)
					Output.print("endAction -> END)");
				else  {
					Iterator K = Conts.iterator();
					first=true;
					while (K.hasNext()) {
						if (first)
							first = false;
						else
							Output.print(" | ");
						BasicMSC bMSCto = (BasicMSC) K.next();
						Integer num = (Integer) bMSC_To_Ids.get(bMSCto);
						
						//Here we also have to add transition probability values before the TAU transition
						String transVal = S.getTransProbability(bMSCfrom.name, bMSCto);
						if (num.intValue() != 0 || Instance){
							String internalTrans = " ("+transVal+") " +TAU + " -> "+  l.getLabel() +"_N" + num;
						    //System.out.println("ImplementationSynthesiser.printComponent: Internal TAU => "+internalTrans);
							Output.print(internalTrans);
						}
						else
							Output.print(TAU + " -> " + ClassInitialNode);

					}
					Output.print(")");
				}
			}

			if (Instance)
				Output.println(".");
			else
				Output.println(",");

			l.setComponentLabel(name);
			return "HMSC_" + l.getLabel();
	}




}



