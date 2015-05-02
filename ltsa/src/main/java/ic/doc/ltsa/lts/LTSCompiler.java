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

import ic.doc.ltsa.common.iface.IActionLabels;
import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.IFormulaSyntax;
import ic.doc.ltsa.common.iface.ILTSCompiler;
import ic.doc.ltsa.common.iface.ISymbol;
import ic.doc.ltsa.common.iface.LTSInput;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.*;

import java.util.*;
import java.io.File;

import ic.doc.ltsa.lts.ltl.*;

public class LTSCompiler implements ILTSCompiler {

	private Lex lex;

	private LTSOutput output;

	private String currentDirectory;

	private File currentFile;

	private Symbol current;

	private InputSource source;

	static Hashtable processes;

	static Hashtable compiled;

	static Hashtable composites;

	public LTSCompiler(LTSInput input, LTSOutput output,
			String currentDirectory, File p_currentFile) {

		this(input, output, currentDirectory);
		currentFile = p_currentFile;
		//System.out.println("LTSCompiler.constructor: "+currentFile.getAbsolutePath());
	}

	public LTSCompiler(LTSInput input, LTSOutput output, String currentDirectory) {

		lex = new Lex(input);
		this.output = output;
		this.currentDirectory = currentDirectory;
		Diagnostics.init(output);
		SeqProcessRef.output = output;
		StateMachine.output = output;
		Expression.constants = new Hashtable();
		Range.ranges = new Hashtable();
		LabelSet.constants = new Hashtable();
		ProgressDefinition.definitions = new Hashtable();
		MenuDefinition.definitions = new Hashtable();
		PredicateDefinition.init();
		AssertDefinition.init();
	}

	private Symbol next_symbol() {
		current = lex.next_symbol();
		//System.out.print("LTSCompiler in ltsa project: "+current.toString());
		return current;//(current = lex.next_symbol());
	}

	private void push_symbol() {
		lex.push_symbol();
	}

	private void error(String errorMsg) {

		Diagnostics.fatal(errorMsg, current);
	}

	private void current_is(int kind, String errorMsg) {
		if (current.kind != kind)
			error(errorMsg);
	}

	public ICompositeState compile(String name) {
		processes = new Hashtable(); // processes
		composites = new Hashtable(); // composites
		compiled = new Hashtable(); // compiled
		doparse(composites, processes, compiled);
		ProgressDefinition.compile();
		MenuDefinition.compile();
		PredicateDefinition.compileAll();
		AssertDefinition.compileAll(output);
		CompositionExpression ce = (CompositionExpression) composites.get(name);
		if (ce == null && composites.size() > 0) {
			Enumeration e = composites.elements();
			ce = (CompositionExpression) e.nextElement();
		}
		if (ce != null)
			return ce.compose(null);
		else {
			compileProcesses(processes, compiled);
			return noCompositionExpression(compiled);
		}
	}

	// put compiled definitions in Hashtable compiled
	private void compileProcesses(Hashtable h, Hashtable compiled) {
		Enumeration e = h.elements();
		while (e.hasMoreElements()) {
			ProcessSpec p = (ProcessSpec) e.nextElement();
			if (!p.imported()) {
				StateMachine one = new StateMachine(p);
				CompactState c = one.makeCompactState();
				output.outln("Compiled: " + c.getName());
				compiled.put(c.getName(), c);
			} else {
				CompactState c = new AutCompactState(p.name, p.importFile);
				output.outln("Imported: " + c.getName());
				compiled.put(c.getName(), c);
			}
		}
		AssertDefinition.compileConstraints(output, compiled);
	}

	public void parse(Map comps, Map procs) {
		doparse(comps, procs, null);
	}

	public Map resolveIncludes(Map pFiles) {

		next_symbol();
		while (current.kind != Symbol.EOFSYM) {

			if (current.kind == Symbol.INCLUDE) {

				next_symbol();
				processInclude(pFiles);
			}

			next_symbol();
		}

		return pFiles;
	}

	private void processInclude(Map p_files) {

		current_is(Symbol.STRING_VALUE, " - included file name expected");

		File x_file = new File(currentDirectory, current.toString());
		LTSInput x_extern = new InputSource(x_file);

		if (!p_files.containsKey(x_file)) {

			p_files.put(x_file, x_extern);

			ILTSCompiler x_comp = new LTSCompiler(x_extern, output,
					currentDirectory, x_file);
			x_comp.resolveIncludes(p_files);
		}
	}

