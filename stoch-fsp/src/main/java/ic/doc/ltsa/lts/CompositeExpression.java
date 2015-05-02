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

/* -----------------------------------------------------------------------*/

class CompositionExpression {
    Symbol name;
    CompositeBody body;
    Hashtable constants;
    Hashtable init_constants = new Hashtable();  // constant table
    Vector parameters = new Vector();       // position of names in constants
    Map processes;                    // table of process definitions
    Map compiledProcesses;            // table of compiled definitions
    Map composites;                   // table of composite definitions
    LTSOutput output;                       // a bit of a hack
    boolean priorityIsLow = true;
    LabelSet priorityActions;                // priority action set
    LabelSet alphaHidden;                     // concealments
    boolean exposeNotHide=false;
    boolean makeDeterministic = false;
    boolean makeMinimal = false;
    boolean makeProperty = false;
    boolean makeCompose = false;

    CompositeState compose(Vector actuals,boolean probabilisticSystem) {
        Vector machines = new Vector(); // list of instantiated machines
        Hashtable locals = new Hashtable();
        constants = (Hashtable)init_constants.clone();
        Vector references;              // list of parsed process references
        if (actuals!=null) doParams(actuals);
        body.compose(this,machines,locals,probabilisticSystem);
        Vector flatmachines = new Vector();
        for (Enumeration e = machines.elements(); e.hasMoreElements();) {
            Object o = e.nextElement();
            if (o instanceof CompactState)
                flatmachines.addElement(o);
            else {
                CompositeState cs = (CompositeState)o;
                for (Enumeration ee = cs.machines.elements(); ee.hasMoreElements();) {
                    flatmachines.addElement(ee.nextElement());
                }
            }
        }
        String refname = (actuals==null)?name.toString() : name.toString()+StateMachine.paramString(actuals);
        CompositeState c = new CompositeState(refname,flatmachines);
        c.priorityIsLow = priorityIsLow;
        c.priorityLabels = computeAlphabet(priorityActions);
        c.hidden = computeAlphabet(alphaHidden);
        c.exposeNotHide = exposeNotHide;
        c.makeDeterministic = makeDeterministic;
        c.makeMinimal = makeMinimal;
        c.makeCompose = makeCompose;
        if (makeProperty) {c.makeDeterministic = true; c.isProperty = true;}
        return c;
    }

    private void doParams(Vector actuals) {
        Enumeration a = actuals.elements();
        Enumeration f = parameters.elements();
        while(a.hasMoreElements() && f.hasMoreElements())
            constants.put(f.nextElement(),a.nextElement());
    }

    private Vector computeAlphabet(LabelSet a) {
      if (a ==null) return null;
      return a.getActions(constants);
    }

}


