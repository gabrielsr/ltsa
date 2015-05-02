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
import ic.doc.ltsa.common.iface.IPredicateDefinition;
import ic.doc.ltsa.common.infra.Diagnostics;
import ic.doc.ltsa.lts.*;

import java.util.*;
/* -----------------------------------------------------------------------*/

public class PredicateDefinition implements IPredicateDefinition {

    Symbol name;
    ActionLabels trueSet, falseSet;
    Vector trueActions, falseActions;
    Stack expr;
    boolean initial;
    ActionLabels range;    //range of fluents

    static Hashtable definitions;
    
    public PredicateDefinition(){}
    
    private PredicateDefinition(Symbol n, ActionLabels rng, ActionLabels ts, ActionLabels fs, Stack es){
	name = n;
	range = rng;
	trueSet = ts;
	falseSet = fs;
	expr = es;
	initial = false;
    }
	
    PredicateDefinition(Symbol n, Vector TA, Vector FA)  {
	name = n;
	trueActions = TA;
	falseActions = FA;
    }
	
    PredicateDefinition(String n, Vector TA, Vector FA, boolean init)  {
	name = new Symbol(Symbol.UPPERIDENT,n);
	trueActions = TA;
	falseActions = FA;
	initial = init;
    }

    
    public static void put(Symbol n, ActionLabels rng, ActionLabels ts, ActionLabels fs, Stack es) {
	if(definitions==null) definitions = new Hashtable();
	if(definitions.put(n.toString(),new PredicateDefinition(n,rng,ts,fs,es))!=null) {
            Diagnostics.fatal ("duplicate LTL predicate definition: "+n, n);
        } 
    }
	
    public static boolean contains(Symbol n)  {
	if (definitions==null) return false;
	return definitions.containsKey(n.toString());
    }
    
    public static void init(){
	definitions = null;
    }
	
    public static void compileAll()  {
	if (definitions == null) return;
	List v = new ArrayList();
	v.addAll(definitions.values());
	Iterator e = v.iterator();
	while (e.hasNext())  {
	    PredicateDefinition p = (PredicateDefinition)e.next();
	    compile(p); 
	}
    }
	
	
    public static PredicateDefinition get(String n)  {
	if (definitions==null) return null;
	PredicateDefinition p = (PredicateDefinition)definitions.get(n);
	if (p==null) return null;
	if (p.range!=null) return null;
	return p;
    }

    public Collection getAll() {

	if ( definitions == null ) { return null; }
	return definitions.values();
    }

    public static void compile(PredicateDefinition p){
        if (p == null) 	return;
	if (p.range == null)  {
	    p.trueActions = p.trueSet.getActions(null,null);
	    p.falseActions = p.falseSet.getActions(null,null);
	    assertDisjoint(p.trueActions,p.falseActions,p);
            if (p.expr!=null) {
		int ev = Expression.evaluate(p.expr,null,null);
		p.initial = (ev>0);
            }
        } else  {
	    Hashtable locals = new Hashtable();
            p.range.initContext(locals,null);
            while(p.range.hasMoreNames()) {
                String s = p.range.nextName();
                Vector PA = p.trueSet.getActions(locals,null);
		Vector NA = p.falseSet.getActions(locals,null);
		boolean init = false;
                assertDisjoint(PA,NA,p);
		if (p.expr!=null)  {
		    int ev = Expression.evaluate(p.expr,locals,null);
		    init = (ev>0);
		}
		String newName = p.name+"."+s;
		definitions.put(newName,new PredicateDefinition(newName,PA,NA,init));
            }
            p.range.clearContext();
        }
    }
	
    private static void assertDisjoint(Vector PA, Vector NA, PredicateDefinition p)  {
	Set s = new TreeSet(PA);
        s.retainAll(NA);
        if (!s.isEmpty())
	    Diagnostics.fatal("Predicate "+p.name+" True & False sets must be disjoint",p.name);
    }

	
    public int query(String s)  {
	if (trueActions.contains(s)) return 1;
	if (falseActions.contains(s)) return -1;
	return 0;
    }
	
    public int initial()  {
	return initial?1:-1;
    }
    
    public String toString()  {
	return name.toString();
    }

    public String getName() { return name.toString(); }

    public Set getInitiatingActions() { return new HashSet( trueActions ); }
    public Set getTerminatingActions() { return new HashSet( falseActions ); }
}



