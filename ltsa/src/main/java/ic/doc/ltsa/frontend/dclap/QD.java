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

// dclap/quickdraw.java
// Macintosh QuickDraw info for PICT data
// d.gilbert, dec. 1996


package ic.doc.ltsa.frontend.dclap;
// pack edu.indiana.bio.dclap;

import java.lang.String;




class QuickDrawFont {
  int val;
  String name;
  QuickDrawFont(int v, String n) { val= v; name= n; }
  int fontval(String n) {
    if (name.equalsIgnoreCase(n)) return val;
    else return -1;
    }
}


public class QD {
  // QuickDraw constants

    // font styles
  public final static int bold = 1;
  public final static int italic = 2;
  public final static int underline = 4;
  public final static int outline = 8;
  public final static int shadow = 16;
  public final static int condense = 32;
  public final static int extend = 64;

    // pen draw modes
  public final static int patCopy = 8;
  public final static int patOr = 9;
  public final static int patXor = 10;
  public final static int patBic = 11;
  public final static int hilite = 50;
    // also notPatXXX

  // MacDraw picComments
  public final static int picDwgBeg    = 130;
  public final static int picDwgEnd    = 131;
  public final static int picGrpBeg    = 140;
  public final static int picGrpEnd    = 141;
  public final static int textBegin    = 150;
  public final static int textEnd      = 151;
  public final static int textCenter   = 154;
  // picComments for laserwriter
  public final static int dashedLine  = 180;
  public final static int dashedStop  = 181;
  public final static int setLineWidth  = 182;


  public final static int version2 = 0x2ff;
    // enum PICTops // djang java for no enums !
  public final static int oNOP= 0;
  public final static int oClip= 1;
  public final static int oBkPat= 2;
  public final static int oTxFont= 3;
  public final static int oTxFace= 4;
  public final static int oTxMode= 5;
  public final static int oSpExtra= 6;
  public final static int oPnSize= 7;
  public final static int oPnMode= 8;
  public final static int oPnPat= 9;
  public final static int oFillPat= 10;
  public final static int oOvSize= 11;
  public final static int oOrigin= 12;
  public final static int oTxSize= 13;
  public final static int oFgColor= 14;
  public final static int oBkColor= 15;
  public final static int oTxRatio= 0x10;
  public final static int oVersion= 0x11;
  public final static int oBkPixPat= 0x12;
  public final static int oPnPixPat= 0x13;
  public final static int oFillPixPat= 0x14;
  public final static int oPnLocHFrac= 0x15;
  public final static int oChExtra= 0x16;
  //r17,r18,r19,
  public final static int oRGBFgCol= 0x1a;
  public final static int oRGBBkCol= 0x1b;
  public final static int oHiliteMode= 0x1c;
  public final static int oHiliteColor= 0x1d;
  public final static int oDefHilite= 0x1e;
  public final static int oOpColor= 0x1f;
  public final static int oLine= 0x20;
  public final static int oLineFrom= 0x21;
  public final static int oShortLine= 0x22;
  public final static int oShortLineFrom= 0x23;
  //r24,r25,r26,r27,
  public final static int oLongText= 0x28;
  public final static int oDHText= 0x29;
  public final static int oDVText= 0x2a;
  public final static int oDHDVText= 0x2b;
  public final static int oFontName= 0x2c;
  //r2d,
  //public final static int or2e_mov,
  //r2f,
  public final static int oframeRect= 0x30;
  public final static int opaintRect= 0x31;
  public final static int oeraseRect= 0x32;
  public final static int oinvertRect= 0x33;
  public final static int ofillRect= 0x34;
  //r35,r36,r37,
  public final static int oframeSameRect= 0x38;
  public final static int opaintSameRect= 0x39;
  public final static int oeraseSameRect= 0x3a;
  public final static int oinvertSameRect= 0x3b;
  public final static int ofillSameRect= 0x3c;
  //r3d,r3e,r3f,
  public final static int oframeRRect= 0x40;
  public final static int opaintRRect= 0x41;
  public final static int oeraseRRect= 0x42;
  public final static int oinvertRRect= 0x43;
  public final static int ofillRRect= 0x44;
  //r45,r46,r47,
  public final static int oframeSameRRect= 0x48;
  public final static int opaintSameRRect= 0x49;
  public final static int oeraseSameRRect= 0x4a;
  public final static int oinvertSameRRect= 0x4b;
  public final static int ofillSameRRect= 0x4c;
  //r4d,r4e,r4f,
  public final static int oframeOval= 0x50;
  public final static int opaintOval= 0x51;
  public final static int oeraseOval= 0x52;
  public final static int oinvertOval= 0x53;
  public final static int ofillOval= 0x54;
  //r55,r56,r57,
  public final static int oframeSameOval= 0x58;
  public final static int opaintSameOval= 0x59;
  public final static int oeraseSameOval= 0x5a;
  public final static int oinvertSameOval= 0x5b;
  public final static int ofillSameOval= 0x5c;
  //r5d,r5e,r5f,
  public final static int oframeArc= 0x60;
  public final static int opaintArc= 0x61;
  public final static int oeraseArc= 0x62;
  public final static int oinvertArc= 0x63;
  public final static int ofillArc= 0x64;
  //r65,r66,r67,
  public final static int oframeSameArc= 0x68;
  public final static int opaintSameArc= 0x69;
  public final static int oeraseSameArc= 0x6a;
  public final static int oinvertSameArc= 0x6b;
  public final static int ofillSameArc= 0x6c;
  //r6d,r6e,r6f,
  public final static int oframePoly= 0x70;
  public final static int opaintPoly= 0x71;
  public final static int oerasePoly= 0x72;
  public final static int oinvertPoly= 0x73;
  public final static int ofillPoly= 0x74;
  //r75,r76,r77,
  public final static int oframeSamePoly= 0x78;
  public final static int opaintSamePoly= 0x79;
  public final static int oeraseSamePoly= 0x7a;
  public final static int oinvertSamePoly= 0x7b;
  public final static int ofillSamePoly= 0x7c;
  //r7d,r7e,r7f,
  public final static int oframeRgn= 0x80;
  public final static int opaintRgn= 0x81;
  public final static int oeraseRgn= 0x82;
  public final static int oinvertRgn= 0x83;
  public final static int ofillRgn= 0x84;
  //r85,r86,r87,
  public final static int oframeSameRgn= 0x88;
  public final static int opaintSameRgn= 0x89;
  public final static int oeraseSameRgn= 0x8a;
  public final static int oinvertSameRgn= 0x8b;
  public final static int ofillSameRgn= 0x8c;
  //r8d,r8e,r8f,
  public final static int oBitsRect= 0x90;
  public final static int oBitsRgn= 0x91;
  //r92,r93,r94,r95,r96,r97,
  public final static int oPackBitsRect= 0x98;
  public final static int oPackBitsRgn= 0x99;
  public final static int oOpcode9A= 0x9a;
  //r9b,r9c,r9d,r9e,r9f,
  public final static int oShortComment= 0xa0;
  public final static int oLongComment= 0xa1;
  // a2..af,b0..fe unused
  public final static int oopEndPic= 0x00ff;
  // 0100..ffff unused
  public final static int oHeaderOp= 0x0c00;

