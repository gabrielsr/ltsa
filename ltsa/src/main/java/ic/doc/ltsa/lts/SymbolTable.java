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

public class SymbolTable {

	private static Hashtable keyword;

    static {
        keyword = new Hashtable();
        keyword.put ("const", new Integer (Symbol.CONSTANT));
        keyword.put ("property", new Integer(Symbol.PROPERTY));
        keyword.put ("range", new Integer(Symbol.RANGE));
        keyword.put ("if", new Integer(Symbol.IF));
        keyword.put ("then", new Integer(Symbol.THEN));
        keyword.put ("else", new Integer(Symbol.ELSE));
        keyword.put ("forall", new Integer(Symbol.FORALL));
        keyword.put ("when", new Integer(Symbol.WHEN));
        keyword.put ("set", new Integer(Symbol.SET));
        keyword.put ("progress", new Integer(Symbol.PROGRESS));
        keyword.put ("menu", new Integer(Symbol.MENU));
        keyword.put ("animation", new Integer(Symbol.ANIMATION));
        keyword.put ("actions", new Integer(Symbol.ACTIONS));
        keyword.put ("controls", new Integer(Symbol.CONTROLS));
        keyword.put ("deterministic", new Integer(Symbol.DETERMINISTIC));
        keyword.put ("minimal", new Integer(Symbol.MINIMAL));
        keyword.put ("compose", new Integer(Symbol.COMPOSE));
        keyword.put ("target", new Integer(Symbol.TARGET));
        keyword.put ("import", new Integer(Symbol.IMPORT));
        keyword.put ("assert", new Integer(Symbol.ASSERT));
        keyword.put ("fluent", new Integer(Symbol.PREDICATE));
		keyword.put ("exists", new Integer(Symbol.EXISTS));
		keyword.put ("rigid", new Integer(Symbol.RIGID));
		keyword.put ("fluent", new Integer(Symbol.PREDICATE));
		keyword.put ("constraint", new Integer(Symbol.CONSTRAINT));
        keyword.put ("initially", new Integer(Symbol.INIT));
        keyword.put ("include", new Integer(Symbol.INCLUDE));
     }

    public static Object get(String s) {
        return keyword.get(s);
    }
}