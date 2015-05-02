package mscedit;

import org.jdom.*;
import java.util.*;

public class BMSC {

    private Element o_root;

    public BMSC() {}

    public BMSC( String p_name ) {

	o_root = new Element("bmsc").setAttribute("name" , p_name );
    }

    public BMSC( Element p_root ) {

	o_root = p_root;
    }

    public BMSC addInstance( String p_instance ) {

	o_root.addContent( new Element("instance").setAttribute("name" , p_instance ) );
	return this;	  
    }

    public Element getXML() { return o_root; }

    public List getInstances() {

	List x_elems = o_root.getChildren("instance");
	List x_instances = new ArrayList();

	for( Iterator i = x_elems.iterator(); i.hasNext() ; ) {

	    x_instances.add( new Instance( (Element)i.next() ) );
	}

	return x_instances;
    }

    public List getLinks() {
        
	List x_instances = getInstances();
	Vector x_outputs = new Vector();
	Vector x_inputs = new Vector();
	Vector x_links = new Vector();

	for ( Iterator i = x_instances.iterator() ; i.hasNext() ; ) {

	    Instance x_inst = (Instance)i.next();
	    x_outputs.addAll( x_inst.getOutputs() );
	    x_inputs.addAll( x_inst.getInputs() );
	}

	for ( Iterator i = x_outputs.iterator() ; i.hasNext() ; ) {

	    Output x_output = (Output)i.next();
	    String x_out_id = x_output.getId();

	    for ( Iterator j = x_inputs.iterator() ; j.hasNext() ; ) {

		Input x_input = (Input)j.next();
		String x_in_id = x_input.getId();
		if ( x_out_id.equals( x_in_id ) ) { 
		    Link x_link = new Link( x_input.getFrom() , x_output.getTo() , x_output.getName() , x_input.getTimeIndex() );
		    if ( x_input.isNegative() ) { x_link.setNegative(); }
            //if (x_output.hasWeight())
            //{
            //    x_link.setWeight(x_output.getWeight());
            //}
		  if (x_input.hasWeight())
            {
                x_link.setWeight(x_input.getWeight());
            }
		    
		    x_links.add( x_link );
		}
	    }
	}

	return x_links;
    }

    public List getDividers() {

	List x_elems = o_root.getChildren("divider");
	List x_dividers = new ArrayList();

	for( Iterator i = x_elems.iterator(); i.hasNext() ; ) {

	    x_dividers.add( new Divider( (Element)i.next() ) );
	}

	return x_dividers;
    }

    public BMSC addLink( Link p_link ) {

	addLink( p_link.getFrom() , p_link.getTo() , p_link.getName() , p_link.getTimeIndex() );
	sortChronologically();
	return this;
    }

    public BMSC addLink( String p_from , String p_to , String p_name , int p_timeindex ) {

	addOutput( p_from , p_to , p_name , p_timeindex , false );
	addInput(  p_from , p_to , p_name , p_timeindex , false );

	return this;
    } 

    public BMSC addNegativeLink( String p_from , String p_to , String p_name , int p_timeindex ) {

	addOutput( p_from , p_to , p_name , p_timeindex , true );
	addInput(  p_from , p_to , p_name , p_timeindex , true );
	setNegativeLink( p_name );
	return this;
    } 

    public BMSC addDivider( int p_timeindex ) {

	o_root.addContent( new Element("divider").setAttribute("timeindex" , String.valueOf(p_timeindex) ) );
	return this;
    }

    public BMSC deleteLink( String p_id ) {

	deleteInput( p_id );
	deleteOutput( p_id );

	return this;
    }

    public BMSC setNegativeLink( String p_name ) {

	o_root.setAttribute( "negative" , p_name );
	return this;
    }

    public BMSC unsetNegativeLink() {

	o_root.removeAttribute( "negative" );
	return this;
    }

    public String getNegativeLink() {

	String x_name = o_root.getAttribute("negative").getValue();
	if ( x_name != null && ! x_name.equals("") ) { return x_name; } else { return null; }
    }

