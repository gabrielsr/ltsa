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

package ic.doc.ltsa.common;

import ic.doc.ltsa.common.iface.ICompilerFactory;
import ic.doc.ltsa.common.iface.ICompositeState;
import ic.doc.ltsa.common.iface.ILTSCompiler;
import ic.doc.ltsa.common.iface.LTSOutput;
import ic.doc.ltsa.common.infra.*;

import java.util.*;
import java.io.File;


/**
 * This class manages compiler operations (parsing etc), so that details of the
 * separate compiler phases are hidden from the main GUI classes behind a facade.
 * 
 * The CompileManager accepts CompilerFactory plugins, provided by whichever
 * pluggable backend is available.
 */
public class CompileManager {

    private LTSOutput oOutput;
    private String oCurrentDir;
    
    private CompositeInputSource oComposite;
    private ICompilerFactory oCompilerFactory;
    
    public CompileManager( LTSOutput pOutput ) {
        
        oOutput = pOutput;
        com.chatley.magicbeans.PluginManager.getInstance().addBackDatedObserver(this);
    }
    
    public void setCurrentDirectory( String pCurrentDir ) {
        
        oCurrentDir = pCurrentDir;
    }
    
    
    public Hashtable parse( CompositeInputSource pSource ) throws LTSException {
        
        Map<String, InputSource> xIncludes = new HashMap<String, InputSource>();
        
        for( Iterator i = pSource.getSources().iterator() ; i.hasNext() ; ) {
            
            InputSource xSrc = (InputSource)i.next();
            xSrc.reset();
            xIncludes.put( xSrc.getName() , xSrc );
        }
                
        return doParse( pSource , xIncludes );
    }
    
    
    public Hashtable parse( InputSource pSource ) throws LTSException {
        
        Map<String, InputSource> xIncludes = new HashMap<String, InputSource>();
        pSource.reset();
        xIncludes.put( pSource.getName() , pSource );
        
        return doParse( pSource, xIncludes );
    }
        
    private Hashtable doParse(InputSource pSource, Map<String, InputSource> xIncludes) {

        oComposite = new CompositeInputSource();
        Hashtable cs = new Hashtable();
        Hashtable ps = new Hashtable();

        ILTSCompiler comp = oCompilerFactory.createCompiler(pSource, oOutput, oCurrentDir);

        comp.resolveIncludes(xIncludes);

        pSource.reset();

        for (Iterator<String> i = xIncludes.keySet().iterator(); i.hasNext();) {

            Object xObj = i.next();
            String xName;
            if (xObj instanceof File) {
                File xFile = (File) xObj;
                xName = xFile.getName();
            } else {
                xName = (String) xObj;
            }

            InputSource xSrc = xIncludes.get(xObj);
            xSrc.reset();
            oComposite.add(xSrc);
        }
        
        ILTSCompiler xComp = oCompilerFactory.createCompiler(oComposite, oOutput, oCurrentDir);

        xComp.parse(cs, ps);

        return cs;
    }
    
    
    public ICompositeState compile(String pTarget) throws LTSException {

        ICompositeState cs = null;
        oComposite.reset();

        ILTSCompiler comp = oCompilerFactory.createCompiler(oComposite, oOutput, oCurrentDir);
        cs = comp.compile(pTarget);

        return cs;
    }

    public String getSourceNameForMarker(int i) {

        return oComposite.getSourceNameForMarker( i );
    }

    public InputSource getSourceByName(String fileName) {

        return oComposite.getSourceByName( fileName );
    }

    public int getLocalMarker(int i) {

        return oComposite.getLocalMarker( i );
    }


    public List getAllProcessNames( InputSource pSource ) throws LTSException {

        List x_list = new ArrayList();

        ILTSCompiler comp = oCompilerFactory.createCompiler( pSource , oOutput, oCurrentDir ); 
        Hashtable cs = new Hashtable();
        Hashtable ps = new Hashtable();
        
        comp.parse(cs, ps);
        x_list.addAll(ps.keySet());
        
        return x_list;
    }

    public void pluginAdded( ICompilerFactory pCompFac ) {
        
        System.out.println("Connected Compiler Factory");
        oCompilerFactory = pCompFac;
    }
}
