 package org.xbib.sql.parser.expression.operators.conditional;

import org.xbib.sql.parser.expression.BinaryExpression;
import org.xbib.sql.parser.expression.Expression;
import org.xbib.sql.parser.expression.ExpressionVisitor;

 public class AndExpression extends BinaryExpression {
     public AndExpression(Expression leftExpression, Expression rightExpression) {
         setLeftExpression(leftExpression);
         setRightExpression(rightExpression);
     }
     public void accept(ExpressionVisitor expressionVisitor) {
         expressionVisitor.visit(this);
     }

     public String getStringExpression() {
         return "AND";
     }
 }