    public boolean isNegative() {

	if (  o_root.getAttribute( "negative" ) != null ) {
	    String x_neg = o_root.getAttribute( "negative" ).getValue();
	    if ( x_neg != null && ! x_neg.equals( "" ) ) { return true; }
	}
	return false;
    }

    public String getName() {

	return o_root.getAttribute( "name" ).getValue();
    }

    public void setName( String p_name ) {

	o_root.setAttribute( "name" , p_name );
    }

    public Element asXML() {

	return o_root;
    }

    public void apply( Visitor v ) {

	if ( isNegative() ) {

	    v.caseANegativeBMSC( this );

	} else {

	    v.caseABMSC( this );
	}
    }

    private BMSC addOutput( String p_from , String p_to , String p_name , int p_timeindex , boolean p_neg ) {

	List x_instances = getInstances();
	Instance x_from = null;

	for( Iterator i = x_instances.iterator() ; i.hasNext() ; ) {

	    x_from = (Instance)i.next();

	    if ( x_from.getName().equals( p_from ) )
		break;
	}
	
	Element x_output = new Element( "output" );
	x_output.setAttribute("timeindex" , String.valueOf( p_timeindex ) );
	if ( p_neg ) { x_output.setAttribute( "negative" , "true" ); }
	x_output.addContent( new Element( "name" ).setText( p_name ) );
	x_output.addContent( new Element( "to" ).setText( p_to ) );
	x_from.add( x_output );
       
	return this;
    }

    private BMSC addInput( String p_from , String p_to , String p_name , int p_timeindex , boolean p_neg ) {

	List x_instances = getInstances();  
	Instance x_to = null;

	for( Iterator i = x_instances.iterator() ; i.hasNext() ; ) {

	    x_to = (Instance)i.next();

	    if ( x_to.getName().equals( p_to ) )
		break;
	}
	
	Element x_input = new Element( "input" );
	x_input.setAttribute("timeindex" , String.valueOf( p_timeindex ) );
	if ( p_neg ) { x_input.setAttribute( "negative" , "true" ); }
	x_input.addContent( new Element( "name" ).setText( p_name ) );
	x_input.addContent( new Element( "from" ).setText( p_from ) );
	x_to.add( x_input );
       
	return this;
    }
    
    private void deleteInput( String p_id ) {
	
	delete( "input" , p_id );
    }

    private void deleteOutput( String p_id ) {

	delete( "output" , p_id );
    }

    private void delete( String p_in_or_out , String p_id ) {
	
	List x_instances = getInstances();

	for( Iterator i = x_instances.iterator() ; i.hasNext() ; ) {

	    Instance x_inst = (Instance)i.next();
	    List x_children;

	    if ( p_in_or_out.equals("output") ) { x_children = x_inst.getOutputs(); }
	    else { x_children = x_inst.getInputs(); }

	    for ( Iterator j = x_children.iterator() ; j.hasNext() ; ) {
	
		InputOrOutput x_child = (InputOrOutput)j.next();
		if ( x_child.getId().equals( p_id ) ) {
		    x_inst.remove( x_child );
		    return;
		}
	    }
	}
    }


    public void changeLinkName( String p_id , String p_to ) {
	
	List x_instances = getInstances();

	for( Iterator i = x_instances.iterator() ; i.hasNext() ; ) {

	    Instance x_inst = (Instance)i.next();
	    List x_children = x_inst.getInputs();      
	    for ( Iterator j = x_children.iterator() ; j.hasNext() ; ) {
	
		Input x_child = (Input)j.next();
		if ( x_child.getId().equals( p_id ) ) {
		    x_child.setName( p_to );
		    break;
		}
	    }

	    x_children = x_inst.getOutputs();      
	    for ( Iterator j = x_children.iterator() ; j.hasNext() ; ) {
	
		Output x_child = (Output)j.next();
		if ( x_child.getId().equals( p_id ) ) {
		    x_child.setName( p_to );
		    break;
		}
	    }
	}
    }

    public void moveDivider( int p_old_ti , int p_new_ti ) {

	List x_dividers = getDividers();

	for ( Iterator i = x_dividers.iterator() ; i.hasNext() ; ) {

	    Divider x_div = (Divider)i.next();
	    if ( x_div.getTimeIndex() == p_old_ti ) {
		x_div.setTimeIndex( p_new_ti );
		break;
	    }
	}
    }

