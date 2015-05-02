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

import ic.doc.ltsa.common.iface.LTSOutput;

import java.util.Vector;

/**
 * This interface represents those objects which may be compiled into
 * {@link CompactState} objects. Specifically, these are likely to be
 * either {@link ProcessSpec} or {@link MeasureSpec} objects held in
 * the {@link LTSCompiler#processes} map.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:29 $
 * @see ProcessSpec
 * @see MeasureSpec
 */
public interface Compilable {
    /**
     * Compiles the process specification into a {@link CompactState}
     * object.
     * @param output The output to write messages to.
     */
    public CompactState makeCompactState( LTSOutput output, boolean probabilisticSystem );

    /**
     * Compiles the process specification into a {@link CompactState}
     * object using the actual parameter values given.
     * @param output The output to write messages to.
     * @param params The actual parameters to use.
     */
    public CompactState makeCompactState( LTSOutput output, Vector params, boolean probabilisticSystem );

    /**
     * Returns the number of parameters this process takes.
     */
    public int getNumberOfParameters();
}
