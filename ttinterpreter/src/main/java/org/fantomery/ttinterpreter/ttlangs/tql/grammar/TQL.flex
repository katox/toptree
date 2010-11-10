/* A lexical analyzer for grammar of Top Tree Query Language TQL. */

package ttlangs.tql;

import java_cup.runtime.*;
import numbers.InfiniteNumber.*;
import numbers.InfiniteInteger;
import numbers.InfiniteReal;
import ttlangs.tql.TQLError;

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
  "("                           { return symbol(sym.LPAREN); }
  ")"                           { return symbol(sym.RPAREN); }
  "["                           { return symbol(sym.LBRACKET); }
  "]"                           { return symbol(sym.RBRACKET); }
  "{"                           { return symbol(sym.LBRACE); }
  "}"                           { return symbol(sym.RBRACE); }
  "L"                           { return symbol(sym.L); }
  "R"                           { return symbol(sym.R); }
  ";"                           { return symbol(sym.SEMI); }
  ","                           { return symbol(sym.COMMA); }
  "="                           { return symbol(sym.ASSIGN); }
  "ImINF"                       { return symbol(sym.IMINF, new InfiniteInteger(Finiteness.NEGATIVE_INFINITY)); }
  "IpINF"                       { return symbol(sym.IPINF, new InfiniteInteger(Finiteness.POSITIVE_INFINITY)); }
  "RmINF"                       { return symbol(sym.RMINF, new InfiniteReal(Finiteness.NEGATIVE_INFINITY)); }
  "RpINF"                       { return symbol(sym.RPINF, new InfiniteReal(Finiteness.POSITIVE_INFINITY)); }
  "+"                           { return symbol(sym.PLUS); }
  "-"                           { return symbol(sym.MINUS); }
  {INumber}                     { return symbol(sym.I_NUMBER, new InfiniteInteger(new Integer(yytext()))); }
  {RNumber}                     { return symbol(sym.R_NUMBER, new InfiniteReal(new Double(yytext()))); }
  "true"                        { return symbol(sym.TRUE,  new Boolean(true)); }
  "false"                       { return symbol(sym.FALSE, new Boolean(false)); }
  "node"                        { return symbol(sym.NODE); }
  "edge"                        { return symbol(sym.EDGE); }
  "exit"                        { return symbol(sym.EXIT); }
  "info"                        { return symbol(sym.INFO); }
  "--"                          { return symbol(sym.LINK); }
  "##"                          { return symbol(sym.CUT); }
  \"                            { string.setLength(0); yybegin(STRING); }
  {Variable}                    { return symbol(sym.VARIABLE, new String(yytext())); }
}
 

/* exclusive state: preparing STRING_LITERAL terminal */
<STRING> {
  \"                            { yybegin(YYINITIAL); 
                                  return symbol(sym.STRING_LITERAL, new String(string.toString())); }
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
.                               { throw new TQLError("Illegal character <" + yytext() + "> at position "
                                                     + (yycolumn+1) + "." ); }
