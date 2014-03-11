package org.xbib.tools.aleph;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.MarcXchangeListener;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Stack;

public class AlephPublish2MarcXML extends AlephPublish2Tar implements MarcXchangeListener {

    private final static Logger logger = LoggerFactory.getLogger(AlephPublish2MarcXML.class.getName());

    private final static XMLInputFactory factory = XMLInputFactory.newInstance();

    private MarcXchangeListener listener;

    public AlephPublish2MarcXML setListener(MarcXchangeListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected void process(String s) {
        if (listener != null) {
            try {
                try (StringReader sr = new StringReader(s)) {
                    XMLEventReader xmlReader = factory.createXMLEventReader(sr);
                    Stack<Field> stack = new Stack();
                    while (xmlReader.hasNext()) {
                        processEvent(stack, xmlReader.peek());
                        xmlReader.nextEvent();
                    }
                }
            } catch (XMLStreamException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void beginRecord(String format, String type) {
        if (listener != null) {
            listener.beginRecord(format, type);
        }
    }

    public void endRecord() {
        if (listener != null) {
            listener.endRecord();
        }
    }

    public void leader(String label) {
        if (listener != null) {
            listener.leader(label);
        }
    }

    public void beginControlField(Field designator) {
        if (listener != null) {
            listener.beginControlField(designator);
        }
    }

    public void endControlField(Field designator) {
        if (listener != null) {
            listener.endControlField(designator);
        }
    }

    public void beginDataField(Field designator) {
        if (listener != null) {
            listener.beginDataField(designator);
        }
    }

    public void endDataField(Field designator) {
        if (listener != null) {
            listener.endDataField(designator);
        }
    }

    public void beginSubField(Field designator) {
        if (listener != null) {
            listener.beginSubField(designator);
        }
    }

    public void endSubField(Field designator) {
        if (listener != null) {
            listener.endSubField(designator);
        }
    }

    private StringBuilder sb = new StringBuilder();

    private boolean inRecord = false;

    private void processEvent(Stack<Field> stack, XMLEvent event) {
        if (event.isStartElement()) {
            StartElement element = (StartElement) event;
            String localName = element.getName().getLocalPart();
            Iterator<?> it = element.getAttributes();
            String format = null;
            String type = null;
            String tag = null;
            char ind1 = '\u0000';
            char ind2 = '\u0000';
            char code = '\u0000';
            while (it.hasNext()) {
                Attribute attr = (Attribute) it.next();
                QName attributeName = attr.getName();
                String attributeLocalName = attributeName.getLocalPart();
                String attributeValue = attr.getValue();
                switch (attributeLocalName) {
                    case "tag":
                        tag = attributeValue;
                        break;
                    case "ind1":
                        ind1 = attributeValue.charAt(0);
                        if (ind1 == '-') {
                            ind1 = ' '; // replace illegal blank symbols
                        }
                        break;
                    case "ind2":
                        ind2 = attributeValue.charAt(0);
                        if (ind2 == '-') {
                            ind2 = ' '; // replace illegal blank symbols
                        }
                        break;
                    case "code":
                        code = attributeValue.charAt(0);
                        break;
                    case "format":
                        format = attributeValue;
                        break;
                    case "type":
                        type = attributeValue;
                        break;
                }
            }
            switch (localName) {
                case "subfield": {
                    Field f = stack.peek();
                    Field subfield = new Field(f.tag(), f.indicator(), Character.toString(code));
                    stack.push(subfield);
                    beginSubField(subfield);
                    break;
                }
                case "datafield": {
                    Field field = ind2 != '\u0000'
                            ? new Field(tag, Character.toString(ind1) + Character.toString(ind2))
                            : new Field(tag, Character.toString(ind1));
                    stack.push(field);
                    beginDataField(field);
                    break;
                }
                case "controlfield": {
                    Field field = new Field(tag);
                    stack.push(field);
                    beginControlField(field);
                    break;
                }
                case "record": {
                    if (!inRecord) {
                        beginRecord(format != null ? format : "AlephPublish", type);
                        inRecord = true;
                    }
                    break;
                }
            }
        } else if (event.isCharacters()) {
            Characters c = (Characters) event;
            if (!c.isIgnorableWhiteSpace()) {
                if (sb.length() == 0) {
                    sb.append(" "); // for subfield ID
                }
                sb.append(c.getData());
            }
        } else if (event.isEndElement()) {
            EndElement element = (EndElement) event;
            String localName = element.getName().getLocalPart();
            switch (localName) {
                case "subfield":
                    String subfieldId = sb.substring(0,1);
                    String data = sb.substring(1);
                    stack.peek().subfieldId(subfieldId).data(data);
                    endSubField(stack.pop());
                    break;
                case "datafield":
                    // can't have data
                    endDataField(stack.pop());
                    break;
                case "controlfield":
                    stack.peek().data(sb.toString());
                    endControlField(stack.pop());
                    break;
                case "leader":
                    leader(sb.toString());
                    break;
                case "record":
                    if (inRecord) {
                        endRecord();
                        inRecord = false;
                    }
                    break;
            }
            sb.setLength(0);
        }
    }
}
