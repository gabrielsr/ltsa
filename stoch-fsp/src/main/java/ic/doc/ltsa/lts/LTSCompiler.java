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
import java.io.File;

import ic.doc.ltsa.common.iface.IActionLabels;
import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.IFormulaSyntax;
import ic.doc.ltsa.common.iface.ILTSCompiler;
import ic.doc.ltsa.common.iface.ISymbol;
import ic.doc.ltsa.common.iface.LTSInput;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.lts.ltl.*;

/** Using Lex as the lexical analyser, this compiler generates
 *  abstract representations of FSP processes (CompactState), and
 *  their compositions (CompositeState).
 * */
public class LTSCompiler implements ILTSCompiler {

    private Lex lex;
    private LTSOutput output;
    private String currentDirectory;
    private Symbol current;
    private boolean probabilisticSystem;
    
    static Hashtable processes;
    static Hashtable compiled;
    static Hashtable composites;
    
	private ExpressionParser exprParser = new ExpressionParser();
	private ExpressionParser flExprParser = new FloatExpressionParser();


    public LTSCompiler (LTSInput input, LTSOutput output, String currentDirectory) {
		probabilisticSystem = true;
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

    private Symbol next_symbol () {
  	    current = lex.next_symbol();
		return current;
    }

    private void push_symbol () {
        lex.push_symbol();
    }

    private void error (String errorMsg) {
        Diagnostics.fatal (errorMsg, current);
    }

    private void current_is (int kind, String errorMsg) {
        if (current.kind != kind)
            error (errorMsg);
    }

    public ICompositeState compile(String name) {
        processes = new Hashtable(); // processes
        composites = new Hashtable(); // composites
        compiled = new Hashtable(); //compiled
        doparse(composites,processes,compiled);
        ProgressDefinition.compile();
        MenuDefinition.compile();
	    PredicateDefinition.compileAll();
		AssertDefinition.compileAll(output);
        CompositionExpression ce = (CompositionExpression)composites.get(name);
        if (ce==null && composites.size()>0) {
            Enumeration e = composites.elements();
            ce = (CompositionExpression)e.nextElement();
        }
        if(ce!=null){
        		return ce.compose(null,probabilisticSystem);
        }
        else {
        		compileProcesses(processes,compiled);
            return noCompositionExpression(compiled);
        }
    }

    //put compiled definitions  in Hashtable compiled
    private void compileProcesses(Hashtable h, Hashtable compiled) {
/* moved implementation to ProcessSpec.java
        Enumeration e = h.elements();
        while(e.hasMoreElements()) {
            ProcessSpec p = (ProcessSpec)e.nextElement();
            if (!p.imported()) {
	            StateMachine  one = new StateMachine(p);
	            CompactState c = one.makeCompactState();
	            output.outln("Compiled: "+c.name);
	            compiled.put(c.name,c);
            } else {
            	CompactState c = new AutCompactState(p.name, p.importFile);
            	output.outln("Imported: "+c.name);
            	compiled.put(c.name,c);
            }
        }
*/        
		Enumeration e = h.elements();
		while(e.hasMoreElements()) {
			CompactState c = ((Compilable) e.nextElement()).makeCompactState( output, probabilisticSystem );
			compiled.put( c.getName(), c );
		}
    }

    public void parse(Map composites, Map processes) {
        doparse(composites,processes,null);
    }

    private void doparse(Map composites, Map processes, Map compiled) {
       next_symbol ();
       while (current.kind != Symbol.EOFSYM) {
          if (current.kind ==Symbol.CONSTANT) {
              next_symbol();
              constantDefinition(Expression.constants,false);
          } else if( current.kind == Symbol.FLOAT ) {
			  next_symbol();
			  constantDefinition( Expression.constants,true );
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
          	   assertDefinition();
          } else if (current.kind == Symbol.PREDICATE) {
          	   next_symbol();
          	   predicateDefinition();
          } else if (current.kind == Symbol.IMPORT) {
          	   next_symbol();
          	   ProcessSpec p = importDefinition();
          	   if (processes.put(p.name.toString(), p)!=null){
                  Diagnostics.fatal ("duplicate process definition: "+p.name, p.name);
               }
		  } else if( current.kind == Symbol.TIMER ) {
			  MeasureSpec t = timerDef();
			  if( processes.put( t.getName(), t ) != null ) {
			  Diagnostics.fatal( "Duplicate process definition: " + t.getName() );
			  }
		  } else if( current.kind == Symbol.MEASURE ) {
			  MeasureSpec m = measureDef();
			  if( processes.put( m.getName(), m ) != null ) {
			  Diagnostics.fatal( "Duplicate process definition: " + m.getName() );
			  }
		  } else if( current.kind == Symbol.COUNTER ) {
			  MeasureSpec c = counterDef();
			  if( processes.put( c.getName(), c ) != null ) {
			  Diagnostics.fatal( "Duplicate process definition: " + c.getName() );
			  }
          } else if (current.kind==Symbol.OR
                    || current.kind == Symbol.DETERMINISTIC
                    || current.kind == Symbol.MINIMAL
                    || current.kind == Symbol.PROPERTY
                    || current.kind == Symbol.COMPOSE) {
              boolean makeDet = false;
              boolean makeMin = false;
              boolean makeProp = false;
              boolean makeComp = false;
              if (current.kind == Symbol.DETERMINISTIC) { makeDet = true; next_symbol();}
              if (current.kind == Symbol.MINIMAL)       { makeMin = true; next_symbol();}
              if (current.kind == Symbol.COMPOSE)       { makeComp = true; next_symbol();}
              if (current.kind == Symbol.PROPERTY)      { makeProp = true; next_symbol();}
              if (current.kind != Symbol.OR) {
                  ProcessSpec p = stateDefns();
                  if (processes.put(p.name.toString(), p)!=null){
                      Diagnostics.fatal ("duplicate process definition: "+p.name, p.name);
                  }
                  p.isProperty = makeProp;
                  p.isMinimal  = makeMin;
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
                  if(composites.put(c.name.toString(),c)!=null){
                    Diagnostics.fatal ("duplicate composite definition: "+c.name, c.name);
                  }
              }
           } else {
              ProcessSpec p = stateDefns();
              if(processes.put(p.name.toString(), p)!=null){
                Diagnostics.fatal ("duplicate process definition: "+p.name, p.name);
              }
           }

           next_symbol();
       }
    }

    private CompositeState noCompositionExpression(Hashtable h) {
        Vector v = new Vector(16);
        Enumeration e = h.elements();
        while(e.hasMoreElements()) {
            v.addElement(e.nextElement());
        }
        return new CompositeState(v);
    }

    private CompositionExpression composition() {
        current_is(Symbol.OR, "|| expected");
        next_symbol();
        CompositionExpression c = new CompositionExpression();
        current_is(Symbol.UPPERIDENT,"process identifier expected");
        c.name = current;
        next_symbol();
        paramDefns(c.init_constants,c.parameters);
        current_is(Symbol.BECOMES,"= expected");
        next_symbol();
        c.body = compositebody();
        c.priorityActions = priorityDefn(c);
        if (current.kind==Symbol.BACKSLASH || current.kind==Symbol.AT) {
            c.exposeNotHide = (current.kind==Symbol.AT);
            next_symbol();
            c.alphaHidden = labelSet();
        }
        current_is(Symbol.DOT,"dot expected");
        return c;
    }

    private CompositeBody compositebody() {
        CompositeBody b = new CompositeBody();
        if (current.kind == Symbol.IF) {
            next_symbol();
            b.boolexpr = new Stack();
            exprParser.expression(b.boolexpr);
            current_is(Symbol.THEN,"keyword then expected");
            next_symbol();
            b.thenpart = compositebody();
            if(current.kind==Symbol.ELSE) {
                next_symbol();
                b.elsepart = compositebody();
            }
        } else if (current.kind == Symbol.FORALL) {
            next_symbol();
            b.range = forallRanges();
            b.thenpart = compositebody();
        } else {
            //get accessors if any
            if (isLabel()) {
                IActionLabels el = labelElement();
                if (current.kind==Symbol.COLON_COLON){
                    b.accessSet=el;
                    next_symbol();
                    if (isLabel()) {
                        b.prefix=labelElement();
                        current_is(Symbol.COLON," : expected");
                        next_symbol();
                    }
                } else if (current.kind==Symbol.COLON){
                    b.prefix=el;
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

    private IActionLabels forallRanges(){
        current_is(Symbol.LSQUARE,"range expected");
        ActionLabels head = range();
        ActionLabels next = head;
        while (current.kind==Symbol.LSQUARE) {
            ActionLabels t = range();
            next.addFollower(t);
            next = t;
        }
        return head;
    }


    private Vector processRefs() {
        Vector procRefs =  new Vector();
        current_is(Symbol.LROUND,"( expected");
        next_symbol();
        if (current.kind!=Symbol.RROUND) {
            procRefs.addElement(compositebody());
            while (current.kind == Symbol.OR) {
                next_symbol();
                procRefs.addElement(compositebody());
            }
        current_is(Symbol.RROUND,") expected");
        }
        next_symbol();
        return procRefs;
    }

    private Vector relabelDefns() {
        if (current.kind != Symbol.DIVIDE) return null;
        next_symbol();
        return relabelSet();
    }

    private LabelSet priorityDefn(CompositionExpression c) {
        if (current.kind != Symbol.SHIFT_RIGHT && current.kind!=Symbol.SHIFT_LEFT) return null;
        if (current.kind == Symbol.SHIFT_LEFT) c.priorityIsLow=false;
        next_symbol();
        return labelSet();
    }

    private Vector relabelSet() {
        current_is(Symbol.LCURLY,"{ expected");
        next_symbol();
        Vector v = new Vector();
        v.addElement(relabelDefn());
        while(current.kind == Symbol.COMMA) {
            next_symbol();
            v.addElement(relabelDefn());
        }
        current_is(Symbol.RCURLY,"} expected");
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
            current_is(Symbol.DIVIDE,"/ expected");
            next_symbol();
            r.oldlabel = labelElement();
        }
        return r;
    }

    private ProcessRef processRef() {
        ProcessRef p = new ProcessRef();
        current_is(Symbol.UPPERIDENT, "process identifier expected");
        p.name=current;
        next_symbol();
        p.actualParams = actualParameters();
        return p;
    }

    private Vector actualParameters() {
        if (current.kind!=Symbol.LROUND) return null;
        Vector v = new Vector();
        next_symbol();
        Stack stk = new Stack();
		exprParser.expression(stk);
        v.addElement(stk);
        while(current.kind==Symbol.COMMA) {
            next_symbol();
            stk = new Stack();
			exprParser.expression(stk);
            v.addElement(stk);
        }
        current_is(Symbol.RROUND,") - expected");
        next_symbol();
        return v;
    }


    private ProcessSpec stateDefns() {
        ProcessSpec p = new ProcessSpec();
        current_is(Symbol.UPPERIDENT,"process identifier expected");
        Symbol temp = current;
        next_symbol();
        paramDefns(p.init_constants,p.parameters);
        push_symbol();
        current = temp;
        p.stateDefns.addElement(stateDefn());
        while(current.kind==Symbol.COMMA) {
            next_symbol();
            p.stateDefns.addElement(stateDefn());
        }
        if (current.kind==Symbol.PLUS) {
            next_symbol();
            p.alphaAdditions = labelSet();
        }
        p.alphaRelabel = relabelDefns();
        if (current.kind==Symbol.BACKSLASH || current.kind==Symbol.AT) {
            p.exposeNotHide = (current.kind==Symbol.AT);
            next_symbol();
            p.alphaHidden = labelSet();
        }
        p.getname();
        current_is(Symbol.DOT, "dot expected");
        return p;
    }

    private boolean isLabelSet(){
        if (current.kind==Symbol.LCURLY)return true;
        if (current.kind!=Symbol.UPPERIDENT) return false;
        return LabelSet.constants.containsKey(current.toString());
    }

    private boolean isLabel(){
        return (isLabelSet()||current.kind ==Symbol.IDENTIFIER ||current.kind ==Symbol.LSQUARE);
    }
    
    private ProcessSpec importDefinition() {
    	   current_is(Symbol.UPPERIDENT,"imported process identifier expected");
			 ProcessSpec p = new ProcessSpec();
			 p.name = current;
			 next_symbol();
        current_is(Symbol.BECOMES,"= expected");
        next_symbol();
        current_is(Symbol.STRING_VALUE," - imported file name expected");
        p.importFile = new File(currentDirectory,current.toString());
        return p;
    }


    private void animationDefinition() {
        current_is(Symbol.UPPERIDENT,"animation identifier expected");
        MenuDefinition m = new MenuDefinition();
        m.name = current;
        next_symbol();
        current_is(Symbol.BECOMES,"= expected");
        next_symbol();
        current_is(Symbol.STRING_VALUE," - XML file name expected");
        m.params = current;
        next_symbol();
        if (current.kind ==Symbol.TARGET) {
            next_symbol();
            current_is(Symbol.UPPERIDENT," - target composition name expected");
            m.target = current;
            next_symbol();
        }
        if (current.kind == Symbol.COMPOSE) {
            next_symbol();
            current_is(Symbol.LCURLY,"{ expected");
            next_symbol();
            current_is(Symbol.UPPERIDENT,"animation name expected");
            Symbol name = current;
            next_symbol();
            m.addAnimationPart(name,relabelDefns());
            while (current.kind == Symbol.OR) {
               next_symbol();
               current_is(Symbol.UPPERIDENT,"animation name expected");
               name = current;
               next_symbol();
               m.addAnimationPart(name,relabelDefns());
            }
            current_is(Symbol.RCURLY,"} expected");
            next_symbol();
        }
        if (current.kind == Symbol.ACTIONS) {
            next_symbol();
            m.actionMapDefn = relabelSet();
        }
        if (current.kind == Symbol.CONTROLS) {
            next_symbol();
            m.controlMapDefn = relabelSet();
        }
        push_symbol();
        if(MenuDefinition.definitions.put(m.name.toString(),m)!=null) {
            Diagnostics.fatal ("duplicate menu/animation definition: "+m.name, m.name);
        }
    }

    private void menuDefinition() {
        current_is(Symbol.UPPERIDENT,"menu identifier expected");
        MenuDefinition m = new MenuDefinition();
        m.name = current;
        next_symbol();
        current_is(Symbol.BECOMES,"= expected");
        next_symbol();
        m.actions = labelElement();
        push_symbol();
        if(MenuDefinition.definitions.put(m.name.toString(),m)!=null) {
            Diagnostics.fatal ("duplicate menu/animation definition: "+m.name, m.name);
        }
    }


    private void progressDefinition() {
        current_is(Symbol.UPPERIDENT,"progress test identifier expected");
        ProgressDefinition p = new ProgressDefinition();
        p.name = current;
        next_symbol();
        if (current.kind==Symbol.LSQUARE)
            p.range = forallRanges();
        current_is(Symbol.BECOMES,"= expected");
        next_symbol();
        if (current.kind==Symbol.IF) {
            next_symbol();
            p.pactions=labelElement();
            current_is(Symbol.THEN,"then expected");
            next_symbol();
            p.cactions = labelElement();
        } else {
            p.pactions = labelElement();
        }
        if(ProgressDefinition.definitions.put(p.name.toString(),p)!=null) {
            Diagnostics.fatal ("duplicate progress test: "+p.name, p.name);
        }
        push_symbol();
    }

    private void setDefinition() {
        current_is(Symbol.UPPERIDENT,"set identifier expected");
        Symbol temp = current;
        next_symbol();
        current_is(Symbol.BECOMES,"= expected");
        next_symbol();
        LabelSet ls = new LabelSet(temp,setValue());
        push_symbol();
    }

    private LabelSet labelSet() {
       if (current.kind==Symbol.LCURLY)
        return new LabelSet(setValue());
       else if (current.kind==Symbol.UPPERIDENT) {
        LabelSet ls = (LabelSet)LabelSet.constants.get(current.toString());
        if (ls==null) error("set definition not found for: "+current);
        next_symbol();
        return ls;
       } else {
        error("{ or set identifier expected");
        return null;
       }
    }

    private Vector setValue(){
        current_is(Symbol.LCURLY,"{ expected");
        next_symbol();
        Vector v = new Vector();
        v.addElement(labelElement());
        while(current.kind==Symbol.COMMA) {
            next_symbol();
            v.addElement(labelElement());
        }
        current_is(Symbol.RCURLY,"} expected");
        next_symbol();
        return v;
    }

    private  IActionLabels labelElement() {
        if (current.kind !=Symbol.IDENTIFIER
            && !isLabelSet()
            && current.kind != Symbol.LSQUARE)
                error("identifier, label set or range expected");
        IActionLabels e = null;
        if(current.kind==Symbol.IDENTIFIER) {
            if ("tau".equals(current.toString()))
                error("'tau' cannot be used as an action label");
            e = new ActionName(current);
            next_symbol();
        } else if (isLabelSet()) {
        	   LabelSet left = labelSet();
        	   if (current.kind==Symbol.BACKSLASH) {
        	   	  next_symbol();
        	   	  LabelSet right = labelSet();
        	   	  e = new ActionSetExpr(left,right);
        	   } else {
                e = new ActionSet(left);
        	   }
        }
        else if (current.kind==Symbol.LSQUARE)
            e = range();
        if (current.kind==Symbol.DOT || current.kind==Symbol.LSQUARE) {
            if (current.kind==Symbol.DOT) next_symbol();
            if (e!=null) ((ActionLabels)e).addFollower(labelElement());
        }
        return e;
     }

   private void constantDefinition(Hashtable p, boolean isFloat) {
		ExpressionParser ep;
		if( isFloat ) ep = flExprParser;
		   else ep = exprParser;
        current_is(Symbol.UPPERIDENT,"constant, upper case identifier expected");
        Symbol name = current;
        next_symbol();
        current_is(Symbol.BECOMES,"= expected");
        next_symbol();
        Stack tmp = new Stack();
		ep.simpleExpression(tmp);
		push_symbol();
		if( isFloat ) {
			Value v = new Value( Expression.getDoubleValue( tmp, null, null ) );
			if( p.put( name.toString(), v ) != null ) {
				Diagnostics.fatal( "duplicate constant definition: "+name, name);
			}
		} else {
			Value v = Expression.getValue( tmp, null, null);
			if( p.put( name.toString(), v ) != null ) {
				Diagnostics.fatal ("duplicate constant definition: "+name, name);
			}
		}
/*
	    if (p.put(name.toString(),Expression.getValue(tmp,null,null))!=null){
            Diagnostics.fatal ("duplicate constant definition: "+name, name);
        }
*/
    }

   private void paramDefns(Hashtable p, Vector parameters) {
        if (current.kind==Symbol.LROUND) {
            next_symbol();
            parameterDefinition(p,parameters);
            while(current.kind==Symbol.COMMA) {
                next_symbol();
                parameterDefinition(p,parameters);
            }
            current_is(Symbol.RROUND,") expected");
            next_symbol();
        }
   }


    private void parameterDefinition(Hashtable p, Vector parameters) {
        current_is(Symbol.UPPERIDENT,"parameter, upper case identifier expected");
        Symbol name = current;
        next_symbol();
        current_is(Symbol.BECOMES,"= expected");
        next_symbol();
        Stack tmp = new Stack();
		exprParser.expression(tmp);
        push_symbol();
		if( p.get( name.toString() ) != null ){
			Diagnostics.fatal ("duplicate parameter definition: "+name, name);
		} else {
			Value v;
			if( Expression.isDoubleExpr( tmp, null, null ) )
				v = new Value( Expression.getDoubleValue( tmp, null, null ) );
			else
				v = Expression.getValue( tmp, null, null );
			p.put( name.toString(), v );
		}
/*
        if(p.put(name.toString(),Expression.getValue(tmp,null,null))!=null){
            Diagnostics.fatal ("duplicate parameter definition: "+name, name);
        }
*/
        if (parameters!=null){
            parameters.addElement(name.toString());
            next_symbol();
        }
    }

    private StateDefn stateDefn() {
        StateDefn s = new StateDefn();
        current_is(Symbol.UPPERIDENT,"process identifier expected");
        s.name = current;
        next_symbol();
        if (current.kind==Symbol.AT) {
            s.accept = true;
            next_symbol();
        }
        if (current.kind==Symbol.DOT || current.kind==Symbol.LSQUARE) {
            if (current.kind==Symbol.DOT) next_symbol();
            s.range = labelElement();
        }
        current_is(Symbol.BECOMES,"= expected");
        next_symbol();
        s.stateExpr = stateExpr();
        return s;
    }

    private Stack getEvaluatedExpression() {
        Stack tmp = new Stack();
		exprParser.simpleExpression(tmp);
        int v = Expression.evaluate(tmp,null,null);
        tmp = new Stack();
        tmp.push(new Symbol(Symbol.INT_VALUE,v));
        return tmp;
    }

    private void rangeDefinition() {
        current_is(Symbol.UPPERIDENT,"range name, upper case identifier expected");
        Symbol name = current;
        next_symbol();
        current_is(Symbol.BECOMES,"= expected");
        next_symbol();
        Range r = new Range();
        r.low = getEvaluatedExpression();
        current_is(Symbol.DOT_DOT,"..  expected");
        next_symbol();
        r.high = getEvaluatedExpression();
        if(Range.ranges.put(name.toString(), r)!=null){
            Diagnostics.fatal ("duplicate range definition: "+name, name);;
        }
        push_symbol();
    }

    private ActionLabels range() {  //this is a mess.. needs to be rewritten
        if (current.kind == Symbol.LSQUARE) {
            next_symbol();
            ActionLabels r;
            Stack low = null;
            Stack high = null;
            if (current.kind != Symbol.IDENTIFIER) {
                if (isLabelSet()) {
                      r = new ActionSet(labelSet());
                } else if(current.kind==Symbol.UPPERIDENT
                          && Range.ranges.containsKey(current.toString())){
                      r = new ActionRange((Range)Range.ranges.get(current.toString()));
                      next_symbol();
                 } else {
                    low = new Stack();
					exprParser.expression(low);
                    r = new ActionExpr(low);
                 }
                 if (current.kind == Symbol.DOT_DOT) {
                    next_symbol();
                    high = new Stack();
					exprParser.expression(high);
                    r = new ActionRange(low,high);
                }              
            } else {
               Symbol varname =current;
               next_symbol();
               if (current.kind == Symbol.COLON) {
                   next_symbol();
                   if (isLabelSet()) {
                      r  = new ActionVarSet(varname,labelSet());
                   } else if(current.kind==Symbol.UPPERIDENT
                             && Range.ranges.containsKey(current.toString())){
                      r = new ActionVarRange(varname,(Range)Range.ranges.get(current.toString()));
                      next_symbol();
                   } else {
                       low = new Stack();
					   exprParser.expression(low);
                       current_is(Symbol.DOT_DOT,"..  expected");
                       next_symbol();
                       high = new Stack();
					   exprParser.expression(high);
                       r = new ActionVarRange(varname,low,high);
                   }
               } else {
                   push_symbol();
                   current=varname;
                   low = new Stack();
				   exprParser.expression(low);
                   if (current.kind == Symbol.DOT_DOT) {
                        next_symbol();
                        high = new Stack();
					    exprParser.expression(high);
                        r = new ActionRange(low,high);
                    } else
                        r = new ActionExpr(low);
               }
            }
            current_is(Symbol.RSQUARE,"] expected");
            next_symbol();
            return r;
        } else
            return null;
    }


    private StateExpr  stateExpr(){
        StateExpr s = new StateExpr();
        if(current.kind==Symbol.UPPERIDENT)
            stateRef(s);
        else if(current.kind==Symbol.IF) {
            next_symbol();
            s.boolexpr = new Stack();
			exprParser.expression(s.boolexpr);
            current_is(Symbol.THEN,"keyword then expected");
            next_symbol();
            s.thenpart = stateExpr();
            if(current.kind==Symbol.ELSE) {
                next_symbol();
                s.elsepart = stateExpr();
            } else {
                Symbol stop = new Symbol(Symbol.UPPERIDENT,"STOP");
                StateExpr se = new StateExpr();
                se.name=stop;
                s.elsepart = se;
            }
        } else if(current.kind==Symbol.LROUND){
          next_symbol();
		  // choiceExpr(s);
		  if( current.kind == PROB_OPEN )
			  probChoiceExpr( s );
		  else
			  nonDetChoiceExpr( s );
		  current_is(Symbol.RROUND,") expected");
          next_symbol();

        } else
          error(" (, if or process identifier expected");
        return s;
    }

    private void stateRef(StateExpr s) {
        current_is(Symbol.UPPERIDENT,"process identifier expected");
        s.name = current;
        next_symbol();
        while (current.kind == Symbol.SEMICOLON || current.kind == Symbol.LROUND) {
             s.addSeqProcessRef(new SeqProcessRef(s.name,actualParameters()));
             next_symbol();
             current_is(Symbol.UPPERIDENT,"process identifier expected");
             s.name = current;
             next_symbol();
        }    
        if (current.kind == Symbol.LSQUARE) {
            s.expr = new Vector();
            while(current.kind == Symbol.LSQUARE) {
                next_symbol();
                Stack x = new Stack();
				exprParser.expression(x);
                s.expr.addElement(x);
                current_is(Symbol.RSQUARE,"] expected");
                next_symbol();
            }
        }
    }

    private void nonDetChoiceExpr(StateExpr s) {
        s.choices = new Vector();
        ChoiceElement c = choiceElement();
		Stack st = new Stack();
		st.push(new Symbol(Symbol.INT_VALUE,1));
        //if (probabilisticSystem) c.setProbExpr(st);
		//System.out.println("LTScompiler.nonDetChoiceExpr is going to check if isProbabilisticsystem");
		if (probabilisticSystem){
			//System.out.println("LTScompiler.nonDetChoiceExpr -> isProbabilisticsystem");
			c.setProbExpr(st);
		}
		s.choices.addElement(c);
        while (current.kind == Symbol.BITWISE_OR) {
            next_symbol();
			c = choiceElement();
			if (probabilisticSystem){
				//System.out.println("LTScompiler.nonDetChoiceExpr -> isProbabilisticsystem");
				c.setProbExpr(st);
			}
			s.choices.addElement(c);
        }
		// normalise probabilities
		if (probabilisticSystem) {
		  double amount = s.choices.size();
		  st = new Stack();
		  st.push(new Symbol(Symbol.DOUBLE_VALUE));
		  ((Symbol)st.peek()).doubleValue = 1/amount;
		  Iterator i = s.choices.iterator();
		  while (i.hasNext()) {
			  ((ChoiceElement)i.next()).prob = st;
		  }
		}
    }

   private ChoiceElement choiceElement(){
        ChoiceElement first = new ChoiceElement();
        if (current.kind == Symbol.WHEN) {
            next_symbol();
            first.guard = new Stack();
			exprParser.expression(first.guard);
        }
		ChoiceElement next;
		ChoiceElement last;
	
		next = doClockShorthand( first );
		if( next == first ) next.clockConditions = doClockGuard();
			next.action = labelElement();
		if( next == first ) next.clockActions = doClockSetting();
		
		last = next;
	
		current_is(Symbol.ARROW,"-> expected");
		next_symbol();
		while(current.kind == Symbol.IDENTIFIER
			  || current.kind == Symbol.LSQUARE
			  || isLabelSet()
			  || current.kind == CL_C_OPEN
			  || current.kind == CL_S_OPEN ) {
			StateExpr ex = new StateExpr();
			next = new ChoiceElement();
	
			// use this to see if clock shorthand is parsed
			ChoiceElement tmp = next;
			next = doClockShorthand( next );
			if( tmp == next ) next.clockConditions = doClockGuard();
				next.action = labelElement();
			if( tmp == next ) next.clockActions = doClockSetting();
		
			ex.choices = new Vector();
			Stack s = new Stack();
			// if extended, add probability decoration
			s.push(new Symbol(Symbol.INT_VALUE,1));
			if (probabilisticSystem) tmp.setProbExpr(s);
			ex.choices.addElement(tmp);
			last.stateExpr = ex;
			last = next;
			current_is(Symbol.ARROW,"-> expected");
			next_symbol();
		}
		next.stateExpr = stateExpr();
		
		return first;
/*        
        first.action = labelElement();
        current_is(Symbol.ARROW,"-> expected");
        ChoiceElement next = first;
        ChoiceElement last = first;
        next_symbol();
        while(current.kind == Symbol.IDENTIFIER
              || current.kind == Symbol.LSQUARE
              || isLabelSet()) {
            StateExpr ex = new StateExpr();
            next = new ChoiceElement();
            next.action = labelElement();
            ex.choices = new Vector();
            ex.choices.addElement(next);
            last.stateExpr = ex;
            last = next;
            current_is(Symbol.ARROW,"-> expected");
            next_symbol();
        }
        next.stateExpr = stateExpr();
        return first;
*/
    }

    private Symbol event() {
        current_is(Symbol.IDENTIFIER,"event identifier expected");
        Symbol e = current;
        next_symbol();
          return e;
    }


// LABELCONSTANT -------------------------------

    private IActionLabels labelConstant(){
        next_symbol();
        IActionLabels el = labelElement();
        if (el!=null) {
            return el;
        } else
            error("label definition expected");
        return null;
    }
 
// set selection @(set , expr)
private void set_select(Stack expr) {
     Symbol op = current;
     next_symbol();
     current_is (Symbol.LROUND, "( expected to start set index selection");
     Symbol temp = current; //preserve marker
     temp.setAny(labelConstant());
     temp.kind = Symbol.LABELCONST;
     expr.push(temp);
     current_is (Symbol.COMMA, ", expected before set index expression");
     next_symbol();
	 exprParser.expression (expr);
     current_is (Symbol.RROUND, ") expected to end set index selection");
     next_symbol();
     expr.push(op);
}

// UNARY ---------------------------------
    private void unary (Stack expr) {  // +, -, identifier,
    	Symbol unary_operator;
    	switch (current.kind) {
    	case Symbol.PLUS:
    		unary_operator = current;
    		unary_operator.kind = Symbol.UNARY_PLUS;
    		next_symbol ();
    		break;
    	case Symbol.MINUS:
    		unary_operator = current;
    		unary_operator.kind = Symbol.UNARY_MINUS;
    		next_symbol ();
    		break;
	    case Symbol.PLING:
		    unary_operator = current;
		    next_symbol ();
		    break;

    	default:
    		unary_operator = null;
    	}
    	switch (current.kind) {
    	case Symbol.IDENTIFIER:
    	case Symbol.UPPERIDENT:
    	case Symbol.INT_VALUE:
     		expr.push (current);
    		next_symbol ();
    		break;
    	case Symbol.LROUND:
    	    next_symbol ();
			exprParser.expression (expr);
    		current_is (Symbol.RROUND, ") expected to end expression");
    		next_symbol();
    		break;
      case Symbol.HASH:
      	  unary_operator=new Symbol(current);
      case Symbol.QUOTE:         // this is a labelConstant
            Symbol temp = current; //preserve marker
            temp.setAny(labelConstant());
            temp.kind = Symbol.LABELCONST;
            expr.push(temp);
            break;
      case Symbol.AT:
           set_select(expr);
           break;
    	default:
    		error ("syntax error in expression");
    	}

    	if (unary_operator != null)
    		expr.push(unary_operator);
    }

// MULTIPLICATIVE

    private void multiplicative (Stack expr) {	// *, /, %
    	unary (expr);
    	while (
    		current.kind == Symbol.STAR    ||
    		current.kind == Symbol.DIVIDE  ||
    		current.kind == Symbol.MODULUS
    	) {
    		Symbol op = current;
    		next_symbol ();
    		unary (expr);
    		expr.push (op);
    	}
    }

// _______________________________________________________________________________________
// ADDITIVE

    private void additive (Stack expr) {	// +, -
    	multiplicative (expr);
    	while (current.kind == Symbol.PLUS || current.kind == Symbol.MINUS) {
    		Symbol op = current;
    		next_symbol();
    		multiplicative (expr);
    		expr.push(op);
    	}
    }


    // _______________________________________________________________________________________
// SHIFT

private void shift (Stack expr) {	// <<, >>
	additive (expr);
	while (current.kind==Symbol.SHIFT_LEFT || current.kind==Symbol.SHIFT_RIGHT) {
		Symbol op = current;
		next_symbol ();
		additive (expr);
		expr.push(op);
	}
}

// _______________________________________________________________________________________
// RELATIONAL

private void relational (Stack expr) {	// <, <=, >, >=
	shift (expr);
	while (
		current.kind == Symbol.LESS_THAN ||
		current.kind == Symbol.LESS_THAN_EQUAL ||
		current.kind == Symbol.GREATER_THAN ||
		current.kind == Symbol.GREATER_THAN_EQUAL
	) {
		Symbol op = current;
		next_symbol ();
		shift (expr);
		expr.push (op);
	}
}

// _______________________________________________________________________________________
// EQUALITY

private void equality (Stack expr) {	// ==, !=
	relational (expr);
	while (current.kind == Symbol.EQUALS || current.kind == Symbol.NOT_EQUAL) {
		Symbol op = current;
		next_symbol ();
		relational (expr);
		expr.push (op);
	}
}

// _______________________________________________________________________________________
// AND

private void and (Stack expr) {	// &
	equality (expr);
	while (current.kind == Symbol.BITWISE_AND) {
		Symbol op = current;
		next_symbol ();
		equality (expr);
		expr.push (op);
	}
}

// _______________________________________________________________________________________
// EXCLUSIVE_OR

private void exclusive_or (Stack expr) {	// ^
	and (expr);
	while (current.kind == Symbol.CIRCUMFLEX) {
		Symbol op = current;
		next_symbol ();
		and (expr);
		expr.push (op);
	}
}

// _______________________________________________________________________________________
// INCLUSIVE_OR

private void inclusive_or (Stack expr) {	// |
	exclusive_or (expr);
	while (current.kind == Symbol.BITWISE_OR) {
		Symbol op = current;
		next_symbol ();
		exclusive_or (expr);
		expr.push (op);
	}
}

// _______________________________________________________________________________________
// LOGICAL_AND

private void logical_and (Stack expr) {	// &&
	inclusive_or (expr);
	while (current.kind == Symbol.AND) {
		Symbol op = current;
		next_symbol ();
		inclusive_or (expr);
		expr.push(op);
	}
}

// _______________________________________________________________________________________
// LOGICAL_OR

private void logical_or (Stack expr) {	// ||
	logical_and (expr);
	while (current.kind == Symbol.OR) {
		Symbol op = current;
		next_symbol ();
		logical_and (expr);
		expr.push (op);
	}
}

/*
This is now in a separate class at the bottom
 
// _______________________________________________________________________________________
// EXPRESSION

private void expression (Stack expr) {
	logical_or (expr);
}

// this is used to avoid a syntax problem
// when a parallel composition
// follows a range or constant definition e.g.
// const N = 3
// ||S = (P || Q)
private void simpleExpression (Stack expr) {
    additive (expr);
}
*/

// _______________________________________________________________________________________
// LINEAR TEMPORAL LOGIC ASSERTIONS



private void assertDefinition() {
	current_is(Symbol.UPPERIDENT,"LTL property identifier expected");
	ISymbol name = current;
	LabelSet ls = null;
    next_symbol();
    current_is(Symbol.BECOMES,"= expected");
    next_symbol_mod();
	IFormulaSyntax f = ltl_unary();
	if (current.kind==Symbol.PLUS) {
            next_symbol();
            ls = labelSet();
    }
	push_symbol();
	if(processes!=null && processes.get(name.toString())!=null ||composites!=null && composites.get(name.toString())!=null) {
     Diagnostics.fatal ("name already defined  "+name, name);
   }
	
	//rbc - added last three arguments as defaults
	AssertDefinition.put(name,f,ls,null,null,false);
}

 // do not want X and U to be keywords outside of LTL expressions
  private Symbol modify(Symbol s) {
  	  if (s.kind!=Symbol.UPPERIDENT) return s;
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

  private void next_symbol_mod()  {
  	   next_symbol();
	   current = modify(current);
  }
  
// _______________________________________________________________________________________
// LINEAR TEMPORAL LOGIC EXPRESSION

private IFormulaSyntax ltl_unary() {   // !,<>,[]
	  ISymbol op = current;
	  switch (current.kind) {
	   case Symbol.PLING:
	   case Symbol.NEXTTIME:
       case Symbol.EVENTUALLY:
       case Symbol.ALWAYS:
		    next_symbol_mod ();
    		return FormulaSyntax.make(null,op,ltl_unary());
       case Symbol.UPPERIDENT:
    		next_symbol_mod();
    		if (current.kind==Symbol.LSQUARE)  {
				IActionLabels range = forallRanges();
				current = modify(current);
    		     return FormulaSyntax.make(op,range);
    		} else  {
    	        return  FormulaSyntax.make(op);
			}
    	case Symbol.LROUND:
    	    next_symbol_mod ();
    		IFormulaSyntax right = ltl_or ();
    		current_is (Symbol.RROUND, ") expected to end LTL expression");
    		next_symbol_mod();
    		return right;
    	case Symbol.IDENTIFIER:
		case Symbol.LSQUARE:
		case Symbol.LCURLY:
			IActionLabels ts = labelElement();
			push_symbol();
			next_symbol_mod();
    		return FormulaSyntax.make(ts);
		case Symbol.ALLOR:
			next_symbol_mod();
			IActionLabels ff = forallRanges();
			push_symbol();
			next_symbol_mod();
			return FormulaSyntax.make(new Symbol(Symbol.OR),ff,ltl_unary());
		case Symbol.ALLAND:
			next_symbol_mod();
			ff = forallRanges();
			push_symbol();
			next_symbol_mod();
			return FormulaSyntax.make(new Symbol(Symbol.AND),ff,ltl_unary());
		default:
		    Diagnostics.fatal("syntax error in LTL expression",current);
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

    private IFormulaSyntax ltl_or () {	// |
 
        IFormulaSyntax left = ltl_binary ();
        while (current.kind == Symbol.OR) {
            ISymbol op = current;
            next_symbol_mod ();
            IFormulaSyntax right = ltl_binary ();
            left = FormulaSyntax.make(left,op,right);
        }
        return left;
    }

// _______________________________________________________________________________________
// LTS_BINARY

    private IFormulaSyntax ltl_binary () {	// until, ->
        IFormulaSyntax left = ltl_and ();
        if (current.kind == Symbol.UNTIL || current.kind == Symbol.WEAKUNTIL || current.kind == Symbol.ARROW || current.kind == Symbol.EQUIVALENT) {
            ISymbol op = current;
            next_symbol_mod ();
            IFormulaSyntax right = ltl_and ();
            left = FormulaSyntax.make(left,op,right);
        }
        return left;
    }


//
//___________________________________________________________________________________
// STATE PREDICATE DEFINITIONS

    private void predicateDefinition() {

	current_is(Symbol.UPPERIDENT,"predicate identifier expected");
	ISymbol name = current;
	IActionLabels range = null;
	next_symbol();
	if (current.kind==Symbol.LSQUARE)
	    range = forallRanges();
	current_is(Symbol.BECOMES,"= expected");
	next_symbol();
	current_is(Symbol.LESS_THAN,"< expected");
	next_symbol();
	IActionLabels ts = labelElement();
	current_is(Symbol.COMMA,", expected");
	next_symbol();
	IActionLabels fs = labelElement();
	current_is(Symbol.GREATER_THAN,"> expected");
	next_symbol();
	if (current.kind == Symbol.INIT) {
  	    next_symbol();
	    Stack tmp = new Stack();
	    exprParser.simpleExpression(tmp);
	    push_symbol();
	    PredicateDefinition.put(name,range,ts,fs,tmp);
	} else {
  	    push_symbol();
	    PredicateDefinition.put(name,range,ts,fs,null);
	}
    }


	/**
	 * The class encapsulates an expression parser. By encapsulating
	 * in this way, the handling of integer and floating point
	 * expressions can be done in a uniform and simple manner.
	 */
	protected class ExpressionParser {

	// ___________________________________________________________________
	// EXPRESSION

	public void expression (Stack expr) {
		logical_or (expr);
	}

	// this is used to avoid a syntax problem
	// when a parallel composition
	// follows a range or constant definition e.g.
	// const N = 3
	// ||S = (P || Q)
	public void simpleExpression (Stack expr) {
		additive (expr);
	}

	// UNARY ---------------------------------
	protected void unary (Stack expr) {  // +, -, identifier,
		Symbol unary_operator;
		switch (current.kind) {
		case Symbol.PLUS:
			unary_operator = current;
			unary_operator.kind = Symbol.UNARY_PLUS;
			next_symbol ();
			break;
		case Symbol.MINUS:
			unary_operator = current;
			unary_operator.kind = Symbol.UNARY_MINUS;
			next_symbol ();
			break;
		case Symbol.PLING:
		unary_operator = current;
		next_symbol ();
		break;

		default:
			unary_operator = null;
		}
		switch (current.kind) {
		case Symbol.IDENTIFIER:
		case Symbol.UPPERIDENT:
		case Symbol.INT_VALUE:
		case Symbol.DOUBLE_VALUE:
			expr.push (current);
			next_symbol ();
			break;
		case Symbol.LROUND:
		next_symbol ();
			expression (expr);
			current_is (Symbol.RROUND, ") expected to end expression");
			next_symbol();
			break;
		case Symbol.HASH:
		unary_operator=new Symbol(current);
		case Symbol.QUOTE:         // this is a labelConstant
		Symbol temp = current; //preserve marker
		temp.setAny(labelConstant());
		temp.kind = Symbol.LABELCONST;
		expr.push(temp);
		break;
		case Symbol.AT:
		set_select(expr);
		break;
		default:
			error ("syntax error in expression");
		}

		if (unary_operator != null)
			expr.push(unary_operator);
	}

	// MULTIPLICATIVE

	protected void multiplicative (Stack expr) {	// *, /, %
		unary (expr);
		while (
		   current.kind == Symbol.STAR    ||
		   current.kind == Symbol.DIVIDE  ||
		   current.kind == Symbol.MODULUS
		   ) {
			Symbol op = current;
			next_symbol ();
			unary (expr);
			expr.push (op);
		}
	}

	// _______________________________________________________________________________________
	// ADDITIVE

	protected void additive (Stack expr) {	// +, -
		multiplicative (expr);
		while (current.kind == Symbol.PLUS || current.kind == Symbol.MINUS) {
			Symbol op = current;
			next_symbol();
			multiplicative (expr);
			expr.push(op);
		}
	}


	// _______________________________________________________________________________________
	// SHIFT

	protected void shift (Stack expr) {	// <<, >>
		additive (expr);
		while (current.kind==Symbol.SHIFT_LEFT || current.kind==Symbol.SHIFT_RIGHT) {
		Symbol op = current;
		next_symbol ();
		additive (expr);
		expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// RELATIONAL

	protected void relational (Stack expr) {	// <, <=, >, >=
		shift (expr);
		while (
		   current.kind == Symbol.LESS_THAN ||
		   current.kind == Symbol.LESS_THAN_EQUAL ||
		   current.kind == Symbol.GREATER_THAN ||
		   current.kind == Symbol.GREATER_THAN_EQUAL
		   ) {
		Symbol op = current;
		next_symbol ();
		shift (expr);
		expr.push (op);
		}
	}

	// _______________________________________________________________________________________
	// EQUALITY

	protected void equality (Stack expr) {	// ==, !=
		relational (expr);
		while (current.kind == Symbol.EQUALS || current.kind == Symbol.NOT_EQUAL) {
		Symbol op = current;
		next_symbol ();
		relational (expr);
		expr.push (op);
		}
	}

	// _______________________________________________________________________________________
	// AND

	protected void and (Stack expr) {	// &
		equality (expr);
		while (current.kind == Symbol.BITWISE_AND) {
		Symbol op = current;
		next_symbol ();
		equality (expr);
		expr.push (op);
		}
	}

	// _______________________________________________________________________________________
	// EXCLUSIVE_OR

	protected void exclusive_or (Stack expr) {	// ^
		and (expr);
		while (current.kind == Symbol.CIRCUMFLEX) {
		Symbol op = current;
		next_symbol ();
		and (expr);
		expr.push (op);
		}
	}

	// _______________________________________________________________________________________
	// INCLUSIVE_OR

	protected void inclusive_or (Stack expr) {	// |
		exclusive_or (expr);
		while (current.kind == Symbol.BITWISE_OR) {
		Symbol op = current;
		next_symbol ();
		exclusive_or (expr);
		expr.push (op);
		}
	}

	// _______________________________________________________________________________________
	// LOGICAL_AND

	protected void logical_and (Stack expr) {	// &&
		inclusive_or (expr);
		while (current.kind == Symbol.AND) {
		Symbol op = current;
		next_symbol ();
		inclusive_or (expr);
		expr.push(op);
		}
	}

	// _______________________________________________________________________________________
	// LOGICAL_OR

	protected void logical_or (Stack expr) {	// ||
		logical_and (expr);
		while (current.kind == Symbol.OR) {
		Symbol op = current;
		next_symbol ();
		logical_and (expr);
		expr.push (op);
		}
	}
	}

	private class FloatExpressionParser
	extends ExpressionParser {

	public void expression( Stack s ) {
		super.expression( s );
	}

	public void simpleExpression( Stack s ) {
		super.simpleExpression( s );
	}

	protected void unary (Stack expr) {  // +, -, identifier,
		/* handle unary operators */
		Symbol unary_operator;
		switch (current.kind) {
		case Symbol.PLUS:
			unary_operator = current;
			unary_operator.kind = Symbol.UNARY_PLUS;
			next_symbol ();
			break;
		case Symbol.MINUS:
			unary_operator = current;
			unary_operator.kind = Symbol.UNARY_MINUS;
			next_symbol ();
			break;
		case Symbol.PLING:
		unary_operator = current;
		next_symbol ();
		break;

		default:
			unary_operator = null;
		}

		/* handles BaseFloatExpr */
		switch (current.kind) {
		case Symbol.IDENTIFIER:
		case Symbol.UPPERIDENT:
		case Symbol.INT_VALUE:
		case Symbol.DOUBLE_VALUE:
			expr.push (current);
			next_symbol ();
			break;
		case Symbol.LROUND:
		next_symbol ();
		// only allow FloatExpression in bracket terms
			simpleExpression( expr );
			current_is (Symbol.RROUND, ") expected to end expression");
			next_symbol();
			break;
		default:
			error ("syntax error in expression");
		}

		if (unary_operator != null)
			expr.push(unary_operator);
	}

	protected void multiplicative (Stack expr) {
		unary( expr );
		while( current.kind == Symbol.STAR
		   || current.kind == Symbol.DIVIDE ) {
			Symbol op = current;
			next_symbol();
			unary( expr );
			expr.push( op );
		}
	}
	}

	/*---------------------------------------------------------------------*
	 *-- Extended FSP Code                                               --*
	 *---------------------------------------------------------------------*/

	/** Symbol that opens a probability expression */
	private static final int PROB_OPEN = Symbol.LROUND;
	/** Symbol that closes a probability expression */
	private static final int PROB_CLOSE = Symbol.RROUND;
	/** Symbol that opens a clock condition */
	private static final int CL_C_OPEN = Symbol.QUESTION;
	/** Symbol that closes a clock condition */
	private static final int CL_C_CLOSE = Symbol.QUESTION;
	/** Symbol that opens a clock setting action */
	private static final int CL_S_OPEN = Symbol.LESS_THAN;
	/** Symbol that closes a clock setting action */
	private static final int CL_S_CLOSE = Symbol.GREATER_THAN;

	/** ProbChoice */
	private void probChoiceExpr( StateExpr stateExpr ) {
		stateExpr.choices = new Vector();
	
		push_symbol();
		do {
			Stack expr;
			ChoiceElement choice;
	
			// parse probability expression
			next_symbol();
			current_is( PROB_OPEN, "expected '"	+ (new Symbol(PROB_OPEN)).toString() + "'" );
	
			next_symbol();
			expr = new Stack();
			flExprParser.simpleExpression( expr );
	
			current_is( PROB_CLOSE, "expected '" + (new Symbol(PROB_CLOSE)).toString() + "'" );
		    
			next_symbol();
	
			choice = choiceElement();
			// only set probability if extended code is turned on
			if (probabilisticSystem) choice.setProbExpr(expr);
			stateExpr.choices.addElement(choice);
			// must have at least two choices in probabilistic transition
			// not anymore
//			if (stateExpr.choices.size() == 1 )
//				current_is( Symbol.BITWISE_OR, "expected '|': must have at least two probabilistic choices" );
		} while( current.kind == Symbol.BITWISE_OR );
		
	}

	/** Parses <code>ClockSettings</code> grammar terms.
	 * @return A set of {@link ClockActionSpec} */
	private Collection clockSettings() {
	Collection setActions = new Vector();

	setActions.add( clockSetting() );

	while( current.kind != CL_S_CLOSE ) {
		current_is( Symbol.COMMA, "',' or '>' expected" );
		next_symbol();
		setActions.add( clockSetting() );
	}

	return setActions;
	}

	/** Parses <code>ClockSetting</code> grammar terms.
	 * @return a description of the clock action */
	private ClockActionSpec clockSetting() {
	String clockName;
	ClockActionSpec spec;

	current_is( Symbol.IDENTIFIER, "identifier expected" );
	clockName = current.toString();
	next_symbol();

	current_is( Symbol.COLON, "':' expected" );
	next_symbol();

	current_is(Symbol.IDENTIFIER, "hold, resume or distribution expected");

	if( current.toString().equals( "hold" ) ) {
		spec = new ClockHoldSpec( clockName );
		next_symbol();
	} else if( current.toString().equals( "resume" ) ) {
		spec = new ClockResumeSpec( clockName );
		next_symbol();
	} else {
		spec = new ClockSettingSpec( clockName, distribution() );
	}

	return spec;
	}

	/** Parses <code>Distribution</code> grammar terms.
	 * @return A description of the distribution */
	private DistributionSpec distribution() {
	DistributionSpec dist;

	dist = new DistributionSpec( current.toString() );
	next_symbol();

	if( current.kind == Symbol.LROUND ) {
		Stack tmp = new Stack();

		next_symbol();

		flExprParser.simpleExpression( tmp );
		dist.addParameter( tmp );

		while( current.kind != Symbol.RROUND ) {
		current_is( Symbol.COMMA, "',' expected" );
		next_symbol();

		tmp = new Stack();
		flExprParser.simpleExpression( tmp );
		dist.addParameter( tmp );
		}

		next_symbol();
	}

	dist.lockParameters();
	return dist;
	}

	/** Processes clock shorthand syntax, creating the appropriate
	 * extra <code>tau</code> actions with clock setting and clock
	 * conditions.
	 * @param ce The current choice element (should be new)
	 * @return The choice element following the <code>tau</code>
	 * actions, if any, otherwise the choice element passed in.
	 */
	private ChoiceElement doClockShorthand( ChoiceElement ce ) {
	if( current.kind == CL_S_OPEN ) {
		Symbol tmp = current;
		next_symbol();
		if( current.kind == CL_C_OPEN ) {
		DistributionSpec dist;
		// dealing with the shorthand case
		next_symbol();
		dist = distribution();

		current_is( CL_C_CLOSE, "'?' expected" );
		next_symbol();
		current_is( CL_S_CLOSE, "'>' expected" );
		next_symbol();

		// now have to kludge the appropriate tau actions into
		// the choice element
		StateExpr stateExpr;
		String clockName = ClockSettingSpec.getAnonymousClockName();

		ce.action = new ActionName( new Symbol( Symbol.IDENTIFIER, "tau" ) );
		ce.clockConditions = new Vector();
		ce.clockActions = new Vector();
		ce.clockActions.add( new ClockSettingSpec( clockName, dist ) );
		
		stateExpr = new StateExpr();
		ce.stateExpr = stateExpr;

		ce = new ChoiceElement();
		ce.clockConditions = new Vector();
		ce.clockConditions.add( new ClockConditionSpec( clockName ) );
		ce.clockActions = new Vector();
		Stack s = new Stack();
		// if extended, add probability decoration
		s.push(new Symbol(Symbol.INT_VALUE,1));
		if (probabilisticSystem) ce.setProbExpr(s);
		stateExpr.choices = new Vector();
		stateExpr.choices.add( ce );
		} else {
		// not shorthand, leave for doClockSetting
		push_symbol();
		current = tmp;
		}
	}

	return ce;
	}

	/** Parses clock guards.
	 * @return A collection of {@link ClockConditionSpec}
	 */
	private Collection doClockGuard() {
	Collection guards = new Vector();

	if( current.kind == CL_C_OPEN ) {
		next_symbol();
		guards = clockConditions();
		current_is( CL_C_CLOSE, "'?' expected" );
		next_symbol();
	}

	return guards;
	}

	/** Parses clock setting action sets.
	 * @return A set of {@link ClockSettingSpec} objects
	 */
	private Collection doClockSetting() {
	Collection settings = new Vector();

	if( current.kind == CL_S_OPEN ) {
		next_symbol();
		settings = clockSettings();
		current_is( CL_S_CLOSE, "'>' expected" );
		next_symbol();
	}

	return settings;
	}

	/** Parses <code>ClockConditions</code> grammar terms.
	 * @return A (possibly empty) {@link Collection} of {@link
	 * ClockConditionSpec} objects */
	private Collection clockConditions() {
	Collection conds = new Vector();
	conds.add( clockCondition() );

	while( current.kind != CL_C_CLOSE ) {
		current_is( Symbol.COMMA, "',' or '?' expected" );
		next_symbol();

		conds.add( clockCondition() );
	}

	return conds;
	}

	private ClockConditionSpec clockCondition() {
	boolean conditionType = true;
	String clockName;
	String errMsg = "! or identifier expected";
	
	if( current.kind == Symbol.PLING ) {
		conditionType = false;
		errMsg = "identifier expected";
		next_symbol();
	}

	current_is( Symbol.IDENTIFIER, errMsg );
	clockName = current.toString();
	next_symbol();

	ClockConditionSpec r = new ClockConditionSpec( clockName, conditionType );
	return r;
	}

	/** TimerDef */
	private TimerSpec timerDef() {
		TimerSpec t;
	
		current_is( Symbol.TIMER, "incorrect use of timerDef" );
		next_symbol();
	
		current_is( Symbol.UPPERIDENT, "process identifier expected" );
		t= new TimerSpec( current.toString() );
		next_symbol();
	
		if( current.kind == Symbol.LESS_THAN ) {
			timerPair( t );
			push_symbol();
		} else if( current.kind == Symbol.LCURLY ) {
			next_symbol();
		    
			timerPair( t );
			while( current.kind == Symbol.COMMA ) {
			next_symbol();
			timerPair( t );
			}
		    
			current_is( Symbol.RCURLY, "'}' expected" );
		} else {
			error( "'<' or '{' expected" );
		}
	
		return t;
	}

	private void timerPair( TimerSpec t ) {
		IActionLabels start, stop, range=null;
	
		if( current.kind == Symbol.FORALL ) {
			next_symbol();
			range = forallRanges();
		}
	
		current_is( Symbol.LESS_THAN, "'forall' or '<' expected" );
		next_symbol();
	
		start = labelElement();
	
		current_is( Symbol.COMMA, "',' expected" );
		next_symbol();
	
		stop = labelElement();
	
		current_is( Symbol.GREATER_THAN, "'>' expected" );
		next_symbol();
	
		if( range!= null ) t.addStartStopRange( start, stop, range );
		else t.addStartStopPair( start, stop );
	}

	private PopulationCounterSpec measureDef() {
	PopulationCounterSpec m;
	String name;

	current_is( Symbol.MEASURE, "incorrect use of measureDef" );
	next_symbol();
	
	current_is( Symbol.UPPERIDENT, "process identifier expected" );
	name = current.toString();
	next_symbol();

	current_is( Symbol.LESS_THAN, "'<' expected" );

	m = new PopulationCounterSpec( name );
	next_symbol();
	    
	m.setIncrementActions( labelElement() );
	    
	current_is( Symbol.COMMA, "',' expected" );
	next_symbol();
	
	m.setDecrementActions( labelElement() );
	    
	current_is( Symbol.GREATER_THAN, "'>' expected" );

	return m;
	}

	/** CounterDef */
	private ActionCounterSpec counterDef() {
	ActionCounterSpec spec;
	String name;

	current_is( Symbol.COUNTER, "incorrect use of counterDef" );
	next_symbol();

	current_is( Symbol.UPPERIDENT, "process identifier expected" );
	name = current.toString();
	next_symbol();

	spec = new ActionCounterSpec( name );

	spec.setActions( new ActionSet( labelSet() ) );
	push_symbol();

	return spec;
	}

    public Map resolveIncludes(Map p_files) {

        // TODO Auto-generated method stub
        return null;
    }
}

