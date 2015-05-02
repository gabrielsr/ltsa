package ic.doc.ltsa.common.infra;

import ic.doc.ltsa.common.iface.LTSInput;

import java.io.*;

public class InputSource implements LTSInput {
 
    private String oName;
    private String oSrc;
    protected int oPos;
    
    public InputSource() {}

    public InputSource(String pSrc) {

        oPos = -1;
        oSrc = pSrc;
    }
    
    public InputSource( String pName , String pSrc ) {
        
        this( pSrc );
        oName = pName;
    }
    
    public InputSource( File pFile ) {
        
        oPos = -1;
        oName = pFile.getName();
        
        StringBuffer xSB = new StringBuffer("");
        
		BufferedReader xBuf = null;
		
        try {
             xBuf = new BufferedReader( new FileReader( pFile ));
            
            for ( String xLine = xBuf.readLine() ; xLine != null ; xLine = xBuf.readLine() ) {
                xSB.append( xLine + "\n" );
            }
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
			if ( xBuf != null ) { 
				try {
					xBuf.close();
				} catch (IOException e) { // can't do much here
				} 
			}			
        }

        oSrc = xSB.toString();
    }
    
    public char nextChar() {

        oPos = oPos + 1;  
        if ( oPos < oSrc.length()) {             
            return oSrc.charAt(oPos);
        } else {
            oPos = oPos - 1;
            return '\u0000';
        }
    }

    public char backChar() {

        char ch;
        oPos = oPos - 1;
        
        if (oPos < 0) {
            oPos = 0;
            return '\u0000';
        } else {
          
            return oSrc.charAt(oPos);
        }
    }

    public int getMarker() {

        return oPos;
    }
   
    public String getCompleteSource() {
        
        return oSrc;
    }
    
    public String getName() {
        
        return oName;
    }
    
    public void reset() {
     
        oPos = -1;
    }
    
    public int length() {
        
        return oSrc.length();
    }
    
    protected boolean hasNext() {
        
        return oPos < oSrc.length() - 1;
    }
    
    protected boolean hasPrevious() {
        
        return oPos > -1;
    }
    
}