	private void doparse(Map comps, Map procs, Map compiled) {
		next_symbol();
		while (current.kind != Symbol.EOFSYM) {
			if (current.kind == Symbol.CONSTANT) {
				next_symbol();
				constantDefinition(Expression.constants);
			} else if (current.kind == Symbol.RANGE) {
				next_symbol();
				rangeDefinition();
			} else if (current.kind == Symbol.SET) {
				next_symbol();
				setDefinition();
			} else if (current.kind == Symbol.PROGRESS) {
				next_symbol();
				progressDefinition();
			} else if (current.kind == Symbol.MENU) {
				next_symbol();
				menuDefinition();
			} else if (current.kind == Symbol.ANIMATION) {
				next_symbol();
				animationDefinition();
			} else if (current.kind == Symbol.ASSERT) {
				next_symbol();
				assertDefinition(false);
			} else if (current.kind == Symbol.CONSTRAINT) {
				next_symbol();
				assertDefinition(true);
			} else if (current.kind == Symbol.PREDICATE) {
				next_symbol();
				predicateDefinition();
			} else if (current.kind == Symbol.IMPORT) {
				next_symbol();
				ProcessSpec p = importDefinition();
				if (procs.put(p.name.toString(), p) != null) {
					Diagnostics.fatal(
							"duplicate process definition: " + p.name, p.name);
				}
			} else if (current.kind == Symbol.INCLUDE) {

				next_symbol();
				current_is(Symbol.STRING_VALUE,
						" - included file name expected");

			} else if (current.kind == Symbol.OR
					|| current.kind == Symbol.DETERMINISTIC
					|| current.kind == Symbol.MINIMAL
					|| current.kind == Symbol.PROPERTY
					|| current.kind == Symbol.COMPOSE) {
				boolean makeDet = false;
				boolean makeMin = false;
				boolean makeProp = false;
				boolean makeComp = false;
				if (current.kind == Symbol.DETERMINISTIC) {
					makeDet = true;
					next_symbol();
				}
				if (current.kind == Symbol.MINIMAL) {
					makeMin = true;
					next_symbol();
				}
				if (current.kind == Symbol.COMPOSE) {
					makeComp = true;
					next_symbol();
				}
				if (current.kind == Symbol.PROPERTY) {
					makeProp = true;
					next_symbol();
				}
				if (current.kind != Symbol.OR) {
					ProcessSpec p = stateDefns();
					if (procs.put(p.name.toString(), p) != null) {
						Diagnostics.fatal("duplicate process definition: "
								+ p.name, p.name);
					}
					p.isProperty = makeProp;
					p.isMinimal = makeMin;
					p.isDeterministic = makeDet;
				} else if (current.kind == Symbol.OR) {
					CompositionExpression c = composition();
					c.composites = composites;
					c.processes = processes;
					c.compiledProcesses = compiled;
					c.output = output;
					c.makeDeterministic = makeDet;
					c.makeProperty = makeProp;
					c.makeMinimal = makeMin;
					c.makeCompose = makeComp;
					if (comps.put(c.name.toString(), c) != null) {
						Diagnostics.fatal("duplicate composite definition: "
								+ c.name, c.name);
					}
				}
			} else {
				ProcessSpec p = stateDefns();
				if (procs.put(p.name.toString(), p) != null) {
					Diagnostics.fatal(
							"duplicate process definition: " + p.name, p.name);
				}
			}

			next_symbol();
		}
	}

	private CompositeState noCompositionExpression(Hashtable h) {
		Vector v = new Vector(16);
		Enumeration e = h.elements();
		while (e.hasMoreElements()) {
			v.addElement(e.nextElement());
		}
		return new CompositeState(v);
	}

	private CompositionExpression composition() {
		current_is(Symbol.OR, "|| expected");
		next_symbol();
		CompositionExpression c = new CompositionExpression();
		current_is(Symbol.UPPERIDENT, "process identifier expected");
		c.name = current;
		next_symbol();
		paramDefns(c.init_constants, c.parameters);
		current_is(Symbol.BECOMES, "= expected");
		next_symbol();
		c.body = compositebody();
		c.priorityActions = priorityDefn(c);
		if (current.kind == Symbol.BACKSLASH || current.kind == Symbol.AT) {
			c.exposeNotHide = (current.kind == Symbol.AT);
			next_symbol();
			c.alphaHidden = labelSet();
		}
		current_is(Symbol.DOT, "dot expected");
		return c;
	}

	private CompositeBody compositebody() {
		CompositeBody b = new CompositeBody();
		if (current.kind == Symbol.IF) {
			next_symbol();
			b.boolexpr = new Stack();
			expression(b.boolexpr);
			current_is(Symbol.THEN, "keyword then expected");
			next_symbol();
			b.thenpart = compositebody();
			if (current.kind == Symbol.ELSE) {
				next_symbol();
				b.elsepart = compositebody();
			}
		} else if (current.kind == Symbol.FORALL) {
			next_symbol();
			b.range = forallRanges();
			b.thenpart = compositebody();
		} else {
			// get accessors if any
			if (isLabel()) {
				ActionLabels el = labelElement();
				if (current.kind == Symbol.COLON_COLON) {
					b.accessSet = el;
					next_symbol();
					if (isLabel()) {
						b.prefix = labelElement();
						current_is(Symbol.COLON, " : expected");
						next_symbol();
					}
				} else if (current.kind == Symbol.COLON) {
					b.prefix = el;
					next_symbol();
				} else
					error(" : or :: expected");
			}
			if (current.kind == Symbol.LROUND) {
				b.procRefs = processRefs();
				b.relabelDefns = relabelDefns();
			} else {
				b.singleton = processRef();
				b.relabelDefns = relabelDefns();
			}
		}
		return b;
	}

