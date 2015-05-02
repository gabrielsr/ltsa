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

import java.util.*;

class StateExpr extends Declaration {
    //if name !=null then no choices
    Vector processes;
    Symbol name;
    Vector expr; //vector of expressions stacks, one for each subscript
    Vector choices;
    Stack boolexpr;
    StateExpr thenpart;
    StateExpr elsepart;
    
    public void addSeqProcessRef(SeqProcessRef sp) {
       if (processes==null) processes = new Vector();
       processes.addElement(sp);
    }
    
    public CompactState makeInserts(Hashtable locals, StateMachine m,boolean probabilisticSystem) {
    	  Vector seqs = new Vector();
        Enumeration e = processes.elements();
        while(e.hasMoreElements()) {
            SeqProcessRef sp = (SeqProcessRef)e.nextElement();
            CompactState mach = sp.instantiate(locals,m.constants,probabilisticSystem);
            if (!mach.isEnd()) seqs.addElement(mach);
        }
        if (seqs.size()>0)
          return CompactState.sequentialCompose(seqs);
        return null;
    }

    public Integer instantiate(Integer to, Hashtable locals, StateMachine m, boolean probabilisticSystem ) {
        if (processes==null) return to;
        CompactState seqmach = makeInserts(locals,m,probabilisticSystem);
        if (seqmach == null) return to;
        Integer start = m.stateLabel.interval(seqmach.maxStates);
        seqmach.offsetSeq(start.intValue(),to.intValue());
        m.addSequential(start,seqmach);
        return start;
    }

    public void firstTransition(int from, Hashtable locals, StateMachine m,boolean probabilisticSystem) {
       if (boolexpr!=null) {
            if (Expression.evaluate(boolexpr,locals,m.constants)!=0){
                if(thenpart.name==null)
                   thenpart.firstTransition(from,locals,m,probabilisticSystem);
            } else {
                if(elsepart.name==null)
                    elsepart.firstTransition(from,locals,m,probabilisticSystem);
            }
       } else
           addTransition(from,locals,m,probabilisticSystem);
      }

    public void addTransition(int from, Hashtable locals, StateMachine m,boolean probabilisticSystem) {
       Enumeration e = choices.elements() ;
       while (e.hasMoreElements()) {
          ChoiceElement d = (ChoiceElement) e.nextElement();
          d.addTransition(from,locals,m,probabilisticSystem);
       }
    }

	public void endTransition( int from, Symbol event, Hashtable locals, StateMachine m, Collection conditions, Collection actions, boolean probabilisticSystem ) {
		endTransition( from, event, locals, m, conditions, actions, null, probabilisticSystem );
	}


    public void endTransition(int from, Symbol event, Hashtable locals, StateMachine m, Collection conditions, Collection actions, Stack probExpr,boolean probabilisticSystem) {
      if (boolexpr!=null) {
        if (Expression.evaluate(boolexpr,locals,m.constants)!=0)
            thenpart.endTransition(from,event,locals,m,conditions,actions,probabilisticSystem);
        else
            elsepart.endTransition(from,event,locals,m,conditions,actions,probabilisticSystem);
      } else {
        Integer to;
        if (name!=null) {
            to = (Integer) m.explicit_states.get(evalName(locals,m));
            if (to==null) {
                if(evalName(locals,m).equals("STOP")) {
                    m.explicit_states.put("STOP",to=m.stateLabel.label());
                } else if(evalName(locals,m).equals("ERROR")) {
                    m.explicit_states.put("ERROR",to=new Integer(Declaration.ERROR));
                } else if(evalName(locals,m).equals("END")) {
                    m.explicit_states.put("END",to=m.stateLabel.label());
                } else {
                   m.explicit_states.put(evalName(locals,m),to=new Integer(Declaration.ERROR));
                   Diagnostics.warning (evalName(locals,m)+ " defined to be ERROR",
                                        "definition not found- "+evalName(locals,m), name);
                }
            }
            to = instantiate(to,locals,m,probabilisticSystem);
			if( probExpr != null ) 
				doAddTransition( from, event, to.intValue(), conditions, actions, locals, m, probExpr);
			else
				doAddTransition( from, event, to.intValue(), conditions, actions, locals, m );
         } else {
            to = m.stateLabel.label();
			if( probExpr != null )
				doAddTransition( from, event, to.intValue(), conditions, actions, locals, m, probExpr);
			else
				doAddTransition( from, event, to.intValue(), conditions, actions, locals, m );
            addTransition(to.intValue(),locals,m,probabilisticSystem);
        }
      }
    }
    
