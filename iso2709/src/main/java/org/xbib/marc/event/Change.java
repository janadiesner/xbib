package org.xbib.marc.event;

public interface Change {

    Change setChange(Object prev, Object next);

    Object getPrev();

    Object getNext();
}