    public void deleteDivider( int p_timeindex ) {

	List x_dividers =  o_root.getChildren("divider");

	for ( Iterator i = x_dividers.iterator() ; i.hasNext() ; ) {

	    Element x_div = (Element)i.next();
	    if ( Integer.parseInt( x_div.getAttribute( "timeindex" ).getValue() ) == p_timeindex ) {
		i.remove();
		break;
	    }
	}
    }
    
    public void deleteInstance( String p_name ) {

	List x_instances = o_root.getChildren("instance");

	for( Iterator i = x_instances.iterator() ; i.hasNext() ; ) {

	    Instance x_inst = new Instance( (Element)i.next() );
	   		
	    List x_ins = x_inst.getInputs();

	    for( Iterator j = x_ins.iterator() ; j.hasNext() ; ) {

		Input x_i = (Input)j.next();
		if ( x_i.getFrom() != null && p_name.equals( x_i.getFrom() ) )
		    j.remove();
	    }

	    List x_outs = x_inst.getOutputs();

	    for( Iterator j = x_outs.iterator() ; j.hasNext() ; ) {

		Output x_o = (Output)j.next();
		if ( x_o.getTo() != null && p_name.equals( x_o.getTo() ) )
		    j.remove();
	    }

	    if ( x_inst.getName().equals( p_name ) ) {

		i.remove();
	    }
	}
    }
   

    //  public void changeTimeIndex( String p_name , int p_timeindex ) {
	
    public void changeTimeIndex( String p_id , int p_timeindex ) {

	List x_instances = getInstances();

	for( Iterator i = x_instances.iterator() ; i.hasNext() ; ) {

	    Instance x_inst = (Instance)i.next();
	    List x_children = x_inst.getInputs();      
	    for ( Iterator j = x_children.iterator() ; j.hasNext() ; ) {
	
		Input x_child = (Input)j.next();
		if ( x_child.getId().equals( p_id ) ) {
		    x_child.setTimeIndex( p_timeindex );
		    break;
		}
	    }

	    x_children = x_inst.getOutputs();      
	    for ( Iterator j = x_children.iterator() ; j.hasNext() ; ) {
	
		Output x_child = (Output)j.next();
		if ( x_child.getId().equals( p_id ) ) {
		    x_child.setTimeIndex( p_timeindex );
		    break;
		}
	    }
	}
    }

    public void reverseLink( String p_id ) {

	String x_from = "";
	String x_to = "";
	String x_name = "";
	int x_time = 0;

	List x_instances = getInstances();

	for( Iterator i = x_instances.iterator() ; i.hasNext() ; ) {

	    Instance x_inst = (Instance)i.next();
	    List x_children = x_inst.getOutputs();      
	    for ( Iterator j = x_children.iterator() ; j.hasNext() ; ) {
		Output x_child = (Output)j.next();
		if ( x_child.getId().equals( p_id ) ) {
		    x_name = x_child.getName();
		    x_from = x_inst.getName();
		    x_to = x_child.getTo();
		    x_time = x_child.getTimeIndex();
		    break;
		}
	    }
	}

	deleteLink( p_id );
	addLink( x_to , x_from , x_name , x_time );
    }

    public void negateLastMessage() {

	List x_links = getLinks();

	//return if there are no links in this msc
	if ( x_links.size() < 1 ) return;

	

       	Link x_last_link = (Link)x_links.get(0);;

	for ( Iterator i = x_links.iterator() ; i.hasNext() ; ) {
	    Link x_link = (Link)i.next();
	    if ( x_link.getTimeIndex() > x_last_link.getTimeIndex() ) x_last_link = x_link;
	}

	negateLink( x_last_link.getId() );
	changeTimeIndex( x_last_link.getId() , x_last_link.getTimeIndex() + 1 );
	addDivider( x_last_link.getTimeIndex() );
    }

