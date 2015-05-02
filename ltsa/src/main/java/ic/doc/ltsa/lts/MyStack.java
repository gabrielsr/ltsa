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

/*
* Simple low overhead Stack data type used by analyser
*/

class StackEntries {
	final static int N = 1024;
	byte[] val[] = new byte[N][];
	boolean[] marks = new boolean[N];
	int index;
	StackEntries next;
	
	StackEntries(StackEntries se) {
		 index=0;
		 next = se;
	}
	
	boolean empty() {return index==0;}
	
	boolean full()  {return index==N;}
	
  void push(byte[] o) {
  	 val[index] = o;
  	 marks[index]=false;
  	 ++index;
  }
  
  byte[] pop() {
  	--index;
  	return val[index];
  }
  
  byte[] peek() { return val[index-1]; }
  void mark() { marks[index-1] = true;}
  boolean marked() {return marks[index-1];}
		 
}

class MyStack {
	
	protected StackEntries head = null;
	protected int depth = 0;
	
	boolean empty() { return head==null;}
	
	void push(byte[] o) {
		 if (head ==null) {
		 	head = new StackEntries(null);
		 } else if (head.full()) {
		 	head = new StackEntries(head);
		 }
		 head.push(o);
		 ++depth;
	}
	
	byte[] pop() {
		byte[] t = head.pop();
		--depth;
		if (head.empty())head = head.next;
		return t;
	}
	
	byte[] peek() {return head.peek();}
	
  void mark() {head.mark(); }
	
	boolean marked(){return head.marked();}

	int getDepth() {return depth;}
	
}