package ic.doc.ltsa.lts;

import ic.doc.ltsa.common.iface.LTSOutput;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

class ProcessRef {
    Symbol name;
    Vector actualParams;                   // Vector of expressions stacks

    public void instantiate(CompositionExpression c, Vector machines, LTSOutput output, Hashtable locals, boolean probabilisticSystem ) {
        //compute parameters
        Vector actuals = paramValues(locals,c);
        String refname = (actuals==null)? name.toString() : name.toString() + StateMachine.paramString(actuals);
        // have we already compiled it?
        CompactState mach = (CompactState)c.compiledProcesses.get(refname);
        if(mach!=null) {
            machines.addElement(mach.myclone());
            return;
        }
        // we have not got one so first see if its a process
        Compilable p = (Compilable)c.processes.get(name.toString());
        if (p!=null) {
            if (actualParams!=null) {  //check that parameter arity is correct
                if (actualParams.size()!=p.getNumberOfParameters())
                    Diagnostics.fatal ("actuals do not match formal parameters", name);
            }
			mach = p.makeCompactState( output, actuals, probabilisticSystem );
			machines.addElement(mach.myclone());      // pass back clone
			c.compiledProcesses.put(mach.getName(),mach);  // add to compiled processes
			return;
/*
            if (!p.imported()) {
	            StateMachine one = new StateMachine(p,actuals);
	            mach = one.makeCompactState();
            } else {
            	mach = new AutCompactState(p.name, p.importFile);
            }
            machines.addElement(mach.myclone());      // pass back clone
            c.compiledProcesses.put(mach.name,mach);  // add to compiled processes
            if (!p.imported()) 
            	c.output.outln("Compiled: "+mach.name);
            else
              c.output.outln("Imported: "+mach.name);
            return;
*/
        }
        // it must be a composition
        CompositionExpression ce = (CompositionExpression)c.composites.get(name.toString());
        if (ce==null) Diagnostics.fatal ("definition not found- "+name, name);
        if (actualParams!=null) {  //check that parameter arity is correct
             if (actualParams.size()!=ce.parameters.size())
             Diagnostics.fatal ("actuals do not match formal parameters", name);
        }
        CompositeState cs;
        if (ce==c) {
           Hashtable save = (Hashtable)c.constants.clone();
           cs = ce.compose(actuals,probabilisticSystem);
           c.constants=save;
        } else
           cs = ce.compose(actuals,probabilisticSystem);
        // dont compose if not necessary, maintain as a list of machines
        if (cs.needNotCreate()) {
            for(Enumeration e = cs.machines.elements();e.hasMoreElements();) {
                mach = (CompactState)e.nextElement();
                mach.setName(cs.name+"."+mach.getName());
				mach.prefixMeasureNames( cs.name+"."); // ensure measure names updated
            }
            machines.addElement(cs);  //flatten later if correct
         } else {
            mach = cs.create(output);
            c.compiledProcesses.put(mach.getName(),mach);  // add to compiled processes
            c.output.outln("Compiled: "+mach.getName());
            machines.addElement(mach.myclone()); //pass back clone
        }
    }

    private Vector paramValues(Hashtable locals, CompositionExpression c) {
		if (actualParams==null) return null;

		Enumeration e = actualParams.elements();
		Enumeration d = getDefaultValues( c ).elements();
		Vector v = new Vector();
		while( e.hasMoreElements() && d.hasMoreElements() ) {
			Stack stk = (Stack) e.nextElement();
			Value def = (Value) d.nextElement();
			// check default values to determine parameter type
			if( def.isDouble() )
				v.addElement(new Value(Expression.getDoubleValue( stk, locals,c.constants )));
			else
				v.addElement(Expression.getValue( stk, locals, c.constants ));
			}
		return v;
    }
    
	/** Extracts the default parameter values for the process referred
	 * to by {@link #name}.
	 */
	private Vector getDefaultValues( CompositionExpression c ) {
		Vector defaults;
		// try and obtain from a process
		Compilable p = (Compilable) c.processes.get( name.toString() );
		if( p != null ) {
			// is a process
			try {
				ProcessSpec ps = (ProcessSpec) p;
				defaults = makeParamVector( ps.init_constants, ps.parameters );
			} catch( ClassCastException cce ) {
				// must be a measurement spec
				defaults = new Vector();
			}
		} else {
			// must be a composition
			CompositionExpression ce = (CompositionExpression)
			c.composites.get( name.toString() );
			if( ce != null ) {
				defaults = makeParamVector( ce.init_constants, ce.parameters );
			} else {
				defaults = null;
				Diagnostics.fatal( "definition not found- " + name, name );
			}
		}
		return defaults;
	}

	private Vector makeParamVector( Hashtable paramValues, Vector paramNames ) {
		Vector r = new Vector();
		for( Iterator i=paramNames.iterator(); i.hasNext(); ) {
			r.add( paramValues.get( i.next() ) );
		}
		return r;
	}

}
