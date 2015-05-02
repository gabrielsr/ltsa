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
 
package ic.doc.ltsa.common.infra;

import ic.doc.ltsa.common.iface.ISymbol;
import ic.doc.ltsa.common.iface.LTSOutput;

public class Diagnostics {

private static LTSOutput output = null;
public  static boolean warningFlag = true;
public  static boolean warningsAreErrors = false;

public static void init(LTSOutput o){
    output = o;
}

public static void fatal (String errorMsg) {
	throw new LTSException (errorMsg);
}

public static void fatal (String errorMsg, Object marker) {
	throw new LTSException (errorMsg, marker);
}

public static void fatal (String errorMsg, Object marker , String file ) {
	throw new LTSException (errorMsg, marker, file);
}

public static void fatal (String errorMsg, ISymbol symbol) {
    if (symbol!=null)
	    throw new LTSException (errorMsg, new Integer(symbol.getStartPos()));
	else
	    throw new LTSException (errorMsg);
}


public static void fatal (String errorMsg, ISymbol symbol, String file ) {
	if (symbol != null ) {
	    throw new LTSException (errorMsg, new Integer(symbol.getStartPos()), file);
	} else {
	    throw new LTSException ( errorMsg );
	}
}


public static void warning(String warningMsg, String errorMsg, ISymbol symbol){
    if (warningsAreErrors) {
        fatal(errorMsg,symbol);
    } else if (warningFlag) {
        if (output==null)
            fatal("Diagnostic not initialised");
        output.outln("Warning - "+warningMsg);
    }
}

}