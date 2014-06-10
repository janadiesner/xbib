 package org.xbib.sql.parser.expression.operators.relational;

import org.xbib.sql.parser.statement.select.SubSelect;

 public interface ItemsListVisitor {

     void visit(SubSelect subSelect);

     void visit(ExpressionList expressionList);
 }
