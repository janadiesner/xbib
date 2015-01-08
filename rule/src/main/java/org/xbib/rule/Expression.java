package org.xbib.rule;

public interface Expression {

    boolean interpret(Binding binding);
}