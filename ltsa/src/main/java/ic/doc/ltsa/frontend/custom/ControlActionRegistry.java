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

package ic.doc.ltsa.frontend.custom;

import java.util.*;

import ic.doc.ltsa.common.infra.Relation;

public class ControlActionRegistry {

    Map<String,Integer> actionNumber     = new HashMap<String,Integer>();      // maps model action names to menu indices
    Map<String,Integer> controlNumber    = new HashMap<String,Integer>();      // maps control names to indices

    Relation actionsToControls;                        // map of action names to controls
    Relation controlsToActions;                        // map of controls to action names

    int controlMap[][];                                // control indices to action indices
    int actionMap[][];                                 // action indices to control indices

    String actionAlphabet[];                           // action indice to action name
    String controlAlphabet[];                          // control indice to control name

    boolean controlState[];                            // true if signalled
    AnimationMessage msg;

    public ControlActionRegistry(Relation actionsToControls, AnimationMessage msg) {
        this.actionsToControls = actionsToControls;
        this.msg = msg;
    }

    void getAnimatorControls() {
        int index = 0;
        List<String> c = new ArrayList<String>();
        Enumeration e = controlsToActions.keys();
        while(e.hasMoreElements()) {
            String s = (String)e.nextElement();
            controlNumber.put(s,new Integer(index));
            c.add(s);
            ++index;
        }
        controlAlphabet = new String[index];
        c.toArray(controlAlphabet);
    }

    public List<String> getControls() {
        List<String> c = new ArrayList<String>();
        Enumeration e = actionsToControls.keys();
        while(e.hasMoreElements()) {
            String s = (String)e.nextElement();
            c.add(s);
        }
        return c;
     }
     
     public int controlled(String name) {
         Integer I = (Integer)actionNumber.get(name);
         if (I==null) return -1;
         return I.intValue();
     }

    void initMap(String[] alpha) {
        int temp[];
        actionAlphabet = alpha;
        //create map from action name to indice
        for(int i = 1; i<alpha.length; ++i)
            actionNumber.put(alpha[i], new Integer(i));
        //remove entries in actionsToControls that are not in actionNumber
        Enumeration e = actionsToControls.keys();
        Vector rr = new Vector();
        while (e.hasMoreElements()) {
            String s = (String)e.nextElement();
            if (actionNumber.get(s)==null) rr.addElement(s);
        }
        e = rr.elements();
        while(e.hasMoreElements()) {
            actionsToControls.remove(e.nextElement());
        }
        //create inverse mapping
        controlsToActions      = actionsToControls.inverse();
        //create control alphabet
        getAnimatorControls();
        controlMap = new int[controlAlphabet.length][];
        controlState = new boolean[controlAlphabet.length];
        //controls are initially true
        for(int i=0; i<controlState.length; ++i) controlState[i]=true;
        // now create map from animation control number to action number
        initControlMap();
        // now create map from action number to animation control number
        initActionMap();
    }

    protected void initControlMap() {
        int temp[];
        Enumeration e = controlsToActions.keys();
        while(e.hasMoreElements()) {
            String control = (String)e.nextElement();
            int ic = ((Integer)controlNumber.get(control)).intValue();
            Object actions = controlsToActions.get(control);
            if (actions instanceof String) {
                int ia = ((Integer)actionNumber.get((String)actions)).intValue();
                temp = new int[1];
                temp[0] = ia;
                controlMap[ic] = temp;
            } else {
                Vector v = (Vector) actions;
                temp = new int[v.size()];
                Enumeration en = v.elements();
                int ii = 0;
                while(en.hasMoreElements()) {
                    String s = (String) en.nextElement();
                    int ia = ((Integer)actionNumber.get(s)).intValue();
                    temp[ii] = ia;
                    ++ii;
                }
                controlMap[ic] = temp;
            }
        }
        //print();
    }

    protected void initActionMap() {
        actionMap = new int[actionAlphabet.length][];
        int temp[];
        Enumeration e = actionsToControls.keys();
        while(e.hasMoreElements()) {
            String action = (String)e.nextElement();
            int ia = ((Integer)actionNumber.get(action)).intValue();
            Object controls = actionsToControls.get(action);
            if (controls instanceof String) {
                int ic = ((Integer)controlNumber.get((String)controls)).intValue();
                temp = new int[1];
                temp[0] = ic;
                actionMap[ia] = temp;
            } else {
                Vector v = (Vector) controls;
                temp = new int[v.size()];
                Enumeration en = v.elements();
                int ii = 0;
                while(en.hasMoreElements()) {
                    String s = (String) en.nextElement();
                    int ic = ((Integer)controlNumber.get(s)).intValue();
                    temp[ii] = ic;
                    ++ii;
                }
                actionMap[ia] = temp;
            }
        }
    }


    void print() {
        for (int i=0; i<controlMap.length; i++ ) {
            System.out.println(controlAlphabet[i]);
            for (int j = 0; j<controlMap[i].length; j++) {
                System.out.print(" "+actionAlphabet[controlMap[i][j]]);
            }
            System.out.println();
        }
    }

    void mapControl(String name, boolean[] enabled, boolean set) {
        String s;
        if (set) s = "-enable-";
        else     s = "-disabl-";
        msg.debugMsg("-control"+s+name);
        Integer II = (Integer)controlNumber.get(name);
        if (II==null) return;
        int ic = II.intValue();  // the control index number
        controlState[ic] = set;
        if (controlMap[ic]==null) return;
        for (int i=0; i<controlMap[ic].length; ++i) {
            int ia = controlMap[ic][i];
            if (actionMap[ia].length == 1 ) {
                enabled[ia] = set;
            } else {
                boolean mask = set;
                for (int j = 0; j<actionMap[ia].length; ++j)
                    mask = mask && controlState[actionMap[ia][j]];
                enabled[ia] = mask;
            }
        }
    }
}
