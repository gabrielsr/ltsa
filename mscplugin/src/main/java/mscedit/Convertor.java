package mscedit;

import java.io.*;
import java.util.*;

public class Convertor {

    private Specification o_spec;

    public Convertor( String p_file ) {

	File x_file = null;

	try { 
	    x_file = new File( p_file ); 
	} catch ( Exception e ) { System.err.println( "Error converting file " + p_file + "\n" + e.getMessage() ); }

	o_spec = new Specification( XMLUtil.loadFromFile( x_file ) );

	List x_bmscs = o_spec.getBMSCs();

	for ( Iterator i = x_bmscs.iterator() ; i.hasNext() ; ) {

	    BMSC x_bmsc = (BMSC)i.next();

       	    List x_links = x_bmsc.getLinks();

	    for ( Iterator j = x_links.iterator() ; j.hasNext() ; ) {

		Link x_link = (Link)j.next();
		String x_newname = x_link.getFrom().toLowerCase() + "," + x_link.getTo().toLowerCase() + "," +  x_link.getName().replace( '.' , '_' );
 
		x_bmsc.changeLinkName( x_link.getName() + x_link.getTimeIndex() , x_newname );
	    }
       
	    List x_insts = x_bmsc.getInstances();

	    for ( Iterator j = x_insts.iterator() ; j.hasNext() ; ) {

		Instance x_inst = (Instance)j.next();

    		x_inst.setName( rename( x_inst.getName() ) );

		List x_inputs = x_inst.getInputs();

		for ( Iterator k = x_inputs.iterator() ; k.hasNext() ; ) {

		    Input x_input = (Input)k.next();
		    x_input.setFrom( rename( x_input.getFrom() ) );
		}

		List x_outputs = x_inst.getOutputs();
		
		for ( Iterator k = x_outputs.iterator() ; k.hasNext() ; ) {
		    
		    Output x_output = (Output)k.next();
		    x_output.setTo( rename( x_output.getTo() ) );
		}
	    }
	}

	XMLUtil.saveToFile( o_spec.asXML() , x_file );

	System.out.println( "Converted file " + x_file.getName() );
    }

    private void changeLinkName( String p_id , String p_to , BMSC p_bmsc ) {

	List x_instances = p_bmsc.getInstances();

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

    private String rename( String p_old ) {

	return p_old.toLowerCase() + ":" + p_old;
    }
    
    public static void main( String[] args ) {

	new Convertor( args[0] );
    }
}
