package mscedit;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.*;

class XMLUtil {
    
    public static Document loadFromFile( String p_filename ) {
	
		return loadFromFile( new File( p_filename ) );
    }

    public static Document loadFromFile( File p_file ) {

	SAXBuilder x_in = new SAXBuilder();

	try {
   
	    return x_in.build( new FileInputStream( p_file ) );

	} catch ( Exception e ) { e.printStackTrace(); }	
  
	return null;
    }

    public static void saveToFile( Document p_doc , String p_filename ) {

	saveToFile( p_doc , new File( p_filename ) );
    }

    public static void saveToFile( Document p_doc , File p_file ) {
	
	try {
	    saveToFile( p_doc , new FileOutputStream( p_file ) );
	} catch ( Exception e ) { e.printStackTrace(); }
    }

    public static void saveToFile( Document p_doc , FileOutputStream p_fos ) {

	XMLOutputter x_out = new XMLOutputter();
	x_out.setIndent("  ");
	x_out.setNewlines(true);

	try {
	    x_out.output( p_doc , p_fos );
	} catch ( Exception e ) { e.printStackTrace(); }
    }
}



