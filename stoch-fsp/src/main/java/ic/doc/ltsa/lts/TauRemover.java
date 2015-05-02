package ic.doc.ltsa.lts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ic.doc.ltsa.common.iface.IEventState;
import ic.doc.ltsa.common.iface.LTSOutput;
//TODO handle case when prob > 1, 
//TODO print out time for tau removal in LTSOutput
//TODO use only one adjacency matrix by updating the weights in correct order
//TODO use a map for the adjancency matrix?

public class TauRemover {

	CompactState machine;
	List<CompactTransition> tauTransitions;
	// store the non-tau transitions of every states
	List[] nonTauTransitions;
	// the states in tau closure
	Set<Integer> tauComponent;
	// states that will be unreachable after tau-removal
	Set<Integer> unreachable;
	// used to renumber the states of the tau component
	Map<Integer,Integer> oldToNewIDMap;
	// adjacency matrices for the states of the tau component
	AdjMatrix curr;
	AdjMatrix prev;
	
	private TauRemover(CompactState machine) {
		this.machine = machine;
	}
	
	// destructive tau-removal on machine
	public static CompactState removeTau(CompactState machine, LTSOutput output) {
		TauRemover r = new TauRemover(machine);
		return r.auxRemoveTau();
	}
	
	private CompactState auxRemoveTau() {
		initialise();
		runWFGJ();
		
		// based on "Generic Epsilon-Removal and Input Epsilon-Normalization Algorithms
		// for Weighted Tranducers" by Mehryar Mohri
        for (int i=0; i<machine.maxStates; i++) { 
            for(int j=0; j<machine.maxStates; j++) {
        		
            	// get the sum of the probability of all epsilon-labeled-paths from i to j
            	double prob = 0;
        		if(oldToNewIDMap.containsKey(i) && oldToNewIDMap.containsKey(j)) {
        			prob = curr.getWeight(oldToNewIDMap.get(i),oldToNewIDMap.get(j));
        		}
        		
        		// if there exist such path
            	if(prob != 0) {
            		for(Object t : nonTauTransitions[j]) {
            			CompactTransition ct = (CompactTransition)t;
            			// add new transition
            			machine.getStates()[i] = EventState.add(machine.getStates()[i],
            				new EventState(ct.label,ct.toState,null,null,prob*ct.weight));
            		}
            	}
            }
        }
        
        return machine;
	}
	
	// get all required information from machine for tau-removal 
    private void initialise() {
    	
    	// initialisation of fields
    	tauTransitions = new LinkedList<CompactTransition>();
	   	nonTauTransitions = new LinkedList[machine.maxStates];
    	tauComponent = new HashSet<Integer>();
    	oldToNewIDMap = new HashMap<Integer,Integer>();
    	
    	// for all states in the machine
        for (int i = 0; i<machine.maxStates; i++) {
        	IEventState es = machine.getStates()[i];
        	boolean hasTau = false;
     
        	/*-----------------------------------------------------------------*/
        	// get all the states in the tau component
        	
        	// while there are more taus
            while(es != null && es.getEvent() == Declaration.TAU) {
            	hasTau = true;
            	tauTransitions.add(new CompactTransition(i, 0, es.getNext(), es.getProb()));
                
            	// add the next state of the tau transition to tau component
            	Integer st = new Integer( es.getNext() );
            	if( tauComponent.add(st) ) {
            		// renumber the next state
                	oldToNewIDMap.put(st, new Integer(oldToNewIDMap.size()));
            	}
            	// get next transition
            	es = es.getList();
            }
            
            if( hasTau ) {
            	// add the source of the tau transition to tau component
            	Integer st = new Integer(i);
            	if( tauComponent.add(st) ) {
            		// renumber the source
            		oldToNewIDMap.put(st, new Integer(oldToNewIDMap.size()));
            	}
            	
            	// store the new state will all tau transitions removed
            	machine.getStates()[i] = es;       	
            	hasTau = false;
            }
            
            /*-----------------------------------------------------------------*/
            // keep the non-tau transitions in a list
            
        	nonTauTransitions[i] = new LinkedList<CompactTransition>();
        	while(es != null) {
        		// add the non-tau transition
        		nonTauTransitions[i].add(
        		   new CompactTransition(i, es.getEvent(), es.getNext(), es.getProb()));
        		// get the next non-tau transition
        		es = es.getList();
        	}
        }
        
        /*-----------------------------------------------------------------*/
        // initialises the unreachable states
        
        // only the states in tau component can be unreachable
        unreachable = new HashSet<Integer>(tauComponent);
    }
	
