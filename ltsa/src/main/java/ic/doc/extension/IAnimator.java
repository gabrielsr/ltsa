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

package ic.doc.extension;

import java.util.*;
/**
 * Animator interface for triggering transitions in LTS models
 * 
 * @author jnm,rbc
 */
public interface IAnimator {

    /**
    * initialises the Animator with the list of
    * actions that are to be controlled
    * menu is a vector of strings
    * returns bitset of eligible menu actions
    */
    public BitSet initialise(List<String> name);

    /**
    * returns the alphabet of names from menu
    * that are contained in the model alphabet
    * indexes in this string array are consistent with
    * the bitsets returned by the step operations.
    * if menu was null then the complete model alphabet is returned
    */
    public String[] getMenuNames();

    /**
    * returns the model alphabet
    */
    public String[] getAllNames();

    /**
    * causes the Animator to take a single step
    * by choosing one of the eligible menu actions
    * returns a bitset of eligible menu actions
    */
    public BitSet menuStep(int choice);

    /**
    * causes the Animator to take a single step
    * by choosing one of the eligible non-menu actions
    */
    public BitSet singleStep();

    /**
    * return the action number in AllNames
    * of the last action chosen and executed
    */
    public int actionChosen();

    /**
    * return the action name
    * of the last action chosen and executed
    */
    public String actionNameChosen();


    /**
    * return true if error state has been reached
    */
    public boolean isError();
    
    /**
    * return true if END state has been reached
    */
    public boolean isEnd();

    /**
    * returns true if there is an eligible action
    * that is not a menu action
    */
    public boolean nonMenuChoice();


    /**
    * returns the bitset of menu actions with higher/lower priority
    */
    public BitSet getPriorityActions();

    /**
    * returns true if priority actions are low priority
    * flase if they are low priority
    */
    public boolean getPriority();

    /**
    * prints message on LTSA window
    */
    public void message(String msg);
    
    /**
    * -- these are used for replaying error traces
    */
    
    /** 
    * returns true if error trace exists
    */
    public boolean hasErrorTrace();
    
    /**
    *returns true if next element in the trace is eligible
    */
     public boolean traceChoice();
     
     /**
     *execute next step in error trace
     */
     public BitSet traceStep();
}
