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

class ProgressDefinition {
    Symbol name;
    ActionLabels pactions;
    ActionLabels cactions; //if P then C
    ActionLabels range;    //range of tests

    static Hashtable definitions;

    public static void compile(){
        ProgressTest.init();
        Enumeration e = definitions.elements();
        while (e.hasMoreElements()){
            ProgressDefinition p = (ProgressDefinition)e.nextElement();
            p.makeProgressTest();
        }
    }

    public void makeProgressTest(){
        Vector pa=null;
        Vector ca=null;
        String na = name.toString();
        if (range==null) {
            pa = pactions.getActions(null,null);
            if (cactions!=null) ca = cactions.getActions(null,null);
            new ProgressTest(na,pa,ca);
        } else {
            Hashtable locals = new Hashtable();
            range.initContext(locals,null);
            while(range.hasMoreNames()) {
                String s = range.nextName();
                pa = pactions.getActions(locals,null);
                if (cactions!=null) ca = cactions.getActions(locals,null);
                new ProgressTest(na+"."+s,pa,ca);
            }
            range.clearContext();
        }
    }
}

