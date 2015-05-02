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


/* MyHash is a speciallized Hashtable for the analyser
*  -- assumes no attempt to input duplicate key
*/

class MyIntHashEntry {
    int key;
    int value;
    MyIntHashEntry next;

    MyIntHashEntry(int l) {
        key =l; value=0; next = null;
    }

    MyIntHashEntry(int l, int v) {
        key =l; value=v; next = null;
    }

 }

public class MyIntHash {

    private MyIntHashEntry [] table;
    private int count =0;

    public MyIntHash(int size) {
        table = new MyIntHashEntry[size];
    }


    public void put(int key) {
        MyIntHashEntry entry = new MyIntHashEntry(key);
        int hash = key % table.length;
        entry.next=table[hash];
        table[hash]=entry;
        ++count;
    }

    public void put(int key, int value) {
    	  int hash = key % table.length;
    	  MyIntHashEntry entry = table[hash];
        while (entry!=null) {
            if (entry.key == key) {entry.value = value; return;};
            entry = entry.next;
        }
        entry = new MyIntHashEntry(key,value);
        entry.next=table[hash];
        table[hash]=entry;
        ++count;
    }

    public boolean containsKey(int key) {
        int hash = key % table.length;
        MyIntHashEntry entry = table[hash];
        while (entry!=null) {
            if (entry.key == key) return true;
            entry = entry.next;
        }
        return false;
    }

    public int get(int key) {
        int hash = key % table.length;
        MyIntHashEntry entry = table[hash];
        while (entry!=null) {
            if (entry.key == key) return entry.value;
            entry = entry.next;
        }
        return -99999;
    }

    public int size() {return count;}

}