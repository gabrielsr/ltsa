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

import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.IEventState;
import ic.doc.ltsa.common.iface.ITransitionPrinter;
import ic.doc.ltsa.common.iface.LTSOutput;

public class PrintTransitions implements ITransitionPrinter {
  
  ICompactState sm;
  
  public PrintTransitions (ICompactState pCS) {
      sm = pCS;
  }

    
    public void print( LTSOutput output ) {

        print( output , 400 );
    }
  
  public void print(LTSOutput output, int MAXPRINT) {
    int linecount =0;
    // print name
    output.outln("Process:");
    output.outln("\t"+sm.getName());
    // print number of states
    output.outln("States:");
    output.outln("\t"+sm.getMaxStates());
    output.outln("Transitions:");
    output.outln("\t"+sm.getName()+ " = Q0,");
    for (int i = 0; i<sm.getMaxStates(); i++ ){
      output.out("\tQ"+i+"\t= ");
      IEventState current = EventState.transpose(sm.getStates()[i]);
      if (current == null) {
        if (i==sm.getEndseq())
          output.out("END");
        else
          output.out("STOP");
        if (i<sm.getMaxStates()-1) 
           output.outln(","); 
        else 
           output.outln(".");  
      } else {
        output.out("(");
        while (current != null) {
          linecount++;
          if (linecount>MAXPRINT) {
            output.outln("EXCEEDED MAXPRINT SETTING");
            return;
          }
          String[] events = EventState.eventsToNext(current,sm.getAlphabet());
          Alphabet a = new Alphabet(events);
          output.out(a.toString()+" -> ");
          if (current.getNext()<0) 
            output.out("ERROR"); 
          else 
            output.out("Q"+current.getNext());
          current = current.getList();
          if (current==null) {
            if (i<sm.getMaxStates()-1) 
              output.outln("),"); 
            else 
              output.outln(").");
          } else {
            output.out("\n\t\t  |");
          }
        }
      }
    }
  }
  
}
  
  