    public void negateLink( String p_id ) {

	List x_instances = getInstances();
	String x_name = "";

	for( Iterator i = x_instances.iterator() ; i.hasNext() ; ) {

	    Instance x_inst = (Instance)i.next();
	    List x_children = x_inst.getInputs();      
	    for ( Iterator j = x_children.iterator() ; j.hasNext() ; ) {
	
		Input x_child = (Input)j.next();
		if ( x_child.getId().equals( p_id ) ) {
		    if ( x_child.isNegative() ) { x_child.setNegative( false ); }
		    else {
			x_child.setNegative( true );
			x_name = x_child.getName();
		    }
		    break;
		}
	    }
	    
	    x_children = x_inst.getOutputs();      
	    for ( Iterator j = x_children.iterator() ; j.hasNext() ; ) {
		
		Output x_child = (Output)j.next();
		if ( x_child.getId().equals( p_id ) ) {
		    if ( x_child.isNegative() ) { x_child.setNegative( false ); }
		    else { x_child.setNegative( true ); }
		    break;
		}
	    }
	}
	
	// this isn't very good, will not work well in some circumstances
	// TODO: do this better!

	if ( ! isNegative() ) { 
	    setNegativeLink( x_name ); 
	} else {
	    unsetNegativeLink();
	}
    }

    public void tidy() {

	//compact all the time indeces so that they are consecutive

	List x_components = getLinks();
	x_components.addAll( getDividers() );

	int x_high = getHighestTimeIndex( x_components );
	int x_current = 0;

	for ( int i = 0 ; i <= x_high ; i++ ) {

	    boolean x_set = false;

	    for( Iterator j = x_components.iterator() ; j.hasNext() ; ) {

		BMSCComponent x_comp = (BMSCComponent)j.next();
		if ( x_comp.getTimeIndex() == i && i > x_current ) {

		    //HACK

		    if ( x_comp instanceof Link ) {
			changeTimeIndex( ((Link)x_comp).getId() , x_current + 1 );
		    } else {
			x_comp.setTimeIndex( x_current + 1 );
		    }
		    //<<HACK

		    x_set = true;
		}
	    }

	    if ( x_set ) x_current++;
	}
    }

    public int getLastTimeIndex() {

	return getHighestTimeIndex( getLinks() );
    }

    private int getHighestTimeIndex( List p_links ) {

	int x_hi = 0;

	for (Iterator i = p_links.iterator() ; i.hasNext() ; ) {

	    int x_temp = ((BMSCComponent)i.next()).getTimeIndex();

	    if ( x_temp > x_hi ) x_hi = x_temp;
	}

	return x_hi;
    }

    private void sortChronologically() {

	List x_instances = getInstances();

	for( Iterator i = x_instances.iterator() ; i.hasNext() ; ) {

	    Collections.sort( ((Instance)i.next()).getInputsAndOutputs() , new Comparator() {

		    public int compare( Object p_a , Object p_b ) {
			
			int x_time_a = ((InputOrOutput)p_a).getTimeIndex();
			int x_time_b = ((InputOrOutput)p_b).getTimeIndex();

			if ( x_time_a > x_time_b ) return 1;
			else if ( x_time_a < x_time_b ) return -1;
			else return 0;		    
		    }
		});
	}
    }

    public BMSC duplicate() {

	return new BMSC( (Element)o_root.clone() );
    }
    
    public void setLinkWeight(String p_id, double p_weight) {
        // rewrite XML
        List x_list = o_root.getChildren("instance");
        Iterator x_it = x_list.iterator();
        while (x_it.hasNext()) {
            Instance x_instance = new Instance((Element)x_it.next());
            List x_inputs = x_instance.getInputs();
            Iterator x_it2 = x_inputs.iterator();
            while (x_it2.hasNext()) {
                Input x_input = (Input)x_it2.next();
                if (x_input.getId().equals(p_id)) {
                    if (x_input.hasWeight())
                    		x_input.removeWeight();
                		x_input.setWeight(p_weight);
                }
            //List x_outputs = x_instance.getOutputs();
            //Iterator x_it2 = x_outputs.iterator();
            //while (x_it2.hasNext()) {
            //    Output x_output = (Output)x_it2.next();
            //    if (x_output.getId().equals(p_id)) {
            //        x_output.setWeight(p_weight);
            //    }
            }
        }
    }
    
}
