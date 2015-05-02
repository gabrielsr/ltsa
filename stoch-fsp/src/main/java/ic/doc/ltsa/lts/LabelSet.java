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

/* -----------------------------------------------------------------------*/

public class LabelSet {
    boolean isConstant=false;
    Vector labels;     // list of unevaluates ActionLabels, null if this is a constant set
    Vector actions;    // list of action names for an evaluated constant set

    static Hashtable constants; // hashtable of constant sets, <string,LabelSet>

    public LabelSet(Symbol s, Vector lbs) {
        labels = lbs;
        if(constants.put(s.toString(),this)!=null) {
            Diagnostics.fatal("duplicate set definition: "+s,s);
        }
        actions = getActions(null);  // name must be null here
        isConstant=true;
        labels = null;
    }

    public LabelSet(Vector lbs) {
        labels = lbs;
    }

    public Vector getActions(Hashtable params) {
        return getActions(null,params);
    }

    public Vector getActions(Hashtable locals, Hashtable params) {
      if (isConstant) return actions;
      if (labels ==null) return null;
      Vector v = new Vector();
      Hashtable dd = new Hashtable(); // detect and discard duplicates
      Hashtable mylocals = locals!=null?(Hashtable)locals.clone():null;
      Enumeration e = labels.elements();
      while (e.hasMoreElements()) {
         ActionLabels l = (ActionLabels)e.nextElement();
         l.initContext(mylocals,params);
         while(l.hasMoreNames()) {
            String s = l.nextName();
            if (!dd.containsKey(s)) {
                v.addElement(s);
                dd.put(s,s);
            }
         }
         l.clearContext();
      }
      return v;
    }


}
