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

import ic.doc.ltsa.common.infra.Relation;
import ic.doc.ltsa.common.iface.ISymbol;
import ic.doc.ltsa.common.iface.LTSOutput;

import java.util.*;
import java.io.File;

class ProcessSpec extends Declaration implements Compilable {
    ISymbol name;
    Hashtable constants;
    Hashtable init_constants = new Hashtable();
    Vector parameters    = new Vector();
    Vector stateDefns   = new Vector();
    LabelSet alphaAdditions;
    LabelSet alphaHidden;
    Vector alphaRelabel;
    boolean isProperty = false;
    boolean isMinimal = false;
    boolean isDeterministic = false;
    boolean exposeNotHide = false;
    
    File importFile = null;   // used if the process is imported from a .aut file
    
	/* Compilable implementation */
	public CompactState makeCompactState( LTSOutput output, boolean probabilisticSystem ) {
		return makeCompactState( output, null, probabilisticSystem );
	}
    
	public CompactState makeCompactState( LTSOutput output, Vector params, boolean probabilisticSystem ) {
		CompactState c;
	
		if( !imported() ) {
				StateMachine sm = (params != null) ? new StateMachine( this, params, probabilisticSystem ) : new StateMachine( this, probabilisticSystem );
				c = sm.makeCompactState();
				output.outln( "Compiled: " + c.getName() );
		} else {
			c = new AutCompactState( name, importFile );
			output.outln( "Imported: " + c.getName() );
		}
		
		return c;
	}
	
	public int getNumberOfParameters() {
		return (parameters==null) ? 0 : parameters.size(); 
	}
   
    public boolean imported() {return importFile!=null;}
    
    public String getname() {
        constants = (Hashtable)init_constants.clone();
        StateDefn s = (StateDefn) stateDefns.firstElement();
        name = s.name;
        if (s.range != null)
            Diagnostics.fatal ("process name cannot be indexed", name);
        return s.name.toString();
    }

    public void explicitStates(StateMachine m) {
      Enumeration e = stateDefns.elements() ;
      while (e.hasMoreElements()) {
        Declaration d = (Declaration) e.nextElement();
        d.explicitStates(m);
      }
    }

    public void addAlphabet(StateMachine m) {
      if (alphaAdditions != null ) {
        Vector a = alphaAdditions.getActions(constants);
        Enumeration e = a.elements();
        while (e.hasMoreElements()) {
            String s = (String)e.nextElement();
            if(!m.alphabet.containsKey(s))
                m.alphabet.put(s,m.eventLabel.label());
        }
      }
    }

    public void hideAlphabet(StateMachine m) {
      if (alphaHidden ==null) return;
      m.hidden = alphaHidden.getActions(constants);
    }

    public void relabelAlphabet(StateMachine m) {
        if (alphaRelabel==null) return;
        m.relabels = new Relation();
        Enumeration e = alphaRelabel.elements();
        while(e.hasMoreElements()) {
           RelabelDefn r = (RelabelDefn)e.nextElement();
           r.makeRelabels(constants,m.relabels);
        }
    }

    public void crunch(StateMachine m,boolean probabilisticSystem) {
      Enumeration e = stateDefns.elements() ;
      while (e.hasMoreElements()) {
        Declaration d = (Declaration) e.nextElement();
        d.crunch(m,probabilisticSystem);
      }
    }

    public void transition(StateMachine m,boolean probabilisticSystem) {
      Enumeration e = stateDefns.elements() ;
      while (e.hasMoreElements()) {
        Declaration d = (Declaration) e.nextElement();
        d.transition(m,probabilisticSystem);
      }
    }

    public void doParams(Vector actuals) {
        Enumeration a = actuals.elements();
        Enumeration f = parameters.elements();
        while(a.hasMoreElements() && f.hasMoreElements())
            constants.put(f.nextElement(),a.nextElement());
    }

    public ProcessSpec myclone() {
    	  ProcessSpec p = new ProcessSpec();
    	  p.name = name;
       p.constants = (Hashtable)constants.clone();
       p.init_constants = init_constants;
       p.parameters    = parameters;
       Enumeration e = stateDefns.elements();
       while(e.hasMoreElements())
       	p.stateDefns.addElement(((StateDefn)e.nextElement()).myclone());
       p.alphaAdditions = alphaAdditions;
       p.alphaHidden = alphaHidden;
    		p.alphaRelabel = alphaRelabel;
    		p.isProperty = isProperty;
    		p.isMinimal = isMinimal;
    		p.isDeterministic = isDeterministic;
    		p.exposeNotHide = exposeNotHide;
    		p.importFile = importFile;
    		return p;
    }

	public String toString() {
		return stateDefns.toString();
	}

}
