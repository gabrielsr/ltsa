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

import java.util.List;
import java.util.Vector;

public interface ICompositeState {
    
    public ICompactState getComposition();
    
    public Vector getMachines();
    
    public String getName();
    
    public void analyse(LTSOutput pOut);
    public void analyse(LTSOutput pOut, boolean pCheckDeadlock );

    public void compose(LTSOutput pOut);

    public void minimise(LTSOutput pOut);

    public void setErrorTrace(List pTrace);

    public List getErrorTrace();

    public void checkProgress(LTSOutput pOut);

    public void checkLTL(LTSOutput pOut, ICompositeState ltl_property);

    public IFluentTrace getTracer();

    public void setReduction(boolean b);
}
