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

import java.io.*;

public class OutputBuffer implements ic.doc.ltsa.common.iface.LTSOutput {

    private ByteArrayOutputStream oBaos;
    private PrintStream oPs;

    public OutputBuffer() {

        clearOutput();
    }

    public void out(String pStr) {

        oPs.print(pStr);
    }

    public void outln(String pStr) {

        oPs.println(pStr);
    }

    public void clearOutput() {

        oBaos = new ByteArrayOutputStream(10000);
        oPs = new PrintStream(oBaos);
    }

    public String toString() {

        return oBaos.toString();
    }
}
