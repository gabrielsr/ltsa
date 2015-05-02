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
 
package ic.doc.ltsa.lts;

import ic.doc.ltsa.common.iface.ILex;
import ic.doc.ltsa.common.iface.ISymbol;
import ic.doc.ltsa.common.iface.LTSInput;
import ic.doc.ltsa.common.infra.Diagnostics;

public class Lex implements ILex {

	private LTSInput input;
	private Symbol symbol;
	private char ch;
	private boolean eoln;
	private boolean newSymbols = true; // true is return new object per symbol


public Lex (LTSInput input) {
	this(input,true);
}

public Lex (LTSInput input, boolean newSymbols) {
  this.input = input;
  this.newSymbols = newSymbols;
  if (!newSymbols) symbol = new Symbol(); // use this for everything
}

private void error (String errorMsg) {
	Diagnostics.fatal (errorMsg, new Integer(input.getMarker ()));
}

private void next_ch () {
	ch = input.nextChar ();
	eoln = (ch == '\n' || ch == '\u0000');
}

private void back_ch () {
	ch = input.backChar ();
	eoln = (ch == '\n' || ch == '\u0000');
}

private void in_comment () {
	if (ch == '/') { 		// Skip C++ Style comment
		do {
			next_ch ();
		} while (eoln == false);
	} else {  					// Skip C Style comment
		do {
			do {
				next_ch ();
			} while (ch != '*' && ch!='\u0000');
			do {
			  next_ch ();
			} while (ch == '*' && ch!='\u0000');
		} while (ch != '/'&& ch!='\u0000');
		next_ch ();
	}
  if (!newSymbols) {
    symbol.kind = Symbol.COMMENT;
    back_ch();
  }
}

private boolean isodigit (char ch) {
	return ch >= '0' && ch <= '7';
}

private boolean isxdigit (char ch) {
	return (ch >= '0' && ch <= '9')	|| (ch >= 'A' && ch <= 'F')
		  || (ch >= 'a' && ch <= 'f');
}

private boolean isbase (char ch, int base) {
	switch (base) {
	case 10:
		return Character.isDigit (ch);
	case 16:
		return isxdigit (ch);
	case 8:
		return isodigit (ch);
  }
	return true; // dummy statement to stop to remove compiler warning
}


private void in_number () {
	long intValue = 0;
	int digit = 0;
	int base = 10;

	symbol.kind = Symbol.INT_VALUE;			// assume number is a INT

	// determine base of number
	if (ch == '0') {
		next_ch ();

		if (ch == 'x' || ch == 'X') {
			base = 16;
			next_ch ();
		} else {
			base = 8;
		}

	} else {
		base = 10;
	}

	StringBuffer realBuf = new StringBuffer ();  // holds potential real literals

	while (isbase (ch, base)) {
		realBuf.append (ch);
		switch (base) {
		case 8:
		case 10:
			digit = ch - '0';
			break;
		case 16:
			if (Character.isUpperCase (ch))
				digit = (ch - 'A')+10;
			else if (Character.isLowerCase (ch))
				digit = (ch - 'a')+10;
			else
				digit = ch - '0';
		}

		if (intValue * base > Integer.MAX_VALUE - digit) {
			error ("Integer Overflow");
			intValue = Integer.MAX_VALUE;
			break;
		} else {
			intValue = intValue * base + digit;
		}

		next_ch ();
	}
/*
	if (base == 10) {  // determine if real
		boolean numIsDouble = false;

		if (ch == '.') {
			numIsDouble = true;
			do {
				realBuf.append (ch);  next_ch ();
			} while (Character.isDigit (ch));
		}

		if (ch == 'e' || ch == 'E') {
			numIsDouble = true;
			realBuf.append (ch); next_ch ();

			if (ch == '+' || ch == '-') {
				realBuf.append (ch); next_ch ();
			}

			if (Character.isDigit (ch)) {
				while (Character.isDigit (ch)) {
					realBuf.append (ch); next_ch ();
				}
			} else
				error ("exponent expected after e or E");
		}

		if (numIsDouble) {
			try {
				symbol.doubleValue = Double.valueOf (realBuf.toString ()).doubleValue ();
				symbol.kind = Symbol.DOUBLE_VALUE;
			} catch (NumberFormatException msg) {
				error ("Bad double value. " + msg);
			}
		}

	} else if (ch == 'U' || ch == 'u' || ch == 'L' || ch == 'U')
		next_ch ();
    */
	symbol.setValue((int)intValue);
	back_ch ();

}

// _______________________________________________________________________________________
// IN_ESCSEQ

private void in_escseq () {
	int n;
	while (ch == '\\') {
		next_ch ();
		switch (ch) {
		case 'a':
			ch = 'a';  // TODO - check is a BELL?
			break;

		case 'b':
			ch = '\b';
			break;

		case 'f':
			ch = '\f';
			break;

		case 'n':
			ch = '\n';
			break;

		case 'r':
			ch = '\r';
			break;

		case 't':
			ch = '\t';
			break;

		case 'v':
			// ch = '\v';	// TODO - check vertical tab
			break;

		case '\\':
			ch = '\\';
			break;

		case '\'':
			ch = '\'';
			break;

		case '\"':
			ch = '\"';
			break;

		case '?':
			ch = '?'; 	// TODO  - check
			break;

		case '0': case '1': case '2': case '3':
		case '4': case '5': case '6': case '7':
			n = ch - '0';
			next_ch ();
			if (isodigit (ch)) {
				n = n * 8 + ch - '0';
				next_ch ();
				if (isodigit (ch)) {
				  n = n * 8 + ch - '0';
				}
			}
		  ch = (char) n;
			break;

		case 'x':
		case 'X':
			n = 0;
			next_ch ();
			if (!isxdigit (ch))
				 error ("hex digit expected after \\x");
			else {
				int hex_digits = 0;
				while (isxdigit (ch) && hex_digits < 2) {  // max of two hex digits for IDL
					hex_digits = hex_digits + 1;
					if (Character.isDigit (ch))
						n = n * 16 + ch - '0';
					else if (Character.isUpperCase (ch))
						n = n * 16 + ch - 'A';
					else
						n = n * 16 + ch - 'a';
					next_ch ();
				}
			}
			ch = (char) n;
		}
	}
}


// _______________________________________________________________________________________
// IN_STRING

