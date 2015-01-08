package org.xbib.rule;

public interface Dispatcher {

    void fire(Expression expression, Binding binding);
}