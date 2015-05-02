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

class SeqProcessRef {
   Symbol name;
   Vector actualParams;
   
   static LTSOutput output;
   SeqProcessRef(Symbol n, Vector params) {
       name = n;
       actualParams = params;
   }
   
   CompactState instantiate(Hashtable locals, Hashtable constants,boolean probabilisticSystem) {
        //compute parameters
        Vector actuals = paramValues(locals,constants);
        String refname = (actuals==null)? name.toString() : name.toString() + StateMachine.paramString(actuals);
        // have we already compiled it?
        CompactState mach = (CompactState)LTSCompiler.compiled.get(refname);
        if (mach==null) {
          // we have not got one so first see if its a defined process
          //ProcessSpec p = (ProcessSpec)LTSCompiler.processes.get(name.toString());
		  Compilable p = (Compilable) LTSCompiler.processes.get(name.toString());
          if (p!=null) {
/*
          	   p = p.myclone();
              if (actualParams!=null) {  //check that parameter arity is correct
                  if (actualParams.size()!=p.parameters.size())
                      Diagnostics.fatal ("actuals do not match formal parameters", name);
              }
              StateMachine one = new StateMachine(p,actuals);
              mach = one.makeCompactState();
              output.outln("-- compiled:"+mach.name);          
*/
			  if( actualParams != null ) {  //check that parameter arity is correct
				  if( actualParams.size() != p.getNumberOfParameters() )
					  Diagnostics.fatal ("actuals do not match formal parameters", name);
			  }
			  mach = p.makeCompactState( output, actuals, probabilisticSystem );
		  }
        }
        if (mach == null) {
          CompositionExpression ce = (CompositionExpression)LTSCompiler.composites.get(name.toString());
          if (ce!=null) {
            CompositeState cs = ce.compose(actuals,probabilisticSystem);
            mach = cs.create(output);
          }
        }
        if (mach !=null) {    
          LTSCompiler.compiled.put(mach.getName(),mach);  // add to compiled processes
          if (!mach.isSequential()) 
                  Diagnostics.fatal ("process is not sequential - "+name, name);
          return mach.myclone();
        }
        Diagnostics.fatal ("process definition not found- "+name, name);
        return null;
   }

    private Vector paramValues(Hashtable locals, Hashtable constants) {
        if (actualParams==null) return null;
        Enumeration e = actualParams.elements();
        Vector v = new Vector();
        while(e.hasMoreElements()) {
            Stack stk = (Stack)e.nextElement();
            v.addElement(Expression.getValue(stk,locals,constants));
        }
        return v;
    }

}