	private ActionLabels forallRanges() {
		current_is(Symbol.LSQUARE, "range expected");
		ActionLabels head = range();
		ActionLabels next = head;
		while (current.kind == Symbol.LSQUARE) {
			ActionLabels t = range();
			next.addFollower(t);
			next = t;
		}
		return head;
	}

	private Vector processRefs() {
		Vector procRefs = new Vector();
		current_is(Symbol.LROUND, "( expected");
		next_symbol();
		if (current.kind != Symbol.RROUND) {
			procRefs.addElement(compositebody());
			while (current.kind == Symbol.OR) {
				next_symbol();
				procRefs.addElement(compositebody());
			}
			current_is(Symbol.RROUND, ") expected");
		}
		next_symbol();
		return procRefs;
	}

	private Vector relabelDefns() {
		if (current.kind != Symbol.DIVIDE)
			return null;
		next_symbol();
		return relabelSet();
	}

	private LabelSet priorityDefn(CompositionExpression c) {
		if (current.kind != Symbol.SHIFT_RIGHT
				&& current.kind != Symbol.SHIFT_LEFT)
			return null;
		if (current.kind == Symbol.SHIFT_LEFT)
			c.priorityIsLow = false;
		next_symbol();
		return labelSet();
	}

	private Vector relabelSet() {
		current_is(Symbol.LCURLY, "{ expected");
		next_symbol();
		Vector v = new Vector();
		v.addElement(relabelDefn());
		while (current.kind == Symbol.COMMA) {
			next_symbol();
			v.addElement(relabelDefn());
		}
		current_is(Symbol.RCURLY, "} expected");
		next_symbol();
		return v;
	}

	private RelabelDefn relabelDefn() {
		RelabelDefn r = new RelabelDefn();
		if (current.kind == Symbol.FORALL) {
			next_symbol();
			r.range = forallRanges();
			r.defns = relabelSet();
		} else {
			r.newlabel = labelElement();
			current_is(Symbol.DIVIDE, "/ expected");
			next_symbol();
			r.oldlabel = labelElement();
		}
		return r;
	}

	private ProcessRef processRef() {
		ProcessRef p = new ProcessRef();
		current_is(Symbol.UPPERIDENT, "process identifier expected");
		p.name = current;
		next_symbol();
		p.actualParams = actualParameters();
		return p;
	}

	private Vector actualParameters() {
		if (current.kind != Symbol.LROUND)
			return null;
		Vector v = new Vector();
		next_symbol();
		Stack stk = new Stack();
		expression(stk);
		v.addElement(stk);
		while (current.kind == Symbol.COMMA) {
			next_symbol();
			stk = new Stack();
			expression(stk);
			v.addElement(stk);
		}
		current_is(Symbol.RROUND, ") - expected");
		next_symbol();
		return v;
	}

	private ProcessSpec stateDefns() {
		ProcessSpec p = new ProcessSpec();

		current_is(Symbol.UPPERIDENT, "process identifier expected");
		Symbol temp = current;
		next_symbol();
		paramDefns(p.init_constants, p.parameters);
		push_symbol();
		current = temp;
		p.stateDefns.addElement(stateDefn());
		while (current.kind == Symbol.COMMA) {
			next_symbol();
			p.stateDefns.addElement(stateDefn());
		}
		if (current.kind == Symbol.PLUS) {
			next_symbol();
			p.alphaAdditions = labelSet();
		}
		p.alphaRelabel = relabelDefns();
		if (current.kind == Symbol.BACKSLASH || current.kind == Symbol.AT) {
			p.exposeNotHide = (current.kind == Symbol.AT);
			next_symbol();
			p.alphaHidden = labelSet();
		}
		p.getname();
		current_is(Symbol.DOT, "dot expected");
		return p;
	}

	private boolean isLabelSet() {
		if (current.kind == Symbol.LCURLY)
			return true;
		if (current.kind != Symbol.UPPERIDENT)
			return false;
		return LabelSet.constants.containsKey(current.toString());
	}

	private boolean isLabel() {
		return (isLabelSet() || current.kind == Symbol.IDENTIFIER || current.kind == Symbol.LSQUARE);
	}

	private ProcessSpec importDefinition() {
		current_is(Symbol.UPPERIDENT, "imported process identifier expected");
		ProcessSpec p = new ProcessSpec();
		p.name = current;
		next_symbol();
		current_is(Symbol.BECOMES, "= expected");
		next_symbol();
		current_is(Symbol.STRING_VALUE, " - imported file name expected");
		p.importFile = new File(currentDirectory, current.toString());
		return p;
	}

