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

import ic.doc.ltsa.common.iface.ICompilerFactory;
import ic.doc.ltsa.common.iface.ILTSCompiler;
import ic.doc.ltsa.common.iface.ILex;
import ic.doc.ltsa.common.iface.LTSInput;
import ic.doc.ltsa.common.iface.LTSOutput;

/**
 * This class provides a factory for "backend" compiler components. It provides
 * an implementation of ICompilerFactory, and forms the binding point between
 * the backend component and the core. The factory plugs in to the CompilerManager and the
 * core then calls it to create any instances of the compiler classes that it needs.
 * These can then all be encapsulated within the particular backend component.
 * 
 * This set of classes implements the "standard" backend, without support for
 * probabilistic models.
 * 
 * @author rbc
 */
public class CompilerFactory implements ICompilerFactory {

    public ILTSCompiler createCompiler(LTSInput pInput, LTSOutput pOutput, String pCurrentDirectory) {

        return new LTSCompiler( pInput , pOutput , pCurrentDirectory );
    }

    public ILex createLex( LTSInput pInput , boolean b ) {

        return new Lex( pInput , b );
    }
}
