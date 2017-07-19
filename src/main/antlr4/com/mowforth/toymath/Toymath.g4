grammar Toymath;

main:
  MAIN OPEN_BRACE expression CLOSE_BRACE;

expression:
  INT PLUS INT #AddValues
  | INT PLUS expression #AddExpr;

// lexer rules

MAIN: 'main';
OPEN_BRACE: '{';
CLOSE_BRACE: '}';

INT: [0-9]+;
PLUS: '+';

WS: [ \t\r\n\f]+ -> skip;
