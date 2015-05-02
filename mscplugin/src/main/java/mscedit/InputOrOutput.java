package mscedit;

import org.jdom.*;

public abstract class InputOrOutput {
 
    protected Element o_xml;

    public abstract void apply( Visitor v );
    public int getTimeIndex() {
	return Integer.parseInt( o_xml.getAttribute( "timeindex" ).getValue() );
    }

    public void setTimeIndex( int p_ti ) {
	o_xml.setAttribute( "timeindex" , String.valueOf( p_ti ) ); 
    }

    public String getTo() {
	return o_xml.getChild("to").getText();
    }

    public void setTo( String p_to ) {

	o_xml.removeChild("to");
	o_xml.addContent( new Element("to").setText( p_to ) );
    }

    public String getFrom() {
	return o_xml.getChild("from").getText();
    }

    public void setFrom( String p_from ) {

	o_xml.removeChild("from");
	o_xml.addContent( new Element("from").setText( p_from ) );
    }

    public String getName() {
	return o_xml.getChild("name").getText();
    }

    public void setName( String p_to ) {

	o_xml.removeChild("name");
	o_xml.addContent( new Element("name").setText( p_to ) );
    }

    public String getId() {
	return getName() + getTimeIndex();
    }

    public boolean isNegative() {

	if ( o_xml.getAttribute( "negative" ) != null ) {
	    String x_neg = o_xml.getAttribute( "negative" ).getValue();
	    if ( x_neg != null && x_neg.equals( "true" ) ) { return true; }
	}
	return false;
    }

    public void setNegative( boolean p_neg ) {

	if ( p_neg ) {

	    o_xml.setAttribute( "negative" , "true" );

	} else {

	    o_xml.removeAttribute( "negative" );
	}
    }
    
}

