
grammar Expression;

@header {
package org.example;
}

expression
    : logicalOrExpression EOF
    ;

logicalOrExpression
    : logicalAndExpression (OR logicalAndExpression)*
    ;

logicalAndExpression
    : equalityExpression (AND equalityExpression)*
    ;

equalityExpression
    : relationalExpression (('==' | '!=') relationalExpression)*
    ;

relationalExpression
    : additiveExpression (('<=' | '>=' | '<' | '>') additiveExpression)*
    ;

additiveExpression
    : multiplicativeExpression
    | additiveExpression '+' multiplicativeExpression
    | additiveExpression '-' multiplicativeExpression
    ;

multiplicativeExpression
    : primaryExpression
    | multiplicativeExpression '*' primaryExpression
    | multiplicativeExpression '/' primaryExpression
    ;

primaryExpression
    : NUMBER
    | STRING
    | functionCall
    | fieldReference
    | '(' logicalOrExpression ')'
    ;

fieldReference
    : IDENTIFIER ('.' IDENTIFIER)*
    ;

functionCall
    : IDENTIFIER '(' argumentList? ')'
    ;

argumentList
    : logicalOrExpression (',' logicalOrExpression)*
    ;

AND
    : [aA][nN][dD]
    ;

OR
    : [oO][rR]
    ;

NUMBER
    : [0-9]+ ('.' [0-9]+)?
    ;

STRING
    : '"' (~["\\] | '\\' .)* '"'
    ;

IDENTIFIER
    : [a-zA-Z_][a-zA-Z0-9_]*
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