    // compute all-pairs total distance using Warshall-Floyd-Gauss-Jordan algorithm
    // refer to "Algebraic Path Problem" by S.Rajopadhye, CSU Computer Science
    private void runWFGJ() {
    	
    	// initialisation
    	int size = tauComponent.size(); 
    	
		prev = new AdjMatrix(size);
		curr = new AdjMatrix(size);

		for( CompactTransition t : tauTransitions ) {
			curr.addWeight(oldToNewIDMap.get(t.fromState), oldToNewIDMap.get(t.toState), t.weight);
		}
		
		/*-----------------------------------------------------------------*/
		// main loop
		for(int k=0; k<size; k++) {
			// swap prev and curr
			AdjMatrix temp = prev;
			prev = curr;
			curr = temp;
			
			for(int i=0; i<size; i++) {
				for(int j=0; j<size; j++) {
					if(i==j && j==k) {
						curr.setWeight(i, j, closure(prev.getWeight(i,j)));
					}
					else if (i==k && k!=j) {
						curr.setWeight(i, j, 
					       (1+closure(prev.getWeight(k,k)))*prev.getWeight(i,j) );
					}
					else if(j==k && k!= i) {
						curr.setWeight(i, j, 
						   prev.getWeight(i,j)*(1+closure(prev.getWeight(k,k))) );
					}
					else {
						curr.setWeight(i, j, 
						   prev.getWeight(i,j)+
						      ( prev.getWeight(i,k)*(1+closure(prev.getWeight(k,k)))
						    	*prev.getWeight(k,j) ) );
					}
				}
			}
		}	
    }
    
    // auxiliary method in runWFGJ to compute the sum of the geometric progression 
    // infinite series of the given weight
    private double closure(double weight) {
    	return weight/(1-weight);
    }
   
}

//--------------------------------------------------------------------------------

class AdjMatrix {

	double[][] matrix;

	AdjMatrix(int size) {
		matrix = new double[size][size];
		
		// initialise weights to zero
		for(int i = 0; i<size; i++)
			Arrays.fill(matrix[i], 0);
	}

	public double getWeight(int i, int j) {		
		return matrix[i][j];
	}
	
	public void setWeight(int i, int j, double weight) {
		matrix[i][j] = weight;	
	}
	
	public void addWeight(int i, int j, double weight) {
		matrix[i][j] += weight;
	}
	
}

//private static getWeaklyConnectedTauSubMachines(CompactState machine) {
//	
//}


/*
private CompactState getWeightedTauClosure(CompactState machine) {
	double[] d = new double[machine.getMaxStates()];
	double[] r = new double[machine.getMaxStates()];
	Arrays.fill(d,0);
	Arrays.fill(r,0);
	d[0] = r[0] = 1;
	
	Queue<Integer> queue = new LinkedList<Integer>();
	queue.offer(0);
	
	while( !queue.isEmpty() ) {
		int head = queue.poll();
		double r2 = r[head];
		r[head] = 0;
		 
		for(Enumeration e = machine.getStates()[head].elements(); e.hasMoreElements();) {
			IEventState st = (IEventState)e.nextElement();
			if( d[st.getNext()] != d[st.getNext()] + (r2*st.getProb()) ) {
				d[st.getNext()] = d[st.getNext()] + (r2*st.getProb());
				r[st.getNext()] = r[st.getNext()] + (r2*st.getProb());
				if( !queue.contains(st.getNext()) ) {
					queue.offer(st.getNext());
				}
			}
		}
	}
	
	d[0] = 1;
	
	return machine;	
}
*/
