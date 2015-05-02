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

import java.util.*;

public abstract class ActionLabels implements IActionLabels {
  
  protected ActionLabels follower;  // next part of compound label    
  public void addFollower(IActionLabels f) { follower = (ActionLabels)f;}
  public ActionLabels getFollower() {return follower; } 
  
  protected Hashtable  locals;
  protected Hashtable  globals;
  
  /**
  * - initialises context for label generation
  */
  public  void initContext(Hashtable locals, Hashtable globals) {
      this.locals  = locals;
      this.globals = globals;
      initialise();
      checkDuplicateVarDefn();
      if (follower!=null) follower.initContext(locals,globals);
  }
  
  public void clearContext() {
    removeVarDefn();
    if (follower!=null) follower.clearContext();
  } 
  
  /**
  * - returns string for this label and moves counter
  */
  public String nextName(){ 
    String s = computeName();
    if (follower!=null) {     
      s = s+"."+follower.nextName();
      if (!follower.hasMoreNames()) {
         follower.initialise();
         next();
       }
     } else {
       next();
     }
     return s;
  }
  
  /** 
  * - returns false if no more names
  */
  public abstract boolean hasMoreNames();
  
  /** 
  * default implementations for ActionLabels with no variables 
  */
  
  public Vector getActions(Hashtable locals, Hashtable constants) {
        Vector v = new Vector();
        initContext(locals,constants);
        while(hasMoreNames()) {
            String s = nextName();
            v.addElement(s);
        }
        clearContext();
        return v;
  }
  
  public boolean hasMultipleValues() {
      if (this instanceof ActionRange || this instanceof ActionSet
          || this instanceof ActionVarRange || this instanceof ActionVarSet)
         return true;
      else if (follower!=null)
         return follower.hasMultipleValues();
      return false;
  }
    
  /** 
  * default implementations for ActionLabels with no variables 
  */  
  
  protected void checkDuplicateVarDefn(){}
  
  protected void removeVarDefn(){}
  
  protected abstract String computeName(); 
  
  protected abstract void next(); 
  
  protected abstract void initialise();
  
  /* clone operation
  */
  
 public ActionLabels myclone() {
  	     ActionLabels an = make();
  	     if (follower!=null)
  	     	an.follower = follower.myclone();
  	     return an;
 }  
  
  protected abstract ActionLabels make();
    
}

/**
*  -- evaluate lowerident labels
*/
class ActionName extends ActionLabels {
  
  protected Symbol name;
  
  public ActionName(Symbol name) {
    this.name = name;
  }
  
  protected String computeName() {return name.toString();}
  
  protected boolean consumed;
  
  protected void initialise() {consumed = false;}
  protected void next() {consumed = true;}
  public boolean  hasMoreNames() {return !consumed;}
  
  protected ActionLabels make() {
  	     return new ActionName(name);
  }
  
  public String toString() {
	return name.toString();
  }
  
}

/**
*  -- evaluate [expr] labels
*/
class ActionExpr extends ActionLabels {
  
  protected Stack expr;
  
  public ActionExpr(Stack expr) {
    this.expr = expr;
  }
  
  protected String computeName() {
     Value v = Expression.getValue(expr,locals,globals);
     return v.toString();
  }
    
  protected boolean consumed;
  
  protected void initialise() {consumed = false;}
  protected void next() {consumed = true;}
  public boolean  hasMoreNames() {return !consumed;}
  
  protected ActionLabels make() {
  	     return new ActionExpr(expr);
  }
  
  public String toString() {
	  return( "[expr]" );
  }

}

/**
*  -- evaluate {a,b,c,d,e} labels
*/
class ActionSet extends ActionLabels {
  
  protected LabelSet set;
  protected Vector actions;

  public ActionSet(LabelSet set) {
    this.set = set;
  }
  
  protected String computeName() {
     return (String)actions.elementAt(current);
  }
    
  protected int current,high,low;
  
