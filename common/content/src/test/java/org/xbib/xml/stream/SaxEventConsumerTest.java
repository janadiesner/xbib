package org.xbib.xml.stream;

import org.testng.annotations.Test;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

public class SaxEventConsumerTest {

    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    @Test
    public void testSaxEventConsumer() throws Exception {
        SaxEventConsumer c = new SaxEventConsumer(new DefaultHandler());
        List<Namespace> namespaces = newLinkedList();
        c.add(eventFactory.createStartElement(new QName("http://localhost/elems/", "elementname"),
                getAttributes().iterator(), namespaces.iterator()));
        c.add(eventFactory.createEndElement( new QName("http://localhost/elems/", "elementname"),
                namespaces.iterator()));
    }

    private List<Attribute> getAttributes() {
        List<Attribute> list = newLinkedList();
        QName q = new QName("http://localhost/attrs/", "attributename");
        list.add(eventFactory.createAttribute(q, "attributevalue"));
        return list;
    }

}
