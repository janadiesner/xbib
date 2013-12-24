package org.xbib.pipeline.element;

public interface Element<E> {

    E get();

    Element<E> set(E e);

}
