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
import ic.doc.ltsa.common.iface.IActionLabels;
import ic.doc.ltsa.common.iface.IMenuDefinition;
import ic.doc.ltsa.common.iface.ISymbol;

import java.util.*;


/* -----------------------------------------------------------------------*/

public class MenuDefinition implements IMenuDefinition {
    ISymbol name;
    IActionLabels actions;
    ISymbol params;
    ISymbol target;
    Vector actionMapDefn;
    Vector controlMapDefn;
    Vector animations;

    public static Hashtable definitions;

    public static void compile(){
        RunMenu.init();
        Enumeration e = definitions.elements();
        while (e.hasMoreElements()){
            MenuDefinition m = (MenuDefinition)e.nextElement();
            RunMenu.add(m.makeRunMenu());
        }
    }

    public String[] names() {
        if (definitions==null) return null;
        int n = definitions.size();
        String na[];
        if (n==0) return null; else na = new String[n];
        Enumeration e = definitions.keys();
        int i = 0;
        while (e.hasMoreElements())
            na[i++] = (String)e.nextElement();
        return na;
    }
    
    public boolean[] enabled(String targ) {
        if (definitions==null) return null;
        int n = definitions.size();
        boolean na[];
        if (n==0) return null; else na = new boolean[n];
        Enumeration e = definitions.keys();
        int i = 0;
        while (e.hasMoreElements()) {
            MenuDefinition m = (MenuDefinition)definitions.get((String)e.nextElement());
            na[i++] = m.target==null?true:targ.equals(m.target.toString());
        } 
        return na;
    }

    public RunMenu makeRunMenu(){
        String na = name.toString();
        if (params==null) {
            Vector a=null;
            a = actions.getActions(null,null);
            return new RunMenu(na,a);
        } else {
            Relation a = RelabelDefn.getRelabels(actionMapDefn);
            Relation c = RelabelDefn.getRelabels(controlMapDefn);
            if (a==null) a = new Relation(); else a = a.inverse();
            if (c==null) c = new Relation(); else c = c.inverse();
            includeParts(a,c);
            return new RunMenu(na,params==null?null:params.toString(),a,c);
        }
    }
    
    protected void includeParts(Relation actions, Relation controls){
        if (animations == null) return;
        Enumeration e = animations.elements();
        while(e.hasMoreElements()) {
           AnimationPart ap = (AnimationPart)e.nextElement();
           ap.makePart();
           actions.union(ap.getActions());
           controls.union(ap.getControls());
        }
    }        
    
    public void addAnimationPart(Symbol n, Vector r) {
      if (animations==null) animations =  new Vector();
      animations.addElement(new AnimationPart(n,r));
    }
    
    class AnimationPart {
      Symbol name;
      Vector relabels;
      RunMenu compiled;
      
      AnimationPart(Symbol n, Vector r) {
        name = n;
        relabels = r;
      }
      
      void makePart() {
        MenuDefinition m = (MenuDefinition)definitions.get(name.toString());
        if (m==null) {
          Diagnostics.fatal ("Animation not found: "+name, name);
          return;
        }
        if (m.params == null) {
          Diagnostics.fatal ("Not an animation: "+name, name);
          return;
        }
        compiled = m.makeRunMenu();
        if (relabels!=null) {
          Relation r = RelabelDefn.getRelabels(relabels);
          if (compiled.actions!=null) compiled.actions.relabel(r);
          if (compiled.controls!=null) compiled.controls.relabel(r);
        }
      }      
    
      Relation getActions() {
        if (compiled!=null) 
           return compiled.actions;
        else
           return null;
      }
      
      Relation getControls() {
        if (compiled!=null)
          return compiled.controls;
        else
          return null;
      }
      
    }// end AnimationPart
        

}