	private void doAddTransition( int from, Symbol event, int to, Stack probExpr, Hashtable locals, StateMachine m ) {
		double prob = Expression.getDoubleValue( probExpr, locals, m.constants );
		if (prob == 0) throw new LTSException("Probabilities must not be 0!"); 
		Transition t = new ProbabilisticTimedTransition( from, event, to, prob );
		m.transitions.addElement( t );
	}

	private void doAddTransition( int from, Symbol event, int to, Collection conditions, Collection actions, Hashtable locals, StateMachine m, Stack probExpr ) {
		double prob = Expression.getDoubleValue( probExpr, locals, m.constants );
		if (prob == 0) throw new LTSException("Probabilities must not be 0!");
		Transition t = new ProbabilisticTimedTransition( from, event, to, conditions, actions, locals, m.constants, prob );
		m.transitions.addElement( t );
	}

	/** An auxillary to add a transition to the state machine. */
	private void doAddTransition( int from, Symbol event, int to, Collection conditions, Collection actions, Hashtable locals, StateMachine m ) {
		if( !isClocked( conditions, actions ) )
			doAddTransition( from, event, to, m );
		else {
			Transition t = new ProbabilisticTimedTransition( from, event, to, conditions, actions, locals, m.constants );
			m.transitions.addElement( t );
		}
	}

	/** An auxillary to add a transition to the state machine. */
	private void doAddTransition( int from, Symbol event, int to, StateMachine m ) {
		Transition t = new Transition( from, event, to );
		m.transitions.addElement( t );
	}

	/** Tests the given vectors to see if the transition has clock
	 * setting actions or clock conditions. */
	private boolean isClocked( Collection conditions, Collection actions ) {
		return( (conditions != null && conditions.size() > 0 )
                || (actions != null && actions.size() > 0 ) );
	}

    public String evalName(Hashtable locals, StateMachine m) {
        if (expr==null)
            return name.toString();
        else {
            Enumeration e = expr.elements();
            String s = name.toString();
            while (e.hasMoreElements()) {
                Stack x = (Stack) e.nextElement();
                s = s + "."+Expression.getValue(x,locals,m.constants);
            }
            return s;
        }
    }
    
    public StateExpr myclone() {
    	   StateExpr se = new StateExpr();
    	   se.processes = processes;
    		 se.name = name;
        se.expr = expr;    //expressions are cloned when used
        if (choices!=null) {
        	   se.choices =  new Vector();
        	   Enumeration e = choices.elements();
        	   while(e.hasMoreElements())
        	   		se.choices.addElement(((ChoiceElement)e.nextElement()).myclone());
        }
       se.boolexpr = boolexpr;
       if (thenpart!=null) se.thenpart = thenpart.myclone();
       if (elsepart!=null) se.elsepart = elsepart.myclone();
       return se;
    }

	public String toString() {
		StringBuffer s = new StringBuffer();
	
		if( name != null ) {
			// process ref
			s.append( name.toString() );
			if( expr != null ) {
				// add process subscripts
				s.append(expr.toString());
/*
				Iterator i = expr.iterator();
				while( i.hasNext() ) {
					i.next();
					s.append( "[expr]" );
				}
*/
			}
		} else {
			if( choices.size() == 1 ) {
				// deterministic process
				s.append( choices.elementAt( 0 ).toString() );
			} else {
				// non-deterministic process
				s.append( "(" );
				Iterator i = choices.iterator();
				while( i.hasNext() ) {
					s.append( i.next().toString() );
					if( i.hasNext() ) s.append( "|" );
				}
				s.append( ")" );
			}
		}
	
		return s.toString();
	}
	
}
