/******************************************************************************
 * LTSA (Labelled Transition System Analyser) - LTSA is a verification tool   *
 * for concurrent systems. It mechanically checks that the specification of a *
 * concurrent system satisfies the properties required of its behaviour.      *
 * Copyright (C) 2001-2004 Jeff Magee (with additions by Robert Chatley)      *
 *                                                                            *
 * This program is free software; you can redistribute it and/or              *
 * modify it under the terms of the GNU General Public License                *
 * as published by the Free Software Foundation; either version 2             *
 * of the License, or (at your option) any later version.                     *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program; if not, write to the Free Software                *
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA *
 *                                                                            *
 * The authors can be contacted by email at {jnm,rbc}@doc.ic.ac.uk            *
 *                                                                            *
 ******************************************************************************/

package ic.doc.ltsa.common.infra;

import java.util.*;

/**
 * This class allows the aggregation of a number of InputSources so that the
 * appear as one, i.e. when the end of one InputSource is reached, input is
 * taken from the next. Composites can also compose Composites, in accordance
 * with the composite design pattern (GoF).
 * 
 * @author rbc
 */
public class CompositeInputSource extends InputSource {
    
    private Map<String, InputSource> oSources;
    private Map<InputSource, String> oNames;
    private ListIterator<InputSource> oSourceIter;
    private InputSource oCurrentSource;

    public CompositeInputSource() {
        
        oSources = new TreeMap<String, InputSource>();
        oNames = new HashMap<InputSource, String>();
    }
    
    public void add( InputSource pSrc ) {
        
        oSources.put( pSrc.getName() , pSrc );
        oNames.put( pSrc , pSrc.getName() );
     }
    
    public char nextChar() {

        if (oSourceIter == null) {
            init();
        }
        
        if (oCurrentSource != null) {

            if (oCurrentSource.hasNext()) {

                oPos += 1;
                
                return oCurrentSource.nextChar();

            } else if (oSourceIter.hasNext()) {

                nextSource();
                return nextChar();
            }
        }

        return '\u0000';
    }

    public char backChar() {

        if ( oSourceIter == null ) { init(); }
        
        if ( oCurrentSource.hasPrevious() ) {
            
            oPos -= 1;
            
            return oCurrentSource.backChar();
            
        } else if ( oSourceIter.hasPrevious() ) {
            
            previousSource();
            return backChar();
        
        } else {
            
            return '\u0000';
        }
    }
    
    public int getLocalMarker(int pMarker) {

        int xMarker = pMarker;

        for (Iterator<InputSource> i = new ArrayList<InputSource>( oSources.values() ).listIterator(); i.hasNext();) {

            InputSource xSource = i.next();
        
            if (xMarker <= xSource.length()) {
                return xMarker;
            } else {
                xMarker = xMarker - xSource.length();
            }
        }
        
        return xMarker;
    }

    public String getSourceNameForMarker( int pMarker ) {
        
        reset();
        
        while( oPos < pMarker ) {
            
            nextChar();
        }
        
        return oNames.get(oCurrentSource);
    }
    
    public InputSource getSourceByName( String pName ) {
        
        return oSources.get( pName );
    }
    
    private void init() {
        
        oSourceIter = new ArrayList<InputSource>( oSources.values() ).listIterator();
        nextSource();
    }

    private void nextSource() {
        
        oCurrentSource = (InputSource)oSourceIter.next();
    }
    
    private void previousSource() {
        
        oCurrentSource = (InputSource)oSourceIter.previous();
    }
    
    public void reset() {
        
        init();
        super.reset();
        
        for (Iterator<InputSource> i = oSources.values().iterator() ; i.hasNext() ; ) {
            (i.next()).reset();
        }  
    }
    
    public String getCompleteSource() {
        
        StringBuffer xSrc = new StringBuffer();
        
        for ( Iterator<InputSource> i = oSources.values().iterator() ; i.hasNext() ; ) {
            xSrc.append( (i.next()).getCompleteSource() );
        }
        
        return xSrc.toString();
    }

    public Collection<InputSource> getSources() {

        return oSources.values();
    }
}
