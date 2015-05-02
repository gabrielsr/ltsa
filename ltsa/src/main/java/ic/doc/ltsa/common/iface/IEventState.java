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

package ic.doc.ltsa.common.iface;

import java.util.Enumeration;

public interface IEventState {

    int getEvent();

    int getNext();

    void setList(IEventState head);

    IEventState getList();

    IEventState getNondet();

    void setNondet(IEventState tr);

    void setNext(int i);

    int getMachine();

    void setEvent(int i);

    IEventState getPath();

    void setPath(IEventState path);

    void setMachine(int k);

    Enumeration elements();
    
    //-----------------
    
    double getProb();
    
    void setProb( double p );
    
//    IAction getAction();
//
//    void setAction(IAction pAct);
//    
//    ICondition getCondition();
//    
//    void setCondition(ICondition pCond);
}
