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

package ic.doc.ltsa.frontend.custom;

import java.util.*;

import ic.doc.ltsa.common.infra.Relation;

public class OutputActionRegistry {

    Hashtable outputs = new Hashtable(); //Hashtable of Vectors
    Relation actionMap;
    AnimationMessage msg;

    public OutputActionRegistry(Relation actionMap, AnimationMessage msg) {
        this.actionMap = actionMap;
        this.msg = msg;
    }

    public void register(String name, AnimationAction action) {
        Vector a = (Vector)outputs.get(name);
        if (a!=null) {
            a.addElement(action);
        } else {
            a = new Vector();
            a.addElement(action);
            outputs.put(name,a);
        }
    }

    public void doAction(String name) {
        msg.traceMsg(name);
        Object o = actionMap.get(name);
        if (o==null) {
            return;  //if its not mapped don't do it
            //execute(name);
        } else if (o instanceof String ) {
            execute((String)o);
        } else {
            Vector a = (Vector)o;
            Enumeration e = a.elements();
            while(e.hasMoreElements()) {
                execute((String)e.nextElement());
            }
        }
    }

    private void execute(String name) {
        msg.debugMsg("-action -" + name);
        Vector a = (Vector)outputs.get(name);
        if (a==null) return;
        Enumeration e = a.elements();
        while(e.hasMoreElements()) {
            AnimationAction action = (AnimationAction)e.nextElement();
            action.action();
        }
    }

}
