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

import ic.doc.ltsa.common.iface.IAlphabet;
import ic.doc.ltsa.common.iface.ICompactState;
import ic.doc.ltsa.common.iface.LTSOutput;

import java.util.*;

public class Alphabet implements IAlphabet {

    PrefixTree root = null;
    String[] myAlpha;
    ICompactState sm;
    private int maxLevel = 0;

    public Alphabet(ICompactState sm) {
        this.sm = sm;
        myAlpha = new String[sm.getAlphabet().length];
        for (int  i = 0; i<sm.getAlphabet().length; ++i)
            myAlpha[i] = sm.getAlphabet()[i];
        sort(myAlpha,1);
        for (int  i = 1; i<myAlpha.length; ++i)
            root = PrefixTree.addName(root,myAlpha[i]);
        if (root!=null) maxLevel = root.maxDepth();
    }
    
    public Alphabet(String[] inames) {
	  String names[] = new String[inames.length];
	  for (int  i = 0; i<names.length; ++i)
	       names[i] = inames[i];
      if (names.length>1) sort(names,0);
      for (int  i = 0; i<names.length; ++i)
            root = PrefixTree.addName(root,names[i]);
    }
    
    public Alphabet(Vector names) {
      this((String[])names.toArray(new String[names.size()]));
    }
    
    public void setMaxLevel(int maxLevel) {

        this.maxLevel = maxLevel;
    }

    public int getMaxLevel() {

        return maxLevel;
    }

    public String toString() {
      if (root==null)
        return "{}";
      else
        return root.toString();
    }
    
    public void print(LTSOutput output, int level) {
       output.outln("Process:\n\t"+sm.getName());
       output.outln("Alphabet:");
       if (root==null) {
            output.outln("\t{}");
            return;
       }
       if (level==0) {
          output.outln("\t"+root.toString());
       } else {
          output.out("\t{ ");
          Vector v = new Vector();
          root.getStrings(v,level-1,null);
          Enumeration e = v.elements();
          boolean first = true;
          while(e.hasMoreElements()){
            String s = (String)e.nextElement();
            if(!first) output.out("\t  ");
            if (e.hasMoreElements())
                output.outln(s+",");
            else
                output.outln(s);
            first=false;
          }
          output.outln("\t}");
       }
    }

    private void sort(String a[], int from) { //simple shell sort
        int min;
        for (int i = from; i<a.length-1; i++) {
            min = i;
            for (int j = i+1; j<a.length; j++) {
                if (a[j].compareTo(a[min])<0) min = j;
            }
            //swap
            String temp = a[i]; a[i]=a[min]; a[min]=temp;
        }
    }
}

/**
* class PrefixTree - list of subtrees
*/

class PrefixTree {
    String name;
    int value;
    boolean isInt = false;
    PrefixTree subname = null;
    PrefixTree list = null;
    boolean lastprefix = false;

    PrefixTree(String n) {
        name = n;
        checkInt();
    }

    static  PrefixTree addName(PrefixTree pt, String s) {
        if (pt==null) {
            pt = new PrefixTree(prefix(s,0));
        }
        pt.add(s,0);
        return pt;
    }

    private void add(String s, int level) {
        String ps = prefix(s,level);
        if (ps==null) return;
        if  (ps.equals(name) && !lastprefix) {
            String pps = prefix(s,level+1);
            if (pps==null) {lastprefix=true; return;}
            if (subname==null) subname = new PrefixTree(pps);
            subname.add(s,level+1);
        } else {
            if (list==null) list = new PrefixTree(ps);
            list.add(s,level);
        }
        return;
    }

    public static boolean equals(PrefixTree one, PrefixTree two) {
        if (one==two) return true;
        if (one == null || two ==null) return false;
        if (!one.name.equals(two.name)) return false;
        return equals(one.subname,two.subname)
            && equals(one.list,two.list);
    }

    // get sub lists of names with the same suffix
    PrefixTree[] getSubLists() {
        Vector subs = new Vector();
        PrefixTree pt= this;
        PrefixTree ptt = list;
        subs.addElement(pt);
        while (ptt!=null) {
            if (!equals(pt.subname,ptt.subname) || pt.isInt!=ptt.isInt) {
                subs.addElement(ptt);
                pt=ptt;
            }
            ptt=ptt.list;
        }
        subs.addElement(null); //sentinel
        PrefixTree[] sl = new PrefixTree[subs.size()];
        subs.copyInto(sl);
        return sl;
    }

