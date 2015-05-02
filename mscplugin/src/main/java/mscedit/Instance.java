package mscedit;

import org.jdom.*;
import java.util.*;

public class Instance {

    private Element o_xml;

    private static Comparator s_sorter = new Comparator() {

	    public int compare(Object p_a, Object p_b) {
		
		InputOrOutput a = (InputOrOutput)p_a;
		InputOrOutput b = (InputOrOutput)p_b;
		
		if ( a.getTimeIndex() < b.getTimeIndex() ) return -1 ;
		if ( a.getTimeIndex() > b.getTimeIndex() ) return 1;
		return 0;
	    }

	    public boolean equals( Object p_obj ) { return false; }
	};

    public Instance() {};

    Instance( Element p_xml ) { o_xml = p_xml; }

    public String getName() { return o_xml.getAttribute("name").getValue(); }
    public void setName( String p_name ) { o_xml.setAttribute("name" , p_name ); }
 
    public void add( Element p_elem ) { o_xml.addContent( p_elem ); }
    public void remove( Element p_elem ) { o_xml.removeContent( p_elem ); }

    public void remove( InputOrOutput p_ioo ) {

	List x_xml_outs = o_xml.getChildren("output");
	for ( Iterator i = x_xml_outs.iterator() ; i.hasNext() ; ) {
	    Element x_elem = (Element)i.next();
	    if ( p_ioo.getId().equals( new Output( x_elem ).getId() ) ) {
		i.remove();
	    }
	}

	List x_xml_ins = o_xml.getChildren("input");
	for ( Iterator i = x_xml_ins.iterator() ; i.hasNext() ; ) {
	    Element x_elem = (Element)i.next();
	    if ( p_ioo.getId().equals( new Input( x_elem ).getId() ) ) {
		i.remove();
	    }
	}
    }

    public List getOutputs() {
	
	List x_outs = new ArrayList();
	List x_xml_outs = o_xml.getChildren("output");
	for ( Iterator i = x_xml_outs.iterator() ; i.hasNext() ; ) {
	    x_outs.add( new Output( (Element)i.next() ) );
	}

	Collections.sort( x_outs , s_sorter );

	return x_outs;
    }
    
    public List getInputs() {
	
	List x_outs = new ArrayList();
	List x_xml_outs = o_xml.getChildren("input");
	for ( Iterator i = x_xml_outs.iterator() ; i.hasNext() ; ) {
	    x_outs.add( new Input( (Element)i.next() ) );
	}

	Collections.sort( x_outs , s_sorter );

	return x_outs;
    } 

     public List getInputsAndOutputs() {
	
	 List x_ins_outs = getInputs();
	 x_ins_outs.addAll( getOutputs() );

	 Collections.sort( x_ins_outs , s_sorter );

	 return x_ins_outs;
     } 
    
    public void apply( Visitor v ) {
	v.caseAInstance( this );
    }
}

