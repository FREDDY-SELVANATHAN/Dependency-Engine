grammar Expression;

@header {
package org.example;
}

expression
    : additiveExpression EOF
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
    | fieldReference
    | functionCall
    | '(' additiveExpression ')'
    ;

fieldReference
    : IDENTIFIER ('.' IDENTIFIER)*
    ;

functionCall
    : IDENTIFIER '(' argumentList? ')'
    ;

argumentList
    : additiveExpression (',' additiveExpression)*
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