	private Map consumeIncludeStatement() {

		current_is(Symbol.UPPERIDENT, "imported process identifier expected");
		String x_process = current.toString();
		next_symbol();
		current_is(Symbol.STRING_VALUE, " - included file name expected");

		File x_file = new File(currentDirectory, current.toString());
		LTSInput x_extern = new InputSource(x_file);
		ILTSCompiler x_comp = new LTSCompiler(x_extern, output,
				currentDirectory, x_file);

		Map x_procs = new Hashtable();
		Map x_comps = new Hashtable();
		Map x_ret = new Hashtable();

		x_comp.parse(x_procs, x_comps);

		x_ret.putAll(x_procs);
		x_ret.putAll(x_comps);

		// for (Iterator i = x_procs.keySet().iterator(); i.hasNext();) {
		// Symbol x_name = (Symbol)i.next();
		// if ( x_name.toString().equals( x_process )) {
		// x_ret.put( x_name , x_procs.get( x_name ) );
		// }
		// }
		//        	
		// for (Iterator i = x_comps.keySet().iterator(); i.hasNext();) {
		// String x_name = (String)i.next();
		// if ( x_name.equals( x_process )) {
		// x_ret.put( x_name , x_comps.get( x_name ) );
		// }
		// }

		return x_ret;
	}

	private void animationDefinition() {
		current_is(Symbol.UPPERIDENT, "animation identifier expected");
		MenuDefinition m = new MenuDefinition();
		m.setName(current);
		next_symbol();
		current_is(Symbol.BECOMES, "= expected");
		next_symbol();
		current_is(Symbol.STRING_VALUE, " - XML file name expected");
		m.setParams(current);
		next_symbol();
		if (current.kind == Symbol.TARGET) {
			next_symbol();
			current_is(Symbol.UPPERIDENT, " - target composition name expected");
			m.setTarget(current);
			next_symbol();
		}
		if (current.kind == Symbol.COMPOSE) {
			next_symbol();
			current_is(Symbol.LCURLY, "{ expected");
			next_symbol();
			current_is(Symbol.UPPERIDENT, "animation name expected");
			Symbol name = current;
			next_symbol();
			m.addAnimationPart(name, relabelDefns());
			while (current.kind == Symbol.OR) {
				next_symbol();
				current_is(Symbol.UPPERIDENT, "animation name expected");
				name = current;
				next_symbol();
				m.addAnimationPart(name, relabelDefns());
			}
			current_is(Symbol.RCURLY, "} expected");
			next_symbol();
		}
		if (current.kind == Symbol.ACTIONS) {
			next_symbol();
			m.setActionMapDefn(relabelSet());
		}
		if (current.kind == Symbol.CONTROLS) {
			next_symbol();
			m.setControlMapDefn(relabelSet());
		}
		push_symbol();
		if (MenuDefinition.definitions.put(m.getName().toString(), m) != null) {
			Diagnostics.fatal("duplicate menu/animation definition: "
					+ m.getName(), m.getName());
		}
	}

	private void menuDefinition() {
		current_is(Symbol.UPPERIDENT, "menu identifier expected");
		MenuDefinition m = new MenuDefinition();
		m.setName(current);
		next_symbol();
		current_is(Symbol.BECOMES, "= expected");
		next_symbol();
		m.setActions(labelElement());
		push_symbol();
		if (MenuDefinition.definitions.put(m.getName().toString(), m) != null) {
			Diagnostics.fatal("duplicate menu/animation definition: "
					+ m.getName(), m.getName());
		}
	}

	private void progressDefinition() {
		current_is(Symbol.UPPERIDENT, "progress test identifier expected");
		ProgressDefinition p = new ProgressDefinition();
		p.name = current;
		next_symbol();
		if (current.kind == Symbol.LSQUARE)
			p.range = forallRanges();
		current_is(Symbol.BECOMES, "= expected");
		next_symbol();
		if (current.kind == Symbol.IF) {
			next_symbol();
			p.pactions = labelElement();
			current_is(Symbol.THEN, "then expected");
			next_symbol();
			p.cactions = labelElement();
		} else {
			p.pactions = labelElement();
		}
		if (ProgressDefinition.definitions.put(p.name.toString(), p) != null) {
			Diagnostics.fatal("duplicate progress test: " + p.name, p.name);
		}
		push_symbol();
	}

	private void setDefinition() {
		current_is(Symbol.UPPERIDENT, "set identifier expected");
		Symbol temp = current;
		next_symbol();
		current_is(Symbol.BECOMES, "= expected");
		next_symbol();
		LabelSet ls = new LabelSet(temp, setValue());
		push_symbol();
	}

	private LabelSet labelSet() {
		if (current.kind == Symbol.LCURLY)
			return new LabelSet(setValue());
		else if (current.kind == Symbol.UPPERIDENT) {
			LabelSet ls = (LabelSet) LabelSet.constants.get(current.toString());
			if (ls == null)
				error("set definition not found for: " + current);
			next_symbol();
			return ls;
		} else {
			error("{ or set identifier expected");
			return null;
		}
	}

	private Vector setValue() {
		current_is(Symbol.LCURLY, "{ expected");
		next_symbol();
		Vector v = new Vector();
		v.addElement(labelElement());
		while (current.kind == Symbol.COMMA) {
			next_symbol();
			v.addElement(labelElement());
		}
		current_is(Symbol.RCURLY, "} expected");
		next_symbol();
		return v;
	}

