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

import ic.doc.ltsa.lts.StochasticAutomata;
import ic.doc.ltsa.common.iface.LTSOutput;

import ic.doc.simulation.tools.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map;
import java.util.Hashtable;

import java.lang.reflect.Constructor;

/**
 * This class encapsulates a simulation run on a process.
 *
 * @author Thomas Ayles
 * @version $Revision: 1.1 $ $Date: 2005/02/14 14:09:30 $
 */
public class Simulation
    implements SimulationState {

    private static final int N_PROGRESS_EVENTS = 100;

    private SimulationOptions options;
    private StochasticAutomata process;
    private Thread simThread;
    private String[] alphabet;
    private Sim sim;
    private Map timers, actionCounters, populationCounters;
    private PerformanceMeasure[] measures;
    private Collection progressListeners;
    
    private boolean abort = false;

    /**
     * Creates a new simulation context for the given state machine.
     * @param automata The state machine to simulate.
     */
    public Simulation( StochasticAutomata automata, LTSOutput output ) {
		this.process = automata;
		alphabet = process.getAlphabet();
		options = new SimulationOptions();
		progressListeners = new Vector();
	
		sim = new Sim() {
			public boolean stop() throws SimulationAbortionException {
				if (abort) throw new SimulationAbortionException();
			    return now() > getOptions().getRunLength();
			}
		    };
		initMeasures();
    }

    /** Returns a result set from a completed simulation run.
     */
    public ResultSet getResultSet() {
		ResultSet rs = new ResultSet();
	
		for( Iterator i = getPerformanceMeasures().iterator(); i.hasNext(); ) {
		    PerformanceMeasure m = (PerformanceMeasure) i.next();
		    rs.addResult( m.getName(), m.getResult() );
		}
	
		return rs;
    }

    /**initialises performance measures in this simulation context. */
    private void initMeasures() {
		timers = new Hashtable();
		actionCounters = new Hashtable();
		populationCounters = new Hashtable();
	
		String[] mNames = process.getMeasureNames();
		Class[] mTypes = process.getMeasureTypes();
	
		if( mNames != null ) {
		    measures = new PerformanceMeasure[mNames.length];
		    for( int i=0; i<mNames.length; i++ ) {
				measures[i] = instantiate( mNames[i], mTypes[i] );
		    }
		}
    }

    private PerformanceMeasure instantiate( String name, Class type ) {
		Constructor c = null;
	
		// try the single argument form
		try {
		    Class[] p = { String.class };
		    c = type.getConstructor( p );
		    Object[] args = { name };
		    return (PerformanceMeasure) c.newInstance( args );
		}
		catch( NoSuchMethodException nsme ) {}
		catch( Exception e ) {
		    e.printStackTrace();
		    throw new RuntimeException( e.getMessage() ); 
		}
	
		// didn't work, try two argument form
		try {
		    Class[] p = { String.class, Sim.class };
		    c = type.getConstructor( p );
		    Object[] args = { name, sim };
		    return (PerformanceMeasure) c.newInstance( args );
		}
		catch( NoSuchMethodException nsme ) {}
		catch( Exception e ) {
		    e.printStackTrace();
		    throw new RuntimeException( e.getMessage() );
		}
	
		//didn't work, fail
		String msg = "no constructor for measure " + name + " of type " + type;
		throw new RuntimeException( msg );
    }

    /** protected null constructor (does very little) */
    protected Simulation() {
		progressListeners = new Vector();
    }

    /** Adds a listener to be informed of the simulation's progress.
     * @l The listener.
     */
    public void addProgressListener( ProgressListener l ) {
		if( l != null ) progressListeners.add( l );
    }

    /** Sets options for the simulation run.
     * @param opt The options.
     */
    public void setOptions( SimulationOptions opt ) {
		options = opt;
    }

    /** Returns the options for the simulation run.
     */
    public SimulationOptions getOptions() {
		return options;
    }

    private boolean prepared = false;

    /**
     * Must be called before the first run on a simulation.
     */
    public void prepare() {
		if( measures != null ) {
		    for( int i=0; i<measures.length; i++ ) {
			try {
			    ((ChartablePerformanceMeasure) measures[i]).setMovingAverage( options.isMovingAverage() );
			} catch( ClassCastException e ) {}
		    }
		}
		reset();
		prepared = true;
    }

    /**
     * Carries out the execution of the simulation. Calling this
     * method initialises the state and clock vectors.
     */
    public void run() throws SimulationAbortionException {
		if (!prepared) prepare();
		sim.execute();
    }
    
    public void abort() {
    	abort = true;
    }

    /**
     * Resets the simulation to its initial state. When called after
     * running a simulation, it resets the simulations state to how it
     * was before {@link #run()} was called.
     */
    public void reset() {
		initClocks();
		setState( process.START() );
		sim.reset();
		resetMeasures();
	
		// schedule initial event
		new SimulationEvent( sim, 0, process, this, getOptions().getZenoThreshold() );
		scheduleSampleEvents();
		scheduleProgressEvents();

		// only reset timers if requested
		if (getOptions().getTransientResetTime()>0)
			new MeasurementResetEvent( sim, getOptions().getTransientResetTime() );
    }

    /**
     * Performs a hard reset on all performance measures.
     */
    private void resetMeasures() {
		for( Iterator i = getPerformanceMeasures().iterator();
		     i.hasNext(); ) {
		    ((PerformanceMeasure) i.next()).hardReset();
		}
    }

    private void scheduleSampleEvents() {
		double delta = getOptions().getRunLength() / getOptions().getNumberOfSamplePoints();
		for( int i=1; i<=getOptions().getNumberOfSamplePoints(); i++ ) {
		    new SampleEvent( sim, i*delta );
		}
    }

    private void scheduleProgressEvents() {
		double delta = getOptions().getRunLength() / N_PROGRESS_EVENTS;
		for( int i=1; i<=N_PROGRESS_EVENTS; i++ ) {
		    new ProgressEvent( sim, i*delta, i );
		}
    }

    private class ProgressEvent extends Event {
		private final int pct;
		public ProgressEvent( Sim s, double t, int pct ) {
		    super( s, t );
		    this.pct = pct;
		}
		public void call() {
		    percentComplete( pct );
		}
    }

    private class MeasurementResetEvent	extends Event {
		public MeasurementResetEvent( Sim s, double t ) {
		    super( s, t );
		}
		public void call() {
		    for( Iterator i = getPerformanceMeasures().iterator(); i.hasNext(); ) {
		    	// jew01: now calls special function transientReset() to ensure smooth transition
				((PerformanceMeasure) i.next()).transientReset();
		    }
		}
    }

    /** Event to sample the current values of all chartable measures
     * in the simulation.
     */
    private class SampleEvent extends Event {
		public SampleEvent( Sim s, double t ) {
		    super( s, t );
		}
		public void call() {
		    for( Iterator i=getPerformanceMeasures().iterator(); i.hasNext(); ) {
				try {
				    ChartablePerformanceMeasure m = (ChartablePerformanceMeasure) i.next();
				    m.addSample();
				} catch( ClassCastException e ) {}
		    }
		}
    }

    /** Informs all listeners of a change in the progress of the
     * simulation.
     * @param p The percentage of the simulation completed.
     */
    protected void percentComplete( int p ) {
		for( Iterator i = progressListeners.iterator(); i.hasNext(); ) {
		    ((ProgressListener) i.next()).percentComplete( p );
		}
    }

    /** initialises clocks */
    private void initClocks() {
		clocks = new double[process.getMaxClockIdentifier()+1];
		clockRunning = new boolean[process.getMaxClockIdentifier()+1];
		for( int i = 0; i <= process.getMaxClockIdentifier(); i++ ) {
		    clocks[i] = 0;
		    clockRunning[i] = true;
		}
    }


    /************** SimulationState interface implementation *************/
    private double[] clocks;
    private boolean[] clockRunning;
    private byte[] state;

    public int numClocks() {
		return clocks.length;
    }

    public double getClock( int c ) {
		return clocks[c];
    }

    public void setClock( int c, double v ) {
		clocks[c] = v>0 ? v : 0;
    }

    public void holdClock( int c ) {
		clockRunning[c] = false;
    }

    public void resumeClock( int c ) {
		clockRunning[c] = true;
    }

    public boolean clockRunning( int c ) {
		return clockRunning[c];
    }

    public byte[] getState() {
		return state;
    }

    public void setState( byte[] s ) {
		state = s;
    }

    public void advanceClocks( double t ) {
		for( int i = 0; i < clocks.length; i++ ) {
		    if( clockRunning[i] )
			clocks[i] = (clocks[i] - t) > 0 ? clocks[i] - t : 0;
		}
    }

    public PerformanceMeasure getMeasure( int id ) {
		return measures[id];
    }

    public Collection getPerformanceMeasures() {
		Vector v = new Vector();
		if( measures != null ) {
		    for( int i=0; i<measures.length; i++ ) {
				v.add( measures[i] );
		    }
		}
		return v;
    }
}
