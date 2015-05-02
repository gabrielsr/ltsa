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

import ic.doc.ltsa.common.iface.IActionLabels;
import ic.doc.ltsa.common.iface.ISymbol;

import java.util.*;

class StateDefn extends Declaration {
    ISymbol name;
    boolean accept = false;
    IActionLabels range;  //use label with no name
    StateExpr stateExpr;

    private void check_put(String s,StateMachine m) {
        if(m.explicit_states.containsKey(s))
           Diagnostics.fatal ("duplicate definition -"+name, name);
        else
           m.explicit_states.put(s,m.stateLabel.label());
    }

    public void explicitStates(StateMachine m) {
        if (range == null) {
            String s = name.toString();
            if (s.equals("STOP") || s.equals("ERROR") || s.equals("END"))
              Diagnostics.fatal ("reserved local process name -"+name, name);
            check_put(s,m);
        } else {
            Hashtable locals = new Hashtable();
            range.initContext(locals, m.constants);
            while (range.hasMoreNames()) {
                check_put(name.toString()+"."+range.nextName(),m);
            }
            range.clearContext();
        }
    }

    private void crunchAlias(StateExpr st,String n,Hashtable locals,StateMachine m,boolean probabilisticSystem) {
        String s = st.evalName(locals,m);
        Integer i = (Integer) m.explicit_states.get(s);
        if (i==null) {
            if(s.equals("STOP")) {
                m.explicit_states.put("STOP",i=m.stateLabel.label());
            } else if (s.equals("ERROR")) {
                m.explicit_states.put("ERROR",i=new Integer(Declaration.ERROR));
            } else if (s.equals("END")) {
                m.explicit_states.put("END",i=m.stateLabel.label());
            } else {
                m.explicit_states.put("ERROR",i=new Integer(Declaration.ERROR));
                Diagnostics.warning (s + " defined to be ERROR",
                                    "definition not found- "+s, st.name);
            }
        }
        CompactState mach = null;
        if (st.processes!=null) 
        	mach = st.makeInserts(locals,m,probabilisticSystem);
        if (mach!=null)
            m.preAddSequential((Integer)m.explicit_states.get(n),i,mach);
        else 
           m.aliases.put(m.explicit_states.get(n),i);
    }

    public void crunch(StateMachine m,boolean probabilisticSystem) {
        if (stateExpr.name==null && stateExpr.boolexpr==null) return;
        Hashtable locals = new Hashtable();
        if (range == null)
            crunchit(m,locals,stateExpr,name.toString(),probabilisticSystem);
        else {
            range.initContext(locals, m.constants);
            while (range.hasMoreNames()) {
                String s = ""+name+"."+range.nextName();
                crunchit(m,locals,stateExpr,s,probabilisticSystem);
            }
            range.clearContext();
        }
    }

    private void crunchit(StateMachine m, Hashtable locals, StateExpr st, String s,boolean probabilisticSystem) {
        if (st.name!=null)
            crunchAlias(st,s,locals,m,probabilisticSystem);
        else if (st.boolexpr!=null) {
            if (Expression.evaluate(st.boolexpr,locals,m.constants)!=0)
                st = st.thenpart;
            else
                st = st.elsepart;
            if(st!=null) crunchit(m,locals,st,s,probabilisticSystem);
        }
    }

    public void transition(StateMachine m,boolean probabilisticSystem) {
        int from;
        if (stateExpr.name!=null) return; //this is an alias definition
        Hashtable locals = new Hashtable();
        if (range == null) {
           from = ((Integer)m.explicit_states.get(""+ name)).intValue();
           stateExpr.firstTransition(from,locals,m,probabilisticSystem);
           if (accept) {
            if(!m.alphabet.containsKey("@"))
                m.alphabet.put("@",m.eventLabel.label());
            Symbol e = new Symbol(Symbol.IDENTIFIER,"@");
            m.transitions.addElement(new Transition(from, e,from));
	       }
        } else {
           range.initContext(locals, m.constants);
           while (range.hasMoreNames()) {
                from = ((Integer)m.explicit_states.get(""+name+"."+range.nextName())).intValue();
                stateExpr.firstTransition(from,locals,m,probabilisticSystem);
           }
           range.clearContext();
        }
        // normalise probabilities
        // TODO: this is not very good at all, but a quick fix
        // which normalises probabilities
        if (probabilisticSystem) {
	        Vector copy = (Vector)m.transitions.clone();
			while (copy.size()>0) {
				Vector list = new Vector();
				Iterator i = copy.iterator();
				ProbabilisticTimedTransition compare = (ProbabilisticTimedTransition)i.next();
				list.add(compare);
				i.remove();
		        while (i.hasNext()) {
		        	ProbabilisticTimedTransition t = (ProbabilisticTimedTransition)i.next();
		        	if (t.from == compare.from) {
		        		list.add(t);
		        		i.remove();
		        	}
		        }
		        // take elements from list and update probabilities
		        double sum = 0;
				i = list.iterator();
				while (i.hasNext()) {
					sum += ((ProbabilisticTimedTransition)i.next()).prob;
				}
				i = list.iterator();
				while (i.hasNext()) {
					((ProbabilisticTimedTransition)i.next()).prob /= sum;
				}
	        }
        }
    }
    
    public StateDefn myclone() {
    	  StateDefn sd = new StateDefn();
    	  sd.name = name;
       sd.accept = accept;
       if (range!=null)
       	sd.range = ((ActionLabels)range).myclone();  
       if (stateExpr!=null)
    			sd.stateExpr = stateExpr.myclone();
    		return sd;
    }

	public String toString() {
		StringBuffer s = new StringBuffer();
	
		s.append( name.toString() );
		s.append( "=" );
		s.append( stateExpr.toString() );
	
		return s.toString();
	}

}