	private ActionLabels labelElement() {;
		if (current.kind != Symbol.IDENTIFIER && !isLabelSet()
				&& current.kind != Symbol.LSQUARE) {
			System.err.println("LTSCompiler.labelElement: \n\tcurrent.kind: "
					+ current.kind + "\n\tcurrent.toString(): "
					+ current.toString());
			error("identifier, label set or range expected");
		}
		ActionLabels e = null;
		if (current.kind == Symbol.IDENTIFIER) {
			if ("tau".equals(current.toString()))
				error("'tau' cannot be used as an action label");
			e = new ActionName(current);
			next_symbol();
		} else if (isLabelSet()) {
			LabelSet left = labelSet();
			if (current.kind == Symbol.BACKSLASH) {
				next_symbol();
				LabelSet right = labelSet();
				e = new ActionSetExpr(left, right);
			} else {
				e = new ActionSet(left);
			}
		} else if (current.kind == Symbol.LSQUARE)
			e = range();
		if (current.kind == Symbol.DOT || current.kind == Symbol.LSQUARE) {
			if (current.kind == Symbol.DOT)
				next_symbol();
			if (e != null)
				e.addFollower(labelElement());
		}
		return e;
	}

	private void constantDefinition(Hashtable p) {
		current_is(Symbol.UPPERIDENT,
				"constant, upper case identifier expected");
		Symbol name = current;
		next_symbol();
		current_is(Symbol.BECOMES, "= expected");
		next_symbol();
		Stack tmp = new Stack();
		simpleExpression(tmp);
		push_symbol();
		if (p.put(name.toString(), Expression.getValue(tmp, null, null)) != null) {
			Diagnostics.fatal("duplicate constant definition: " + name, name);
		}
	}

	private void paramDefns(Hashtable p, Vector parameters) {
		if (current.kind == Symbol.LROUND) {
			next_symbol();
			parameterDefinition(p, parameters);
			while (current.kind == Symbol.COMMA) {
				next_symbol();
				parameterDefinition(p, parameters);
			}
			current_is(Symbol.RROUND, ") expected");
			next_symbol();
		}
	}

	private void parameterDefinition(Hashtable p, Vector parameters) {
		current_is(Symbol.UPPERIDENT,
				"parameter, upper case identifier expected");
		Symbol name = current;
		next_symbol();
		current_is(Symbol.BECOMES, "= expected");
		next_symbol();
		Stack tmp = new Stack();
		expression(tmp);
		push_symbol();
		if (p.put(name.toString(), Expression.getValue(tmp, null, null)) != null) {
			Diagnostics.fatal("duplicate parameter definition: " + name, name);
		}
		if (parameters != null) {
			parameters.addElement(name.toString());
			next_symbol();
		}
	}

	private StateDefn stateDefn() {
		StateDefn s = new StateDefn();
		current_is(Symbol.UPPERIDENT, "process identifier expected");
		s.name = current;
		next_symbol();
		if (current.kind == Symbol.AT) {
			s.accept = true;
			next_symbol();
		}
		if (current.kind == Symbol.DOT || current.kind == Symbol.LSQUARE) {
			if (current.kind == Symbol.DOT)
				next_symbol();
			s.range = labelElement();
		}
		current_is(Symbol.BECOMES, "= expected");
		next_symbol();
		s.stateExpr = stateExpr();
		return s;
	}

	private Stack getEvaluatedExpression() {
		Stack tmp = new Stack();
		simpleExpression(tmp);
		int v = Expression.evaluate(tmp, null, null);
		tmp = new Stack();
		tmp.push(new Symbol(Symbol.INT_VALUE, v));
		return tmp;
	}

	private void rangeDefinition() {
		current_is(Symbol.UPPERIDENT,
				"range name, upper case identifier expected");
		Symbol name = current;
		next_symbol();
		current_is(Symbol.BECOMES, "= expected");
		next_symbol();
		Range r = new Range();
		r.low = getEvaluatedExpression();
		current_is(Symbol.DOT_DOT, "..  expected");
		next_symbol();
		r.high = getEvaluatedExpression();
		if (Range.ranges.put(name.toString(), r) != null) {
			Diagnostics.fatal("duplicate range definition: " + name, name);
			;
		}
		push_symbol();
	}

	private ActionLabels range() { // this is a mess.. needs to be rewritten
		if (current.kind == Symbol.LSQUARE) {
			next_symbol();
			ActionLabels r;
			Stack low = null;
			Stack high = null;
			if (current.kind != Symbol.IDENTIFIER) {
				if (isLabelSet()) {
					r = new ActionSet(labelSet());
				} else if (current.kind == Symbol.UPPERIDENT
						&& Range.ranges.containsKey(current.toString())) {
					r = new ActionRange((Range) Range.ranges.get(current
							.toString()));
					next_symbol();
				} else {
					low = new Stack();
					expression(low);
					r = new ActionExpr(low);
				}
				if (current.kind == Symbol.DOT_DOT) {
					next_symbol();
					high = new Stack();
					expression(high);
					r = new ActionRange(low, high);
				}
			} else {
				Symbol varname = current;
				next_symbol();
				if (current.kind == Symbol.COLON) {
					next_symbol();
					if (isLabelSet()) {
						r = new ActionVarSet(varname, labelSet());
					} else if (current.kind == Symbol.UPPERIDENT
							&& Range.ranges.containsKey(current.toString())) {
						r = new ActionVarRange(varname, (Range) Range.ranges
								.get(current.toString()));
						next_symbol();
					} else {
						low = new Stack();
						expression(low);
						current_is(Symbol.DOT_DOT, "..  expected");
						next_symbol();
						high = new Stack();
						expression(high);
						r = new ActionVarRange(varname, low, high);
					}
				} else {
					push_symbol();
					current = varname;
					low = new Stack();
					expression(low);
					if (current.kind == Symbol.DOT_DOT) {
						next_symbol();
						high = new Stack();
						expression(high);
						r = new ActionRange(low, high);
					} else
						r = new ActionExpr(low);
				}
			}
			current_is(Symbol.RSQUARE, "] expected");
			next_symbol();
			return r;
		} else
			return null;
	}

