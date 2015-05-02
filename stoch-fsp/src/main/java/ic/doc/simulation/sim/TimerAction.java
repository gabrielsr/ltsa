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
 * Objects of this class represent {@link Timer} update actions, that
 * is, actions in a measurement process which start and stop timer
 * measures. The action can be one of two types: <code>START</code>
 * and <code>STOP</code>, representing starting and stopping a timer
 * respectively. <code>START</code> actions call {@link Timer#start(int)}
 * and <code>STOP</code> actions call {@link Timer#stop(int)}.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 * @see Timer
 */
public class TimerAction
    extends MeasureAction
    implements Cloneable {

    /** Constant defining the <code>START</code> action type. */
    public static final int START = 0;
    /** Constant defining the <code>STOP</code> action type. */
    public static final int STOP = 1;

    private final int action;
    private final int queue;

    /**
     * Creates a new timer action to operate on the named
     * {@link Timer} in the given way.
     * @param identifier The identifier of the timer to operate on.
     * @param action The action to perform - must be either
     * {@link #START} or {@link #STOP}.
     * @param queue The sample queue to use.
     */
    public TimerAction( int identifier, int action, int queue ) {
		super( identifier );
		if( action != START && action != STOP )
		    throw new RuntimeException( "Bad action type " + action );
		this.action = action;
		this.queue = queue;
    }
    
    public void execute( SimulationState state ) {
		Timer t = (Timer) state.getMeasure( getIdentifier() );
		if( action == START ) t.start( queue );
		else t.stop( queue );
    }

    public int getType() { return action; }

    public Action cloneAction() {
		try {
		    return (Action) super.clone();
		} catch( CloneNotSupportedException e ) {
		    throw new RuntimeException( e.getMessage() );
		}
    }

    public boolean equals( Object o ) {
		if( o instanceof TimerAction ) {
		    TimerAction t = (TimerAction) o;
		    return t.action == this.action 
			&& t.queue == this.queue
			&& t.getIdentifier() == this.getIdentifier();
		} else return false;
    }

    public int hashCode() {
		return action ^ queue ^ getIdentifier();
    }

/*
    public String prettyPrint( StochasticAutomata m ) {
		return getMeasureName(m) + "(" + queue + ")"
		    + "." + (action==START?"start":"stop");
    }
*/

    public String toString() {
		return( getIdentifier() + "(" + queue + ")"
			+ "." + (action==START?"start":"stop") );
    }

}
