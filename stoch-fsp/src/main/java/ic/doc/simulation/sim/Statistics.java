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

package ic.doc.simulation.sim;

/**
 * <p>A collection of statistics utilities. The class cannot be
 * instantiated, it is simply a collection of static methods.
 *
 * <p>This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version, provided that any
 * use properly credits the author.  This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details at <a href="http://www.gnu.org">http://www.gnu.org</a>.
 *
 * @author Thomas Ayles, except {@link #tQuantile(double,int,boolean)}
 * adapted from code by Sundar Dorai-Raj (sdoraira@vt.edu)
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class Statistics {

    private Statistics() {}

    /** Calculates the sum of a set of data points.
     */
    public static double sum( double[] x ) {
		double sum = 0;
		for( int i=0; i<x.length; i++ ) sum += x[i];
		return sum;
    }

    /** Calculates the arithmetic mean of a set of data points.
     */
    public static double mean( double[] x ) {
		return sum(x) / x.length;
    }

    /** Calculates the variance of a set of data points.
     */
    public static double variance( double[] x ) {
    	if (x.length==1) return 0;
		double var = 0;
		double mean = mean( x );
		for( int i=0; i<x.length; i++ ) {
		    var += (x[i] - mean)*(x[i] - mean);
		}
		return var / (x.length-1);
    }

    /** Finds the minimum value in a set of data points.
     */
    public static double minimum( double[] x ) {
		double min = x[0];
		for( int i=1; i<x.length; i++ ) {
		    if( x[i] < min ) min = x[i];
		}
		return min;
    }

    /** Finds the maximum in a set of data points.
     */
    public static double maximum( double[] x ) {
		double max = x[0];
		for( int i=1; i<x.length; i++ ) {
		    if( x[i] > max ) max = x[i];
		}
		return max;
    }

    /** Converts a statistic into a t-statistic.
     * @param x The statistic to convert.
     * @param mean Sample mean.
     * @param var Sample variance.
     * @param n Number of samples.
     */
    public static double xToT( double x, double mean, double var, int n ) {
		return (x - mean) / (Math.sqrt( var ) * Math.sqrt( n ));
    }

    /** Converts a t-statistic into a regular statistic.
     * @param t The t-stat to convert.
     * @param mean Sample mean.
     * @param var Sample variance.
     * @param n Number of samples.
     */
    public static double tToX( double t, double mean, double var, int n ) {
		return (t * Math.sqrt( var ) * Math.sqrt( n )) + mean;
    }

    /** Produces t-quantile values. Ported from JavaScript.
     * Copyright (c) 2000 by Sundar Dorai-Raj
     * @author Sundar Dorai-Raj sdoraira@vt.edu
     * @param p The probability value to use, eg 0.95.
     * @param df The number of degrees of freedom.
     * @param lower True iff the lower quantile is to be produced.
     */
    public static double tQuantile( double p,
				    final int ndf,
				    final boolean lower_tail ) {
		// Algorithm 396: Student's t-quantiles by
		// G.W. Hill CACM 13(10), 619-620, October 1970
		if( p<=0 || p>=1 || ndf<1 ) throw new IllegalArgumentException();
	
		final double eps = 1e-12;
		boolean neg;
	
		if((lower_tail && p > 0.5) || (!lower_tail && p < 0.5)) {
		    neg = false;
		    p = 2 * (lower_tail ? (1 - p) : p);
		}
		else {
		    neg = true;
		    p = 2 * (lower_tail ? p : (1 - p));
		}
		
		double q;
		if(Math.abs(ndf - 2) < eps) {   /* df ~= 2 */
		    q = Math.sqrt(2 / (p * (2 - p)) - 2);
		}
		else if (ndf < 1 + eps) {   /* df ~= 1 */
		    final double prob = p * (Math.PI/2);
		    q = Math.cos(prob)/Math.sin(prob);
		}
		else {      /*-- usual case;  including, e.g.,  df = 1.1 */
		    double a = 1 / (ndf - 0.5);
		    double b = 48 / (a * a);
		    double c = ((20700 * a / b - 98) * a - 16) * a + 96.36;
		    double d = ((94.5 / (b + c) - 3) / b + 1) * Math.sqrt(a * (Math.PI/2)) * ndf;
		    double y = Math.pow(d * p, 2 / ndf);
		    if (y > 0.05 + a) {
			/* Asymptotic inverse expansion about normal */
			final double x = qnorm(0.5 * p);
			y = x * x;
			if (ndf < 5)
			    c += 0.3 * (ndf - 4.5) * (x + 0.6);
			c = (((0.05 * d * x - 5) * x - 7) * x - 2) * x + b + c;
			y = (((((0.4 * y + 6.3) * y + 36) * y + 94.5) / c - y - 3) / b + 1) * x;
			y = a * y * y;
			if (y > 0.002)
			    y = Math.exp(y) - 1;
			else { /* Taylor of    e^y -1 : */
			    y = (0.5 * y + 1) * y;
			}
		    }
		    else {
			y = ((1 / (((ndf + 6) / (ndf * y) - 0.089 * d - 0.822)
				   * (ndf + 2) * 3) + 0.5 / (ndf + 4))
			     * y - 1) * (ndf + 1) / (ndf + 2) + 1 / y;
		    }
		    q = Math.sqrt(ndf * y);
		}
		if(neg) q = -q;
		return q;
    }
    
    /**
     * Ported from JavaScript code by S. Dorai-Raj.    
     * Copyright (c) 2000 by Sundar Dorai-Raj
     * @author Sundar Dorai-Raj, sdoraira@vt.edu
     */
    private static double qnorm( final double p ) {
		// ALGORITHM AS 111, APPL.STATIST., VOL.26, 118-121, 1977.
		// Computes z=invNorm(p)
		final double split=0.42;
		final double a0=  2.50662823884;
		final double a1=-18.61500062529;
		final double a2= 41.39119773534;
		final double a3=-25.44106049637;
		final double b1= -8.47351093090;
		final double b2= 23.08336743743;
		final double b3=-21.06224101826;
		final double b4=  3.13082909833;
		final double c0= -2.78718931138;
		final double c1= -2.29796479134;
		final double c2=  4.85014127135;
		final double c3=  2.32121276858;
		final double d1=  3.54388924762;
		final double d2=  1.63706781897;
		
		final double q=p-0.5;
		double ppnd;
		if( Math.abs( q ) <= split ) {
		    double r = q * q;
		    ppnd = q*(((a3*r+a2)*r+a1)*r+a0)/((((b4*r+b3)*r+b2)*r+b1)*r+1);
		} else {
		    double r = p;
		    if( q>0 ) r = 1-p;
		    if( r>0 ) {
				r = Math.sqrt( -Math.log( r ) );
				ppnd = (((c3*r+c2)*r+c1)*r+c0)/((d2*r+d1)*r+1);
			if( q<0 ) ppnd = -ppnd;
		    } else {
				ppnd=0;
		    }
		}
		return ppnd;
    }
}
