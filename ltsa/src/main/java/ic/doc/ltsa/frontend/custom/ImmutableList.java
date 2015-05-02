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

package ic.doc.ltsa.frontend.custom;
import java.util.*;

public class ImmutableList {
  ImmutableList next;
  Object item;

  private ImmutableList(ImmutableList next, Object item) {
    this.next = next; this.item=item;
  }

  public static ImmutableList add(ImmutableList list, Object item) {
    return new ImmutableList(list, item);
  }

  public static ImmutableList remove(ImmutableList list, Object target) {
    if (list == null) return null;
    return list.remove(target);
  }

  private ImmutableList remove(Object target) {
    if (item == target) {
      return next;
    } else {
      ImmutableList new_next = remove(next,target);
      if (new_next == next ) return this;
      return new ImmutableList(new_next,item);
    }
  }

  public static Enumeration elements(ImmutableList list) {
        return new ImmutableListEnumerator(list);
  }
}

final class ImmutableListEnumerator implements Enumeration {

    private ImmutableList current;

    ImmutableListEnumerator(ImmutableList l){current=l;};

    public boolean hasMoreElements() {return current != null;}

    public Object nextElement() {
      if (current!=null) {
        Object o = current.item;
        current = current.next;
        return o;
      }
      throw new NoSuchElementException("ImmutableListEnumerator");
    }
}