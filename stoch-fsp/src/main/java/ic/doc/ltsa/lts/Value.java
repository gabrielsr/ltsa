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

/* -------------------------------------------------------------------------------*/

class Value {
    private int val;
	private double dval;
    private String sval;
    private boolean sonly;
	private boolean isDouble;

	public Value(double d) {
		dval = d;
		isDouble = true;
		sonly = false;
	}

    public Value(int i) {
        val = i;
        sonly = false;
        sval = String.valueOf(i);
        isDouble = false;
    }

    public Value(String s) {   //convert string to integer if possible
        sval = s;
        isDouble = false;
        try {
            val = Integer.parseInt(s);
            sonly = false;
        } catch (NumberFormatException e) {
            sonly = true;
        }
    }

    public String toString() {
		if( isDouble )
			return Double.toString( dval );
		else
			return sval;
    }

    public int intValue() {
        return val;
    }
    
	public double doubleValue() {
		return dval;
	}

	public boolean isInt() {
		return !sonly && !isDouble;
	}

	public boolean isLabel() {
		return sonly;
	}

	public boolean isDouble() {
		return isDouble;
	}

}