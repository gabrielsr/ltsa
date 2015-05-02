package synthesis;

import mscedit.*;
//import java.io.*;
import java.util.*;
import java.lang.Exception;
import ic.doc.ltsa.common.iface.LTSOutput;

public class SpecificationLoader extends Visitor {
	private boolean dbg = false;

	private Specification S;
	private BasicMSC B;
	private boolean buildingNeg=false;
	private Instance I;
	private LTSOutput o;
	private Set<Vector> Txs;
	boolean Error = false;
	String ErrorMSG = "DefaultError";


	public Specification getSpecification (mscedit.Specification Spec, LTSOutput out) throws Exception {
		o = out;

		try {
			Spec.apply(this);
		}
		catch(Exception e) {
			o.outln("Error Visiting MSC Specification");
			throw new Exception(e);
		}
		if (Error) {
			o.outln(ErrorMSG);
			throw new Exception();
		}
		if (dbg) o.outln("Succesful parse");
		
		
		S.addMissingComponents();
		
		try {
			S.checkConsistency();
		} catch (Exception e) {
			o.outln(e.toString());
			throw new Exception(e);
		}
		if (dbg) o.outln("Succesful check");
		S.eliminateEmptyScenarios(o);
		if (dbg) o.outln("Succesful elimination");
		if (dbg) S.print(o);
		return S;
	}

	public void inASpecification(mscedit.Specification node) {
		S = new Specification();
		Txs = new HashSet();
	}

	public void outASpecification(mscedit.Specification node) {
//		boolean moreThanOne = false;
		//Check if it has more than one initial bMSC
/*		{
			Iterator I = Txs.iterator();
			int Count = 0;
			while (I.hasNext()) {
				Vector v = (Vector) I.next();
				String From = (String) v.get(0);	
				if (From.equals("init")) 
					Count++;
			}
			moreThanOne = (Count>1);
		}
*/		
		BasicMSC Init=null;
//		if (moreThanOne) {
			Init = new BasicMSC();
			if (S.getbMSCs().size()>0)
				Init.copyComponents((BasicMSC) S.getbMSCs().iterator().next());
			Init.name = "Init";
			S.addbMSC(Init);
			S.addRelationInit(Init);
//		}
		
		Iterator I = Txs.iterator();
		while (I.hasNext()) {
			Vector v = (Vector) I.next();
			
			BasicMSC From, To;
			String SFrom, STo, SWeight;
			SFrom = (String) v.get(0);
			STo = (String) v.get(1);
			SWeight = "";
			//obtain probability value for transition, if present.
			boolean prob = false;
			if (v.size() == 3){
				SWeight = (String) v.get(2);
				prob = true;
			}
//			if (STo.equals("Final")) {
//				try {
//					From = S.getBMsc(SFrom);
//					S.addRelationFinal(From);
//					} catch (Exception e) {
//						ErrorMSG = "bMSC '" + SFrom + "' appears in hMSC but has not been defined.";
//						Error = true;
//						return;
//					}
//			}
//			else {
			if (SFrom.equals("init") && STo.equals("init")) {
				//Loop on initial symbol: Ignore.
			} else if (SFrom.equals("init") && !STo.equals("init")) {
				try {
					To = S.getBMsc(STo);
				} catch (Exception e) {
					ErrorMSG = "bMSC '" + STo + "' appears in hMSC but has not been defined.";
					Error = true;
					return;
				}
//					if (!moreThanOne) 
//						S.addRelationInit(To);
//					else
					if (!prob)
						S.addRelation(Init, To);
					else
						S.addProbabilisticRelation(Init, To, SWeight, prob);
					
				} else if (!SFrom.equals("init") && STo.equals("init")) {
				try {
					From = S.getBMsc(SFrom);
				} catch (Exception e) {
					ErrorMSG = "bMSC '" + SFrom + "' appears in hMSC but has not been defined.";
					Error = true;
					return;
				}
//					if (!moreThanOne) 
//						S.addRelationInit(To);
//					else
				if (!prob)
					S.addRelation(From, Init);
				else
					S.addProbabilisticRelation(From, Init, SWeight, prob);
				}
			else { //Neither are "init"
				try {
					From = S.getBMsc(SFrom);
				} catch (Exception e) {
					ErrorMSG = "bMSC '" + SFrom + "' appears in hMSC but has not been defined.";
					Error = true;
					return;
				}
				try {
					To = S.getBMsc(STo);
					if (!prob)
						S.addRelation(From, To);
					else
						S.addProbabilisticRelation(From, To, SWeight, prob);
				} catch (Exception e) {
					ErrorMSG = "bMSC '" + STo + "' appears in hMSC but has not been defined.";
					Error = true;
					return;
				}
			}
		} //end while	
	} //end method

  	public void inABMSC(mscedit.BMSC node) {
		B = new BasicMSC();
  	}


	public void outABMSC(mscedit.BMSC node) {
		B.name = node.getName();
		if (!buildingNeg) {
			if (!S.containsBMsc(B.name) && !B.name.equals("init"))
				S.addbMSC(B);
			else {
				if (!B.name.equals("init")) {
					ErrorMSG = "bMSC '" + B.name + "' has been defined twice.";
					Error = true;
				}
			}
		}
	}

	public void inAInstance(mscedit.Instance node) {
	    I = new Instance();
	}

  	public void outAInstance(mscedit.Instance node) {
		B.addInstance(node.getName(), I);
	  }

