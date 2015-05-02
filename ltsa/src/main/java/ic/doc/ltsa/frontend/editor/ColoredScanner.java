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
import ic.doc.ltsa.common.iface.ICompilerFactory;
import ic.doc.ltsa.common.iface.ILex;
import ic.doc.ltsa.common.iface.ISymbol;
import ic.doc.ltsa.common.iface.LTSInput;

import javax.swing.text.*;
import java.awt.Color;

public class ColoredScanner implements LTSInput {

    private Document		doc;   //current document
	private int				d_Pos; //current position	in d_text
	private Segment			d_text; //current text contents
	private int				d_offset; //start location of string in document
	private ILex			lex;
	private ISymbol			current; //current Symbol
    private ICompilerFactory oCompilerFactory;
	
	// .......................................................................
	
	// CONSTRUCTOR-DESTRUCTOR
	ColoredScanner(Document document) { 
		d_text = new Segment();
		doc = document;
		d_Pos = -1;
		d_offset = 0; 
		if (doc.getLength()>0) {
			try {          
			   document.getText(0,doc.getLength(),d_text);
			}
			catch (BadLocationException e) {}
		}
		
		com.chatley.magicbeans.PluginManager.getInstance().addBackDatedObserver(this);
	} 

	// ..........................................................................
	
  
  
  //move symbol on one position
	public void next() { 
		try {
			current = lex.in_sym();
        }
	    catch(Exception ex) {  }
	} 
	
	// .......................................................................
	
	/**
	 * Sets the range of the scanner.  This should be called
	 * to reinitialize the scanner to the desired range of
	 * coverage.
	 */
	public void setRange(int beginPos, int endPos) throws BadLocationException {
	    doc.getText(beginPos, endPos-beginPos, d_text);
	    d_Pos = -1;
	    d_offset = beginPos;
	    current = null;
	} 

	// ..........................................................................
	
	/**
	 * This fetches the starting location of the current
	 * token in the document.
	 */
	public final int getStartOffset(){
		if (current == null)
			return (d_offset + d_Pos);
		else
			return (current.getStartPos() + d_offset);
	} 

	/**
	 * This fetches the ending location of the current
	 * token in the document.
	 */
	public final int getEndOffset() {
		if (current == null)
			return (d_offset + d_Pos+1);
		else
			return (current.getEndPos() + d_offset+1);
	} 
  
  /** 
   * get the desired color of the current token
   */
  public Color getColor() {
	if (current == null)
		return Color.black;
	else
		return current.getColor();
  }
  /** -------------------------
  * Implementation of LTSInput
  */
  
	public char nextChar () {
		d_Pos++;
		if (d_Pos < d_text.count) {
			return d_text.array[d_Pos];
		} else {
			return '\u0000';
		}
	}


  public char backChar () {
	char ch;
	d_Pos--;
	if (d_Pos < 0) {
		d_Pos = -1;
		return '\u0000';
	}
	else
		return d_text.array [d_Pos];
  }


  public int getMarker () {
    return d_Pos;
  }  
  
  public void pluginAdded( ICompilerFactory pCompFact ) {
      
      System.out.println("Connected compiler factory to ColoredScanner");
      
      oCompilerFactory = pCompFact;
      lex = oCompilerFactory.createLex(this,false);
  }
} 