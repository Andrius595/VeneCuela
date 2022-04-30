grammar VeneCuela;

program
 : statement+ EOF
 ;

statement
 : assignment
 | systemFunctionCall
 | ifElseIfElseStatement
 | ifElseStatement
 | ifStatement
 ;

assignment
 : IDENTIFIER TYPE '=' expression
 | IDENTIFIER TYPE '=' assignment
 ;

systemFunctionCall
 : PRINT '(' expression ')'                             #printFunctionCall
 ;

ifElseIfElseStatement: 'suppose' 'that' '(' expression ')' 'then' block 'otherwise' 'that' '(' expression ')' 'then' block 'other' block;
ifElseStatement: 'suppose' 'that' '(' expression ')' 'then' block 'other' block;
ifStatement: 'suppose' 'that' '(' expression ')' 'then' block ;

block: '{' statement* '}' ;

constant: INTEGER | BOOLEAN | STRING ;

expression
 : constant                                             #constantExpression
 | IDENTIFIER                                           #identifierExpression
 | '(' expression ')'                                   #parenthesesExpression
 | booleanUnaryOp expression                            #booleanUnaryOpExpression
 | expression booleanBinaryOp expression                #booleanBinaryOpExpression
 | expression numericMultiOp expression                 #numericMultiOpExpression
 | expression numericAddOp expression                   #numericAddOpExpression
 | expression stringBinaryOp expression                 #stringBinaryOpExpression
 ;

booleanUnaryOp : '!' ;

booleanBinaryOp : '||' | '&&' ;

numericMultiOp : '*' | '/' | '%' ;

numericAddOp : '+' | '-' ;

stringBinaryOp : '..' ; //concat

PRINT : 'print';

INTEGER : [0-9]+ ;
BOOLEAN : 'true' | 'false' ;
STRING : ["] ( ~["\r\n\\] | '\\' ~[\r\n] )* ["] ;

TYPE : 'INT' | 'STRING' | 'BOOLEAN' ;

IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]* ;

COMMENT : ( '//' ~[\r\n]* | '/*' .*? '*/' ) -> skip ;

WS : [ \t\f\r\n]+ -> skip ;