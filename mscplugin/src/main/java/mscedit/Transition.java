package mscedit;

import org.jdom.*;

import java.util.*;

public class Transition {

    protected Element o_xml;

    public Transition() {}

    Transition( Element p_elem ) {

		o_xml = p_elem;
    }

    public String getTo() { return o_xml.getChild("to").getText(); }
    public String getFrom() { return o_xml.getChild("from").getText(); }
    public void setTo( String p_to ) { o_xml.getChild("to").setText( p_to ); }
    public void setFrom( String p_from ) { o_xml.getChild("from").setText( p_from ); }

    public List getOutputs() { return o_xml.getChildren( "output" ); }
    public List getInputs() { return o_xml.getChildren( "input" ); }

    public void apply( Visitor v ) {
		v.caseATransition( this );
    }

	public void setWeight(String p_prob) {
		o_xml.setAttribute( "weight" , p_prob );
	}

	public double getWeight() {
		try {
			if ( o_xml.getAttribute( "weight" ) == null ) { return 1; }
			return o_xml.getAttribute("weight").getDoubleValue();
		} catch (DataConversionException e) {
			// problem with weight attribute format - return weight of 1 for safety
			return 1;
		}
	}
}



