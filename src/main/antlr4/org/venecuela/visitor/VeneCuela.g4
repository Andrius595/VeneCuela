grammar VeneCuela;

program
 : line+ EOF
 ;

line
 : functionDeclaration
 | statement
 ;

statement
 : variableDeclaration
 | assignment
 | functionCall
 | systemFunctionCall
 | block
 | ifElseIfElseStatement
 | ifElseStatement
 | ifStatement
 | returnStatement
 ;

functionDeclaration
 : 'func' IDENTIFIER '(' paramList? ')' functionBody
 ;

 paramList
  : (TYPE IDENTIFIER) (',' (TYPE IDENTIFIER))*
  ;

assignment
 : IDENTIFIER '=' expression
 | IDENTIFIER '=' assignment
 ;

variableDeclaration
 : TYPE IDENTIFIER '=' expression
 ;


functionCall
 : IDENTIFIER '(' expressionList? ')'
 ;

systemFunctionCall
 : PRINT '(' expression ')'                             #printFunctionCall
 | EMIGRATE '(' IDENTIFIER ')'                          #emigrateFunctionCall
 | IMMIGRATE '(' IDENTIFIER ')'                         #immigrateFunctionCall
 ;

ifElseIfElseStatement: 'suppose' 'that' '(' expression ')' 'then' block 'otherwise' 'that' '(' expression ')' 'then' block 'other' block;
ifElseStatement: 'suppose' 'that' '(' expression ')' 'then' block 'other' block;
ifStatement: 'suppose' 'that' '(' expression ')' 'then' block ;

block: '{' statement* '}' ;

functionBody: '{' statement* '}' ;

constant: INTEGER | BOOLEAN | STRING ;

returnStatement : 'return' expression? ;

expressionList
 : expression (',' expression)*
 ;

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

EMIGRATE : 'emigrate' ;
IMMIGRATE : 'immigrate' ;

INTEGER : [0-9]+ ;
BOOLEAN : 'true' | 'false' ;
STRING : ["] ( ~["\r\n\\] | '\\' ~[\r\n] )* ["] ;

TYPE : 'bolivar' | 'cuerda' | 'boo' ;

IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]* ;

COMMENT : ( '//' ~[\r\n]* | '/*' .*? '*/' ) -> skip ;

WS : [ \t\f\r\n]+ -> skip ;