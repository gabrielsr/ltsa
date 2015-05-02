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
 
package gov.nasa.ltl.graph;

import java.util.List;

/**
 * This class provides a null implementation of the Node class to
 * compile against. This can be exchanged for the full version, from NASA AMES
 * at runtime, if the user has access to that code.
 */
public class Node {

    public Node( Graph p_g ) {}

    public void setStringAttribute( String p_s1 , String p_s2 ) {}
    public String getStringAttribute( String p_s ) { return null; }
    public void setBooleanAttribute( String p_s , boolean p_b ) {}
    public boolean getBooleanAttribute( String p_s ) { return false; }
    public void setIntAttribute( String p_s , int p_i ) {}
    public int getIntAttribute( String p_s ) { return 0; }

    public List getOutgoingEdges() { return null; }
    public int getId() { return 0; }
}