    void checkInt(){
        try{
            value = Integer.parseInt(name);
            isInt =true;
        } catch (NumberFormatException e){}
    }


    static String prefix(String s, int level) {
        int start=0;
        for (int i = 0; i<level ; i++) {
            start = s.indexOf('.',start);
            if (start<0) return null;
            ++start;
        }
        int finish = s.indexOf('.',start);
        if (finish<0)
           return s.substring(start);
        else
           return s.substring(start,finish);
    }

    public void getStrings(Vector v, int level, String prefix) {
        PrefixTree pt = this;
        while (pt!=null) {
            String pre;
            if (prefix == null)
                pre = pt.item();
            else
                pre = prefix + dotted(pt.item());
            if (pt.subname==null)
                v.addElement(pre);
            else if (level>0)
                pt.subname.getStrings(v,level-1,pre);
            else
                v.addElement(pre+dotted(pt.subname.toString()));
            pt = pt.list;
        }
    }

    public int maxDepth() {
        PrefixTree pt = this;
        int max = 0;
        while(pt!=null) {
            if (pt.subname==null)
                max = Math.max(max,1);
            else
                max = Math.max(1+pt.subname.maxDepth(),max);
            pt = pt.list;
        }
        return max;
    }

    //routines to stringify PrefixTree

    public String toString() {
        PrefixTree[] subs = getSubLists();
        String s;
        if (subs.length>2) s="{"; else s="";
        for (int i=0; i<subs.length-1; ++i) {
            if (i<subs.length-2)
                s = s+listString(subs[i],subs[i+1])+", ";
            else
                s = s+listString(subs[i],subs[i+1]);
        }
        if (subs.length>2) return s+"}"; else return s;
    }

    static String listString(PrefixTree start, PrefixTree end) {
        String s;
        if (start.list == end) {
            s = start.item();
        } else {
            if (intRange(start,end))
                s = rangeString(start,end);
            else {
                s = "{"+start.item();
                PrefixTree pt = start.list;
                while(pt!=end) {
                    s = s+", "+pt.item();
                    pt = pt.list;
                }
                s = s+"}";
            }
        }
        if (start.subname!=null)
            return s+dotted(start.subname.toString());
        else
            return s;
    }

    static private String dotted(String suffix) { //decide whether dot needed
        if (suffix.charAt(0) == '[')
                return suffix;
            else
                return "."+suffix;
    }


    String item() {
        if (isInt)
              return "["+name+"]";
        else
              return  name;
    }



    static boolean intRange(PrefixTree start, PrefixTree end) {
        PrefixTree pt = start;
        while(pt!=end) {
            if (!pt.isInt) return false;
            pt = pt.list;
        }
        return true;
    }

    static String rangeString(PrefixTree start, PrefixTree end) {
        PrefixTree pt = start; int n =0;
        while(pt!=end) {pt=pt.list; ++n;}
        int a[] = new int[n];
        pt = start;
        for (int i=0;i<a.length;++i) {a[i] = pt.value; pt=pt.list;}
        sort(a);
        if (isOneRange(a)) {
            return "["+a[0]+".."+ a[a.length-1]+"]";
        } else {
            int j = 0;
            String s = "{";
            while(j<a.length) {
               int i;
               for (i=j; i<a.length-1 && a[i+1]-a[i]==1; ++i);
               if (i==j)
                  s =s + "["+a[j]+"]";
               else
                  s =s + "["+a[j]+".."+ a[i]+"]";
               j=i+1;
               if (j<a.length) s = s+ ", ";
            }
            s = s+ "}";
            return s;
        }
    }

    static private boolean isOneRange(int a[]) {
        for (int i=0; i<a.length-1; ++i) {
            if ( a[i+1]-a[i]!=1 ) return false;
        }
        return true;
    }

    static private void sort(int a[]) {  //simple shell sort
        int min;
        for (int i = 0; i<a.length-1; i++) {
            min = i;
            for (int j = i+1; j<a.length; j++) {
                if (a[j]<a[min]) min = j;
            }
            //swap
            int temp = a[i]; a[i]=a[min]; a[min]=temp;
        }
    }

}