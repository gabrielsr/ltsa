/******************************************************************************
 * LTSA (Labelled Transition System Analyser) - LTSA is a verification tool   *
 * for concurrent systems. It mechanically checks that the specification of a *
 * concurrent system satisfies the properties required of its behaviour.      *
 * Copyright (C) 2001-2004 Jeff Magee (with additions by Robert Chatley)      *
 *                                                                            *
 * This program is free software; you can redistribute it and/or              *
 * modify it under the terms of the GNU General Public License                *
 * as published by the Free Software Foundation; either version 2             *
 * of the License, or (at your option) any later version.                     *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program; if not, write to the Free Software                *
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA *
 *                                                                            *
 * The authors can be contacted by email at {jnm,rbc}@doc.ic.ac.uk            *
 *                                                                            *
 ******************************************************************************/

package ic.doc.ltsa.lts;

import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.IEventState;
import ic.doc.ltsa.common.iface.ITransitionPrinter;
import ic.doc.ltsa.common.iface.LTSOutput;

public class PrintTransitions implements ITransitionPrinter {
  
  ICompactState sm;
  
  public PrintTransitions (ICompactState sm) {
      this.sm = sm;
  }
  
  // chuan: some modification here
  public void print(LTSOutput output, int MAXPRINT) {
    int linecount = 0;
    
    // used to sort the event states in the correct order of printing
    PriorityQueue<IEventState> queue = new PriorityQueue<IEventState>(5, 
        	new Comparator<IEventState>() {
        	  	public int compare(IEventState a, IEventState b) {
       	        return (a.getNext()  > b.getNext()  ?  1 :
       	        	    a.getNext()  < b.getNext()  ? -1 :
       	                a.getProb()  > b.getProb()  ?  1 :
        	            a.getProb()  < b.getProb()  ? -1 :
        	            a.getEvent() > b.getEvent() ?  1 :
        	            a.getEvent() < b.getEvent() ? -1 : 0 );
        	    }
          	} );
    
    // print name
    output.outln("Process:");
    output.outln("\t"+sm.getName());
    // print number of states
    output.outln("States:");
    output.outln("\t"+sm.getMaxStates());
    // print transitions
    output.outln("Transitions:");
    output.outln("\t"+sm.getName()+ " = Q0,");
    
    // for all states
    for (int i = 0; i<sm.getMaxStates(); i++ ){
    	output.out("\tQ"+i+"\t= ");
  		
  		// if there are no transitions
  		if( sm.getStates()[i] == null ) {
  			output.out( i==sm.getEndseq() ? "END" : "STOP" );
  			output.outln( i<sm.getMaxStates()-1 ? "," : "." );
  			continue;
  		}
  		
      	// otherwise sort the transitions of state i
  		for(Enumeration e = sm.getStates()[i].elements(); e.hasMoreElements();) {
  			IEventState st = (IEventState)e.nextElement();
  			queue.add(st);
  		}
  		
  		// if there are some transitions
  	    output.out("(");
  		
  		// for all transitions in state i
  		while( !queue.isEmpty() ) {
  			linecount++;
  			if (linecount>MAXPRINT) {
  				output.outln("EXCEEDED MAXPRINT SETTING");
  				return;
  			}
  			
  			// get transtions to the same next state with the same probability
  			Queue<IEventState> transitions = new LinkedList<IEventState>();
  			IEventState head = queue.poll();
  			int next = head.getNext();
  			double prob = head.getProb();
  			transitions.offer( head );
  			
  			while( queue.peek() != null
  				   && queue.peek().getNext() == next 
  				   && queue.peek().getProb() == prob ) {
  				transitions.offer( queue.poll() );
  			}
  			
  			// get the alphabet strings
  			String[] events = new String[transitions.size()];
  			for(int j=0; j<events.length; j++) {
  				events[j] = sm.getAlphabet()[transitions.poll().getEvent()];
  			}
  			
  			Alphabet a = new Alphabet(events);
  			output.out(" ("+prob+") "+a.toString()+" -> ");
            output.out( next<0 ? "ERROR" : "Q"+next );
  			
  			if ( !queue.isEmpty() ) {
  				 output.out("\n\t\t  |");          
  			}
  		}
  	  
  		output.outln( i<sm.getMaxStates()-1 ? ")," : ")." );
    }
  }

    public void print(LTSOutput pOutput) {

        print(pOutput, 400);
    }
  
}
  
  