	public void outAInput(mscedit.Input node) {
		InputEvent e = new InputEvent (node.getName());
		e.setFrom(node.getFrom());
		//Add weight to the event in case it's weighted
		if (node.hasWeight()){
			String eWeight = String.valueOf(node.getWeight());
			e.setWeight(eWeight);
		}
		I.appendEvent(e);
  	}

	public void outAOutput(mscedit.Output node) {
		OutputEvent e = new OutputEvent(node.getName());
		e.setTo(node.getTo());
		//Add weight to the event in case it's weighted
		//if (node.hasWeight()){
		//	String eWeight = String.valueOf(node.getWeight());
		//	e.setWeight(eWeight);
		//}
		I.appendEvent(e);
	}
	
	public void outATransition(mscedit.Transition node) {
		BasicMSC From, To;
		String SFrom, STo;
		SFrom = node.getFrom();
		STo = node.getTo();
		Vector v = new Vector();
		v.add(0, SFrom);
		v.add(1, STo);
		Txs.add(v);
		
	}
	
	@Override
	public void outAProbabilisticTransition(ProbabilisticTransition node) {
		BasicMSC From, To;
		String SFrom, STo, SWeight;
		SFrom = node.getFrom();
		STo = node.getTo();
		SWeight = String.valueOf(node.getWeight());
		//System.out.println("From: "+SFrom+" To: "+STo+" Weight: "+SWeight);
		Vector<String> v = new Vector<String>();
		v.add(0, SFrom);
		v.add(1, STo);
		v.add(2, SWeight);
		Txs.add(v);
	}
	
	public void inANegativeBMSC(mscedit.BMSC node) {
		buildingNeg = true;
  	}

  	public void outANegativeBMSC(mscedit.BMSC node) {
  		buildingNeg = false;
  		String proscribed = node.getNegativeLink();
		String lbl = B.name;
		if (!S.containsBMsc(lbl))
			S.addBasicNegbMSC(lbl, B, proscribed);
		else {
			ErrorMSG = "bMSC '" + B.name + "' has been defined twice.";
			Error = true;
		}
	}
	
	
}


	/*


	private BasicMSC W;
	private BasicMSC U;
	private BasicMSC P;


	  public void inAScopedmsc (AScopedmsc node) {
	  	B = new BasicMSC();
	  }

	  public void outAScopedmsc (AScopedmsc node) {
	  	if (W==null)  {
			W = B;
			WScope = Alpha;
	  	}
		else  {
			U = B;
			UScope = Alpha;
		}
  	}

	private String disallows;
	private StringSet Alpha;
	private StringSet WScope;
	private StringSet UScope;


  public void outADisallows (ADisallows node) {
  	disallows = node.getAlbl().getText();
  }


  public void inAAllScope (AAllScope node) {
  	Alpha=null;
  }

  public void inASomeScope (ASomeScope node) {
  	Alpha=new StringSet();
  }

  public void inAEmptyScope (AEmptyScope node) {
  	Alpha=new StringSet();
  }

  public void outAAlphabet (AAlphabet node) {
  	Alpha.add(node.getAlbl().getText());
  }

  public void outAAlphabetTail (AAlphabetTail node) {
  	Alpha.add(node.getAlbl().getText());
  }

  public void outAUnnamedmsc(AUnnamedmsc node) {
		B.name = "precondition";
	}

   public void inAUnnamedmsc(AUnnamedmsc node) {
		B = new BasicMSC();;
   }



  public void inAWhenNegmscs(AWhenNegmscs node) {
  		U=null;
		W=null;
		UScope=null;
		WScope=null;
  }


 
  public void inAWhenuntilNegmscs(AWhenuntilNegmscs node) {
  		U=null;
  		W=null;
		UScope=null;
  		WScope=null;
  }

 public void inABasicNegmscs(ABasicNegmscs node) {
	B = new BasicMSC();
  }

  public void outABasicNegmscs(ABasicNegmscs node) {
		if (dbg) o.outln("in_outABasicNegmscs");
		String lbl = (node.getLbl().getText());
		if (!S.containsBMsc(lbl))
			S.addBasicNegbMSC(lbl, B, disallows);
		else {
			E = new bMSCDefinedTwice("bMSC '" + lbl + "' has been defined twice.");
			error = true;
		}
		if (dbg) o.outln("out_outANegmscs");
	}


  public void outAWhenNegmscs(AWhenNegmscs node) {
		if (dbg) o.outln("in_outANegmscs");
		String lbl = (node.getLbl().getText());
		if (!S.containsBMsc(lbl))
			S.addNegbMSC(lbl, W, WScope, disallows, null, null);
		else {
			E = new bMSCDefinedTwice("bMSC '" + lbl + "' has been defined twice.");
			error = true;
		}
		if (dbg) o.outln("out_outANegmscs");
	}

  public void outAWhenuntilNegmscs(AWhenuntilNegmscs node) {
		if (dbg) o.outln("in_outANegmscs");
		String lbl = (node.getLbl().getText());
		if (!S.containsBMsc(lbl))
			S.addNegbMSC(lbl, W, WScope, disallows, U, UScope);
		else {
			E = new bMSCDefinedTwice("bMSC '" + lbl + "' has been defined twice.");
			error = true;
		}
		if (dbg) o.outln("out_outANegmscs");
	}


  public void outAStateEvt(AStateEvt node) {
    I.appendEvent(new ConditionEvent (node.getLbl().getText()));
  }

	*/
