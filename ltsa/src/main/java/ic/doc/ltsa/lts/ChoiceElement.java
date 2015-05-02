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

import java.util.Hashtable;
import java.util.Stack;

class ChoiceElement extends Declaration {
    Stack guard;
    ActionLabels action;
    StateExpr stateExpr;

    private void add(int from, Hashtable locals, StateMachine m, ActionLabels action) {
        action.initContext(locals, m.constants);
        while (action.hasMoreNames()) {
            String s = action.nextName();
            Symbol e = new Symbol(Symbol.IDENTIFIER, s);
            if(!m.alphabet.containsKey(s))
                m.alphabet.put(s,m.eventLabel.label());
            stateExpr.endTransition(from, e, locals, m);
        }
        action.clearContext();
    }

    private void add(int from, Hashtable locals, StateMachine m, String s) {
        Symbol e = new Symbol(Symbol.IDENTIFIER, s);
        if(!m.alphabet.containsKey(s))
            m.alphabet.put(s,m.eventLabel.label());
        stateExpr.endTransition(from, e, locals, m);
    }


    public void addTransition(int from, Hashtable locals, StateMachine m){
        if (guard==null || Expression.evaluate(guard,locals,m.constants)!=0) {
            if (action!=null) {
                add(from,locals,m,action);
            }
        }
    }
    
    public ChoiceElement myclone() {
    	   ChoiceElement ce = new ChoiceElement();
       ce.guard = guard;
       if (action!=null)
       	 ce.action = action.myclone();
       if (stateExpr!=null) 
       	 ce.stateExpr = stateExpr.myclone();
       return ce;
    }

}