	private StateExpr stateExpr() {
		StateExpr s = new StateExpr();
		if (current.kind == Symbol.UPPERIDENT)
			stateRef(s);
		else if (current.kind == Symbol.IF) {
			next_symbol();
			s.boolexpr = new Stack();
			expression(s.boolexpr);
			current_is(Symbol.THEN, "keyword then expected");
			next_symbol();
			s.thenpart = stateExpr();
			if (current.kind == Symbol.ELSE) {
				next_symbol();
				s.elsepart = stateExpr();
			} else {
				Symbol stop = new Symbol(Symbol.UPPERIDENT, "STOP");
				StateExpr se = new StateExpr();
				se.name = stop;
				s.elsepart = se;
			}
		} else if (current.kind == Symbol.LROUND) {
			next_symbol();
			choiceExpr(s);
			current_is(Symbol.RROUND, ") expected");
			next_symbol();
		} else
			error(" (, if or process identifier expected");

		return s;
	}

	private void stateRef(StateExpr s) {
		current_is(Symbol.UPPERIDENT, "process identifier expected");
		s.name = current;
		next_symbol();
		while (current.kind == Symbol.SEMICOLON
				|| current.kind == Symbol.LROUND) {
			s.addSeqProcessRef(new SeqProcessRef(s.name, actualParameters()));
			next_symbol();
			current_is(Symbol.UPPERIDENT, "process identifier expected");
			s.name = current;
			next_symbol();
		}
		if (current.kind == Symbol.LSQUARE) {
			s.expr = new Vector();
			while (current.kind == Symbol.LSQUARE) {
				next_symbol();
				Stack x = new Stack();
				expression(x);
				s.expr.addElement(x);
				current_is(Symbol.RSQUARE, "] expected");
				next_symbol();
			}
		}
	}

	private void choiceExpr(StateExpr s) {
		s.choices = new Vector();
		s.choices.addElement(choiceElement());
		while (current.kind == Symbol.BITWISE_OR) {
			next_symbol();
			s.choices.addElement(choiceElement());
		}
	}

	private ChoiceElement choiceElement() {
		ChoiceElement first = new ChoiceElement();
		if (current.kind == Symbol.WHEN) {
			next_symbol();
			first.guard = new Stack();
			expression(first.guard);
		}
		first.action = labelElement();
		current_is(Symbol.ARROW, "-> expected");
		ChoiceElement next = first;
		ChoiceElement last = first;
		next_symbol();
		while (current.kind == Symbol.IDENTIFIER
				|| current.kind == Symbol.LSQUARE || isLabelSet()) {
			StateExpr ex = new StateExpr();
			next = new ChoiceElement();
			next.action = labelElement();
			ex.choices = new Vector();
			ex.choices.addElement(next);
			last.stateExpr = ex;
			last = next;
			current_is(Symbol.ARROW, "-> expected");
			next_symbol();
		}
		next.stateExpr = stateExpr();
		return first;
	}

	private Symbol event() {
		current_is(Symbol.IDENTIFIER, "event identifier expected");
		Symbol e = current;
		next_symbol();
		return e;
	}

	// LABELCONSTANT -------------------------------

	private ActionLabels labelConstant() {
		next_symbol();
		ActionLabels el = labelElement();
		if (el != null) {
			return el;
		} else
			error("label definition expected");
		return null;
	}

	// set selection @(set , expr)
	private void set_select(Stack expr) {
		Symbol op = current;
		next_symbol();
		current_is(Symbol.LROUND, "( expected to start set index selection");
		Symbol temp = current; // preserve marker
		temp.setAny(labelConstant());
		temp.kind = Symbol.LABELCONST;
		expr.push(temp);
		current_is(Symbol.COMMA, ", expected before set index expression");
		next_symbol();
		expression(expr);
		current_is(Symbol.RROUND, ") expected to end set index selection");
		next_symbol();
		expr.push(op);
	}

