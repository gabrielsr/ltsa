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

import ic.doc.ltsa.common.infra.StackCheck;

/* MyHashProg is a speciallized Hashtable progress analysis
* it includes a queue structure through the hash table entries
*  -- assumes no attempt to input duplicate key
*  
*/

public class MyHashProg implements StackCheck {

    private MyHashProgEntry [] table;
    private int count =0;
    
    public MyHashProg(){
    	  table = new MyHashProgEntry[100001];
    }

    public MyHashProg(int size) {
        table = new MyHashProgEntry[size];
    }

    public void add(byte[] key, MyHashProgEntry parent) {
        MyHashProgEntry entry = new MyHashProgEntry(key, parent);
        //insert in hash table
        int hash = StateCodec.hash(key) % table.length;
        entry.next=table[hash];
        table[hash]=entry;
        ++count;
    }
    
    public MyHashProgEntry get(byte[] key) {
        int hash = StateCodec.hash(key) % table.length;
        MyHashProgEntry entry = table[hash];
        while (entry!=null) {
            if (StateCodec.equals(entry.key,key)) return entry;
            entry = entry.next;
        }
        return null;
    }
    
    public boolean onStack(byte[] key) {
    	  MyHashProgEntry entry = get(key);
    	  if (entry==null) return false;
    	  return (entry.isReturn && !entry.isProcessed);
    }

    public int size() {return count;}

}