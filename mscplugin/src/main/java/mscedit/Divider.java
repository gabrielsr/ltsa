package mscedit;

import org.jdom.*;

public class Divider implements BMSCComponent {

    private Element o_xml;

    Divider( Element p_xml ) { o_xml = p_xml; }

    public int getTimeIndex() { return Integer.parseInt( o_xml.getAttribute("timeindex").getValue() ); }
    
    public void setTimeIndex( int p_timeindex ) { o_xml.setAttribute("timeindex" , String.valueOf( p_timeindex ) ); }

    public void apply( Visitor v ) {
       	v.caseADivider( this );
    }
}