	// UNARY ---------------------------------
	private void unary(Stack expr) { // +, -, identifier,
		Symbol unary_operator;
		switch (current.kind) {
		case Symbol.PLUS:
			unary_operator = current;
			unary_operator.kind = Symbol.UNARY_PLUS;
			next_symbol();
			break;
		case Symbol.MINUS:
			unary_operator = current;
			unary_operator.kind = Symbol.UNARY_MINUS;
			next_symbol();
			break;
		case Symbol.PLING:
			unary_operator = current;
			next_symbol();
			break;

		default:
			unary_operator = null;
		}
		switch (current.kind) {
		case Symbol.IDENTIFIER:
		case Symbol.UPPERIDENT:
		case Symbol.INT_VALUE:
			expr.push(current);
			next_symbol();
			break;
		case Symbol.LROUND:
			next_symbol();
			expression(expr);
			current_is(Symbol.RROUND, ") expected to end expression");
			next_symbol();
			break;
		case Symbol.HASH:
			unary_operator = new Symbol(current);
		case Symbol.QUOTE: // this is a labelConstant
			Symbol temp = current; // preserve marker
			temp.setAny(labelConstant());
			temp.kind = Symbol.LABELCONST;
			expr.push(temp);
			break;
		case Symbol.AT:
			set_select(expr);
			break;
		default:
			error("syntax error in expression");
		}

		if (unary_operator != null)
			expr.push(unary_operator);
	}

	// MULTIPLICATIVE

