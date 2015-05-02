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

import ic.doc.ltsa.common.infra.Relation;

/* -----------------------------------------------------------------------*/

public class RelabelDefn {
    ActionLabels newlabel;
    ActionLabels oldlabel;
    ActionLabels range;
    Vector defns;

    public void makeRelabels(Hashtable constants, Relation relabels) {
        Hashtable locals = new Hashtable();
        mkRelabels(constants,locals,relabels);
    }

    public void makeRelabels(Hashtable constants, Hashtable locals, Relation relabels) {
        mkRelabels(constants,locals,relabels);
    }


    private void mkRelabels(Hashtable constants, Hashtable locals, Relation relabels) {
        if (range!=null) {
            range.initContext(locals,constants);
            while(range.hasMoreNames()) {
                range.nextName();
                Enumeration e = defns.elements();
                while(e.hasMoreElements()) {
                    RelabelDefn r = (RelabelDefn)e.nextElement();
                    r.mkRelabels(constants,locals,relabels);
                }
            }
            range.clearContext();
        } else {
            newlabel.initContext(locals,constants);
            while(newlabel.hasMoreNames()) {
                String newName=newlabel.nextName();
                oldlabel.initContext(locals,constants);
                while(oldlabel.hasMoreNames()) {
                    String oldName=oldlabel.nextName();
                    relabels.put(oldName,newName);
                }
            }
            newlabel.clearContext();
        }
    }

    public static Relation getRelabels(Vector relabelDefns, Hashtable constants, Hashtable locals){
        if (relabelDefns == null) return null;
        Relation relabels = new Relation();
        Enumeration e = relabelDefns.elements();
        while(e.hasMoreElements()) {
             RelabelDefn r = (RelabelDefn)e.nextElement();
             r.makeRelabels(constants,locals,relabels);
        }
        return relabels;
    }

    public static Relation getRelabels(Vector relabelDefns) {
        return getRelabels(relabelDefns, new Hashtable(), new Hashtable());
    }
}
