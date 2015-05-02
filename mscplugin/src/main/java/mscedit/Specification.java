package mscedit;

import org.jdom.*;
import java.util.*;

public class Specification {

    private Document o_doc;

    public Specification() {

        o_doc = new Document(new Element("specification"));
    }

    public Specification(Document p_doc) {

        o_doc = p_doc;
    }

    public HMSC getHMSC() {

        return new HMSC(o_doc.getRootElement().getChild("hmsc"));
    }

    public List getBMSCs() {

        List x_elems = o_doc.getRootElement().getChildren("bmsc");
        List x_bmscs = new Vector();

        for (Iterator i = x_elems.iterator(); i.hasNext();) {

            x_bmscs.add(new BMSC((Element) i.next()));
        }

        return x_bmscs;
    }

    public void deleteBMSC(String p_name) {

        List x_elems = o_doc.getRootElement().getChildren("bmsc");

        for (Iterator i = x_elems.iterator(); i.hasNext();) {

            if (((Element) i.next()).getAttribute("name").getValue().equals(p_name)) {
                i.remove();
            }
        }
    }

    public void renameBMSC(String p_oldname, String p_newname) {

        List x_elems = o_doc.getRootElement().getChildren("bmsc");

        for (Iterator i = x_elems.iterator(); i.hasNext();) {

            Element x_elem = (Element) i.next();

            if (x_elem.getAttribute("name").getValue().equals(p_oldname)) {
                x_elem.getAttribute("name").setValue(p_newname);
            }
        }
    }

    public List getPositiveBMSCs() {

        List x_all_bmscs = getBMSCs();

        for (Iterator i = x_all_bmscs.iterator(); i.hasNext();) {

            BMSC x_bmsc = (BMSC) i.next();
            if (x_bmsc.isNegative()) {
                i.remove();
            }
        }

        return x_all_bmscs;
    }

    public List getNegativeBMSCs() {

        List x_all_bmscs = getBMSCs();

        for (Iterator i = x_all_bmscs.iterator(); i.hasNext();) {

            BMSC x_bmsc = (BMSC) i.next();
            if (!x_bmsc.isNegative()) {
                i.remove();
            }
        }

        return x_all_bmscs;
    }

    public List getAllLinks() {

        List x_bmscs = getBMSCs();
        List x_links = new ArrayList();

        for (Iterator i = x_bmscs.iterator(); i.hasNext();) {

            x_links.addAll(((BMSC) i.next()).getLinks());
        }

        return x_links;
    }

    public void addBMSC(BMSC p_bmsc) {

        o_doc.getRootElement().addContent(p_bmsc.asXML());
    }

    public void addHMSC(HMSC p_hmsc) {

        o_doc.getRootElement().addContent(p_hmsc.asXML());
    }

    public Document asXML() {

        return o_doc;
    }

    public void apply(Visitor v) {

        v.caseASpecification(this);
    }
}

