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

import java.awt.*;
import javax.swing.text.*;

public class ColoredContext extends StyleContext implements ViewFactory {
    /**
     * View that uses the lexical information to determine the
     * style characteristics of the text that it renders.  This
     * simply colorizes the various tokens and assumes a constant
     * font family and size.
     */
	static class ColoredView extends PlainView {
		private ColoredScanner lexer;
		private boolean   lexerValid;
		
		// ...................................................................
		
		// CONSTRUCTOR-DESTRUCTOR
		/**
		 * Construct a simple colorized view of darwin
		 * text.
		 */
		ColoredView(Element elem) { 
      super(elem);
			ColoredDocument doc = (ColoredDocument) getDocument();
			lexer            = doc.getScanner();
			lexerValid       = false;		
		} 

		// ...................................................................

		/**
		 * Renders using the given rendering surface and area 
		 * on that surface.  This is implemented to invalidate
		 * the lexical scanner after rendering so that the next
		 * request to drawUnselectedText will set a new range
		 * for the scanner.
		 *
		 * @param g the rendering surface to use
		 * @param a the allocated region to render into
		 *
		 * @see View#paint
		 */
		public void paint(Graphics g, Shape a) { 
      super.paint(g, a); 
      lexerValid = false; 
    }

		// ...................................................................

		/**
		 * Renders the given range in the model as normal unselected
		 * text.  This is implemented to paint colors based upon the
		 * token-to-color translations.  To reduce the number of calls
		 * to the Graphics object, text is batched up until a color
		 * change is detected or the entire requested range has been
		 * reached.
		 *
		 * @param g the graphics context
		 * @param x the starting X coordinate
		 * @param y the starting Y coordinate
		 * @param p0 the beginning position in the model
		 * @param p1 the ending position in the model
		 * @returns the location of the end of the range
		 * @exception BadLocationException if the range is invalid
		 */
		protected int drawUnselectedText(Graphics g, int x, int y, int beginPos, int endPos) 
			throws BadLocationException {
			Document document;
			Color    lastForeground, foreground;
			int      markPos, pos;
			Style    style;
			Segment  text;
			
			document       = getDocument();
			lastForeground = null;
			markPos        = beginPos;
			
			while (beginPos < endPos) {
				updateScanner(beginPos);
				pos   = Math.min(lexer.getEndOffset(), endPos);
				pos   = (pos <= beginPos) ? endPos : pos;
				foreground = lexer.getColor();
				if ((foreground != lastForeground) && (lastForeground != null)) {
					// color change, flush what we have
					g.setColor(lastForeground);
					text = getLineBuffer();
					document.getText(markPos, beginPos - markPos, text);
					x = Utilities.drawTabbedText(text, x, y, g, this, markPos);
					markPos = beginPos;
				}
				lastForeground = foreground;
				beginPos       = pos;
			}
			// flush remaining
			g.setColor(lastForeground);
			text = getLineBuffer();
			document.getText(markPos, endPos - markPos, text);
			x = Utilities.drawTabbedText(text, x, y, g, this, markPos);
			
			return(x);
		} // drawUnselectedText()

		// ...................................................................

		/**
		 * Update the scanner (if necessary) to point to the appropriate
		 * token for the given start position needed for rendering.
		 */
		void updateScanner(int pos) {
			try {
			if (!lexerValid) {
				ColoredDocument doc = (ColoredDocument) getDocument();
				lexer.setRange(doc.getScannerStart(pos), doc.getLength());
				lexerValid = true;
			}
			while (lexer.getEndOffset() <= pos) {	lexer.next(); }
			// can't adjust scanner... calling logic will simply render the remaining text.
			} catch (Throwable e) {	e.printStackTrace(); }
		} // updateScanner()	
    } // C_DarwinView
	
	// .......................................................................

	// CONSTRUCTOR-DESTRUCTOR
	/**
     * Constructs a set of styles to represent darwin lexical 
     * tokens.  By default there are no colors or fonts specified.
     */
    public ColoredContext() { super(); } 

	// .......................................................................
	
    // ViewFactory methods
    public View create(Element elem) { return new ColoredView(elem); }
}