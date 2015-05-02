package ic.doc.ltsa.lts;

import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.IEventState;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.lts.CompactState;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ErrorManager {

	//TODO: This method needs to be checked again after new procedure for error transitions insertion	
	public CompactState mergeErrors(CompactState machine) {
		
		
		//System.out.println("In merge error ------------------------");
		List<IEventState> newStatesList = new LinkedList<IEventState>();
		
		for(int i=0; i<machine.getMaxStates(); i++ ) {
			//double prob = 0;
			//int event = 0;
			//int nxt = 0;
			//boolean hasError = false;
			IEventState newTr = null;
			if( machine.getStates()[i]!= null ) { 
				for(Enumeration e = machine.getStates()[i].elements(); e.hasMoreElements();) {
					IEventState st = (IEventState)e.nextElement();
					//System.out.println("action --------------------------------------------"+st.getEvent());
					
					// The assignment to newTr below happens only in case the if statement below is commented out
					newTr = EventState.add(newTr,new EventState(st.getEvent(),st.getNext(),null,null,st.getProb()));	
					
					//if (st.getNext() != -1) {
					//	newTr = EventState.add(newTr,new EventState(st.getEvent(),st.getNext(),null,null,st.getProb()));		
					//} else {
					//	prob += st.getProb();
					//	hasError = true;
					//	event = st.getEvent();
					//	nxt = st.getNext();
					//}
					
					
				}
				
				//if (hasError) {
				//	newTr = EventState.add(newTr,new EventState(event,nxt,null,null,prob));
				//}
					
			}
			
			newStatesList.add(newTr);
		}
	
		
		//TODO: The part below seems to be the only part we actually need
		//      but check why we need the bits after the external if statement
		CompactState m = new CompactState();
		String compName = machine.getName();
		m.setName(machine.getName());
		m.alphabet = machine.alphabet.clone();
		if (!compName.endsWith("WCoordAct")){
			for (int i=0; i<m.alphabet.length; i++) {
	    	 		if(m.alphabet[i].matches("error_.*")) {
	    	 			//System.out.println("Matches       "+m.alphabet[i]);
	    	 			//m.alphabet[i] = "error_"+machine.getName();
	    	 			String aux = m.alphabet[i];
	    	 			m.alphabet[i] = aux.substring(6);
	    	 		}
			}
		}
	    m.maxStates = newStatesList.size();
	    m.setStates(new EventState[m.maxStates]);
	    int stateNum = 0;
	    
	    for (IEventState st : newStatesList) {
	       m.getStates()[stateNum] = st;
	       stateNum++;
	     }
	         	   
	      return m;
	}  
}