	private void in_string() {
		char quote = ch;
		boolean more;

		StringBuffer buf = new StringBuffer();
		do {
			next_ch();
			/*
			 * if (ch == '\\') in_escseq ();
			 */// no esc sequence in strings
			if (more = (ch != quote && !eoln))
				buf.append(ch);
		} while (more);
		symbol.setString(buf.toString());
		if (eoln)
			error("No closing character for string constant");
		symbol.kind = Symbol.STRING_VALUE;
	}

// _______________________________________________________________________________
// IN_IDENTIFIER

private void in_identifier () {
	StringBuffer buf = new StringBuffer ();
	do {
		buf.append (ch);
		next_ch();
	} while (Character.isLetterOrDigit (ch) || ch == '_');
    String s = buf.toString ();
	symbol.setString (s);
	Object kind = SymbolTable.get(s);
	if (kind == null){
	    if (Character.isUpperCase(s.charAt(0)))
		  symbol.kind = Symbol.UPPERIDENT;
		else
		  symbol.kind = Symbol.IDENTIFIER;
	} else {
		symbol.kind = ((Integer) kind).intValue ();
	}

	back_ch();
}

// _______________________________________________________________________________________
// IN_SYM

public ISymbol in_sym () {
	next_ch ();
	if (newSymbols) symbol = new Symbol ();


	boolean DoOnce = true;

	while (DoOnce) {
		DoOnce = false;

		symbol.startPos = input.getMarker ();
		switch (ch) {
		case '\u0000':
			symbol.kind = Symbol.EOFSYM;
			break;

	// Whitespaces, Comments & Line directives
		case ' ':
		case '\t':
		case '\n':
		case '\r':
	//	case '\v':
		case '\f':
			while (Character.isWhitespace (ch))
				next_ch ();
			DoOnce = true;
			break;

		case '/':
			next_ch ();
			if (ch == '/' || ch == '*') {
				in_comment ();
				if (newSymbols) DoOnce = true;
				continue;
			} else {
				symbol.kind = Symbol.DIVIDE;
			   back_ch ();
			}
			break;

	// Identifiers, numbers and strings
		case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g':
		case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n':
		case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u':
		case 'v': case 'w': case 'x': case 'y': case 'z':
		case 'A': case 'B': case 'C': case 'D': case 'E': case 'F': case 'G':
		case 'H': case 'I': case 'J': case 'K': case 'L': case 'M': case 'N':
		case 'O': case 'P': case 'Q': case 'R': case 'S': case 'T': case 'U':
		case 'V': case 'W': case 'X': case 'Y': case 'Z':
		case '_':
			in_identifier ();
			break;

		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
			in_number ();
			break;

		// Single character symbols
		case '#':
			symbol.kind = Symbol.HASH;
			break;
		case '\'':
			symbol.kind = Symbol.QUOTE;
			break;

	  case '"':
	    in_string();
      break;

		case '+':
			symbol.kind = Symbol.PLUS;
			break;

		case '*':
			symbol.kind = Symbol.STAR;
			break;

		case '%':
			symbol.kind = Symbol.MODULUS;
			break;

		case '^':
			symbol.kind = Symbol.CIRCUMFLEX;
			break;

		case '~':
			symbol.kind = Symbol.SINE;
			break;

		case '?':
			symbol.kind = Symbol.QUESTION;
			break;

		case ',':
			symbol.kind = Symbol.COMMA;
			break;

		case '(':
			symbol.kind = Symbol.LROUND;
			break;

		case ')':
			symbol.kind = Symbol.RROUND;
			break;

		case '{':
			symbol.kind = Symbol.LCURLY;
			break;

		case '}':
			symbol.kind = Symbol.RCURLY;
			break;

		case ']':
			symbol.kind = Symbol.RSQUARE;
			break;

		case ';':
			symbol.kind = Symbol.SEMICOLON;
			break;

		case '@':
			symbol.kind = Symbol.AT;
			break;
			
	  case '\\':
		   symbol.kind =Symbol.BACKSLASH;
		   break;
			
		// Double character symbols
	   case '[':
	   	next_ch();
	   	if (ch == ']')
	   		symbol.kind = Symbol.ALWAYS;
	     else {
		 	   symbol.kind = Symbol.LSQUARE;
		 	   back_ch();
	     }
			break;

		case '|':
			next_ch ();
			if (ch == '|')
				symbol.kind = Symbol.OR;
			else {
				symbol.kind = Symbol.BITWISE_OR;
				back_ch ();
			}
			break;

		case '&':
			next_ch ();
			if (ch == '&')
				symbol.kind = Symbol.AND;
			else {
				symbol.kind = Symbol.BITWISE_AND;
				back_ch ();
			}
			break;

		case '!':
			next_ch ();
			if (ch == '=')
				symbol.kind = Symbol.NOT_EQUAL;
			else {
				symbol.kind = Symbol.PLING;
				back_ch ();
			}
			break;

		case '<':
			next_ch ();
			if (ch == '=')
				symbol.kind = Symbol.LESS_THAN_EQUAL;
			else if (ch == '<')
				symbol.kind = Symbol.SHIFT_LEFT;
		  else if (ch == '>')
				symbol.kind = Symbol.EVENTUALLY;
			else if (ch == '-') {
				next_ch();
				if (ch == '>') 
						symbol.kind = Symbol.EQUIVALENT;
				else {
						symbol.kind = Symbol.LESS_THAN;
				     back_ch ();
              back_ch ();
				}
			} else {
				symbol.kind = Symbol.LESS_THAN;
				back_ch ();
			}
			break;

		case '>':
			next_ch ();
			if (ch == '=')
				symbol.kind = Symbol.GREATER_THAN_EQUAL;
			else if (ch == '>')
				symbol.kind = Symbol.SHIFT_RIGHT;
			else {
				symbol.kind = Symbol.GREATER_THAN;
				back_ch ();
			}
			break;

		case '=':
			next_ch ();
			if (ch == '=')
				symbol.kind = Symbol.EQUALS;
			else {
				symbol.kind = Symbol.BECOMES;
				back_ch ();
			}
			break;

		case '.':
			next_ch ();
			if (ch == '.')
				symbol.kind = Symbol.DOT_DOT;
			else {
				symbol.kind = Symbol.DOT;
				back_ch ();
			}
			break;

		case '-':
			next_ch ();
			if (ch == '>')
				symbol.kind = Symbol.ARROW;
			else {
				symbol.kind = Symbol.MINUS;
				back_ch ();
			}
			break;

		case ':':
			next_ch ();
			if (ch == ':')
				symbol.kind = Symbol.COLON_COLON;
			else {
				symbol.kind = Symbol.COLON;
				back_ch ();
			}
			break;


		default:
			error ("unexpected character encountered");
		} // endswitch

	}
  symbol.endPos = input.getMarker();
	return symbol;

}

/* push back interface to Lex */

	private Symbol current = null;
	private Symbol buffer  = null;

   public Symbol next_symbol () {
      if (buffer == null) {
          current = (Symbol)in_sym ();
       } else {
          current = buffer;
          buffer = null;
       }
          return current;
     }

    public void push_symbol () {
        buffer = current;
    }
    
    public Symbol current() {
    	   return current;
    }


//*****************************************************************************************
}