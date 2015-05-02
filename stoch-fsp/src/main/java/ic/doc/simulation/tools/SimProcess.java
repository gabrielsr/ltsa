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

package ic.doc.simulation.tools;

public abstract class SimProcess {
  static ThreadGroup allThreads = new ThreadGroup( "Processes" ) ;
  Semaphore processSem = new Semaphore() ;
  protected double creationTime ;
  double wakeTime ;
  boolean isActive = false ;
  SystemMeasure utilisationMeasure = new SystemMeasure() ;

  public SimProcess() {
    creationTime = Time.vtime ;
    ( new SimThread() ).start() ;
  }

  public abstract void runProcess() throws InterruptedException ;

  class SimThread extends Thread {
    public SimThread() {
      super( allThreads, "SimProcess" ) ;
    }
    public void run() {
      try {
        processSem.down() ;
        runProcess() ;
        die() ;
      }
      catch ( InterruptedException e ) {
      }
    }
  }

  public void hold( double t ) throws InterruptedException {
    wakeTime = Time.vtime + t ;
    PSim.procList.insert( this ) ;
    waitToBeWoken() ;
  }

  public void passivate() throws InterruptedException {
    if ( !isActive ) System.out.println( "ERROR: Attempt to passivate a passive SimProcess!" ) ;
    isActive = false ;
    utilisationMeasure.update( 0.0 ) ;
    waitToBeWoken() ;
  }

  public boolean isActive() {
    return this.isActive ;
  }

// Note: you can ONLY activate a passivated process...
// Sleeping processes are active

  public void activate() {
    if ( isActive ) System.out.println( "ERROR: Attempt to activate an active SimProcess!" ) ;
    this.isActive = true ;
    utilisationMeasure.update( 1.0 ) ;
    wakeTime = Time.vtime ;
    PSim.procList.insert( this ) ;
  }

  public double utilisation() {
    return utilisationMeasure.mean() ;
  }

  void waitToBeWoken() throws InterruptedException {
    PSim.managerSem.up() ;
    processSem.down() ;
  }

  void die() throws InterruptedException {
    PSim.managerSem.up() ;
  }
}

