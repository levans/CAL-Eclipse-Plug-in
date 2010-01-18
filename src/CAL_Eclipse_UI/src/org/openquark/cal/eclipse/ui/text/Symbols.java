/*******************************************************************************
 * Copyright (c) 2006 Business Objects Software Limited and others.
 * All rights reserved. 
 * This file is made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Business Objects Software Limited - initial API and implementation based on Eclipse 3.1.2 code for
 *                             /org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/Symbols.java
 *                           Eclipse source is available at: http://www.eclipse.org/downloads/
 *******************************************************************************/

/*
 * Symbols.java
 * Creation date: Feb 8, 2006.
 * By: Edward Lam
 */
package org.openquark.cal.eclipse.ui.text;



/**
 * Symbols for the heuristic cal scanner.
 * @author Edward Lam
 */
public class Symbols {

    public static final int TokenEOF = -1;
    public static final int TokenLBRACE = 1;
    public static final int TokenRBRACE = 2;
    public static final int TokenLBRACKET = 3;
    public static final int TokenRBRACKET = 4;
    public static final int TokenLPAREN = 5;
    public static final int TokenRPAREN = 6;
    public static final int TokenSEMICOLON = 7;
    public static final int TokenOTHER = 8;
    public static final int TokenCOLON = 9;
    public static final int TokenQUESTIONMARK = 10;
    public static final int TokenCOMMA = 11;
    public static final int TokenPERIOD = 12;
    public static final int TokenEQUAL = 13;
    public static final int TokenLESSTHAN = 14;
    public static final int TokenGREATERTHAN = 15;
    public static final int TokenDASH = 16;
    public static final int TokenBAR = 17;
    public static final int TokenBACKSLASH = 18;
    public static final int TokenIF = 109;
    public static final int TokenDO = 1010;
    public static final int TokenFOR = 1011;
    public static final int TokenTRY = 1012;
    public static final int TokenCASE = 1013;
    public static final int TokenELSE = 1014;
    public static final int TokenBREAK = 1015;
    public static final int TokenCATCH = 1016;
    public static final int TokenWHILE = 1017;
    public static final int TokenRETURN = 1018;
    public static final int TokenSTATIC = 1019;
    public static final int TokenSWITCH = 1020;
    public static final int TokenFINALLY = 1021;
    public static final int TokenSYNCHRONIZED = 1022;
    public static final int TokenGOTO = 1023;
    public static final int TokenDEFAULT = 1024;
    public static final int TokenNEW = 1025;
    public static final int TokenCLASS = 1026;
    public static final int TokenINTERFACE = 1027;
    public static final int TokenENUM = 1028;
    public static final int TokenCONSIDENT = 2000;
    public static final int TokenOTHERIDENT = 2001;
    
    public static final int TokenPUBLIC = 1050;
    public static final int TokenPROTECTED = 1051;
    public static final int TokenPRIVATE = 1052;
    
    public static final int TokenMODULE = 1060;
    public static final int TokenFRIEND = 1061;
    
    public static final int TokenIMPORT = 1070;
    public static final int TokenUSING = 1071;
    public static final int TokenFUNCTION = 1072;
    public static final int TokenDATACONSTRUCTOR = 1073;
    public static final int TokenTYPECONSTRUCTOR = 1074;
    public static final int TokenTYPECLASS = 1075;
    
    public static final int TokenLET = 1080;
    public static final int TokenIN = 1081;
    public static final int TokenTHEN = 1082;           // matches if/else
    public static final int TokenOF = 1083;             // matches case
    
    public static final int TokenPRIMITIVE = 1090;
    
    public static final int TokenINSTANCE = 1100;       // class
    public static final int TokenWHERE = 1101;
    
    public static final int TokenDATA = 1200;
    public static final int TokenDERIVING = 1201;
    public static final int TokenFOREIGN = 1202;
    public static final int TokenUNSAFE = 1203;
    public static final int TokenJVM = 1204;

}


    // used:

//  => :: ->
//  ! \ | && || ++ $ # ` _ ; : , = < >
    
//  public static final int TokenEOF = -1;
//  public static final int TokenLBRACE = 1;
//  public static final int TokenLBRACKET = 3;
//  public static final int TokenLPAREN = 5;
//  public static final int TokenRBRACE = 2;
//  public static final int TokenRBRACKET = 4;
//  public static final int TokenRPAREN = 6;
//  public static final int TokenSEMICOLON = 7;
//  public static final int TokenCOLON = 9;
//  public static final int TokenCOMMA = 11;
//  public static final int TokenEQUAL = 12;
//  public static final int TokenLESSTHAN = 13;
//  public static final int TokenGREATERTHAN = 14;
    
//  public static final int TokenCASE = 1013;
//  public static final int TokenOF = 1083;             // matches case
//  public static final int TokenCLASS = 1026;

//  public static final int TokenOTHER = 8;
//  public static final int TokenIDENT = 2000;  // identifier
//
//  public static final int TokenPUBLIC = 1050;
//  public static final int TokenPROTECTED = 1051;
//  public static final int TokenPRIVATE = 1052;

//  public static final int TokenMODULE = 1060;
//  public static final int TokenFRIEND = 1061;
//
//  public static final int TokenIMPORT = 1070;
//  public static final int TokenUSING = 1071;
//  public static final int TokenFUNCTION = 1072;
//  public static final int TokenDATACONSTRUCTOR = 1073;
//  public static final int TokenTYPECONSTRUCTOR = 1074;
//  public static final int TokenTYPECLASS = 1075;
//
//  public static final int TokenLET = 1080;
//  public static final int TokenIN = 1081;
//  public static final int TokenIF = 109;
//  public static final int TokenTHEN = 1082;           // matches if/else
//  public static final int TokenELSE = 1014;
//
//  public static final int TokenPRIMITIVE = 1090;
//
//  public static final int TokenINSTANCE = 1100;       // class
//  public static final int TokenWHERE = 1101;
//
//  public static final int TokenDATA = 1200;
//  public static final int TokenDERIVING = 1201;
//  public static final int TokenFOREIGN = 1202;
//  public static final int TokenUNSAFE = 1203;
//  public static final int TokenJVM = 1204;


    // remove:
    
//  public static final int TokenQUESTIONMARK = 10;
//  public static final int TokenDO = 1010;
//  public static final int TokenFOR = 1011;
//  public static final int TokenTRY = 1012;
//  public static final int TokenNEW = 1025;
//  public static final int TokenENUM = 1028;
//  public static final int TokenGOTO = 1023;
//  public static final int TokenBREAK = 1015;
//  public static final int TokenCATCH = 1016;
//  public static final int TokenWHILE = 1017;
//  public static final int TokenRETURN = 1018;
//  public static final int TokenSTATIC = 1019;
//  public static final int TokenSWITCH = 1020;
//  public static final int TokenDEFAULT = 1024;
//  public static final int TokenFINALLY = 1021;
//  public static final int TokenINTERFACE = 1027;
//  public static final int TokenSYNCHRONIZED = 1022;

