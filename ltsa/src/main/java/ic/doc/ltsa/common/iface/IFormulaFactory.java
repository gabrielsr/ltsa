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

package ic.doc.ltsa.common.iface;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;

import ic.doc.ltsa.common.ast.IFormula;

public interface IFormulaFactory {

    IFormula make(ISymbol proposition);

    void setFormula(IFormula formula);

    IFormula make(ISymbol proposition, IActionLabels range, Hashtable locals, Hashtable globals);

    IFormula makeOr(IFormula f, IFormula formula);

    IFormula makeAnd(IFormula f, IFormula formula);

    IFormula make(IFormula formula, ISymbol operator, IFormula formula2);

    IFormula make(Stack stack, Hashtable locals, Hashtable globals);

    IFormula getFormula();

    SortedSet getProps();

    int processUntils(IFormula formula, List list);

    boolean syntaxImplied(IFormula sub2, Set one, Set two);

    IFormula makeNot(IFormula f);

    IFormula make(IActionLabels action, Hashtable locals, Hashtable globals);

    boolean specialCaseV(IFormula f, Set one);

    Hashtable getActionPredicates();
}
