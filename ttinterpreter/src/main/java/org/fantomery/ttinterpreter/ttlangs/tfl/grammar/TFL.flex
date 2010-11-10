/* A lexical analyzer for grammar of Top Tree Friendly Language TFL. */

package org.fantomery.ttinterpreter.ttlangs.tfl;

import java_cup.runtime.*;
import org.fantomery.ttinterpreter.ttlangs.tfl.TFLCompiler.*;

%%

%class lexer
%apiprivate
%unicode
%cup
%line
%column

%{
  StringBuffer string = new StringBuffer();

  private Symbol symbol(int type) {
    return new Symbol(type, yyline, yycolumn);
  }
  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]
WhiteSpace     = {LineTerminator} | [ \t\f]

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}
TraditionalComment   = "/*" ~"*/"
EndOfLineComment     = "//" {InputCharacter}* {LineTerminator}

/* form of comments */
Variable = [a-zA-Z][a-zA-Z0-9_]*

/* accepted forms of numeric terminals */
INumber = [0-9]+
RNumber = [0-9]+\.[0-9]+


/* preparing any string into String class */
%xstate STRING


%%


/* terminals */
<YYINITIAL> {
  "algorithm"                   { return symbol(sym.ALGORITHM); }
  "vertex"                      { return symbol(sym.VERTEX); }
  "cluster"                     { return symbol(sym.CLUSTER); }
  "var"                         { return symbol(sym.VAR); }
  "integer"                     { return symbol(sym.DATA_TYPE, DataType.INTEGER); }
  "real"                        { return symbol(sym.DATA_TYPE, DataType.REAL); }
  "string"                      { return symbol(sym.DATA_TYPE, DataType.STRING); }
  "boolean"                     { return symbol(sym.DATA_TYPE, DataType.BOOLEAN); }
  "array"                       { return symbol(sym.ARRAY); }
  "create"                      { return symbol(sym.CREATE); }
  "destroy"                     { return symbol(sym.DESTROY); }
  "c"                           { return symbol(sym.C); }
  "left"                        { return symbol(sym.LEFT); }
  "right"                       { return symbol(sym.RIGHT); }
  "."                           { return symbol(sym.DOT); }
  "="                           { return symbol(sym.ASSIGN); }
  "+="                          { return symbol(sym.PLUS_ASSIGN); }
  "-="                          { return symbol(sym.MINUS_ASSIGN); }
  "*="                          { return symbol(sym.TIMES_ASSIGN); }
  "/="                          { return symbol(sym.DIVIDE_ASSIGN); }
  "&="                          { return symbol(sym.AMPERSAND_ASSIGN); }
  "true"                        { return symbol(sym.TRUE); }
  "false"                       { return symbol(sym.FALSE); }
  "("                           { return symbol(sym.LPAREN); }
  ")"                           { return symbol(sym.RPAREN); }
  "["                           { return symbol(sym.LBRACKET); }
  "]"                           { return symbol(sym.RBRACKET); }
  "{"                           { return symbol(sym.LBRACE); }
  "}"                           { return symbol(sym.RBRACE); }
  "&&"                          { return symbol(sym.AND); }
  "||"                          { return symbol(sym.OR); }
  "=="                          { return symbol(sym.EQUAL); }
  "!="                          { return symbol(sym.NOT_EQUAL); }
  "<"                           { return symbol(sym.LESS); }
  ">"                           { return symbol(sym.GREATER); }
  "<="                          { return symbol(sym.LESS_EQUAL); }
  ">="                          { return symbol(sym.GREATER_EQUAL); }
  "&"                           { return symbol(sym.AMPERSAND); }
  "ImINF"                       { return symbol(sym.IMINF, new String("Finiteness.NEGATIVE_INFINITY")); }
  "IpINF"                       { return symbol(sym.IPINF, new String("Finiteness.POSITIVE_INFINITY")); }
  "RmINF"                       { return symbol(sym.RMINF, new String("Finiteness.NEGATIVE_INFINITY")); }
  "RpINF"                       { return symbol(sym.RPINF, new String("Finiteness.POSITIVE_INFINITY")); }
  {INumber}                     { return symbol(sym.I_NUMBER, new String(yytext())); }
  {RNumber}                     { return symbol(sym.R_NUMBER, new String(yytext())); }
  "MAX"                         { return symbol(sym.MAX); }
  "MIN"                         { return symbol(sym.MIN); }
  "EXISTS"                      { return symbol(sym.EXISTS); }
  ","                           { return symbol(sym.COMMA); }
  "+"                           { return symbol(sym.PLUS); }
  "-"                           { return symbol(sym.MINUS); }
  "*"                           { return symbol(sym.TIMES); }
  "/"                           { return symbol(sym.DIVIDE); }
  "if"                          { return symbol(sym.IF); }
  "elseif"                      { return symbol(sym.ELSEIF); }
  "else"                        { return symbol(sym.ELSE); }
  "path"                        { return symbol(sym.PATH); }
  "point"                       { return symbol(sym.POINT); }
  ";"                           { return symbol(sym.SEMI); }
  "a"                           { return symbol(sym.A); }
  "b"                           { return symbol(sym.B); }
  "child"                       { return symbol(sym.CHILD); }
  "common"                      { return symbol(sym.COMMON); }
  "border"                      { return symbol(sym.BORDER); }
  "join"                        { return symbol(sym.JOIN); }
  "split"                       { return symbol(sym.SPLIT); }
  "path_child"                  { return symbol(sym.PATH_CHILD); }
  "point_child"                 { return symbol(sym.POINT_CHILD); }
  "path_parent"                 { return symbol(sym.PATH_PARENT); }
  "point_parent"                { return symbol(sym.POINT_PARENT); }
  "path_and_path"               { return symbol(sym.PATH_AND_PATH); }     
  "path_and_point"              { return symbol(sym.PATH_AND_POINT); }    
  "point_and_path"              { return symbol(sym.POINT_AND_PATH); }    
  "point_and_point"             { return symbol(sym.POINT_AND_POINT); }   
  "lpoint_over_rpoint"          { return symbol(sym.LPOINT_OVER_RPOINT); }
  "rpoint_over_lpoint"          { return symbol(sym.RPOINT_OVER_LPOINT); }
  "lpoint_and_rpoint"           { return symbol(sym.LPOINT_AND_RPOINT); } 
  "selectQuestion"              { return symbol(sym.SELECT_QUESTION); }       
  "select"                      { return symbol(sym.SELECT); }       
  \"                            { string.setLength(0); yybegin(STRING); }
  {Variable}                    { return symbol(sym.VARIABLE, new String(yytext())); }
}
 

/* exclusive state: preparing STRING_LITERAL terminal */
<STRING> {
  \"                            { yybegin(YYINITIAL); 
                                  return symbol(sym.STRING_LITERAL, new String("\"" + string.toString() + "\"")); }
  [^\n\r\"\\]+                  { string.append( yytext() ); }
  \\t                           { string.append('\t'); }
  \\n                           { string.append('\n'); }
  \\r                           { string.append('\r'); }
  \\\"                          { string.append('\"'); }
  \\                            { string.append('\\'); }
}


/* comments */
{Comment}                       { /* ignore */ }
 
/* whitespace */
{WhiteSpace}                    { /* ignore */ }

/* error fallback */
.                           { throw new Error("Illegal character <" + yytext() + "> at line(position) "
                                              + yyline + "(" + yycolumn + ")!" ); }
