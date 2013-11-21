
package org.xbib.standardnumber;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ISBNRangeMessageConfigurator {

    private final Stack<StringBuilder> content;

    private final List<String> ranges;

    private String prefix;

    private String rangeBegin;

    private String rangeEnd;

    private int length;

    private boolean valid;

    public ISBNRangeMessageConfigurator() {
        content = new Stack<StringBuilder>();
        ranges = new ArrayList<String>();
        length = 0;
        try {
            InputStream in = getClass().getResourceAsStream("/org/xbib/standardnumber/RangeMessage.xml");
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader xmlReader = xmlInputFactory.createXMLEventReader(in);
            while (xmlReader.hasNext()) {
                processEvent(xmlReader.peek());
                xmlReader.nextEvent();
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void processEvent(XMLEvent e) {
        switch (e.getEventType()) {
            case XMLEvent.START_ELEMENT: {
                StartElement element = e.asStartElement();
                String name = element.getName().getLocalPart();
                if ("RegistrationGroups".equals(name)) {
                    valid = true;
                }
                content.push(new StringBuilder());
                break;
            }
            case XMLEvent.END_ELEMENT: {
                EndElement element = e.asEndElement();
                String name = element.getName().getLocalPart();
                String v = content.pop().toString();
                if ("Prefix".equals(name)) {
                    prefix = v;
                }
                if ("Range".equals(name)) {
                    int pos = v.indexOf('-');
                    if (pos > 0) {
                        rangeBegin = v.substring(0, pos);
                        rangeEnd = v.substring(pos + 1);
                    }
                }
                if ("Length".equals(name)) {
                    length = Integer.parseInt(v);
                }
                if ("Rule".equals(name)) {
                    if (valid && rangeBegin != null && rangeEnd != null) {
                        if (length > 0) {
                            ranges.add(prefix + "-" + rangeBegin.substring(0, length));
                            ranges.add(prefix + "-" + rangeEnd.substring(0, length));
                        }
                    }
                }
                break;
            }
            case XMLEvent.CHARACTERS: {
                Characters c = (Characters) e;
                if (!c.isIgnorableWhiteSpace()) {
                    String text = c.getData().trim();
                    if (text.length() > 0 && !content.empty()) {
                        content.peek().append(text);
                    }
                }
                break;
            }
        }
    }

    public List<String> getRanges() {
        return ranges;
    }

}
