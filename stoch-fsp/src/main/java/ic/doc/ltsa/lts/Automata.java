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

import ic.doc.ltsa.common.infra.MyList;

import java.util.Vector;

/* this interface presents the common operations
*  between composed automata & on-the-fly composition
*/

public interface Automata {
	
    //returns the alphabet
    public String[] getAlphabet();
    
    //returns the transitions from a particular state
    public MyList getTransitions(byte[] state);
    
    //returns name of violated property if ERROR found in getTransitions
    public String getViolatedProperty();
    
    //returns shortest trace to  state (vector of Strings)
    public Vector getTraceToState(byte[] from, byte[] to);
    
    //returns true if  END state
    public boolean END(byte[] state);
    
    //return the number of the START state
    public byte[] START();
    
    //set the Stack Checker for partial order reduction
    public void setStackChecker(StackCheck s);
    
    //returns true if partial order reduction
    public boolean isPartialOrder();
    
    //diable partial order 
    public void disablePartialOrder();
    
    //enable partial order
    public void enablePartialOrder();

    //returns true if Accepting state
    public  boolean isAccepting(byte[] state);
}
