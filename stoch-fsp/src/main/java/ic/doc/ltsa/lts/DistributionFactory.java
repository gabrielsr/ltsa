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

import ic.doc.simulation.tools.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Stack;

/**
 * Manages the instantiation of distribution samplers from
 * distribution specifications. The instantiation of this class is
 * controlled for performance reasons - on instantiation a large
 * number of expensive reflection calls are made.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class DistributionFactory {
    private static Map distTypes;

    private static final int EXP = 0;
    private static final int UNIFORM = 1;
    private static final int FIXED = 2;
    private static final int ERLANG = 3;
    private static final int GAMMA = 4;
    private static final int GEOMETRIC = 5;
    private static final int NORMAL = 6;
    private static final int WEIBULL = 7;

    private DistributionFactory() {
	distTypes = new Hashtable();

	try {
	    distTypes.put( "exp", getInfo( EXP ) );
	    distTypes.put( "uniform", getInfo( UNIFORM ) );
	    distTypes.put( "fixed", getInfo( FIXED ) );
	    distTypes.put( "erlang", getInfo( ERLANG ) );
	    distTypes.put( "gamma", getInfo( GAMMA ) );
	    distTypes.put( "geometric", getInfo( GEOMETRIC ) );
	    distTypes.put( "normal", getInfo( NORMAL ) );
	    distTypes.put( "weibull", getInfo( WEIBULL ));
	} catch( ClassNotFoundException e ) {
	    throw new RuntimeException( e.toString() );
	}
    }

    private static DistributionFactory inst = new DistributionFactory();

    /**
     * Returns an instance of a DistributionFactory.
     */
    public static DistributionFactory getInstance() {
	return inst;
    }

    /**
     * Instantiates the distribution specified, evaluating parameter
     * expressions to actual values using the local variables given.
     * @param name The name of the distribution type, e.g. 'exp'
     * @param params A vector of expression {@link Stack}s
     * @param locals A hashtable of local variables
     * @param globals A hashtable of global variables
     * @return A distribution sampler instantiated with the
     * appropriate parameters
     * @throws Exception If the instantiation fails for any reason
     */
    public DistributionSampler getDistributionInstance( String name,
							Vector params,
							Hashtable locals,
							Hashtable globals )
	throws Exception {
	DistInfo info = (DistInfo) distTypes.get( name );
	Vector actuals;
	DistributionSampler sampler;

	if( info == null )
	    Diagnostics.fatal( "Distribution type " + name + " does not exist" );

	if( params == null || params.size() != info.params.size() ) {
		StringBuffer s = new StringBuffer("Distribution type "+name + " expects parameters (");
		for (int i=0; i<info.params.size(); i++) {
			s.append(info.params.elementAt(i).toString().equals("double") ? "float" : "const");
			if (i<info.params.size()-1) s.append(',');
		}
		s.append(")");
	    Diagnostics.fatal(s.toString());
	}

	actuals = doParams( info, params, locals, globals );

	sampler = instantiate( info, actuals );
	
	return sampler;
    }

    private static DistributionSampler instantiate( DistInfo info, Vector params )
	throws NoSuchMethodException,
	SecurityException,
	InstantiationException,
	IllegalAccessException,
	IllegalArgumentException,
	InvocationTargetException
    {
	Class[] types = new Class[params.size()];
	Iterator i = params.iterator();
	int j = 0;
	
	while( i.hasNext() ) {
	    Object o = i.next();
	    types[j] = o.getClass();
	}


	Constructor c = info.impl.getConstructor( info.getParamTypes() );
	DistributionSampler s 
	    = (DistributionSampler) c.newInstance( params.toArray() );
	return s;
    }

    private static Vector doParams( DistInfo info, Vector params,
				    Hashtable locals, Hashtable globals ) {
	Iterator i, j;
	Vector actuals = new Vector();

	i = info.params.iterator();
	j = params.iterator();

	while( i.hasNext() ) {
	    Class type = (Class) i.next();
	    Stack expr = (Stack) j.next();

	    if( type.equals( double.class ) ) {
		double val = Expression.getDoubleValue( expr, locals, globals );
		actuals.add( new Double( val ) );
	    } else {
		int val = Expression.evaluate( expr, locals, globals );
		actuals.add( new Integer( val ) );
	    }
	}

	return actuals;
    }

    private static DistInfo getInfo( int type )
	throws ClassNotFoundException {
	DistInfo info = new DistInfo();
	switch( type ) {
	case EXP:
	    info.impl = Class.forName( "ic.doc.simulation.tools.Exp" );
	    info.params.add( double.class );
	    break;
	case UNIFORM:
	    info.impl = Class.forName( "ic.doc.simulation.tools.Uniform" );
	    info.params.add( double.class );
	    info.params.add( double.class );
	    break;
	case FIXED:
	    info.impl = Class.forName( "ic.doc.simulation.tools.Fixed" );
	    info.params.add( double.class );
	    break;
	case ERLANG:
	    info.impl = Class.forName( "ic.doc.simulation.tools.Erlang" );
	    info.params.add( int.class );
	    info.params.add( double.class );
	    break;
	case GAMMA:
	    info.impl = Class.forName( "ic.doc.simulation.tools.Gamma" );
	    info.params.add( int.class );
		info.params.add( double.class );
	    break;
	case GEOMETRIC:
	    info.impl = Class.forName( "ic.doc.simulation.tools.Geometric" );
	    info.params.add( double.class );
	    break;
	case NORMAL:
	    info.impl = Class.forName( "ic.doc.simulation.tools.Normal" );
	    info.params.add( double.class );
	    info.params.add( double.class );
	    break;
	case WEIBULL:
		info.impl = Class.forName( "ic.doc.simulation.tools.Weibull" );
		info.params.add( double.class );
		info.params.add( double.class );
		break;
	default:
	    throw new RuntimeException( "Unsupported distribution type" );
	}

	return info;
    }

    private static class DistInfo {
	public Class impl;
	public Vector params = new Vector();

	public Class[] getParamTypes() {
	    Class[] types = new Class[params.size()];

	    for( int i = 0; i < params.size(); i++ ) {
		types[i] = (Class) params.elementAt( i );
	    }

	    return types;
	}
    }
}
