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

package ic.doc.ltsa.frontend.editor;

import javax.swing.text.*;


/**
 * uses ColoredDocument to render  text according to FSP syntax
 */
public class ColoredEditorKit extends DefaultEditorKit {
    private ColoredContext d_Preferences;

	// .......................................................................
	
	// CONSTRUCTOR-DESTRUCTOR
    public ColoredEditorKit() { 
      super(); 
      d_Preferences = new ColoredContext(); 
    }

	// .......................................................................
	
	// MANIPULATORS
    public void setStylePreferences(ColoredContext prefs) {
       d_Preferences = prefs; 
    }
    public ColoredContext getStylePreferences() { 
        return(d_Preferences); 
    }
	
    /**
     * Fetches a factory that is suitable for producing 
     * views of any models that are produced by this
     * kit. The default is to have the UI produce the
     * factory, so this method has no implementation.
     *
     * @return the view factory
     */
    public final ViewFactory getViewFactory() {	
      return(getStylePreferences()); 
    }

	// .......................................................................
	
 	// ACCESSORS
   /**
     * Get the MIME type of the data that this
     * kit represents support for.  This kit supports
     * the type <code>text/lts</code>.
     */
    public String getContentType() { return("text/lts"); }

    /**
     * Create a copy of the editor kit.  This
     * allows an implementation to serve as a prototype
     * for others, so that they can be quickly created.
     */
    public Object clone() {
      	ColoredEditorKit kit = new ColoredEditorKit(); 
	     	kit.d_Preferences = d_Preferences; 
        return(kit); 
    }

    /**
     * Creates an uninitialized text storage model
     * that is appropriate for this type of editor.
     *
     * @return the model
     */
    public Document createDefaultDocument() { 
        return(new ColoredDocument()); 
    }
}