  protected static int fontnum = 101; // value past QuickDrawFont values?

  protected static QuickDrawFont[] QDFonts;

  static {
    int i=0;
    QDFonts= new QuickDrawFont[18]; //! can't let compiler count?!
    QDFonts[i++]= new QuickDrawFont( 0,"Chicago");  // system
    QDFonts[i++]= new QuickDrawFont( 1,"Geneva");   // applic
    QDFonts[i++]= new QuickDrawFont( 2,"New York");
    QDFonts[i++]= new QuickDrawFont( 3,"Geneva");
    QDFonts[i++]= new QuickDrawFont( 4,"Monaco");
    QDFonts[i++]= new QuickDrawFont(13,"Zapf Dingbats");
    QDFonts[i++]= new QuickDrawFont(14,"Bookman");
    QDFonts[i++]= new QuickDrawFont(16,"Palatino");
    QDFonts[i++]= new QuickDrawFont(18,"Zapf Chancery");
    QDFonts[i++]= new QuickDrawFont(19,"Souvenir");
    QDFonts[i++]= new QuickDrawFont(20,"Times");
    QDFonts[i++]= new QuickDrawFont(21,"Helvetica");
    QDFonts[i++]= new QuickDrawFont(22,"Courier");
    QDFonts[i++]= new QuickDrawFont(23,"Symbol");
    QDFonts[i++]= new QuickDrawFont(26,"Lubalin Graph");
    QDFonts[i++]= new QuickDrawFont(33,"Avant Garde");
    QDFonts[i++]= new QuickDrawFont(21,"SansSerif");
    QDFonts[i++]= new QuickDrawFont(20,"Serif");
    }

  public static int getQuickDrawFontNum(String name) {
    for (int i=0; i<QDFonts.length; i++) {
      int num= QDFonts[i].fontval(name);
      if (num>=0) return num;
      }
    return -1;
    }

}