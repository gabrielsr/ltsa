package ic.doc.ltsa.lts;
import ic.doc.ltsa.common.iface.IEventState;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.lts.CompactState;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MohriDeterminiser {
	
	CompactState machine;
	LTSOutput output;

	//-------------------------------------------------------------------------------
	// constructors

	private MohriDeterminiser(CompactState machine, LTSOutput output) {
		this.machine = machine;
		this.output = output;
	}
	
	//-------------------------------------------------------------------------------
	// main use of this class
	
	// determinise the given machine non-destructively
	// return null if the determinisation fails
	public static CompactState determinise(CompactState machine, LTSOutput output) {
		MohriDeterminiser d = new MohriDeterminiser(machine, output);
		return d.auxDeterminise();
	}
		
	//-------------------------------------------------------------------------------
	// The general idea of this method is inspired by the Power Series Determinisation
	// algorithm explained in "Finite State Transducers in Language and Speech Processing"
	// by Mehryar Mohri. 

	private CompactState auxDeterminise() {
		// efficient compact state data structure that we will be working with
		CompactForm compactForm = new CompactForm();
		// alphabet of the compact state in compact form
		int[] alphabet = compactForm.getAlphabet();
		
		// used to build the new determinised compact state
		IEventState newTr;
		List<IEventState> newStatesList = new LinkedList<IEventState>();
		
		// represents the current subset being examined
		// maps the states in subset to the corresponding weight
		SubsetMap subset;
		// represents the destination subset 
		SubsetMap destSubset;
		// maps new states to their new ID numbers
		Map<SubsetMap,MyInteger> newStates = new HashMap<SubsetMap,MyInteger>();
		// used to number the new states
		// MyInteger currSubsetID;
		// queue of subsets prior to processing
		Queue<SubsetMap> uncompletedStates = new LinkedList<SubsetMap>();
		// a counter to calculate the weight of the label
		double labelWeight;
		// holds the weight of a path
		double pathWeight;
		MyDouble tmpMyDouble;
		// indicates whether the subset has the label
		boolean hasLabel;

		/*-----------------------------------------------------------------*/
		// initialisation
		
		// create the initial subset containing the initial pair
		subset = new SubsetMap();
		subset.put(new MyInteger(0), new MyDouble(1));
		
		// record the new ID of the state
		newStates.put(subset, new MyInteger(0));
		
		// send the subset to the queue for processing
		uncompletedStates.add(subset);
		
		//MyInteger currSubsetID = new MyInteger(-1);
		/*-----------------------------------------------------------------*/
		
		boolean isErrorStateCreated = false;
		
		// while there are more subsets to be processed
		while(uncompletedStates.size() != 0) {
			
			// get the subset from the head of the queue
			subset = uncompletedStates.poll();
			
			// get the ID for the subset
			// currSubsetID.val++;
			
			// get a new clean state
			newTr = null;

			// for all labels of the compact state
			for( int i=0; i<alphabet.length; i++ ) {
				// reset the flag
				hasLabel = false;
				// reset the counter
				labelWeight = 0;
				// create a new destination subset for this label
				destSubset = new SubsetMap();
				
				// for all pairs in subset that has the label
				for( Map.Entry<MyInteger,MyDouble> pair : subset.entrySet() ) {
					 if( compactForm.contains(alphabet[i],pair.getKey().val) ) {
						hasLabel = true;
						
						// for all transition with the given state and label
						for( CompactTransition transition 
							  : compactForm.mapLabelAndFromState(alphabet[i],pair.getKey().val) ) {
	 
							// calculating the weight of the label
							pathWeight = pair.getValue().val * transition.weight;
							labelWeight += pathWeight;
							
							// calculating the weights of the states in the destination subset
							tmpMyDouble = destSubset.getOrCreate(transition.toState); 
							tmpMyDouble.val += pathWeight;
						}
					 }
				}
				
				 // subset has the current label to the destination subset
				if( hasLabel ) {
					// finalise the weights in the destination subset
					for( Map.Entry<MyInteger,MyDouble> pair : destSubset.entrySet() ) {
						pair.getValue().val /= labelWeight; 
					}
				
					MyInteger destSubsetID = null;
					destSubsetID = newStates.get(destSubset);
					//System.out.println("MohriDeterminiser.auxDeterminise: newStates.size() = " + newStates.size());
					//destSubset.print();
					//System.out.println( destSubset.isErrorState() );
					
					// if the destination subset is not processed before
					if ( destSubsetID == null && !destSubset.isErrorState() ) {
						//System.out.println("inside if one ---------------------------------\n");
						// name the new destination subset
						destSubsetID = new MyInteger( newStates.size() );
						// record the new state in the map
						newStates.put(destSubset, destSubsetID );
						// add it to the queue for future processing
						if( !uncompletedStates.offer(destSubset) ) {
							output.outln("ERROR! Cannot add state to queue. Determinisation is cancelled.");
							return null;
						}
					} else if ( destSubset.isErrorState() ) {
						//System.out.println("inside if two ---------------------------------\n");
						//destSubsetID = new MyInteger(Declaration.ERROR);
						if ( !isErrorStateCreated ) {
							destSubsetID = new MyInteger(Declaration.ERROR);
							// record the new error state in the map
							newStates.put(destSubset, destSubsetID);
							// add it to the queue for future processing
							if( !uncompletedStates.offer(destSubset) ) {
								output.outln("ERROR! Cannot add state to queue. Determinisation is cancelled.");
								return null;
							}
							
							isErrorStateCreated = true;
						}
					}
					
					// TODO save condition and action when building the map
					newTr = EventState.add(newTr,new EventState(alphabet[i],destSubsetID.val,null,null,labelWeight));
					//if (destSubsetID.val == -1) {
					//	System.out.println("error ---------------------------------\n");
					//}
				}
			}
			
			// stores the new state
				newStatesList.add(newTr);
		};
		
		return makeNewMachine(newStatesList);	
	}
	
	// converts the list of new states to a compact state
	private CompactState makeNewMachine(List<IEventState> newStatesList) {
        CompactState m = new CompactState();
        m.setName(machine.getName());
        m.alphabet = machine.alphabet.clone();
        m.maxStates = newStatesList.size();
        m.setStates(new EventState[m.maxStates]);
        int stateNum = 0;
        for (IEventState st : newStatesList) {
         		m.getStates()[stateNum] = st;
           		stateNum++;
         		//System.out.println("MohriDeterminiser.makeNewMachine:  m.alphabet[i] = "+m.alphabet[stateNum]);
        }
        
        //if (m.hasTau()){      // remove reflexive tau .... not working ????
        //		for (int i = 0; i<m.maxStates; i++)   
        //			m.getStates()[i] = EventState.removeTau(m.getStates()[i]);
        //		System.out.println("MohriDeterminizer.makeNewMachine: machine has tau .....");
        //}
        /*
		//compute new end state if any
		if (machine.endseq>=0) 
		{
			BitSet es =  new BitSet();
			es.set(machine.endseq);
			Integer ii = (Integer)map.get(es);
            if (ii!=null) m.endseq = ii.intValue();
		}
		*/
        output.outln("DFA("+machine.getName()+") has "+m.maxStates+" states.");
        
     //   output.outln( machine.isTraceEquivalent(m,2) ? 
     //      "Determinisation is successful" :
     //      "Determinisation is not successful" );	   
         
        //remove the prefix error_ from the transitions to ERROR
        ErrorManager e = new ErrorManager();
        m = e.mergeErrors(m);
        
        return m;        
    }
	
	//-------------------------------------------------------------------------------
	
	// efficient data structure used for determinisation
	class CompactForm {
	
		// map (label, fromState) to transitions
		Map< MapKey,List<CompactTransition> > map21;
		// map (label, toState) to transitions
		Map< MapKey,List<CompactTransition> > map24;
		// alphabet of the compact state
		int[] alphabet;
		
		//-----------------------------------------------------------
		// constructors
		
		//TODO use probClockTransitions instead of CompactTransition
		CompactForm() {
			map21 = new HashMap< MapKey,List<CompactTransition> >();
			map24 = new HashMap< MapKey,List<CompactTransition> >();
			Set<MyInteger> alphabetSet = new HashSet<MyInteger>();
			
			MapKey key;
			List<CompactTransition> list;
			
			for(int i=0; i<machine.getMaxStates(); i++ ) {
				if( machine.getStates()[i]!= null ) { 
					for(Enumeration e = machine.getStates()[i].elements(); e.hasMoreElements();) {
						IEventState st = (IEventState)e.nextElement();
						CompactTransition t =  new CompactTransition(i,st.getEvent(),st.getNext(),st.getProb()) ; 
				
						// put the transition in the correct bucket in map21
						key = new MapKey(t.label,t.fromState);
						list = map21.get(key);
						if( list == null ) list = new LinkedList<CompactTransition>();
						list.add(t); 
						map21.put(key,list);
					
						// put the transition in the correct bucket in map24
						key = new MapKey(t.label,t.toState);
						list = map24.get(key);
						if( list == null ) list = new LinkedList<CompactTransition>();
						list.add(t); 
						map24.put(key,list);
					
						// computing the alphabet
						alphabetSet.add( new MyInteger(t.label) );
					}
				}
			}
			
			// convert the set of alphabets to int array
			alphabet = new int[alphabetSet.size()];
			int index = 0;
			for( MyInteger label : alphabetSet ){
				alphabet[index] = label.val;
				index++;
			}
			
			// get new size now and rehash with loadFactor = 0.75
			// and initialCapacity = size/0.75 ?
			// and compare the computation speed
		}
		
		//--------------------------------------------------------- 
		
		public int[] getAlphabet() {
			return alphabet;
		}
		
		public boolean contains(int label, int fromState) {
			
			return map21.containsKey( new MapKey(label,fromState) );
		}

		// get the list of transitions with given label from fromState
		public List<CompactTransition> mapLabelAndFromState(int label, int fromState) {
			return map21.get( new MapKey(label,fromState) );
		}
		
		// get the list of transitions with given label to toState
		public List<CompactTransition> mapLabelAndToState(int label, int toState) {
			return map24.get( new MapKey(label,toState) );
		}
	}
	
	//-------------------------------------------------------------------------------
	
	// type of the key for the maps in CompactForm
	class MapKey {
		int elem1;
		int elem2;
		
		MapKey(int e1, int e2) {
			elem1 = e1;
			elem2 = e2;
		}
		
		public boolean equals(Object o) {
			if( this == o ) return true;
			
			if( !(o instanceof MapKey) ) return false;
			
			MapKey k = (MapKey)o;
			return elem1 == k.elem1 && elem2 == k.elem2;
		}
		
		//TO DO use better hash function or call methods from StateCodec
		public int hashCode() {
			return Integer.toString(elem1).hashCode()
				   + Integer.toString(elem2).hashCode();
		}
	}
	
	//-------------------------------------------------------------------------------
	
	// represents a subset
	// pre: the key is not modified after insertion into the table
	class SubsetMap extends HashMap<MyInteger, MyDouble> {
		
		public MyDouble getOrCreate(int state) {
			MyInteger key = new MyInteger(state);
			MyDouble weight = super.get(key);
			// if state does not exist in subset,
			// create a new state with weight initialised to 0
			if(weight == null) {
				weight = new MyDouble(0);
				put(key,weight);
			}
			return weight;
		}

		public boolean isErrorState() {
			return size() == 1 && containsKey( new MyInteger(-1) );
		}
		
		public void print() {
			System.out.print("Subset : {");
			for (Map.Entry<MyInteger, MyDouble> pair : entrySet()) {
				System.out.println("("+pair.getKey().val+","+pair.getKey().val+")");
			}
			System.out.println("}");
		}
	}
	
	//-------------------------------------------------------------------------------

	// mutable Integer object
	// data might be lost when used as key in hashmap
	class MyInteger {
		public int val;
		
		MyInteger(int val) {
			this.val = val;
		}
		
		public boolean equals(Object o) {
			if( this == o ) return true;
			
			if( !(o instanceof MyInteger) ) return false;
			
			MyInteger k = (MyInteger)o;
			return val == k.val;
		}
		
		//TODO use better hash function or call methods from StateCodec
		public int hashCode() {
			return Integer.toString(val).hashCode();
		}
	}
	
	// mutable double object
	class MyDouble {
		public double val;
		
		MyDouble(int val) {
			this.val = val;
		}
		
		public boolean equals(Object o) {
			if( this == o ) return true;
			
			if( !(o instanceof MyDouble) ) return false;
			
			MyDouble k = (MyDouble)o;
			return val == k.val;
		}
		
		//TODO use better hash function or call methods from StateCodec
		public int hashCode() {
			return Double.toString(val).hashCode();
		}
	}
}
