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
import java.util.*;
// event distribution class

public class EventManager implements Runnable{
    Hashtable clients = new Hashtable();
    Vector queue = new Vector(); // queued messages
    Thread athread;
    boolean stopped=false;

    public EventManager(){
        athread = new Thread(this);
        athread.start();
    }

    public synchronized void addClient(EventClient c) {
        clients.put(c,c);
    }

    public synchronized void removeClient(EventClient c) {
        clients.remove(c);
    }

    public synchronized void post(LTSEvent le) {
        queue.addElement(le);
        notifyAll();
    }

    public void stop() {
        stopped=true;
    }

    private synchronized void dopost() {
        while (queue.size()==0) {
            try{wait();} catch (InterruptedException e) {}
        }
        LTSEvent le = (LTSEvent)queue.firstElement();
        Enumeration e = clients.keys();
        while(e.hasMoreElements()) {
            EventClient c = (EventClient)e.nextElement();
            c.ltsAction(le);
        }
        queue.removeElement(le);
    }

    public void run() {
        while(!stopped) {
            dopost();
        }
    }
}