package synthesis;

import ic.doc.ltsa.common.iface.LTSOutput;
import java.util.*;

public class ImpliedScenarioSynthesiser {

	private LTSOutput o;
	private	Specification S = null;

	public ImpliedScenarioSynthesiser(LTSOutput _o) {
		o = _o;
	}

	public String run(mscedit.Specification XMLSpec) {

		boolean dbg = false;
		SpecificationLoader SL = new SpecificationLoader ();

		try {
			S = SL.getSpecification(XMLSpec, o);
			if (dbg) o.outln("Specification Loaded!");
		} catch (Exception e) {
			o.outln("Error loading specification");
			return null;
		}

		StringBuffer FSPOut = new StringBuffer();

		try {
			synthesiseEverything(S, FSPOut, o);
			return FSPOut.toString();
		} catch (Exception e) {
			o.outln(e.toString());
			o.outln("Error synthesising FSP specification");
			return null;
		}


	}


	public void synthesiseEverything(Specification S, StringBuffer FSPOut, LTSOutput ErrorOut)throws Exception {


		if (S.containsConditions())
			throw new Exception("State labels are not allowed!");

		MyOutput Output = new MyOutput(FSPOut);
		long start;


		start = System.currentTimeMillis();
		Output.println("//------------------------------------------------------------------");
		Output.println("//--------------------------- Coordinator --------------------------");
		Output.println("//------------------------------------------------------------------");

		ControllerSynthesiser controllerGen = new ControllerSynthesiser();
		int nodes = controllerGen.synthesise(S, Output, "Coordinator", ErrorOut);

	  	ErrorOut.outln("Coordinator model synthesis time: " + (System.currentTimeMillis() - start) + "ms. Nodes: " + nodes);

		start = System.currentTimeMillis();
		Output.println("//------------------------------------------------------------------");
		Output.println("//----------------------- Constraint Model -------------------------");
		Output.println("//------------------------------------------------------------------");


		ConstraintSynthesiser constraintGen = new ConstraintSynthesiser();
		constraintGen.printConstraints(Output, S, "ConstraintModel", ErrorOut);

		ErrorOut.outln("Constraint model synthesis time: " + (System.currentTimeMillis() - start) + "ms.");


		start = System.currentTimeMillis();
		Output.println("//------------------------------------------------------------------");
		Output.println("//------------------------ Architecture Model ----------------------");
		Output.println("//------------------------------------------------------------------");
		S.AddScenarioMessages();
		//System.out.println("ImpliedScenariosSynthesiser.synthesiseEverything: Added ScenarioMessages");
		Set ScenarioMessages = S.getScenarioMessages();
		//Output.println("ImpliedScenariosSynthesiser.synthesiseEverything: Got Scenario Messages");
		String Hide = BuildHide(ScenarioMessages );

		//First synthesis model with Coordination actions
		ImplementationSynthesiser gen = new ImplementationSynthesiser();
		gen.synthesise(S, Output, "WCoordAct", "ComponentsWCoordAct", ErrorOut);
		//Output.println("ImpliedScenariosSynthesiser.synthesiseEverything: gen.synthesise()");
		//Build model without cooridinatrion actions (hide and compose)
		String parallelComposition = "||ArchitectureModel = (";
		boolean first = true;
		Iterator I = S.components().iterator();
		while (I.hasNext()) {
			String name = (String) I.next();
			FSPLabel l = new FSPLabel();
			l.setComponentLabel(name);
			Output.println("deterministic ||" + l.getLabel() + " = " + l.getLabel() + "WCoordAct\\{" + Hide + "}.");
			if (!first)
				parallelComposition = parallelComposition + " || ";
			else
				first = false;
			FSPLabel Fl = new FSPLabel();
			Fl.setComponentLabel(name);
			parallelComposition = parallelComposition + Fl.getLabel();
		}
		parallelComposition = parallelComposition + ").";
		Output.println(parallelComposition);
		Output.println("");

		//Remove Coordination Actions
		S.RemoveScenarioMessages();

		ErrorOut.outln("Architecture behaviour model synthesis time: " + (System.currentTimeMillis() - start) + "ms.");
		Output.println("//------------------------------------------------------------------");
		Output.println("//----------------------------- Trace Model ------------------------");
		Output.println("//------------------------------------------------------------------");


		Output.println("deterministic ||TraceModel = (ComponentsWCoordAct || Coordinator) \\{" + Hide + "}.");

		Output.println("//------------------------------------------------------------------");
		Output.println("//---------------------- Properties & Checks -----------------------");
		Output.println("//------------------------------------------------------------------");

		Output.println("||ConstrainedArchitectureModel = (ArchitectureModel || ConstraintModel).");


		Output.println("property ||PTraceModel= TraceModel.");
		Output.println("property ||PConstraintModel = ConstraintModel.");

		Output.println("||ConsistencyCheck = (TraceModel|| PConstraintModel).");
		Output.println("||ImpliedScenarioCheck = (PTraceModel || ConstrainedArchitectureModel).");


	}

	private String BuildHide(Set Messages)  {
		String S = "";
		Iterator I=Messages.iterator();
		while (I.hasNext())  {
			S = S + (String) I.next();
			if (I.hasNext())
				S = S + ",";
		}

		return S;
	}

	public Set getMessageLabels(String p_name , mscedit.Specification p_spec , LTSOutput p_err) {
		SpecificationLoader SL = new SpecificationLoader ();
		try {
			S = SL.getSpecification(p_spec, p_err);

		} catch (Exception e) {
			p_err.outln("Error loading specification");
			return null;
		}

		StringSet Msgs = new StringSet();

		Iterator I = S.components().iterator();
		while (I.hasNext()) {
			String instanceName= (String) I.next();
			int ColonPosition = instanceName.indexOf(":");
			if ((ColonPosition < 0 && instanceName.equals(p_name)) ||	(instanceName.substring(ColonPosition+1).equals(p_name))) {
				//p_err.outln("Component:" + p_name + " Instance = " +instanceName);
				Map Instances = new HashMap();
				try {
					Instances = S.getComponentInstances(instanceName);
				} catch (Exception e) {
					p_err.outln("Error with Component name:" + instanceName);
				}
				Iterator J = Instances.keySet().iterator();
				while (J.hasNext()) {
					String bMSC = (String) J.next();
					Instance Inst = (Instance) Instances.get(bMSC);
					Msgs.addAll(Inst.getAlphabet());
				}
			}
		}
		//p_err.outln("Messages of componenent " + p_name);
		//Msgs.print(p_err);
		return Msgs;
	}

	//PortNames: String->String (msclabel -> portname)
	public String getFSPforComponent( String p_name , mscedit.Specification p_spec , LTSOutput p_err , Map PortNames) {


		SpecificationLoader SL = new SpecificationLoader ();

		try {
			S = SL.getSpecification(p_spec, p_err);

		} catch (Exception e) {
			p_err.outln("Error loading specification");
			return null;
		}

		StringBuffer x_fsp = new StringBuffer();
		MyOutput Output = new MyOutput(x_fsp);

		ImplementationSynthesiser gen = new ImplementationSynthesiser();
		try{
			gen.printComponentClass( S, p_name, "", Output, p_err, PortNames );
		} catch ( Exception p_e ) { p_err.outln( p_e.getMessage() ); }

		return x_fsp.toString();
    }
}