	private void multiplicative(Stack expr) { // *, /, %
		unary(expr);
		while (current.kind == Symbol.STAR || current.kind == Symbol.DIVIDE
				|| current.kind == Symbol.MODULUS) {
			Symbol op = current;
			next_symbol();
			unary(expr);
			expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// ADDITIVE

	private void additive(Stack expr) { // +, -
		multiplicative(expr);
		while (current.kind == Symbol.PLUS || current.kind == Symbol.MINUS) {
			Symbol op = current;
			next_symbol();
			multiplicative(expr);
			expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// SHIFT

	private void shift(Stack expr) { // <<, >>
		additive(expr);
		while (current.kind == Symbol.SHIFT_LEFT
				|| current.kind == Symbol.SHIFT_RIGHT) {
			Symbol op = current;
			next_symbol();
			additive(expr);
			expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// RELATIONAL

	private void relational(Stack expr) { // <, <=, >, >=
		shift(expr);
		while (current.kind == Symbol.LESS_THAN
				|| current.kind == Symbol.LESS_THAN_EQUAL
				|| current.kind == Symbol.GREATER_THAN
				|| current.kind == Symbol.GREATER_THAN_EQUAL) {
			Symbol op = current;
			next_symbol();
			shift(expr);
			expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// EQUALITY

	private void equality(Stack expr) { // ==, !=
		relational(expr);
		while (current.kind == Symbol.EQUALS
				|| current.kind == Symbol.NOT_EQUAL) {
			Symbol op = current;
			next_symbol();
			relational(expr);
			expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// AND

	private void and(Stack expr) { // &
		equality(expr);
		while (current.kind == Symbol.BITWISE_AND) {
			Symbol op = current;
			next_symbol();
			equality(expr);
			expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// EXCLUSIVE_OR

	private void exclusive_or(Stack expr) { // ^
		and(expr);
		while (current.kind == Symbol.CIRCUMFLEX) {
			Symbol op = current;
			next_symbol();
			and(expr);
			expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// INCLUSIVE_OR

	private void inclusive_or(Stack expr) { // |
		exclusive_or(expr);
		while (current.kind == Symbol.BITWISE_OR) {
			Symbol op = current;
			next_symbol();
			exclusive_or(expr);
			expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// LOGICAL_AND

	private void logical_and(Stack expr) { // &&
		inclusive_or(expr);
		while (current.kind == Symbol.AND) {
			Symbol op = current;
			next_symbol();
			inclusive_or(expr);
			expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// LOGICAL_OR

	private void logical_or(Stack expr) { // ||
		logical_and(expr);
		while (current.kind == Symbol.OR) {
			Symbol op = current;
			next_symbol();
			logical_and(expr);
			expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// EXPRESSION

	private void expression(Stack expr) {
		logical_or(expr);
	}

	// this is used to avoid a syntax problem
	// when a parallel composition
	// follows a range or constant definition e.g.
	// const N = 3
	// ||S = (P || Q)
	private void simpleExpression(Stack expr) {
		additive(expr);
	}

	// _______________________________________________________________________________________
	// LINEAR TEMPORAL LOGIC ASSERTIONS

	private void assertDefinition(boolean isConstraint) {
		current_is(Symbol.UPPERIDENT, "LTL property identifier expected");
		Symbol name = current;
		LabelSet ls = null;
		next_symbol();
		Hashtable initparams = new Hashtable();
		Vector params = new Vector();
		paramDefns(initparams, params);
		current_is(Symbol.BECOMES, "= expected");
		next_symbol_mod();
		IFormulaSyntax f = ltl_unary();
		if (current.kind == Symbol.PLUS) {
			next_symbol();
			ls = labelSet();
		}
		push_symbol();
		if (processes != null && processes.get(name.toString()) != null
				|| composites != null
				&& composites.get(name.toString()) != null) {
			Diagnostics.fatal("name already defined  " + name, name);
		}
		AssertDefinition.put(name, f, ls, initparams, params, isConstraint);
	}

	// do not want X and U to be keywords outside of LTL expressions
	private Symbol modify(Symbol s) {
		if (s.kind != Symbol.UPPERIDENT)
			return s;
		if (s.toString().equals("X")) {
			Symbol nx = new Symbol(s);
			nx.kind = Symbol.NEXTTIME;
			return nx;
		}
		if (s.toString().equals("U")) {
			Symbol ut = new Symbol(s);
			ut.kind = Symbol.UNTIL;
			return ut;
		}
		if (s.toString().equals("W")) {
			Symbol wut = new Symbol(s);
			wut.kind = Symbol.WEAKUNTIL;
			return wut;
		}

		return s;
	}

	private void next_symbol_mod() {
		next_symbol();
		current = modify(current);
	}

	// _______________________________________________________________________________________
	// LINEAR TEMPORAL LOGIC EXPRESSION

	private IFormulaSyntax ltl_unary() { // !,<>,[]
		ISymbol op = current;
		switch (current.kind) {
		case Symbol.PLING:
		case Symbol.NEXTTIME:
		case Symbol.EVENTUALLY:
		case Symbol.ALWAYS:
			next_symbol_mod();
			return FormulaSyntax.make(null, op, ltl_unary());
		case Symbol.UPPERIDENT:
			next_symbol_mod();
			if (current.kind == Symbol.LSQUARE) {
				IActionLabels range = forallRanges();
				current = modify(current);
				return FormulaSyntax.make(op, range);
			} else if (current.kind == Symbol.LROUND) {
				Vector actparams = actualParameters();
				return FormulaSyntax.make(op, actparams);
			} else {
				return FormulaSyntax.make(op);
			}
		case Symbol.LROUND:
			next_symbol_mod();
			IFormulaSyntax right = ltl_or();
			current_is(Symbol.RROUND, ") expected to end LTL expression");
			next_symbol_mod();
			return right;
		case Symbol.IDENTIFIER:
		case Symbol.LSQUARE:
		case Symbol.LCURLY:
			ActionLabels ts = labelElement();
			push_symbol();
			next_symbol_mod();
			return FormulaSyntax.make(ts);
		case Symbol.EXISTS:
			next_symbol_mod();
			ActionLabels ff = forallRanges();
			push_symbol();
			next_symbol_mod();
			return FormulaSyntax.make(new Symbol(Symbol.OR), ff, ltl_unary());
		case Symbol.FORALL:
			next_symbol_mod();
			ff = forallRanges();
			push_symbol();
			next_symbol_mod();
			return FormulaSyntax.make(new Symbol(Symbol.AND), ff, ltl_unary());
		case Symbol.RIGID:
			next_symbol_mod();
			Stack tmp = new Stack();
			simpleExpression(tmp);
			push_symbol();
			next_symbol_mod();
			return FormulaSyntax.makeE(op, tmp);
		default:
			Diagnostics.fatal("syntax error in LTL expression", current);
		}
		return null;
	}

	// _______________________________________________________________________________________
	// LTL_AND

	private IFormulaSyntax ltl_and() { // &
		IFormulaSyntax left = ltl_unary();
		while (current.kind == Symbol.AND) {
			ISymbol op = current;
			next_symbol_mod();
			IFormulaSyntax right = ltl_unary();
			left = FormulaSyntax.make(left, op, right);
		}
		return left;
	}

	// _______________________________________________________________________________________
	// LTL_OR

	private IFormulaSyntax ltl_or() { // |
		IFormulaSyntax left = ltl_binary();
		while (current.kind == Symbol.OR) {
			ISymbol op = current;
			next_symbol_mod();
			IFormulaSyntax right = ltl_binary();
			left = FormulaSyntax.make(left, op, right);
		}
		return left;
	}

	// _______________________________________________________________________________________
	// LTS_BINARY

	private IFormulaSyntax ltl_binary() { // until, ->
		IFormulaSyntax left = ltl_and();
		if (current.kind == Symbol.UNTIL || current.kind == Symbol.WEAKUNTIL
				|| current.kind == Symbol.ARROW
				|| current.kind == Symbol.EQUIVALENT) {
			ISymbol op = current;
			next_symbol_mod();
			IFormulaSyntax right = ltl_and();
			left = FormulaSyntax.make(left, op, right);
		}
		return left;
	}

	//
	// ___________________________________________________________________________________
	// STATE PREDICATE DEFINITIONS

	private void predicateDefinition() {
		current_is(Symbol.UPPERIDENT, "predicate identifier expected");
		Symbol name = current;
		ActionLabels range = null;
		next_symbol();
		if (current.kind == Symbol.LSQUARE)
			range = forallRanges();
		current_is(Symbol.BECOMES, "= expected");
		next_symbol();
		current_is(Symbol.LESS_THAN, "< expected");
		next_symbol();
		ActionLabels ts = labelElement();
		current_is(Symbol.COMMA, ", expected");
		next_symbol();
		ActionLabels fs = labelElement();
		current_is(Symbol.GREATER_THAN, "> expected");
		next_symbol();
		if (current.kind == Symbol.INIT) {
			next_symbol();
			Stack tmp = new Stack();
			simpleExpression(tmp);
			push_symbol();
			PredicateDefinition.put(name, range, ts, fs, tmp);
		} else {
			push_symbol();
			PredicateDefinition.put(name, range, ts, fs, null);
		}
	}

}
