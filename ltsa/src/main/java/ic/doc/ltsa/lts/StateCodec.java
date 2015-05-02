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

/**
* Compresses integer vector of a a composite machine state into
* a bit packed byte vector
*
*/

public class StateCodec {
	
	int [] bitSize;   //size in bits in byte array
	int NBIT;         //number of bits required to encode state
	int NBYTE;        //number of bytes required to encode state
	static int masks[] ={0x0,0x1,0x3,0x7,0xF,0x1F,0x3F,0x7F,
		                  0xFF,0x1FF,0x3FF,0x7FF,0xFFF,0x1FFF,0x3FFF,0x7FFF,
		                  0xFFFF,0x1FFFF,0x3FFFF,0x7FFFF,0xFFFFF,0x1FFFFF,0x3FFFFF,0x7FFFFF, 
	                    0xFFFFFF,0x1FFFFFF,0x3FFFFFF,0x7FFFFFF,0xFFFFFFF,0x1FFFFFFF,0x3FFFFFFF,0x7FFFFFFF};
  int boundaries[];
	                    	
public StateCodec(int[] sm) {	
		bitSize = new int[sm.length];
	  NBIT =0;
	  int longwords = 1;
		for (int i=sm.length-1; i>=0; --i) {
			bitSize[i] = nbits(sm[i]-1);
			if ((NBIT+bitSize[i])>longwords*64) {NBIT = longwords*64; ++longwords;}  //pad to 64 bit boundary
			NBIT+=bitSize[i];
		}
		NBYTE = NBIT/8;
		if (NBIT%8>0) ++NBYTE;
		boundaries = new int[longwords];
		int bits =0;
		int boundary =0;
		for (int i=sm.length-1; i>=0; --i) {
			if ((bits+bitSize[i])<= 64) {
				   bits+=bitSize[i];
			} else {
				 boundaries[boundary]=i+1;
				 bits = bitSize[i];
				 ++boundary;
			}
		}
		boundaries[boundary]=0;     
	}
	
	private void longToBytes(byte[] code, long x, int start, int finish) {
		  for(int i=start; i<finish; ++i) {
		  	   code[i] |= (byte)x;
		  	   x = x >>>8;
		  }
	}
	
	private long bytesToLong(byte[] code,int start,int finish) {
		 long x =0;
		 for(int i=finish-1; i>=start; --i) {
		  	   x |= (long)(code[i])& 0xFFL;
		  	   if (i>start) x = x << 8;
		  }
		  return x;
	}
	
	/**
	* return number of bits to code statespace
	*/
	public int bits(){
		int nb =0;
		for (int i=0; i<bitSize.length; ++i)
			nb+=bitSize[i];
	  return nb;
	}
	
	/**
	* return a coded zero state
	*/
	
	public byte[] zero() {
		return new byte[NBYTE];
	}
	
	/**
	* encode states into byte array
	*/
	public byte[] encode(int[] states) {
		byte code[] = new byte[NBYTE];
		int start = bitSize.length-1;
		int insertfrom = NBYTE;
		for (int lw =0; lw<boundaries.length; ++lw) {
			long x = 0;
			for (int i=start; i>=boundaries[lw]; --i) {
				  if (states[i]<0) 
				     {return null;}
				  x |=states[i];
				  if (i>boundaries[lw]) x = x<<bitSize[i-1];
			}
			int t = insertfrom-8;
			if (t<0) t=0;
			longToBytes(code,x,t,insertfrom);
			insertfrom = t;
			//System.out.println("Encode x: "+x);
			start = boundaries[lw]-1;
		}		
			return code;
	}
		
	/**
	* decode states from byte array
	*/
  public int[] decode(byte[] code) {
  	  int states[] = new int[bitSize.length+1];
  	  	int finish = bitSize.length;
		int getfrom = NBYTE;
		for (int lw =0; lw<boundaries.length; ++lw) {
			int t = getfrom-8;
			if (t<0) t=0;
  	     long x = bytesToLong(code,t,getfrom);
  	  		//System.out.println("Decode x: "+x);
  	  		for (int i=boundaries[lw]; i<finish; ++i) {
       	states[i] = (int)x & masks[bitSize[i]];
         x = x>>>bitSize[i];
  	  		}
  	  		getfrom = t;
  	  		finish=boundaries[lw];
  	  }
  	  return states;
  }
  
  public static int hash(byte[] code) {
  	  long h = 0;
  	  for (int i=0; i<code.length; ++i) {
  	  	  h = h*127 + code[i];
  	  }
  	  int ih = (int)((h ^ (h >>> 32)));
  	  return ih & 0x7FFFFFFF;
  }
  
  public static long hashLong(byte[] code) {
  	  long h = 0;
  	  for (int i=0; i<code.length; ++i) {
  	  	  h = h*255 + code[i];
  	  }
  	  return h;
  }

  
  public static boolean equals(byte[] code1, byte[] code2) {
  	  if (code1==null && code2==null) return true;
  	  if (code1==null || code2==null) return false;
  	  if (code1.length!=code2.length) return true;
  	  for (int i =0; i<code1.length; ++i) {
  	  	   if (code1[i]!=code2[i]) return false;
  	  }
  	  	return true;
  }
  
  /**
  * calculate number of bits necessary to represent i
  */
  private int nbits (int i) {
  	  int count =0;
  	  while (i!=0) {
  	  	   i = i>>>1;
  	  	   ++count;
  	  }
  	  return count;
  }

  /* test harness
  public static void main ( String[] args ) {
  	    int test[] = {1024, 1024, 1024,1024,1024,1024,1024,1024,1024,1024, 1024, 1024,1024,1024,1024,1024,1024,1024};
			StateCodec c = new StateCodec(test);
			System.out.print("bitSize: {");
			for(int i=0; i<c.bitSize.length;++i) System.out.print(" "+c.bitSize[i]+",");
		  System.out.println("}");
			System.out.println("NBIT: "+c.NBIT);
			System.out.println("NBYTE: "+c.NBYTE);
			System.out.print("boundaries: {");
			for(int i=0; i<c.boundaries.length;++i) System.out.print(" "+c.boundaries[i]+",");
		  System.out.println("}");
			int state[] = {999,2,999,0,4,5,6,7,1,999,1,999,999,4,5,6,7,1};
			byte code[] = c.encode(state);
			int result[] = c.decode(code);
			System.out.print("code: {");
			for(int i=0; i<code.length;++i) System.out.print(" "+code[i]+",");
		  System.out.println("}");		
		  System.out.print("result: {");
			for(int i=0; i<c.bitSize.length;++i) System.out.print(" "+result[i]+",");
		  System.out.println("}");
		  System.out.println("hash: "+hash(code));
  }
*/
  
}
  
  
  	
  	