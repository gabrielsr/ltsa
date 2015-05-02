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

import java.awt.Font;

import ic.doc.ltsa.common.iface.*;
import ic.doc.ltsa.common.infra.EventManager;
import ic.doc.ltsa.frontend.gui.LTSCanvas;
import ic.doc.extension.IAnimator;
import ic.doc.ltsa.lts.ltl.AssertDefinition;
import ic.doc.ltsa.lts.ltl.PredicateDefinition;

/**
 * This class provides a factory for "backend" analysis components. It provides
 * an implementation of IAnalysis factory, and forms the binding point between
 * the backend component and the core. The factory plugs in to the core and the
 * core then calls it to create any instances of the analysis classes that it needs.
 * These can then all be encapsulated within the particular backend component.
 * 
 * This set of classes implements the "standard" backend, without support for
 * probabilistic models.
 * 
 * @author rbc
 */
public class AnalysisFactory implements IAnalysisFactory {

    private static IMenuDefinition sMenuDefinition = new MenuDefinition();
    private static IPredicateDefinition sPredicateDefinition = new PredicateDefinition();

    public static ISuperTrace createSuperTrace() {

        return new SuperTrace();
    }

    public IProgressCheck createProgressCheck() {

        return new ProgressCheck();
    }

    public IAssertDefinition createAssertDefinition() {

        return new AssertDefinition();
    }

    public IAutomata createAutomata() {

        return new Analyser();
    }

    public IAutomata createAutomata(ICompositeState pComp, LTSOutput pOut, EventManager pEvMag) {

        return new Analyser(pComp, pOut, pEvMag);
    }

    public IAnimator createAnimator(ICompositeState pComp, LTSOutput pOut, EventManager pEvMag) {

        return new Analyser(pComp, pOut, pEvMag);
    }

    public ITransitionPrinter createTransitionPrinter(ICompactState composition) {

        return new PrintTransitions(composition);
    }

    public ICompositeState createCompositeState() {

        return new CompositeState();
    }

    public IAlphabet createAlphabet(ICompactState state) {

        return new Alphabet(state);
    }

    public IDrawMachine createDrawMachine(ICompactState m, LTSCanvas canvas, Font nameFont, Font labelFont, boolean displayName, boolean newLabelFormat,
            int separation, int arcinc) {

        return new DrawMachine(m, canvas, nameFont, labelFont, displayName, newLabelFormat, separation, arcinc);
    }

    public IMenuDefinition getMenuDefinition() {

        return sMenuDefinition;
    }

    public IPredicateDefinition getPredicateDefinition() {

        return sPredicateDefinition;
    }
}