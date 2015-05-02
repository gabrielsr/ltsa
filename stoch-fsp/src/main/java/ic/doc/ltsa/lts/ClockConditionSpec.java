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

/**
 * Describes a clock condition during the compilation process.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:29 $
 */
class ClockConditionSpec {
    private String name;
    private boolean negate;

    /** Creates a clock condition which asserts that the given clock
     * must have expired.
     * @param name The clock name that must have expired.
     * @pre name != null
     */
    public ClockConditionSpec( String name ) {
	this( name, true );
    }

    /** Creates a clock condition on the given clock and parity.
     * @param name The clock that this condition refers to.
     * @param cond The parity of the condition, i.e. true for must
     * have expired, false for must not have expired.
     * @pre name != null
     */
    public ClockConditionSpec( String name, boolean cond ) {
	this.name = name;
	this.negate = !cond;
    }

    /** Returns the name of the clock referred to. */
    public String getClockName() { return name; }

    /** Returns true if the condition is negated. */
    public boolean isNegated() { return negate; }

    public String toString() {
	return (negate ? "!" : "" ) + name;
    }
}
