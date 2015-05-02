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
 
package ic.doc.ltsa.frontend.gui;

import ic.doc.ltsa.common.infra.Relation;

import java.util.*;

public class RunMenu {

    private String oName;
    private Vector oAlphabet;   // vector of strings
    private String oParams;
    private Relation oActions;
    private Relation oControls;

    public static Hashtable<String, RunMenu> sMenus; //  vector of all menus

    public static void init(){
        sMenus = new Hashtable<String, RunMenu>();
    }

    public RunMenu(String pName, String pParams, Relation pActions, Relation pControls) {
        oName = pName;
        oParams = pParams;
        oActions = pActions;
        oControls = pControls;
    }

    public RunMenu(String pName, Vector pActions) {
        oName = pName;
        oAlphabet = pActions;
    }
    
    public static void add(RunMenu pMenu) {
       sMenus.put(pMenu.oName ,pMenu);
    }

    public boolean isCustom() { return oParams!=null;}
    
    public String getParams() { return oParams; }
    
    public Relation getActions() { return oActions; }
    
    public Relation getControls() { return oControls; }
    
    public Vector getAlphabet() { return oAlphabet; }
}
