package ic.doc.ltsa.lts;

import ic.doc.ltsa.common.infra.Relation;
import ic.doc.ltsa.common.iface.IActionLabels;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

class CompositeBody {
    // single process reference P
    ProcessRef  singleton;
    // list of CompositeBodies ( P || Q)
    Vector procRefs;
    // conditional if Bexp then P else Q
    Stack boolexpr;
    CompositeBody thenpart; //overloaded as body of replicator
    CompositeBody elsepart;
    // forall[i:R][j:S].
    IActionLabels range;     // used to store forall range/ranges
    // the following are only  applied to singletons & procRefs (...)
    IActionLabels prefix;     // a:
    IActionLabels accessSet;  // S::
    Vector relabelDefns;     // list of relabelling defns

    private Vector accessors = null;
    private Relation relabels = null;

    void compose(CompositionExpression c, Vector machines, Hashtable locals,boolean probabilisticSystem) {
        Vector accessors  = accessSet==null? null : accessSet.getActions(locals,c.constants);
        Relation relabels = RelabelDefn.getRelabels(relabelDefns,c.constants,locals);
        //conditional compostion
        if (boolexpr!=null) {
            if (Expression.evaluate(boolexpr,locals,c.constants)!=0)
                thenpart.compose(c,machines,locals,probabilisticSystem);
            else if (elsepart !=null)
               elsepart.compose(c,machines,locals,probabilisticSystem);
        } else if (range!=null) {
        //replicated composition
            range.initContext(locals,c.constants);
            while(range.hasMoreNames()) {
                range.nextName();
                thenpart.compose(c,machines,locals,probabilisticSystem);
            }
            range.clearContext();
        } else {
        //singleton or list
            Vector tempMachines = getPrefixedMachines(c,locals,probabilisticSystem);
            // apply accessors
            if (accessors!=null)
                for(Enumeration e = tempMachines.elements(); e.hasMoreElements();) {
                    Object o = e.nextElement();
                    if (o instanceof CompactState) {
                        CompactState mach = (CompactState)o;
                        mach.addAccess(accessors);
                    } else {
                        CompositeState cs = (CompositeState)o;
                        cs.addAccess(accessors);
                    }
                }
            // apply relabels
            if (relabels!=null)
                for(int i = 0; i<tempMachines.size(); ++i) {
                    Object o = tempMachines.elementAt(i);
                    if (o instanceof CompactState) {
                        CompactState mach = (CompactState)o;
                        mach.relabel(relabels);
                    } else {
                        CompositeState cs = (CompositeState)o;
                        CompactState mm = cs.relabel(relabels,c.output);
                        if (mm!=null) tempMachines.setElementAt(mm,i);
                    }
                }
            // add tempMachines to machines
            for(Enumeration e = tempMachines.elements(); e.hasMoreElements();) {
                    machines.addElement(e.nextElement());
            }
        }
    }

    private Vector getPrefixedMachines(CompositionExpression c, Hashtable locals,boolean probabilisticSystem) {
        if (prefix==null) {
            return getMachines(c,locals,probabilisticSystem);
        } else {
            Vector pvm = new Vector();
            prefix.initContext(locals,c.constants);
            while (prefix.hasMoreNames()) {
                String px = prefix.nextName();
                Vector vm = getMachines(c,locals,probabilisticSystem);
                for(Enumeration e = vm.elements(); e.hasMoreElements();) {
                    Object o = e.nextElement();
                    if (o instanceof CompactState) {
                        CompactState m = (CompactState)o;
                        m.prefixLabels(px);
                        pvm.addElement(m);
                    } else {
                        CompositeState cs = (CompositeState) o;
                        cs.prefixLabels(px);
                        pvm.addElement(cs);
                    }
                }
            }
            prefix.clearContext();
            return pvm;
         }
    }

    private Vector getMachines(CompositionExpression c, Hashtable locals, boolean probabilisticSystem) {
        Vector vm = new Vector();
        if (singleton!=null) {
            singleton.instantiate(c,vm,c.output,locals,probabilisticSystem);
        } else if (procRefs!=null) {
            Enumeration e = procRefs.elements();
            while(e.hasMoreElements()){
                CompositeBody cb = (CompositeBody)e.nextElement();
                cb.compose(c,vm,locals,probabilisticSystem);
            }
        }
        return vm;
    }

}
