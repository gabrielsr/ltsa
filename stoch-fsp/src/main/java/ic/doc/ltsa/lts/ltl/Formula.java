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
 
package ic.doc.ltsa.lts.ltl;

import ic.doc.ltsa.common.ast.*;
import ic.doc.ltsa.common.iface.ISymbol;

import java.util.BitSet;

/*
* abstract syntax tree for LTL formlae
*/



abstract public class Formula implements IFormula, Comparable {

    private int id = -1;

    public void setId(int i) {

        id = i;
    }

    public int getId() {

        return id;
    }

    private int untilsIndex = -1;
    private BitSet rightOfWhichUntil;
    private boolean _visited = false;

    public boolean visited() {

        return _visited;
    }

    public void setVisited() {

        _visited = true;
    }

    public int getUI() {

        return untilsIndex;
    }

    public void setUI(int i) {

        untilsIndex = i;
    }

    public void setRofUI(int i) {

        if (rightOfWhichUntil == null)
            rightOfWhichUntil = new BitSet();
        rightOfWhichUntil.set(i);
    }

    public BitSet getRofWU() {

        return rightOfWhichUntil;
    }

    public boolean isRightOfUntil() {

        return rightOfWhichUntil != null;
    }

    public int compareTo(Object obj) {

        return id - ((Formula) obj).id;
    }

    public abstract IFormula accept(IVisitor v);

    public boolean isLiteral() {

        return false;
    }

    public IFormula getSub1() {

        return accept(Sub1.get());
    }

    public IFormula getSub2() {

        return accept(Sub2.get());
    }
}


/*
*  get left sub formula or right for R
*/

class Sub1 implements IVisitor {
	private static Sub1 inst;
	private Sub1(){}
	public static Sub1 get(){ 
		 if (inst==null) inst = new Sub1();
		 return inst;
	}
	public IFormula visit(ITrue t) {return null;}
	public IFormula visit(IFalse f){return null;}
	public IFormula visit(IProposition p){return null;}
	public IFormula visit(INot n){return n.getNext();}
	public IFormula visit(INext n){return n.getNext();}
	public IFormula visit(IAnd a){return a.getLeft();}
	public IFormula visit(IOr o){return o.getLeft();}
	public IFormula visit(IUntil u){return u.getLeft();}
	public IFormula visit(IRelease r){return r.getRight();}
}

/*
*  get right sub formula or left for R
*/

class Sub2 implements IVisitor {
	private static Sub2 inst;
	private Sub2(){}
	public static Sub2 get(){ 
		 if (inst==null) inst = new Sub2();
		 return inst;
	}
	public IFormula visit(ITrue t) {return null;}
	public IFormula visit(IFalse f){return null;}
	public IFormula visit(IProposition p){return null;}
	public IFormula visit(INot n){return null;}
	public IFormula visit(INext n){return null;}
	public IFormula visit(IAnd a){return a.getRight();}
	public IFormula visit(IOr o){return o.getRight();}
	public IFormula visit(IUntil u){return u.getRight();}
	public IFormula visit(IRelease r){return r.getLeft();}
}


/*
* represent constant True
*/	

class True extends Formula implements ITrue {
	 private static True t;
	 private True(){}
	 
	 public static True make() {
	 	if (t==null) {t = new True(); t.setId(1);}
	 	return t;
	 }
	 public String toString() { return "true";}
	 public IFormula accept(IVisitor v) {return v.visit(this);}
	 public boolean isLiteral() {return true;}
}

/*
* represent constant False
*/	

class False extends Formula implements IFalse {
	private static False f;
	 private False(){}
	 
	 public static False make() {
	 	if (f==null) {f = new False(); f.setId(0);}
	 	return f;
	 }
	 public String toString() { return "false";}
	 public IFormula accept(IVisitor v) {return v.visit(this);}
	 public boolean isLiteral() {return true;}
}

/*
* represent proposition
*/	

class Proposition extends Formula implements IProposition {
	 ISymbol sym;
	 Proposition(ISymbol s) {sym = s;}
	 public String toString() { return sym.toString();}
	 public IFormula accept(IVisitor v) {return v.visit(this);}
	 public boolean isLiteral() {return true;}
}

/*
* represent not !
*/	

class Not extends Formula implements INot {
	 IFormula next;
	 public IFormula getNext() {return next;}
	 Not(IFormula f) {next = f;}
	 public String toString() { return "!"+next.toString();}
	 public IFormula accept(IVisitor v) {return v.visit(this);}
	 public boolean isLiteral() {return next.isLiteral();}
}

/*
* represent next  X
*/	

class Next extends Formula implements INext {
	 IFormula next;
	 public IFormula getNext() {return next;}
	 Next(IFormula f) {next = f;}
	 public String toString() { return "X "+next.toString();}
	 public IFormula accept(IVisitor v) {return v.visit(this);}
}


/*
* represent or \/ |
*/	

class Or extends Formula implements IOr {
	 IFormula left,right;
	 public IFormula getLeft() {return left;}
	 public IFormula getRight() {return right;}
	 Or(IFormula l, IFormula r) {left=l; right=r;}
	 public String toString() { 	return "("+left.toString()+" | "+right.toString()+")";}
	 public IFormula accept(IVisitor v) {return v.visit(this);}
}

/*
* represent and /\ &
*/	

class And extends Formula implements IAnd {
	 IFormula left,right;
	 public IFormula getLeft() {return left;}
	 public IFormula getRight() {return right;}
	 And(IFormula l, IFormula r) {left=l; right=r;}
	 public String toString() { 	return "("+left.toString()+" & "+right.toString()+")";}
	 public IFormula accept(IVisitor v) {return v.visit(this);}
}

/*
* represent until U
*/	

class Until extends Formula implements IUntil {
	 IFormula left,right;
	 public IFormula getLeft() {return left;}
	 public IFormula getRight() {return right;}
	 Until(IFormula l, IFormula r) {left=l; right=r;}
	 public String toString() { 	return "("+left.toString()+" U "+right.toString()+")";}
	 public IFormula accept(IVisitor v) {return v.visit(this);}
}

/*
* represent release R
*/	

class Release extends Formula implements IRelease {
	 IFormula left,right;
	 public IFormula getLeft() {return left;}
	 public IFormula getRight() {return right;}
	 Release(IFormula l, IFormula r) {left=l; right=r;}
	 public String toString() { 	return "("+left.toString()+" R "+right.toString()+")";}
	 public IFormula accept(IVisitor v) {return v.visit(this);}
}
