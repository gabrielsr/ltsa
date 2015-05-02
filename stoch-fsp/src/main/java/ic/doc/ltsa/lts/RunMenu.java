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

import java.util.*;

public class RunMenu {

    public String name;
    public Vector alphabet;   // vector of strings
    public String params;
    public Relation actions;
    public Relation controls;

    public static Hashtable menus; //  vector of all menus

    public static void init(){
        menus = new Hashtable();
    }

    public RunMenu(String name, String params, Relation actions, Relation controls) {
        this.name = name;
        this.params = params;
        this.actions = actions;
        this.controls = controls;
        //menus.put(name,this);
    }

    public RunMenu(String name, Vector actions) {
        this.name = name;
        this.alphabet = actions;
        //menus.put(name,this);
    }
    
    public static void add(RunMenu r) {
       menus.put(r.name,r);
    }

    public boolean isCustom() { return params!=null;}

}
