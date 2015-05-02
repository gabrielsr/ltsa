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
 * Simple extension of plain document to permit colored text
*/
public class ColoredDocument extends PlainDocument {
    ColoredScanner scanner;
	// CONSTRUCTOR-DESTRUCTOR
    public ColoredDocument() { 
      super(new GapContent(1024)); 
      scanner = new ColoredScanner(this);
      // Set the maximum line width
      putProperty(PlainDocument.lineLimitAttribute, new Integer(256)); 
      // Set TAB size
      putProperty(PlainDocument.tabSizeAttribute, new Integer(4)); 
    }

	/**
	 * return the lexical analyzer to produce colors for this document.
	 */
	public ColoredScanner getScanner() {
    return scanner;  
	} 

	// ...................................................................
	
    /**
     * Fetch a reasonable location to start scanning
     * given the desired start location.  This allows
     * for adjustments needed to accomodate multiline
     * comments.
     * Currently scans the complete document. This is not efficient
     * but it's safe
     */
	public int getScannerStart(int p) { return(0); }

	// ..................................................................
}