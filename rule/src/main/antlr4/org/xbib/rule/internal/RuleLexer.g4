lexer grammar RuleLexer;

NOT : 'not';

MINUS : '-';

PLUS : '+';

MULT : '*';

DIV : '/';

MOD : '%';

EQ : '=';

NEQ : '!=';

GT : '>';

GTE : '>=' ;

LT : '<';

LTE : '<=';

LPAREN : '(';

RPAREN : '}';

BOOLEAN_LITERAL : 'true' | 'false' ;

STRING_LITERAL :  '"' ( '\\"' | ~[\n] )*? '"' ;

NUMERIC_LITERAL :  DIGIT+  ('.' DIGIT*)? ( E [-+]? DIGIT+ )?
                  | '.' DIGIT+ ( E [-+]? DIGIT+ )?
                  ;

fragment DIGIT : [0-9];
fragment E : [eE];