  protected void initialise() {
      actions = set.getActions(locals,globals);
      current = low= 0;
      high = actions.size()-1;
  }
  protected void next() {++current;}
  public boolean  hasMoreNames() {return (current<=high);}
  
  protected ActionLabels make() {
  	     return new ActionSet(set);
  }

}

/**
*  -- evaluates {a,b,c,d,e}\{d,e} labels
*/

class ActionSetExpr extends ActionLabels {
  
  protected LabelSet left;
  protected LabelSet right;
  protected Vector actions;

  public ActionSetExpr(LabelSet left, LabelSet right ) {
    this.left = left;
    this.right = right;
  }
  
  protected String computeName() {
     return (String)actions.elementAt(current);
  }
    
  protected int current,high,low;
  
  protected void initialise() {
      Vector left_actions = left.getActions(locals,globals);
      Vector right_actions = right.getActions(locals,globals);
      actions = new Vector();
      Enumeration e = left_actions.elements();
      while(e.hasMoreElements()){
      	  String s = (String)e.nextElement();
      	  if (!right_actions.contains(s)) actions.addElement(s);
      }
      current = low= 0;
      high = actions.size()-1;
  }
  protected void next() {++current;}
  public boolean  hasMoreNames() {return (current<=high);}
  
  protected ActionLabels make() {
  	     return new ActionSetExpr(left,right);
  }

}


/**
*  -- evaluate [low..high] labels
*/
class ActionRange extends ActionLabels {
  
  Stack rlow;
  Stack rhigh;

  public ActionRange(Stack low, Stack high) {
    this.rlow = low;
    this.rhigh = high;
  }
  
  public ActionRange(Range r) {
    rlow = r.low;
    rhigh = r.high;
  }
  
  protected String computeName() {
     return String.valueOf(current);
  }
    
  protected int current,high,low;
  
  protected void initialise() {
      low = Expression.evaluate(rlow,locals,globals);
      high = Expression.evaluate(rhigh,locals,globals);
      if (low>high) Diagnostics.fatal("Range not defined",(Symbol)rlow.peek());
      current = low;
  }
  protected void next() {++current;}
  public boolean  hasMoreNames() {return (current<=high);}
  
  protected ActionLabels make() {
  	     return new ActionRange(rlow,rhigh);
  }

}
 
/**
*  -- evaluate [i:low..high] labels
*/
class ActionVarRange extends ActionRange {
  
  protected Symbol var;

  public ActionVarRange(Symbol var, Stack low, Stack high) {
    super(low,high);
    this.var = var;
  }
  
  public ActionVarRange(Symbol var, Range r) {
    super(r);
    this.var = var;
  }

    
  protected String computeName() {
     if (locals!=null) 
       locals.put(var.toString(),new Value(current));
     return String.valueOf(current);
  }
  
  protected void checkDuplicateVarDefn(){
     if (locals == null) return;
     if (locals.get(var.toString())!=null)
        Diagnostics.fatal("Duplicate variable definition: "+var,var);
  }
  
  protected void removeVarDefn(){
    if (locals!=null)
       locals.remove(var.toString());
  } 
  
  protected ActionLabels make() {
  	     return new ActionVarRange(var,rlow,rhigh);
  }

}

/**
*  -- evaluate [i:low..high] labels
*/
class ActionVarSet extends ActionSet {
  
  protected Symbol var;

  public ActionVarSet(Symbol var, LabelSet set) {
    super(set);
    this.var = var;
  }
  
  protected String computeName() {
     String s = (String)actions.elementAt(current);
     if (locals!=null) 
       locals.put(var.toString(),new Value(s));
     return s;
  }
  
  protected void checkDuplicateVarDefn(){
     if (locals == null) return;
     if (locals.get(var.toString())!=null)
        Diagnostics.fatal("Duplicate variable definition: "+var,var);
  }
  
  protected void removeVarDefn(){
    if (locals!=null)
       locals.remove(var.toString());
  } 
  
  protected ActionLabels make() {
  	     return new ActionVarSet(var,set);
  }

}


  
  
  