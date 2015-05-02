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

/**
 * The initialise() method in ILTSAPlugin does not take any parameters, so it
 * is not possible to pass parameters from the LTSA core to the plugin during
 * the initialisation phase through the ILTSA interface.
 * 
 * The Initialisable interface allows plugins to provide a method than can be
 * called by their requirer, passing any parameters that are needed.
 * 
 * This is not a standard operation. It is currently only used in the SceneBeans
 * plugin. To use it in another plugin would require a change to the caller.
 * 
 * @author rbc
 */
public interface Initialisable {

    public void initialise( Object[] p_params );
}
