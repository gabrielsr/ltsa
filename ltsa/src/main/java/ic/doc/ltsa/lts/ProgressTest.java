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

public class ProgressTest {

    String name;
    Vector pactions;   // vector of strings
    BitSet pset;
    Vector cactions;   //if P then C
    BitSet cset;

    static Vector tests; //  vector of progress tests

    public static void init(){
        tests = new Vector();
    }

    public ProgressTest(String name, Vector pactions, Vector cactions) {
        this.name = name;
        this.cactions = cactions;
        this.pactions = pactions;
        tests.addElement(this);
    }

    //return convert sets of actions to bitset using alphabet
    public static void initTests(String alphabet[]) {
        if (tests==null || tests.size()==0) return;
        // convert alphabet to hashtable
        Hashtable stoi = new Hashtable(alphabet.length);
        for (int i=0; i<alphabet.length; ++i)
            stoi.put(alphabet[i],new Integer(i));
        // for each key
        Enumeration e = tests.elements();
        while(e.hasMoreElements()) {
            ProgressTest p = (ProgressTest) e.nextElement();
            p.pset= alphaToBit(p.pactions,stoi);
            p.cset= alphaToBit(p.cactions,stoi);
        }
     }

    public static boolean noTests() {
        return (tests==null || tests.size()==0);
    }

    private static BitSet alphaToBit(Vector actions, Hashtable stoi) {
        if (actions==null) return null;
        BitSet b = new BitSet(stoi.size());
        Enumeration en = actions.elements();
        while(en.hasMoreElements()) {
             String s = (String)en.nextElement();
             Integer I = (Integer)stoi.get(s);
             if (I!=null) b.set(I.intValue());
        }
        return b;
    }


}

