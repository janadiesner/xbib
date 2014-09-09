parser grammar RuleParser;

options {
  tokenVocab = RuleLexer;
}

rulelang : expr EOF ;

expr
    : LPAREN expr RPAREN                 # parens
    | (NOT) expr                         # not
    | (MINUS|PLUS) expr                  # sign
    | expr (MULT|DIV|MOD) expr          # multiplying
    | expr (PLUS|MINUS) expr             # adding
    | expr (EQ|NEQ|GT|GTE|LT|LTE) expr  # relational
    | literal=(BOOLEAN_LITERAL
              |STRING_LITERAL
              |NUMERIC_LITERAL)          # literal
    ;