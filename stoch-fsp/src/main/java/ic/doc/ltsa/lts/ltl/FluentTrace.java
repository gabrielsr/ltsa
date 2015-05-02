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

package ic.doc.ltsa.lts.ltl;
import ic.doc.ltsa.common.iface.IFluentTrace;
import ic.doc.ltsa.common.iface.LTSOutput;
import java.util.*;
/* -----------------------------------------------------------------------*/

public class FluentTrace implements IFluentTrace {

PredicateDefinition[] fluents;

int[] state;


public FluentTrace(PredicateDefinition f[])  {
	if (f!=null)  {
 		fluents = f;
		state = new int[fluents.length];
	}
}

private void initialise() {
	if (state==null) return;
	for(int i = 0; i<state.length; ++i)
		state[i] = fluents[i].initial();
}	

private void update(String a)  {
	if (state == null) return;
	for(int i=0; i<state.length; ++i)  {
		int res = fluents[i].query(a);
		if (res!=0) state[i] = res;
	}
}

private String fluentString()  {
	if (state == null) return "";
    StringBuffer buf = new StringBuffer();
	buf.append("\t\t");
	boolean first = true;
	for(int i=0; i<state.length; ++i)  {
		if (state[i]>0)  {
			if (!first) buf.append(" && ");
			buf.append(fluents[i].toString());
			first = false;
		}
	}
	return buf.toString();
}

public void print(LTSOutput out, List trace)  {
	if (trace == null) return;
	initialise();
	Iterator I = trace.iterator();
	while (I.hasNext())  {
		String act = (String)I.next();
		update(act);
		out.outln("\t"+act+fluentString());
	}
}

public void print(LTSOutput out, List trace, boolean arg2) {

    print( out , trace , true );
    
}	

}


