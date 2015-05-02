package mscedit;

import java.util.*;

public class XMLTester {
	
	public XMLTester() {}
	
	public static void main( String[] args ) {
		
		new XMLTester().test();
	}
	
	public void test() {
		
		BMSC x_reg = new BMSC( "register" );
		x_reg.addInstance( "sensor" );
		x_reg.addInstance( "database" );
		x_reg.addLink( "sensor" , "database" , "pressure" , 1 );
		
		List x_links = x_reg.getLinks();
		for( Iterator i = x_links.iterator() ; i.hasNext() ; System.out.println( i.next() ) );
	}
}





