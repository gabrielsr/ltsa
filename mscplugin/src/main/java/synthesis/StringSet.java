package synthesis;

import java.util.Set;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import ic.doc.ltsa.common.iface.LTSOutput;

class StringSet implements Set {

    private HashSet S;

    public StringSet() {

        S = new HashSet();
    }

    public StringSet(Set A) {

        S = new HashSet(A);
    }

    public int size() {

        return S.size();
    }

    public boolean contains(Object str) {

        StringIterator I;
        boolean found;
        String s = (String) str;
        if (S.contains(s))
            return true;
        else {
            I = stringIterator();
            found = false;
            while (I.hasNext() && !found) {
                found = I.nextString().equals(s);
            }
            return found;
        }
    }

    public boolean hasSameElements(StringSet A) {

        if (A.size() != size())
            return false;
        Iterator I = A.iterator();
        while (I.hasNext()) {
            if (!contains(I.next()))
                return false;
        }
        return true;
    }

    public boolean equals(Object o) {

        return S.equals(o);
    }

    public int hashCode() {

        return S.hashCode();
    }

    public boolean isEmpty() {

        return S.isEmpty();
    }

    public boolean containsAll(Collection c) {

        return S.containsAll(c);
    }

    public Iterator iterator() {

        return S.iterator();
    }

    public StringIterator stringIterator() {

        return new StringIterator(S.iterator());
    }

    public String getString(String s) {

        StringIterator I;
        boolean found;
        String retVal = "";

        if (S.contains(s))
            return s;
        else {
            I = (StringIterator) iterator();
            found = false;
            while (I.hasNext() && !found) {
                retVal = I.nextString();
                found = retVal.equals(s);
            }
            if (found)
                return retVal;
            else
                return null;
        }
    }

    public void addCopy(String s) {

        if (!S.contains(s))
            S.add(new String(s));
    }

    public boolean add(Object str) {

        String s = (String) str;
        if (!S.contains(s)) {
            S.add(s);
            return true;
        } else
            return false;
    }

    public boolean addAll(Collection c) {

        return S.addAll(c);
    }

    public boolean addAll(StringSet A) {

        Iterator I = A.iterator();
        boolean added = false;
        while (I.hasNext()) {
            if (add((String) I.next()))
                added = true;
        }
        return added;
    }

    public void clear() {

        S.clear();
    }

    public Object[] toArray() {

        return S.toArray();
    }

    //public <T> T[] toArray(T[] a) {
    //   return S.toArray(a);
    //}

    public boolean remove(Object o) {

        return S.remove(o);
    }

    public boolean remove(String r) {

        Iterator I;
        boolean found;
        String aux = "";

        I = S.iterator();
        found = false;
        while (I.hasNext() && !found) {
            aux = (String) I.next();
            found = aux.equals(r);
        }
        if (found) {
            S.remove(aux);
            return true;
        } else
            return false;
    }

    public boolean removeAll(Collection c) {

        return S.removeAll(c);
    }

    public boolean removeAll(StringSet A) {

        Iterator I = A.iterator();
        boolean removed = false;
        while (I.hasNext()) {
            if (remove((String) I.next()))
                removed = true;
        }
        return removed;
    }

    public boolean retainAll(Collection c) {

        return S.retainAll(c);
    }

    public Object clone() {

        return new StringSet((Set) S.clone());
    }

    public StringSet intersection(StringSet A) {

        StringSet retVal = new StringSet();
        Iterator I = A.iterator();
        while (I.hasNext()) {
            String s = (String) I.next();
            if (contains(s))
                retVal.add(s);
        }
        return retVal;
    }

    public void print(LTSOutput o) {

        Iterator I = S.iterator();
        while (I.hasNext()) {
            String s = (String) I.next();
            o.outln(s);
        }
    }

    public Object[] toArray(Object[] a ) {

        return S.toArray(a);
    }

}