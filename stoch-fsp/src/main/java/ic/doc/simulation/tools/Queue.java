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

public class Queue {
  private int pop = 0 ;
  private Measure responseTimeMeasure = new Measure() ;
  private SystemMeasure popMeasure = new SystemMeasure() ;
  private List q = new List() ;

  public int queueLength() {
    return pop ;
  }

  public void enqueue( Object o ) {
    pop++ ;
    popMeasure.update( (float)pop ) ;
    q.insertAtBack( new QueueEntry( o ) ) ;
  }

  public Object dequeue() {
    if ( pop == 0 ) System.out.println( "ERROR: Attempt to dequeue an empty Queue!" ) ;
    pop-- ;
    popMeasure.update( (float)pop ) ;
    QueueEntry e = (QueueEntry) q.removeFromFront() ;
    responseTimeMeasure.add( Time.vtime - e.entryTime ) ;
    return e.entry ;
  }

  public Object front() {
    return ( (QueueEntry) q.first() ).entry ;
  }

  public boolean isEmpty() {
    return ( pop == 0 ) ;
  }

  public double meanQueueLength() {
    return popMeasure.mean() ;
  }

  public double varQueueLength() {
    return popMeasure.variance() ;
  }

  public double meanTimeInQueue() {
    return responseTimeMeasure.mean() ;
  }

  public void reset() {
    responseTimeMeasure.reset() ;
    popMeasure.reset() ;
  }

  class QueueEntry {
    double entryTime ;
    Object entry ;

    public QueueEntry( Object o ) {
      entryTime = Time.vtime ;
      entry = o ;
    }
  }

}




