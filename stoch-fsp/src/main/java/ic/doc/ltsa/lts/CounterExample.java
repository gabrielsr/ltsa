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
import ic.doc.ltsa.common.iface.LTSOutput;

import java.util.*;

/** This class tries to find a deadlock in the system */
public class CounterExample {

    protected CompositeState mach;
    protected Vector errorTrace = null;

    public CounterExample(CompositeState m) {
        mach = m;
    }

    public void print(LTSOutput output) {
		print( output , true );
    }

    public void print(LTSOutput output , boolean checkDeadlocks) {
        EventState trace = new EventState(0,0);
        int result = EventState.search(
                         trace,
                         mach.composition.getStates(),
                         	0,
                         Declaration.ERROR,
                         mach.composition.endseq,
			 checkDeadlocks
                     );
        errorTrace = null;
        switch(result) {
        case Declaration.SUCCESS:
            output.outln("No deadlocks/errors");
            break;
        case Declaration.STOP:

           output.outln("Trace to DEADLOCK: ");
           errorTrace = EventState.getPath(trace.path,mach.composition.alphabet);
           printPath(output,errorTrace);

           break;
        case Declaration.ERROR:
           errorTrace = EventState.getPath(trace.path,mach.composition.alphabet);
           String name = findComponent(errorTrace);
           output.outln("Trace to property violation in "+name+":");
           printPath(output,errorTrace);
           break;
        }
    }

    private void printPath(LTSOutput output, Vector v) {
        Enumeration e = v.elements();
        while (e.hasMoreElements())
            output.outln("\t"+(String)e.nextElement());
    }

    private String findComponent(Vector trace) {
        Enumeration e = mach.machines.elements();
        while (e.hasMoreElements()) {
            CompactState cs = (CompactState)e.nextElement();
            if (cs.isErrorTrace(trace)) return cs.getName();
        }
        return "?";
    }
    
    public Vector getErrorTrace(){ return errorTrace;}
}

