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

import java.util.Collection;
import java.util.Iterator;

public class Util {
    public static String toString( int[] s ) {
	StringBuffer str = new StringBuffer();

	str.append( '(' );
	for( int i=0; i<s.length; i++ ) {
	    str.append( s[i] );
	    if( i < s.length-1 ) str.append( ',' );
	}
	str.append( ')' );

	return str.toString();
    }

    public static String toString( byte[] s ) {
	StringBuffer str = new StringBuffer();

	if( s==null ) return "null";

	str.append( '(' );
	for( int i=0; i<s.length; i++ ) {
	    str.append( Byte.toString( s[i] ) );
	    if( i < s.length-1 ) str.append( ',' );
	}
	str.append( ')' );

	return str.toString();
    }

    public static String toString( double[] s ) {
	StringBuffer str = new StringBuffer();

	str.append( '[' );
	for( int i=0; i<s.length; i++ ) {
	    str.append( s[i] );
	    if( i < s.length-1 ) str.append( ',' );
	}
	str.append( ']' );

	return str.toString();
    }

    public static String toString( Collection c ) {
	if( c==null ) return "null";

	StringBuffer str = new StringBuffer();
	Iterator i = c.iterator();

	str.append( "[" );
	while( i.hasNext() ) {
	    Object o = i.next();
	    if( o instanceof byte[] )
		str.append( toString( (byte[]) o ) );
	    if( o instanceof int[] )
		str.append( toString( (int[]) o ) );
	    if( i.hasNext() )
		str.append( "," );
	}
	str.append( "]" );

	return str.toString();
    }
}
