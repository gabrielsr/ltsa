package ic.doc.ltsa.lts;

import java.awt.Font;

import ic.doc.ltsa.common.iface.*;
import ic.doc.ltsa.common.infra.EventManager;
import ic.doc.ltsa.frontend.gui.LTSCanvas;
import ic.doc.extension.IAnimator;
import ic.doc.ltsa.lts.ltl.AssertDefinition;
import ic.doc.ltsa.lts.ltl.PredicateDefinition;

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

        return new StochasticAnalyser();
    }

    public IAutomata createAutomata(ICompositeState pComp, LTSOutput pOut, EventManager pEvMag) {

        return new StochasticAnalyser(pComp, pOut, pEvMag);
    }

    public IAnimator createAnimator(ICompositeState pComp, LTSOutput pOut, EventManager pEvMag) {

        return new StochasticAnalyser(pComp, pOut, pEvMag);
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